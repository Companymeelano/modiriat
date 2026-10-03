#!/usr/bin/env python3
"""
Live comparison harness for the four manager sections (خانه / گزارش‌ها / هوش مدیریتی / نظارت بر فروش).

For every data block behind those pages it runs the predicate the app uses TODAY (Jalali date stored
in a char(10) but filtered with TRY_CONVERT(date,...)+DATEADD(...), visitor key resolved as rdf/RDF/id/ID)
and the FIXED predicate (Persian string range, visitor key vis_rdf), then reports rows / whether the
amounts agree / milliseconds / errors for both.

Privacy: only counts, booleans and timings are written - never a customer name, product name or amount.
"""
import json
import os
import re
import sys
import time

import pytds

SRC = "MEELANO-Android/app/src/main/java/ir/meelano/android/MainActivity.java"
OUT = sys.argv[1] if len(sys.argv) > 1 else "atiran-check.json"
S_KEY = 73


def hidden(name, src):
    m = re.search(r"int\[\] " + name + r" = \{([^}]*)\}", src)
    return "".join(chr(int(x) ^ S_KEY) for x in m.group(1).replace("\n", "").split(",") if x.strip())


class H:
    def __init__(self, cn):
        self.cn = cn
        self.out = {"blocks": {}, "errors": []}

    def one(self, sql, params=None):
        """Return (first_row, ms) or (None, ms) with the error recorded."""
        t = time.perf_counter()
        try:
            cur = self.cn.cursor()
            cur.execute(sql, params)
            row = cur.fetchone()
            cur.close()
            return row, int((time.perf_counter() - t) * 1000)
        except Exception as ex:
            self.out["errors"].append({"sql": sql[:90], "err": str(ex)[:220]})
            return None, int((time.perf_counter() - t) * 1000)

    def pair(self, block, old_sql, new_sql):
        """Run the app's predicate and the fixed predicate; publish counts, agreement, timings."""
        o_row, o_ms = self.one(old_sql)
        n_row, n_ms = self.one(new_sql)
        rec = {"old_ms": o_ms, "new_ms": n_ms}
        try:
            rec["old_rows"] = int(o_row[0]) if o_row and o_row[0] is not None else None
        except Exception:
            rec["old_rows"] = None
        try:
            rec["new_rows"] = int(n_row[0]) if n_row and n_row[0] is not None else None
        except Exception:
            rec["new_rows"] = None
        if o_row and n_row and len(o_row) > 1 and len(n_row) > 1:
            try:
                o_sum, n_sum = float(o_row[1] or 0), float(n_row[1] or 0)
                rec["old_has_amount"] = o_sum > 0
                rec["new_has_amount"] = n_sum > 0
                rec["amount_delta_pct"] = (round((n_sum - o_sum) / o_sum * 100, 2) if o_sum else None)
            except Exception:
                pass
        self.out["blocks"][block] = rec
        return rec


# ---------------------------------------------------------------- dialect helpers
def old_range(col, latest, rng):
    """Exactly what ManagerAnalytics.rangeCondition builds today."""
    d = "TRY_CONVERT(date,TRY_CONVERT(nvarchar(30),%s))" % col
    q = "N'%s'" % latest
    if rng == 0:
        return "(TRY_CONVERT(nvarchar(30),%s)=%s OR LEFT(TRY_CONVERT(nvarchar(30),%s),10)=LEFT(%s,10))" % (col, q, col, q)
    if rng == 1:
        return "%s>=DATEADD(day,-6,TRY_CONVERT(date,%s))" % (d, q)
    if rng == 2:
        return "%s>=DATEADD(month,-1,TRY_CONVERT(date,%s))" % (d, q)
    return "%s>=DATEADD(month,-12,TRY_CONVERT(date,%s))" % (d, q)


def new_range(col, frm, to):
    """Fixed: plain varchar range on the Persian 'YYYY/MM/DD' text - sargable and loss-free."""
    return "%s>='%s' AND %s<='%s'" % (col, frm, col, to)


def main():
    src = open(SRC, encoding="utf-8").read()
    cn = pytds.connect(server=hidden("S_HOST", src), port=1433, database=hidden("S_DB", src),
                       user=hidden("S_USER", src), password=hidden("S_PASS", src),
                       login_timeout=20, timeout=180, autocommit=True)
    cn.cursor().execute("SET TRANSACTION ISOLATION LEVEL READ UNCOMMITTED; SET LOCK_TIMEOUT 4000;")
    h = H(cn)

    def val(sql, params=None):
        row, ms = h.one(sql, params)
        return (row[0] if row else None), ms

    # ---------- Persian today + the boundaries the fixed code computes in Java ----------
    today = str(val("SELECT CAST(dbo.UDF_Gregorian_To_Persian(GETDATE()) AS nvarchar(30))")[0]).strip()
    def pd(days):
        return str(val("SELECT CAST(dbo.UDF_Gregorian_To_Persian(DATEADD(day,%d,GETDATE())) AS nvarchar(30))" % days)[0]).strip()
    from7, from30, from365 = pd(-6), pd(-29), pd(-364)
    month_ago = pd(-30)
    prev_from, prev_to = pd(-59), pd(-30)
    h.out["today"] = today
    h.out["boundaries"] = {"from7": from7, "from30": from30, "from365": from365}

    latest = str(val("SELECT MAX(NULLIF(CONVERT(nvarchar(20),[date]),N'')) FROM dbo.sailfact")[0] or "").strip()
    h.out["latest_sale_date"] = latest
    # the app anchors ranges on the latest sale; the fix anchors on the server's Persian today
    anchor = today if today >= latest else latest

    A = "x.active='t'"          # sailfact/buyfact active flag (char, 't')
    SD = "ISNULL(x.Deleted,0)=0"  # sailfact soft delete (bit)

    # ---------- 1. sales / purchases per range: old vs fixed ----------
    for rng, name in ((0, "today"), (1, "7d"), (2, "30d"), (3, "12m")):
        frm = {0: anchor, 1: from7, 2: from30, 3: from365}[rng]
        h.pair("sales_" + name,
               "SELECT COUNT_BIG(1), ISNULL(SUM(TRY_CONVERT(decimal(19,2),x.[all])),0) FROM dbo.sailfact x WITH (NOLOCK) WHERE %s AND %s AND %s"
               % (old_range("x.[date]", latest, rng), A, SD),
               "SELECT COUNT_BIG(1), ISNULL(SUM(TRY_CONVERT(decimal(19,2),x.[all])),0) FROM dbo.sailfact x WITH (NOLOCK) WHERE %s AND %s AND %s"
               % (new_range("x.[date]", frm, anchor), A, SD))
    h.pair("purchases_30d",
           "SELECT COUNT_BIG(1), ISNULL(SUM(TRY_CONVERT(decimal(19,2),x.[all])),0) FROM dbo.buyfact x WITH (NOLOCK) WHERE %s AND %s"
           % (old_range("x.[DATE]", latest, 2), A),
           "SELECT COUNT_BIG(1), ISNULL(SUM(TRY_CONVERT(decimal(19,2),x.[all])),0) FROM dbo.buyfact x WITH (NOLOCK) WHERE %s AND %s"
           % (new_range("x.[DATE]", from30, anchor), A))

    # ---------- 2. why rows disappear: the TRY_CONVERT(date) hole ----------
    hole, _ = h.one("SELECT COUNT_BIG(1) FROM dbo.sailfact WHERE TRY_CONVERT(date,TRY_CONVERT(nvarchar(30),[date])) IS NULL")
    h.out["rows_lost_by_date_conversion"] = int(hole[0]) if hole else None
    dup, _ = h.one("SELECT COUNT_BIG(1), COUNT(DISTINCT CAST(shfacfo AS nvarchar(60))) FROM dbo.sailfact")
    h.out["invoice_no_total_vs_distinct"] = [int(dup[0]), int(dup[1])] if dup else None

    # ---------- 3. weekday mix (byDay) : DATEADD/DATEPART on a Jalali date is meaningless ----------
    h.pair("weekday_mix",
           "SELECT COUNT(DISTINCT DATEPART(dw,TRY_CONVERT(date,TRY_CONVERT(nvarchar(30),x.[date])))) FROM dbo.sailfact x WITH (NOLOCK) WHERE %s AND %s"
           % (old_range("x.[date]", latest, 2), A),
           "SELECT COUNT(DISTINCT dbo.dif_date(x.[date])) FROM dbo.sailfact x WITH (NOLOCK) WHERE %s AND %s"
           % (new_range("x.[date]", from30, anchor), A))

    # ---------- 4. visitor share: the app looks for visitors.rdf (does not exist) ----------
    h.pair("visitor_share",
           """SELECT COUNT_BIG(1) FROM dbo.sailfact x WITH (NOLOCK)
              LEFT JOIN dbo.visitors v ON TRY_CONVERT(nvarchar(100),v.[rdf])=TRY_CONVERT(nvarchar(100),x.vis_rdf)
              WHERE %s AND %s""" % (old_range("x.[date]", latest, 2), A),
           """SELECT COUNT_BIG(DISTINCT v.vis_rdf) FROM dbo.sailfact x WITH (NOLOCK)
              LEFT JOIN dbo.visitors v ON TRY_CONVERT(nvarchar(100),v.[vis_rdf])=TRY_CONVERT(nvarchar(100),x.vis_rdf)
              WHERE %s AND %s""" % (new_range("x.[date]", from30, anchor), A))
    vk, _ = h.one("SELECT COUNT(*) FROM sys.columns WHERE object_id=OBJECT_ID('dbo.visitors') AND name IN ('rdf','RDF','id','ID')")
    h.out["visitors_has_app_expected_key"] = bool(vk and vk[0])

    # ---------- 5. checks: the app compares a Jalali date to GETDATE() ----------
    h.pair("check_buckets",
           """SELECT COUNT_BIG(1) FROM dbo.getchk WHERE TRY_CONVERT(date,TRY_CONVERT(nvarchar(30),[sarresid]))<CONVERT(date,GETDATE())""",
           """SELECT COUNT_BIG(1) FROM dbo.getchk WHERE NULLIF(LTRIM(RTRIM([sarresid])),'')<'%s'""" % anchor)
    sr, _ = h.one("SELECT TOP (3) CAST([sarresid] AS nvarchar(20)) FROM dbo.getchk ORDER BY Rdf_ DESC")
    h.out["check_sample_due_dates_shape"] = "persian" if sr and re.match(r"^1[34]\d{2}/", str(sr[0] or "")) else "other"

    # ---------- 6. aging (collection) - the function exists, only the range anchor matters ----------
    h.pair("aging_unpaid",
           "SELECT COUNT_BIG(1) FROM dbo.sailfact WHERE tasvieh='f' AND NULLIF([t_date],'') IS NOT NULL AND dbo.dif_date_alan([t_date])<=0",
           "SELECT COUNT_BIG(1) FROM dbo.sailfact WITH (NOLOCK) WHERE tasvieh='f' AND NULLIF([t_date],'') IS NOT NULL AND dbo.dif_date_alan([t_date])<=0")

    # ---------- 7. receivables / debtors / customer activity ----------
    h.pair("debtors",
           "SELECT COUNT_BIG(1), ISNULL(SUM(TRY_CONVERT(decimal(19,2),c.[man])),0) FROM dbo.CUSTOMERS c WHERE TRY_CONVERT(decimal(19,2),c.[man])>0",
           "SELECT COUNT_BIG(1), ISNULL(SUM(c.[man]),0) FROM dbo.CUSTOMERS c WITH (NOLOCK) WHERE c.[man]>0")
    h.pair("customer_activity_30d",
           """SELECT COUNT_BIG(1) FROM dbo.CUSTOMERS c WHERE EXISTS (SELECT 1 FROM dbo.sailfact s WITH (NOLOCK)
                  WHERE s.shmo=c.SHMO AND %s)""" % old_range("s.[date]", latest, 2),
           """SELECT COUNT_BIG(1) FROM dbo.CUSTOMERS c WITH (NOLOCK) WHERE EXISTS (SELECT 1 FROM dbo.sailfact s WITH (NOLOCK)
                  WHERE s.shmo=c.SHMO AND %s)""" % new_range("s.[date]", from30, anchor))

    # ---------- 8. products / profit: join + Jalali range ----------
    h.pair("top_products_30d",
           """SELECT COUNT(DISTINCT d.SHKA) FROM dbo.subsailfact d WITH (NOLOCK) JOIN dbo.sailfact s WITH (NOLOCK) ON s.shfacfo=d.shfacfo
              WHERE %s AND d.active='t'""" % old_range("s.[date]", latest, 2),
           """SELECT COUNT(DISTINCT d.SHKA) FROM dbo.subsailfact d WITH (NOLOCK) JOIN dbo.sailfact s WITH (NOLOCK) ON s.shfacfo=d.shfacfo
              WHERE %s AND d.active='t'""" % new_range("s.[date]", from30, anchor))
    h.pair("product_profit_30d",
           """SELECT COUNT_BIG(1) FROM dbo.subsailfact d WITH (NOLOCK) JOIN dbo.sailfact s WITH (NOLOCK) ON s.shfacfo=d.shfacfo
              JOIN dbo.inventory i WITH (NOLOCK) ON i.shka=d.SHKA
              WHERE %s AND d.active='t' AND i.pure_buy_price IS NOT NULL""" % old_range("s.[date]", latest, 2),
           """SELECT COUNT_BIG(1) FROM dbo.subsailfact d WITH (NOLOCK) JOIN dbo.sailfact s WITH (NOLOCK) ON s.shfacfo=d.shfacfo
              JOIN dbo.inventory i WITH (NOLOCK) ON i.shka=d.SHKA
              WHERE %s AND d.active='t' AND i.pure_buy_price IS NOT NULL""" % new_range("s.[date]", from30, anchor))
    inv, _ = h.one("SELECT SUM(CASE WHEN ISNULL(pure_buy_price,0)<>0 THEN 1 ELSE 0 END), SUM(CASE WHEN ISNULL(buy_price,0)<>0 THEN 1 ELSE 0 END) FROM dbo.inventory")
    h.out["inventory_price_filled"] = {"pure_buy_price": int(inv[0] or 0), "buy_price": int(inv[1] or 0)} if inv else None

    # ---------- 9. stock ledger (ka_act) - what the warehouse/zero-stock cards need ----------
    h.pair("stock_ledger_join",
           "SELECT COUNT(DISTINCT k.shka) FROM dbo.ka_act k WITH (NOLOCK) JOIN dbo.inventory i WITH (NOLOCK) ON i.shka=k.shka WHERE k.active='t'",
           "SELECT COUNT(DISTINCT k.shka) FROM dbo.ka_act k WITH (NOLOCK) JOIN dbo.inventory i WITH (NOLOCK) ON i.shka=k.shka WHERE k.active='t'")

    # ---------- 10. warehouses / goals / periods / credit / feed ----------
    h.pair("warehouses", "SELECT COUNT_BIG(1) FROM dbo.anbars a LEFT JOIN dbo.inventory_anbars ia ON ia.rdf_anbars=a.rdf_anbar",
                        "SELECT COUNT_BIG(1) FROM dbo.anbars a WITH (NOLOCK) LEFT JOIN dbo.inventory_anbars ia WITH (NOLOCK) ON ia.rdf_anbars=a.rdf_anbar")
    h.pair("visitor_goals", "SELECT COUNT_BIG(1) FROM dbo.vis_goals g LEFT JOIN dbo.visitors v ON v.[rdf]=g.vis_rdf",
                           "SELECT COUNT_BIG(1) FROM dbo.vis_goals g WITH (NOLOCK) LEFT JOIN dbo.visitors v WITH (NOLOCK) ON v.[vis_rdf]=g.vis_rdf")
    bz, _ = h.one("SELECT COUNT_BIG(1) FROM dbo.baze WHERE TRY_CONVERT(date,TRY_CONVERT(nvarchar(30),sta))<=TRY_CONVERT(date,'%s') AND TRY_CONVERT(date,TRY_CONVERT(nvarchar(30),end_))>=TRY_CONVERT(date,'%s')" % (anchor, anchor))
    bz2, _ = h.one("SELECT COUNT_BIG(1) FROM dbo.baze WHERE sta<='%s' AND end_>='%s'" % (anchor, anchor))
    h.out["open_period_row"] = {"old": int(bz[0]) if bz else None, "new": int(bz2[0]) if bz2 else None}
    h.pair("credit_risk", "SELECT COUNT_BIG(1) FROM dbo.Sys_Mandeh_Customer WITH (NOLOCK)",
                         "SELECT COUNT_BIG(1) FROM dbo.Sys_Mandeh_Customer WITH (NOLOCK)")
    h.pair("activity_feed", "SELECT COUNT_BIG(1) FROM dbo.sailfact WHERE %s" % old_range("[date]", latest, 2),
                           "SELECT COUNT_BIG(1) FROM dbo.sailfact WITH (NOLOCK) WHERE %s" % new_range("[date]", from30, anchor))

    # ---------- 11. tables the pages need but that do not exist / are empty ----------
    ex = {}
    for t in ["Visit", "MasirGoals", "vw_customer", "VW_Forush_DarBazeZamani", "ka_act", "putchk", "sys_cus", "cus_image"]:
        r, _ = h.one("SELECT CASE WHEN OBJECT_ID(N'dbo.[%s]') IS NULL THEN 0 ELSE 1 END" % t)
        n, _ = h.one("SELECT COUNT_BIG(1) FROM dbo.[%s] WITH (NOLOCK)" % t) if (r and r[0]) else (None, 0)
        ex[t] = {"exists": bool(r and r[0]), "rows": int(n[0]) if n else None}
    h.out["required_objects"] = ex

    # ---------- 12. round-trip budget: what one page costs today vs batched ----------
    t = time.perf_counter()
    for _ in range(12):
        h.one("SELECT COUNT_BIG(1) FROM dbo.[sailfact]")
    h.out["twelve_separate_queries_ms"] = int((time.perf_counter() - t) * 1000)
    t = time.perf_counter()
    h.one("""SELECT (SELECT COUNT_BIG(1) FROM dbo.sailfact), (SELECT COUNT_BIG(1) FROM dbo.CUSTOMERS),
                    (SELECT COUNT_BIG(1) FROM dbo.inventory), (SELECT COUNT_BIG(1) FROM dbo.getchk),
                    (SELECT COUNT_BIG(1) FROM dbo.buyfact), (SELECT COUNT_BIG(1) FROM dbo.visitors),
                    (SELECT COUNT_BIG(1) FROM dbo.vis_goals), (SELECT COUNT_BIG(1) FROM dbo.sailfact_pish)""")
    h.out["one_batched_query_ms"] = int((time.perf_counter() - t) * 1000)

    json.dump(h.out, open(OUT, "w", encoding="utf-8"), ensure_ascii=False, default=str, indent=1)
    print("check done:", len(h.out["blocks"]), "blocks,", len(h.out["errors"]), "errors")
    print(json.dumps(h.out.get("blocks", {}), ensure_ascii=False)[:2000])


if __name__ == "__main__":
    main()

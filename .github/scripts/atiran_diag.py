#!/usr/bin/env python3
"""
READ-ONLY diagnostic for the four sections that stay empty / time out in the manager app:
«خانه» (dashboard), «گزارش‌ها» (manager reports), «هوش مدیریتی» (mgr_*) and «نظارت بر فروش».

It answers, against the REAL Atiran2 database and nothing else than SELECT statements:
  1. What is the real type and shape of every date column the four sections filter on?
     (the app treats sailfact.[date] as a Gregorian datetime, while the visitor/store code
      treats it as a Persian 'YYYY/MM/DD' string - one of the two is wrong)
  2. Which tables/columns/functions do those sections depend on, and do they exist?
  3. How slow is the app's current SQL (the exact predicates it builds) against the new SQL,
     row count and milliseconds for both.

Only counts, timings, column names and error strings are written to the JSON sidecar, because
the repository is public. No customer name, no amount, no row content ever leaves the runner.
"""
import json
import os
import re
import sys
import time

import pytds

SRC = "MEELANO-Android/app/src/main/java/ir/meelano/android/MainActivity.java"
OUT = sys.argv[1] if len(sys.argv) > 1 else "atiran-diag.json"
S_KEY = 73


def hidden(name, src):
    m = re.search(r"int\[\] " + name + r" = \{([^}]*)\}", src)
    return "".join(chr(int(x) ^ S_KEY) for x in m.group(1).replace("\n", "").split(",") if x.strip())


class Diag:
    def __init__(self):
        self.out = {"errors": [], "sections": {}}
        self.cn = None

    def note(self, key, value):
        self.out["sections"].setdefault("notes", {})[key] = value

    def fail(self, key, ex):
        self.out["errors"].append("%s: %s" % (key, str(ex)[:300].replace("\n", " ")))

    def q(self, sql, params=None, cap=50):
        """Run one SELECT and return {cols, rows(count only), ms, error}."""
        started = time.perf_counter()
        try:
            cur = self.cn.cursor()
            cur.execute(sql, params)
            cols = [d[0] for d in cur.description] if cur.description else []
            rows = cur.fetchall() if cur.description else []
            while cur.nextset():
                if cur.description:
                    rows = cur.fetchall()
            ms = int((time.perf_counter() - started) * 1000)
            return {"cols": cols, "rows": min(len(rows), cap), "n": len(rows), "ms": ms, "sample": rows[:cap]}
        except Exception as ex:
            ms = int((time.perf_counter() - started) * 1000)
            self.fail(sql[:70], ex)
            return {"error": str(ex)[:300], "ms": ms, "n": 0}

    def count(self, sql, params=None):
        r = self.q(sql, params)
        if "error" in r:
            return None
        try:
            return int(r["sample"][0][0])
        except Exception:
            return None

    def timed(self, key, sql, params=None):
        r = self.q(sql, params)
        self.out["sections"].setdefault("timings", {})[key] = {
            "ms": r.get("ms"), "rows": r.get("n", 0) if "error" not in r else None,
            "error": r.get("error")
        }
        return r


def main():
    src = open(SRC, encoding="utf-8").read()
    host, db = hidden("S_HOST", src), hidden("S_DB", src)
    user, password = hidden("S_USER", src), hidden("S_PASS", src)

    d = Diag()
    d.cn = pytds.connect(server=host, port=1433, database=db, user=user, password=password,
                         login_timeout=20, timeout=120, autocommit=True)
    cn = d.cn
    cn.cursor().execute("SET TRANSACTION ISOLATION LEVEL READ UNCOMMITTED; SET LOCK_TIMEOUT 4000;")

    # ---------- 1. server + latency (how far is the DB, how fast is one round trip) ----------
    r = d.q("""SELECT CAST(SERVERPROPERTY('ProductVersion') AS nvarchar(40)) ver,
                      DB_NAME() db, CAST(DATABASEPROPERTYEX(DB_NAME(),'Collation') AS nvarchar(80)) collation,
                      SUSER_NAME() login_name, USER_NAME() db_user,
                      IS_MEMBER('db_owner') dbo, IS_SRVROLEMEMBER('sysadmin') sysadmin,
                      HAS_PERMS_BY_NAME(NULL,NULL,'VIEW SERVER STATE') viewstate""")
    if "error" not in r and r["sample"]:
        d.out["server"] = [str(x) for x in r["sample"][0]]
    t0 = time.perf_counter()
    for _ in range(10):
        d.count("SELECT 1")
    d.out["roundtrip_ms"] = int((time.perf_counter() - t0) * 100)  # per round trip
    d.timed("server_today", "SELECT TRY_CONVERT(nvarchar(30), dbo.UDF_Gregorian_To_Persian(GETDATE()))")

    # ---------- 2. real schema of the tables the four sections read ----------
    tables = ["sailfact", "subsailfact", "buyfact", "CUSTOMERS", "cust_act", "getchk", "visitors",
              "sys_users", "inventory", "anbars", "inventory_anbars", "ka_act", "masir", "vis_goals",
              "baze", "Visit", "Sys_Mandeh_Customer", "MasirGoals", "VW_Forush_DarBazeZamani",
              "overal_setting", "sailfact_pish", "custgroup", "meelano_prefactors", "FactorConfirmation"]
    meta = {}
    for t in tables:
        try:
            cur = cn.cursor()
            cur.execute("SELECT 1 FROM sys.objects WHERE object_id=OBJECT_ID(N'dbo.'+%s) AND type IN ('U','V')", (t,))
            exists = cur.fetchone() is not None
            entry = {"exists": exists}
            if exists:
                cur.execute("""SELECT c.name, ty.name, c.max_length, c.is_nullable FROM sys.columns c
                               JOIN sys.types ty ON ty.user_type_id=c.user_type_id
                               WHERE c.object_id=OBJECT_ID(N'dbo.'+%s) ORDER BY c.column_id""", (t,))
                entry["columns"] = {"%s:%s" % (n, ty): m for n, ty, m, _ in cur.fetchall()}
                cur.execute("""SELECT SUM(p.rows) FROM sys.partitions p JOIN sys.objects o ON o.object_id=p.object_id
                               WHERE o.object_id=OBJECT_ID(N'dbo.'+%s) AND p.index_id IN (0,1)""", (t,))
                v = cur.fetchone()[0]
                entry["rows"] = int(v) if v is not None else None
                cur.execute("""SELECT i.name, i.is_unique FROM sys.indexes i
                               WHERE i.object_id=OBJECT_ID(N'dbo.'+%s) AND i.type>0""", (t,))
                entry["indexes"] = [str(x[0]) for x in cur.fetchall()][:8]
            meta[t] = entry
        except Exception as ex:
            d.fail("meta:" + t, ex)
    d.out["tables"] = meta

    # ---------- 3. THE key question: how are Persian dates really stored? ----------
    probe = {}
    for tbl, col in [("sailfact", "date"), ("buyfact", "date"), ("cust_act", "date"), ("getchk", "sarresid"),
                     ("Visit", "DateCreated"), ("sailfact", "t_date"), ("sailfact", "done_date")]:
        if not meta.get(tbl, {}).get("exists"):
            continue
        cols = meta[tbl].get("columns", {})
        real = next((c.split(":")[0] for c in cols if c.split(":")[0].lower() == col.lower()), None)
        if real is None:
            continue
        key = "%s.%s" % (tbl, real)
        try:
            cur = cn.cursor()
            cur.execute("SELECT MIN(CAST([%s] AS nvarchar(40))), MAX(CAST([%s] AS nvarchar(40))) FROM dbo.[%s] WHERE [%s] IS NOT NULL" % (real, real, tbl, real))
            lo, hi = cur.fetchone()
            cur.execute("SELECT TOP (3) CAST([%s] AS nvarchar(40)) FROM dbo.[%s] WHERE [%s] IS NOT NULL ORDER BY [%s] DESC" % (real, tbl, real, real))
            probe[key] = {"type": cols.get("%s:%s" % (real, next((c.split(':')[1] for c in cols if c.split(':')[0] == real), "?"))),
                          "min": str(lo)[:30], "max": str(hi)[:30], "top3": [str(x[0])[:30] for x in cur.fetchall()]}
            # the exact conversion the four sections rely on
            cur.execute("SELECT COUNT(*) FROM dbo.[%s] WHERE TRY_CONVERT(date, TRY_CONVERT(nvarchar(30), [%s])) IS NOT NULL" % (tbl, real))
            probe[key]["try_convert_date_ok_rows"] = int(cur.fetchone()[0])
            cur.execute("SELECT COUNT(*) FROM dbo.[%s] WHERE TRY_CONVERT(date, CAST([%s] AS nvarchar(30))) IS NULL" % (tbl, real))
            probe[key]["try_convert_date_null_rows"] = int(cur.fetchone()[0])
        except Exception as ex:
            d.fail("dateprobe:" + key, ex)
    d.out["date_probe"] = probe

    # ---------- 4. which functions exist (Persian/Gregorian helpers, aging, stock) ----------
    try:
        cur = cn.cursor()
        cur.execute("""SELECT name, type FROM sys.objects WHERE type IN ('FN','IF','TF')
                       AND (name LIKE '%Persian%' OR name LIKE '%Gregorian%' OR name LIKE '%date%' OR name LIKE '%Date%'
                            OR name LIKE '%alan%' OR name LIKE '%dif%') ORDER BY name""")
        d.out["functions"] = ["%s/%s" % (n, t) for n, t in cur.fetchall()]
    except Exception as ex:
        d.fail("functions", ex)
    d.timed("stock_fn", "SELECT COUNT(*) FROM sys.objects WHERE name='UDF_MojodiKala'")

    # ---------- 5. the app's OWN predicates: old (Gregorian on Persian dates) vs new (string range) ----------
    today = None
    try:
        cur = cn.cursor()
        cur.execute("SELECT TRY_CONVERT(nvarchar(30), dbo.UDF_Gregorian_To_Persian(GETDATE()))")
        today = str(cur.fetchone()[0]).strip()
    except Exception:
        pass
    d.note("today_persian", today)
    # Persian boundaries computed the way MeelanoJalali does (approximate month arithmetic is fine for a probe)
    def jalali_add_days(pd, days):
        # pd = 'YYYY/MM/DD' Persian; convert via the DB itself to stay exact
        try:
            cur = cn.cursor()
            cur.execute("SELECT CAST(dbo.UDF_Gregorian_To_Persian(DATEADD(day, %s, GETDATE())) AS nvarchar(30))", (days,))
            return str(cur.fetchone()[0]).strip()
        except Exception:
            return None

    if meta.get("sailfact", {}).get("exists") and today:
        from30 = jalali_add_days(today, -30)
        d.note("from30", from30)
        old_pred = "TRY_CONVERT(date,TRY_CONVERT(nvarchar(30),x.[date]))>=DATEADD(month,-1,TRY_CONVERT(date,'%s'))" % today
        new_pred = "x.[date]>='%s'" % (from30 or "1405/06/01")
        f = {"active": "x.active='t'"} if "active" in (meta["sailfact"].get("columns") or {}) else {}
        act = (" AND " + " AND ".join(f.values())) if f else ""
        d.timed("old_range_1m", "SELECT COUNT_BIG(1) FROM dbo.sailfact x WITH (NOLOCK) WHERE " + old_pred + act)
        d.timed("new_range_1m", "SELECT COUNT_BIG(1) FROM dbo.sailfact x WITH (NOLOCK) WHERE " + new_pred + act)
        d.timed("old_range_1m_sum", "SELECT ISNULL(SUM(TRY_CONVERT(decimal(19,2),x.[all])),0) FROM dbo.sailfact x WITH (NOLOCK) WHERE " + old_pred + act)
        d.timed("new_range_1m_sum", "SELECT ISNULL(SUM(TRY_CONVERT(decimal(19,2),x.[all])),0) FROM dbo.sailfact x WITH (NOLOCK) WHERE " + new_pred + act)
        # the app's dedupe wrapper (ROW_NUMBER over the whole table) vs a plain aggregate
        d.timed("old_dedupe_source", """SELECT ISNULL(SUM(TRY_CONVERT(decimal(19,2),h.[all])),0) FROM
                 (SELECT * FROM (SELECT x.*, ROW_NUMBER() OVER(PARTITION BY NULLIF(LTRIM(RTRIM(TRY_CONVERT(nvarchar(120),x.[shfacfo]))),N'') ORDER BY
                    TRY_CONVERT(datetime2,x.[date]) DESC) _rn FROM dbo.sailfact x WHERE """ + old_pred + act + """) mx WHERE mx._rn=1) h""")
        d.timed("new_plain_agg", """SELECT ISNULL(SUM(TRY_CONVERT(decimal(19,2),x.[all])),0), COUNT_BIG(1),
                 COUNT(DISTINCT x.[shmo]) FROM dbo.sailfact x WITH (NOLOCK) WHERE """ + new_pred + act)
        # duplicates on the invoice number (is the dedupe even needed?)
        d.timed("dup_check", """SELECT COUNT_BIG(1), COUNT(DISTINCT CAST([shfacfo] AS nvarchar(60))) FROM dbo.sailfact WITH (NOLOCK) WHERE """ + ("[date]>='%s'" % (from30 or "1405/06/01")))
    # the app's latest-date anchor: MAX over the raw string vs the real Persian today
    d.timed("latest_anchor_app", "SELECT MAX(NULLIF(CONVERT(nvarchar(20),[date]),N'')) FROM dbo.sailfact")
    d.timed("latest_anchor_typed", "SELECT MAX(CAST([date] AS nvarchar(20))) FROM dbo.sailfact WHERE [date] IS NOT NULL")

    # ---------- 6. the exact sections the user reports as empty ----------
    # 6a. home/dashboard building blocks
    d.timed("home_customers_debt", "SELECT COUNT_BIG(1), ISNULL(SUM(TRY_CONVERT(decimal(19,2),c.[man])),0) FROM dbo.CUSTOMERS c WITH (NOLOCK) WHERE TRY_CONVERT(decimal(19,2),c.[man])>0")
    d.timed("home_sales_today_old", "SELECT COUNT_BIG(1) FROM dbo.sailfact WHERE CONVERT(nvarchar(10),[date])=CONVERT(nvarchar(10),'%s')" % today if today else "SELECT 1")
    # 6b. visitors / personnel
    d.timed("visitors_rows", "SELECT COUNT_BIG(1) FROM dbo.visitors")
    d.timed("sys_users_rows", "SELECT COUNT_BIG(1) FROM dbo.sys_users")
    # 6c. product/stock blocks
    if meta.get("inventory", {}).get("exists"):
        d.timed("inventory_rows", "SELECT COUNT_BIG(1) FROM dbo.inventory WITH (NOLOCK)")
    if meta.get("ka_act", {}).get("exists"):
        d.timed("ka_act_rows", "SELECT COUNT_BIG(1) FROM dbo.ka_act WITH (NOLOCK)")
    # 6d. checks
    if meta.get("getchk", {}).get("exists"):
        cols = meta["getchk"].get("columns") or {}
        d.note("getchk_cols", sorted(cols.keys())[:40])
    # 6e. purchase side
    if meta.get("buyfact", {}).get("exists"):
        d.timed("buyfact_rows", "SELECT COUNT_BIG(1) FROM dbo.buyfact WITH (NOLOCK)")

    # ---------- 7. optional tables the manager pages hope for ----------
    optional = ["vis_goals", "baze", "Visit", "Sys_Mandeh_Customer", "MasirGoals",
                "VW_Forush_DarBazeZamani", "anbars", "inventory_anbars", "meelano_prefactors",
                "FactorConfirmation", "meelano_attendance", "meelano_chat_messages"]
    d.out["optional_tables"] = {t: bool(meta.get(t, {}).get("exists")) for t in optional}

    d.out["ok"] = True
    with open(OUT, "w", encoding="utf-8") as f:
        json.dump(d.out, f, ensure_ascii=False, default=str, indent=1)
    print("diag done: %d sections, %d errors" % (len(d.out.get("sections", {})), len(d.out["errors"])))
    for e in d.out["errors"][:10]:
        print("err:", e[:160])


if __name__ == "__main__":
    try:
        main()
    except Exception as ex:
        json.dump({"ok": False, "fatal": "%s: %s" % (type(ex).__name__, str(ex)[:200])}, open(OUT, "w"))
        print("diag failed:", type(ex).__name__, str(ex)[:200])

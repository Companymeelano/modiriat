#!/usr/bin/env python3
"""
Second live check: the exact facts the rewrite depends on.

1. sailfact duplicate structure -> the correct unique key for invoice aggregates.
2. filter flags (active/Deleted/Status/ismodify) -> which rows are real sales.
3. getchk real due-date column (sardate) and the cheque buckets built from Persian today.
4. ka_act act_id semantics + the cost of the stock OUTER APPLY.
5. the batched KPI queries the rewrite uses, timed against the live server.

Only counts, booleans and timings are written.
"""
import json
import re
import sys
import time

import pytds

SRC = "MEELANO-Android/app/src/main/java/ir/meelano/android/MainActivity.java"
OUT = sys.argv[1] if len(sys.argv) > 1 else "atiran-check2.json"
S_KEY = 73
STOCK_OUT = "20,22,5,19,18,48,26,85,133"


def hidden(name, src):
    m = re.search(r"int\[\] " + name + r" = \{([^}]*)\}", src)
    return "".join(chr(int(x) ^ S_KEY) for x in m.group(1).replace("\n", "").split(",") if x.strip())


def main():
    src = open(SRC, encoding="utf-8").read()
    cn = pytds.connect(server=hidden("S_HOST", src), port=1433, database=hidden("S_DB", src),
                       user=hidden("S_USER", src), password=hidden("S_PASS", src),
                       login_timeout=20, timeout=180, autocommit=True)
    cn.cursor().execute("SET TRANSACTION ISOLATION LEVEL READ UNCOMMITTED; SET LOCK_TIMEOUT 4000;")
    out = {"errors": []}

    def q(sql, params=None):
        try:
            cur = cn.cursor()
            cur.execute(sql, params)
            rows = cur.fetchall()
            return [[str(v)[:40] for v in r] for r in rows] if rows else []
        except Exception as ex:
            out["errors"].append("%s | %s" % (sql[:70], str(ex)[:180]))
            return []

    def sc(sql, key):
        rows = q(sql)
        out[key] = rows[0][0] if rows else None
        return out[key]

    def dist(sql, key):
        out[key] = ["%s:%s" % (r[0], r[1]) for r in q(sql)]

    def timed(key, sql):
        t = time.perf_counter()
        rows = q(sql)
        out[key] = {"ms": int((time.perf_counter() - t) * 1000), "rows": len(rows)}
        return rows

    jd = "SELECT CAST(dbo.UDF_Gregorian_To_Persian(GETDATE()) AS nvarchar(30))"
    today = str(q(jd)[0][0]).strip()
    f7 = str(q("SELECT CAST(dbo.UDF_Gregorian_To_Persian(DATEADD(day,-6,GETDATE())) AS nvarchar(30))")[0][0]).strip()
    f30 = str(q("SELECT CAST(dbo.UDF_Gregorian_To_Persian(DATEADD(day,-29,GETDATE())) AS nvarchar(30))")[0][0]).strip()
    f365 = str(q("SELECT CAST(dbo.UDF_Gregorian_To_Persian(DATEADD(day,-364,GETDATE())) AS nvarchar(30))")[0][0]).strip()
    j7 = str(q("SELECT CAST(dbo.UDF_Gregorian_To_Persian(DATEADD(day,7,GETDATE())) AS nvarchar(30))")[0][0]).strip()
    out["boundaries"] = {"today": today, "f7": f7, "f30": f30, "f365": f365, "plus7": j7}

    # ---- 1. duplicates on the invoice number, and the flags of duplicated rows
    sc("SELECT COUNT_BIG(1) FROM dbo.sailfact", "sailfact_rows")
    sc("SELECT COUNT_BIG(DISTINCT CAST(shfacfo AS nvarchar(60))) FROM dbo.sailfact", "sailfact_distinct_number")
    sc("SELECT COUNT_BIG(DISTINCT CAST(shfacfo AS nvarchar(60))+N'#'+CAST(rdf__ AS nvarchar(20))) FROM dbo.sailfact", "sailfact_distinct_number_rdf")
    sc("SELECT COUNT_BIG(DISTINCT CAST(sysid AS nvarchar(30))) FROM dbo.sailfact", "sailfact_distinct_sysid")
    dist("SELECT TOP (5) CAST(active AS nvarchar(10)), COUNT(*) FROM dbo.sailfact GROUP BY active ORDER BY 2 DESC", "sailfact_active")
    dist("SELECT TOP (5) CAST(Deleted AS nvarchar(10)), COUNT(*) FROM dbo.sailfact GROUP BY Deleted ORDER BY 2 DESC", "sailfact_deleted")
    dist("SELECT TOP (8) CAST(Status AS nvarchar(10)), COUNT(*) FROM dbo.sailfact GROUP BY Status ORDER BY 2 DESC", "sailfact_status")
    dist("SELECT TOP (5) CAST(ismodify AS nvarchar(10)), COUNT(*) FROM dbo.sailfact GROUP BY ismodify ORDER BY 2 DESC", "sailfact_ismodify")
    dist("SELECT TOP (5) CAST(tasvieh AS nvarchar(10)), COUNT(*) FROM dbo.sailfact GROUP BY tasvieh ORDER BY 2 DESC", "sailfact_tasvieh")
    dist("""SELECT TOP (6) CONVERT(nvarchar(20),deleted)+N'/'+CONVERT(nvarchar(20),active)+N'/s'+CONVERT(nvarchar(20),status)+N'/m'+CONVERT(nvarchar(20),ismodify), COUNT(*)
            FROM (SELECT *, COUNT(*) OVER(PARTITION BY shfacfo) c FROM dbo.sailfact) z WHERE z.c>1
            GROUP BY deleted, active, status, ismodify ORDER BY 2 DESC""", "sailfact_dup_flags")
    sc("SELECT COUNT_BIG(1) FROM dbo.sailfact WHERE active='t' AND ISNULL(Deleted,0)=0", "sailfact_clean_rows")
    sc("SELECT COUNT_BIG(DISTINCT CAST(shfacfo AS nvarchar(60))) FROM dbo.sailfact WHERE active='t' AND ISNULL(Deleted,0)=0", "sailfact_clean_distinct_number")

    # ---- 2. cheques: the real due-date column is sardate
    dist("SELECT TOP (5) CAST(sardate AS nvarchar(20)), COUNT(*) FROM dbo.getchk GROUP BY sardate ORDER BY 2 DESC", "getchk_sardate_top")
    dist("SELECT TOP (6) CAST(chk_satus AS nvarchar(10)), COUNT(*) FROM dbo.getchk GROUP BY chk_satus ORDER BY 2 DESC", "getchk_status")
    sc("SELECT COUNT_BIG(1) FROM dbo.getchk WHERE NULLIF(LTRIM(RTRIM(sardate)),'') IS NULL", "getchk_missing_sardate")
    rows = q("""SELECT (SELECT COUNT_BIG(1) FROM dbo.getchk WITH (NOLOCK) WHERE LTRIM(RTRIM(sardate))<'%s'),
                       (SELECT COUNT_BIG(1) FROM dbo.getchk WITH (NOLOCK) WHERE LTRIM(RTRIM(sardate))>='%s' AND LTRIM(RTRIM(sardate))<='%s'),
                       (SELECT COUNT_BIG(1) FROM dbo.getchk WITH (NOLOCK) WHERE LTRIM(RTRIM(sardate))>'%s'),
                       (SELECT COUNT_BIG(1) FROM dbo.getchk WITH (NOLOCK)),
                       (SELECT ISNULL(SUM(getchkmab),0) FROM dbo.getchk WITH (NOLOCK) WHERE LTRIM(RTRIM(sardate))<'%s')""" % (today, today, j7, j7, today))
    out["getchk_buckets_new_over_soon_ok_total_and_over_amount_positive"] = (rows[0] if rows else None)
    out["putchk_columns_head"] = [r[0] for r in q("SELECT TOP (8) name FROM sys.columns WHERE object_id=OBJECT_ID('dbo.putchk')")]
    sc("SELECT COUNT_BIG(1) FROM dbo.putchk", "putchk_rows")

    # ---- 3. ka_act act ids + the cost of the stock apply used by the product radar
    dist("SELECT TOP (12) CAST(act_id AS nvarchar(10)), COUNT(*) FROM dbo.ka_act GROUP BY act_id ORDER BY 2 DESC", "ka_act_ids")
    timed("stock_apply_20_items", """SELECT TOP (20) i.shka, ISNULL(stx.stock_qty,0) FROM dbo.inventory i WITH (NOLOCK)
        OUTER APPLY (SELECT CAST(1 AS bigint) stock_rows, CAST(ISNULL(SUM(CAST(ISNULL(k.tedvah,0) AS decimal(19,3))*CASE WHEN k.act_id IN (%s) THEN -1 ELSE 1 END),0)
        *ISNULL(NULLIF(TRY_CONVERT(decimal(19,3),i.mohvah),0),1)+ISNULL(SUM(CAST(ISNULL(k.tedjoz,0) AS decimal(19,3))*CASE WHEN k.act_id IN (%s) THEN -1 ELSE 1 END),0) AS decimal(19,3)) stock_qty
        FROM dbo.ka_act k WHERE k.shka=i.shka AND k.active='t') stx""" % (STOCK_OUT, STOCK_OUT))

    # ---- 4. the batched queries the rewrite uses (one round trip each)
    timed("batched_dashboard_kpis", """SELECT
        (SELECT COUNT_BIG(1) FROM dbo.CUSTOMERS WITH (NOLOCK)),
        (SELECT COUNT_BIG(1) FROM dbo.inventory WITH (NOLOCK)),
        (SELECT COUNT_BIG(1) FROM dbo.sailfact WITH (NOLOCK)),
        (SELECT COUNT_BIG(1) FROM dbo.sailfact_pish WITH (NOLOCK)),
        (SELECT COUNT_BIG(1) FROM dbo.getchk WITH (NOLOCK)),
        (SELECT COUNT_BIG(1) FROM dbo.putchk WITH (NOLOCK)),
        (SELECT COUNT_BIG(1) FROM dbo.visitors WITH (NOLOCK)),
        (SELECT COUNT_BIG(1) FROM dbo.vis_goals WITH (NOLOCK)),
        (SELECT COUNT_BIG(1) FROM dbo.sailfact WITH (NOLOCK) WHERE [date]='%s' AND active='t' AND ISNULL(Deleted,0)=0),
        (SELECT COUNT_BIG(1) FROM dbo.sailfact WITH (NOLOCK) WHERE [date]>='%s' AND [date]<='%s' AND active='t' AND ISNULL(Deleted,0)=0),
        (SELECT COUNT_BIG(1) FROM dbo.sailfact WITH (NOLOCK) WHERE [date]>='%s' AND [date]<='%s' AND active='t' AND ISNULL(Deleted,0)=0),
        (SELECT COUNT_BIG(1) FROM dbo.sailfact WITH (NOLOCK) WHERE [date]>='%s' AND [date]<='%s' AND active='t' AND ISNULL(Deleted,0)=0),
        (SELECT COUNT_BIG(1) FROM dbo.CUSTOMERS c WITH (NOLOCK) WHERE c.[man]>0)
        """ % (today, f7, today, f30, today, f365, today))
    timed("batched_manager_reports", """SELECT
        (SELECT COUNT_BIG(1) FROM dbo.sailfact WITH (NOLOCK) WHERE [date]>='%s' AND [date]<='%s' AND active='t' AND ISNULL(Deleted,0)=0),
        (SELECT COUNT_BIG(1) FROM dbo.buyfact WITH (NOLOCK) WHERE [DATE]>='%s' AND [DATE]<='%s' AND active='t'),
        (SELECT COUNT_BIG(DISTINCT v.vis_rdf) FROM dbo.sailfact s WITH (NOLOCK) LEFT JOIN dbo.visitors v WITH (NOLOCK) ON v.vis_rdf=s.vis_rdf WHERE s.[date]>='%s' AND s.[date]<='%s' AND s.active='t'),
        (SELECT COUNT_BIG(1) FROM dbo.getchk WITH (NOLOCK) WHERE LTRIM(RTRIM(sardate))<'%s'),
        (SELECT COUNT_BIG(1) FROM dbo.CUSTOMERS c WITH (NOLOCK) WHERE c.[man]>0),
        (SELECT COUNT_BIG(1) FROM dbo.CUSTOMERS c WITH (NOLOCK) WHERE NOT EXISTS (SELECT 1 FROM dbo.sailfact s WITH (NOLOCK) WHERE s.shmo=c.SHMO)),
        (SELECT COUNT_BIG(1) FROM dbo.sailfact WITH (NOLOCK) WHERE tasvieh='f' AND NULLIF([t_date],'') IS NOT NULL AND dbo.dif_date_alan([t_date])<0)
        """ % (f30, today, f30, today, f30, today, today))
    timed("batched_daily_items", """SELECT TOP (8) d.naka, ISNULL(SUM(d.LINESUM),0) FROM dbo.subsailfact d WITH (NOLOCK)
        JOIN dbo.sailfact s WITH (NOLOCK) ON s.shfacfo=d.shfacfo WHERE s.[date]='%s' AND s.active='t' AND d.active='t'
        GROUP BY d.naka ORDER BY 2 DESC""" % today)
    timed("batched_trend_7", """SELECT TOP (7) s.[date], ISNULL(SUM(s.[all]),0) FROM dbo.sailfact s WITH (NOLOCK)
        WHERE s.[date]>='%s' AND s.[date]<='%s' AND s.active='t' AND ISNULL(s.Deleted,0)=0 GROUP BY s.[date] ORDER BY s.[date] DESC""" % (f7, today))
    timed("batched_top_debtors", """SELECT TOP (8) CAST(c.SHMO AS nvarchar(30)), TRY_CONVERT(nvarchar(250),c.MONAME), c.[man]
        FROM dbo.CUSTOMERS c WITH (NOLOCK) WHERE c.[man]>0 ORDER BY c.[man] DESC""")
    timed("batched_checks_breakdown", """SELECT TOP (6) CAST(chk_satus AS nvarchar(20)), COUNT_BIG(1), ISNULL(SUM(getchkmab),0)
        FROM dbo.getchk WITH (NOLOCK) GROUP BY chk_satus ORDER BY 3 DESC""")
    timed("batched_visitor_share", """SELECT TOP (6) ISNULL(v.vis_name, N'بدون ویزیتور'), ISNULL(SUM(s.[all]),0), COUNT_BIG(1)
        FROM dbo.sailfact s WITH (NOLOCK) LEFT JOIN dbo.visitors v WITH (NOLOCK) ON v.vis_rdf=s.vis_rdf
        WHERE s.[date]>='%s' AND s.[date]<='%s' AND s.active='t' AND ISNULL(s.Deleted,0)=0
        GROUP BY v.vis_name ORDER BY 2 DESC""" % (f30, today))

    json.dump(out, open(OUT, "w", encoding="utf-8"), ensure_ascii=False, default=str, indent=1)
    print("check2 done, errors:", len(out["errors"]))
    print(json.dumps({k: v for k, v in out.items() if k not in ("errors",)}, ensure_ascii=False)[:2500])


if __name__ == "__main__":
    main()

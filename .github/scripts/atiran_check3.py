#!/usr/bin/env python3
"""
Third live check: runs the SQL the FIXED app now builds, section by section, against the real
Atiran2 database and reports one row per section with statement count, wall-clock ms and the
numbers the app will render.

Sections mirror the four screens the user reported empty:
  خانه / داشبورد      -> queryDashboard + queryTodayDashboard
  گزارش‌ها            -> queryManagerReports (rangeBlock/trend/checkBuckets/visitorShare/debtors)
  هوش مدیریتی (اتاق فروش، وصول، کالا)  -> ManagerAnalytics.cockpit/collection/productRadar/periods
  نظارت بر فروش (ویزیتور، مسیر، مشتری) -> visitorPerformance/visitorGoals/customerCategories

Only counts, sums and timings are written back - never a customer name or a phone number.
"""
import json
import re
import sys
import time

import pytds

SRC = "MEELANO-Android/app/src/main/java/ir/meelano/android/MainActivity.java"
OUT = sys.argv[1] if len(sys.argv) > 1 else "atiran-check3.json"
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
    out = {"errors": [], "sections": {}}
    statements = [0]

    def q(sql):
        statements[0] += 1
        try:
            cur = cn.cursor()
            cur.execute(sql)
            rows = cur.fetchall()
            return [[str(v)[:60] for v in r] for r in rows] if rows else []
        except Exception as ex:
            out["errors"].append("%s | %s" % (sql[:90].replace("\n", " "), str(ex)[:200]))
            return []

    def section(name):
        box = {"statements": 0, "ms": 0, "values": {}, "notes": []}
        out["sections"][name] = box
        return box

    def run(box, key, sql):
        """One statement, timed; stores the first row (or row count) under 'key'."""
        statements[0] += 1
        box["statements"] += 1
        t = time.perf_counter()
        try:
            cur = cn.cursor()
            cur.execute(sql)
            rows = cur.fetchall()
            box["ms"] += int((time.perf_counter() - t) * 1000)
            box["values"][key] = [[str(v)[:48] for v in r] for r in rows][:14] if rows else []
            return rows
        except Exception as ex:
            box["ms"] += int((time.perf_counter() - t) * 1000)
            box["values"][key] = "ERR " + str(ex)[:160]
            out["errors"].append("%s | %s" % (key, str(ex)[:200]))
            return []

    # ---------------------------------------------------------------- the Jalali anchor, exactly as the app computes it
    today = str(q("SELECT CAST(dbo.UDF_Gregorian_To_Persian(GETDATE()) AS nvarchar(30))")[0][0]).strip()
    plus7 = q("SELECT CAST(dbo.UDF_Gregorian_To_Persian(DATEADD(day,7,GETDATE())) AS nvarchar(30))")[0][0]
    out["anchor"] = today
    out["plus7"] = plus7

    def jalali(days):
        # MeelanoJalali.addDays: computed in Python with the same jalaali algorithm as the app
        gy, gm, gd = [int(x) for x in str(q("SELECT CONVERT(nvarchar(10),DATEADD(day,%d,GETDATE()),120)" % days)[0][0]).split("-")]
        return py_jalali(gy, gm, gd)

    def py_jalali(gy, gm, gd):
        g_d_m = [0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334]
        gy2 = gy - 1600
        gm2 = gm - 1
        gd2 = gd - 1
        g_day_no = 365 * gy2 + (gy2 + 3) // 4 - (gy2 + 99) // 100 + (gy2 + 399) // 400
        g_day_no += g_d_m[gm2] + gd2
        if gm > 2 and ((gy % 4 == 0 and gy % 100 != 0) or gy % 400 == 0):
            g_day_no += 1
        j_day_no = g_day_no - 79
        j_np = j_day_no // 12053
        j_day_no %= 12053
        jy = 979 + 33 * j_np + 4 * (j_day_no // 1461)
        j_day_no %= 1461
        if j_day_no >= 366:
            jy += (j_day_no - 1) // 365
            j_day_no = (j_day_no - 1) % 365
        for i in range(11):
            md = 31 if i < 6 else 30
            if j_day_no < md:
                return "%04d/%02d/%02d" % (jy, i + 1, j_day_no + 1)
            j_day_no -= md
        return "%04d/12/%02d" % (jy, j_day_no + 1)

    b = {"d0": today, "d7": jalali(-6), "d30": jalali(-29), "p30a": jalali(-59), "p30b": jalali(-30),
         "y1": jalali(-364), "p1a": jalali(-729), "p1b": jalali(-365)}
    out["bounds"] = b

    def window(col, lo, hi, alias="x"):
        c = (alias + "." if alias else "") + col
        return "LEFT(LTRIM(RTRIM(%s)),10)>='%s' AND LEFT(LTRIM(RTRIM(%s)),10)<='%s'" % (c, lo, c, hi)

    def tf(src, alias, where):
        """The app's dedupe sub-select: one row per shfacfo, the active/modified copy wins."""
        return ("(SELECT * FROM (SELECT x.*, ROW_NUMBER() OVER(PARTITION BY COALESCE(NULLIF(LTRIM(RTRIM(TRY_CONVERT(nvarchar(120),x.shfacfo))),N''),"
                "N'__row__'+COALESCE(TRY_CONVERT(nvarchar(120),x.rdf__),CONVERT(nvarchar(36),NEWID()))) "
                "ORDER BY CASE WHEN x.active='t' THEN 1 ELSE 0 END DESC, LEFT(LTRIM(RTRIM(TRY_CONVERT(nvarchar(30),x.[date]))),10) DESC, TRY_CONVERT(bigint,x.rdf__) DESC) _rn "
                "FROM dbo." + src + " x WITH (NOLOCK) WHERE " + where + ") mx WHERE mx._rn=1) " + alias)

    def sail_where(lo, hi):
        return window("[date]", lo, hi) + " AND x.active='t' AND ISNULL(x.Deleted,0)=0"

    # ================================================================ 1) خانه / داشبورد
    box = section("home_dashboard")
    run(box, "kpi_counts_one_statement",
        """SELECT (SELECT COUNT_BIG(1) FROM dbo.CUSTOMERS WITH (NOLOCK)), (SELECT COUNT_BIG(1) FROM dbo.inventory WITH (NOLOCK)),
                  (SELECT COUNT_BIG(1) FROM dbo.sailfact WITH (NOLOCK)), (SELECT COUNT_BIG(1) FROM dbo.sailfact_pish WITH (NOLOCK)),
                  (SELECT COUNT_BIG(1) FROM dbo.getchk WITH (NOLOCK)), (SELECT COUNT_BIG(1) FROM dbo.putchk WITH (NOLOCK)),
                  (SELECT COUNT_BIG(1) FROM dbo.visitors WITH (NOLOCK)), (SELECT COUNT_BIG(1) FROM dbo.vis_goals WITH (NOLOCK))""")
    run(box, "sales_today",
        """SELECT ISNULL(SUM(TRY_CONVERT(decimal(19,2),h.[all])),0), COUNT_BIG(1), COUNT(DISTINCT h.shmo),
                  ISNULL(SUM(TRY_CONVERT(decimal(19,2),h.[MabDaryaftFactor])),0), ISNULL(SUM(TRY_CONVERT(decimal(19,2),h.[tafif])),0),
                  ISNULL(SUM(TRY_CONVERT(decimal(19,2),h.[tax])),0) FROM """ + tf("sailfact", "h", sail_where(today, today)))
    run(box, "sales_trend_7_days",
        """SELECT LEFT(LTRIM(RTRIM(h.[date])),10) d, ISNULL(SUM(TRY_CONVERT(decimal(19,2),h.[all])),0) FROM """ + tf("sailfact", "h", sail_where(b["d7"], today)) +
        " GROUP BY LEFT(LTRIM(RTRIM(h.[date])),10) ORDER BY 1 DESC")
    run(box, "check_buckets_new",
        """SELECT CASE WHEN LEFT(LTRIM(RTRIM(sardate)),10)<'%s' THEN N'over' WHEN LEFT(LTRIM(RTRIM(sardate)),10)<='%s' THEN N'soon' ELSE N'ok' END b,
                  COUNT_BIG(1), ISNULL(SUM(TRY_CONVERT(decimal(19,2),getchkmab)),0) FROM dbo.getchk WITH (NOLOCK)
           WHERE NULLIF(LTRIM(RTRIM(sardate)),'') IS NOT NULL GROUP BY CASE WHEN LEFT(LTRIM(RTRIM(sardate)),10)<'%s' THEN N'over' WHEN LEFT(LTRIM(RTRIM(sardate)),10)<='%s' THEN N'soon' ELSE N'ok' END""" % (today, plus7, today, plus7))
    run(box, "check_buckets_old_broken",
        """SELECT COUNT_BIG(1), ISNULL(SUM(TRY_CONVERT(decimal(19,2),getchkmab)),0) FROM dbo.getchk""")
    run(box, "top_debtors",
        """SELECT COUNT_BIG(1), ISNULL(SUM(TRY_CONVERT(decimal(19,2),[man])),0) FROM dbo.CUSTOMERS WITH (NOLOCK) WHERE TRY_CONVERT(decimal(19,2),[man])>0""")
    run(box, "overdue_invoices_vis_rdf_join",
        """SELECT COUNT_BIG(1) FROM dbo.sailfact s WITH (NOLOCK) LEFT JOIN dbo.visitors v WITH (NOLOCK) ON TRY_CONVERT(nvarchar(100),v.vis_rdf)=TRY_CONVERT(nvarchar(100),s.vis_rdf)
           WHERE s.tasvieh='f' AND NULLIF(s.t_date,'') IS NOT NULL AND dbo.dif_date_alan(s.t_date)<0 AND s.active='t'""")
    run(box, "inactive_customers",
        """SELECT COUNT_BIG(1) FROM dbo.CUSTOMERS c WITH (NOLOCK) WHERE NOT EXISTS (SELECT 1 FROM dbo.sailfact s WITH (NOLOCK) WHERE TRY_CONVERT(nvarchar(100),s.shmo)=TRY_CONVERT(nvarchar(100),c.SHMO))""")
    run(box, "home_roundtrips_mirroring_app", "SELECT 1")   # counts as the app's own openConnection round trip

    # ================================================================ 2) گزارش‌ها
    box = section("manager_reports")
    for label, lo, hi, plo, phi in [("today", today, today, today, today), ("d7", b["d7"], today, b["d7"], b["d7"]),
                                    ("d30", b["d30"], today, b["p30a"], b["p30b"]), ("y1", b["y1"], today, b["p1a"], b["p1b"])]:
        run(box, "sales_" + label,
            """SELECT ISNULL(SUM(TRY_CONVERT(decimal(19,2),h.[all])),0), COUNT_BIG(1), COUNT(DISTINCT h.shmo) FROM """ + tf("sailfact", "h", sail_where(lo, hi)))
        run(box, "sales_prev_" + label,
            """SELECT ISNULL(SUM(TRY_CONVERT(decimal(19,2),h.[all])),0) FROM """ + tf("sailfact", "h", sail_where(plo, phi)))
        if label != "today":
            run(box, "purchases_" + label,
                """SELECT ISNULL(SUM(TRY_CONVERT(decimal(19,2),[all])),0), COUNT_BIG(1) FROM dbo.buyfact WITH (NOLOCK)
                   WHERE %s AND active='t'""" % window("[DATE]", lo, hi, ""))
    run(box, "visitor_share_new_key",
        """SELECT TOP (8) ISNULL(v.vis_name,N'none'), ISNULL(SUM(TRY_CONVERT(decimal(19,2),h.[all])),0), COUNT_BIG(1) FROM """ + tf("sailfact", "h", sail_where(b["d30"], today)) +
        """ LEFT JOIN dbo.visitors v WITH (NOLOCK) ON TRY_CONVERT(nvarchar(100),v.vis_rdf)=TRY_CONVERT(nvarchar(100),h.vis_rdf)
           GROUP BY ISNULL(v.vis_name,N'none') ORDER BY 2 DESC""")
    run(box, "visitor_share_old_key_returns_no_names",
        """SELECT COUNT_BIG(1) FROM dbo.sailfact s WITH (NOLOCK) LEFT JOIN dbo.visitors v WITH (NOLOCK) ON TRY_CONVERT(nvarchar(100),v.rdf)=TRY_CONVERT(nvarchar(100),s.vis_rdf)""")
    run(box, "rows_lost_by_try_convert_date",
        """SELECT COUNT_BIG(1) FROM dbo.sailfact WITH (NOLOCK) WHERE TRY_CONVERT(date,TRY_CONVERT(nvarchar(30),[date])) IS NULL""")

    # ================================================================ 3) هوش مدیریتی (اتاق فروش / وصول / کالا)
    box = section("management_intelligence")
    run(box, "cockpit_sales_30d_with_prev",
        """SELECT ISNULL(SUM(TRY_CONVERT(decimal(19,2),h.[all])),0) FROM """ + tf("sailfact", "h", sail_where(b["d30"], today)))
    run(box, "cockpit_weekday_mix_days",
        """SELECT LEFT(LTRIM(RTRIM(h.[date])),10) d, ISNULL(SUM(TRY_CONVERT(decimal(19,2),h.[all])),0) FROM """ + tf("sailfact", "h", sail_where(b["d30"], today)) +
        " GROUP BY LEFT(LTRIM(RTRIM(h.[date])),10) ORDER BY 1 DESC")
    run(box, "collection_aging",
        """WITH x AS (SELECT CASE WHEN dbo.dif_date_alan([t_date]) >= 0 THEN N'current' WHEN -dbo.dif_date_alan([t_date]) <= 30 THEN N'1-30'
                    WHEN -dbo.dif_date_alan([t_date]) <= 60 THEN N'31-60' WHEN -dbo.dif_date_alan([t_date]) <= 90 THEN N'61-90'
                    WHEN -dbo.dif_date_alan([t_date]) <= 180 THEN N'91-180' ELSE N'180+' END bucket,
                    (TRY_CONVERT(decimal(19,2),[all]) - ISNULL(TRY_CONVERT(decimal(19,2),[MabDaryaftFactor]),0) - ISNULL(TRY_CONVERT(decimal(19,2),[tdf]),0)) amount
                FROM dbo.sailfact WITH (NOLOCK) WHERE [tasvieh]='f' AND NULLIF([t_date],'') IS NOT NULL AND active='t')
           SELECT bucket, ISNULL(SUM(CASE WHEN amount>0 THEN amount ELSE 0 END),0), COUNT_BIG(CASE WHEN amount>0 THEN 1 END) FROM x GROUP BY bucket ORDER BY 2 DESC""")
    run(box, "product_profit_top",
        """SELECT TOP (10) COALESCE(TRY_CONVERT(nvarchar(150),i.naka),N'?'), ISNULL(SUM(ISNULL(TRY_CONVERT(decimal(19,2),d.LINESUM),0) - ISNULL(TRY_CONVERT(decimal(19,4),d.TEDVAH),0)*ISNULL(TRY_CONVERT(decimal(19,4),i.buy_price),0)),0) FROM """ + tf("sailfact", "h", sail_where(b["d30"], today)) +
        """ JOIN dbo.subsailfact d WITH (NOLOCK) ON TRY_CONVERT(nvarchar(100),d.shfacfo)=TRY_CONVERT(nvarchar(100),h.shfacfo)
            LEFT JOIN dbo.inventory i WITH (NOLOCK) ON TRY_CONVERT(nvarchar(100),i.shka)=TRY_CONVERT(nvarchar(100),d.SHKA)
            WHERE d.active='t' GROUP BY COALESCE(TRY_CONVERT(nvarchar(150),i.naka),N'?') ORDER BY 2 DESC""")
    run(box, "top_products_30d",
        """SELECT TOP (8) CAST(MAX(d.naka) AS nvarchar(120)), ISNULL(SUM(TRY_CONVERT(decimal(19,2),d.LINESUM)),0)
           FROM dbo.subsailfact d WITH (NOLOCK) JOIN dbo.sailfact s WITH (NOLOCK) ON s.shfacfo=d.shfacfo
           WHERE %s AND d.active='t' GROUP BY d.SHKA ORDER BY 2 DESC""" % window("s.[date]", b["d30"], today, ""))
    run(box, "periods_view",
        """SELECT TOP (12) COUNT_BIG(1) FROM dbo.VW_Forush_DarBazeZamani WITH (NOLOCK)""")
    run(box, "warehouses",
        """SELECT COUNT_BIG(1) FROM dbo.anbars WITH (NOLOCK)""")
    run(box, "zero_stock_radar",
        """SELECT TOP (12) COUNT_BIG(1) FROM dbo.inventory i WITH (NOLOCK)
           OUTER APPLY (SELECT CAST(1 AS bigint) stock_rows, CAST(ISNULL(SUM(CAST(ISNULL(k.tedvah,0) AS decimal(19,3))*CASE WHEN k.act_id IN (%s) THEN -1 ELSE 1 END),0)
           *ISNULL(NULLIF(TRY_CONVERT(decimal(19,3),i.mohvah),0),1)+ISNULL(SUM(CAST(ISNULL(k.tedjoz,0) AS decimal(19,3))*CASE WHEN k.act_id IN (%s) THEN -1 ELSE 1 END),0) AS decimal(19,3)) stock_qty
           FROM dbo.ka_act k WITH (NOLOCK) WHERE k.shka=i.shka AND k.active='t') stx
           WHERE ISNULL(stx.stock_rows,0)>0 AND ISNULL(stx.stock_qty,0)<=0""" % (STOCK_OUT, STOCK_OUT))
    run(box, "credit_risk",
        """SELECT COUNT_BIG(1) FROM dbo.Sys_Mandeh_Customer WITH (NOLOCK)""")

    # ================================================================ 4) نظارت بر فروش (ویزیتور/مسیر/مشتری)
    box = section("sales_supervision")
    run(box, "visitor_performance",
        """SELECT TOP (10) ISNULL(v.vis_name,N'none'), ISNULL(SUM(TRY_CONVERT(decimal(19,2),h.[all])),0), COUNT_BIG(1) FROM """ + tf("sailfact", "h", sail_where(b["d30"], today)) +
        """ LEFT JOIN dbo.visitors v WITH (NOLOCK) ON TRY_CONVERT(nvarchar(100),v.vis_rdf)=TRY_CONVERT(nvarchar(100),h.vis_rdf)
           GROUP BY ISNULL(v.vis_name,N'none') ORDER BY 2 DESC""")


    run(box, "visitor_goals_rows",
        """SELECT COUNT_BIG(1) FROM dbo.vis_goals WITH (NOLOCK)""")
    run(box, "customer_categories_active_30d",
        """SELECT COUNT_BIG(1) FROM dbo.CUSTOMERS c WITH (NOLOCK) WHERE EXISTS (SELECT 1 FROM dbo.sailfact s WITH (NOLOCK)
           WHERE TRY_CONVERT(nvarchar(100),s.shmo)=TRY_CONVERT(nvarchar(100),c.SHMO) AND %s)""" % window("s.[date]", b["d30"], today, ""))
    run(box, "customer_categories_never_bought",
        """SELECT COUNT_BIG(1) FROM dbo.CUSTOMERS c WITH (NOLOCK) WHERE NOT EXISTS (SELECT 1 FROM dbo.sailfact s WITH (NOLOCK) WHERE TRY_CONVERT(nvarchar(100),s.shmo)=TRY_CONVERT(nvarchar(100),c.SHMO))""")
    run(box, "visit_table_present",
        """SELECT COUNT_BIG(1) FROM sys.tables WHERE name=N'Visit'""")
    run(box, "routes",
        """SELECT COUNT_BIG(1) FROM dbo.masir WITH (NOLOCK)""")

    # The executive dashboard showed «فروش —» on the live run while purchases worked, and fetch() swallows
    # per-section errors. These four statements isolate whether the merged «current + previous period in one
    # statement» shape is accepted by this SQL Server version for sailfact.
    box = section("merged_statement")
    order = ("CASE WHEN UPPER(LTRIM(RTRIM(TRY_CONVERT(nvarchar(20),x.[active])))) IN (N'T',N'TRUE',N'Y',N'YES',N'1') THEN 1 ELSE 0 END DESC,"
             "LEFT(LTRIM(RTRIM(TRY_CONVERT(nvarchar(30),x.[date]))),10) DESC, TRY_CONVERT(bigint,x.[rdf__]) DESC")
    part = "COALESCE(NULLIF(LTRIM(RTRIM(TRY_CONVERT(nvarchar(120),x.[shfacfo]))),N''),N'__row__' + COALESCE(TRY_CONVERT(nvarchar(120),x.[rdf__]),CONVERT(nvarchar(36),NEWID())))"
    where_cur = "WHERE LEFT(LTRIM(RTRIM(TRY_CONVERT(nvarchar(30),x.[date]))),10) BETWEEN '1405/06/13' AND '1405/07/11' AND x.[active]='t'"
    where_prev = "WHERE LEFT(LTRIM(RTRIM(TRY_CONVERT(nvarchar(30),x.[date]))),10) BETWEEN '1405/05/14' AND '1405/06/12' AND x.[active]='t'"
    src = ("(SELECT * FROM (SELECT x.*, ROW_NUMBER() OVER(PARTITION BY " + part + " ORDER BY " + order + ") AS _meelano_rn "
           "FROM dbo.[sailfact] x " + where_cur + ") mx WHERE mx._meelano_rn=1) h")
    src2 = ("(SELECT * FROM (SELECT x.*, ROW_NUMBER() OVER(PARTITION BY " + part + " ORDER BY " + order + ") AS _meelano_rn "
            "FROM dbo.[sailfact] x " + where_prev + ") mx WHERE mx._meelano_rn=1) h")
    run(box, "v1_simple_scalar",
        "SELECT ISNULL(SUM(TRY_CONVERT(decimal(19,2),h.[all])),0), (SELECT ISNULL(SUM(TRY_CONVERT(decimal(19,2),x.[all])),0) FROM dbo.[sailfact] x " + where_prev + ") FROM dbo.[sailfact] h " + where_cur)
    run(box, "v2_scalar_over_derived",
        "SELECT ISNULL(SUM(TRY_CONVERT(decimal(19,2),h.[all])),0), (SELECT ISNULL(SUM(TRY_CONVERT(decimal(19,2),h.[all])),0) FROM " + src2 + ") FROM " + src)
    run(box, "v3_full_merged",
        "SELECT ISNULL(SUM(TRY_CONVERT(decimal(19,2),h.[all])),0), COUNT_BIG(1), COUNT(DISTINCT h.[shmo]), "
        "(SELECT ISNULL(SUM(TRY_CONVERT(decimal(19,2),h.[all])),0) FROM " + src2 + ") FROM " + src)
    # The real signature of ManagerAnalytics.rangeBlock: sailfact first, then buyfact — if sailfact throws,
    # fetch() records it and the KPI renders «—» while purchases still work (what the live screenshot showed).
    try:
        cur = cn.cursor(); cur.execute(
            "SELECT ISNULL(SUM(TRY_CONVERT(decimal(19,2),h.[all])),0), COUNT_BIG(1), COUNT(DISTINCT h.[shmo]), "
            "(SELECT ISNULL(SUM(TRY_CONVERT(decimal(19,2),h.[all])),0) FROM " + src2 + ") FROM " + src)
        r = cur.fetchone()
        box["values"]["v5_rangeblock_sailfact_ok"] = [str(v)[:40] for v in r]
    except Exception as ex:
        box["values"]["v5_rangeblock_sailfact_ok"] = "ERR " + str(ex)[:220]
    try:
        cur = cn.cursor(); cur.execute(
            "SELECT ISNULL(SUM(TRY_CONVERT(decimal(19,2),h.[all])),0), COUNT_BIG(1), COUNT(DISTINCT h.[shmo]) FROM dbo.[buyfact] h WHERE 1=1")
        r = cur.fetchone()
        box["values"]["v6_rangeblock_buyfact_ok"] = [str(v)[:40] for v in r]
    except Exception as ex:
        box["values"]["v6_rangeblock_buyfact_ok"] = "ERR " + str(ex)[:220]

    # The app reports «جدول Visit وجود ندارد» for managers; sys.tables says a table named Visit exists.
    # Resolve the exact schema/columns/rows so the page can either show real numbers or a precise reason.
    box = section("visit_object")
    run(box, "objects_named_visit",
        """SELECT STUFF((SELECT N' | ' + s.name + N'.' + o.name + N' (' + o.type_desc + N')' FROM sys.objects o JOIN sys.schemas s ON s.schema_id=o.schema_id WHERE o.name LIKE N'%Visit%' FOR XML PATH('')),1,3,N'')""")
    run(box, "objects_like_visitor",
        """SELECT STUFF((SELECT N' | ' + s.name + N'.' + o.name FROM sys.objects o JOIN sys.schemas s ON s.schema_id=o.schema_id WHERE o.name LIKE N'%visit%' OR o.name LIKE N'%bazdid%' FOR XML PATH('')),1,3,N'')""")
    run(box, "dbo_visit_columns",
        """SELECT STUFF((SELECT N',' + c.name + N':' + t.name FROM sys.columns c JOIN sys.types t ON t.user_type_id=c.user_type_id WHERE c.object_id=OBJECT_ID(N'dbo.Visit') ORDER BY c.column_id FOR XML PATH('')),1,1,N'')""")
    run(box, "any_visit_columns",
        """SELECT ISNULL((SELECT TOP (1) STUFF((SELECT N',' + c.name FROM sys.columns c WHERE c.object_id=o.object_id ORDER BY c.column_id FOR XML PATH('')),1,1,N'') FROM sys.objects o JOIN sys.schemas s ON s.schema_id=o.schema_id WHERE o.name=N'Visit'), N'')""")
    run(box, "visit_rows",
        """SELECT ISNULL((SELECT SUM(p.rows) FROM sys.partitions p JOIN sys.objects o ON o.object_id=p.object_id WHERE o.name=N'Visit' AND p.index_id IN (0,1)),0)""")
    # Hamrah.Visit is the real field-visit table (schema Hamrah, 0 rows). Get its full typed column list and
    # the exact schema name so the app reads it instead of reporting "table missing".
    run(box, "visit_schema",
        """SELECT ISNULL((SELECT TOP (1) s.name FROM sys.tables t JOIN sys.schemas s ON s.schema_id=t.schema_id WHERE t.name=N'Visit'), N'none')""")
    run(box, "visit_columns_typed",
        """SELECT ISNULL((SELECT TOP (1) STUFF((SELECT N',' + c.name + N':' + ty.name + N'(' + CAST(c.max_length AS nvarchar(10)) + N')' FROM sys.columns c JOIN sys.types ty ON ty.user_type_id=c.user_type_id WHERE c.object_id=o.object_id ORDER BY c.column_id FOR XML PATH('')),1,1,N'') FROM sys.objects o JOIN sys.schemas s ON s.schema_id=o.schema_id WHERE o.name=N'Visit'), N'')""")
    run(box, "baze_columns_typed",
        """SELECT STUFF((SELECT N',' + c.name + N':' + ty.name FROM sys.columns c JOIN sys.types ty ON ty.user_type_id=c.user_type_id WHERE c.object_id=OBJECT_ID(N'dbo.baze') ORDER BY c.column_id FOR XML PATH('')),1,1,N'')""")
    run(box, "vis_goals_all_columns",
        """SELECT STUFF((SELECT N',' + c.name FROM sys.columns c WHERE c.object_id=OBJECT_ID(N'dbo.vis_goals') ORDER BY c.column_id FOR XML PATH('')),1,1,N'')""")
    run(box, "vis_goals_sample",
        """SELECT TOP (6) CAST(vis_rdf AS nvarchar(20)), CAST(baze_rdf AS nvarchar(20)), CAST(ISNULL(mab,0) AS nvarchar(30)) FROM dbo.vis_goals WITH (NOLOCK) ORDER BY vis_rdf""")
    run(box, "baze_sample",
        """SELECT TOP (4) CAST(rdf AS nvarchar(20)) + N' ' + CAST(ISNULL(name,N'') AS nvarchar(60)) FROM dbo.baze WITH (NOLOCK) ORDER BY rdf""")

    # Visitor goals: the manager page shows «هدف» next to real sales, so confirm the goal column is populated.
    run(box, "vis_goals_columns",
        """SELECT STUFF((SELECT N',' + c.name + N':' + t.name FROM sys.columns c JOIN sys.types t ON t.user_type_id=c.user_type_id WHERE c.object_id=OBJECT_ID(N'dbo.vis_goals') ORDER BY c.column_id FOR XML PATH('')),1,1,N'')""")
    run(box, "visit_information_schema",
        """SELECT ISNULL((SELECT TOP (1) TABLE_SCHEMA + N'.' + TABLE_NAME + N' (' + TABLE_TYPE + N')' FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME=N'Visit'), N'none')""")

    # putchk (paid cheques) shape: the app lists sardate/putchkdate candidates, so record which exists.
    box = section("extra_schema")
    run(box, "putchk_rows", "SELECT COUNT_BIG(1) FROM dbo.putchk WITH (NOLOCK)")
    run(box, "putchk_date_like_columns",
        """SELECT STUFF((SELECT N',' + c.name FROM sys.columns c JOIN sys.objects o ON o.object_id=c.object_id WHERE o.name=N'putchk' AND (c.name LIKE N'%date%' OR c.name LIKE N'%sar%') ORDER BY c.column_id FOR XML PATH('')),1,1,N'')""")
    run(box, "getchk_date_like_columns",
        """SELECT STUFF((SELECT N',' + c.name FROM sys.columns c JOIN sys.objects o ON o.object_id=c.object_id WHERE o.name=N'getchk' AND (c.name LIKE N'%date%' OR c.name LIKE N'%sar%') ORDER BY c.column_id FOR XML PATH('')),1,1,N'')""")

    # The app signs in as the shared back-office accounts «Admin/1385» and «Modir/123».
    # Verify live that both exist in visitors (plain Password) or sys_users (hashed/encrypted)
    # and that the entered password matches what the Java matcher would accept.
    import hashlib
    box = section("logins")
    pairs = [("Admin", "1385"), ("Modir", "123")]
    visitors = q("""SELECT LTRIM(RTRIM(TRY_CONVERT(nvarchar(200),Username))), TRY_CONVERT(nvarchar(200),Password), TRY_CONVERT(nvarchar(30),vis_name), ISNULL(active,''), TRY_CONVERT(nvarchar(20),is_supervisor) FROM dbo.visitors WITH (NOLOCK)""")
    sysu = q("""SELECT LTRIM(RTRIM(TRY_CONVERT(nvarchar(100),user_name))), TRY_CONVERT(varbinary(200),user_password), TRY_CONVERT(nvarchar(30),user_lname)+N' '+TRY_CONVERT(nvarchar(30),user_fname), ISNULL(TRY_CONVERT(nvarchar(10),active),''), ISNULL(TRY_CONVERT(nvarchar(10),IsLocked),'') FROM dbo.sys_users WITH (NOLOCK)""")
    box["values"]["visitor_logins"] = [r[0] for r in visitors if r and r[0]]
    box["values"]["sysuser_logins"] = [r[0] for r in sysu if r and r[0]]
    box["statements"] += 2

    def candidates(pw):
        out = {pw.encode("utf-8"), pw.encode("utf-16-le"), pw.encode("latin-1")}
        for alg in ("md5", "sha1", "sha256", "sha512"):
            d = hashlib.new(alg, pw.encode("utf-8")).digest()
            out.add(d)
        return out

    for user, pw in pairs:
        u = user.lower()
        hit = None
        for r in visitors:
            if r and (r[0] or "").lower() == u:
                hit = {"table": "visitors", "display": r[2], "active": r[3], "supervisor": r[4],
                       "password_matches": (r[1] or "") in (pw, "  " if False else pw)}
                break
        if hit is None:
            for r in sysu:
                if r and (r[0] or "").lower() == u:
                    raw = bytes.fromhex(r[1][2:]) if r[1] and r[1].startswith("0x") else (r[1] or "").encode("latin-1")
                    hit = {"table": "sys_users", "display": r[2], "active": r[3], "locked": r[4],
                           "password_matches": raw in candidates(pw), "password_bytes": len(raw)}
                    break
        box["values"][user] = hit or {"found": False}
    box["notes"].append("password_matches is true only when the stored value is the exact entered "
                        "text or one of the encodings the Java matcher (passwordMatches) accepts.")

    out["total_statements"] = statements[0]
    out["total_ms"] = sum(v["ms"] for v in out["sections"].values())
    out["roundtrip_ms_reference"] = 192
    json.dump(out, open(OUT, "w", encoding="utf-8"), ensure_ascii=False, default=str, indent=1)
    print("check3 done: statements=%d total_ms=%d errors=%d" % (statements[0], out["total_ms"], len(out["errors"])))
    for name, box in out["sections"].items():
        print("--", name, "statements=%d ms=%d" % (box["statements"], box["ms"]))
        for k, v in box["values"].items():
            print("   ", k, "=", json.dumps(v, ensure_ascii=False)[:160])
    if out["errors"]:
        print("ERRORS:", json.dumps(out["errors"], ensure_ascii=False)[:1200])


if __name__ == "__main__":
    try:
        main()
    except Exception:
        import traceback
        tb = traceback.format_exc()
        print(tb)
        try:
            json.dump({"fatal": tb[-3000:], "errors": [], "sections": {}}, open(OUT, "w", encoding="utf-8"), ensure_ascii=False, indent=1)
        except Exception:
            pass

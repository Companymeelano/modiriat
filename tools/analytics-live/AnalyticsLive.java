package ir.meelano.android;

import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Set;

/**
 * Runs the shipped management data layer (MeelanoSql + ManagerAnalytics + MeelanoJalali) against the
 * production Atiran server through the same jTDS driver, URL and credentials as the Android app.
 *
 * Purpose: the emulator review can only show what the screen renders; this harness shows the exact
 * statement the app sends, how long each section takes and the server's own error text — so a broken
 * KPI is traced to a line of SQL instead of guessed from a screenshot.
 *
 * Prints a plain-text report (stdout) that the workflow commits as diag/analytics-live.txt.
 */
public final class AnalyticsLive {

    public static void main(String[] args) {
        System.out.println("== AnalyticsLive ==");
        System.out.println("app build: MeelanoSql + ManagerAnalytics + MeelanoJalali, jTDS "
                + net.sourceforge.jtds.jdbc.Driver.class.getPackage().getImplementationVersion());
        System.out.println("server: " + MainActivity.sqlHost() + ":" + MainActivity.sqlPort() + "/" + MainActivity.sqlDatabase());

        // ---- connection warm-up (first jTDS login to a server 170 ms away)
        try {
            long t0 = System.currentTimeMillis();
            Connection c = MeelanoSql.lease();
            c.close();
            System.out.println("first connection (login + TCP): " + (System.currentTimeMillis() - t0) + " ms");
        } catch (Throwable t) {
            System.out.println("FATAL: cannot connect - " + t);
            System.out.println("ANYERR=1");
            return;
        }

        // ---- speed: the dashboard path (parallel) against the old sequential path, same warm pool
        timed("cold fetchParallel(2)  [first dashboard load]", () -> ManagerAnalytics.fetchParallel(2));
        timed("warm fetch(conn,2)      [old sequential path]", () -> {
            try (Connection c = MeelanoSql.lease()) { return ManagerAnalytics.fetch(c, 2); }
        });
        timed("warm fetchParallel(2)   [shipped path]", () -> ManagerAnalytics.fetchParallel(2));

        boolean anyError = false;
        // ---- correctness: every window index the dashboard offers (0=today 1=7d 2=30d 3=12m)
        for (int range : new int[]{0, 1, 2, 3}) {
            try {
                long t0 = System.currentTimeMillis();
                JSONObject j = ManagerAnalytics.fetchParallel(range);
                long ms = System.currentTimeMillis() - t0;
                System.out.println("\n================ fetchParallel(range=" + range + ")  " + ms + " ms ================");
                JSONArray errors = j.optJSONArray("errors");
                if (errors != null && errors.length() > 0) {
                    anyError = true;
                    System.out.println("-- errors (" + errors.length() + ") --");
                    for (int i = 0; i < errors.length(); i++) System.out.println("   " + errors.optString(i));
                } else {
                    System.out.println("-- no section errors --");
                }
                System.out.println("-- section timings (ms) --");
                System.out.println("   " + j.opt("timings"));
                System.out.println("-- figures --");
                System.out.println(summary(j));
                System.out.println("-- full payload --");
                System.out.println(j.toString(2));
            } catch (Throwable t) {
                anyError = true;
                System.out.println("FATAL for range " + range + ": " + t);
                t.printStackTrace(System.out);
            }
        }

        try { everyEntryPoint(); } catch (Throwable t) { System.out.println("entry point sweep failed: " + t); }
        try { sqlLengthProbe(); } catch (Throwable t) { System.out.println("probe failed: " + t); }
        try { guardProbe(); } catch (Throwable t) { System.out.println("guard probe failed: " + t); }

        System.out.println("\nMeelanoSql.stats = " + MeelanoSql.stats());
        System.out.println("ANYERR=" + (anyError ? 1 : 0));
    }

    private interface Work { JSONObject run() throws Exception; }

    private static void timed(String label, Work work) {
        try {
            long t0 = System.currentTimeMillis();
            JSONObject j = work.run();
            long ms = System.currentTimeMillis() - t0;
            System.out.println(String.format("%-46s %6d ms   errors=%d", label, ms, j.optJSONArray("errors") == null ? 0 : j.optJSONArray("errors").length()));
        } catch (Throwable t) {
            System.out.println(String.format("%-46s FAILED: %s", label, t));
        }
    }

    /** The figures a manager reads off the dashboard, one line each — easy to check by eye. */
    private static String summary(JSONObject j) {
        StringBuilder b = new StringBuilder();
        JSONObject sales = j.optJSONObject("sales");
        if (sales != null) b.append("  sales: total=").append(sales.opt("total")).append(" docs=").append(sales.opt("docs"))
                .append(" parties=").append(sales.opt("parties")).append(" paid=").append(sales.opt("paid"))
                .append(" prev=").append(sales.opt("prevTotal")).append(" date=").append(sales.opt("date")).append("\n");
        JSONObject pur = j.optJSONObject("purchases");
        if (pur != null) b.append("  purchases: total=").append(pur.opt("total")).append(" docs=").append(pur.opt("docs"))
                .append(" prev=").append(pur.opt("prevTotal")).append("\n");
        JSONObject recv = j.optJSONObject("receivables");
        if (recv != null) b.append("  receivables: total=").append(recv.opt("total")).append(" count=").append(recv.opt("count")).append("\n");
        JSONObject cust = j.optJSONObject("customers");
        if (cust != null) b.append("  customers: ").append(cust).append("\n");
        JSONObject chk = j.optJSONObject("checkBuckets");
        if (chk != null) b.append("  checkBuckets: ").append(chk).append("\n");
        JSONArray aging = j.optJSONArray("aging");
        if (aging != null) {
            b.append("  aging: ");
            for (int i = 0; i < aging.length(); i++) {
                JSONObject o = aging.optJSONObject(i);
                if (o != null) b.append(o.opt("label")).append("=").append(o.opt("value")).append("/").append(o.opt("docs")).append("  ");
            }
            b.append("\n");
        }
        JSONArray tr = j.optJSONArray("trend");
        if (tr != null) {
            b.append("  trend: ");
            for (int i = 0; i < tr.length(); i++) {
                JSONObject o = tr.optJSONObject(i);
                if (o != null) b.append(o.opt("label")).append("=").append(o.opt("value")).append("  ");
            }
            b.append("\n");
        }
        JSONArray vis = j.optJSONArray("visitors");
        if (vis != null) {
            b.append("  visitors: ");
            for (int i = 0; i < Math.min(4, vis.length()); i++) b.append(JSONObject.valueToString(vis.optJSONObject(i))).append("  ");
            b.append("\n");
        }
        JSONArray prod = j.optJSONObject("products") == null ? null : j.optJSONObject("products").optJSONArray("top");
        if (prod != null) {
            b.append("  products top: ");
            for (int i = 0; i < Math.min(3, prod.length()); i++) b.append(JSONObject.valueToString(prod.optJSONObject(i))).append("  ");
            b.append("\n");
        }
        JSONArray feed = j.optJSONArray("feed");
        if (feed != null) b.append("  feed: ").append(feed.length()).append(" rows, first=").append(feed.length() > 0 ? JSONObject.valueToString(feed.optJSONObject(0)) : "-").append("\n");
        JSONArray cr = j.optJSONArray("credit");
        if (cr != null) b.append("  credit: ").append(cr.length()).append(" rows, first=").append(cr.length() > 0 ? JSONObject.valueToString(cr.optJSONObject(0)) : "-").append("\n");
        JSONArray db = j.optJSONArray("debtors");
        if (db != null) b.append("  debtors: ").append(db.length()).append(" rows, first=").append(db.length() > 0 ? JSONObject.valueToString(db.optJSONObject(0)) : "-").append("\n");
        return b.toString();
    }

    /**
     * Every manager-facing entry point the screens call, one line each: the executive dashboard, the
     * cockpit, the collection centre, the visitor/goal pages, the product radar, the field-visit page,
     * the drill-downs and the reports. A page that «does not fetch» is one of these lines failing.
     */
    private static void everyEntryPoint() {
        System.out.println("\n================ every manager analytics entry point ================");
        String[][] calls = {
                {"cockpit(2)", "COCKPIT"},
                {"collection()", "COLLECTION"},
                {"visitorGoals(2)", "VISITORS"},
                {"productRadar(2)", "PRODUCTS"},
                {"fieldVisits(2)", "FIELD_VISITS"},
                {"routeGoals()", "ROUTE_GOALS"},
                {"periodSales()", "PERIOD_SALES"},
                {"productProfit(2)", "PROFIT"},
                {"warehouses()", "WAREHOUSES"},
                {"debtors(8)", "DEBTORS"},
                {"receivables()", "RECEIVABLES"},
                {"creditRisk()", "CREDIT"},
                {"activityFeed()", "FEED"},
                {"trend(7)", "TREND"},
                {"customerCategories(2)", "CUSTOMERS"},
                {"drill(debtors)", "DRILL_DEBTORS"},
                {"drill(ledger)", "DRILL_LEDGER"},
                {"drill(sales)", "DRILL_SALES"},
                {"drill(checks)", "DRILL_CHECKS"},
                {"drill(products)", "DRILL_PRODUCTS"},
                {"drill(visitors)", "DRILL_VISITORS"},
        };
        Connection c = null;
        try {
            c = MeelanoSql.lease();
            for (String[] call : calls) {
                String label = call[0];
                long t0 = System.currentTimeMillis();
                String verdict;
                try {
                    Object out = run(c, label);
                    int size = out instanceof JSONArray ? ((JSONArray) out).length() : 1;
                    verdict = "OK rows/keys=" + size + "  " + shorten(JSONObject.valueToString(out));
                } catch (Throwable t) {
                    verdict = "ERR " + t.getClass().getSimpleName() + ": " + t.getMessage()
                            + " | sql=" + shorten(MeelanoSql.lastSql());
                }
                System.out.printf("  %-24s %6d ms  %s%n", label, System.currentTimeMillis() - t0, verdict);
            }
        } catch (Throwable t) {
            System.out.println("  sweep could not connect: " + t);
        } finally {
            if (c != null) try { c.close(); } catch (Exception ignored) { }
        }
    }

    private static Object run(Connection c, String label) throws Exception {
        switch (label) {
            case "cockpit(2)": return ManagerAnalytics.cockpit(c, 2);
            case "collection()": return ManagerAnalytics.collection(c);
            case "visitorGoals(2)": return ManagerAnalytics.visitorGoals(c, 2);
            case "productRadar(2)": return ManagerAnalytics.productRadar(c, 2);
            case "fieldVisits(2)": return ManagerAnalytics.fieldVisits(c, 2);
            case "routeGoals()": return ManagerAnalytics.routeGoals(c);
            case "periodSales()": return ManagerAnalytics.periodSales(c);
            case "productProfit(2)": return ManagerAnalytics.productProfit(c, 2);
            case "warehouses()": return ManagerAnalytics.warehouses(c);
            case "debtors(8)": return ManagerAnalytics.debtors(c, 8);
            case "receivables()": return ManagerAnalytics.receivables(c);
            case "creditRisk()": return ManagerAnalytics.creditRisk(c);
            case "activityFeed()": return ManagerAnalytics.activityFeed(c);
            case "trend(7)": return ManagerAnalytics.trend(c, 7);
            case "customerCategories(2)": return ManagerAnalytics.customerCategories(c, 2);
            default:
                break;
        }
        return ManagerAnalytics.drill(c, label.substring(label.indexOf('(') + 1, label.length() - 1), 2);
    }

    private static String shorten(String s) {
        if (s == null) return "null";
        s = s.replace("\n", " ").trim();
        return s.length() > 220 ? s.substring(0, 220) + "…" : s;
    }

    /**
     * Statement-length probe. Two shapes matter on a jTDS client:
     *  - no parameters  → jTDS sends the text as a plain batch (most manager sections);
     *  - with parameters → jTDS wraps the text into sp_executesql, where the statement travels as a
     *    LONGVARCHAR parameter and the driver has to chunk it.
     * Both are grown with an ASCII comment so character and byte counts move together.
     */
    private static void sqlLengthProbe() throws Exception {
        System.out.println("\n================ jTDS statement-length probe ================");
        Connection c = null;
        try {
            c = MeelanoSql.lease();
            Set<String> cols = ManagerAnalytics.columns(c, "sailfact");
            String numExpr = ManagerAnalytics.sqlNumberExpr("h", "all", "decimal(19,2)");
            String soft = ManagerAnalytics.softDeleteCondition(cols, "x");
            String where = MeelanoSql.rangeCondition("date", MeelanoSql.rangeAnchor(c), 2, "h");
            if (!soft.isEmpty()) where += " AND " + soft;
            String head = "SELECT ISNULL(SUM(" + numExpr + "),0), COUNT_BIG(1), COUNT(DISTINCT h.[shmo]) FROM dbo.[sailfact] h"
                    + " WHERE " + where;
            System.out.println("  base statement: " + head.length() + " chars / " + (head.length() * 2) + " utf16 bytes");
            for (int targetLen : new int[]{4000, 6000, 8000, 12000, 16000, 20000}) {
                String sql = head + " /*" + pad(targetLen - head.length() - 5) + "*/";
                String plain;
                long t0 = System.currentTimeMillis();
                try (PreparedStatement ps = c.prepareStatement(sql)) {
                    try (ResultSet r = ps.executeQuery()) { plain = r.next() ? ("OK " + r.getBigDecimal(1)) : "OK (no row)"; }
                } catch (Exception ex) { plain = "ERR " + ex.getMessage(); }
                long plainMs = System.currentTimeMillis() - t0;
                String param;
                t0 = System.currentTimeMillis();
                try (PreparedStatement ps = c.prepareStatement(sql + " AND (N'x' = ? OR ? IS NULL)")) {
                    ps.setString(1, "x");
                    ps.setString(2, "y");
                    try (ResultSet r = ps.executeQuery()) { param = r.next() ? ("OK " + r.getBigDecimal(1)) : "OK (no row)"; }
                } catch (Exception ex) { param = "ERR " + ex.getMessage(); }
                long paramMs = System.currentTimeMillis() - t0;
                System.out.println("  len=" + sql.length() + " chars (" + (sql.length() * 2) + " bytes)  plain: " + plainMs + "ms "
                        + plain + "   |   with params: " + paramMs + "ms " + param);
            }
        } finally {
            if (c != null) try { c.close(); } catch (Exception ignored) { }
        }
    }

    private static String pad(int n) {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < n; i++) b.append('p');
        return b.toString();
    }

    /** Positive control for the local parenthesis guard: an unbalanced statement must fail locally. */
    private static void guardProbe() throws Exception {
        System.out.println("\n================ parenthesis guard ================");
        Connection c = null;
        try {
            c = MeelanoSql.lease();
            String[] samples = {
                    "SELECT COUNT_BIG(1) FROM dbo.[sailfact] h WHERE (1=1) AND (2=2)",
                    "SELECT COUNT_BIG(1) FROM dbo.[sailfact] h WHERE (1=1 AND (2=2)",   // unbalanced on purpose
                    "SELECT N')' , N'(' FROM dbo.[sailfact] h"                            // literals must be ignored
            };
            for (String sql : samples) {
                String verdict;
                try (PreparedStatement ps = c.prepareStatement(sql)) {
                    try (ResultSet r = ps.executeQuery()) {
                        verdict = r.next() ? ("OK " + r.getString(1)) : "OK";
                    }
                } catch (Exception ex) {
                    verdict = "ERR " + ex.getMessage();
                }
                System.out.println("  " + (sql.length() > 70 ? sql.substring(0, 70) + "…" : sql) + "  ->  " + verdict);
            }
        } finally {
            if (c != null) try { c.close(); } catch (Exception ignored) { }
        }
    }
}

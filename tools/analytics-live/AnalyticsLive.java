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
 * KPI can be traced to a line of SQL instead of guessed from a screenshot.
 *
 * Prints a plain-text report (stdout) that the workflow commits as diag/analytics-live.txt.
 */
public final class AnalyticsLive {

    private static long sectionMs;

    public static void main(String[] args) {
        System.out.println("== AnalyticsLive ==");
        System.out.println("app build: MeelanoSql + ManagerAnalytics, jTDS driver "
                + net.sourceforge.jtds.jdbc.Driver.class.getPackage().getImplementationVersion());
        System.out.println("server: " + MainActivity.sqlHost() + ":" + MainActivity.sqlPort() + "/" + MainActivity.sqlDatabase());
        boolean anyError = false;

        int[] ranges = {1, 7, 30};
        for (int range : ranges) {
            Connection c = null;
            try {
                c = MeelanoSql.lease();
                long t0 = System.currentTimeMillis();
                JSONObject j = ManagerAnalytics.fetch(c, range);
                long ms = System.currentTimeMillis() - t0;
                System.out.println("\n================ fetch(range=" + range + ")  " + ms + " ms ================");
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
                System.out.println("-- metrics --");
                System.out.println(j.toString(2));
            } catch (Throwable t) {
                anyError = true;
                System.out.println("FATAL for range " + range + ": " + t);
                t.printStackTrace(System.out);
            } finally {
                if (c != null) try { c.close(); } catch (Exception ignored) { }
            }
        }

        try { sqlLengthProbe(); } catch (Throwable t) { System.out.println("probe failed: " + t); }
        try { guardProbe(); } catch (Throwable t) { System.out.println("guard probe failed: " + t); }

        System.out.println("\nMeelanoSql.stats = " + MeelanoSql.stats());
        System.out.println("ANYERR=" + (anyError ? 1 : 0));
    }

    /**
     * The app's sales statement is ~4.3k characters. jTDS sends a prepared statement through
     * sp_executesql with the SQL text as a parameter, so the exact length matters: this probe runs the
     * same app-shaped statement padded to increasing lengths and reports where the server starts
     * rejecting it. Pad is an ASCII comment, so byte and character counts move together.
     */
    private static void sqlLengthProbe() throws Exception {
        System.out.println("\n================ jTDS statement-length probe ================");
        Connection c = null;
        try {
            c = MeelanoSql.lease();
            Set<String> cols = ManagerAnalytics.columns(c, "sailfact");
            String numExpr = ManagerAnalytics.sqlNumberExpr("h", "all", "decimal(19,2)");
            String soft = ManagerAnalytics.softDeleteCondition(cols, "x");
            String src = "(SELECT x.* FROM dbo.[sailfact] x WHERE " + MeelanoSql.rangeCondition("date", MeelanoSql.rangeAnchor(c), 30, "x")
                    + " AND " + soft + ") h";
            String head = "SELECT ISNULL(SUM(" + numExpr + "),0), COUNT_BIG(1), COUNT(DISTINCT h.[shmo]) FROM " + src
                    + " WHERE (N''''=''?'' OR ? IS NULL)";
            // a real parameter keeps jTDS on the sp_executesql path, exactly like ManagerAnalytics
            for (int targetLen : new int[]{2500, 3500, 3900, 3990, 4000, 4010, 4050, 4100, 4200, 4300, 4500}) {
                String sql = head;
                if (sql.length() < targetLen) {
                    sql = sql + " /*" + pad(targetLen - sql.length() - 5) + "*/";
                }
                long t0 = System.currentTimeMillis();
                String verdict;
                try (PreparedStatement ps = c.prepareStatement(sql)) {
                    ps.setString(1, "x");
                    ps.setString(2, "y");
                    try (ResultSet r = ps.executeQuery()) {
                        verdict = r.next() ? ("OK rows=" + r.getBigDecimal(1) + "/" + r.getLong(2)) : "OK (no row)";
                    }
                } catch (Exception ex) {
                    verdict = "ERR " + ex.getClass().getSimpleName() + ": " + ex.getMessage();
                }
                System.out.println("  len=" + sql.length() + " chars (" + (sql.length() * 2) + " utf16 bytes)  "
                        + (System.currentTimeMillis() - t0) + " ms  -> " + verdict);
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

    /** Positive control for the local parenthesis guard added to MeelanoSql: it must reject, locally. */
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

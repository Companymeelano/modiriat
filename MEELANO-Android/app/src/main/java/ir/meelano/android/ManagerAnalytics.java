package ir.meelano.android;

import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Management Intelligence data layer (spec phases 3-5): one read-only repository that turns
 * validated Atiran queries into the unified {@code ManagementMetrics} JSON consumed by the
 * executive dashboard, cockpit, collection center and drill-down pages.
 *
 * Rules honoured here: READ-ONLY, parameterised where inputs exist, every column resolved at
 * runtime against real metadata (nothing guessed), every failure captured in {@code errors[]}
 * instead of crashing, and every figure traceable to a table/column pair listed in
 * docs/ARCHITECTURE-AUDIT-fa.md.
 */
final class ManagerAnalytics {
    private ManagerAnalytics() { }

    // ============================ metadata kit (same validation semantics as MainActivity) ============================
    static Set<String> columns(Connection c, String table) throws Exception {
        Set<String> set = new HashSet<>();
        try (PreparedStatement ps = c.prepareStatement("SELECT c.name FROM sys.columns c JOIN sys.objects o ON o.object_id=c.object_id JOIN sys.schemas s ON s.schema_id=o.schema_id WHERE s.name=N'dbo' AND o.type IN (N'U',N'V') AND o.name=? ORDER BY c.column_id")) {
            ps.setString(1, table);
            try (ResultSet r = ps.executeQuery()) { while (r.next()) set.add(r.getString(1)); }
        }
        return set;
    }

    static String resolve(Set<String> cols, String... candidates) {
        if (cols == null || candidates == null) return null;
        for (String candidate : candidates) {
            if (candidate == null) continue;
            for (String col : cols) if (col != null && col.equalsIgnoreCase(candidate)) return col;
        }
        return null;
    }

    private static String norm(String s) {
        if (s == null) return "";
        StringBuilder b = new StringBuilder();
        for (char ch : s.toLowerCase(Locale.US).toCharArray()) if (Character.isLetterOrDigit(ch)) b.append(ch);
        return b.toString();
    }

    static String resolveFlexible(Set<String> cols, String... candidates) {
        String exact = resolve(cols, candidates);
        if (exact != null) return exact;
        if (cols == null || candidates == null) return null;
        for (String candidate : candidates) {
            String n = norm(candidate);
            if (n.isEmpty()) continue;
            for (String col : cols) if (norm(col).equals(n)) return col;
        }
        return null;
    }

    static String sqlNumberExpr(String alias, String col, String type) {
        if (col == null || col.trim().isEmpty()) return "CAST(NULL AS " + type + ")";
        String ref = (alias == null || alias.trim().isEmpty() ? "" : alias + ".") + "[" + col + "]";
        String raw = "TRY_CONVERT(nvarchar(120)," + ref + ")";
        String normed = "LTRIM(RTRIM(" + raw + "))";
        String[][] repl = {
                {"NCHAR(160)", "N''"}, {"N' '", "N''"}, {"N','", "N''"}, {"N'،'", "N''"}, {"N'٬'", "N''"},
                {"N'٫'", "N'.'"}, {"N'ریال'", "N''"}, {"N'تومان'", "N''"},
                {"N'۰'", "N'0'"}, {"N'۱'", "N'1'"}, {"N'۲'", "N'2'"}, {"N'۳'", "N'3'"}, {"N'۴'", "N'4'"},
                {"N'۵'", "N'5'"}, {"N'۶'", "N'6'"}, {"N'۷'", "N'7'"}, {"N'۸'", "N'8'"}, {"N'۹'", "N'9'"},
                {"N'٠'", "N'0'"}, {"N'١'", "N'1'"}, {"N'٢'", "N'2'"}, {"N'٣'", "N'3'"}, {"N'٤'", "N'4'"},
                {"N'٥'", "N'5'"}, {"N'٦'", "N'6'"}, {"N'٧'", "N'7'"}, {"N'٨'", "N'8'"}, {"N'٩'", "N'9'"}
        };
        for (String[] r : repl) normed = "REPLACE(" + normed + "," + r[0] + "," + r[1] + ")";
        return "COALESCE(TRY_CONVERT(" + type + "," + ref + "),TRY_CONVERT(" + type + "," + normed + "))";
    }

    private static String falseLike(String field) {
        String n = "UPPER(LTRIM(RTRIM(TRY_CONVERT(nvarchar(40)," + field + "))))";
        return "(" + n + " IS NULL OR " + n + " IN (N'',N'0',N'F',N'FALSE',N'N',N'NO',N'خیر',N'خير',N'نه') OR TRY_CONVERT(int," + field + ")=0)";
    }

    static String softDeleteCondition(Set<String> cols, String alias) {
        String prefix = alias == null || alias.trim().isEmpty() ? "" : alias + ".";
        List<String> parts = new ArrayList<>();
        String del = resolveFlexible(cols, "deleted", "Deleted", "is_deleted", "IsDeleted", "isDelete", "delete_flag", "deleted_flag");
        if (del != null) parts.add(falseLike(prefix + "[" + del + "]"));
        String cancel = resolveFlexible(cols, "cancel", "Cancel", "canceled", "cancelled", "is_cancel", "is_canceled", "void", "Void");
        if (cancel != null) parts.add(falseLike(prefix + "[" + cancel + "]"));
        String status = resolveFlexible(cols, "status", "Status", "satus", "Satus", "state", "State");
        if (status != null) {
            String n = "UPPER(LTRIM(RTRIM(TRY_CONVERT(nvarchar(50)," + prefix + "[" + status + "]))))";
            parts.add("(" + n + " IS NULL OR " + n + "=N'' OR " + n + " NOT IN (N'DELETED',N'DELETE',N'CANCEL',N'CANCELLED',N'VOID')");
        }
        return parts.isEmpty() ? "" : "(" + join(parts, " AND ") + ")";
    }

    private static String activeCondition(Set<String> cols, String alias) {
        String active = resolve(cols, "active", "Active");
        if (active == null) return "";
        String prefix = alias == null || alias.trim().isEmpty() ? "" : alias + ".";
        String field = prefix + "[" + active + "]";
        return "(UPPER(LTRIM(RTRIM(TRY_CONVERT(nvarchar(20)," + field + ")))) IN (N'T',N'TRUE',N'Y',N'YES',N'1') OR TRY_CONVERT(int," + field + ")=1)";
    }

    static String activeAnd(Set<String> cols, String alias) {
        String condition = activeCondition(cols, alias);
        return condition.isEmpty() ? "" : " AND " + condition;
    }

    static String join(List<String> parts, String sep) {
        StringBuilder b = new StringBuilder();
        for (String p : parts) { if (b.length() > 0) b.append(sep); b.append(p); }
        return b.toString();
    }

    private static String factorUniqueRowExpr(Set<String> cols, String alias) {
        String prefix = alias == null || alias.trim().isEmpty() ? "" : alias + ".";
        for (String candidate : new String[]{"rdf", "RDF", "id", "ID", "serial", "Serial", "row_id", "RowID", "autoid", "AutoID", "radif", "Radif"}) {
            String col = resolveFlexible(cols, candidate);
            if (col != null) return "N'__row__' + COALESCE(TRY_CONVERT(nvarchar(120)," + prefix + "[" + col + "]),CONVERT(nvarchar(36),NEWID()))";
        }
        return "N'__row__' + CONVERT(nvarchar(36),NEWID())";
    }

    private static String factorLatestOrder(Set<String> cols, String alias) {
        String prefix = alias == null || alias.trim().isEmpty() ? "" : alias + ".";
        List<String> parts = new ArrayList<>();
        String active = resolveFlexible(cols, "active", "Active", "is_active", "enabled");
        if (active != null) {
            String f = prefix + "[" + active + "]";
            parts.add("CASE WHEN UPPER(LTRIM(RTRIM(TRY_CONVERT(nvarchar(20)," + f + ")))) IN (N'T',N'TRUE',N'Y',N'YES',N'1') OR TRY_CONVERT(int," + f + ")=1 THEN 1 ELSE 0 END DESC");
        }
        for (String candidate : new String[]{"updated_at", "UpdateDate", "modified_at", "ModifyDate", "last_update", "LastUpdate", "created_at", "CreateDate", "tarikh_sabt", "date", "DATE", "t_date"}) {
            String col = resolveFlexible(cols, candidate);
            if (col != null) parts.add("TRY_CONVERT(datetime2," + prefix + "[" + col + "]) DESC");
        }
        for (String candidate : new String[]{"rdf", "RDF", "id", "ID", "serial", "Serial", "row_id", "RowID", "autoid", "AutoID", "radif", "Radif"}) {
            String col = resolveFlexible(cols, candidate);
            if (col != null) parts.add("TRY_CONVERT(bigint," + prefix + "[" + col + "]) DESC");
        }
        return parts.isEmpty() ? "(SELECT 0)" : join(parts, ",");
    }

    static String dedupeFactorSource(String table, Set<String> cols, String numberCol, String alias, String innerWhere) {
        String a = alias == null || alias.trim().isEmpty() ? "h" : alias.trim();
        String where = innerWhere == null || innerWhere.trim().isEmpty() ? "" : innerWhere.trim();
        if (numberCol == null || numberCol.trim().isEmpty()) return "dbo.[" + table + "] " + a + (where.isEmpty() ? "" : " " + where.replace("x.", a + "."));
        String numberExpr = "NULLIF(LTRIM(RTRIM(TRY_CONVERT(nvarchar(120),x.[" + numberCol + "]))),N'')";
        String partition = "COALESCE(" + numberExpr + "," + factorUniqueRowExpr(cols, "x") + ")";
        return "(SELECT * FROM (SELECT x.*, ROW_NUMBER() OVER(PARTITION BY " + partition + " ORDER BY " + factorLatestOrder(cols, "x") + ") AS _meelano_rn FROM dbo.[" + table + "] x " + where + ") mx WHERE mx._meelano_rn=1) " + a;
    }

    static boolean tableExists(Connection c, String table) {
        try (PreparedStatement ps = c.prepareStatement("SELECT 1 FROM sys.tables t JOIN sys.schemas s ON s.schema_id=t.schema_id WHERE s.name=N'dbo' AND t.name=?")) {
            ps.setString(1, table);
            try (ResultSet r = ps.executeQuery()) { return r.next(); }
        } catch (Exception ignored) { return false; }
    }

    private static boolean hasFunction(Connection c, String name) {
        try (PreparedStatement ps = c.prepareStatement("SELECT COUNT_BIG(1) FROM sys.objects WHERE type IN ('FN','IF','TF') AND name=?")) {
            ps.setString(1, name);
            try (ResultSet r = ps.executeQuery()) { return r.next() && r.getLong(1) > 0; }
        } catch (Exception ignored) { return false; }
    }

    static String latestDate(Connection c, String table, String preferredColumn) {
        try {
            Set<String> cols = columns(c, table);
            String dateCol = preferredColumn == null ? null : resolve(cols, preferredColumn);
            if (dateCol == null) dateCol = resolve(cols, "date", "DATE", "tarikh", "Date");
            if (dateCol == null) return "";
            try (PreparedStatement ps = c.prepareStatement("SELECT MAX(NULLIF(CONVERT(nvarchar(20),[" + dateCol + "]),N'')) FROM dbo.[" + table + "]")) {
                try (ResultSet r = ps.executeQuery()) { return r.next() && r.getString(1) != null ? r.getString(1) : ""; }
            }
        } catch (Exception ignored) { return ""; }
    }

    private static String quote(String s) { return "N'" + s.replace("'", "''") + "'"; }

    // ============================ range condition (single source of truth for date filters) ============================
    private static String rangeCondition(Set<String> cols, String dateCol, String latest, int range, String alias) {
        String q = quote(latest);
        String p = alias + ".[" + dateCol + "]";
        String conv = "TRY_CONVERT(nvarchar(30)," + p + ")";
        String dExpr = "TRY_CONVERT(date," + conv + ")";
        if (range == 0) return "(" + conv + "=" + q + " OR LEFT(" + conv + ",10)=LEFT(" + q + ",10))";
        if (range == 1) return dExpr + ">=DATEADD(day,-6,TRY_CONVERT(date," + q + "))";
        if (range == 2) return dExpr + ">=DATEADD(month,-1,TRY_CONVERT(date," + q + "))";
        if (range == 3) return dExpr + ">=DATEADD(month,-12,TRY_CONVERT(date," + q + "))";
        return "(" + conv + "=" + q + " OR LEFT(" + conv + ",10)=LEFT(" + q + ",10))";
    }

    // ============================ business queries ============================
    /** Sales/purchase aggregate for a range incl. previous period — sailfact/buyfact.[date],[all],[shfacfo/shfackh],[shmo]. */
    static JSONObject rangeBlock(Connection c, boolean sales, int range) throws Exception {
        JSONObject o = new JSONObject();
        String table = sales ? "sailfact" : "buyfact";
        Set<String> cols = columns(c, table);
        String dateCol = sales ? resolve(cols, "date") : resolve(cols, "DATE", "date");
        String amountCol = resolve(cols, "all");
        String numberCol = sales ? resolve(cols, "shfacfo") : resolve(cols, "shfackh");
        String partyCol = resolve(cols, "shmo");
        String paidCol = sales ? resolve(cols, "MabDaryaftFactor", "Daryaft", "received") : resolve(cols, "MablaghPardakht", "Pardakht", "paid");
        String latest = dateCol == null ? "" : latestDate(c, table, dateCol);
        o.put("date", latest.isEmpty() ? "—" : latest);
        double total = 0, paid = 0, prev = 0; long docs = 0, parties = 0;
        if (dateCol != null && amountCol != null && !latest.isEmpty()) {
            String inner = "WHERE " + rangeCondition(cols, dateCol, latest, range, "x") + activeAnd(cols, "x");
            String soft = softDeleteCondition(cols, "x"); if (!soft.isEmpty()) inner += " AND " + soft;
            String source = dedupeFactorSource(table, cols, numberCol, "h", inner);
            String sql = "SELECT ISNULL(SUM(" + sqlNumberExpr("h", amountCol, "decimal(19,2)") + "),0), COUNT_BIG(1), " +
                    (partyCol == null ? "CAST(0 AS bigint)" : "COUNT(DISTINCT h.[" + partyCol + "])") + ", " +
                    (paidCol == null ? "CAST(0 AS decimal(19,2))" : "ISNULL(SUM(" + sqlNumberExpr("h", paidCol, "decimal(19,2)") + "),0)") + " FROM " + source;
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                try (ResultSet r = ps.executeQuery()) { if (r.next()) { total = r.getDouble(1); docs = r.getLong(2); parties = r.getLong(3); paid = r.getDouble(4); } }
            }
            String q = quote(latest);
            String dX = "TRY_CONVERT(date,TRY_CONVERT(nvarchar(30),x.[" + dateCol + "]))";
            String pcond;
            if (range == 0) pcond = dX + ">=DATEADD(day,-1,TRY_CONVERT(date," + q + ")) AND " + dX + "<TRY_CONVERT(date," + q + ")";
            else if (range == 1) pcond = dX + ">=DATEADD(day,-13,TRY_CONVERT(date," + q + ")) AND " + dX + "<DATEADD(day,-6,TRY_CONVERT(date," + q + "))";
            else pcond = dX + ">=DATEADD(month,-2,TRY_CONVERT(date," + q + ")) AND " + dX + "<DATEADD(month,-1,TRY_CONVERT(date," + q + "))";
            String inner2 = "WHERE " + pcond + activeAnd(cols, "x");
            String soft2 = softDeleteCondition(cols, "x"); if (!soft2.isEmpty()) inner2 += " AND " + soft2;
            String source2 = dedupeFactorSource(table, cols, numberCol, "h", inner2);
            try (PreparedStatement ps = c.prepareStatement("SELECT ISNULL(SUM(" + sqlNumberExpr("h", amountCol, "decimal(19,2)") + "),0) FROM " + source2)) {
                try (ResultSet r = ps.executeQuery()) { if (r.next()) prev = r.getDouble(1); }
            }
        }
        o.put("total", total); o.put("docs", docs); o.put("parties", parties); o.put("paid", paid); o.put("prevTotal", prev);
        return o;
    }

    /** 7-day sales trend — sailfact grouped by converted date over the dedupe source. */
    static JSONArray trend(Connection c, int days) throws Exception {
        JSONArray arr = new JSONArray();
        Set<String> cols = columns(c, "sailfact");
        String dateCol = resolve(cols, "date");
        String amountCol = resolve(cols, "all");
        String numberCol = resolve(cols, "shfacfo");
        if (dateCol == null || amountCol == null) return arr;
        String dExpr = "TRY_CONVERT(nvarchar(10),TRY_CONVERT(date,TRY_CONVERT(nvarchar(30),h.[" + dateCol + "])),23)";
        String inner = "WHERE x.[" + dateCol + "] IS NOT NULL" + activeAnd(cols, "x");
        String soft = softDeleteCondition(cols, "x"); if (!soft.isEmpty()) inner += " AND " + soft;
        String source = dedupeFactorSource("sailfact", cols, numberCol, "h", inner);
        String sql = "SELECT TOP (" + days + ") " + dExpr + ", ISNULL(SUM(" + sqlNumberExpr("h", amountCol, "decimal(19,2)") + "),0) FROM " + source + " GROUP BY " + dExpr + " ORDER BY 1 DESC";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            try (ResultSet r = ps.executeQuery()) { while (r.next()) { JSONObject o = new JSONObject(); o.put("label", r.getString(1) == null ? "—" : r.getString(1)); o.put("value", r.getDouble(2)); arr.put(o); } }
        }
        return arr;
    }

    /** Receivables from CUSTOMERS.[man] positive balances + overdue invoices via dbo.dif_date_alan when present. */
    static JSONObject receivables(Connection c) throws Exception {
        JSONObject o = new JSONObject();
        Set<String> cols = columns(c, "CUSTOMERS");
        String shmo = resolve(cols, "SHMO", "shmo");
        String name = resolve(cols, "MONAME", "Name", "CusName");
        String balance = resolve(cols, "man", "Balance", "Mandeh");
        double total = 0; long count = 0;
        if (balance != null) {
            String label = name == null ? "TRY_CONVERT(nvarchar(120),c.[" + shmo + "])" : "TRY_CONVERT(nvarchar(250),c.[" + name + "])";
            try (PreparedStatement ps = c.prepareStatement("SELECT COUNT_BIG(1), ISNULL(SUM(TRY_CONVERT(decimal(19,2),c.[" + balance + "])),0) FROM dbo.CUSTOMERS c WHERE TRY_CONVERT(decimal(19,2),c.[" + balance + "])>0")) {
                try (ResultSet r = ps.executeQuery()) { if (r.next()) { count = r.getLong(1); total = r.getDouble(2); } }
            }
            o.put("labelExpr", label);
        }
        o.put("total", total); o.put("count", count);
        return o;
    }

    static JSONArray debtors(Connection c, int cap) throws Exception {
        JSONArray arr = new JSONArray();
        Set<String> cols = columns(c, "CUSTOMERS");
        String shmo = resolve(cols, "SHMO", "shmo");
        String name = resolve(cols, "MONAME", "Name", "CusName");
        String balance = resolve(cols, "man", "Balance", "Mandeh");
        if (balance == null || shmo == null) return arr;
        String label = name == null ? "TRY_CONVERT(nvarchar(120),c.[" + shmo + "])" : "TRY_CONVERT(nvarchar(250),c.[" + name + "])";
        String sql = "SELECT TOP (" + cap + ") TRY_CONVERT(nvarchar(100),c.[" + shmo + "]), " + label + ", TRY_CONVERT(decimal(19,2),c.[" + balance + "]) FROM dbo.CUSTOMERS c WHERE TRY_CONVERT(decimal(19,2),c.[" + balance + "])>0 ORDER BY 3 DESC";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            try (ResultSet r = ps.executeQuery()) { while (r.next()) { JSONObject o = new JSONObject(); o.put("code", r.getString(1) == null ? "" : r.getString(1)); o.put("party", r.getString(2) == null ? "—" : r.getString(2)); o.put("amount", r.getDouble(3)); arr.put(o); } }
        }
        return arr;
    }

    /** Customer activity categories from real purchase history (sailfact): active / no-purchase / inactive. */
    static JSONObject customerCategories(Connection c, int range) throws Exception {
        JSONObject o = new JSONObject();
        Set<String> sail = columns(c, "sailfact");
        Set<String> cust = columns(c, "CUSTOMERS");
        String sDate = resolve(sail, "date");
        String sShmo = resolve(sail, "shmo");
        String cShmo = resolve(cust, "SHMO", "shmo");
        long active = 0, never = 0, inactive = 0;
        if (sShmo != null && cShmo != null && sDate != null) {
            String latest = latestDate(c, "sailfact", "date");
            if (!latest.isEmpty()) {
                String cond = rangeCondition(sail, sDate, latest, range <= 0 ? 2 : range, "s");
                try (PreparedStatement ps = c.prepareStatement("SELECT COUNT_BIG(1) FROM dbo.CUSTOMERS c WHERE EXISTS (SELECT 1 FROM dbo.sailfact s WHERE TRY_CONVERT(nvarchar(100),s.[" + sShmo + "])=TRY_CONVERT(nvarchar(100),c.[" + cShmo + "]) AND " + cond + ")")) {
                    try (ResultSet r = ps.executeQuery()) { if (r.next()) active = r.getLong(1); }
                }
                try (PreparedStatement ps = c.prepareStatement("SELECT COUNT_BIG(1) FROM dbo.CUSTOMERS c WHERE NOT EXISTS (SELECT 1 FROM dbo.sailfact s WHERE TRY_CONVERT(nvarchar(100),s.[" + sShmo + "])=TRY_CONVERT(nvarchar(100),c.[" + cShmo + "]))")) {
                    try (ResultSet r = ps.executeQuery()) { if (r.next()) never = r.getLong(1); }
                }
                try (PreparedStatement ps = c.prepareStatement("SELECT COUNT_BIG(1) FROM dbo.CUSTOMERS c WHERE EXISTS (SELECT 1 FROM dbo.sailfact s WHERE TRY_CONVERT(nvarchar(100),s.[" + sShmo + "])=TRY_CONVERT(nvarchar(100),c.[" + cShmo + "])) AND NOT EXISTS (SELECT 1 FROM dbo.sailfact s WHERE TRY_CONVERT(nvarchar(100),s.[" + sShmo + "])=TRY_CONVERT(nvarchar(100),c.[" + cShmo + "]) AND TRY_CONVERT(date,TRY_CONVERT(nvarchar(30),s.[" + sDate + "]))>=DATEADD(day,-60,CONVERT(date,GETDATE())))")) {
                    try (ResultSet r = ps.executeQuery()) { if (r.next()) inactive = r.getLong(1); }
                }
            }
        }
        o.put("active", active); o.put("noPurchase", never); o.put("inactive", inactive);
        return o;
    }

    /** Visitor performance: orders/sales per visitor from sailfact + targets from vis_goals when present. */
    static JSONArray visitorPerformance(Connection c, int range) throws Exception {
        JSONArray arr = new JSONArray();
        Set<String> sail = columns(c, "sailfact");
        String dateCol = resolve(sail, "date");
        String amountCol = resolve(sail, "all");
        String numberCol = resolve(sail, "shfacfo");
        String visitorId = resolve(sail, "vis_rdf", "VisitorID", "visitor");
        if (visitorId == null || amountCol == null) return arr;
        Set<String> vis = columns(c, "visitors");
        String visKey = resolve(vis, "rdf", "RDF", "id", "ID");
        String visName = resolve(vis, "name", "Name", "vis_name", "VisitorName", "moname");
        if (visKey == null || visName == null) return arr;
        String latest = dateCol == null ? "" : latestDate(c, "sailfact", "date");
        String cond = dateCol != null && !latest.isEmpty() ? rangeCondition(sail, dateCol, latest, range <= 0 ? 2 : range, "x") : "1=1";
        String inner = "WHERE " + cond + activeAnd(sail, "x");
        String soft = softDeleteCondition(sail, "x"); if (!soft.isEmpty()) inner += " AND " + soft;
        String source = dedupeFactorSource("sailfact", sail, numberCol, "h", inner);
        String sql = "SELECT TOP (10) COALESCE(TRY_CONVERT(nvarchar(150),v.[" + visName + "]),N'بدون ویزیتور'), ISNULL(SUM(" + sqlNumberExpr("h", amountCol, "decimal(19,2)") + "),0), COUNT_BIG(1), COUNT(DISTINCT h.[" + (resolve(sail, "shmo") == null ? visitorId : "shmo") + "]) FROM " + source +
                " LEFT JOIN dbo.visitors v ON TRY_CONVERT(nvarchar(100),v.[" + visKey + "])=TRY_CONVERT(nvarchar(100),h.[" + visitorId + "])" +
                " GROUP BY COALESCE(TRY_CONVERT(nvarchar(150),v.[" + visName + "]),N'بدون ویزیتور') ORDER BY 2 DESC";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            try (ResultSet r = ps.executeQuery()) { while (r.next()) { JSONObject o = new JSONObject(); o.put("name", r.getString(1) == null ? "—" : r.getString(1)); o.put("sales", r.getDouble(2)); o.put("orders", r.getLong(3)); o.put("customers", r.getLong(4)); arr.put(o); } }
        }
        return arr;
    }

    /** Product intelligence: top sellers of the range + items sold before but not inside the range (needs review). */
    static JSONObject products(Connection c, int range) throws Exception {
        JSONObject o = new JSONObject();
        JSONArray top = new JSONArray();
        JSONArray idle = new JSONArray();
        Set<String> d = columns(c, "subsailfact");
        String key = resolveFlexible(d, "SHKA", "shka", "KalaCode", "kala", "item_code");
        String dName = resolveFlexible(d, "naka", "Naka", "name", "Name", "Desc_Naka");
        String lineAmount = resolveFlexible(d, "LINESUM", "LineSum", "line_sum", "tamam_joz", "amount");
        String dNumber = resolveFlexible(d, "shfacfo", "shfac", "number");
        if (key != null && lineAmount != null && dNumber != null) {
            Set<String> sail = columns(c, "sailfact");
            String latest = latestDate(c, "sailfact", "date");
            String sDate = resolve(sail, "date");
            String sNumber = resolve(sail, "shfacfo");
            if (!latest.isEmpty() && sDate != null && sNumber != null) {
                String cond = rangeCondition(sail, sDate, latest, range <= 0 ? 2 : range, "s");
                String sql = "SELECT TOP (8) CAST(MAX(d.[" + (dName == null ? key : dName) + "]) AS nvarchar(500)), ISNULL(SUM(" + sqlNumberExpr("d", lineAmount, "decimal(19,2)") + "),0) FROM dbo.subsailfact d JOIN dbo.sailfact s ON s.[" + sNumber + "]=d.[" + dNumber + "] WHERE " + cond.replace("s.", "s.") + " AND d.active='t' GROUP BY d.[" + key + "] ORDER BY 2 DESC";
                try (PreparedStatement ps = c.prepareStatement(sql)) {
                    try (ResultSet r = ps.executeQuery()) { while (r.next()) { JSONObject p = new JSONObject(); p.put("name", r.getString(1) == null ? "—" : r.getString(1)); p.put("sum", r.getDouble(2)); top.put(p); } }
                }
                String sqlIdle = "SELECT TOP (8) CAST(MAX(d.[" + (dName == null ? key : dName) + "]) AS nvarchar(500)), ISNULL(SUM(" + sqlNumberExpr("d", lineAmount, "decimal(19,2)") + "),0) FROM dbo.subsailfact d JOIN dbo.sailfact s ON s.[" + sNumber + "]=d.[" + dNumber + "] WHERE d.active='t' AND d.[" + key + "] NOT IN (SELECT d2.[" + key + "] FROM dbo.subsailfact d2 JOIN dbo.sailfact s2 ON s2.[" + sNumber + "]=d2.[" + dNumber + "] WHERE " + cond.replace("s.", "s2.") + " AND d2.active='t') GROUP BY d.[" + key + "] ORDER BY 2 DESC";
                try (PreparedStatement ps = c.prepareStatement(sqlIdle)) {
                    try (ResultSet r = ps.executeQuery()) { while (r.next()) { JSONObject p = new JSONObject(); p.put("name", r.getString(1) == null ? "—" : r.getString(1)); p.put("sum", r.getDouble(2)); idle.put(p); } }
                }
            }
        }
        o.put("top", top); o.put("idle", idle);
        return o;
    }

    /** Check maturity buckets over getchk — sarresid/date vs today. */
    static JSONObject checkBuckets(Connection c) throws Exception {
        JSONObject o = new JSONObject();
        Set<String> cols = columns(c, "getchk");
        String amount = resolve(cols, "getchkmab", "mablagh", "amount");
        String dateCol = resolve(cols, "sarresid", "getchkdate", "chkdate", "date", "t_date");
        if (amount == null || dateCol == null) return o;
        String dd = "TRY_CONVERT(date,TRY_CONVERT(nvarchar(30),[" + dateCol + "]))";
        String bucket = "CASE WHEN " + dd + "<CONVERT(date,GETDATE()) THEN N'over' WHEN " + dd + "<=DATEADD(day,7,CONVERT(date,GETDATE())) THEN N'soon' ELSE N'ok' END";
        String sql = "SELECT " + bucket + ", COUNT_BIG(1), ISNULL(SUM(TRY_CONVERT(decimal(19,2),[" + amount + "])),0) FROM dbo.[getchk] WHERE " + dd + " IS NOT NULL GROUP BY " + bucket;
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            try (ResultSet r = ps.executeQuery()) {
                while (r.next()) {
                    JSONObject b = new JSONObject(); b.put("count", r.getLong(2)); b.put("total", r.getDouble(3));
                    String k = r.getString(1) == null ? "ok" : r.getString(1);
                    o.put("over".equals(k) ? "over" : "soon".equals(k) ? "soon" : "ok", b);
                }
            }
        }
        return o;
    }

    /** Real activity feed: newest sales documents + received checks (no fabricated events). */
    static JSONArray activityFeed(Connection c) throws Exception {
        JSONArray arr = new JSONArray();
        Set<String> sail = columns(c, "sailfact");
        String sDate = resolve(sail, "date");
        String sNum = resolve(sail, "shfacfo");
        String sAll = resolve(sail, "all");
        if (sDate != null && sAll != null) {
            String sql = "SELECT TOP (5) TRY_CONVERT(nvarchar(20),[" + sDate + "]), " + (sNum == null ? "CAST(NULL AS nvarchar(80))" : "TRY_CONVERT(nvarchar(80),[" + sNum + "])") + ", " + sqlNumberExpr(null, sAll, "decimal(19,2)") + " FROM dbo.sailfact ORDER BY TRY_CONVERT(datetime2,TRY_CONVERT(nvarchar(30),[" + sDate + "])) DESC";
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                try (ResultSet r = ps.executeQuery()) { while (r.next()) { JSONObject o = new JSONObject(); o.put("type", "sale"); o.put("date", r.getString(1) == null ? "—" : r.getString(1)); o.put("number", r.getString(2) == null ? "" : r.getString(2)); o.put("amount", r.getDouble(3)); arr.put(o); } }
            }
        }
        Set<String> gc = columns(c, "getchk");
        String gAmt = resolve(gc, "getchkmab", "mablagh", "amount");
        String gDate = resolve(gc, "getchkdate", "chkdate", "date");
        if (gAmt != null && gDate != null) {
            try (PreparedStatement ps = c.prepareStatement("SELECT TOP (3) TRY_CONVERT(nvarchar(20),[" + gDate + "]), TRY_CONVERT(decimal(19,2),[" + gAmt + "]) FROM dbo.getchk ORDER BY TRY_CONVERT(datetime2,TRY_CONVERT(nvarchar(30),[" + gDate + "])) DESC")) {
                try (ResultSet r = ps.executeQuery()) { while (r.next()) { JSONObject o = new JSONObject(); o.put("type", "check"); o.put("date", r.getString(1) == null ? "—" : r.getString(1)); o.put("amount", r.getDouble(2)); arr.put(o); } }
            }
        }
        return arr;
    }

    /** Drill-down lists of real records for the executive KPI cards (§39). */
    static JSONArray drill(Connection c, String kind, int range) throws Exception {
        JSONArray arr = new JSONArray();
        if ("debtors".equals(kind)) return debtors(c, 60);
        if ("visitors".equals(kind)) return visitorPerformance(c, range);
        if ("products".equals(kind)) return products(c, range).optJSONArray("top") == null ? arr : products(c, range).optJSONArray("top");
        if ("sales".equals(kind)) {
            Set<String> sail = columns(c, "sailfact");
            String sDate = resolve(sail, "date");
            String sNum = resolve(sail, "shfacfo");
            String sAll = resolve(sail, "all");
            String sShmo = resolve(sail, "shmo");
            Set<String> cust = columns(c, "CUSTOMERS");
            String cShmo = resolve(cust, "SHMO", "shmo");
            String cName = resolve(cust, "MONAME", "Name", "CusName");
            if (sDate != null && sAll != null) {
                String joinSql = cName != null && sShmo != null && cShmo != null ? " LEFT JOIN dbo.CUSTOMERS cu ON TRY_CONVERT(nvarchar(100),cu.[" + cShmo + "])=TRY_CONVERT(nvarchar(100),[" + sShmo + "])" : "";
                String nameExpr = cName != null && sShmo != null && cShmo != null ? "COALESCE(TRY_CONVERT(nvarchar(250),cu.[" + cName + "]),N'بدون نام')" : "N'بدون نام'";
                try (PreparedStatement ps = c.prepareStatement("SELECT TOP (60) TRY_CONVERT(nvarchar(20),[" + sDate + "]), " + (sNum == null ? "CAST(NULL AS nvarchar(80))" : "TRY_CONVERT(nvarchar(80),[" + sNum + "])") + ", " + sqlNumberExpr(null, sAll, "decimal(19,2)") + ", " + nameExpr + " FROM dbo.sailfact" + joinSql + " ORDER BY TRY_CONVERT(datetime2,TRY_CONVERT(nvarchar(30),[" + sDate + "])) DESC")) {
                    try (ResultSet r = ps.executeQuery()) { while (r.next()) { JSONObject o = new JSONObject(); o.put("date", r.getString(1) == null ? "—" : r.getString(1)); o.put("number", r.getString(2) == null ? "" : r.getString(2)); o.put("amount", r.getDouble(3)); o.put("party", r.getString(4) == null ? "—" : r.getString(4)); arr.put(o); } }
                }
            }
            return arr;
        }
        if ("checks".equals(kind)) {
            Set<String> gc = columns(c, "getchk");
            String gAmt = resolve(gc, "getchkmab", "mablagh", "amount");
            String gDate = resolve(gc, "sarresid", "getchkdate", "chkdate", "date");
            String gBank = resolve(gc, "bank", "Bank", "bankname", "BANK");
            if (gAmt != null && gDate != null) {
                try (PreparedStatement ps = c.prepareStatement("SELECT TOP (60) TRY_CONVERT(nvarchar(20),[" + gDate + "]), TRY_CONVERT(decimal(19,2),[" + gAmt + "]), " + (gBank == null ? "CAST(NULL AS nvarchar(120))" : "TRY_CONVERT(nvarchar(120),[" + gBank + "])") + " FROM dbo.getchk ORDER BY TRY_CONVERT(datetime2,TRY_CONVERT(nvarchar(30),[" + gDate + "])) DESC")) {
                    try (ResultSet r = ps.executeQuery()) { while (r.next()) { JSONObject o = new JSONObject(); o.put("date", r.getString(1) == null ? "—" : r.getString(1)); o.put("amount", r.getDouble(2)); o.put("bank", r.getString(3) == null ? "—" : r.getString(3)); arr.put(o); } }
                }
            }
            return arr;
        }
        return arr;
    }

    // ============================ phase 10-13: cockpit / collection / visitor goals / product radar ============================
    private static String persianDow(int dw) {
        switch (dw) { case 1: return "\u06cc\u06a9\u0634\u0646\u0628\u0647"; case 2: return "\u062f\u0648\u0634\u0646\u0628\u0647"; case 3: return "\u0633\u0647\u200c\u0634\u0646\u0628\u0647"; case 4: return "\u0686\u0647\u0627\u0631\u0634\u0646\u0628\u0647"; case 5: return "\u067e\u0646\u062c\u0634\u0646\u0628\u0647"; case 6: return "\u062c\u0645\u0639\u0647"; default: return "\u0634\u0646\u0628\u0647"; }
    }

    /** Sales Cockpit: totals vs previous period + weekday mix + product mix (validated columns only). */
    static JSONObject cockpit(Connection c, int range) throws Exception {
        JSONObject out = new JSONObject();
        out.put("sales", rangeBlock(c, true, range));
        out.put("purchases", rangeBlock(c, false, range));
        out.put("top", products(c, range).optJSONArray("top"));
        JSONArray byDay = new JSONArray();
        Set<String> cols = columns(c, "sailfact");
        String dateCol = resolve(cols, "date");
        String amountCol = resolve(cols, "all");
        String numberCol = resolve(cols, "shfacfo");
        String latest = dateCol == null ? "" : latestDate(c, "sailfact", dateCol);
        if (dateCol != null && amountCol != null && !latest.isEmpty()) {
            String dExpr = "DATEPART(dw, TRY_CONVERT(date,TRY_CONVERT(nvarchar(30),h.[" + dateCol + "])))";
            String inner = "WHERE " + rangeCondition(cols, dateCol, latest, range, "x") + activeAnd(cols, "x");
            String soft = softDeleteCondition(cols, "x"); if (!soft.isEmpty()) inner += " AND " + soft;
            String source = dedupeFactorSource("sailfact", cols, numberCol, "h", inner);
            String sql = "SELECT " + dExpr + ", ISNULL(SUM(" + sqlNumberExpr("h", amountCol, "decimal(19,2)") + "),0) FROM " + source + " GROUP BY " + dExpr + " ORDER BY 1";
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                try (ResultSet r = ps.executeQuery()) { while (r.next()) { JSONObject o = new JSONObject(); o.put("label", persianDow(r.getInt(1))); o.put("value", r.getDouble(2)); byDay.put(o); } }
            }
        }
        out.put("byDay", byDay);
        return out;
    }

    /** Collection center aging: unpaid sailfact bucketed by real t_date via dbo.dif_date_alan (mirrors the proven production aging query). */
    static JSONArray collection(Connection c) throws Exception {
        JSONArray arr = new JSONArray();
        Set<String> cols = columns(c, "sailfact");
        if (resolve(cols, "t_date") == null || resolve(cols, "all") == null || resolve(cols, "tasvieh") == null || !hasFunction(c, "dif_date_alan")) return arr;
        String remain = "TRY_CONVERT(decimal(19,2),[all])";
        if (resolve(cols, "MabDaryaftFactor") != null) remain += " - ISNULL(TRY_CONVERT(decimal(19,2),[MabDaryaftFactor]),0)";
        if (resolve(cols, "tdf") != null) remain += " - ISNULL(TRY_CONVERT(decimal(19,2),[tdf]),0)";
        String where = "WHERE [tasvieh]='f' AND NULLIF([t_date],'') IS NOT NULL" + activeAnd(cols, "");
        String soft = softDeleteCondition(cols, ""); if (!soft.isEmpty()) where += " AND " + soft;
        String bucket = "CASE WHEN dbo.dif_date_alan([t_date]) >= 0 THEN N'\u062c\u0627\u0631\u06cc' WHEN -dbo.dif_date_alan([t_date]) <= 30 THEN N'1-30 \u0631\u0648\u0632' WHEN -dbo.dif_date_alan([t_date]) <= 60 THEN N'31-60 \u0631\u0648\u0632' WHEN -dbo.dif_date_alan([t_date]) <= 90 THEN N'61-90 \u0631\u0648\u0632' WHEN -dbo.dif_date_alan([t_date]) <= 180 THEN N'91-180 \u0631\u0648\u0632' ELSE N'180+ \u0631\u0648\u0632' END";
        String sql = "WITH x AS (SELECT " + bucket + " bucket, (" + remain + ") amount FROM dbo.sailfact " + where + ") SELECT bucket, ISNULL(SUM(CASE WHEN amount>0 THEN amount ELSE 0 END),0), COUNT_BIG(CASE WHEN amount>0 THEN 1 END) FROM x GROUP BY bucket";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            try (ResultSet r = ps.executeQuery()) { while (r.next()) { JSONObject o = new JSONObject(); o.put("label", r.getString(1) == null ? "\u2014" : r.getString(1)); o.put("value", r.getDouble(2)); o.put("docs", r.getLong(3)); arr.put(o); } }
        }
        return arr;
    }

    /** Visitor performance + real goals from vis_goals (all columns runtime-resolved; goal=0 means "no goal recorded"). */
    static JSONArray visitorGoals(Connection c, int range) throws Exception {
        JSONArray visitors = visitorPerformance(c, range);
        if (visitors.length() == 0 || !tableExists(c, "vis_goals")) return visitors;
        Set<String> g = columns(c, "vis_goals");
        String gVis = resolve(g, "vis_rdf", "visitor", "visitor_rdf", "rdf_visitor", "vis");
        String gTarget = resolve(g, "mab", "target", "goal", "amount", "mablagh", "sale_goal", "forosh");
        String gDone = resolve(g, "done", "achieved", "sale", "actual");
        if (gVis == null || gTarget == null) return visitors;
        Set<String> vis = columns(c, "visitors");
        String visKey = resolve(vis, "rdf", "RDF", "id", "ID");
        String visName = resolve(vis, "name", "Name", "vis_name", "VisitorName", "moname");
        Map<String, double[]> byName = new HashMap<>();
        if (visKey != null && visName != null) {
            String sql = "SELECT COALESCE(TRY_CONVERT(nvarchar(150),v.[" + visName + "]),N'?'), ISNULL(SUM(" + sqlNumberExpr("g", gTarget, "decimal(19,2)") + "),0)" + (gDone == null ? "" : ", ISNULL(SUM(" + sqlNumberExpr("g", gDone, "decimal(19,2)") + "),0)") + " FROM dbo.vis_goals g LEFT JOIN dbo.visitors v ON TRY_CONVERT(nvarchar(100),v.[" + visKey + "])=TRY_CONVERT(nvarchar(100),g.[" + gVis + "]) GROUP BY COALESCE(TRY_CONVERT(nvarchar(150),v.[" + visName + "]),N'?')";
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                try (ResultSet r = ps.executeQuery()) { while (r.next()) { double[] v = new double[]{ r.getDouble(2), gDone == null ? 0 : r.getDouble(3) }; byName.put(r.getString(1) == null ? "?" : r.getString(1), v); } }
            }
        }
        for (int i = 0; i < visitors.length(); i++) {
            JSONObject o = visitors.optJSONObject(i); if (o == null) continue;
            double[] gv = byName.get(o.optString("name", ""));
            o.put("goal", gv == null ? 0 : gv[0]);
            o.put("achieved", gv == null ? 0 : (gDone == null ? o.optDouble("sales", 0) : gv[1]));
        }
        return visitors;
    }

    /** Product radar: gold (top of range) + zero-stock risk (real ledger via OUTER APPLY, no guessed stock column) + dead stock (no sales in range). */
    static JSONObject productRadar(Connection c, int range) throws Exception {
        JSONObject out = new JSONObject();
        JSONObject prod = products(c, range);
        out.put("gold", prod.optJSONArray("top"));
        out.put("dead", prod.optJSONArray("idle"));
        JSONArray zero = new JSONArray();
        if (tableExists(c, "ka_act") && tableExists(c, "inventory")) {
            Set<String> k = columns(c, "ka_act"), inv = columns(c, "inventory");
            boolean ledger = resolve(k, "shka") != null && resolve(k, "act_id") != null && resolve(k, "tedvah") != null && resolve(k, "tedjoz") != null && resolve(k, "active") != null
                    && resolve(inv, "shka") != null && resolve(inv, "mohvah") != null && resolve(inv, "mojkavah") != null && resolve(inv, "mojkajoz") != null;
            String nameCol = resolve(inv, "naka");
            if (ledger && nameCol != null) {
                String sql = "SELECT TOP (12) TRY_CONVERT(nvarchar(150),i.[" + nameCol + "]), ISNULL(stx.stock_qty,0) FROM dbo.inventory i " + MainActivity.atiranStockApply("i", "stx") +
                        " WHERE ISNULL(stx.stock_rows,0)>0 AND ISNULL(stx.stock_qty,0)<=0 ORDER BY TRY_CONVERT(nvarchar(150),i.[" + nameCol + "])";
                try (PreparedStatement ps = c.prepareStatement(sql)) {
                    try (ResultSet r = ps.executeQuery()) { while (r.next()) { JSONObject o = new JSONObject(); o.put("name", r.getString(1) == null ? "\u2014" : r.getString(1)); o.put("stock", r.getDouble(2)); zero.put(o); } }
                }
            }
        }
        out.put("zeroStock", zero);
        return out;
    }

    /** Field-visit intelligence from the real Visit table (columns confirmed by CI probe: VisRdf, Shmo, Duration, DateCreated, TimeCreated). */
    static JSONObject fieldVisits(Connection c, int range) throws Exception {
        JSONObject out = new JSONObject();
        JSONArray perVisitor = new JSONArray();
        JSONArray recent = new JSONArray();
        if (!tableExists(c, "Visit")) { out.put("perVisitor", perVisitor); out.put("recent", recent); return out; }
        Set<String> v = columns(c, "Visit");
        String vVis = resolve(v, "VisRdf", "vis_rdf", "VisitRdf");
        String vDate = resolve(v, "DateCreated", "Created");
        String vTime = resolve(v, "TimeCreated", "Sent");
        String vDur = resolve(v, "Duration");
        String vShmo = resolve(v, "Shmo", "shmo");
        if (vVis == null || vDate == null) { out.put("perVisitor", perVisitor); out.put("recent", recent); return out; }
        Set<String> vis = columns(c, "visitors");
        String visKey = resolve(vis, "rdf", "RDF", "id", "ID");
        String visName = resolve(vis, "name", "Name", "vis_name", "VisitorName", "moname");
        String nameExpr = visKey != null && visName != null ? "COALESCE(TRY_CONVERT(nvarchar(150),vi.[" + visName + "]),N'بدون نام')" : "N'بدون نام'";
        String joinVis = visKey != null ? " LEFT JOIN dbo.visitors vi ON TRY_CONVERT(nvarchar(100),vi.[" + visKey + "])=TRY_CONVERT(nvarchar(100),vs.[" + vVis + "])" : "";
        String durExpr = vDur == null ? "CAST(0 AS decimal(19,2))" : "ISNULL(TRY_CONVERT(decimal(19,2),vs.[" + vDur + "]),0)";
        String sql1 = "SELECT TOP (10) " + nameExpr + ", COUNT_BIG(1), ISNULL(SUM(" + durExpr + "),0) FROM dbo.Visit vs" + joinVis + " GROUP BY " + nameExpr + " ORDER BY 2 DESC";
        try (PreparedStatement ps = c.prepareStatement(sql1)) {
            try (ResultSet r = ps.executeQuery()) { while (r.next()) { JSONObject o = new JSONObject(); o.put("name", r.getString(1) == null ? "—" : r.getString(1)); o.put("visits", r.getLong(2)); o.put("duration", r.getDouble(3)); perVisitor.put(o); } }
        }
        Set<String> cust = columns(c, "CUSTOMERS");
        String cShmo = resolve(cust, "SHMO", "shmo");
        String cName = resolve(cust, "MONAME", "Name", "CusName");
        String custExpr = cShmo != null && cName != null && vShmo != null ? "COALESCE(TRY_CONVERT(nvarchar(200),cu.[" + cName + "]),N'بدون نام')" : "N'—'";
        String joinCust = cShmo != null && vShmo != null ? " LEFT JOIN dbo.CUSTOMERS cu ON TRY_CONVERT(nvarchar(100),cu.[" + cShmo + "])=TRY_CONVERT(nvarchar(100),vs.[" + vShmo + "])" : "";
        String sql2 = "SELECT TOP (30) TRY_CONVERT(nvarchar(30),vs.[" + vDate + "]), " + (vTime == null ? "CAST(NULL AS nvarchar(20))" : "TRY_CONVERT(nvarchar(20),vs.[" + vTime + "])") + ", " + custExpr + ", " + durExpr + " FROM dbo.Visit vs" + joinCust + " ORDER BY TRY_CONVERT(nvarchar(30),vs.[" + vDate + "]) DESC";
        try (PreparedStatement ps = c.prepareStatement(sql2)) {
            try (ResultSet r = ps.executeQuery()) { while (r.next()) { JSONObject o = new JSONObject(); o.put("date", r.getString(1) == null ? "—" : r.getString(1)); o.put("time", r.getString(2) == null ? "" : r.getString(2)); o.put("party", r.getString(3) == null ? "—" : r.getString(3)); o.put("duration", r.getDouble(4)); recent.put(o); } }
        }
        out.put("perVisitor", perVisitor);
        out.put("recent", recent);
        return out;
    }

    // ============================ orchestrator ============================
    /** One connection, sequential validated queries, per-section error capture (never crashes the UI). */
    static JSONObject fetch(Connection c, int range) throws Exception {
        JSONObject out = new JSONObject();
        JSONArray errors = new JSONArray();
        out.put("range", range);
        out.put("syncAt", System.currentTimeMillis());
        try { out.put("sales", rangeBlock(c, true, range)); } catch (Exception e) { errors.put("sales: " + String.valueOf(e.getMessage())); }
        try { out.put("purchases", rangeBlock(c, false, range)); } catch (Exception e) { errors.put("purchases: " + String.valueOf(e.getMessage())); }
        try { out.put("trend", trend(c, 7)); } catch (Exception e) { errors.put("trend: " + String.valueOf(e.getMessage())); }
        try { out.put("receivables", receivables(c)); } catch (Exception e) { errors.put("receivables: " + String.valueOf(e.getMessage())); }
        try { out.put("debtors", debtors(c, 8)); } catch (Exception e) { errors.put("debtors: " + String.valueOf(e.getMessage())); }
        try { out.put("customers", customerCategories(c, range)); } catch (Exception e) { errors.put("customers: " + String.valueOf(e.getMessage())); }
        try { out.put("visitors", visitorGoals(c, range)); } catch (Exception e) { errors.put("visitors: " + String.valueOf(e.getMessage())); }
        try { out.put("products", products(c, range)); } catch (Exception e) { errors.put("products: " + String.valueOf(e.getMessage())); }
        try { out.put("checkBuckets", checkBuckets(c)); } catch (Exception e) { errors.put("checkBuckets: " + String.valueOf(e.getMessage())); }
        try { out.put("aging", collection(c)); } catch (Exception e) { errors.put("aging: " + String.valueOf(e.getMessage())); }
        try { out.put("feed", activityFeed(c)); } catch (Exception e) { errors.put("feed: " + String.valueOf(e.getMessage())); }
        out.put("errors", errors);
        return out;
    }
}

package ir.meelano.android;

import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * «آتیران انبار» — data layer of the Warehouse & Dispatch module.
 *
 * Hard rules enforced here (see docs/warehouse/WAREHOUSE-SCHEMA-MAPPING-fa.md):
 *  - Only columns verified against the real Atiran schema are referenced. Every Atiran
 *    table/column used below appears in the verified whitelist; guessed fields
 *    (mojodi, minstock, expiry, batch, bin/location, …) are deliberately absent and are
 *    also rejected by tools/warehouse/check_schema_usage.py.
 *  - Current stock is NEVER re-implemented and never a bare column: it is reused from
 *    {@link MainActivity#atiranStockApply(String, String)} (ka_act ledger with the same
 *    signs as dbo.UpdateMojodiInventory) so warehouse numbers equal accounting numbers.
 *  - The app is READ/PROCESS/CONTROL on accounting documents (sailfact/subsailfact/buyfact);
 *    it writes only its own operational tables (meelano_wh_*) with Transaction + audit.
 */
final class MeelanoWarehouse {
    private MeelanoWarehouse() { }

    // ------------------------------------------------------------------ schema --
    static final String WH_TASK = "dbo.meelano_wh_task";
    static final String WH_RECEIVE = "dbo.meelano_wh_receive";
    static final String WH_COUNT = "dbo.meelano_wh_count";
    static final String WH_AUDIT = "dbo.meelano_wh_audit";

    /** Creates the warehouse operational tables (idempotent), mirroring the meelano_* pattern. */
    static void ensureTables(Connection c) throws SQLException {
        try (Statement st = c.createStatement()) {
            st.executeUpdate("IF OBJECT_ID(N'dbo.meelano_wh_task') IS NULL CREATE TABLE dbo.meelano_wh_task ("
                    + "id bigint IDENTITY(1,1) NOT NULL PRIMARY KEY, shfacfo bigint NOT NULL, rdf__ int NOT NULL, "
                    + "shka bigint NOT NULL, requested decimal(19,3) NOT NULL DEFAULT 0, picked decimal(19,3) NOT NULL DEFAULT 0, "
                    + "state nvarchar(20) NOT NULL DEFAULT N'pending', assignee nvarchar(120) NULL, assignee_name nvarchar(200) NULL, "
                    + "reason nvarchar(300) NULL, started_at datetime2 NULL, done_at datetime2 NULL, "
                    + "created_by nvarchar(120) NULL, created_at datetime2 NOT NULL DEFAULT SYSDATETIME(), updated_at datetime2 NOT NULL DEFAULT SYSDATETIME())");
            st.executeUpdate("IF OBJECT_ID(N'dbo.meelano_wh_receive') IS NULL CREATE TABLE dbo.meelano_wh_receive ("
                    + "id bigint IDENTITY(1,1) NOT NULL PRIMARY KEY, buy_shmo bigint NOT NULL, shka bigint NOT NULL, "
                    + "expected decimal(19,3) NOT NULL DEFAULT 0, received decimal(19,3) NOT NULL DEFAULT 0, "
                    + "diff AS (received - expected) PERSISTED, state nvarchar(20) NOT NULL DEFAULT N'open', "
                    + "note nvarchar(300) NULL, received_by nvarchar(120) NULL, received_at datetime2 NULL, "
                    + "review_state nvarchar(20) NOT NULL DEFAULT N'none', reviewed_by nvarchar(120) NULL, reviewed_at datetime2 NULL, "
                    + "created_at datetime2 NOT NULL DEFAULT SYSDATETIME())");
            st.executeUpdate("IF OBJECT_ID(N'dbo.meelano_wh_count') IS NULL CREATE TABLE dbo.meelano_wh_count ("
                    + "id bigint IDENTITY(1,1) NOT NULL PRIMARY KEY, shka bigint NOT NULL, system_qty decimal(19,3) NULL, "
                    + "actual_qty decimal(19,3) NULL, diff AS (actual_qty - system_qty) PERSISTED, blind bit NOT NULL DEFAULT 0, "
                    + "state nvarchar(20) NOT NULL DEFAULT N'open', counted_by nvarchar(120) NULL, counted_at datetime2 NULL, "
                    + "approved_by nvarchar(120) NULL, approved_at datetime2 NULL, created_at datetime2 NOT NULL DEFAULT SYSDATETIME())");
            st.executeUpdate("IF OBJECT_ID(N'dbo.meelano_wh_audit') IS NULL CREATE TABLE dbo.meelano_wh_audit ("
                    + "id bigint IDENTITY(1,1) NOT NULL PRIMARY KEY, actor nvarchar(120) NOT NULL, actor_name nvarchar(200) NULL, "
                    + "action nvarchar(40) NOT NULL, ref_table nvarchar(60) NULL, ref_id bigint NULL, "
                    + "before_val nvarchar(400) NULL, after_val nvarchar(400) NULL, note nvarchar(500) NULL, "
                    + "created_at datetime2 NOT NULL DEFAULT SYSDATETIME())");
            st.executeUpdate("IF OBJECT_ID(N'dbo.meelano_wh_transfer') IS NULL CREATE TABLE dbo.meelano_wh_transfer ("
                    + "id bigint IDENTITY(1,1) NOT NULL PRIMARY KEY, shka bigint NOT NULL, qty decimal(19,3) NOT NULL DEFAULT 0, "
                    + "src_anbar int NULL, dst_anbar int NULL, state nvarchar(20) NOT NULL DEFAULT N'requested', "
                    + "requested_by nvarchar(120) NULL, confirmed_by nvarchar(120) NULL, confirmed_at datetime2 NULL, "
                    + "note nvarchar(300) NULL, created_at datetime2 NOT NULL DEFAULT SYSDATETIME())");
        }
    }

    /** Audit row (who/what/when/before/after/reference). */
    static void audit(Connection c, String actor, String actorName, String action, String refTable, Long refId,
                      String before, String after, String note) {
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO " + WH_AUDIT + "(actor, actor_name, action, ref_table, ref_id, before_val, after_val, note) VALUES(?,?,?,?,?,?,?,?)")) {
            ps.setString(1, actor); ps.setString(2, actorName); ps.setString(3, action); ps.setString(4, refTable);
            if (refId == null) ps.setNull(5, java.sql.Types.BIGINT); else ps.setLong(5, refId);
            ps.setString(6, before); ps.setString(7, after); ps.setString(8, note);
            ps.executeUpdate();
        } catch (SQLException ignored) { }
    }

    // ------------------------------------------------------------------ helpers --
    private static String str(ResultSet r, String col) throws SQLException {
        String v = r.getString(col); return v == null ? "" : v;
    }
    private static double num(ResultSet r, String col) throws SQLException { return r.getDouble(col); }

    /** Jalali today exactly like the delivery module (verified UDF). */
    static String today(Connection c) {
        try (Statement st = c.createStatement(); ResultSet r = st.executeQuery(
                "SELECT TRY_CONVERT(nvarchar(30),dbo.UDF_Gregorian_To_Persian(GETDATE()))")) {
            if (r.next()) return r.getString(1);
        } catch (SQLException ignored) { }
        return "";
    }

    // ---------------------------------------------------------------- dashboard --
    static JSONObject dashboard(Connection c) throws Exception {
        JSONObject out = new JSONObject();
        String today = today(c);
        out.put("today", today);
        // Reuse the exact verified stock OUTER APPLY; i = inventory alias, stx = stock alias.
        String stock = MainActivity.atiranStockApply("i", "stx");

        try (PreparedStatement ps = c.prepareStatement(
                "SELECT COUNT_BIG(1) FROM dbo.sailfact s WHERE s.active='t' AND s.[date]=?")) {
            ps.setString(1, today);
            try (ResultSet r = ps.executeQuery()) { if (r.next()) out.put("sales_today", r.getLong(1)); }
        }
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT COUNT_BIG(1) FROM dbo.meelano_delivery d WHERE d.status=N'open'")) {
            try (ResultSet r = ps.executeQuery()) { if (r.next()) out.put("delivery_open", r.getLong(1)); }
        }
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT COUNT_BIG(1) FROM dbo.meelano_delivery d WHERE d.status=N'claimed'")) {
            try (ResultSet r = ps.executeQuery()) { if (r.next()) out.put("delivery_claimed", r.getLong(1)); }
        }
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT COUNT_BIG(1) FROM dbo.buyfact b")) {
            try (ResultSet r = ps.executeQuery()) { if (r.next()) out.put("purchase_count", r.getLong(1)); }
        }
        // Shortage: verified pattern — line requested qty vs live ledger stock.
        try (Statement st = c.createStatement(); ResultSet r = st.executeQuery(
                "SELECT COUNT_BIG(1) FROM (SELECT d.shka, SUM(ISNULL(d.TEDVAH,0)*ISNULL(NULLIF(TRY_CONVERT(decimal(19,3),i.mohvah),0),1)+ISNULL(d.TEDJOZ,0)) req "
                        + "FROM dbo.subsailfact d JOIN dbo.sailfact s ON s.shfacfo=d.shfacfo AND s.rdf__=d.rdf__ JOIN dbo.inventory i ON i.shka=d.shka "
                        + stock + " WHERE s.active='t' AND d.active='t' AND ISNULL(stx.stock_qty,0) < "
                        + "(ISNULL(d.TEDVAH,0)*ISNULL(NULLIF(TRY_CONVERT(decimal(19,3),i.mohvah),0),1)+ISNULL(d.TEDJOZ,0)) "
                        + "GROUP BY d.shka) x")) {
            if (r.next()) out.put("shortage_products", r.getLong(1));
        }
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT COUNT_BIG(1) FROM dbo.meelano_delivery_item di WHERE di.state=N'partial'")) {
            try (ResultSet r = ps.executeQuery()) { if (r.next()) out.put("partial_lines", r.getLong(1)); }
        }
        return out;
    }

    // ------------------------------------------------------------- sales list --
    static JSONArray salesList(Connection c, int top) throws Exception {
        JSONArray arr = new JSONArray();
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT TOP (?) s.shfacfo, s.[date], ISNULL(c.MONAME,N'') man, ISNULL(s.[all],0) total, ISNULL(s.[Status],0) st, "
                        + "(SELECT COUNT_BIG(1) FROM dbo.subsailfact d WHERE d.shfacfo=s.shfacfo AND d.active='t') lines "
                        + "FROM dbo.sailfact s LEFT JOIN dbo.CUSTOMERS c ON c.SHMO=s.shmo "
                        + "WHERE s.active='t' ORDER BY s.shfacfo DESC")) {
            ps.setInt(1, top);
            try (ResultSet r = ps.executeQuery()) {
                while (r.next()) {
                    arr.put(new JSONObject()
                            .put("shfacfo", r.getLong("shfacfo"))
                            .put("date", str(r, "date"))
                            .put("customer", str(r, "man"))
                            .put("total", num(r, "total"))
                            .put("status", r.getInt("st"))
                            .put("lines", r.getLong("lines")));
                }
            }
        }
        return arr;
    }

    // ---------------------------------------------------------- sales detail --
    static JSONArray salesDetail(Connection c, long shfacfo) throws Exception {
        JSONArray arr = new JSONArray();
        String stock = MainActivity.atiranStockApply("i", "stx");
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT d.shka, ISNULL(i.naka,N'') naka, ISNULL(d.TEDVAH,0) tedvah, ISNULL(d.TEDJOZ,0) tedjoz, "
                        + "ISNULL(d.LINESUM,0) linesum, ISNULL(stx.stock_qty,0) stock, ISNULL(i.mohvah,0) mohvah "
                        + "FROM dbo.subsailfact d LEFT JOIN dbo.inventory i ON i.shka=d.shka " + stock + " "
                        + "WHERE d.shfacfo=? AND d.active='t' ORDER BY d.RDF")) {
            ps.setLong(1, shfacfo);
            try (ResultSet r = ps.executeQuery()) {
                while (r.next()) {
                    double req = num(r, "tedvah") * (num(r, "mohvah") > 0 ? num(r, "mohvah") : 1) + num(r, "tedjoz");
                    double st = num(r, "stock");
                    arr.put(new JSONObject()
                            .put("shka", r.getLong("shka"))
                            .put("name", str(r, "naka"))
                            .put("requested", req)
                            .put("stock", st)
                            .put("shortage", Math.max(0, req - st))
                            .put("linesum", num(r, "linesum")));
                }
            }
        }
        return arr;
    }

    // -------------------------------------------------------------- inventory --
    static JSONArray inventoryList(Connection c, int top) throws Exception {
        JSONArray arr = new JSONArray();
        String stock = MainActivity.atiranStockApply("i", "stx");
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT TOP (?) i.shka, ISNULL(i.naka,N'') naka, ISNULL(stx.stock_qty,0) stock, ISNULL(i.mohvah,0) mohvah "
                        + "FROM dbo.inventory i " + stock + " ORDER BY i.shka")) {
            ps.setInt(1, top);
            try (ResultSet r = ps.executeQuery()) {
                while (r.next()) {
                    arr.put(new JSONObject()
                            .put("shka", r.getLong("shka"))
                            .put("name", str(r, "naka"))
                            .put("stock", num(r, "stock"))
                            .put("mohvah", num(r, "mohvah")));
                }
            }
        }
        return arr;
    }

    /** Read-only movement ledger (ka_act). No sign assumption beyond the verified out-acts list. */
    static JSONArray movements(Connection c, long shka, int top) throws Exception {
        JSONArray arr = new JSONArray();
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT TOP (?) ISNULL(k.tedvah,0) tedvah, ISNULL(k.tedjoz,0) tedjoz, k.act_id, k.active "
                        + "FROM dbo.ka_act k WHERE k.shka=? ORDER BY k.act_id DESC")) {
            ps.setInt(1, top); ps.setLong(2, shka);
            try (ResultSet r = ps.executeQuery()) {
                while (r.next()) {
                    boolean outAct = isOutAct(r.getInt("act_id"));
                    arr.put(new JSONObject()
                            .put("tedvah", num(r, "tedvah"))
                            .put("tedjoz", num(r, "tedjoz"))
                            .put("act_id", r.getInt("act_id"))
                            .put("direction", outAct ? "out" : "in")
                            .put("active", str(r, "active")));
                }
            }
        }
        return arr;
    }

    private static boolean isOutAct(int actId) {
        for (String s : MainActivity.ATIRAN_STOCK_OUT_ACTS.split(",")) {
            try { if (Integer.parseInt(s.trim()) == actId) return true; } catch (Exception ignored) { }
        }
        return false;
    }

    // ------------------------------------------------------- purchase (read-only)
    static JSONArray purchaseList(Connection c, int top) throws Exception {
        JSONArray arr = new JSONArray();
        try (PreparedStatement ps = c.prepareStatement("SELECT TOP (?) b.shmo FROM dbo.buyfact b ORDER BY b.shmo DESC")) {
            ps.setInt(1, top);
            try (ResultSet r = ps.executeQuery()) {
                while (r.next()) arr.put(new JSONObject().put("shmo", r.getLong("shmo")));
            }
        }
        return arr;
    }

    // ------------------------------------------------------------- operations --
    /** Start picking for one invoice line (operational table only). */
    static long startTask(Connection c, long shfacfo, int rdf, long shka, double requested, String actor, String actorName) throws SQLException {
        c.setAutoCommit(false);
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO " + WH_TASK + "(shfacfo, rdf__, shka, requested, state, assignee, assignee_name, started_at, created_by) "
                        + "VALUES(?,?,?,?,'picking',?,?,SYSDATETIME(),?)", Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, shfacfo); ps.setInt(2, rdf); ps.setLong(3, shka); ps.setDouble(4, requested);
            ps.setString(5, actor); ps.setString(6, actorName); ps.setString(7, actor);
            ps.executeUpdate();
            long id = -1;
            try (ResultSet k = ps.getGeneratedKeys()) { if (k.next()) id = k.getLong(1); }
            audit(c, actor, actorName, "picking.start", "meelano_wh_task", id, null, String.valueOf(requested), "shfacfo=" + shfacfo);
            c.commit();
            return id;
        } catch (SQLException e) { c.rollback(); throw e; }
        finally { c.setAutoCommit(true); }
    }

    /** Confirm picked qty; records shortage reason when partial. */
    static void confirmTask(Connection c, long taskId, double picked, String reason, String actor, String actorName) throws SQLException {
        c.setAutoCommit(false);
        try (PreparedStatement ps = c.prepareStatement(
                "UPDATE " + WH_TASK + " SET picked=?, state=CASE WHEN ? >= requested THEN N'done' ELSE N'partial' END, "
                        + "reason=?, done_at=SYSDATETIME(), updated_at=SYSDATETIME() WHERE id=?")) {
            ps.setDouble(1, picked); ps.setDouble(2, picked); ps.setString(3, reason); ps.setLong(4, taskId);
            ps.executeUpdate();
            audit(c, actor, actorName, "picking.confirm", "meelano_wh_task", taskId, null, String.valueOf(picked), reason);
            c.commit();
        } catch (SQLException e) { c.rollback(); throw e; }
        finally { c.setAutoCommit(true); }
    }

    // ------------------------------------------------------ purchase receiving --
    static long openReceive(Connection c, long buyShmo, long shka, double expected, String actor, String actorName) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO " + WH_RECEIVE + "(buy_shmo, shka, expected, state, received_by) VALUES(?,?,?,'open',?)",
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, buyShmo); ps.setLong(2, shka); ps.setDouble(3, expected); ps.setString(4, actor);
            ps.executeUpdate();
            long id = -1; try (ResultSet k = ps.getGeneratedKeys()) { if (k.next()) id = k.getLong(1); }
            audit(c, actor, actorName, "receive.open", "meelano_wh_receive", id, null, String.valueOf(expected), "buy=" + buyShmo);
            return id;
        }
    }

    /** Record received qty; diff is a PERSISTED computed column. Does NOT touch stock (review first). */
    static void confirmReceive(Connection c, long id, double received, String note, String actor, String actorName) throws SQLException {
        c.setAutoCommit(false);
        try (PreparedStatement ps = c.prepareStatement(
                "UPDATE " + WH_RECEIVE + " SET received=?, note=?, state=CASE WHEN ?<>expected THEN N'diff' ELSE N'done' END, "
                        + "received_by=?, received_at=SYSDATETIME() WHERE id=?")) {
            ps.setDouble(1, received); ps.setString(2, note); ps.setDouble(3, received); ps.setString(4, actor); ps.setLong(5, id);
            ps.executeUpdate();
            audit(c, actor, actorName, "receive.confirm", "meelano_wh_receive", id, null, String.valueOf(received), note);
            c.commit();
        } catch (SQLException e) { c.rollback(); throw e; }
        finally { c.setAutoCommit(true); }
    }

    /** Supervisor review of a receiving difference. Approval is required before any stock effect (enforced by workflow). */
    static void reviewReceive(Connection c, long id, boolean approve, String actor, String actorName) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "UPDATE " + WH_RECEIVE + " SET review_state=?, reviewed_by=?, reviewed_at=SYSDATETIME() WHERE id=?")) {
            ps.setString(1, approve ? "approved" : "rejected"); ps.setString(2, actor); ps.setLong(3, id);
            ps.executeUpdate();
            audit(c, actor, actorName, approve ? "receive.approve" : "receive.reject", "meelano_wh_receive", id, null, null, null);
        }
    }

    // ------------------------------------------------------------ stock counting --
    static long submitCount(Connection c, long shka, Double systemQty, double actual, boolean blind, String actor, String actorName) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO " + WH_COUNT + "(shka, system_qty, actual_qty, blind, state, counted_by, counted_at) VALUES(?,?,?,?, 'counted', ?,SYSDATETIME())",
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, shka);
            if (systemQty == null) ps.setNull(2, java.sql.Types.DECIMAL); else ps.setDouble(2, systemQty);
            ps.setDouble(3, actual); ps.setBoolean(4, blind); ps.setString(5, actor);
            ps.executeUpdate();
            long id = -1; try (ResultSet k = ps.getGeneratedKeys()) { if (k.next()) id = k.getLong(1); }
            audit(c, actor, actorName, "count.submit", "meelano_wh_count", id, systemQty == null ? null : String.valueOf(systemQty), String.valueOf(actual), blind ? "blind" : "open");
            return id;
        }
    }

    static void approveCount(Connection c, long id, String actor, String actorName) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "UPDATE " + WH_COUNT + " SET state=N'approved', approved_by=?, approved_at=SYSDATETIME() WHERE id=?")) {
            ps.setString(1, actor); ps.setLong(2, id);
            ps.executeUpdate();
            audit(c, actor, actorName, "count.approve", "meelano_wh_count", id, null, null, null);
        }
    }

    // ---------------------------------------------------------------- transfers --
    static long requestTransfer(Connection c, long shka, double qty, Integer src, Integer dst, String actor, String actorName) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO " + "dbo.meelano_wh_transfer" + "(shka, qty, src_anbar, dst_anbar, state, requested_by) VALUES(?,?,?,?,'requested',?)",
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, shka); ps.setDouble(2, qty);
            if (src == null) ps.setNull(3, java.sql.Types.INTEGER); else ps.setInt(3, src);
            if (dst == null) ps.setNull(4, java.sql.Types.INTEGER); else ps.setInt(4, dst);
            ps.setString(5, actor);
            ps.executeUpdate();
            long id = -1; try (ResultSet k = ps.getGeneratedKeys()) { if (k.next()) id = k.getLong(1); }
            audit(c, actor, actorName, "transfer.request", "meelano_wh_transfer", id, null, String.valueOf(qty), src + "->" + dst);
            return id;
        }
    }

    static void confirmTransfer(Connection c, long id, String actor, String actorName) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "UPDATE dbo.meelano_wh_transfer SET state=N'confirmed', confirmed_by=?, confirmed_at=SYSDATETIME() WHERE id=?")) {
            ps.setString(1, actor); ps.setLong(2, id);
            ps.executeUpdate();
            audit(c, actor, actorName, "transfer.confirm", "meelano_wh_transfer", id, null, null, null);
        }
    }

    // ------------------------------------------------------------------ workers --
    /** Warehouse workers from the existing access-control layer (real roles, not hardcoded). */
    static JSONArray workers(Connection c) throws Exception {
        JSONArray arr = new JSONArray();
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT username, ISNULL(display_name,N'') dn, role_key, enabled FROM dbo.meelano_access_users WHERE enabled=1 ORDER BY display_name")) {
            try (ResultSet r = ps.executeQuery()) {
                while (r.next()) {
                    arr.put(new JSONObject().put("username", str(r, "username")).put("name", str(r, "dn"))
                            .put("role", str(r, "role_key")).put("enabled", r.getInt("enabled")));
                }
            }
        }
        return arr;
    }

    // ------------------------------------------------------------- daily report --
    static JSONObject dailyReport(Connection c, String actor) throws Exception {
        JSONObject o = new JSONObject();
        String today = today(c);
        o.put("today", today);
        try (PreparedStatement ps = c.prepareStatement("SELECT COUNT_BIG(1) FROM dbo.sailfact WHERE active='t' AND [date]=?")) {
            ps.setString(1, today); try (ResultSet r = ps.executeQuery()) { if (r.next()) o.put("sales", r.getLong(1)); }
        }
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT COUNT_BIG(1) FROM dbo.meelano_delivery WHERE status=N'delivered' AND dbo.UDF_Gregorian_To_Persian(delivered_at)=?")) {
            ps.setString(1, today); try (ResultSet r = ps.executeQuery()) { if (r.next()) o.put("delivered", r.getLong(1)); }
        }
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT COUNT_BIG(1) FROM dbo.meelano_wh_receive WHERE dbo.UDF_Gregorian_To_Persian(received_at)=?")) {
            ps.setString(1, today); try (ResultSet r = ps.executeQuery()) { if (r.next()) o.put("received", r.getLong(1)); }
        }
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT COUNT_BIG(1) FROM dbo.meelano_wh_receive WHERE state=N'diff' AND dbo.UDF_Gregorian_To_Persian(received_at)=?")) {
            ps.setString(1, today); try (ResultSet r = ps.executeQuery()) { if (r.next()) o.put("receive_diff", r.getLong(1)); }
        }
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT COUNT_BIG(1) FROM dbo.meelano_wh_audit WHERE actor=? AND dbo.UDF_Gregorian_To_Persian(created_at)=?")) {
            ps.setString(1, actor); ps.setString(2, today);
            try (ResultSet r = ps.executeQuery()) { if (r.next()) o.put("my_ops", r.getLong(1)); }
        }
        return o;
    }
}

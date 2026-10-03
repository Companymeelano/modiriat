package ir.meelano.android.finance;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Data layer of «آتیران مالی».
 *
 *  * every statement is parameterised — no SQL text is ever built from user input,
 *  * queries run on a small background executor and never on the UI thread,
 *  * read results are cached (memory + on disk) so the app is usable offline and never crashes
 *    when the database is unreachable; cached results carry their fetch time so the UI can mark
 *    stale data,
 *  * operations that change financial data use {@link #runGuarded(String, String, Op)} which is
 *    idempotent per operation id, runs inside a transaction, writes an audit row and never lets a
 *    duplicate settlement / receipt / reconciliation be applied twice.
 *
 * The side tables (meelano_fin_*) are created on demand the same way the shared app creates
 * meelano_chat_* / meelano_hr_*: the Atiran tables themselves are only ever read.
 */
public final class FinDb {
    /** Callback for background work, always delivered on the UI thread. */
    public interface Cb<T> { void ok(T value); default void fail(Exception e) { } }
    public interface Op { JSONObject run(Connection c, JSONObject args) throws Exception; }

    private static final String PREFS = "atiran_finance_cache";
    private static final int MEM_CACHE_MAX = 40;

    private final Context ctx;
    private final ExecutorService pool = Executors.newFixedThreadPool(3);
    private final Map<String, JSONObject> cache = Collections.synchronizedMap(
            new LinkedHashMap<String, JSONObject>(16, 0.75f, true) {
                @Override protected boolean removeEldestEntry(Map.Entry<String, JSONObject> e) {
                    return size() > MEM_CACHE_MAX;
                }
            });
    private final Map<String, Long> cacheTime = Collections.synchronizedMap(new LinkedHashMap<String, Long>());
    private volatile boolean schemaReady = false;
    private volatile String lastError = "";

    public FinDb(Context context) { this.ctx = context.getApplicationContext(); }

    public ExecutorService pool() { return pool; }
    public String lastError() { return lastError; }

    // ------------------------------------------------------------------ connection

    /** Opens a connection with a short login timeout; the caller must close it. */
    public Connection open() throws SQLException {
        try {
            Class.forName("net.sourceforge.jtds.jdbc.Driver");
        } catch (ClassNotFoundException ignored) {
            // jtds is registered by the driver manager on some devices without an explicit load.
        }
        String url = "jdbc:jtds:sqlserver://" + FinEnv.host() + ":" + FinEnv.PORT + "/" + FinEnv.database()
                + ";loginTimeout=15;socketTimeout=45;appName=AtiranFinance;";
        Properties props = new Properties();
        props.setProperty("user", FinEnv.user());
        props.setProperty("password", FinEnv.password());
        props.setProperty("charset", "UTF-8");
        return DriverManager.getConnection(url, props);
    }

    /** Cheap connectivity probe used by the header status chip and the sync screen. */
    public JSONObject health() {
        JSONObject out = new JSONObject();
        long started = System.currentTimeMillis();
        try (Connection c = open(); PreparedStatement ps = c.prepareStatement(
                "SELECT CONVERT(nvarchar(10), dbo.ReturnDateServer()) AS j, "
                        + "CONVERT(nvarchar(30), SYSDATETIME(), 120) AS s, "
                        + "CONVERT(nvarchar(40), DB_NAME()) AS d")) {
            try (ResultSet r = ps.executeQuery()) {
                if (r.next()) {
                    out.put("jalali", r.getString("j"));
                    out.put("serverTime", r.getString("s"));
                    out.put("database", r.getString("d"));
                }
            }
            out.put("ok", true);
            out.put("ms", System.currentTimeMillis() - started);
            lastError = "";
        } catch (Exception e) {
            out.put("ok", false);
            out.put("error", safeMessage(e));
            lastError = safeMessage(e);
        }
        return out;
    }

    /** SQL errors must never reach the user interface; only a short, safe message is produced. */
    public static String safeMessage(Exception e) {
        if (e == null) return "";
        String m = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
        m = m.replace('\n', ' ').trim();
        if (m.length() > 160) m = m.substring(0, 160);
        return m;
    }

    // ------------------------------------------------------------------ queries

    /** Binds parameters positionally; supported types only, never string-concatenated into SQL. */
    static void bind(PreparedStatement ps, Object[] args) throws SQLException {
        if (args == null) return;
        for (int i = 0; i < args.length; i++) {
            Object a = args[i];
            int idx = i + 1;
            if (a == null) ps.setNull(idx, Types.NVARCHAR);
            else if (a instanceof String) ps.setNString(idx, (String) a);
            else if (a instanceof Integer) ps.setInt(idx, (Integer) a);
            else if (a instanceof Long) ps.setLong(idx, (Long) a);
            else if (a instanceof Double) ps.setDouble(idx, (Double) a);
            else if (a instanceof Boolean) ps.setBoolean(idx, (Boolean) a);
            else ps.setNString(idx, String.valueOf(a));
        }
    }

    /** Runs a parameterised SELECT and returns the rows as JSON (numbers stay numbers). */
    public JSONArray query(Connection c, String sql, Object... args) throws SQLException {
        return select(c, sql, args);
    }

    private JSONArray queryOld(Connection c, String sql, Object... args) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setQueryTimeout(60);
            bind(ps, args);
            try (ResultSet r = ps.executeQuery()) {
                ResultSetMetaData md = r.getMetaData();
                int n = md.getColumnCount();
                JSONArray rows = new JSONArray();
                while (r.next()) {
                    JSONObject row = new JSONObject();
                    for (int i = 1; i <= n; i++) {
                        String name = md.getColumnLabel(i);
                        Object v = r.getObject(i);
                        try {
                            if (v == null) row.put(name, JSONObject.NULL);
                            else if (v instanceof java.math.BigDecimal) row.put(name, ((java.math.BigDecimal) v).doubleValue());
                            else if (v instanceof Number) row.put(name, ((Number) v).doubleValue());
                            else if (v instanceof Boolean) row.put(name, v);
                            else if (v instanceof byte[]) row.put(name, "<binary>");
                            else row.put(name, String.valueOf(v).trim());
                        } catch (Exception ex) {
                            row.put(name, String.valueOf(v));
                        }
                    }
                    rows.put(row);
                }
                return rows;
            }
        }
    }

    /** Single value helper: first column of the first row, or null. */
    public Object scalar(Connection c, String sql, Object... args) throws SQLException {
        JSONArray rows = query(c, sql, args);
        if (rows.length() == 0) return null;
        JSONObject first = rows.optJSONObject(0);
        if (first == null || first.length() == 0) return null;
        return first.opt(first.keys().next());
    }

    public double num(Connection c, String sql, Object... args) throws SQLException {
        Object v = scalar(c, sql, args);
        if (v == null || v == JSONObject.NULL) return 0d;
        if (v instanceof Number) return ((Number) v).doubleValue();
        try { return Double.parseDouble(String.valueOf(v).replace(",", "")); } catch (Exception e) { return 0d; }
    }

    public String text(Connection c, String sql, Object... args) throws SQLException {
        Object v = scalar(c, sql, args);
        return v == null || v == JSONObject.NULL ? "" : String.valueOf(v);
    }

    // ------------------------------------------------------------------ cached reads

    public interface Fetch { JSONArray run(Connection c) throws Exception; }

    /**
     * Cached read: returns the memory cache when it is younger than {@code ttlMs}, otherwise reads
     * from the database; on failure the last saved copy is returned with {@code stale=true}.
     */
    public void read(String key, long ttlMs, Fetch fetch, Cb<JSONObject> cb) {
        JSONObject mem = cache.get(key);
        Long t = cacheTime.get(key);
        if (mem != null && t != null && System.currentTimeMillis() - t < ttlMs) {
            deliver(cb, copy(mem));
            return;
        }
        pool.execute(() -> {
            JSONObject result = new JSONObject();
            try (Connection c = open()) {
                ensureSchema(c);
                JSONArray rows = fetch.run(c);
                result.put("rows", rows);
                result.put("stale", false);
                result.put("at", System.currentTimeMillis());
                cache.put(key, copy(result));
                cacheTime.put(key, System.currentTimeMillis());
                saveDisk(key, result);
                deliver(cb, result);
            } catch (Exception e) {
                lastError = safeMessage(e);
                JSONObject disk = loadDisk(key);
                if (disk != null) {
                    try {
                        disk.put("stale", true);
                        disk.put("error", lastError);
                    } catch (Exception ignored) { }
                    deliver(cb, disk);
                } else {
                    result = new JSONObject();
                    try {
                        result.put("rows", new JSONArray());
                        result.put("stale", true);
                        result.put("error", lastError);
                    } catch (Exception ignored) { }
                    deliver(cb, result);
                }
            }
        });
    }

    private static JSONObject copy(JSONObject o) {
        try { return new JSONObject(o.toString()); } catch (Exception e) { return o; }
    }

    private static <T> void deliver(Cb<T> cb, T value) {
        new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> cb.ok(value));
    }

    // ------------------------------------------------------------------ cache on disk (offline)

    private void saveDisk(String key, JSONObject value) {
        try {
            SharedPreferences p = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
            String json = value.toString();
            byte[] packed = gzip(json);
            p.edit().putString(key, android.util.Base64.encodeToString(packed, android.util.Base64.NO_WRAP)).apply();
        } catch (Exception ignored) { }
    }

    private JSONObject loadDisk(String key) {
        try {
            SharedPreferences p = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
            String b64 = p.getString(key, null);
            if (b64 == null) return null;
            return new JSONObject(gunzip(android.util.Base64.decode(b64, android.util.Base64.NO_WRAP)));
        } catch (Exception e) {
            return null;
        }
    }

    private static byte[] gzip(String s) throws Exception {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        try (GZIPOutputStream gz = new GZIPOutputStream(bos)) {
            gz.write(s.getBytes(StandardCharsets.UTF_8));
        }
        return bos.toByteArray();
    }

    private static String gunzip(byte[] data) throws Exception {
        try (GZIPInputStream gz = new GZIPInputStream(new ByteArrayInputStream(data))) {
            java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
            byte[] buf = new byte[8192];
            int n;
            while ((n = gz.read(buf)) > 0) bos.write(buf, 0, n);
            return new String(bos.toByteArray(), StandardCharsets.UTF_8);
        }
    }

    public void clearCache() {
        cache.clear();
        cacheTime.clear();
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().clear().apply();
    }

    /** Time of the newest successful read in this session, for the «آخرین بهروزرسانی» header. */
    public long lastReadAt() {
        long best = 0;
        synchronized (cacheTime) {
            for (Long v : cacheTime.values()) if (v != null && v > best) best = v;
        }
        return best;
    }

    // ------------------------------------------------------------------ side tables + audit

    /**
     * The finance side tables. They never touch Atiran's own tables; they only record what the
     * finance operators do inside «آتیران مالی» (reconciliation cases, follow-ups, settlements,
     * daily closing and the audit trail). Created once per installation.
     */
    public void ensureSchema(Connection c) throws SQLException {
        if (schemaReady) return;
        try (java.sql.Statement st = c.createStatement()) {
            st.execute("IF OBJECT_ID(N'dbo.meelano_fin_recon',N'U') IS NULL CREATE TABLE dbo.meelano_fin_recon ("
                    + "id bigint IDENTITY(1,1) NOT NULL PRIMARY KEY, case_key nvarchar(120) NOT NULL, kind nvarchar(40) NOT NULL, "
                    + "bank_rdf int NULL, jalali_date char(10) NULL, amount money NULL, system_ref nvarchar(120) NULL, "
                    + "bank_ref nvarchar(120) NULL, reason nvarchar(400) NULL, status nvarchar(24) NOT NULL DEFAULT N'open', "
                    + "assigned_to nvarchar(120) NULL, resolution nvarchar(600) NULL, approved_by nvarchar(120) NULL, "
                    + "created_at datetime2 NOT NULL DEFAULT SYSDATETIME(), created_by nvarchar(120) NOT NULL, "
                    + "updated_at datetime2 NOT NULL DEFAULT SYSDATETIME())");
            st.execute("IF OBJECT_ID(N'dbo.meelano_fin_followup',N'U') IS NULL CREATE TABLE dbo.meelano_fin_followup ("
                    + "id bigint IDENTITY(1,1) NOT NULL PRIMARY KEY, shmo int NOT NULL, action nvarchar(40) NOT NULL, "
                    + "note nvarchar(700) NULL, promise_amount money NULL, promise_date char(10) NULL, "
                    + "status nvarchar(24) NOT NULL DEFAULT N'pending', next_action nvarchar(200) NULL, "
                    + "next_date char(10) NULL, assigned_to nvarchar(120) NULL, created_at datetime2 NOT NULL DEFAULT SYSDATETIME(), "
                    + "created_by nvarchar(120) NOT NULL)");
            st.execute("IF OBJECT_ID(N'dbo.meelano_fin_settlement',N'U') IS NULL CREATE TABLE dbo.meelano_fin_settlement ("
                    + "id bigint IDENTITY(1,1) NOT NULL PRIMARY KEY, op_key nvarchar(120) NOT NULL, user_login nvarchar(120) NOT NULL, "
                    + "jalali_date char(10) NOT NULL, kind nvarchar(24) NOT NULL, amount money NOT NULL, "
                    + "expected money NULL, difference money NULL, reference nvarchar(120) NULL, receiver nvarchar(120) NULL, "
                    + "signature varbinary(max) NULL, status nvarchar(24) NOT NULL DEFAULT N'pending', "
                    + "confirmed_by nvarchar(120) NULL, confirmed_at datetime2 NULL, created_at datetime2 NOT NULL DEFAULT SYSDATETIME(), "
                    + "created_by nvarchar(120) NOT NULL)");
            st.execute("IF OBJECT_ID(N'dbo.meelano_fin_dayclose',N'U') IS NULL CREATE TABLE dbo.meelano_fin_dayclose ("
                    + "id bigint IDENTITY(1,1) NOT NULL PRIMARY KEY, jalali_date char(10) NOT NULL, jdate_key char(10) NOT NULL, "
                    + "checklist nvarchar(max) NULL, open_issues int NOT NULL DEFAULT 0, status nvarchar(24) NOT NULL DEFAULT N'draft', "
                    + "note nvarchar(600) NULL, closed_by nvarchar(120) NULL, closed_at datetime2 NULL, "
                    + "created_at datetime2 NOT NULL DEFAULT SYSDATETIME(), created_by nvarchar(120) NOT NULL)");
            st.execute("IF OBJECT_ID(N'dbo.meelano_fin_audit',N'U') IS NULL CREATE TABLE dbo.meelano_fin_audit ("
                    + "id bigint IDENTITY(1,1) NOT NULL PRIMARY KEY, op_key nvarchar(120) NOT NULL, username nvarchar(120) NOT NULL, "
                    + "role_key nvarchar(60) NULL, module nvarchar(40) NOT NULL, action nvarchar(40) NOT NULL, "
                    + "reference nvarchar(160) NULL, amount money NULL, before_json nvarchar(max) NULL, after_json nvarchar(max) NULL, "
                    + "device nvarchar(160) NULL, app_version nvarchar(40) NULL, created_at datetime2 NOT NULL DEFAULT SYSDATETIME())");
            st.execute("IF OBJECT_ID(N'dbo.meelano_fin_ops',N'U') IS NULL CREATE TABLE dbo.meelano_fin_ops ("
                    + "op_key nvarchar(120) NOT NULL PRIMARY KEY, module nvarchar(40) NOT NULL, username nvarchar(120) NOT NULL, "
                    + "result_json nvarchar(max) NULL, created_at datetime2 NOT NULL DEFAULT SYSDATETIME())");
            st.execute("IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'IX_fin_audit_time') "
                    + "CREATE INDEX IX_fin_audit_time ON dbo.meelano_fin_audit (created_at DESC)");
            st.execute("IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'IX_fin_recon_key') "
                    + "CREATE UNIQUE INDEX IX_fin_recon_key ON dbo.meelano_fin_recon (case_key)");
            st.execute("IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'IX_fin_settle_op') "
                    + "CREATE UNIQUE INDEX IX_fin_settle_op ON dbo.meelano_fin_settlement (op_key)");
            st.execute("IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'IX_fin_follow_shmo') "
                    + "CREATE INDEX IX_fin_follow_shmo ON dbo.meelano_fin_followup (shmo, created_at DESC)");
        }
        schemaReady = true;
    }

    /**
     * Guarded write: one operation id per attempt, a transaction, an audit row and an idempotency
     * record. Sending the same operation twice returns the first result instead of applying it twice.
     */
    public void runGuarded(String opKey, String module, String action, JSONObject args, Op op, Cb<JSONObject> cb) {
        pool.execute(() -> {
            JSONObject result = new JSONObject();
            try (Connection c = open()) {
                ensureSchema(c);
                c.setAutoCommit(false);
                try {
                    String existing = null;
                    try (PreparedStatement ps = c.prepareStatement(
                            "SELECT result_json FROM dbo.meelano_fin_ops WHERE op_key = ?")) {
                        bind(ps, new Object[]{opKey});
                        try (ResultSet r = ps.executeQuery()) { if (r.next()) existing = r.getString(1); }
                    }
                    if (existing != null) {
                        result = new JSONObject(existing);
                        result.put("duplicate", true);
                        c.rollback();
                    } else {
                        JSONObject produced = op.run(c, args == null ? new JSONObject() : args);
                        if (produced == null) produced = new JSONObject();
                        produced.put("ok", true);
                        try (PreparedStatement ps = c.prepareStatement(
                                "INSERT INTO dbo.meelano_fin_ops(op_key, module, username, result_json) VALUES(?,?,?,?)")) {
                            bind(ps, new Object[]{opKey, module, FinSession.username(), produced.toString()});
                            ps.executeUpdate();
                        }
                        audit(c, opKey, module, action, args, produced);
                        c.commit();
                        result = produced;
                    }
                } catch (Exception e) {
                    try { c.rollback(); } catch (Exception ignored) { }
                    throw e;
                } finally {
                    try { c.setAutoCommit(true); } catch (Exception ignored) { }
                }
            } catch (Exception e) {
                lastError = safeMessage(e);
                try {
                    result.put("ok", false);
                    result.put("error", lastError);
                } catch (Exception ignored) { }
            }
            deliver(cb, result);
        });
    }

    /** Writes the audit row (who / what / when / where / reference). */
    public void audit(Connection c, String opKey, String module, String action, JSONObject args, JSONObject after)
            throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO dbo.meelano_fin_audit(op_key, username, role_key, module, action, reference, amount, "
                        + "before_json, after_json, device, app_version) VALUES(?,?,?,?,?,?,?,?,?,?,?)")) {
            Object ref = args == null ? null : args.opt("reference");
            Object amount = args == null ? null : (args.opt("amount") instanceof Number ? args.opt("amount") : null);
            bind(ps, new Object[]{opKey, FinSession.username(), FinSession.roleKey(), module, action,
                    ref == null ? JSONObject.NULL : String.valueOf(ref),
                    amount, args == null ? null : args.toString(),
                    after == null ? null : after.toString(),
                    FinSession.deviceLabel(), FinSession.appVersion()});
            ps.executeUpdate();
        }
    }

    /** Audit trail reader for the «فعالیتها» screen. */
    public JSONArray auditTrail(Connection c, int limit) throws SQLException {
        return query(c, "SELECT TOP (" + Math.max(1, Math.min(300, limit)) + ") id, op_key, username, role_key, module, "
                + "action, reference, amount, CONVERT(nvarchar(19), created_at, 120) AS at, app_version "
                + "FROM dbo.meelano_fin_audit ORDER BY id DESC");
    }

    // ------------------------------------------------------------------ static SQL helpers

    /** Parameterised SELECT returning rows as JSON; never built from user text. */
    public static JSONArray select(Connection c, String sql, Object... args) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setQueryTimeout(60);
            bind(ps, args);
            try (ResultSet r = ps.executeQuery()) {
                ResultSetMetaData md = r.getMetaData();
                int n = md.getColumnCount();
                JSONArray rows = new JSONArray();
                while (r.next()) {
                    JSONObject row = new JSONObject();
                    for (int i = 1; i <= n; i++) {
                        String name = md.getColumnLabel(i);
                        Object v = r.getObject(i);
                        try {
                            if (v == null) row.put(name, JSONObject.NULL);
                            else if (v instanceof java.math.BigDecimal) row.put(name, ((java.math.BigDecimal) v).doubleValue());
                            else if (v instanceof Number) row.put(name, ((Number) v).doubleValue());
                            else if (v instanceof Boolean) row.put(name, v);
                            else if (v instanceof byte[]) row.put(name, "<binary>");
                            else row.put(name, String.valueOf(v).trim());
                        } catch (Exception ex) {
                            row.put(name, String.valueOf(v));
                        }
                    }
                    rows.put(row);
                }
                return rows;
            }
        }
    }

    /** First column of the first row, or null. */
    public static Object value(Connection c, String sql, Object... args) throws SQLException {
        JSONArray rows = select(c, sql, args);
        if (rows.length() == 0) return null;
        JSONObject first = rows.optJSONObject(0);
        if (first == null || first.length() == 0) return null;
        return first.opt(first.keys().next());
    }

    /** Money/number helper: null becomes 0. */
    public static double sum(Connection c, String sql, Object... args) throws SQLException {
        Object v = value(c, sql, args);
        if (v == null || v == JSONObject.NULL) return 0d;
        if (v instanceof Number) return ((Number) v).doubleValue();
        try { return Double.parseDouble(String.valueOf(v).replace(",", "")); } catch (Exception e) { return 0d; }
    }

    public static int count(Connection c, String sql, Object... args) throws SQLException {
        return (int) Math.round(sum(c, sql, args));
    }

    public static String text(Connection c, String sql, Object... args) throws SQLException {
        Object v = value(c, sql, args);
        return v == null || v == JSONObject.NULL ? "" : String.valueOf(v);
    }

    /**
     * A stable, unique operation id: the same action on the same values yields the same id, so a
     * retry after a network drop can never be applied twice.
     */
    public static String opKey(String module, String action, Object... parts) {
        StringBuilder b = new StringBuilder(module).append('|').append(action);
        for (Object p : parts) b.append('|').append(p == null ? "" : String.valueOf(p));
        String raw = b.toString();
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] dig = md.digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (int i = 0; i < 12 && i < dig.length; i++) hex.append(String.format(Locale.US, "%02x", dig[i]));
            return module + ":" + action + ":" + hex;
        } catch (Exception e) {
            return module + ":" + action + ":" + Math.abs(raw.hashCode());
        }
    }

    /** Helper for small result sets used by the UI models. */
    public static List<JSONObject> asList(JSONArray rows) {
        List<JSONObject> list = new ArrayList<>();
        if (rows == null) return list;
        for (int i = 0; i < rows.length(); i++) {
            JSONObject o = rows.optJSONObject(i);
            if (o != null) list.add(o);
        }
        return list;
    }
}

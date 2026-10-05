package ir.meelano.android;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The fast, self-healing SQL kit behind every page that reads Atiran.
 *
 * Why it exists: the manager pages used to open a brand-new jTDS login for every page and to re-read
 * table metadata (sys.columns) dozens of times per page. On a mobile link each round trip to the
 * server costs ~200 ms, so «خانه / گزارش‌ها / هوش مدیریتی / نظارت بر فروش» spent 10-30 seconds
 * talking to the server and ended in «اتصال برقرار نشد» whenever one round trip crossed the jTDS
 * socket timeout. Measured against the live server (CI diagnostic, 1405/07/11):
 *   12 small statements = 5359 ms   ->   the same 8 aggregates batched = 467 ms.
 *
 * What it does:
 *  - keeps a small pool of warm connections (no repeated login per page, automatic reconnect),
 *  - hands them out through a proxy whose close() RETURNS the connection to the pool, so every
 *    existing «try (Connection c = openConnection())» keeps working unchanged,
 *  - caches sys.columns / sys.objects / sys.tables lookups in memory instead of one round trip each,
 *  - caches the Persian «today» and the latest document date, so date filters cost zero round trips,
 *  - builds Persian 'YYYY/MM/DD' ranges that are plain string comparisons - sargable (index friendly)
 *    and loss-free, unlike TRY_CONVERT(date,...) which silently dropped every row whose Jalali date
 *    (e.g. 1405/06/31) is not a valid Gregorian one.
 */
final class MeelanoSql {
    private MeelanoSql() { }

    static final int SQL_PORT = 1433;
    private static final int POOL_MAX = 6;
    private static final long IDLE_VALIDATE_MS = 25_000L;
    private static final long METADATA_TTL_MS = 10 * 60_000L;
    private static final long TODAY_TTL_MS = 5 * 60_000L;
    private static final long LATEST_TTL_MS = 2 * 60_000L;
    private static final Object LOCK = new Object();

    private static final class Pooled {
        Connection raw;
        long lastUsed;
        long generation;
        boolean poisoned;      // a socket-level failure was seen on this connection: never reuse it
    }

    private static final ArrayDeque<Pooled> IDLE = new ArrayDeque<>();
    private static int live = 0;                 // idle + leased + connection attempts in progress
    private static long poolGeneration = 0;
    private static long connects = 0, leases = 0, reconnects = 0, failures = 0;
    private static long lastConnectMs = 0;
    private static String lastError = "";
    private static volatile String lastReconnectReason = "";

    private static final ConcurrentHashMap<String, Set<String>> COLUMNS = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Boolean> TABLES = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Boolean> FUNCTIONS = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, String> LATEST = new ConcurrentHashMap<>();
    private static volatile String todayValue = "";
    private static volatile long todayAt = 0L;
    private static volatile String todayTable = "";   // which table the latest-date fallback looked at

    // ------------------------------------------------------------------ connection pool

    private static Connection build() throws Exception {
        Class.forName("net.sourceforge.jtds.jdbc.Driver");
        String host = MainActivity.sqlHost();
        String url = "jdbc:jtds:sqlserver://" + host + ":" + MainActivity.sqlPort() + "/" + MainActivity.sqlDatabase()
                + ";loginTimeout=8;socketTimeout=30;tcpNoDelay=true;packetSize=8192;appName=MEELANOAndroid;";
        Properties props = new Properties();
        props.setProperty("user", MainActivity.sqlUser());
        props.setProperty("password", MainActivity.sqlPassword());
        props.setProperty("charset", "UTF-8");
        props.setProperty("sendStringParametersAsUnicode", "true");
        Connection c = DriverManager.getConnection(url, props);
        try (Statement st = c.createStatement()) {
            st.execute("SET TRANSACTION ISOLATION LEVEL READ UNCOMMITTED");
            st.execute("SET LOCK_TIMEOUT 5000");
        } catch (Exception ignored) { }
        return c;
    }

    /** A warm pooled connection. close() returns it to the pool instead of closing the socket. */
    static Connection lease() throws Exception {
        Pooled p = take();
        if (p.raw != null) {
            Connection stale = null;
            synchronized (LOCK) {
                if (p.raw != null && p.generation == poolGeneration) {
                    leases++;
                    return wrap(p);
                }
                stale = p.raw;
                p.raw = null;
                live = Math.max(0, live - 1);
                LOCK.notifyAll();
            }
            closeQuietly(stale);
            throw new SQLException("اتصال هنگام تغییر شبکه یا تنظیمات نامعتبر شد؛ دوباره تلاش کنید.");
        }

        // A new connection is built outside LOCK; slow network I/O must never block other borrowers/releases.
        long started = System.currentTimeMillis();
        Connection raw;
        try {
            raw = build();
        } catch (Exception ex) {
            synchronized (LOCK) {
                live = Math.max(0, live - 1);
                failures++;
                lastError = shortMessage(ex);
                LOCK.notifyAll();
            }
            throw ex;
        }
        long took = System.currentTimeMillis() - started;
        boolean stale;
        synchronized (LOCK) {
            stale = p.generation != poolGeneration;
            if (stale) {
                live = Math.max(0, live - 1);
                failures++;
                lastError = "اتصال هنگام تغییر شبکه یا تنظیمات لغو شد.";
                LOCK.notifyAll();
            } else {
                p.raw = raw;
                p.lastUsed = System.currentTimeMillis();
                connects++;
                lastConnectMs = took;
                leases++;
                LOCK.notifyAll();
            }
        }
        if (stale) {
            closeQuietly(raw);
            throw new SQLException("اتصال هنگام تغییر شبکه یا تنظیمات نامعتبر شد؛ دوباره تلاش کنید.");
        }
        return wrap(p);
    }

    /** Take a healthy idle connection or reserve a slot. Validation happens outside LOCK. */
    private static Pooled take() throws SQLException {
        long deadline = System.currentTimeMillis() + 20_000L;
        while (true) {
            Pooled candidate = null;
            Pooled reservation = null;
            Connection stale = null;
            synchronized (LOCK) {
                if (!IDLE.isEmpty()) {
                    Pooled idle = IDLE.pollFirst();
                    if (idle.generation == poolGeneration) candidate = idle;
                    else {
                        stale = idle.raw;
                        idle.raw = null;
                        live = Math.max(0, live - 1);
                    }
                }
                if (candidate == null && live < POOL_MAX) {
                    reservation = new Pooled();
                    reservation.generation = poolGeneration;
                    live++;
                } else if (candidate == null) {
                    long wait = deadline - System.currentTimeMillis();
                    if (wait <= 0) throw new SQLException("ظرفیت اتصال به سرور پر است؛ چند لحظه بعد دوباره تلاش کنید.");
                    try { LOCK.wait(Math.min(wait, 400L)); }
                    catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new SQLException("درخواست اتصال لغو شد.", e);
                    }
                }
            }
            closeQuietly(stale);
            if (reservation != null) return reservation;
            if (candidate == null) continue;

            boolean valid = healthy(candidate);
            Connection discard = null;
            synchronized (LOCK) {
                if (valid && candidate.generation == poolGeneration) return candidate;
                discard = candidate.raw;
                candidate.raw = null;
                live = Math.max(0, live - 1);
                LOCK.notifyAll();
            }
            closeQuietly(discard);
        }
    }

    /** Cheap liveness check; network validation runs outside LOCK so other pool users can make progress. */
    private static boolean healthy(Pooled p) {
        try {
            if (p.raw == null || p.raw.isClosed()) return false;
            long idleFor = System.currentTimeMillis() - p.lastUsed;
            if (idleFor > IDLE_VALIDATE_MS) {
                try (Statement st = p.raw.createStatement()) {
                    st.setQueryTimeout(8);
                    try (ResultSet r = st.executeQuery("SELECT 1")) { return r.next(); }
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static void closeQuietly(Connection c) {
        try { if (c != null) c.close(); } catch (Exception ignored) { }
    }

    /** Return a borrowed connection; stale generations and double-closes are never reinserted. */
    static void release(Pooled p) {
        if (p == null || p.raw == null) {
            synchronized (LOCK) { LOCK.notifyAll(); }
            return;
        }
        Connection raw = p.raw;
        boolean physicallyClosed;
        try { physicallyClosed = raw.isClosed(); } catch (Exception e) { physicallyClosed = true; }
        Connection close = null;
        synchronized (LOCK) {
            if (p.raw == null) { LOCK.notifyAll(); return; }
            p.lastUsed = System.currentTimeMillis();
            boolean usable = !physicallyClosed && !p.poisoned && p.generation == poolGeneration;
            if (usable && IDLE.size() < POOL_MAX) IDLE.addFirst(p);
            else {
                close = p.raw;
                p.raw = null;
                live = Math.max(0, live - 1);
            }
            LOCK.notifyAll();
        }
        closeQuietly(close);
    }

    /** jTDS raises these when the socket to the server died; the whole pool must be rebuilt. */
    private static boolean connectionLost(Throwable t) {
        String m = t == null ? "" : String.valueOf(t.getMessage());
        String c = t == null ? "" : t.getClass().getName();
        String all = (m + " " + c).toLowerCase(Locale.US);
        return all.contains("connection reset") || all.contains("broken pipe") || all.contains("socket")
                || all.contains("read timed out") || all.contains("timed out") || all.contains("connection is closed")
                || all.contains("connection closed") || all.contains("i/o error") || all.contains("network")
                || all.contains("sockettimeout") || all.contains("unexpected end of stream");
    }

    /** Drop idle connections and mark every outstanding lease stale for its next release. */
    static void invalidateAll(String reason) {
        java.util.List<Connection> toClose = new java.util.ArrayList<>();
        synchronized (LOCK) {
            poolGeneration++;
            while (!IDLE.isEmpty()) {
                Pooled p = IDLE.pollFirst();
                if (p.raw != null) toClose.add(p.raw);
                p.raw = null;
                live = Math.max(0, live - 1);
            }
            reconnects++;
            if (reason != null && !reason.isEmpty()) { lastError = reason; lastReconnectReason = reason; }
            LOCK.notifyAll();
        }
        for (Connection c : toClose) closeQuietly(c);
        COLUMNS.clear(); TABLES.clear(); FUNCTIONS.clear(); LATEST.clear(); QUALIFIED.clear();
        todayValue = ""; todayAt = 0L;
    }

    static String reconnectReason() { return lastReconnectReason; }

    static String stats() {
        synchronized (LOCK) {
            return "اتصال‌های ساخته‌شده: " + connects + " • بازاستفاده: " + (leases - connects < 0 ? 0 : leases - connects)
                    + " • اتصال مجدد: " + reconnects + " • خطا: " + failures
                    + " • آخرین اتصال: " + lastConnectMs + "ms" + (lastError.isEmpty() ? "" : " • آخرین خطا: " + lastError);
        }
    }

    static long lastConnectMillis() { synchronized (LOCK) { return lastConnectMs; } }

    static String lastErrorText() { synchronized (LOCK) { return lastError; } }

    private static String shortMessage(Throwable t) {
        String m = t == null ? "" : (t.getMessage() == null ? t.getClass().getSimpleName() : t.getMessage());
        return m.length() > 160 ? m.substring(0, 160) : m;
    }

    private static Connection wrap(final Pooled p) {
        return MeelanoConnectionLease.wrap(p.raw, () -> release(p), failure -> {
            boolean lost = connectionLost(failure);
            String message = shortMessage(failure);
            synchronized (LOCK) {
                lastError = message;
                if (lost) { p.poisoned = true; lastReconnectReason = message; }
            }
            // A network/socket failure usually affects every pooled socket, not only the one that
            // reported it. Retire idle connections now and make all other borrowers retire theirs.
            if (lost) invalidateAll(message);
        });
    }

    // ------------------------------------------------------------------ cached metadata

    /** Column names of dbo.<table>, cached for the life of the process (one round trip per table). */
    /**
     * Columns of a table or view. The object is looked up in dbo first and then in any other schema,
     * because Atiran keeps some tables outside dbo (Hamrah.Visit, for example), and the old dbo-only
     * lookup made those pages report "table missing" while the data was there.
     */
    static Set<String> columns(Connection c, String table) {
        if (table == null || table.trim().isEmpty()) return Collections.emptySet();
        String key = table.trim().toLowerCase(Locale.US);
        Set<String> cached = COLUMNS.get(key);
        if (cached != null) return cached;
        Set<String> set = new LinkedHashSet<>();
        String objectIdSql = "SELECT TOP (1) CAST(o.object_id AS nvarchar(20)) FROM sys.objects o JOIN sys.schemas s ON s.schema_id=o.schema_id "
                + "WHERE o.type IN (N'U',N'V') AND o.name=? ORDER BY CASE WHEN s.name=N'dbo' THEN 0 ELSE 1 END, s.name";
        try (PreparedStatement ps = c.prepareStatement(objectIdSql)) {
            ps.setString(1, table.trim());
            String objectId = null;
            try (ResultSet r = ps.executeQuery()) { if (r.next()) objectId = r.getString(1); }
            if (objectId != null) {
                try (PreparedStatement cs = c.prepareStatement("SELECT name FROM sys.columns WHERE object_id=CAST(? AS int) ORDER BY column_id")) {
                    cs.setString(1, objectId);
                    try (ResultSet r = cs.executeQuery()) { while (r.next()) set.add(r.getString(1)); }
                }
            }
        } catch (Exception ignored) { }
        if (!set.isEmpty()) COLUMNS.put(key, set);
        return set;
    }

    /** «schema.name» of the table/view, dbo preferred, or null when it does not exist anywhere. */
    static String qualifiedTable(Connection c, String name) {
        if (name == null || name.trim().isEmpty()) return null;
        String key = name.trim().toLowerCase(Locale.US);
        if (QUALIFIED.containsKey(key)) return QUALIFIED.get(key);
        String found = null;
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT TOP (1) s.name + N'.' + o.name FROM sys.objects o JOIN sys.schemas s ON s.schema_id=o.schema_id "
                        + "WHERE o.type IN (N'U',N'V') AND o.name=? ORDER BY CASE WHEN s.name=N'dbo' THEN 0 ELSE 1 END, s.name")) {
            ps.setString(1, name.trim());
            try (ResultSet r = ps.executeQuery()) { if (r.next()) found = r.getString(1); }
        } catch (Exception ignored) { }
        QUALIFIED.put(key, found);
        return found;
    }

    private static final Map<String, String> QUALIFIED = new ConcurrentHashMap<>();

    static boolean tableExists(Connection c, String table) {
        if (table == null || table.trim().isEmpty()) return false;
        String key = table.trim().toLowerCase(Locale.US);
        Boolean cached = TABLES.get(key);
        if (cached != null) return cached;
        boolean found = false;
        // Any schema counts (Hamrah.Visit): existence must not depend on the owner schema.
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT 1 FROM sys.tables t WHERE t.name=?")) {
            ps.setString(1, table.trim());
            try (ResultSet r = ps.executeQuery()) { found = r.next(); }
        } catch (Exception ignored) { }
        if (!found) {
            // views count too (VW_Forush_DarBazeZamani, vw_customer, ...)
            try (PreparedStatement ps = c.prepareStatement(
                    "SELECT 1 FROM sys.views v WHERE v.name=?")) {
                ps.setString(1, table.trim());
                try (ResultSet r = ps.executeQuery()) { found = r.next(); }
            } catch (Exception ignored) { }
        }
        TABLES.put(key, found);
        return found;
    }

    static boolean hasFunction(Connection c, String name) {
        if (name == null || name.trim().isEmpty()) return false;
        String key = name.trim().toLowerCase(Locale.US);
        Boolean cached = FUNCTIONS.get(key);
        if (cached != null) return cached;
        boolean found = false;
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT COUNT_BIG(1) FROM sys.objects WHERE type IN ('FN','IF','TF') AND name=?")) {
            ps.setString(1, name.trim());
            try (ResultSet r = ps.executeQuery()) { found = r.next() && r.getLong(1) > 0; }
        } catch (Exception ignored) { }
        FUNCTIONS.put(key, found);
        return found;
    }

    /** dbo.<table>'s newest date text (Persian 'YYYY/MM/DD' or a Gregorian date rendered by the server). */
    static String latestDate(Connection c, String table, String column) {
        if (table == null || column == null) return "";
        String key = (table + "|" + column).toLowerCase(Locale.US);
        String cached = LATEST.get(key);
        if (cached != null) return cached;
        String value = "";
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT MAX(NULLIF(CONVERT(nvarchar(20),[" + column + "]),N'')) FROM dbo.[" + table + "] WITH (NOLOCK)")) {
            ps.setQueryTimeout(12);
            try (ResultSet r = ps.executeQuery()) { if (r.next() && r.getString(1) != null) value = r.getString(1); }
        } catch (Exception ignored) { }
        LATEST.put(key, value);
        return value;
    }

    /** Atiran's Persian «today» (1405/07/11): server clock first, then the newest sales date, then device clock. */
    static String persianToday(Connection c) {
        long now = System.currentTimeMillis();
        String value = todayValue;
        if (!value.isEmpty() && now - todayAt < TODAY_TTL_MS) return value;
        try (Statement st = c.createStatement()) {
            st.setQueryTimeout(10);
            try (ResultSet r = st.executeQuery("SELECT CAST(dbo.UDF_Gregorian_To_Persian(GETDATE()) AS nvarchar(30))")) {
                if (r.next()) {
                    String v = MeelanoJalali.normalize(r.getString(1));
                    if (v.matches("^1[34]\\d{2}/\\d{2}/\\d{2}$")) { todayValue = v; todayAt = now; return v; }
                }
            }
        } catch (Exception ignored) { }
        String latest = latestDate(c, "sailfact", "date");
        if (latest != null && latest.matches("^1[34]\\d{2}/\\d{2}/\\d{2}$")) { todayValue = latest; todayAt = now; return latest; }
        String device = MeelanoJalali.format(MeelanoJalali.today());
        todayValue = device; todayAt = now;
        return device;
    }

    /** The anchor a range filter should use: the server's Persian today, or the newest sale when the data lags. */
    static String rangeAnchor(Connection c) {
        String today = persianToday(c);
        String latest = latestDate(c, "sailfact", "date");
        if (latest != null && latest.matches("^1[34]\\d{2}/\\d{2}/\\d{2}$") && latest.compareTo(today) > 0) return latest;
        return today;
    }

    // ------------------------------------------------------------------ Persian ranges

    /** {from,to} inclusive Persian strings for 0=روز 1=۷ روز 2=۳۰ روز 3=۱۲ ماه. */
    static String[] bounds(String anchor, int range) {
        if (anchor == null || anchor.isEmpty()) anchor = MeelanoJalali.format(MeelanoJalali.today());
        switch (range) {
            case 0: return new String[]{anchor, anchor};
            case 1: return new String[]{MeelanoJalali.addDays(anchor, -6), anchor};
            case 3:
                String yearAgo = MeelanoJalali.monthStart(MeelanoJalali.addDays(anchor, -364));
                return new String[]{yearAgo, anchor};
            case 2:
            default:
                return new String[]{MeelanoJalali.addDays(anchor, -29), anchor};
        }
    }

    /** {from,to} of the period immediately before {@link #bounds} - for «نسبت به بازهٔ قبل». */
    static String[] previousBounds(String anchor, int range) {
        if (anchor == null || anchor.isEmpty()) anchor = MeelanoJalali.format(MeelanoJalali.today());
        switch (range) {
            case 0: return new String[]{MeelanoJalali.addDays(anchor, -1), MeelanoJalali.addDays(anchor, -1)};
            case 1: return new String[]{MeelanoJalali.addDays(anchor, -13), MeelanoJalali.addDays(anchor, -7)};
            case 3: return new String[]{MeelanoJalali.addDays(anchor, -729), MeelanoJalali.addDays(anchor, -365)};
            case 2:
            default: return new String[]{MeelanoJalali.addDays(anchor, -59), MeelanoJalali.addDays(anchor, -30)};
        }
    }

    /** The date part of a char/nvarchar Jalali date column ("1405/07/11" out of "1405/07/11 12:04"). */
    static String dateOf(String alias, String column) {
        String col = (alias == null || alias.isEmpty() ? "" : alias + ".") + "[" + column + "]";
        return "LEFT(LTRIM(RTRIM(TRY_CONVERT(nvarchar(30)," + col + "))),10)";
    }

    /**
     * A Jalali range filter as plain string comparands. Every Jalali date in Atiran is stored
     * zero-padded ("1405/07/11"), so lexicographic order is calendar order: no CAST to date, no
     * invalid date to drop a row, and the predicate stays simple enough to seek an index.
     * The old form TRY_CONVERT(date,...) returned NULL for months 31 like 1405/06/31 and silently
     * removed those documents from every report (37 sailfact rows in the live DB).
     */
    static String range(String alias, String column, String[] bounds) {
        String col = dateOf(alias, column);
        return col + ">='" + literal(bounds[0]) + "' AND " + col + "<='" + literal(bounds[1]) + "'";
    }

    /** The range filter for 0=روز 1=۷ روز 2=۳۰ روز 3=۱۲ ماه, anchored on Atiran's own today. */
    static String rangeCondition(String column, String anchor, int range, String alias) {
        return range(alias, column, bounds(anchor, range));
    }

    /** The period immediately before {@link #rangeCondition} — for «نسبت به بازهٔ قبل». */
    static String previousRangeCondition(String column, String anchor, int range, String alias) {
        return range(alias, column, previousBounds(anchor, range));
    }

    /** The range filter for a period counted back in days from the anchor (used by ageing buckets). */
    static String daysBack(String column, String anchor, int days, String alias) {
        String[] b = new String[]{MeelanoJalali.addDays(anchor, -days), anchor};
        return range(alias, column, b);
    }

    static String day(String alias, String column, String date) {
        return dateOf(alias, column) + "='" + literal(date) + "'";
    }

    /** Safe N-less literal: Atiran dates are ASCII digits and '/', so this never needs an nvarchar cast. */
    static String literal(String s) {
        if (s == null) return "";
        return s.replace("'", "''").replace("\u0000", "");
    }

    /**
     * Throws when the generated statement has unbalanced parentheses. String literals (''), quoted
     * identifiers ([..]) and comments are skipped so real SQL is never rejected by a false positive.
     */
    static void checkSqlParentheses(String sql) throws SQLException {
        if (sql == null || sql.isEmpty()) return;
        int depth = 0, min = 0, n = sql.length();
        for (int i = 0; i < n; i++) {
            char ch = sql.charAt(i);
            if (ch == '\'') {
                i++;
                while (i < n) { if (sql.charAt(i) == '\'') { if (i + 1 < n && sql.charAt(i + 1) == '\'') { i += 2; continue; } break; } i++; }
            } else if (ch == '[') {
                while (i < n && sql.charAt(i) != ']') i++;
            } else if (ch == '-' && i + 1 < n && sql.charAt(i + 1) == '-') {
                while (i < n && sql.charAt(i) != '\n') i++;
            } else if (ch == '/' && i + 1 < n && sql.charAt(i + 1) == '*') {
                i += 2;
                while (i + 1 < n && !(sql.charAt(i) == '*' && sql.charAt(i + 1) == '/')) i++;
            } else if (ch == '(') {
                depth++;
            } else if (ch == ')') {
                depth--;
                if (depth < min) min = depth;
            }
        }
        if (depth != 0 || min < 0) {
            String preview = sql.length() > 400 ? sql.substring(0, 400) + " …" : sql;
            throw new SQLException("MeelanoSql: دستور SQL تولیدشده پرانتز نامتوازن دارد ("
                    + (depth > 0 ? (depth + " پرانتز بسته‌نشده") : "پرانتز بستهٔ اضافه") + ") :: " + preview);
        }
    }

    /** Executes one batched statement and returns its single row (Long/Double/BigDecimal/String). */
    static Object[] oneRow(Connection c, String sql) throws SQLException {
        try (Statement st = c.createStatement()) {
            st.setQueryTimeout(25);
            try (ResultSet r = st.executeQuery(sql)) {
                if (!r.next()) return new Object[0];
                int n = r.getMetaData().getColumnCount();
                Object[] out = new Object[n];
                for (int i = 0; i < n; i++) {
                    Object v = r.getObject(i + 1);
                    if (v instanceof java.math.BigDecimal) out[i] = Double.valueOf(((java.math.BigDecimal) v).doubleValue());
                    else out[i] = v;
                }
                return out;
            }
        }
    }

    static double num(Object[] row, int index) {
        if (row == null || index < 0 || index >= row.length || row[index] == null) return 0;
        Object v = row[index];
        if (v instanceof Number) return ((Number) v).doubleValue();
        try { return Double.parseDouble(String.valueOf(v)); } catch (Exception e) { return 0; }
    }

    static long lng(Object[] row, int index) {
        if (row == null || index < 0 || index >= row.length || row[index] == null) return 0;
        Object v = row[index];
        if (v instanceof Number) return ((Number) v).longValue();
        try { return Long.parseLong(String.valueOf(v)); } catch (Exception e) { return 0; }
    }

    static String str(Object[] row, int index) {
        if (row == null || index < 0 || index >= row.length || row[index] == null) return "";
        return String.valueOf(row[index]).trim();
    }

    /** True when the text looks like a real amount (used instead of a second round trip for a check). */
    static boolean positive(Object[] row, int index) { return num(row, index) > 0; }

    static Set<String> of(String... names) {
        Set<String> set = new HashSet<>();
        if (names != null) for (String n : names) if (n != null) set.add(n);
        return set;
    }
}

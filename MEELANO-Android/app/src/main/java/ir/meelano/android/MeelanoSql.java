package ir.meelano.android;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
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
        long createdAt;
    }

    private static final ArrayDeque<Pooled> IDLE = new ArrayDeque<>();
    private static int live = 0;                 // idle + leased
    private static long connects = 0, leases = 0, reconnects = 0, failures = 0;
    private static long lastConnectMs = 0, lastLeaseMs = 0;
    private static String lastError = "";

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
        if (p != null) {
            synchronized (LOCK) { leases++; }
            return wrap(p);
        }
        // no idle connection and the pool is not full: create one outside the lock (never block the UI thread pool)
        long started = System.currentTimeMillis();
        Connection raw;
        try {
            raw = build();
        } catch (Exception ex) {
            synchronized (LOCK) {
                live--;
                failures++;
                lastError = shortMessage(ex);
                LOCK.notifyAll();
            }
            throw ex;
        }
        long took = System.currentTimeMillis() - started;
        synchronized (LOCK) {
            Pooled np = new Pooled();
            np.raw = raw;
            np.createdAt = System.currentTimeMillis();
            np.lastUsed = np.createdAt;
            connects++;
            lastConnectMs = took;
            leases++;
            LOCK.notifyAll();
            return wrap(np);
        }
    }

    /** Take an idle connection, or reserve a slot (returns null when the caller must connect). */
    private static Pooled take() throws SQLException {
        long deadline = System.currentTimeMillis() + 20_000L;
        synchronized (LOCK) {
            while (true) {
                while (!IDLE.isEmpty()) {
                    Pooled p = IDLE.pollFirst();
                    if (healthy(p)) return p;
                    discard(p);
                }
                if (live < POOL_MAX) { live++; return null; }
                long wait = deadline - System.currentTimeMillis();
                if (wait <= 0) throw new SQLException("ظرفیت اتصال به سرور پر است؛ چند لحظه بعد دوباره تلاش کنید.");
                try { LOCK.wait(Math.min(wait, 400L)); } catch (InterruptedException ignored) { }
            }
        }
    }

    /** Cheap liveness check; only touches the server when the connection has been idle for a while. */
    private static boolean healthy(Pooled p) {
        try {
            if (p.raw == null || p.raw.isClosed()) return false;
            long idleFor = System.currentTimeMillis() - p.lastUsed;
            if (idleFor > IDLE_VALIDATE_MS) {
                try (Statement st = p.raw.createStatement()) {
                    st.setQueryTimeout(8);
                    try (ResultSet r = st.executeQuery("SELECT 1")) { r.next(); }
                }
            }
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    private static void discard(Pooled p) {
        live = Math.max(0, live - 1);
        try { if (p.raw != null) p.raw.close(); } catch (Throwable ignored) { }
    }

    static void release(Pooled p) {
        synchronized (LOCK) {
            if (p == null || p.raw == null) { LOCK.notifyAll(); return; }
            p.lastUsed = System.currentTimeMillis();
            boolean usable;
            try { usable = !p.raw.isClosed(); } catch (Throwable t) { usable = false; }
            if (usable && IDLE.size() < POOL_MAX) IDLE.addFirst(p);
            else discard(p);
            LOCK.notifyAll();
        }
    }

    /** Drop every pooled connection (used after a timeout/retry, and by the connection-health page). */
    static void invalidateAll(String reason) {
        synchronized (LOCK) {
            while (!IDLE.isEmpty()) discard(IDLE.pollFirst());
            reconnects++;
            if (reason != null && !reason.isEmpty()) lastError = reason;
            LOCK.notifyAll();
        }
        COLUMNS.clear(); TABLES.clear(); FUNCTIONS.clear(); LATEST.clear();
        todayValue = ""; todayAt = 0L;
    }

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

    /** Proxy so that legacy try-with-resources blocks release (not close) the pooled connection. */
    private static Connection wrap(final Pooled p) {
        return (Connection) Proxy.newProxyInstance(MeelanoSql.class.getClassLoader(), new Class<?>[]{Connection.class},
                new InvocationHandler() {
                    @Override public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        String name = method.getName();
                        if ("close".equals(name)) { release(p); return null; }
                        if ("isClosed".equals(name)) {
                            try { return p.raw == null || p.raw.isClosed(); } catch (Throwable t) { return true; }
                        }
                        if ("toString".equals(name)) return "meelano-pooled-connection";
                        if ("hashCode".equals(name)) return System.identityHashCode(p);
                        if ("equals".equals(name)) return proxy == (args == null || args.length == 0 ? null : args[0]);
                        try {
                            return method.invoke(p.raw, args);
                        } catch (InvocationTargetException ex) {
                            Throwable cause = ex.getCause() == null ? ex : ex.getCause();
                            synchronized (LOCK) { lastError = shortMessage(cause); }
                            throw cause;
                        }
                    }
                });
    }

    // ------------------------------------------------------------------ cached metadata

    /** Column names of dbo.<table>, cached for the life of the process (one round trip per table). */
    static Set<String> columns(Connection c, String table) {
        if (table == null || table.trim().isEmpty()) return Collections.emptySet();
        String key = table.trim().toLowerCase(Locale.US);
        Set<String> cached = COLUMNS.get(key);
        if (cached != null) return cached;
        Set<String> set = new LinkedHashSet<>();
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT c.name FROM sys.columns c JOIN sys.objects o ON o.object_id=c.object_id "
                        + "JOIN sys.schemas s ON s.schema_id=o.schema_id WHERE s.name=N'dbo' AND o.type IN (N'U',N'V') AND o.name=? ORDER BY c.column_id")) {
            ps.setString(1, table.trim());
            try (ResultSet r = ps.executeQuery()) { while (r.next()) set.add(r.getString(1)); }
        } catch (Exception ignored) { }
        if (!set.isEmpty()) COLUMNS.put(key, set);
        return set;
    }

    static boolean tableExists(Connection c, String table) {
        if (table == null || table.trim().isEmpty()) return false;
        String key = table.trim().toLowerCase(Locale.US);
        Boolean cached = TABLES.get(key);
        if (cached != null) return cached;
        boolean found = false;
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT 1 FROM sys.tables t JOIN sys.schemas s ON s.schema_id=t.schema_id WHERE s.name=N'dbo' AND t.name=?")) {
            ps.setString(1, table.trim());
            try (ResultSet r = ps.executeQuery()) { found = r.next(); }
        } catch (Exception ignored) { }
        if (!found) {
            // views count too (VW_Forush_DarBazeZamani, vw_customer, ...)
            try (PreparedStatement ps = c.prepareStatement(
                    "SELECT 1 FROM sys.views v JOIN sys.schemas s ON s.schema_id=v.schema_id WHERE s.name=N'dbo' AND v.name=?")) {
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

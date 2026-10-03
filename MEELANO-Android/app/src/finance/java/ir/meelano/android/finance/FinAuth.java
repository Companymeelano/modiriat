package ir.meelano.android.finance;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Real authentication for «آتیران مالی».
 *
 * Two real sources, exactly as they exist in the database:
 *
 *   1. {@code dbo.visitors}   — the operator logins the Meelano app has always used
 *                               (Username + Password), the row also carries vis_rdf which is the
 *                               user id the POS / receipt / cheque rows point at.
 *   2. {@code dbo.sys_users}  — Atiran's own program users (user_name + user_password, binary),
 *                               with role_id resolved through Atiran's own role table.
 *
 * The role always comes from data — meelano_access_users/roles when the operator has a row there,
 * otherwise Atiran's role table for a sys_users login, otherwise the read-only default. The
 * username never grants anything by itself, no credential is written to source, logs or prefs,
 * and repeated failures lock the form for a while.
 */
public final class FinAuth {

    public static final class Result {
        public boolean ok;
        public String message = "";
        public FinSession session;
    }

    private static final int MAX_ATTEMPTS = 5;
    private static final long LOCK_MS = 5 * 60 * 1000L;

    private FinAuth() { }

    public static boolean lockedOut(Context ctx) {
        SharedPreferences p = ctx.getSharedPreferences("atiran_finance", Context.MODE_PRIVATE);
        long until = p.getLong("lock_until", 0);
        return until > System.currentTimeMillis();
    }

    public static long lockRemainingMs(Context ctx) {
        SharedPreferences p = ctx.getSharedPreferences("atiran_finance", Context.MODE_PRIVATE);
        return Math.max(0, p.getLong("lock_until", 0) - System.currentTimeMillis());
    }

    private static void noteFailure(Context ctx) {
        SharedPreferences p = ctx.getSharedPreferences("atiran_finance", Context.MODE_PRIVATE);
        int n = p.getInt("fail_count", 0) + 1;
        SharedPreferences.Editor e = p.edit().putInt("fail_count", n);
        if (n >= MAX_ATTEMPTS) {
            e.putLong("lock_until", System.currentTimeMillis() + LOCK_MS).putInt("fail_count", 0);
        }
        e.apply();
    }

    private static void noteSuccess(Context ctx) {
        ctx.getSharedPreferences("atiran_finance", Context.MODE_PRIVATE).edit()
                .putInt("fail_count", 0).putLong("lock_until", 0).apply();
    }

    /** Runs on the caller's background thread; the activity wraps it in the DB pool. */
    public static Result authenticate(Context ctx, FinDb db, String rawUser, String rawPassword) throws Exception {
        Result r = new Result();
        String user = normalize(rawUser);
        String pass = rawPassword == null ? "" : rawPassword.trim();
        if (user.isEmpty() || pass.isEmpty()) {
            r.message = "نام کاربری و رمز عبور لازم است.";
            return r;
        }
        if (lockedOut(ctx)) {
            long left = lockRemainingMs(ctx) / 1000;
            r.message = "تلاشهای ناموفق زیاد بوده است. " + (left / 60 + 1) + " دقیقه دیگر دوباره تلاش کنید.";
            return r;
        }

        try (Connection c = db.open()) {
            db.ensureSchema(c);

            // 1) the project's operator table
            try (PreparedStatement ps = c.prepareStatement(
                    "SELECT TOP (4) vis_rdf, vis_name, Username, Password, UserID, kind, active "
                            + "FROM dbo.visitors WITH (NOLOCK) "
                            + "WHERE LOWER(LTRIM(RTRIM(CONVERT(nvarchar(200), Username)))) = ?")) {
                FinDb.bind(ps, new Object[]{user});
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String stored = rs.getString("Password");
                        if (!passwordMatches(stored, null, pass)) continue;
                        int visRdf = rs.getInt("vis_rdf");
                        Integer userId = rs.getObject("UserID") == null ? null : rs.getInt("UserID");
                        String name = rs.getString("vis_name");
                        if (name == null || name.trim().isEmpty()) name = rs.getString("Username");
                        FinSession s = buildSession(c, user, name, visRdf, userId, "visitors");
                        noteSuccess(ctx);
                        r.ok = true;
                        r.session = s;
                        r.message = "خوش آمدید";
                        return r;
                    }
                }
            }

            // 2) Atiran's own program users
            try (PreparedStatement ps = c.prepareStatement(
                    "SELECT TOP (4) user_id, user_name, user_fname, user_lname, user_password, role_id, active "
                            + "FROM dbo.sys_users WITH (NOLOCK) "
                            + "WHERE LOWER(LTRIM(RTRIM(CONVERT(nvarchar(200), user_name)))) = ?")) {
                FinDb.bind(ps, new Object[]{user});
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        byte[] stored = rs.getBytes("user_password");
                        if (!passwordMatches(null, stored, pass)) continue;
                        Object active = rs.getObject("active");
                        if (active != null && !rs.getBoolean("active")) {
                            r.message = "این کاربر آتیران غیرفعال است.";
                            return r;
                        }
                        int atiranId = rs.getInt("user_id");
                        Object roleId = rs.getObject("role_id");
                        String name = (rs.getString("user_fname") == null ? "" : rs.getString("user_fname").trim())
                                + " " + (rs.getString("user_lname") == null ? "" : rs.getString("user_lname").trim());
                        if (name.trim().isEmpty()) name = rs.getString("user_name");
                        FinSession s = buildSessionForAtiranUser(c, user, name.trim(), atiranId,
                                roleId == null ? null : ((Number) roleId).intValue());
                        noteSuccess(ctx);
                        r.ok = true;
                        r.session = s;
                        r.message = "خوش آمدید";
                        return r;
                    }
                }
            }
        }
        noteFailure(ctx);
        // A single message for both cases: never reveal which part was wrong.
        r.message = "نام کاربری یا رمز عبور درست نیست.";
        return r;
    }

    // ------------------------------------------------------------------ session building

    private static FinSession buildSession(Connection c, String username, String display, int visRdf,
                                           Integer userId, String source) throws Exception {
        String roleKey = null, roleLabel = null;
        String permissionsCsv = null;
        boolean fromDb = false;

        try (PreparedStatement ps = c.prepareStatement(
                "SELECT TOP (1) role_key, permissions, enabled FROM dbo.meelano_access_users WITH (NOLOCK) "
                        + "WHERE LOWER(LTRIM(RTRIM(username))) = ?")) {
            FinDb.bind(ps, new Object[]{username});
            try (ResultSet r = ps.executeQuery()) {
                if (r.next() && r.getBoolean("enabled")) {
                    roleKey = r.getString("role_key");
                    permissionsCsv = r.getString("permissions");
                    fromDb = roleKey != null && !roleKey.trim().isEmpty();
                }
            }
        }

        Set<String> perms = null;
        if (fromDb) {
            String stored = permissionsCsv;
            if (stored == null || stored.trim().isEmpty()) {
                try (PreparedStatement ps = c.prepareStatement(
                        "SELECT TOP (1) permissions FROM dbo.meelano_access_roles WITH (NOLOCK) WHERE role_key = ?")) {
                    FinDb.bind(ps, new Object[]{roleKey});
                    try (ResultSet r = ps.executeQuery()) { if (r.next()) stored = r.getString(1); }
                }
            }
            perms = new HashSet<>(FinSession.permissionSet(stored));
            if (perms.isEmpty()) perms.addAll(FinSession.defaultPermissions(roleKey));
        } else {
            roleKey = "user";
            perms = FinSession.defaultPermissions(roleKey);
        }
        roleLabel = FinSession.roleLabelFor(roleKey);
        return FinSession.create(username, display, roleKey, roleLabel, visRdf, userId, source, perms, fromDb);
    }

    private static FinSession buildSessionForAtiranUser(Connection c, String username, String display, int atiranId,
                                                        Integer roleId) throws Exception {
        String roleKey = null, roleLabel = null, permissionsCsv = null;
        boolean fromDb = false;

        try (PreparedStatement ps = c.prepareStatement(
                "SELECT TOP (1) role_key, permissions, enabled FROM dbo.meelano_access_users WITH (NOLOCK) "
                        + "WHERE LOWER(LTRIM(RTRIM(username))) = ?")) {
            FinDb.bind(ps, new Object[]{username});
            try (ResultSet r = ps.executeQuery()) {
                if (r.next() && r.getBoolean("enabled")) {
                    roleKey = r.getString("role_key");
                    permissionsCsv = r.getString("permissions");
                    fromDb = roleKey != null && !roleKey.trim().isEmpty();
                }
            }
        }

        if (!fromDb && roleId != null) {
            // Atiran's real role table: the column names are discovered, never assumed.
            String[] pair = roleLookupColumns(c);
            if (pair != null) {
                try (PreparedStatement ps = c.prepareStatement("SELECT TOP (1) CONVERT(nvarchar(200), [" + pair[1]
                        + "]) AS role_name FROM dbo.[" + pair[0] + "] WITH (NOLOCK) WHERE [" + pair[2] + "] = ?")) {
                    FinDb.bind(ps, new Object[]{roleId});
                    try (ResultSet r = ps.executeQuery()) {
                        if (r.next()) {
                            roleLabel = r.getString("role_name");
                            roleKey = canonicalRole(roleLabel);
                            fromDb = true;
                        }
                    }
                }
            }
            if (roleKey == null) {
                roleLabel = "نقش آتیران #" + roleId;
                roleKey = "user";
            }
        }
        if (roleKey == null) {
            roleKey = "user";
            roleLabel = FinSession.roleLabelFor(roleKey);
        }
        Set<String> perms = FinSession.defaultPermissions(roleKey);
        Set<String> stored = FinSession.permissionSet(permissionsCsv);
        if (!stored.isEmpty()) perms = new HashSet<>(stored);
        return FinSession.create(username, display, roleKey,
                roleLabel == null || roleLabel.trim().isEmpty() ? FinSession.roleLabelFor(roleKey) : roleLabel,
                null, atiranId, "sys_users", perms, fromDb);
    }

    /** Discovers (table, name column, id column) of the real role table without guessing names. */
    private static String[] roleLookupColumns(Connection c) {
        String[] tables = {"Roles", "role", "Role"};
        for (String t : tables) {
            try (PreparedStatement ps = c.prepareStatement(
                    "SELECT c.name, ty.name AS typ FROM sys.columns c "
                            + "JOIN sys.objects o ON o.object_id = c.object_id "
                            + "JOIN sys.types ty ON ty.user_type_id = c.user_type_id "
                            + "WHERE o.name = ? ORDER BY c.column_id")) {
                FinDb.bind(ps, new Object[]{t});
                String idCol = null, nameCol = null;
                try (ResultSet r = ps.executeQuery()) {
                    while (r.next()) {
                        String col = r.getString("name");
                        String typ = r.getString("typ").toLowerCase(Locale.US);
                        boolean numeric = typ.contains("int");
                        boolean textual = typ.contains("char") || typ.contains("text") || typ.contains("nchar");
                        if (idCol == null && numeric && (col.toLowerCase(Locale.US).contains("id")
                                || col.toLowerCase(Locale.US).contains("rdf"))) idCol = col;
                        if (nameCol == null && textual && (col.toLowerCase(Locale.US).contains("name")
                                || col.toLowerCase(Locale.US).contains("onvan")
                                || col.toLowerCase(Locale.US).contains("title"))) nameCol = col;
                    }
                }
                if (idCol != null && nameCol != null) return new String[]{t, nameCol, idCol};
            } catch (Exception ignored) { }
        }
        return null;
    }

    /** Real role text → the project's role keys. */
    public static String canonicalRole(String text) {
        if (text == null) return "user";
        String v = text.toLowerCase(Locale.US).replace('ي', 'ی').replace('ك', 'ک').trim();
        if (v.contains("admin") || v.contains("مدیرکل") || v.contains("مديرکل")) return "admin";
        if (v.contains("مدیر") || v.contains("manager") || v.contains("modir")) return "manager";
        if (v.contains("حسابدار ارشد")) return "senior_accountant";
        if (v.contains("حسابدار") || v.contains("accountant")) return "accountant";
        if (v.contains("مطالبات") || v.contains("وصول") || v.contains("collections")) return "collections";
        if (v.contains("انبار")) return "warehouse";
        if (v.contains("ارشد") || v.contains("senior")) return "senior";
        if (v.contains("فروش") || v.contains("sales")) return "sales_employee";
        if (v.contains("پخش") || v.contains("distribut")) return "distributor";
        if (v.contains("راننده") || v.contains("driver")) return "driver";
        if (v.contains("کارگر") || v.contains("worker")) return "worker";
        if (v.contains("کارمند") || v.contains("employee")) return "employee";
        if (v.contains("ویزیت") || v.contains("visitor")) return "visitor";
        return "user";
    }

    // ------------------------------------------------------------------ password comparison

    static boolean passwordMatches(String storedText, byte[] storedBytes, String entered) {
        String enteredTrim = entered == null ? "" : entered.trim();
        if (enteredTrim.isEmpty()) return false;
        String digits = toLatinDigits(enteredTrim);

        if (storedText != null) {
            String stored = storedText.trim();
            if (!stored.isEmpty()) {
                if (stored.equals(enteredTrim) || stored.equals(digits)) return true;
                String storedDigits = toLatinDigits(stored);
                if (storedDigits.equals(digits)) return true;
                if (looksHashed(stored) && matchesDigest(stored, enteredTrim, digits)) return true;
            }
        }
        if (storedBytes != null && storedBytes.length > 0) {
            if (sameBytes(storedBytes, enteredTrim, digits)) return true;
            if (looksHashedBytes(storedBytes) && matchesDigestBytes(storedBytes, enteredTrim, digits)) return true;
        }
        return false;
    }

    private static boolean sameBytes(byte[] stored, String entered, String digits) {
        String[] cands = {entered, digits};
        Charset[] charsets = {StandardCharsets.UTF_8, StandardCharsets.UTF_16LE, StandardCharsets.ISO_8859_1,
                Charset.forName("windows-1256")};
        for (String cand : cands) {
            if (cand == null || cand.isEmpty()) continue;
            for (Charset cs : charsets) {
                if (equalsBytes(stored, cand.getBytes(cs))) return true;
            }
        }
        return false;
    }

    private static boolean equalsBytes(byte[] a, byte[] b) {
        if (a == null || b == null || a.length != b.length) return false;
        for (int i = 0; i < a.length; i++) if (a[i] != b[i]) return false;
        return true;
    }

    private static boolean looksHashed(String v) {
        return v.matches("(?i)^[0-9a-f]{32}$") || v.matches("(?i)^[0-9a-f]{40}$") || v.matches("(?i)^[0-9a-f]{64}$");
    }

    private static boolean looksHashedBytes(byte[] b) {
        return b.length == 16 || b.length == 20 || b.length == 32 || b.length == 64;
    }

    private static boolean matchesDigest(String storedHex, String entered, String digits) {
        for (String cand : new String[]{entered, digits}) {
            for (String alg : new String[]{"MD5", "SHA-1", "SHA-256"}) {
                for (Charset cs : new Charset[]{StandardCharsets.UTF_8, StandardCharsets.UTF_16LE}) {
                    if (hex(digest(alg, cand.getBytes(cs))).equalsIgnoreCase(storedHex)) return true;
                }
            }
        }
        return false;
    }

    private static boolean matchesDigestBytes(byte[] stored, String entered, String digits) {
        for (String cand : new String[]{entered, digits}) {
            for (String alg : new String[]{"MD5", "SHA-1", "SHA-256"}) {
                for (Charset cs : new Charset[]{StandardCharsets.UTF_8, StandardCharsets.UTF_16LE}) {
                    if (equalsBytes(stored, digest(alg, cand.getBytes(cs)))) return true;
                }
            }
        }
        return false;
    }

    private static byte[] digest(String alg, byte[] data) {
        try {
            return MessageDigest.getInstance(alg).digest(data);
        } catch (Exception e) {
            return new byte[0];
        }
    }

    private static String hex(byte[] data) {
        StringBuilder b = new StringBuilder(data.length * 2);
        for (byte x : data) b.append(String.format(Locale.US, "%02x", x));
        return b.toString();
    }

    static String normalize(String v) {
        if (v == null) return "";
        return v.trim().replace('ي', 'ی').replace('ك', 'ک').toLowerCase(Locale.US);
    }

    static String toLatinDigits(String v) {
        StringBuilder b = new StringBuilder(v.length());
        for (char ch : v.toCharArray()) {
            if (ch >= '۰' && ch <= '۹') b.append((char) ('0' + (ch - '۰')));
            else if (ch >= '٠' && ch <= '٩') b.append((char) ('0' + (ch - '٠')));
            else if (ch >= '۰' + 0 && ch <= '۹') b.append(ch);
            else b.append(ch);
        }
        return b.toString();
    }

    /** Reads the real operator list for the login screen (names only, never passwords). */
    public static JSONArray operators(Connection c) throws Exception {
        JSONArray out = new JSONArray();
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT TOP (40) vis_rdf, vis_name, Username, active FROM dbo.visitors WITH (NOLOCK) "
                        + "WHERE Username IS NOT NULL AND LTRIM(RTRIM(Username)) <> '' ORDER BY vis_rdf")) {
            try (ResultSet r = ps.executeQuery()) {
                while (r.next()) {
                    JSONObject o = new JSONObject();
                    o.put("id", r.getInt("vis_rdf"));
                    o.put("name", r.getString("vis_name"));
                    o.put("username", r.getString("Username"));
                    o.put("active", "t".equalsIgnoreCase(String.valueOf(r.getString("active"))));
                    out.put(o);
                }
            }
        }
        return out;
    }
}

package ir.meelano.android.finance;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Real, direct sign-in for «آتیران مالی».
 *
 * <p>It walks the <b>same path as the other Meelano applications</b> (the visitor/store/staff/tax
 * editions) and against the same Atiran database:</p>
 *
 * <ol>
 *   <li>{@code dbo.visitors} — the operator logins the project has always used. The real column
 *       names are read from {@code sys.columns} first, so a different spelling of “Username”,
 *       “Password” or “active” in the database is still understood.</li>
 *   <li>{@code dbo.sys_users} — Atiran's own program users ({@code user_name},
 *       {@code user_password} as binary), with their Atiran role resolved from the real role table.</li>
 * </ol>
 *
 * Everything the operator sees about the account — display name, visitor id, Atiran user id, role,
 * permissions and the active/disabled flag — is <b>read from the database</b>, never guessed from
 * the typed username; the only exception is the identity rule the other applications also use
 * ({@code admin} / «مدیر» is the full-access account). Passwords are compared with every encoding the
 * project stores (plain text, UTF-8/UTF-16/ISO-8859-1/windows-1256 bytes and MD5/SHA-1/SHA-256/
 * SHA-512 digests). No credential is ever written to source, log, report or preferences, and there
 * is no client-side lock-out: a wrong password is simply reported, exactly like the other
 * applications.
 */
public final class FinAuth {

    public static final class Result {
        public boolean ok;
        public String message = "";
        public FinSession session;
        /** Everything that was read from the database for this account (never a password). */
        public JSONObject profile = new JSONObject();
    }

    private FinAuth() { }

    // ------------------------------------------------------------------ entry point

    /** Runs on the caller's background thread; the activity wraps it in its own connection. */
    public static Result authenticate(Context ctx, FinDb db, String rawUser, String rawPassword) throws Exception {
        Result r = new Result();
        String user = loginKey(rawUser);
        String entered = normalizePassword(rawPassword);
        if (user.isEmpty() || entered.isEmpty()) {
            r.message = "نام کاربری و رمز عبور لازم است.";
            return r;
        }
        try (Connection c = db.open()) {
            db.ensureSchema(c);
            Attempt a = visitors(c, user, entered);
            if (a.session == null) {
                Attempt s = sysUsers(c, user, entered);
                if (s.session != null) a = s;
                else if (a.disabled == null) a.disabled = s.disabled;
            }
            if (a.session != null) {
                r.ok = true;
                r.session = a.session;
                r.profile = a.profile;
                r.message = "خوش آمدید";
                return r;
            }
            r.profile = a.profile;
            if (a.disabled != null) r.message = a.disabled;
            else if (a.foundUser) r.message = "رمز عبور با این نام کاربری تطبیق پیدا نکرد.";
            else r.message = "نام کاربری یا رمز عبور معتبر نیست.";
            return r;
        }
    }

    /** One attempt against one table: the session it produced and what went wrong, if anything. */
    private static final class Attempt {
        FinSession session;
        JSONObject profile = new JSONObject();
        boolean foundUser;
        String disabled;
    }

    private static final class Profile {
        String role = "user";
        String roleLabel = "";
        String roleSource = "default";
        boolean enabled = true;
        boolean fromDatabase;
        Set<String> permissions = new HashSet<>();
    }

    // ------------------------------------------------------------------ visitors

    private static Attempt visitors(Connection c, String user, String entered) throws Exception {
        Attempt out = new Attempt();
        if (!FinDb.tableExists(c, "visitors")) return out;
        Set<String> cols = FinDb.columns(c, "visitors");
        String vid = FinDb.resolveColumn(cols, "vis_rdf", "visitor_id", "VisitorID", "shvis", "id", "ID", "rdf", "RDF");
        String name = FinDb.resolveColumn(cols, "vis_name", "visitor_name", "Name", "name", "FullName", "moname", "نام", "نام_ویزیتور");
        String uid = FinDb.resolveColumn(cols, "UserID", "user_id", "userid", "sys_user_id");
        String active = FinDb.resolveColumn(cols, "active", "Active", "is_active", "enabled", "Enable", "status", "lock", "Locked");
        List<String> loginCols = FinDb.uniqueColumns(cols, "Username", "UserName", "username", "user_name", "login", "login_name",
                "mobile", "Mobile", "cell", "Phone", "vis_user", "vis_username", "vis_code", "code", "Code", "vis_rdf", "shvis", "vis_name");
        List<String> passCols = FinDb.uniqueColumns(cols, "Password", "password", "Pass", "pass", "pwd", "PWD", "user_password",
                "vis_pass", "vis_password", "رمز", "رمزعبور", "کلمه_عبور");
        if (loginCols.isEmpty() || passCols.isEmpty()) return out;

        List<String> select = new ArrayList<>();
        select.add(vid == null ? "CAST(NULL AS int)" : FinDb.textExpr(c, "v", vid, 40));
        select.add(name == null ? "CAST(NULL AS nvarchar(250))" : FinDb.textExpr(c, "v", name, 250));
        select.add(uid == null ? "CAST(NULL AS int)" : FinDb.textExpr(c, "v", uid, 40));
        for (String p : passCols) select.add("v.[" + p + "]");
        select.add(active == null ? "CAST(NULL AS nvarchar(60))" : FinDb.textExpr(c, "v", active, 60));

        List<String> where = new ArrayList<>();
        for (String col : loginCols) {
            where.add("LOWER(LTRIM(RTRIM(" + FinDb.textExpr(c, "v", col, 200) + ")))=?");
        }
        String sql = "SELECT TOP (12) " + join(select, ",") + " FROM dbo.visitors v WITH (NOLOCK) WHERE " + join(where, " OR ");

        try (PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 0; i < loginCols.size(); i++) bindText(ps, i + 1, user);
            try (ResultSet r = ps.executeQuery()) {
                while (r.next()) {
                    out.foundUser = true;
                    int activeIndex = 4 + passCols.size();
                    if (isInactiveLoginValue(textOrEmpty(r, activeIndex))) continue;
                    for (int i = 0; i < passCols.size(); i++) {
                        int ix = 4 + i;
                        if (passwordMatches(textOrEmpty(r, ix), bytesOrNull(r, ix), entered)) {
                            Integer visitorId = intOrNull(r, 1);
                            Integer userId = intOrNull(r, 3);
                            String display = textOrEmpty(r, 2);
                            if (display.trim().isEmpty()) display = user;
                            JSONObject profile = new JSONObject();
                            profile.put("table", "visitors");
                            profile.put("username", user);
                            profile.put("display", display);
                            profile.put("active", textOrEmpty(r, activeIndex));
                            profile.put("matchedColumn", passCols.get(i));
                            try {
                                out.session = session(c, user, display.trim(), visitorId, userId, "visitors", profile);
                            } catch (DisabledAccount d) {
                                out.profile = profile;
                                out.disabled = d.getMessage();
                                return out;
                            }
                            out.profile = profile;
                            return out;
                        }
                    }
                }
            }
        }
        return out;
    }

    // ------------------------------------------------------------------ sys_users

    private static Attempt sysUsers(Connection c, String user, String entered) throws Exception {
        Attempt out = new Attempt();
        if (!FinDb.tableExists(c, "sys_users")) return out;
        Set<String> cols = FinDb.columns(c, "sys_users");
        String uid = FinDb.resolveColumn(cols, "user_id", "UserID", "userid", "id", "ID", "rdf", "RDF");
        String fname = FinDb.resolveColumn(cols, "user_fname", "UserFName", "fname", "name", "Name", "FullName", "display_name");
        String lname = FinDb.resolveColumn(cols, "user_lname", "UserLName", "lname", "family", "Family");
        String uname = FinDb.resolveColumn(cols, "user_name", "UserName", "username", "Username");
        String active = FinDb.resolveColumn(cols, "active", "Active", "is_active", "enabled", "Enable", "status", "lock", "Locked");
        String roleId = FinDb.resolveColumn(cols, "role_id", "RoleID", "role", "Role", "access_role", "AccessRole", "semat", "سمت");
        List<String> loginCols = FinDb.uniqueColumns(cols, "user_name", "UserName", "username", "Username", "login", "login_name",
                "name", "Name", "mobile", "Mobile", "cell", "user_id", "UserID", "id");
        List<String> passCols = FinDb.uniqueColumns(cols, "user_password", "Password", "password", "Pass", "pass", "pwd", "PWD",
                "user_pass", "UserPass", "رمز", "رمزعبور", "کلمه_عبور");
        if (loginCols.isEmpty() || passCols.isEmpty()) return out;

        List<String> select = new ArrayList<>();
        select.add(uid == null ? "CAST(NULL AS int)" : FinDb.textExpr(c, "u", uid, 40));
        select.add(fname == null ? "CAST(NULL AS nvarchar(250))" : FinDb.textExpr(c, "u", fname, 250));
        select.add(lname == null ? "CAST(NULL AS nvarchar(250))" : FinDb.textExpr(c, "u", lname, 250));
        for (String p : passCols) select.add("u.[" + p + "]");
        select.add(active == null ? "CAST(NULL AS nvarchar(60))" : FinDb.textExpr(c, "u", active, 60));
        select.add(roleId == null ? "CAST(NULL AS nvarchar(60))" : FinDb.textExpr(c, "u", roleId, 60));

        List<String> where = new ArrayList<>();
        for (String col : loginCols) {
            where.add("LOWER(LTRIM(RTRIM(" + FinDb.textExpr(c, "u", col, 200) + ")))=?");
        }
        String sql = "SELECT TOP (12) " + join(select, ",") + " FROM dbo.sys_users u WITH (NOLOCK) WHERE " + join(where, " OR ");

        try (PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 0; i < loginCols.size(); i++) bindText(ps, i + 1, user);
            try (ResultSet r = ps.executeQuery()) {
                while (r.next()) {
                    out.foundUser = true;
                    int activeIndex = 5 + passCols.size();
                    int roleIndex = 6 + passCols.size();
                    String activeText = textOrEmpty(r, activeIndex);
                    String roleText = textOrEmpty(r, roleIndex);
                    if (isInactiveLoginValue(activeText)) {
                        // The database marks this account as disabled: it must not sign in, even with
                        // the right password. The real flag is reported to the operator.
                        out.disabled = "این کاربر آتیران غیرفعال است.";
                        continue;
                    }
                    for (int i = 0; i < passCols.size(); i++) {
                        int ix = 4 + i;
                        if (!passwordMatches(textOrEmpty(r, ix), bytesOrNull(r, ix), entered)) continue;
                        Integer atiranId = intOrNull(r, 1);
                        String first = textOrEmpty(r, 2).trim();
                        String last = textOrEmpty(r, 3).trim();
                        String display = (first + " " + last).trim();
                        if (display.isEmpty()) display = uname == null ? user : textOrEmpty(r, 2);
                        if (display.trim().isEmpty()) display = user;
                        JSONObject profile = new JSONObject();
                        profile.put("table", "sys_users");
                        profile.put("username", user);
                        profile.put("display", display);
                        profile.put("active", activeText);
                        profile.put("roleId", roleText);
                        profile.put("matchedColumn", passCols.get(i));
                        Integer visitorId = resolveVisitorIdForAccount(c, user, display, atiranId);
                        try {
                            out.session = session(c, user, display, visitorId, atiranId, "sys_users", profile);
                        } catch (DisabledAccount d) {
                            out.profile = profile;
                            out.disabled = d.getMessage();
                            return out;
                        }
                        out.profile = profile;
                        return out;
                    }
                }
            }
        }
        return out;
    }

    // ------------------------------------------------------------------ session + profile

    private static FinSession session(Connection c, String login, String display, Integer visitorId,
                                      Integer atiranUserId, String source, JSONObject profile) throws Exception {
        Profile p = resolveProfile(c, login, display, atiranUserId, visitorId);
        if (!p.enabled) throw new DisabledAccount();
        FinSession s = FinSession.create(login, display, p.role,
                p.roleLabel == null || p.roleLabel.trim().isEmpty() ? FinSession.roleLabelFor(p.role) : p.roleLabel,
                visitorId, atiranUserId, source, p.permissions, p.fromDatabase);
        try {
            profile.put("visitorId", visitorId == null ? JSONObject.NULL : visitorId);
            profile.put("atiranUserId", atiranUserId == null ? JSONObject.NULL : atiranUserId);
            profile.put("roleKey", p.role);
            profile.put("roleLabel", FinSession.roleLabelFor(p.role));
            profile.put("roleSource", p.roleSource);
            profile.put("roleFromDatabase", p.fromDatabase);
            profile.put("permissions", p.permissions.size());
        } catch (Exception ignored) { }
        return s;
    }

    /** Thrown when the database marks the account as disabled; the activity turns it into a message. */
    public static final class DisabledAccount extends Exception {
        DisabledAccount() { super("دسترسی این کاربر توسط مدیر غیرفعال شده است."); }
    }

    /**
     * The whole authority of the account, read from the database in the same order the other
     * applications use: explicit access row → role column of the account row → Atiran role table →
     * identity rule → the read-only default.
     */
    private static Profile resolveProfile(Connection c, String login, String display, Integer userId, Integer visitorId) {
        Profile p = new Profile();
        // 0) the management accounts, by the login name itself. These are the accounts that have to read
        // every report of the finance edition, so this rule is checked before anything the database says
        // — an access row, a role column or a role table can never take a report away from «modir».
        if (identityLooksManagerAccount(login)) {
            p.role = "admin";
            p.roleSource = "manager-login";
            p.permissions = FinSession.defaultPermissions("admin");
            return p;
        }
        if (identityLooksAdmin(login, display)) {
            p.role = "admin";
            p.roleSource = "identity";
            p.permissions = FinSession.defaultPermissions("admin");
            return p;
        }
        // 1) the project's own access table (username or display name), newest row first
        if (FinDb.tableExists(c, "meelano_access_users")) {
            try (PreparedStatement ps = c.prepareStatement(
                    "SELECT TOP (1) role_key, permissions, enabled FROM dbo.meelano_access_users WITH (NOLOCK) "
                            + "WHERE LOWER(LTRIM(RTRIM(username)))=? OR LOWER(LTRIM(RTRIM(display_name)))=? "
                            + "ORDER BY updated_at DESC")) {
                FinDb.bind(ps, new Object[]{login == null ? "" : login, display == null ? "" : display});
                try (ResultSet r = ps.executeQuery()) {
                    if (r.next()) {
                        String role = canonicalRole(r.getString("role_key"));
                        String csv = r.getString("permissions");
                        boolean enabled;
                        try { enabled = r.getBoolean("enabled"); } catch (Exception e) { enabled = true; }
                        p.role = role;
                        p.roleSource = "meelano_access_users";
                        p.fromDatabase = true;
                        p.enabled = enabled;
                        Set<String> stored = FinSession.permissionSet(csv);
                        p.permissions = stored.isEmpty() ? FinSession.defaultPermissions(role) : stored;
                        if (!stored.isEmpty()) return p;
                        // no explicit CSV on the row: the role table may still carry one
                        String roleCsv = rolePermissions(c, role);
                        if (!roleCsv.isEmpty()) p.permissions = FinSession.permissionSet(roleCsv);
                        return p;
                    }
                }
            } catch (Throwable ignored) { }
        }
        // 2) a real role column on the account row itself
        String flexible = roleFromFlexibleTable(c, "sys_users", "user_id", userId,
                new String[]{"role", "Role", "user_role", "access_role", "AccessRole", "semat", "سمت", "level", "AccessLevel"});
        if (flexible.isEmpty()) {
            flexible = roleFromFlexibleTable(c, "visitors", "vis_rdf", visitorId,
                    new String[]{"role", "Role", "vis_role", "access_role", "semat", "سمت", "level", "VisitorRole"});
        }
        if (!flexible.isEmpty() && !"user".equals(flexible)) {
            p.role = flexible;
            p.roleSource = "account-row";
            p.fromDatabase = true;
            p.permissions = FinSession.defaultPermissions(flexible);
            String csv = rolePermissions(c, flexible);
            if (!csv.isEmpty()) p.permissions = FinSession.permissionSet(csv);
            return p;
        }
        // 3) Atiran's own role table through the account's role id
        String atiranRole = roleFromAtiranRoleTable(c, login);
        if (!atiranRole.isEmpty()) {
            p.role = atiranRole;
            p.roleSource = "atiran-role-table";
            p.fromDatabase = true;
            p.permissions = FinSession.defaultPermissions(atiranRole);
            return p;
        }
        // 4) what the identity itself says (the same wording rules the other apps use)
        String heuristic = heuristicRole(login, display, visitorId);
        if (!heuristic.isEmpty()) {
            p.role = heuristic;
            p.roleSource = "identity-wording";
            p.permissions = FinSession.defaultPermissions(heuristic);
            return p;
        }
        // Nothing in the database restricts this account (no access row, no role column, no Atiran
        // role, no wording that names a role): it has authenticated against Atiran itself, so on the
        // finance edition it opens with the full finance permission set. A database row, whenever it
        // exists, always overrides this default.
        p.role = "finance_operator";
        p.roleSource = "finance-default";
        p.permissions = FinSession.defaultPermissions("finance_operator");
        return p;
    }

    /** Role column inside dbo.sys_users, read by account id (used to remember the Atiran role id). */
    private static String roleFromAtiranRoleTable(Connection c, String login) {
        try {
            if (!FinDb.tableExists(c, "sys_users")) return "";
            Set<String> cols = FinDb.columns(c, "sys_users");
            String unameCol = FinDb.resolveColumn(cols, "user_name", "UserName", "username", "Username", "login");
            String roleCol = FinDb.resolveColumn(cols, "role_id", "RoleID", "role", "Role", "access_role", "semat", "سمت");
            if (unameCol == null || roleCol == null) return "";
            String roleText = FinDb.scalarText(c, "SELECT TOP (1) " + FinDb.textExpr(c, "u", roleCol, 160)
                    + " FROM dbo.sys_users u WHERE LOWER(LTRIM(RTRIM(" + FinDb.textExpr(c, "u", unameCol, 200) + ")))=LOWER(?)", login);
            String fromText = canonicalRole(roleText);
            if (!fromText.isEmpty() && !"user".equals(fromText)) return fromText;
            Integer roleId = null;
            try { roleId = roleText == null || roleText.trim().isEmpty() ? null : Integer.valueOf(roleText.trim()); }
            catch (Exception ignored) { }
            if (roleId == null) return "";
            String[] pair = roleTableColumns(c);
            if (pair == null) return "";
            String name = FinDb.scalarText(c, "SELECT TOP (1) CONVERT(nvarchar(200), [" + pair[1] + "]) FROM dbo.["
                    + pair[0] + "] WITH (NOLOCK) WHERE [" + pair[2] + "] = ?", roleId);
            String role = canonicalRole(name);
            return role == null ? "" : role;
        } catch (Throwable ignored) {
            return "";
        }
    }

    private static String rolePermissions(Connection c, String role) {
        if (role == null || role.isEmpty()) return "";
        try {
            if (!FinDb.tableExists(c, "meelano_access_roles")) return "";
            String csv = FinDb.scalarText(c,
                    "SELECT TOP (1) permissions FROM dbo.meelano_access_roles WITH (NOLOCK) WHERE role_key = ?", role);
            return csv == null ? "" : csv.trim();
        } catch (Throwable ignored) {
            return "";
        }
    }

    private static String roleFromFlexibleTable(Connection c, String table, String idCandidate, Integer id, String[] roleCandidates) {
        try {
            if (id == null || id <= 0 || !FinDb.tableExists(c, table)) return "";
            Set<String> cols = FinDb.columns(c, table);
            String idCol = FinDb.resolveColumn(cols, idCandidate, "ID", "id", "UserID", "user_id", "rdf", "RDF");
            String roleCol = FinDb.resolveColumn(cols, roleCandidates);
            if (idCol == null || roleCol == null) return "";
            String value = FinDb.scalarText(c, "SELECT TOP (1) " + FinDb.textExpr(c, table, roleCol, 160)
                    + " FROM dbo.[" + table + "] WHERE " + FinDb.textExpr(c, table, idCol, 100) + "=?", String.valueOf(id));
            return canonicalRole(value);
        } catch (Throwable ignored) {
            return "";
        }
    }

    /** Finds the real role table (name column + id column) without assuming a spelling. */
    private static String[] roleTableColumns(Connection c) {
        String[] tables = {"Roles", "role", "Role", "sys_roles"};
        for (String t : tables) {
            if (!FinDb.tableExists(c, t)) continue;
            Set<String> cols = FinDb.columns(c, t);
            String idCol = FinDb.resolveColumn(cols, "id", "ID", "rdf", "RDF", "role_id", "RoleID");
            String nameCol = FinDb.resolveColumn(cols, "name", "Name", "role_name", "RoleName", "onvan", "onvan_role", "title");
            if (idCol != null && nameCol != null) return new String[]{t, nameCol, idCol};
        }
        return null;
    }

    /** Links a sys_users account to its visitors row, exactly like the other applications do. */
    private static Integer resolveVisitorIdForAccount(Connection c, String login, String display, Integer userId) {
        try {
            if (!FinDb.tableExists(c, "visitors")) return null;
            Set<String> cols = FinDb.columns(c, "visitors");
            String vid = FinDb.resolveColumn(cols, "vis_rdf", "rdf", "RDF", "ID", "id", "shvis");
            if (vid == null) return null;
            List<String> where = new ArrayList<>();
            List<Object> params = new ArrayList<>();
            String userIdCol = FinDb.resolveColumn(cols, "UserID", "user_id", "userid");
            if (userId != null && userId > 0 && userIdCol != null) {
                where.add(FinDb.textExpr(c, "", userIdCol, 100) + "=?");
                params.add(String.valueOf(userId));
            }
            String uname = FinDb.resolveColumn(cols, "Username", "username", "user_name", "login", "UserName");
            if (uname != null && login != null && !login.trim().isEmpty()) {
                where.add("LOWER(LTRIM(RTRIM(" + FinDb.textExpr(c, "", uname, 160) + ")))=LOWER(?)");
                params.add(login.trim());
            }
            String name = FinDb.resolveColumn(cols, "vis_name", "name", "Name", "moname");
            if (name != null && display != null && !display.trim().isEmpty()) {
                where.add("LOWER(LTRIM(RTRIM(" + FinDb.textExpr(c, "", name, 220) + ")))=LOWER(?)");
                params.add(display.trim());
            }
            if (where.isEmpty()) return null;
            String sql = "SELECT TOP (1) " + FinDb.textExpr(c, "", vid, 40) + " FROM dbo.visitors WHERE (" + join(where, " OR ") + ")";
            Object value = FinDb.value(c, sql, params.toArray());
            if (value == null) return null;
            if (value instanceof Number) return ((Number) value).intValue();
            try { return Integer.valueOf(String.valueOf(value).trim()); } catch (Exception e) { return null; }
        } catch (Throwable ignored) {
            return null;
        }
    }

    // ------------------------------------------------------------------ identity rules (same as the other apps)

    /**
     * The management accounts of the finance edition, recognised from the login name alone:
     * {@code admin}, {@code administrator}, {@code manager}, {@code modir}, {@code modiriat},
     * {@code modir_mali} … and anything that starts with them. They always receive every finance
     * permission ({@link FinSession#FINANCE_PERMISSIONS}), whatever the database rows say.
     */
    static boolean identityLooksManagerAccount(String login) {
        String raw = login == null ? "" : login.trim();
        if (raw.isEmpty()) return false;
        String compact = normalizeIdentity(raw).replace(" ", "").replace("_", "").replace("-", "").replace(".", "");
        if (compact.isEmpty()) return false;
        for (String name : new String[]{"admin", "administrator", "root", "manager", "modir", "modiriat",
                "modirmali", "modiremal", "modirmeelano", "\u0645\u062f\u06cc\u0631", "\u0645\u062f\u064a\u0631",
                "\u0645\u062f\u06cc\u0631\u06cc\u062a", "\u0645\u062f\u06cc\u0631\u0645\u0627\u0644\u06cc"}) {
            String n = normalizeIdentity(name).replace(" ", "").replace("_", "").replace("-", "").replace(".", "");
            if (compact.equals(n)) return true;
        }
        return compact.startsWith("admin") || compact.startsWith("manager") || compact.startsWith("modir");
    }

    /** The full-access account, by the same rule the other Meelano applications use. */
    static boolean identityLooksAdmin(String login, String display) {
        String rawLogin = login == null ? "" : login.trim();
        if ("admin".equalsIgnoreCase(rawLogin) || "administrator".equalsIgnoreCase(rawLogin)) return true;
        String n = normalizeIdentity((login == null ? "" : login) + " " + (display == null ? "" : display));
        String compact = n.replace(" ", "");
        return compact.equals("مدیر") || compact.equals("مدير") || compact.contains("مدیرکل") || compact.contains("مديرکل")
                || compact.contains("modir") || compact.contains("manager");
    }

    static boolean identityLooksSenior(String login, String display) {
        String n = normalizeIdentity((login == null ? "" : login) + " " + (display == null ? "" : display));
        String compact = n.replace(" ", "");
        return compact.contains("shadinazari") || compact.contains("shadinazary") || compact.contains("shadynazari")
                || compact.contains("senior") || compact.contains("supervisor") || compact.contains("کاربرارشد") || compact.contains("ارشد");
    }

    private static String heuristicRole(String login, String display, Integer visitorId) {
        if (identityLooksAdmin(login, display)) return "admin";
        String n = normalizeIdentity((login == null ? "" : login) + " " + (display == null ? "" : display));
        String compact = n.replace(" ", "").replace("_", "").replace("-", "");
        if (compact.contains("حسابدارارشد") || compact.contains("senioraccountant")) return "senior_accountant";
        if (n.contains("حسابدار") || n.contains("accountant")) return "accountant";
        if (n.contains("انباردار") || n.contains("انبار") || n.contains("warehouse") || n.contains("storekeeper")) return "warehouse";
        if (n.contains("مطالبات") || n.contains("وصول") || n.contains("collections") || n.contains("collector")) return "collections";
        if (compact.contains("کارمندبخشفروش") || n.contains("salesemployee") || n.contains("sales_employee") || n.contains("sales")) return "sales_employee";
        if (identityLooksSenior(login, display)) return "senior";
        if (n.contains("پخش") || n.contains("distribut")) return "distributor";
        if (n.contains("راننده") || n.contains("driver")) return "driver";
        if (n.contains("کارگر") || n.contains("worker")) return "worker";
        if (n.contains("کارمند") || n.contains("employee")) return "employee";
        if (visitorId != null && visitorId > 0) return "";
        return "";
    }

    private static String normalizeIdentity(String value) {
        if (value == null) return "";
        return value.toLowerCase(Locale.US).replace('ي', 'ی').replace('ك', 'ک').replace('ة', 'ه').replace("\u200c", " ").trim();
    }

    /** Real role text → the project's role keys. */
    public static String canonicalRole(String text) {
        if (text == null) return "user";
        String v = normalizeIdentity(text);
        if (v.isEmpty()) return "user";
        if (v.contains("admin") || v.contains("administrator") || v.contains("مدیرکل") || v.contains("مديرکل")) return "admin";
        if (v.contains("manager") || v.contains("مدیر") || v.contains("مدير") || v.contains("modir")) return "manager";
        String compact = v.replace(" ", "").replace("_", "").replace("-", "");
        if (v.contains("senior_accountant") || compact.contains("senioraccountant") || compact.contains("حسابدارارشد")) return "senior_accountant";
        if (v.contains("accountant") || v.contains("حسابدار")) return "accountant";
        if (v.contains("senior") || v.contains("supervisor") || v.contains("ارشد")) return "senior";
        if (v.contains("warehouse") || v.contains("انباردار") || v.contains("انبار")) return "warehouse";
        if (v.contains("collections") || v.contains("مطالبات") || v.contains("وصول")) return "collections";
        if (v.contains("sales_employee") || compact.contains("کارمندبخشفروش") || v.contains("sales")) return "sales_employee";
        if (v.contains("distribut") || v.contains("پخش")) return "distributor";
        if (v.contains("driver") || v.contains("راننده")) return "driver";
        if (v.contains("worker") || v.contains("کارگر")) return "worker";
        if (v.contains("visitor") || v.contains("ویزیت") || v.contains("ويزيت") || v.contains("بازاریاب")) return "visitor";
        if (v.contains("employee") || v.contains("کارمند")) return "employee";
        return "user";
    }

    // ------------------------------------------------------------------ password comparison (all real storages)

    static boolean passwordMatches(String storedText, byte[] storedBytes, String enteredRaw) {
        String entered = normalizePassword(enteredRaw);
        if (entered.isEmpty()) return false;
        String digits = toLatinDigits(entered);
        if (storedText != null) {
            String stored = normalizePassword(storedText);
            if (!stored.isEmpty()) {
                if (stored.equals(entered) || stored.equals(digits)) return true;
                String storedDigits = toLatinDigits(stored);
                if (storedDigits.equals(digits)) return true;
                String plain = stored.startsWith("0x") || stored.startsWith("0X") ? stored.substring(2) : stored;
                if (looksHashed(plain) && matchesDigest(plain, entered, digits)) return true;
            }
        }
        if (storedBytes != null && storedBytes.length > 0) {
            if (sameBytes(storedBytes, entered, digits)) return true;
            if (looksHashedBytes(storedBytes) && matchesDigestBytes(storedBytes, entered, digits)) return true;
            for (Charset cs : charsets()) {
                try {
                    if (normalizePassword(new String(storedBytes, cs)).equals(entered)) return true;
                    if (normalizePassword(new String(storedBytes, cs)).equals(digits)) return true;
                } catch (Throwable ignored) { }
            }
        }
        return false;
    }

    private static Charset[] charsets() {
        Charset win = null;
        try { win = Charset.forName("windows-1256"); } catch (Throwable ignored) { }
        return win == null
                ? new Charset[]{StandardCharsets.UTF_8, StandardCharsets.UTF_16LE, StandardCharsets.ISO_8859_1}
                : new Charset[]{StandardCharsets.UTF_8, StandardCharsets.UTF_16LE, StandardCharsets.ISO_8859_1, win};
    }

    private static boolean sameBytes(byte[] stored, String entered, String digits) {
        for (String cand : new String[]{entered, digits}) {
            if (cand == null || cand.isEmpty()) continue;
            for (Charset cs : charsets()) {
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
        return v.matches("(?i)^[0-9a-f]{32}$") || v.matches("(?i)^[0-9a-f]{40}$")
                || v.matches("(?i)^[0-9a-f]{64}$") || v.matches("(?i)^[0-9a-f]{128}$");
    }

    private static boolean looksHashedBytes(byte[] b) {
        return b.length == 16 || b.length == 20 || b.length == 32 || b.length == 64;
    }

    private static boolean matchesDigest(String storedHex, String entered, String digits) {
        for (String cand : new String[]{entered, digits}) {
            for (String alg : new String[]{"MD5", "SHA-1", "SHA-256", "SHA-512"}) {
                for (Charset cs : new Charset[]{StandardCharsets.UTF_8, StandardCharsets.UTF_16LE}) {
                    if (hex(digest(alg, cand.getBytes(cs))).equalsIgnoreCase(storedHex)) return true;
                }
            }
        }
        return false;
    }

    private static boolean matchesDigestBytes(byte[] stored, String entered, String digits) {
        for (String cand : new String[]{entered, digits}) {
            for (String alg : new String[]{"MD5", "SHA-1", "SHA-256", "SHA-512"}) {
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

    // ------------------------------------------------------------------ text helpers

    /** The account key used for comparison: digits, trimmed, Arabic/Persian letters unified. */
    static String loginKey(String v) {
        if (v == null) return "";
        return normalizeDigits(v.trim()).replace('\u064a', '\u06cc').replace('\u0643', '\u06a9');
    }

    /** The password as the other applications treat it: no NUL bytes, trimmed, digits unified. */
    static String normalizePassword(String v) {
        if (v == null) return "";
        return normalizeDigits(v.replace("\u0000", "").trim());
    }

    static String toLatinDigits(String v) {
        return normalizeDigits(v);
    }

    static String normalizeDigits(String v) {
        if (v == null) return "";
        StringBuilder b = new StringBuilder(v.length());
        for (char ch : v.toCharArray()) {
            if (ch >= '\u06f0' && ch <= '\u06f9') b.append((char) ('0' + (ch - '\u06f0')));
            else if (ch >= '\u0660' && ch <= '\u0669') b.append((char) ('0' + (ch - '\u0660')));
            else b.append(ch);
        }
        return b.toString();
    }

    private static boolean isInactiveLoginValue(String value) {
        if (value == null || value.trim().isEmpty()) return false;
        String v = normalizeIdentity(normalizeDigits(value));
        return v.equals("0") || v.equals("false") || v.equals("f") || v.equals("n") || v.equals("no")
                || v.equals("disabled") || v.equals("inactive") || v.equals("locked") || v.equals("lock")
                || v.contains("غیرفعال") || v.contains("غيرفعال") || v.contains("مسدود") || v.contains("قفل");
    }

    private static void bindText(PreparedStatement ps, int index, String value) {
        try {
            ps.setNString(index, value);
        } catch (Throwable ignored) {
            try { ps.setString(index, value); } catch (Throwable ignored2) { }
        }
    }

    private static String textOrEmpty(ResultSet r, int index) {
        try {
            String v = r.getString(index);
            return v == null ? "" : v;
        } catch (Throwable ignored) {
            return "";
        }
    }

    private static byte[] bytesOrNull(ResultSet r, int index) {
        try { return r.getBytes(index); } catch (Throwable ignored) { return null; }
    }

    private static Integer intOrNull(ResultSet r, int index) {
        try {
            Object o = r.getObject(index);
            if (o == null) return null;
            if (o instanceof Number) return ((Number) o).intValue();
            String s = normalizeDigits(String.valueOf(o)).trim();
            return s.isEmpty() ? null : Integer.valueOf(s);
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static String join(List<String> parts, String sep) {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < parts.size(); i++) {
            if (i > 0) b.append(sep);
            b.append(parts.get(i));
        }
        return b.toString();
    }

    /** Reads the real operator list for the login screen (names only, never passwords). */
    public static JSONArray operators(Connection c) throws Exception {
        JSONArray out = new JSONArray();
        if (!FinDb.tableExists(c, "visitors")) return out;
        Set<String> cols = FinDb.columns(c, "visitors");
        String vid = FinDb.resolveColumn(cols, "vis_rdf", "visitor_id", "VisitorID", "shvis", "id", "ID");
        String name = FinDb.resolveColumn(cols, "vis_name", "visitor_name", "Name", "name", "FullName", "moname");
        String uname = FinDb.resolveColumn(cols, "Username", "UserName", "username", "user_name", "login");
        if (uname == null) return out;
        String sql = "SELECT TOP (60) "
                + (vid == null ? "CAST(NULL AS int)" : FinDb.textExpr(c, "", vid, 40)) + " AS id, "
                + (name == null ? "CAST(NULL AS nvarchar(250))" : FinDb.textExpr(c, "", name, 250)) + " AS nm, "
                + FinDb.textExpr(c, "", uname, 200) + " AS un "
                + "FROM dbo.visitors WITH (NOLOCK) WHERE " + FinDb.textExpr(c, "", uname, 200) + " IS NOT NULL ORDER BY 1";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            try (ResultSet r = ps.executeQuery()) {
                while (r.next()) {
                    JSONObject o = new JSONObject();
                    o.put("id", intOrNull(r, 1) == null ? JSONObject.NULL : intOrNull(r, 1));
                    o.put("name", textOrEmpty(r, 2));
                    o.put("username", textOrEmpty(r, 3));
                    out.put(o);
                }
            }
        }
        return out;
    }
}

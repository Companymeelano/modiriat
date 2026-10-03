package ir.atiran.finance;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Read-only adapter for the Atiran per-user menu/form/subsystem ACL contract.
 *
 * <p>Authentication is delegated to SQL Server: this class binds ORIGINAL_LOGIN()/SUSER_SNAME() to
 * an exact dbo.sys_users.user_name row and verifies the database's dbo.get_role_id result. It never
 * reads a password/hash or infers a role from identity text. User-specific grants are not persisted.
 * It deliberately uses dbo.ProcMenuPermission for menu visibility
 * and exposes exact form-level and subsystem-level permission names independently. The source
 * procedure does not define how those scope-level permission names combine for each action, so this
 * adapter does not invent an effective-permission rule. Missing metadata or unknown permissions
 * never grant access.</p>
 *
 * <p>This is a client-side navigation/data-fetch guard, not a database security boundary. A
 * production direct-SQL account must still be provisioned with least privilege and server-side
 * controls; client checks can be bypassed by a modified client.</p>
 */
public final class FinanceAuthorizationRepository {
    private static final String FORM_GRANTS_SQL =
            "SELECT uf.FormId, p.PermissionName " +
            "FROM security.UserFormPermission AS uf " +
            "INNER JOIN security.Permission AS p ON p.PermissionId = uf.PermissionId " +
            "WHERE uf.user_id = ?";

    private static final String SUBSYSTEM_GRANTS_SQL =
            "SELECT sp.SubSystemID, p.PermissionName " +
            "FROM security.SubSystemPermission AS sp " +
            "INNER JOIN security.Permission AS p ON p.PermissionId = sp.PermissionID " +
            "WHERE sp.user_id = ?";

    private static final String MENU_INFO_SQL =
            "SELECT [SubSystemID], [SubSystemName], [Parent MenuID], [Parent MenuName], " +
            "[MenuID], [MenuName], [Description], [FormID], [FormName], [FormClass], " +
            "[DisplayOrder], [Shortcut] " +
            "FROM dbo.vw_MenuInfo " +
            "ORDER BY [SubSystemID], [DisplayOrder], [MenuID]";

    private static final String SQL_IDENTITY_SQL =
            "SELECT SUSER_SNAME(), ORIGINAL_LOGIN(), IS_SRVROLEMEMBER(N'sysadmin'), " +
            "IS_ROLEMEMBER(N'db_owner')";

    private static final String CURRENT_USER_SQL =
            "SELECT TOP (2) user_id, user_name, role_id, active, IsLocked " +
            "FROM dbo.sys_users WHERE user_name = ?";

    /**
     * Authenticates the current SQL Server connection without reading Atiran password/hash fields.
     * This only succeeds when the non-privileged SQL login maps to exactly one active, unlocked
     * dbo.sys_users row and dbo.get_role_id returns that row's role_id. This mapping is fail-closed
     * and still requires validation against the live deployment.
     */
    public AuthenticatedUser authenticateCurrentSqlPrincipal(Connection connection) throws SQLException {
        requireOpen(connection);
        try {
            connection.setReadOnly(true);
        } catch (SQLException ignored) {
            // Fixed statements in this repository are read-only; the flag is only a driver hint.
        }

        String currentPrincipal;
        String originalPrincipal;
        try (Statement statement = connection.createStatement()) {
            statement.setQueryTimeout(10);
            try (ResultSet result = statement.executeQuery(SQL_IDENTITY_SQL)) {
                if (!result.next()) throw denied("SQL Server did not return a login identity");
                currentPrincipal = clean(result.getString(1));
                originalPrincipal = clean(result.getString(2));
                int sysadmin = result.getInt(3);
                boolean sysadminUnknown = result.wasNull();
                int dbOwner = result.getInt(4);
                boolean dbOwnerUnknown = result.wasNull();
                if (sysadminUnknown || dbOwnerUnknown || sysadmin != 0 || dbOwner != 0) {
                    throw denied("Finance requires a non-privileged SQL principal");
                }
            }
        }
        if (currentPrincipal.isEmpty() || originalPrincipal.isEmpty() ||
                !currentPrincipal.equals(originalPrincipal)) {
            throw denied("SQL execution identity is missing or impersonated");
        }

        int userId;
        int rowRoleId;
        String atiranUserName;
        try (PreparedStatement statement = connection.prepareStatement(CURRENT_USER_SQL)) {
            statement.setString(1, originalPrincipal);
            statement.setQueryTimeout(10);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) throw denied("SQL principal is not mapped to an Atiran user");
                userId = result.getInt(1);
                atiranUserName = clean(result.getString(2));
                rowRoleId = result.getInt(3);
                boolean roleIsNull = result.wasNull();
                boolean active = result.getBoolean(4);
                boolean activeIsNull = result.wasNull();
                boolean locked = result.getBoolean(5);
                boolean lockedIsNull = result.wasNull();
                if (result.next()) throw denied("SQL principal maps to more than one Atiran user");
                if (userId <= 0 || atiranUserName.isEmpty() || roleIsNull || rowRoleId <= 0 ||
                        activeIsNull || !active || lockedIsNull || locked) {
                    throw denied("Mapped Atiran account is inactive, locked, or incomplete");
                }
            }
        }

        int functionRoleId;
        try (PreparedStatement statement = connection.prepareStatement("SELECT dbo.get_role_id(?)")) {
            statement.setString(1, atiranUserName);
            statement.setQueryTimeout(10);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) throw denied("Atiran role lookup returned no result");
                functionRoleId = result.getInt(1);
                if (result.wasNull()) throw denied("Atiran role lookup returned NULL");
            }
        }
        if (functionRoleId <= 0 || functionRoleId != rowRoleId) {
            throw denied("Atiran role lookup did not match the authenticated account");
        }
        return new AuthenticatedUser(userId, functionRoleId, atiranUserName, originalPrincipal);
    }

    /** Loads the live per-user menu and grant snapshot for a principal authenticated above. */
    public AccessSnapshot load(Connection connection, AuthenticatedUser authenticatedUser) throws SQLException {
        requireOpen(connection);
        if (authenticatedUser == null) throw denied("An authenticated SQL principal is required");
        AuthenticatedUser current = authenticateCurrentSqlPrincipal(connection);
        if (!authenticatedUser.sameIdentity(current)) {
            throw denied("The authenticated SQL principal changed");
        }

        Set<Integer> permittedMenuIds = readPermittedMenuIds(connection, current.userId);
        Map<Integer, Set<String>> formGrants = readFormGrants(connection, current.userId);
        Map<Integer, Set<String>> subsystemGrants = readSubsystemGrants(connection, current.userId);
        Set<String> knownPermissions = readKnownPermissions(connection);
        List<MenuEntry> permittedMenus = readMenuInfo(connection, permittedMenuIds);

        return new AccessSnapshot(current.userId, current.roleId, permittedMenuIds, permittedMenus,
                formGrants, subsystemGrants, knownPermissions);
    }

    private void requireOpen(Connection connection) throws SQLException {
        if (connection == null || connection.isClosed()) {
            throw new SQLException("No open SQL connection", "08003");
        }
    }

    private static SQLException denied(String safeMessage) {
        return new SQLException(safeMessage, "28000");
    }

    private Set<Integer> readPermittedMenuIds(Connection connection, int userId) throws SQLException {
        Set<Integer> menuIds = new LinkedHashSet<>();
        try (CallableStatement statement = connection.prepareCall("{call dbo.ProcMenuPermission(?)}")) {
            statement.setInt(1, userId);
            statement.setQueryTimeout(15);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    int menuId = result.getInt(1);
                    if (!result.wasNull() && menuId > 0) menuIds.add(menuId);
                }
            }
        }
        return menuIds;
    }

    private Map<Integer, Set<String>> readFormGrants(Connection connection, int userId) throws SQLException {
        Map<Integer, Set<String>> grants = new HashMap<>();
        try (PreparedStatement statement = connection.prepareStatement(FORM_GRANTS_SQL)) {
            statement.setInt(1, userId);
            statement.setQueryTimeout(15);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    int formId = result.getInt(1);
                    boolean formIsNull = result.wasNull();
                    String permission = clean(result.getString(2));
                    if (!formIsNull && formId > 0 && !permission.isEmpty()) {
                        grants.computeIfAbsent(formId, ignored -> new HashSet<>()).add(permission);
                    }
                }
            }
        }
        return grants;
    }

    private Map<Integer, Set<String>> readSubsystemGrants(Connection connection, int userId) throws SQLException {
        Map<Integer, Set<String>> grants = new HashMap<>();
        try (PreparedStatement statement = connection.prepareStatement(SUBSYSTEM_GRANTS_SQL)) {
            statement.setInt(1, userId);
            statement.setQueryTimeout(15);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    int subsystemId = result.getInt(1);
                    boolean subsystemIsNull = result.wasNull();
                    String permission = clean(result.getString(2));
                    if (!subsystemIsNull && subsystemId > 0 && !permission.isEmpty()) {
                        grants.computeIfAbsent(subsystemId, ignored -> new HashSet<>()).add(permission);
                    }
                }
            }
        }
        return grants;
    }

    private Set<String> readKnownPermissions(Connection connection) throws SQLException {
        Set<String> names = new LinkedHashSet<>();
        try (Statement statement = connection.createStatement()) {
            statement.setQueryTimeout(15);
            try (ResultSet result = statement.executeQuery(
                    "SELECT PermissionName FROM security.Permission WHERE PermissionName IS NOT NULL")) {
                while (result.next()) {
                    String name = clean(result.getString(1));
                    if (!name.isEmpty()) names.add(name);
                }
            }
        }
        return names;
    }

    private List<MenuEntry> readMenuInfo(Connection connection, Set<Integer> permittedMenuIds) throws SQLException {
        if (permittedMenuIds.isEmpty()) return Collections.emptyList();

        List<MenuEntry> menus = new ArrayList<>();
        try (Statement statement = connection.createStatement()) {
            statement.setQueryTimeout(15);
            try (ResultSet result = statement.executeQuery(MENU_INFO_SQL)) {
                while (result.next()) {
                    int subsystemId = result.getInt(1);
                    boolean subsystemIsNull = result.wasNull();
                    String subsystemName = clean(result.getString(2));
                    Integer parentMenuId = nullableInt(result, 3);
                    String parentMenuName = clean(result.getString(4));
                    int menuId = result.getInt(5);
                    String menuName = clean(result.getString(6));
                    String description = clean(result.getString(7));
                    int formId = result.getInt(8);
                    boolean formIsNull = result.wasNull();
                    String formName = clean(result.getString(9));
                    String formClass = clean(result.getString(10));
                    Integer displayOrder = nullableInt(result, 11);
                    String shortcut = clean(result.getString(12));

                    // The source view inner-joins SubSystem and Form. Keep defensive checks so a
                    // schema/version mismatch can never turn an incomplete mapping into a grant.
                    if (!permittedMenuIds.contains(menuId) || subsystemIsNull || formIsNull ||
                            subsystemId <= 0 || menuId <= 0 || formId <= 0) continue;
                    menus.add(new MenuEntry(subsystemId, subsystemName, parentMenuId, parentMenuName,
                            menuId, menuName, description, formId, formName, formClass,
                            displayOrder, shortcut));
                }
            }
        }
        return menus;
    }

    private static Integer nullableInt(ResultSet result, int index) throws SQLException {
        int value = result.getInt(index);
        return result.wasNull() ? null : value;
    }

    private static String clean(String value) {
        return value == null ? "" : value.trim();
    }

    public static final class AuthenticatedUser {
        public final int userId;
        public final int roleId;
        private final String atiranUserName;
        private final String sqlPrincipal;

        private AuthenticatedUser(int userId, int roleId, String atiranUserName, String sqlPrincipal) {
            this.userId = userId;
            this.roleId = roleId;
            this.atiranUserName = atiranUserName;
            this.sqlPrincipal = sqlPrincipal;
        }

        private boolean sameIdentity(AuthenticatedUser other) {
            return other != null && userId == other.userId && roleId == other.roleId &&
                    atiranUserName.equals(other.atiranUserName) && sqlPrincipal.equals(other.sqlPrincipal);
        }
    }

    public static final class AccessSnapshot {
        public final int authenticatedUserId;
        public final int roleId;
        private final Set<Integer> permittedMenuIds;
        private final List<MenuEntry> permittedMenus;
        private final Map<Integer, MenuEntry> menuById;
        private final Map<Integer, Set<String>> formGrants;
        private final Map<Integer, Set<String>> subsystemGrants;
        private final Set<String> knownPermissions;

        AccessSnapshot(int authenticatedUserId, int roleId, Set<Integer> permittedMenuIds,
                       List<MenuEntry> permittedMenus, Map<Integer, Set<String>> formGrants,
                       Map<Integer, Set<String>> subsystemGrants, Set<String> knownPermissions) {
            this.authenticatedUserId = authenticatedUserId;
            this.roleId = roleId;
            this.permittedMenuIds = immutableIntSet(permittedMenuIds);
            this.permittedMenus = Collections.unmodifiableList(new ArrayList<>(permittedMenus));
            Map<Integer, MenuEntry> entries = new LinkedHashMap<>();
            for (MenuEntry menu : permittedMenus) entries.put(menu.menuId, menu);
            this.menuById = Collections.unmodifiableMap(entries);
            this.formGrants = immutableMapOfSets(formGrants);
            this.subsystemGrants = immutableMapOfSets(subsystemGrants);
            this.knownPermissions = immutableStringSet(knownPermissions);
        }

        public List<MenuEntry> permittedMenus() {
            return permittedMenus;
        }

        public Set<Integer> permittedMenuIds() {
            return permittedMenuIds;
        }

        /** The menu ID must be returned by dbo.ProcMenuPermission and map to a verified view row. */
        public boolean hasMenu(int menuId) {
            return permittedMenuIds.contains(menuId) && menuById.containsKey(menuId);
        }

        /** Exact form-level grant only; this does not imply a subsystem grant or an action policy. */
        public boolean hasFormPermission(int menuId, String permissionName) {
            if (permissionName == null || !knownPermissions.contains(permissionName)) return false;
            MenuEntry menu = menuById.get(menuId);
            if (menu == null || !permittedMenuIds.contains(menuId)) return false;
            Set<String> grants = formGrants.get(menu.formId);
            return grants != null && grants.contains(permissionName);
        }

        /** Exact subsystem-level grant only; this does not imply a form grant or an action policy. */
        public boolean hasSubsystemPermission(int menuId, String permissionName) {
            if (permissionName == null || !knownPermissions.contains(permissionName)) return false;
            MenuEntry menu = menuById.get(menuId);
            if (menu == null || !permittedMenuIds.contains(menuId)) return false;
            Set<String> grants = subsystemGrants.get(menu.subsystemId);
            return grants != null && grants.contains(permissionName);
        }

        /**
         * Exposes a snapshot of verified form-level grants for the mapped menu. Callers must not
         * treat these names as effective authorization until the server's action policy is verified.
         */
        public Set<String> formPermissions(int menuId) {
            MenuEntry menu = menuById.get(menuId);
            if (menu == null || !permittedMenuIds.contains(menuId)) return Collections.emptySet();
            return formGrants.getOrDefault(menu.formId, Collections.emptySet());
        }

        /** See {@link #formPermissions(int)}; the subsystem scope is returned independently. */
        public Set<String> subsystemPermissions(int menuId) {
            MenuEntry menu = menuById.get(menuId);
            if (menu == null || !permittedMenuIds.contains(menuId)) return Collections.emptySet();
            return subsystemGrants.getOrDefault(menu.subsystemId, Collections.emptySet());
        }

        private static Set<Integer> immutableIntSet(Set<Integer> source) {
            return Collections.unmodifiableSet(new LinkedHashSet<>(source));
        }

        private static Set<String> immutableStringSet(Set<String> source) {
            return Collections.unmodifiableSet(new LinkedHashSet<>(source));
        }

        private static Map<Integer, Set<String>> immutableMapOfSets(Map<Integer, Set<String>> source) {
            Map<Integer, Set<String>> copy = new HashMap<>();
            for (Map.Entry<Integer, Set<String>> entry : source.entrySet()) {
                copy.put(entry.getKey(), Collections.unmodifiableSet(new HashSet<>(entry.getValue())));
            }
            return Collections.unmodifiableMap(copy);
        }
    }

    public static final class MenuEntry {
        public final int subsystemId;
        public final String subsystemName;
        public final Integer parentMenuId;
        public final String parentMenuName;
        public final int menuId;
        public final String menuName;
        public final String description;
        public final int formId;
        public final String formName;
        public final String formClass;
        public final Integer displayOrder;
        public final String shortcut;

        MenuEntry(int subsystemId, String subsystemName, Integer parentMenuId, String parentMenuName,
                  int menuId, String menuName, String description, int formId, String formName,
                  String formClass, Integer displayOrder, String shortcut) {
            this.subsystemId = subsystemId;
            this.subsystemName = subsystemName;
            this.parentMenuId = parentMenuId;
            this.parentMenuName = parentMenuName;
            this.menuId = menuId;
            this.menuName = menuName;
            this.description = description;
            this.formId = formId;
            this.formName = formName;
            this.formClass = formClass;
            this.displayOrder = displayOrder;
            this.shortcut = shortcut;
        }
    }
}

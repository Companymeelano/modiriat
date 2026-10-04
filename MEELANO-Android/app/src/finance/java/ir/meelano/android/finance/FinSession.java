package ir.meelano.android.finance;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.provider.Settings;

import org.json.JSONObject;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * The signed-in finance operator: real Atiran identity (visitors / sys_users row) plus the real
 * access-control role from the database (meelano_access_users → meelano_access_roles).
 *
 * Permissions are the ones the project already stores as a CSV in those tables, extended with the
 * finance permission keys defined in {@link #FINANCE_PERMISSIONS}. A username is never used as a
 * substitute for a real role: when no role row exists the operator gets the read-only default and
 * the UI says so.
 */
public final class FinSession {
    private static volatile FinSession current;

    public final String username;
    public final String displayName;
    public final String roleKey;
    public final String roleLabel;
    public final Integer visitorId;
    public final Integer atiranUserId;
    public final String source;           // visitors | sys_users
    public final Set<String> permissions; // effective keys
    public final boolean roleFromDatabase;
    public final long loginAt;

    /** Finance permission keys (view / create / confirm / approve / reconcile / export / print / adjust). */
    public static final String[] FINANCE_PERMISSIONS = {
            "finance_dashboard", "finance_sales", "finance_banks", "finance_receivables", "finance_checks",
            "finance_pos", "finance_cash", "finance_settlement", "finance_reconcile", "finance_reports",
            "finance_audit", "finance_settings",
            "finance_create", "finance_confirm", "finance_approve", "finance_adjust", "finance_export",
            "finance_print", "finance_daily_close"
    };

    private FinSession(String username, String displayName, String roleKey, String roleLabel, Integer visitorId,
                       Integer atiranUserId, String source, Set<String> permissions, boolean roleFromDatabase) {
        this.username = username;
        this.displayName = displayName;
        this.roleKey = roleKey;
        this.roleLabel = roleLabel;
        this.visitorId = visitorId;
        this.atiranUserId = atiranUserId;
        this.source = source;
        this.permissions = permissions == null ? new HashSet<>() : permissions;
        this.roleFromDatabase = roleFromDatabase;
        this.loginAt = System.currentTimeMillis();
    }

    public static FinSession create(String username, String displayName, String roleKey, String roleLabel,
                                    Integer visitorId, Integer atiranUserId, String source,
                                    Set<String> permissions, boolean roleFromDatabase) {
        current = new FinSession(username, displayName, roleKey, roleLabel, visitorId, atiranUserId, source,
                permissions, roleFromDatabase);
        return current;
    }

    public static FinSession get() { return current; }
    public static void clear() { current = null; }
    public static boolean isLoggedIn() { return current != null; }
    public static String username() { return current == null ? "" : current.username; }
    public static String roleKey() { return current == null ? "" : current.roleKey; }
    public static String displayOrUser() {
        if (current == null) return "";
        return current.displayName == null || current.displayName.trim().isEmpty() ? current.username : current.displayName;
    }

    public boolean can(String key) {
        if (key == null || key.isEmpty()) return true;
        if (isManager()) {
            // The manager role is the real full-access role of the project (admin/manager).
            return true;
        }
        if (permissions.contains(key)) return true;
        // Read permissions follow the module permission, so a role that may see checks may open the
        // check centre without a second row.
        if ("finance_checks".equals(key)) return permissions.contains("checks") || permissions.contains("dashboard");
        return false;
    }

    public boolean isManager() { return "admin".equals(roleKey) || "manager".equals(roleKey); }

    /** Sensitive actions require an explicit permission — never the role name alone. */
    public boolean canWrite() { return isManager() || permissions.contains("finance_create"); }
    public boolean canConfirm() { return isManager() || permissions.contains("finance_confirm"); }
    public boolean canApprove() { return isManager() || permissions.contains("finance_approve"); }
    public boolean canReconcile() { return isManager() || permissions.contains("finance_reconcile"); }
    public boolean canAdjust() { return isManager() || permissions.contains("finance_adjust"); }
    public boolean canExport() { return isManager() || permissions.contains("finance_export"); }

    public static String appVersion() {
        try {
            Context c = FinApp.context();
            return c == null ? "?" : c.getPackageManager().getPackageInfo(c.getPackageName(), 0).versionName;
        } catch (Exception e) {
            return "?";
        }
    }

    public static String deviceLabel() {
        return Build.MANUFACTURER + " " + Build.MODEL;
    }

    public static String deviceKey(Context ctx) {
        try {
            String id = Settings.Secure.getString(ctx.getContentResolver(), Settings.Secure.ANDROID_ID);
            return id == null ? "device" : id.substring(0, Math.min(12, id.length()));
        } catch (Exception e) {
            return "device";
        }
    }

    // ------------------------------------------------------------------ persistence of the last user

    public void remember(Context ctx) {
        SharedPreferences p = ctx.getSharedPreferences("atiran_finance", Context.MODE_PRIVATE);
        p.edit()
                .putString("last_user", username)
                .putString("last_name", displayName)
                .putString("last_role", roleKey)
                .putString("last_role_label", roleLabel)
                .putString("last_source", source)
                .putLong("last_login", loginAt)
                .apply();
    }

    public static JSONObject lastUser(Context ctx) {
        SharedPreferences p = ctx.getSharedPreferences("atiran_finance", Context.MODE_PRIVATE);
        JSONObject o = new JSONObject();
        try {
            o.put("username", p.getString("last_user", ""));
            o.put("name", p.getString("last_name", ""));
            o.put("role", p.getString("last_role", ""));
            o.put("roleLabel", p.getString("last_role_label", ""));
            o.put("source", p.getString("last_source", ""));
            o.put("at", p.getLong("last_login", 0));
        } catch (Exception ignored) { }
        return o;
    }

    public static String roleLabelFor(String roleKey) {
        if (roleKey == null) return "کاربر محدود";
        switch (roleKey) {
            case "admin": return "مدیر اصلی";
            case "manager": return "مدیر";
            case "senior": return "کاربر ارشد";
            case "senior_accountant": return "حسابدار ارشد";
            case "accountant": return "حسابدار";
            case "collections": return "مامور مطالبات";
            case "warehouse": return "انباردار";
            case "sales_employee": return "کارمند فروش";
            case "visitor": return "ویزیتور";
            case "distributor": return "مامور پخش";
            case "driver": return "راننده";
            case "worker": return "کارگر";
            case "employee": return "کارمند";
            case "finance_operator": return "اپراتور مالی";
            default: return "کاربر محدود";
        }
    }

    /** Default finance permissions per real role; the database row still wins when present. */
    public static Set<String> defaultPermissions(String roleKey) {
        Set<String> s = new HashSet<>();
        String r = roleKey == null ? "" : roleKey;
        boolean manager = "admin".equals(r) || "manager".equals(r);
        boolean accountant = "accountant".equals(r) || "senior_accountant".equals(r);
        boolean collections = "collections".equals(r);
        boolean financeOperator = "finance_operator".equals(r);
        boolean operator = "visitor".equals(r) || "distributor".equals(r) || "driver".equals(r)
                || "worker".equals(r) || "employee".equals(r) || "warehouse".equals(r) || "sales_employee".equals(r);
        if (manager || financeOperator) {
            // The finance edition is the management tool itself: an account that the database does not
            // restrict works with every finance module. Anything the database *does* say (a row in
            // meelano_access_users / a role column / Atiran's role table) still wins, because it is
            // resolved before this default is used.
            s.addAll(Arrays.asList(FINANCE_PERMISSIONS));
        } else if (operator) {
            // Atiran's own operators (the visitors table) work with every finance module as well; the
            // write actions that touch accounting documents stay with the accounting roles above.
            s.addAll(Arrays.asList("finance_dashboard", "finance_sales", "finance_banks", "finance_receivables",
                    "finance_checks", "finance_pos", "finance_cash", "finance_reports", "finance_export",
                    "finance_print", "finance_create"));
        } else if (accountant) {
            s.addAll(Arrays.asList("finance_dashboard", "finance_sales", "finance_banks", "finance_receivables",
                    "finance_checks", "finance_pos", "finance_cash", "finance_settlement", "finance_reports",
                    "finance_reconcile", "finance_create", "finance_confirm", "finance_export", "finance_print",
                    "finance_daily_close", "finance_audit"));
        } else if (collections) {
            s.addAll(Arrays.asList("finance_dashboard", "finance_sales", "finance_banks", "finance_receivables",
                    "finance_checks", "finance_pos", "finance_cash", "finance_reports",
                    "finance_create", "finance_export", "finance_print"));
        } else {
            s.addAll(Arrays.asList("finance_dashboard"));
        }
        return s;
    }

    public static Set<String> permissionSet(String csv) {
        Set<String> out = new HashSet<>();
        if (csv == null) return out;
        for (String part : csv.split(",")) {
            String v = part.trim().toLowerCase(Locale.US);
            if (!v.isEmpty()) out.add(v);
        }
        return Collections.unmodifiableSet(out);
    }
}

package ir.atiran.finance;

import android.app.Activity;
import android.content.pm.ApplicationInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Persian-first, read-only Finance shell. The first direct-SQL step is deliberately limited to
 * catalog metadata in debuggable test builds. No Finance screens query business records yet.
 */
public final class FinanceActivity extends Activity {
    private static final int NAVY = Color.rgb(13, 20, 30);
    private static final int SURFACE = Color.rgb(24, 35, 49);
    private static final int SURFACE_RAISED = Color.rgb(32, 46, 62);
    private static final int GOLD = Color.rgb(214, 179, 106);
    private static final int TEXT = Color.rgb(244, 241, 233);
    private static final int MUTED = Color.rgb(176, 186, 199);
    private static final int BORDER = Color.rgb(58, 72, 88);
    private static final int WARNING = Color.rgb(232, 174, 90);
    private static final int ERROR = Color.rgb(240, 115, 115);
    private static final int SUCCESS = Color.rgb(103, 198, 154);

    private final ExecutorService dbExecutor = Executors.newSingleThreadExecutor();
    private LinearLayout pageContent;
    private LinearLayout navBar;
    private TextView headerStatus;
    private Typeface vazirRegular = Typeface.DEFAULT;
    private Typeface vazirBold = Typeface.DEFAULT_BOLD;
    private String currentPage = "home";
    private String selectedModule = "";
    private String schemaQuery = "";
    private String notice = "";
    private long requestSerial = 0;
    private FinanceSchemaInspector.Snapshot schemaSnapshot;
    private FinanceAuthorizationRepository.AccessSnapshot accessSnapshot;
    private boolean accessCheckPending;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        getWindow().setStatusBarColor(NAVY);
        getWindow().setNavigationBarColor(NAVY);
        loadFonts();
        buildShell();
        renderPage();
    }

    @Override
    protected void onDestroy() {
        requestSerial++;
        accessSnapshot = null;
        dbExecutor.shutdownNow();
        super.onDestroy();
    }

    private void loadFonts() {
        try { vazirRegular = Typeface.createFromAsset(getAssets(), "fonts/Vazirmatn-Regular.ttf"); }
        catch (Exception ignored) { vazirRegular = Typeface.DEFAULT; }
        try { vazirBold = Typeface.createFromAsset(getAssets(), "fonts/Vazirmatn-Bold.ttf"); }
        catch (Exception ignored) { vazirBold = Typeface.DEFAULT_BOLD; }
    }

    private void buildShell() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(NAVY);
        setRtl(root);

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(dp(18), dp(10), dp(18), dp(10));
        header.setBackgroundColor(NAVY);
        setRtl(header);

        TextView mark = label("م", 22, GOLD, true);
        mark.setGravity(Gravity.CENTER);
        mark.setBackground(background(SURFACE_RAISED, 18, GOLD));
        header.addView(mark, new LinearLayout.LayoutParams(dp(48), dp(48)));

        LinearLayout titleBox = new LinearLayout(this);
        titleBox.setOrientation(LinearLayout.VERTICAL);
        titleBox.setGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT);
        titleBox.setPadding(dp(12), 0, dp(6), 0);
        setRtl(titleBox);
        titleBox.addView(label("آتیران مالی", 19, TEXT, true), new LinearLayout.LayoutParams(-1, -2));
        TextView subtitle = label("بررسی Metadata • اتصال مستقیم فقط در نسخهٔ تست", 10.5f, MUTED, false);
        subtitle.setMaxLines(2);
        titleBox.addView(subtitle, new LinearLayout.LayoutParams(-1, -2));
        header.addView(titleBox, new LinearLayout.LayoutParams(0, -2, 1f));

        headerStatus = label("TEST", 9, WARNING, true);
        headerStatus.setGravity(Gravity.CENTER);
        headerStatus.setPadding(dp(9), dp(6), dp(9), dp(6));
        headerStatus.setBackground(background(SURFACE_RAISED, 999, BORDER));
        header.addView(headerStatus, new LinearLayout.LayoutParams(-2, -2));

        View divider = new View(this);
        divider.setBackgroundColor(BORDER);
        root.addView(header, new LinearLayout.LayoutParams(-1, dp(72)));
        root.addView(divider, new LinearLayout.LayoutParams(-1, dp(1)));

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(false);
        scroll.setClipToPadding(false);
        scroll.setVerticalScrollBarEnabled(true);
        scroll.setPadding(dp(16), dp(16), dp(16), dp(16));
        pageContent = new LinearLayout(this);
        pageContent.setOrientation(LinearLayout.VERTICAL);
        setRtl(pageContent);
        scroll.addView(pageContent, new ScrollView.LayoutParams(-1, -2));
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1f));

        navBar = new LinearLayout(this);
        navBar.setOrientation(LinearLayout.HORIZONTAL);
        navBar.setGravity(Gravity.CENTER);
        navBar.setPadding(dp(7), dp(6), dp(7), dp(8));
        navBar.setBackgroundColor(SURFACE);
        setRtl(navBar);
        root.addView(navBar, new LinearLayout.LayoutParams(-1, dp(68)));
        setContentView(root);
        renderNavigation();
    }

    private void renderNavigation() {
        if (navBar == null) return;
        navBar.removeAllViews();
        addNavigationItem("home", "خانه", "⌂");
        addNavigationItem("modules", "ماژول‌ها", "▦");
        addNavigationItem("schema", "Schema", "▤");
        addNavigationItem("security", "امنیت", "◈");
    }

    private void addNavigationItem(String route, String title, String glyph) {
        boolean active = isRouteActive(route);
        LinearLayout item = new LinearLayout(this);
        item.setOrientation(LinearLayout.VERTICAL);
        item.setGravity(Gravity.CENTER);
        item.setPadding(dp(4), dp(4), dp(4), dp(3));
        item.setClickable(true);
        item.setFocusable(true);
        item.setOnClickListener(v -> navigate(route));
        TextView icon = label(glyph, 19, active ? GOLD : MUTED, true);
        icon.setGravity(Gravity.CENTER);
        item.addView(icon, new LinearLayout.LayoutParams(-1, dp(27)));
        TextView text = label(title, 9.5f, active ? TEXT : MUTED, active);
        text.setGravity(Gravity.CENTER);
        item.addView(text, new LinearLayout.LayoutParams(-1, dp(20)));
        navBar.addView(item, new LinearLayout.LayoutParams(0, -1, 1f));
    }

    private boolean isRouteActive(String route) {
        if ("home".equals(route)) return "home".equals(currentPage) || "module".equals(currentPage);
        if ("modules".equals(route)) return "modules".equals(currentPage);
        if ("schema".equals(route)) return "schema".equals(currentPage) || "relations".equals(currentPage) || "connect".equals(currentPage) || "connecting".equals(currentPage);
        return "security".equals(route) && "security".equals(currentPage);
    }

    private void navigate(String route) {
        if (!"security".equals(route)) accessSnapshot = null;
        currentPage = route;
        renderPage();
    }

    private void renderPage() {
        if (pageContent == null) return;
        if ("connect".equals(currentPage) || "connecting".equals(currentPage) ||
                ("security".equals(currentPage) && accessSnapshot != null)) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SECURE);
        }
        if (headerStatus != null) {
            String status = accessSnapshot != null ? "ACL" : schemaSnapshot != null ? "SCHEMA" : "TEST";
            headerStatus.setText(status);
            headerStatus.setTextColor(accessSnapshot != null || schemaSnapshot != null ? SUCCESS : WARNING);
        }
        pageContent.removeAllViews();
        switch (currentPage) {
            case "connect": renderConnectionForm(); break;
            case "connecting": renderConnecting(); break;
            case "schema": renderSchema(); break;
            case "relations": renderRelations(); break;
            case "modules": renderModules(); break;
            case "module": renderModuleDetail(); break;
            case "security": renderSecurity(); break;
            case "home":
            default: renderHome(); break;
        }
        renderNavigation();
    }

    private void renderHome() {
        addHeading("مرکز عملیات مالی", "نسخهٔ پایه برای کشف Schema؛ هنوز هیچ مبلغ، وضعیت یا KPI مالی خوانده نمی‌شود.");
        if (!notice.isEmpty()) addNotice(notice, ERROR);

        LinearLayout safety = panel();
        safety.addView(label("حالت فعلی: فقط بررسی کاتالوگ SQL Server", 14, GOLD, true), marginParams(-1, -2, 0, 0, 0, 6));
        safety.addView(label("در مسیر فعلی فقط نام جدول/View، ستون‌ها، کلیدها و رابطه‌های ثبت‌شده در Metadata خوانده می‌شوند. هیچ رکورد کسب‌وکار و هیچ عملیات نوشتنی اجرا نمی‌شود.", 11, MUTED, false));
        pageContent.addView(safety, marginParams(-1, -2, 0, 0, 0, 14));

        if (schemaSnapshot == null) {
            addStatusCard("Schema هنوز تأیید نشده", "ابتدا از حساب SQL موقتِ فقط‌خواندنی در محیط تست استفاده کنید. مقدارهای اتصال داخل برنامه ذخیره نمی‌شوند.", WARNING);
        } else {
            addStatusCard("Metadata دریافت شد", schemaSnapshot.databaseName + " • " + schemaSnapshot.tableCount() + " جدول • " + schemaSnapshot.viewCount() + " View • " + schemaSnapshot.columnCount() + " ستون", SUCCESS);
        }

        addHeading("ماژول‌ها", "همهٔ منابع و محاسبات تا زمان تطبیق Schema واقعی UNKNOWN هستند.");
        for (Module module : modules()) addModuleCard(module);

        TextView discover = action("کشف Schema در محیط تست", true);
        discover.setOnClickListener(v -> { currentPage = "connect"; renderPage(); });
        pageContent.addView(discover, marginParams(-1, dp(50), 0, dp(6), 0, 12));
    }

    private void renderModules() {
        addHeading("ماژول‌های مالی", "برای چند حوزه Menu/Form واقعی در backup شناسایی شده؛ دادهٔ عملیاتی، دسترسی کاربر و محاسبه هنوز تأیید/وصل نشده‌اند.");
        for (Module module : modules()) addModuleCard(module);
    }

    private void addModuleCard(Module module) {
        LinearLayout box = panel();
        box.setClickable(true);
        box.setFocusable(true);
        box.setOnClickListener(v -> { selectedModule = module.key; currentPage = "module"; renderPage(); });
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        setRtl(row);
        TextView icon = label(module.glyph, 19, GOLD, true);
        icon.setGravity(Gravity.CENTER);
        icon.setBackground(background(SURFACE_RAISED, 15, BORDER));
        row.addView(icon, new LinearLayout.LayoutParams(dp(42), dp(42)));
        LinearLayout copy = new LinearLayout(this);
        copy.setOrientation(LinearLayout.VERTICAL);
        copy.setPadding(dp(10), 0, dp(8), 0);
        setRtl(copy);
        copy.addView(label(module.title, 13, TEXT, true), new LinearLayout.LayoutParams(-1, -2));
        copy.addView(label("Menu/Form کاندید در backup؛ منبع مالی و مجوز کاربر: UNKNOWN", 9.5f, MUTED, false), new LinearLayout.LayoutParams(-1, -2));
        row.addView(copy, new LinearLayout.LayoutParams(0, -2, 1f));
        TextView unknown = label("UNKNOWN", 8.5f, WARNING, true);
        unknown.setGravity(Gravity.CENTER);
        unknown.setPadding(dp(7), dp(5), dp(7), dp(5));
        unknown.setBackground(background(alpha(WARNING, 28), 999, alpha(WARNING, 100)));
        row.addView(unknown, new LinearLayout.LayoutParams(-2, -2));
        box.addView(row, new LinearLayout.LayoutParams(-1, -2));
        pageContent.addView(box, marginParams(-1, -2, 0, 0, 0, 9));
    }

    private void renderModuleDetail() {
        Module module = moduleByKey(selectedModule);
        if (module == null) {
            currentPage = "modules";
            renderModules();
            return;
        }
        addHeading(module.title, "منبع داده، معنی ستون‌ها، وضعیت‌ها و روش محاسبه هنوز با Metadata واقعی تأیید نشده‌اند.");
        addStatusCard("UNKNOWN — فقط‌خواندنی/غیرفعال", "هیچ Query عملیاتی برای این ماژول اجرا نمی‌شود و هیچ دادهٔ صفر یا نمایشی تولید نشده است.", WARNING);
        addBodyPanel("گام لازم", "پس از کشف Schema، مالک دیتابیس باید منبع و Relation رسمی این ماژول را تأیید کند. سپس مجوز واقعی کاربر، معنای Status، تاریخ، Amount و فرمول محاسبه جداگانه آزموده و مستند می‌شوند.");
        TextView schema = action("رفتن به کشف Schema", true);
        schema.setOnClickListener(v -> { currentPage = "schema"; renderPage(); });
        pageContent.addView(schema, marginParams(-1, dp(48), 0, dp(8), 0, 8));
        TextView list = action("بازگشت به فهرست ماژول‌ها", false);
        list.setOnClickListener(v -> navigate("modules"));
        pageContent.addView(list, marginParams(-1, dp(46), 0, 0, 0, 10));
    }

    private void renderConnectionForm() {
        addHeading("اتصال مستقیم آزمایشی به SQL Server", "در Debug دو آزمون جدا وجود دارد: Catalog-only و بررسی هویت/ACL همان SQL principal. اتصال Release مسدود است.");
        if (!notice.isEmpty()) addNotice(notice, ERROR);
        addNotice("فقط از SQL principal غیرـsysadmin و غیرـdb_owner با دسترسی محدود استفاده کنید. آزمون هویت فقط ردیف sys_users حساب جاری و ACL همان user_id را می‌خواند؛ password/hash و ردیف مالی خوانده نمی‌شود. گواهی TLS نامعتبر/ناشناخته باعث رد اتصال می‌شود.", WARNING);

        EditText host = input("Host یا IP سرور");
        host.setSaveEnabled(false);
        host.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
        pageContent.addView(host, marginParams(-1, dp(50), 0, dp(5), 0, 8));

        EditText port = input("Port (پیش‌فرض SQL Server: 1433)");
        port.setSaveEnabled(false);
        port.setInputType(InputType.TYPE_CLASS_NUMBER);
        port.setText("1433");
        pageContent.addView(port, marginParams(-1, dp(50), 0, dp(5), 0, 8));

        EditText catalog = input("نام Database");
        catalog.setSaveEnabled(false);
        catalog.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        pageContent.addView(catalog, marginParams(-1, dp(50), 0, dp(5), 0, 8));

        EditText user = input("کاربر SQL (برای ACL: principal آتیران)");
        user.setSaveEnabled(false);
        user.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        pageContent.addView(user, marginParams(-1, dp(50), 0, dp(5), 0, 8));

        EditText password = input("رمز SQL — ذخیره نمی‌شود");
        password.setSaveEnabled(false);
        if (Build.VERSION.SDK_INT >= 26) password.setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS);
        password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        pageContent.addView(password, marginParams(-1, dp(50), 0, dp(5), 0, 8));

        TextView connect = action("اتصال امن و خواندن Metadata", true);
        connect.setOnClickListener(v -> startSchemaInspection(host, port, catalog, user, password));
        pageContent.addView(connect, marginParams(-1, dp(52), 0, dp(10), 0, 8));

        TextView verify = action("بررسی هویت SQL و ACL کاربر جاری", false);
        verify.setOnClickListener(v -> startAccessCheck(host, port, catalog, user, password));
        pageContent.addView(verify, marginParams(-1, dp(50), 0, 0, 0, 12));
        addBodyPanel("حریم اتصال", "نام کاربری و رمز SQL فقط برای همین درخواست در حافظه استفاده می‌شوند؛ پس از تلاش، فیلد رمز پاک می‌شود و credential در SharedPreferences، فایل یا Log ذخیره نمی‌شود. رشته‌های موقت JVM/درایور ممکن است تا آزادشدن حافظه باقی بمانند. این فرم برای Production تأیید نشده است.");
    }

    private void startSchemaInspection(EditText hostField, EditText portField, EditText catalogField,
                                       EditText userField, EditText passwordField) {
        if (!isDebuggableBuild()) {
            notice = "اتصال مستقیم فقط در Build قابل‌اشکال‌زدایی مجاز است.";
            renderPage();
            return;
        }
        final String host = hostField.getText().toString().trim();
        final String port = portField.getText().toString().trim();
        final String catalog = catalogField.getText().toString().trim();
        final String user = userField.getText().toString().trim();
        final char[] passwordChars = passwordField.getText().toString().toCharArray();
        passwordField.setText("");
        if (!validConnectionInput(host, port, catalog, user, passwordChars)) {
            Arrays.fill(passwordChars, '\0');
            notice = "ورودی اتصال معتبر نیست؛ Host، Port، Database و کاربر را بررسی کنید.";
            currentPage = "connect";
            renderPage();
            return;
        }

        notice = "";
        accessSnapshot = null;
        accessCheckPending = false;
        currentPage = "connecting";
        final long thisRequest = ++requestSerial;
        renderPage();
        dbExecutor.execute(() -> {
            FinanceSchemaInspector.Snapshot found = null;
            String safeError = "";
            try (Connection connection = openMetadataConnection(host, port, catalog, user, passwordChars)) {
                found = FinanceSchemaInspector.inspect(connection);
            } catch (Exception error) {
                safeError = safeFailure(error);
            } finally {
                Arrays.fill(passwordChars, '\0');
            }
            final FinanceSchemaInspector.Snapshot result = found;
            final String failure = safeError;
            runOnUiThread(() -> {
                if (isFinishing() || isDestroyed() || thisRequest != requestSerial) return;
                if (result != null) {
                    schemaSnapshot = result;
                    schemaQuery = "";
                    notice = "";
                    currentPage = "schema";
                } else {
                    notice = failure;
                    currentPage = "connect";
                }
                renderPage();
            });
        });
    }

    private void startAccessCheck(EditText hostField, EditText portField, EditText catalogField,
                                  EditText userField, EditText passwordField) {
        if (!isDebuggableBuild()) {
            notice = "بررسی هویت SQL فقط در Build قابل‌اشکال‌زدایی مجاز است.";
            renderPage();
            return;
        }
        final String host = hostField.getText().toString().trim();
        final String port = portField.getText().toString().trim();
        final String catalog = catalogField.getText().toString().trim();
        final String user = userField.getText().toString().trim();
        final char[] passwordChars = passwordField.getText().toString().toCharArray();
        passwordField.setText("");
        if (!validConnectionInput(host, port, catalog, user, passwordChars)) {
            Arrays.fill(passwordChars, '\0');
            notice = "ورودی اتصال معتبر نیست؛ Host، Port، Database و کاربر را بررسی کنید.";
            currentPage = "connect";
            renderPage();
            return;
        }

        notice = "";
        accessSnapshot = null;
        accessCheckPending = true;
        currentPage = "connecting";
        final long thisRequest = ++requestSerial;
        renderPage();
        dbExecutor.execute(() -> {
            FinanceAuthorizationRepository.AccessSnapshot found = null;
            String safeError = "";
            try (Connection connection = openMetadataConnection(host, port, catalog, user, passwordChars)) {
                FinanceAuthorizationRepository repository = new FinanceAuthorizationRepository();
                FinanceAuthorizationRepository.AuthenticatedUser identity =
                        repository.authenticateCurrentSqlPrincipal(connection);
                found = repository.load(connection, identity);
            } catch (Exception error) {
                safeError = safeFailure(error);
            } finally {
                Arrays.fill(passwordChars, '\0');
            }
            final FinanceAuthorizationRepository.AccessSnapshot result = found;
            final String failure = safeError;
            runOnUiThread(() -> {
                if (isFinishing() || isDestroyed() || thisRequest != requestSerial) return;
                accessCheckPending = false;
                if (result != null) {
                    accessSnapshot = result;
                    notice = "";
                    currentPage = "security";
                } else {
                    accessSnapshot = null;
                    notice = failure;
                    currentPage = "connect";
                }
                renderPage();
            });
        });
    }

    private boolean validConnectionInput(String host, String port, String catalog, String user, char[] password) {
        if (host.isEmpty() || catalog.isEmpty() || user.isEmpty() || password == null || password.length == 0) return false;
        if (!host.matches("[A-Za-z0-9._:-]{1,253}")) return false;
        if (!catalog.matches("[A-Za-z0-9_]{1,128}")) return false;
        if (port.isEmpty() || !port.matches("[0-9]{1,5}")) return false;
        try {
            int value = Integer.parseInt(port);
            return value > 0 && value <= 65535;
        } catch (NumberFormatException ignored) {
            return false;
        }
    }

    private Connection openMetadataConnection(String host, String port, String catalog,
                                              String user, char[] passwordChars) throws Exception {
        Class.forName("net.sourceforge.jtds.jdbc.Driver");
        String url = "jdbc:jtds:sqlserver://" + host + ":" + port + "/" + catalog +
                ";loginTimeout=10;socketTimeout=25;appName=AtiranFinance;ssl=authenticate;";
        Properties properties = new Properties();
        properties.setProperty("user", user);
        properties.setProperty("password", new String(passwordChars));
        properties.setProperty("charset", "UTF-8");
        properties.setProperty("sendStringParametersAsUnicode", "true");
        try {
            Connection connection = DriverManager.getConnection(url, properties);
            try { connection.setReadOnly(true); } catch (SQLException ignored) { }
            return connection;
        } finally {
            properties.remove("password");
            properties.clear();
        }
    }

    private String safeFailure(Exception error) {
        String kind = error == null ? "ConnectionError" : error.getClass().getSimpleName();
        String state = error instanceof SQLException ? ((SQLException) error).getSQLState() : null;
        String code = error instanceof SQLException ? String.valueOf(((SQLException) error).getErrorCode()) : "";
        return "اتصال SQL یا بررسی مجاز ناموفق بود: " + kind +
                (state == null || state.trim().isEmpty() ? "" : " • SQLState " + state) +
                (code.isEmpty() || "0".equals(code) ? "" : " • کد " + code) +
                ". متن خام خطا عمداً نمایش/ثبت نمی‌شود تا اطلاعات اتصال افشا نشود.";
    }

    private boolean isDebuggableBuild() {
        return (getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
    }

    private void renderConnecting() {
        if (accessCheckPending) {
            addHeading("در حال بررسی هویت و ACL", "هویت SQL با sys_users تطبیق می‌شود؛ فقط مجوزهای کاربر جاری خوانده می‌شوند.");
        } else {
            addHeading("در حال بررسی کاتالوگ", "فقط اطلاعات ساختاری SQL Server خوانده می‌شوند.");
        }
        LinearLayout box = panel();
        ProgressBar progress = new ProgressBar(this);
        box.addView(progress, new LinearLayout.LayoutParams(dp(44), dp(44)));
        box.addView(label("هیچ Query روی رکوردهای مالی اجرا نمی‌شود…", 12, MUTED, false), marginParams(-1, -2, 0, dp(10), 0, 0));
        pageContent.addView(box, marginParams(-1, -2, 0, 0, 0, 12));
        TextView cancel = action("بازگشت", false);
        cancel.setOnClickListener(v -> {
            requestSerial++;
            accessCheckPending = false;
            accessSnapshot = null;
            currentPage = "connect";
            renderPage();
        });
        pageContent.addView(cancel, marginParams(-1, dp(46), 0, 0, 0, 10));
    }

    private void renderSchema() {
        addHeading("کاتالوگ واقعی پایگاه داده", schemaSnapshot == null
                ? "هنوز Schemaای از SQL Server خوانده نشده است."
                : schemaSnapshot.databaseName + " • SQL Server " + schemaSnapshot.serverProductVersion);
        if (schemaSnapshot == null) {
            addStatusCard("UNKNOWN", "برای ساخت این گزارش باید یک اتصال تست امن برقرار شود.", WARNING);
            TextView connect = action("اتصال و کشف Schema", true);
            connect.setOnClickListener(v -> { currentPage = "connect"; renderPage(); });
            pageContent.addView(connect, marginParams(-1, dp(48), 0, dp(10), 0, 12));
            return;
        }

        LinearLayout counts = panel();
        counts.addView(label("جدول: " + schemaSnapshot.tableCount() + "   •   View: " + schemaSnapshot.viewCount(), 12, TEXT, true));
        counts.addView(label("ستون: " + schemaSnapshot.columnCount() + "   •   FK column pairs: " + schemaSnapshot.foreignKeyColumns.size() + "   •   Index columns: " + schemaSnapshot.indexColumns.size(), 10.2f, MUTED, false), marginParams(-1, -2, 0, dp(5), 0, 0));
        pageContent.addView(counts, marginParams(-1, -2, 0, 0, 0, 10));

        EditText search = input("جستجوی دقیق نام Schema / جدول / View / ستون / Type");
        search.setText(schemaQuery);
        search.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        pageContent.addView(search, marginParams(-1, dp(50), 0, 0, 0, 8));

        LinearLayout results = new LinearLayout(this);
        results.setOrientation(LinearLayout.VERTICAL);
        setRtl(results);
        pageContent.addView(results, new LinearLayout.LayoutParams(-1, -2));
        renderSchemaObjects(results, schemaQuery);
        search.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                schemaQuery = s == null ? "" : s.toString();
                renderSchemaObjects(results, schemaQuery);
            }
            @Override public void afterTextChanged(Editable s) { }
        });

        TextView relations = action("مشاهدهٔ کلیدها، Relationها و Indexها", false);
        relations.setOnClickListener(v -> { currentPage = "relations"; renderPage(); });
        pageContent.addView(relations, marginParams(-1, dp(48), 0, dp(10), 0, 12));
        addBodyPanel("محدودیت", "وجود نام Table یا Column فقط Metadata است؛ معنای فروش، مانده، وضعیت یا رابطهٔ تجاری را اثبات نمی‌کند. هیچ‌کدام از این ساختارها هنوز به ماژول مالی نگاشت نشده‌اند.");
    }

    private void renderSchemaObjects(LinearLayout results, String rawQuery) {
        results.removeAllViews();
        String query = rawQuery == null ? "" : rawQuery.trim().toLowerCase(Locale.ROOT);
        int matched = 0;
        int shown = 0;
        final int limit = query.isEmpty() ? 40 : 100;
        for (FinanceSchemaInspector.DbObject object : schemaSnapshot.objects) {
            if (!matches(object, query)) continue;
            matched++;
            if (shown >= limit) continue;
            shown++;
            LinearLayout item = panel();
            String type = "TABLE".equals(object.kind) ? "TABLE" : "VIEW";
            item.addView(label(object.schema + "." + object.name, 12.3f, GOLD, true));
            item.addView(label(type + " • " + object.columns.size() + " ستون", 9.5f, MUTED, false), marginParams(-1, -2, 0, dp(3), 0, 4));
            if (object.columns.isEmpty()) {
                item.addView(label("ستونی توسط Metadata قابل مشاهده نبود.", 10, WARNING, false));
            } else {
                StringBuilder columns = new StringBuilder();
                for (int i = 0; i < object.columns.size(); i++) {
                    FinanceSchemaInspector.Column column = object.columns.get(i);
                    if (i > 0) columns.append("  •  ");
                    columns.append(column.name).append(" : ").append(column.sqlType);
                    if (column.primaryKeyOrdinal > 0) columns.append(" [PK ").append(column.primaryKeyOrdinal).append(']');
                    if (column.identity) columns.append(" [IDENTITY]");
                    if (column.computed) columns.append(" [COMPUTED]");
                    if (column.nullable) columns.append(" [NULL]");
                }
                TextView detail = label(columns.toString(), 9.5f, TEXT, false);
                detail.setLineSpacing(dp(2), 1f);
                item.addView(detail, new LinearLayout.LayoutParams(-1, -2));
            }
            results.addView(item, marginParams(-1, -2, 0, 0, 0, 8));
        }
        String summary = query.isEmpty()
                ? "نمایش " + shown + " مورد نخست از " + schemaSnapshot.objects.size() + " شیء. برای یافتن ساختار دقیق جستجو کنید."
                : "نتیجه: " + matched + " شیء؛ نمایش حداکثر " + limit + " مورد.";
        TextView count = label(summary, 10, MUTED, false);
        count.setGravity(Gravity.CENTER);
        results.addView(count, marginParams(-1, -2, 0, dp(2), 0, 10));
        if (shown == 0) {
            TextView empty = label("موردی با این عبارت در Metadata نیامد.", 11, MUTED, false);
            empty.setGravity(Gravity.CENTER);
            results.addView(empty, marginParams(-1, dp(48), 0, 0, 0, 8));
        }
    }

    private boolean matches(FinanceSchemaInspector.DbObject object, String query) {
        if (query.isEmpty()) return true;
        if ((object.schema + "." + object.name + " " + object.kind).toLowerCase(Locale.ROOT).contains(query)) return true;
        for (FinanceSchemaInspector.Column column : object.columns) {
            if ((column.name + " " + column.sqlType).toLowerCase(Locale.ROOT).contains(query)) return true;
        }
        return false;
    }

    private void renderRelations() {
        if (schemaSnapshot == null) {
            addHeading("Relation و Index", "بدون Schema واقعی، چیزی برای نمایش وجود ندارد.");
            addStatusCard("UNKNOWN", "ابتدا Metadata را دریافت کنید.", WARNING);
            return;
        }
        addHeading("کلیدها و Relationهای ثبت‌شده", "خروجی مستقیم Metadata SQL Server؛ معنای تجاری هنوز نیازمند تأیید است.");
        if (schemaSnapshot.foreignKeyColumns.isEmpty()) {
            addStatusCard("Foreign Key ثبت‌شده پیدا نشد", "این به‌تنهایی به معنی نبود رابطهٔ منطقی بین جداول نیست.", WARNING);
        } else {
            for (FinanceSchemaInspector.ForeignKeyColumn fk : schemaSnapshot.foreignKeyColumns) {
                addBodyPanel(fk.name + " • ستون " + fk.ordinal,
                        fk.parentSchema + "." + fk.parentTable + "." + fk.parentColumn +
                                " → " + fk.referencedSchema + "." + fk.referencedTable + "." + fk.referencedColumn);
            }
        }
        addHeading("Indexهای قابل مشاهده", "تعریف Indexها از Catalog خوانده شده است.");
        int shown = 0;
        for (FinanceSchemaInspector.IndexColumn index : schemaSnapshot.indexColumns) {
            if (shown >= 120) break;
            shown++;
            addBodyPanel(index.schema + "." + index.table + " • " + index.name,
                    index.column + (index.primary ? " • PRIMARY KEY" : "") + (index.unique ? " • UNIQUE" : "") +
                            (index.included ? " • INCLUDE" : "") + (index.keyOrdinal > 0 ? " • ترتیب " + index.keyOrdinal : ""));
        }
        if (schemaSnapshot.indexColumns.size() > shown) {
            addStatusCard("فهرست محدود", "از " + schemaSnapshot.indexColumns.size() + " عضو Index، " + shown + " مورد نخست نمایش داده شد.", WARNING);
        }
    }

    private void renderSecurity() {
        addHeading("وضعیت امنیت و آمادگی", "این صفحه وضعیت فعلی پیاده‌سازی را شفاف می‌کند؛ PASS فقط با آزمون واقعی صادر می‌شود.");
        if (accessSnapshot != null) {
            addStatusCard("SQL principal در این درخواست authenticate شد", "RoleID واقعی: " + accessSnapshot.roleId +
                    " • MenuID برگشتی از ProcMenuPermission: " + accessSnapshot.permittedMenuIds().size() +
                    " • Menu/Form قابل نگاشت از vw_MenuInfo: " + accessSnapshot.permittedMenus().size() +
                    " • snapshot فقط در حافظه است؛ هیچ ردیف مالی خوانده نشد.", SUCCESS);
        }
        addStatusCard("Database credential در Source/APK: ندارد", "Finance یک ماژول مستقل است و از credentialهای مبهم‌شدهٔ MainActivity قدیمی استفاده نمی‌کند.", SUCCESS);
        addStatusCard("اتصال مستقیم SQL: Debug و فقط Metadata/ACL", "در Release، شروع اتصال مستقیم در کد مسدود است. مسیر احراز هویت فقط SQL principal محدود و ACL همان حساب را می‌خواند؛ برای دادهٔ مالی Production تأیید نشده.", WARNING);
        addStatusCard("TLS: گواهی باید تأیید شود", "اتصال از jTDS با ssl=authenticate درخواست می‌شود؛ سازگاری گواهی، TLS و نسخهٔ واقعی Driver هنوز آزموده نشده است.", WARNING);
        addStatusCard("Atiran Authentication / Role: PROTOTYPE — NOT VERIFIED LIVE", "دکمهٔ Debug هویت SQL را فقط با تطبیق یکتای sys_users، active/IsLocked و dbo.get_role_id بررسی می‌کند؛ روی سرور زنده آزموده و به session عملیاتی وصل نشده است. دادهٔ مالی ممنوع است.", accessSnapshot == null ? ERROR : WARNING);
        addStatusCard("ACL واقعی: قرارداد کاتالوگ بررسی شده؛ policy action باز است", "dbo.ProcMenuPermission فقط MenuID را با وجود grant فرم و زیرسیستم برمی‌گرداند و PermissionId را فیلتر نمی‌کند؛ Permissionهای جداگانه نمایش داده می‌شوند و ترکیب آن‌ها حدس زده نمی‌شود.", WARNING);
        addStatusCard("SQL provisioning خطرناک: استفاده نمی‌شود", "تعریف dbo.Create_Login به SQL login نقش sysadmin و db_owner می‌دهد؛ Finance آن را اجرا نمی‌کند. اتصال مستقیم Release تا تأیید SQL principal محدود و مجوز سمت سرور مسدود است.", ERROR);
        addStatusCard("نوشتن مالی / Audit / idempotency: غیرفعال", "این نسخه هیچ عملیات INSERT / UPDATE / DELETE / DDL مالی ندارد.", SUCCESS);
        addStatusCard("Offline cache: ندارد", "هیچ داده یا credential مالی به SharedPreferences، فایل یا Backup نوشته نمی‌شود.", SUCCESS);
        addBodyPanel("برای ادامه", "پس از تأیید Schema زنده و policy مؤثر، هر منبع مالی باید جداگانه نگاشت و آزمون شود. هر عملیات نوشتنی باید سمت SQL/سرویس قابل‌اعتماد با Permission، Transaction، Audit و کلید idempotency محافظت شود؛ در این نسخه عمداً پیاده‌سازی نشده است.");
    }

    private void addHeading(String title, String subtitle) {
        TextView heading = label(title, 21, TEXT, true);
        heading.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        pageContent.addView(heading, marginParams(-1, -2, 0, dp(2), 0, 3));
        TextView description = label(subtitle, 10.5f, MUTED, false);
        description.setLineSpacing(dp(2), 1.05f);
        pageContent.addView(description, marginParams(-1, -2, 0, 0, 0, 14));
    }

    private void addStatusCard(String title, String body, int accent) {
        LinearLayout card = panel();
        card.setBackground(background(SURFACE, 19, alpha(accent, 120)));
        card.addView(label(title, 12.5f, accent, true));
        TextView text = label(body, 10.5f, MUTED, false);
        text.setLineSpacing(dp(2), 1.04f);
        card.addView(text, marginParams(-1, -2, 0, dp(5), 0, 0));
        pageContent.addView(card, marginParams(-1, -2, 0, 0, 0, 9));
    }

    private void addBodyPanel(String title, String body) {
        LinearLayout card = panel();
        card.addView(label(title, 11.7f, GOLD, true));
        TextView text = label(body, 10.2f, MUTED, false);
        text.setLineSpacing(dp(2), 1.04f);
        card.addView(text, marginParams(-1, -2, 0, dp(4), 0, 0));
        pageContent.addView(card, marginParams(-1, -2, 0, 0, 0, 8));
    }

    private void addNotice(String message, int accent) {
        LinearLayout box = panel();
        box.setBackground(background(alpha(accent, 24), 17, alpha(accent, 120)));
        TextView t = label(message, 10.5f, accent, true);
        t.setLineSpacing(dp(2), 1.04f);
        box.addView(t, new LinearLayout.LayoutParams(-1, -2));
        pageContent.addView(box, marginParams(-1, -2, 0, 0, 0, 10));
    }

    private LinearLayout panel() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(13), dp(12), dp(13), dp(12));
        box.setBackground(background(SURFACE, 20, BORDER));
        setRtl(box);
        return box;
    }

    private EditText input(String hint) {
        EditText field = new EditText(this);
        field.setSingleLine(true);
        field.setTextSize(12.5f);
        field.setTextColor(TEXT);
        field.setHintTextColor(MUTED);
        field.setHint(hint);
        field.setTypeface(vazirRegular);
        field.setPadding(dp(13), 0, dp(13), 0);
        field.setBackground(background(SURFACE, 15, BORDER));
        field.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        setRtl(field);
        return field;
    }

    private TextView action(String title, boolean primary) {
        TextView button = label(title, 12.3f, primary ? NAVY : TEXT, true);
        button.setGravity(Gravity.CENTER);
        button.setPadding(dp(12), dp(8), dp(12), dp(8));
        button.setBackground(background(primary ? GOLD : SURFACE_RAISED, 16, primary ? GOLD : BORDER));
        button.setClickable(true);
        button.setFocusable(true);
        return button;
    }

    private TextView label(String value, float size, int color, boolean bold) {
        TextView view = new TextView(this);
        view.setText(value == null ? "" : value);
        view.setTextSize(size);
        view.setTextColor(color);
        view.setTypeface(bold ? vazirBold : vazirRegular);
        view.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        view.setTextDirection(View.TEXT_DIRECTION_RTL);
        view.setIncludeFontPadding(true);
        return view;
    }

    private void setRtl(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            view.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
            view.setTextDirection(View.TEXT_DIRECTION_RTL);
        }
    }

    private GradientDrawable background(int color, float radius, int stroke) {
        GradientDrawable shape = new GradientDrawable();
        shape.setColor(color);
        shape.setCornerRadius(dp(radius));
        if (stroke != Color.TRANSPARENT) shape.setStroke(dp(1), stroke);
        return shape;
    }

    private int alpha(int color, int value) {
        return Color.argb(value, Color.red(color), Color.green(color), Color.blue(color));
    }

    private LinearLayout.LayoutParams marginParams(int width, int height, int left, int top, int right, int bottom) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, height);
        params.setMargins(dp(left), dp(top), dp(right), dp(bottom));
        return params;
    }

    private int dp(float value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private List<Module> modules() {
        List<Module> list = new ArrayList<>();
        list.add(new Module("sales", "فروش و وصول", "↗"));
        list.add(new Module("pos", "POS و پرداخت‌ها", "▣"));
        list.add(new Module("cash", "صندوق", "◉"));
        list.add(new Module("settlements", "تسویهٔ کاربران", "⇄"));
        list.add(new Module("bank", "بانک و مغایرت‌گیری", "▤"));
        list.add(new Module("receivables", "مطالبات", "◷"));
        list.add(new Module("checks", "چک‌ها", "▧"));
        list.add(new Module("reports", "گزارش و Audit", "▥"));
        list.add(new Module("calendar", "تقویم و اعلان‌ها", "▦"));
        list.add(new Module("closing", "برج کنترل و بستن روز", "◈"));
        return list;
    }

    private Module moduleByKey(String key) {
        for (Module module : modules()) if (module.key.equals(key)) return module;
        return null;
    }

    private static final class Module {
        final String key;
        final String title;
        final String glyph;
        Module(String key, String title, String glyph) {
            this.key = key;
            this.title = title;
            this.glyph = glyph;
        }
    }
}

package ir.meelano.android.finance;

import ir.meelano.android.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * «آتیران مالی» — the single launcher activity of the finance edition
 * ({@code ir.meelano.atiran.finance}).
 *
 * It owns three things:
 *  • the real sign-in (Atiran visitors / sys_users + the project's access-control role, seen
 *    through {@link FinAuth} and {@link FinSession});
 *  • the shell: gradient header with the live operator, real role, server date and connection state,
 *    plus the bottom navigation  خانه | مالی | چک‌ها | مطالبات | بیشتر  and a screen stack for
 *    drill-downs (KPI → list → detail → source record);
 *  • the shared state every screen reads: server date, period filter, database handle and theme.
 *
 * Startup never happens behind a blank window: the very first content view is the plain
 * {@link FinBoot} panel, and every following step (theme, fonts, data layer, sign-in screen, desk)
 * is announced there with its duration. A step that throws or hangs is therefore visible on the
 * device itself, and the sign-in form on that panel keeps the app usable even if the rich UI cannot
 * be built on a particular phone.
 *
 * Nothing here invents data. Screens load real rows through {@link FinQueries}; a value that has no
 * verified source in the Atiran schema is simply not displayed.
 */
public class AtiranFinanceActivity extends Activity {

    public static final String TAB_HOME = "home";
    public static final String TAB_MONEY = "money";
    public static final String TAB_CHECKS = "checks";
    public static final String TAB_RECEIVABLES = "receivables";
    public static final String TAB_MORE = "more";

    /** A sign-in attempt is abandoned after this long, visibly, instead of hanging for ever. */
    private static final long LOGIN_TIMEOUT_MS = 35_000L;
    /** A startup step longer than this is called out on the boot panel while it is still running. */
    private static final long STEP_WARN_MS = 6_000L;

    private FinDb db;
    private FinUi ui;
    private String tab = TAB_HOME;
    private String periodKey = "month";
    private String periodFrom;
    private String periodTo;
    private String serverToday;
    private boolean hasCache;

    private LinearLayout headerBar;
    private FrameLayout contentHost;
    private LinearLayout navBar;

    private final Deque<FinScreen> stack = new ArrayDeque<>();
    private FinScreen current;
    private boolean showingLogin;

    private final Handler main = new Handler(Looper.getMainLooper());
    private Object backCallback;

    // ---- startup narration
    private FinBoot boot;
    private View bootView;
    private String runningStep;
    private long stepStartedAt;
    private String warnedStep;
    private boolean watchdogOn = true;
    private int renderToken;
    private boolean renderDone;

    // ---- the rich sign-in screen (kept as fields so the shared login flow can drive it)
    private TextView loginState;
    private TextView loginElapsed;
    private Button loginButton;
    private TextView loginHint;

    // ==================================================================== lifecycle

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        FinCrash.install(this);
        boot = new FinBoot(this, new BootListener());
        bootView = boot.view();
        setContentView(bootView);
        String crash = FinCrash.lastCrash(this);
        if (crash != null && !crash.trim().isEmpty()) boot.showCrash(crash);
        boot.note("نسخهٔ برنامه " + safeVersion() + " · " + FinSession.deviceLabel());
        main.postDelayed(this::watchStartup, 2_000L);
        main.postDelayed(this::runStep1, 40L);
    }

    /** 1/6 — the colour theme, the typefaces and the shared widget kit. */
    private void runStep1() {
        if (!beginStep("آماده‌سازی رنگ، قلم و ابزارهای رابط")) return;
        try { ui = new FinUi(this); }
        catch (Throwable t) { failStep("آماده‌سازی رنگ، قلم و ابزارهای رابط", t, true); return; }
        endStep("آماده‌سازی رنگ، قلم و ابزارهای رابط", "قلم " + ui.regularFace().toString());
        main.postDelayed(this::runStep2, 24L);
    }

    /** 2/6 — the data layer (connection pool, caches, guard rails). */
    private void runStep2() {
        if (!beginStep("آماده‌سازی لایهٔ داده")) return;
        try { db = new FinDb(this); }
        catch (Throwable t) { failStep("آماده‌سازی لایهٔ داده", t, true); return; }
        endStep("آماده‌سازی لایهٔ داده", FinEnv.describe());
        main.postDelayed(this::runStep3, 24L);
    }

    /** 3/6 — window chrome (status/navigation bar colours of the active theme). */
    private void runStep3() {
        if (!beginStep("تنظیم نوار وضعیت")) return;
        try { ui.applySystemBars(); }
        catch (Throwable t) { boot.warn("تنظیم نوار وضعیت", t.getClass().getSimpleName()); }
        endStep("تنظیم نوار وضعیت", null);
        main.postDelayed(this::runStep4, 24L);
    }

    /** 4/6 — who is signed in: the desk, or the sign-in screen. */
    private void runStep4() {
        if (!beginStep("بررسی نشست کاربر")) return;
        boolean loggedIn = FinSession.isLoggedIn();
        endStep("بررسی نشست کاربر", loggedIn ? "نشست فعال است" : "ورود لازم است");
        if (loggedIn) main.postDelayed(this::openDesk, 24L);
        else main.postDelayed(this::openLogin, 24L);
    }

    /** 5a/6 — the operator desk. */
    private void openDesk() {
        if (!beginStep("ساخت میزکار")) return;
        try {
            buildShell();
        } catch (Throwable t) {
            failStep("ساخت میزکار", t, true);
            return;
        }
        endStep("ساخت میزکار", null);
        watchdogOn = false;
        if (bootView != null) bootView.postDelayed(() -> { if (boot != null) boot.ok("آماده است"); }, 600L);
    }

    /** 5b/6 — the rich sign-in screen; on failure the plain form on the panel remains usable. */
    private void openLogin() {
        if (!beginStep("ساخت صفحهٔ ورود")) return;
        try {
            buildLogin();
        } catch (Throwable t) {
            failStep("ساخت صفحهٔ ورود", t, false);
            boot.showLogin();
            return;
        }
        endStep("ساخت صفحهٔ ورود", null);
        watchdogOn = false;
    }

    private boolean beginStep(String label) {
        if (boot == null) return false;
        runningStep = label;
        stepStartedAt = System.currentTimeMillis();
        boot.step(label);
        return true;
    }

    private void endStep(String label, String detail) {
        runningStep = null;
        if (boot == null) return;
        boot.ok(label + (detail == null || detail.isEmpty() ? "" : "  —  " + detail));
    }

    /**
     * A startup step that throws is reported on the panel with its real exception text; the app stays
     * alive so the operator can sign in from the plain form and send the report instead of facing a
     * blank screen.
     */
    private void failStep(String label, Throwable t, boolean offerPlainLogin) {
        runningStep = null;
        watchdogOn = false;
        FinCrash.log(this, "startup-failed", label + ": " + t.getClass().getName());
        try { FinCrash.install(this); } catch (Throwable ignored) { }
        boot.fail(label, t);
        if (offerPlainLogin) boot.showLogin();
        try { Toast.makeText(this, "کد رویداد: " + FinCrash.eventCode(t), Toast.LENGTH_LONG).show(); } catch (Throwable ignored) { }
    }

    /** While a step is running, say so every two seconds — a hang then becomes a visible line. */
    private void watchStartup() {
        if (!watchdogOn) return;
        try {
            if (runningStep != null && System.currentTimeMillis() - stepStartedAt > STEP_WARN_MS
                    && !runningStep.equals(warnedStep)) {
                warnedStep = runningStep;
                long sec = (System.currentTimeMillis() - stepStartedAt) / 1000L;
                boot.warn(runningStep, "بیش از " + FinFmt.faNumber(sec) + " ثانیه است که این مرحله ادامه دارد");
            }
        } catch (Throwable ignored) { }
        main.postDelayed(this::watchStartup, 2_000L);
    }

    private String safeVersion() {
        try { return FinSession.appVersion(); } catch (Throwable t) { return "?"; }
    }

    // ==================================================================== accessors used by screens

    public FinUi ui() { return ui; }

    public FinDb db() { return db; }

    public FinSession session() { return FinSession.get(); }

    public String today() { return serverToday == null ? FinFmt.todayLocal() : serverToday; }

    public String periodKey() { return periodKey; }

    public String periodFrom() { return periodFrom == null ? today() : periodFrom; }

    public String periodTo() { return periodTo == null ? today() : periodTo; }

    public boolean hasCache() { return hasCache; }

    public void setPeriod(String key) {
        this.periodKey = key;
        String[] range = FinFmt.periodRange(key, today(), null, null);
        this.periodFrom = range[0];
        this.periodTo = range[1];
        reload();
    }

    /** Shows a modal technical-free summary of a record (drill-down "details" step). */
    public void details(String title, String[][] pairs, String note) {
        LinearLayout col = ui.column();
        col.setPadding(ui.dp(6), ui.dp(4), ui.dp(6), ui.dp(4));
        int index = 0;
        for (String[] p : pairs) {
            if (p == null || p.length < 2) continue;
            col.addView(kvLine(p[0], p[1], index++ % 2 == 0), ui.lp(-1, -2));
        }
        if (note != null && !note.isEmpty()) {
            TextView n = ui.text(note, 11.5f, ui.textDim, false);
            n.setPadding(0, ui.dp(10), 0, 0);
            col.addView(n, ui.lp(-1, -2));
        }
        ScrollView sc = ui.scroll();
        sc.addView(col);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(title)
                .setView(sc)
                .setPositiveButton("بستن", null)
                .create();
        dialog.setOnShowListener(d -> {
            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawable(ui.rounded(ui.surface, 20, ui.stroke, 1));
            }
            Button ok = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            if (ok != null) { ok.setTextColor(ui.goldAccent); ok.setAllCaps(false); ok.setTypeface(ui.boldFace()); }
            int titleId = getResources().getIdentifier("alertTitle", "id", "android");
            TextView t = titleId == 0 ? null : dialog.findViewById(titleId);
            if (t != null) { t.setTextColor(ui.textColor); t.setTypeface(ui.boldFace()); t.setGravity(Gravity.START); }
        });
        dialog.show();
    }

    private View kvLine(String k, String v, boolean shaded) {
        LinearLayout r = ui.row();
        r.setPadding(ui.dp(12), ui.dp(7), ui.dp(12), ui.dp(7));
        if (shaded) r.setBackground(ui.rounded(ui.surface2, 10, 0, 0));
        TextView label = ui.text(k, 12.5f, ui.textDim, false);
        r.addView(label, ui.lp(0, -2, 1f));
        TextView value = ui.text(v, 13f, ui.textColor, true);
        value.setGravity(Gravity.END);
        r.addView(value, ui.lp(0, -2, 1.2f));
        return r;
    }

    public void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    /** True when the operator holds a finance permission; the shell hides what is not allowed. */
    public boolean can(String permission) {
        FinSession s = FinSession.get();
        return s == null || s.can(permission);
    }

    // ==================================================================== sign-in

    /**
     * The sign-in flow, shared by the rich screen and by the plain form on the boot panel, so both
     * behave identically: background work, a visible elapsed counter, a hard timeout, and a message
     * for every outcome — success, refusal, timeout or unexpected error.
     */
    private interface LoginSink {
        void busy(boolean busy);
        void status(String text);
        void elapsed(int seconds);
        void message(String text, boolean error);
        void success();
    }

    private final class DesignedLoginSink implements LoginSink {
        @Override public void busy(boolean busy) {
            if (loginButton != null) {
                loginButton.setEnabled(!busy);
                loginButton.setAlpha(busy ? 0.6f : 1f);
            }
        }
        @Override public void status(String text) {
            if (loginHint != null) loginHint.setText(text);
        }
        @Override public void elapsed(int seconds) {
            if (loginElapsed == null) return;
            loginElapsed.setVisibility(seconds <= 0 ? View.GONE : View.VISIBLE);
            if (seconds > 0) loginElapsed.setText("زمان سپری‌شده: " + FinFmt.faNumber(seconds) + " ثانیه");
        }
        @Override public void message(String text, boolean error) {
            if (loginState == null) return;
            loginState.setTextColor(error ? FinUi.DANGER : FinUi.SUCCESS);
            loginState.setText(text == null ? "" : text);
        }
        @Override public void success() {
            openDesk();
        }
    }

    private final class BootLoginSink implements LoginSink {
        @Override public void busy(boolean busy) { boot.setBusy(busy); }
        @Override public void status(String text) { boot.setStatus(text); }
        @Override public void elapsed(int seconds) { boot.setElapsed(seconds); }
        @Override public void message(String text, boolean error) { boot.setMessage(text, error); }
        @Override public void success() { openDesk(); }
    }

    private final BootLoginSink bootSink = new BootLoginSink();

    private final class BootListener implements FinBoot.Listener {
        @Override public void bootLogin(String user, String pass) {
            attemptLogin(user, pass, bootSink);
        }
        @Override public void bootDiagnostics() { showDiagnostics(bootSink); }
        @Override public void bootSelfTest() { runSelfTest(); }
        @Override public void bootCopy() { copyText(boot.logText(), "گزارش آتیران مالی"); }
        @Override public void bootContinue() {
            boot.showLogin();
            if (ui == null) {
                try { ui = new FinUi(AtiranFinanceActivity.this); } catch (Throwable ignored) { }
            }
            if (db == null) {
                try { db = new FinDb(AtiranFinanceActivity.this); } catch (Throwable ignored) { }
            }
            openLogin();
        }
    }

    private final DesignedLoginSink designedSink = new DesignedLoginSink();

    /**
     * One sign-in attempt. Runs on its own daemon thread (never the UI thread and never the query
     * pool, which must stay free), reports progress to the sink, and always ends with either the desk
     * or a readable message.
     */
    private void attemptLogin(final String rawUser, final String rawPass, final LoginSink sink) {
        if (rawUser == null || rawUser.trim().isEmpty() || rawPass == null || rawPass.isEmpty()) {
            sink.message("نام کاربری و رمز عبور را وارد کنید.", true);
            return;
        }
        if (db == null) {
            try { db = new FinDb(this); } catch (Throwable t) {
                sink.message("لایهٔ داده ساخته نشد (" + t.getClass().getSimpleName() + ") · کد رویداد "
                        + FinCrash.eventCode(t), true);
                return;
            }
        }
        if (FinAuth.lockedOut(this)) {
            sink.message("ورود موقتاً قفل است؛ " + FinFmt.faNumber(FinAuth.lockRemainingMs(this) / 60000L + 1)
                    + " دقیقه دیگر تلاش کنید.", true);
            return;
        }
        sink.busy(true);
        sink.elapsed(0);
        sink.status("در حال بررسی روی سرور آتیران…");
        sink.message("", false);

        final AtomicBoolean finished = new AtomicBoolean(false);
        final Thread worker = new Thread(() -> {
            FinAuth.Result result;
            try {
                result = FinAuth.authenticate(AtiranFinanceActivity.this, db, rawUser, rawPass);
            } catch (Throwable t) {
                result = new FinAuth.Result();
                result.ok = false;
                result.message = friendlyError(t);
                FinCrash.log(AtiranFinanceActivity.this, "login-failed", t.getClass().getName());
            }
            final FinAuth.Result r = result;
            main.post(() -> {
                if (!finished.compareAndSet(false, true)) return;
                sink.busy(false);
                sink.elapsed(0);
                if (r.ok && r.session != null) {
                    try { r.session.remember(AtiranFinanceActivity.this); } catch (Throwable ignored) { }
                    sink.message("خوش آمدید " + FinSession.displayOrUser(), false);
                    sink.success();
                } else {
                    String message = r.message == null || r.message.isEmpty() ? "ورود ناموفق بود." : r.message;
                    sink.message(message + "\nاگر تکرار شد، «بررسی اتصال به سرور» را بزنید.", true);
                }
            });
        }, "fin-login");
        worker.setDaemon(true);
        worker.start();

        final int[] seconds = {0};
        Runnable tick = new Runnable() {
            @Override public void run() {
                if (finished.get()) return;
                seconds[0]++;
                sink.elapsed(seconds[0]);
                if (seconds[0] * 1000L >= LOGIN_TIMEOUT_MS) {
                    if (finished.compareAndSet(false, true)) {
                        try { worker.interrupt(); } catch (Throwable ignored) { }
                        sink.busy(false);
                        sink.elapsed(0);
                        sink.message("مهلت اتصال پس از " + FinFmt.faNumber(LOGIN_TIMEOUT_MS / 1000)
                                + " ثانیه تمام شد. شبکه را بررسی کنید یا «بررسی اتصال به سرور» را بزنید.", true);
                    }
                    return;
                }
                main.postDelayed(this, 1_000L);
            }
        };
        main.postDelayed(tick, 1_000L);
    }

    private String friendlyError(Throwable t) {
        if (t == null) return "خطای غیرمنتظره در ورود.";
        String name = t.getClass().getSimpleName();
        if (t instanceof OutOfMemoryError) return "حافظهٔ برنامه پر شد؛ برنامه را ببندید و دوباره باز کنید.";
        if (t instanceof NoClassDefFoundError) return "کتابخانهٔ ارتباط با سرور بارگذاری نشد (کد "
                + FinCrash.eventCode(t) + ").";
        return "خطای غیرمنتظره (" + name + ") · کد رویداد " + FinCrash.eventCode(t);
    }

    private void buildLogin() {
        showingLogin = true;
        if (ui == null) ui = new FinUi(this);
        if (db == null) db = new FinDb(this);
        ui.applySystemBars();

        LinearLayout root = ui.column();
        root.setBackground(ui.gradient(ui.bg, FinUi.mix(ui.bg, ui.goldAccent, 0.14f), 0));
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(ui.dp(20), ui.dp(28), ui.dp(20), ui.dp(24));

        FinCharts.Logo logo = new FinCharts.Logo(this, ui.goldAccent, ui.silver, ui.surface, ui.bg);
        LinearLayout.LayoutParams logoLp = ui.lp(ui.dp(116), ui.dp(116));
        logoLp.gravity = Gravity.CENTER_HORIZONTAL;
        root.addView(logo, logoLp);

        TextView name = ui.text("آتیران مالی", 25f, ui.goldAccent, true);
        name.setGravity(Gravity.CENTER);
        root.addView(name, ui.lp(-1, -2));
        TextView sub = ui.text("مرکز عملیات مالی، خزانه، مطالبات و مغایرت", 12.5f, ui.textDim, false);
        sub.setGravity(Gravity.CENTER);
        root.addView(sub, ui.lp(-1, -2));
        LinearLayout badges = ui.row();
        badges.setGravity(Gravity.CENTER);
        badges.setPadding(0, ui.dp(10), 0, 0);
        badges.addView(ui.chip("نسخه " + FinSession.appVersion(), ui.goldAccent), ui.lp(-2, -2));
        badges.addView(ui.spacer(6), ui.lp(ui.dp(6), -2));
        badges.addView(ui.chip("خزانه · مطالبات · مغایرت · POS", ui.textFaint), ui.lp(-2, -2));
        root.addView(badges, ui.lp(-1, -2));
        addSpace(root, 18);

        LinearLayout card = ui.gradientCard(ui.goldAccent, 20);
        card.addView(ui.sectionTitle("ورود", "با حساب واقعی آتیران", "🔐"), ui.lp(-1, -2));

        JSONObject last = FinSession.lastUser(this);
        EditText user = fieldWithGlyph(card, "♙", getString(R.string.fin_username), false);
        user.setInputType(InputType.TYPE_CLASS_TEXT);
        String lastUser = last.optString("username", "");
        if (!lastUser.isEmpty()) user.setText(lastUser);

        EditText pass = fieldWithGlyph(card, "🔒", getString(R.string.fin_password), true);
        pass.setImeOptions(EditorInfo.IME_ACTION_DONE);
        final boolean[] visible = {false};
        TextView toggle = ui.text("نمایش", 11f, ui.goldAccent, true);
        toggle.setPadding(ui.dp(8), ui.dp(4), ui.dp(2), ui.dp(4));
        toggle.setOnClickListener(v -> {
            visible[0] = !visible[0];
            pass.setInputType(visible[0]
                    ? InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                    : InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            pass.setSelection(pass.getText().length());
            toggle.setText(visible[0] ? "پنهان" : "نمایش");
        });
        LinearLayout toggleRow = ui.row();
        toggleRow.setGravity(Gravity.END);
        toggleRow.addView(toggle, ui.lp(-2, -2));
        card.addView(toggleRow, ui.lp(-1, -2));

        loginHint = ui.text("نام کاربری و رمز عبور حساب آتیران خود را وارد کنید.", 11.5f, ui.textDim, false);
        loginHint.setPadding(0, ui.dp(6), 0, 0);
        card.addView(loginHint, ui.lp(-1, -2));

        loginElapsed = ui.text("", 11f, ui.textFaint, false);
        loginElapsed.setVisibility(View.GONE);
        card.addView(loginElapsed, ui.lp(-1, -2));

        loginState = ui.text("", 12f, FinUi.DANGER, false);
        loginState.setPadding(0, ui.dp(8), 0, 0);
        card.addView(loginState, ui.lp(-1, -2));

        if (FinAuth.lockedOut(this)) {
            long left = FinAuth.lockRemainingMs(this) / 60000L + 1;
            loginState.setText("ورود موقتاً قفل است؛ " + FinFmt.faNumber(left) + " دقیقه دیگر تلاش کنید.");
        }

        loginButton = ui.primaryButton("ورود به آتیران مالی", ui.goldAccent, null);
        LinearLayout.LayoutParams lp2 = ui.lp(-1, -2);
        lp2.topMargin = ui.dp(12);
        card.addView(loginButton, lp2);

        Button diag = ui.ghostButton("بررسی اتصال به سرور", ui.goldAccent, null);
        LinearLayout.LayoutParams lp3 = ui.lp(-1, -2);
        lp3.topMargin = ui.dp(8);
        card.addView(diag, lp3);

        loginButton.setOnClickListener(v -> attemptLogin(
                user.getText().toString().trim(), pass.getText().toString(), designedSink));
        diag.setOnClickListener(v -> showDiagnostics(designedSink));
        pass.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                attemptLogin(user.getText().toString().trim(), pass.getText().toString(), designedSink);
                return true;
            }
            return false;
        });
        root.addView(card, ui.lp(-1, -2));

        TextView env = ui.text(FinEnv.describe(), 11f, ui.textFaint, false);
        env.setGravity(Gravity.CENTER);
        env.setPadding(0, ui.dp(14), 0, 0);
        root.addView(env, ui.lp(-1, -2));

        TextView support = ui.text("اگر برنامه درست کار نکرد، از «بررسی اتصال به سرور» گزارش بگیرید و برای پشتیبانی بفرستید.",
                11f, ui.textFaint, false);
        support.setGravity(Gravity.CENTER);
        support.setPadding(0, ui.dp(6), 0, 0);
        root.addView(support, ui.lp(-1, -2));

        ScrollView sc = ui.scroll();
        sc.addView(root);
        setContentView(sc);
    }

    /**
     * A field with a glyph badge: rounded container, accent icon, borderless input. It is the same
     * pattern on every form of the app, so the keyboard never covers a one-line field.
     */
    private EditText fieldWithGlyph(LinearLayout parent, String glyph, String hint, boolean password) {
        LinearLayout box = ui.row();
        box.setBackground(ui.rounded(ui.surface2, 14, ui.stroke, 1));
        box.setPadding(ui.dp(10), ui.dp(4), ui.dp(12), ui.dp(4));
        LinearLayout.LayoutParams boxLp = ui.lp(-1, ui.dp(54));
        boxLp.topMargin = ui.dp(6);
        TextView badge = ui.text(glyph, 14f, ui.goldAccent, true);
        badge.setGravity(Gravity.CENTER);
        box.addView(badge, ui.lp(ui.dp(26), -2));
        EditText e = new EditText(this);
        e.setHint(hint);
        e.setHintTextColor(ui.textFaint);
        e.setTextColor(ui.textColor);
        e.setTextSize(15f);
        e.setTypeface(ui.regularFace());
        e.setTextDirection(View.TEXT_DIRECTION_RTL);
        e.setBackgroundColor(android.graphics.Color.TRANSPARENT);
        e.setPadding(ui.dp(4), 0, 0, 0);
        e.setSingleLine(true);
        if (password) e.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        if (password) e.setTransformationMethod(PasswordTransformationMethod.getInstance());
        box.addView(e, ui.lp(0, -2, 1f));
        parent.addView(box, boxLp);
        return e;
    }

    private void addSpace(LinearLayout parent, int dp) {
        parent.addView(ui.spacer(dp));
    }

    // ==================================================================== diagnostics & support

    /** Runs the connection doctor from anywhere in the app (the «بیشتر» screen uses this entry). */
    public void showDiagnostics() { showDiagnostics(null); }

    /** Runs the connection doctor on a background thread and shows the plain report. */
    private void showDiagnostics(final LoginSink sink) {
        if (db == null) {
            try { db = new FinDb(this); } catch (Throwable t) {
                if (sink != null) sink.message("لایهٔ داده ساخته نشد: " + t.getClass().getSimpleName(), true);
                return;
            }
        }
        final ProgressDialog progress = new ProgressDialog(this);
        progress.setMessage("در حال بررسی اینترنت، سرور و پایگاه داده…");
        progress.setCancelable(false);
        try { progress.show(); } catch (Throwable ignored) { }
        new Thread(() -> {
            FinDiag.Report report;
            try {
                report = FinDiag.collect(AtiranFinanceActivity.this, db);
            } catch (Throwable t) {
                report = new FinDiag.Report();
                report.allOk = false;
                report.text = "گزارش بررسی کامل نشد — " + t.getClass().getName() + ": " + t.getMessage()
                        + "\nکد رویداد: " + FinCrash.eventCode(t);
                FinCrash.log(AtiranFinanceActivity.this, "diagnostics-failed", t.getClass().getName());
            }
            final FinDiag.Report r = report;
            main.post(() -> {
                try { progress.dismiss(); } catch (Throwable ignored) { }
                if (boot != null && boot.view().getParent() != null) boot.setReport(r.text);
                if (sink != null) sink.message(FinDiag.summary(r), !r.allOk);
                showReportDialog(r);
            });
        }, "fin-diag").start();
    }

    private void showReportDialog(FinDiag.Report report) {
        TextView body = new TextView(this);
        body.setText(report == null ? "" : report.text);
        body.setTextSize(11.5f);
        body.setTypeface(Typeface.MONOSPACE);
        body.setTextColor(ui == null ? 0xFFF2F5FA : ui.textColor);
        body.setTextDirection(View.TEXT_DIRECTION_RTL);
        body.setPadding(ui == null ? 24 : ui.dp(12), ui == null ? 24 : ui.dp(12),
                ui == null ? 24 : ui.dp(12), ui == null ? 24 : ui.dp(12));
        ScrollView sc = new ScrollView(this);
        sc.addView(body);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("گزارش بررسی اتصال")
                .setView(sc)
                .setPositiveButton("کپی گزارش", (d, w) -> copyText(body.getText().toString(), "گزارش بررسی آتیران مالی"))
                .setNeutralButton("اشتراک‌گذاری", (d, w) -> shareText(body.getText().toString()))
                .setNegativeButton("بستن", null)
                .create();
        dialog.show();
    }

    /** The full technical self test against the real database (declared in the finance manifest). */
    public void runSelfTest() {
        try {
            startActivity(new Intent(this, FinSelfTestActivity.class));
        } catch (Throwable t) {
            toast("آزمون فنی اجرا نشد: " + t.getClass().getSimpleName());
        }
    }

    public void copyText(String text, String label) {
        try {
            ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            if (cm != null) {
                cm.setPrimaryClip(ClipData.newPlainText(label, text == null ? "" : text));
                toast("گزارش در حافظه کپی شد.");
            }
        } catch (Throwable t) {
            toast("کپی انجام نشد.");
        }
    }

    private void shareText(String text) {
        try {
            Intent send = new Intent(Intent.ACTION_SEND);
            send.setType("text/plain");
            send.putExtra(Intent.EXTRA_SUBJECT, "گزارش آتیران مالی");
            send.putExtra(Intent.EXTRA_TEXT, text == null ? "" : text);
            startActivity(Intent.createChooser(send, "ارسال گزارش"));
        } catch (Throwable t) {
            toast("اشتراک‌گذاری انجام نشد.");
        }
    }

    /** Full startup log and crash report — the file an operator can send to support. */
    public void showSupportLog() {
        String log = FinCrash.bootLog(this);
        String crash = FinCrash.lastCrash(this);
        StringBuilder b = new StringBuilder();
        b.append("گزارش راه‌اندازی و پشتیبانی «آتیران مالی»\n");
        b.append("نسخه ").append(safeVersion()).append(" · ").append(FinSession.deviceLabel()).append('\n');
        b.append("هدف: ").append(FinEnv.describe()).append('\n');
        b.append("\n—— رخداد‌های راه‌اندازی ——\n").append(log == null ? "(خالی)" : log);
        if (crash != null && !crash.trim().isEmpty()) b.append("\n—— آخرین خطای کشنده ——\n").append(crash);
        TextView body = new TextView(this);
        body.setText(b.toString());
        body.setTextSize(11f);
        body.setTypeface(Typeface.MONOSPACE);
        body.setTextColor(ui == null ? 0xFFF2F5FA : ui.textColor);
        body.setTextDirection(View.TEXT_DIRECTION_RTL);
        ScrollView sc = new ScrollView(this);
        sc.addView(body);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("گزارش راه‌اندازی")
                .setView(sc)
                .setPositiveButton("کپی", (d, w) -> copyText(b.toString(), "گزارش راه‌اندازی"))
                .setNegativeButton("بستن", null)
                .create();
        dialog.show();
    }

    // ==================================================================== shell

    private void buildShell() {
        showingLogin = false;
        if (ui == null) ui = new FinUi(this);
        if (db == null) db = new FinDb(this);
        ui.applySystemBars();
        String[] range = FinFmt.periodRange(periodKey, FinFmt.todayLocal(), null, null);
        periodFrom = range[0];
        periodTo = range[1];

        LinearLayout root = ui.column();
        root.setBackgroundColor(ui.bg);

        headerBar = ui.column();
        root.addView(headerBar, ui.lp(-1, -2));

        contentHost = new FrameLayout(this);
        root.addView(contentHost, ui.lp(-1, 0, 1f));

        navBar = ui.row();
        navBar.setBackground(ui.gradient(ui.surface, FinUi.mix(ui.surface, ui.goldAccent, 0.08f), 0));
        navBar.setPadding(ui.dp(6), ui.dp(6), ui.dp(6), ui.dp(6));
        root.addView(navBar, ui.lp(-1, -2));

        setContentView(root);
        renderHeader("… در حال اتصال", true);
        renderNav();
        showTab(TAB_HOME, true);
        updateBackCallback();
        loadServerDate();
        probeConnection();
    }

    /** The header carries the operator identity, the real role and the connection state. */
    public void renderHeader(String connectionNote, boolean loading) {
        if (headerBar == null || ui == null) return;
        headerBar.removeAllViews();
        GradientDrawable bg = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{FinUi.mix(ui.surface, ui.goldAccent, 0.10f), ui.surface, FinUi.mix(ui.surface, ui.goldAccent, 0.05f)});
        headerBar.setBackground(bg);
        headerBar.setPadding(ui.dp(14), ui.dp(12), ui.dp(14), ui.dp(10));

        LinearLayout top = ui.row();
        LinearLayout left = ui.column();
        left.addView(ui.text("آتیران مالی", 16f, ui.goldAccent, true), ui.lp(-1, -2));
        FinSession s = FinSession.get();
        String who = s == null ? "" : (FinSession.displayOrUser() + "  ·  " + s.roleLabel);
        left.addView(ui.text(who, 11.5f, ui.textDim, false), ui.lp(-1, -2));
        top.addView(left, ui.lp(0, -2, 1f));

        LinearLayout chips = ui.column();
        chips.setGravity(Gravity.END);
        if (s != null && !s.roleFromDatabase) {
            chips.addView(ui.chip("نقش از جدول دسترسی خوانده نشد", FinUi.WARNING), ui.lp(-2, -2));
        }
        chips.addView(ui.chip(loading ? "… در حال اتصال" : connectionNote, loading ? FinUi.INFO : (hasCache ? FinUi.SUCCESS : FinUi.WARNING)), ui.lp(-2, -2));
        top.addView(chips, ui.lp(-2, -2));
        headerBar.addView(top, ui.lp(-1, -2));

        LinearLayout meta = ui.row();
        meta.setPadding(0, ui.dp(8), 0, 0);
        meta.addView(ui.chip("تاریخ سرور " + FinFmt.faNumber(serverToday == null ? "…" : serverToday), dateColor()), ui.lp(-2, -2));
        meta.addView(ui.spacer(6), ui.lp(ui.dp(6), -2));
        meta.addView(ui.chip("بازه " + FinFmt.periodLabel(periodKey), ui.goldAccent), ui.lp(-2, -2));
        View filler = ui.spacer(1);
        meta.addView(filler, ui.lp(0, -2, 1f));
        meta.addView(ui.chip("نسخه " + FinSession.appVersion(), ui.textFaint), ui.lp(-2, -2));
        headerBar.addView(meta, ui.lp(-1, -2));

        View line = new View(this);
        line.setBackgroundColor(FinUi.alpha(ui.goldAccent, 90));
        headerBar.addView(line, ui.lp(-1, Math.max(1, ui.dp(0.8f))));
    }

    private int dateColor() {
        return serverToday == null ? FinUi.WARNING : ui.goldSoft;
    }

    private void renderNav() {
        if (navBar == null || ui == null) return;
        navBar.removeAllViews();
        String[][] tabs = {
                {TAB_HOME, "خانه", "⌂"},
                {TAB_MONEY, "مالی", "₪"},
                {TAB_CHECKS, "چک‌ها", "◫"},
                {TAB_RECEIVABLES, "مطالبات", "⇄"},
                {TAB_MORE, "بیشتر", "⋯"}
        };
        for (String[] t : tabs) {
            boolean active = t[0].equals(tab);
            LinearLayout cell = ui.column();
            cell.setGravity(Gravity.CENTER);
            cell.setPadding(0, ui.dp(7), 0, ui.dp(7));
            GradientDrawable bg = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                    active ? new int[]{FinUi.mix(ui.surface2, ui.goldAccent, 0.30f), FinUi.mix(ui.surface, ui.goldAccent, 0.12f)}
                            : new int[]{ui.surface, ui.surface});
            bg.setCornerRadius(ui.dp(14));
            if (active) bg.setStroke(Math.max(1, ui.dp(1)), FinUi.alpha(ui.goldAccent, 170));
            cell.setBackground(bg);
            TextView g = ui.text(t[2], 15f, active ? ui.goldAccent : ui.textFaint, true);
            g.setGravity(Gravity.CENTER);
            TextView l = ui.text(t[1], 10.5f, active ? ui.textColor : ui.textDim, active);
            l.setGravity(Gravity.CENTER);
            cell.addView(g, ui.lp(-1, -2));
            cell.addView(l, ui.lp(-1, -2));
            cell.setOnClickListener(v -> {
                if (!t[0].equals(tab)) showTab(t[0], true);
            });
            cell.setClickable(true);
            cell.setFocusable(true);
            ui.applyTouch(cell);
            LinearLayout.LayoutParams p = ui.lp(0, -2, 1f);
            p.leftMargin = ui.dp(3);
            p.rightMargin = ui.dp(3);
            navBar.addView(cell, p);
        }
    }

    public void showTab(String target, boolean resetStack) {
        tab = target;
        if (resetStack) stack.clear();
        renderNav();
        switch (target) {
            case TAB_MONEY: stack.push(new FinScreenSales(this)); break;
            case TAB_CHECKS: stack.push(new FinScreenCheques(this)); break;
            case TAB_RECEIVABLES: stack.push(new FinScreenReceivables(this)); break;
            case TAB_MORE: stack.push(new FinScreenMore(this)); break;
            default: stack.push(new FinScreenHome(this)); break;
        }
        renderTop(false);
    }

    /** Drill-down: KPI → list → detail → source record. */
    public void open(FinScreen screen) {
        stack.push(screen);
        renderTop(true);
    }

    /**
     * Builds the screen on top of the stack. The construction happens one frame later, behind a small
     * loading card, so a heavy screen can never look like a frozen app — and a screen that throws is
     * replaced by a readable error card with a retry action instead of killing the process.
     */
    private void renderTop(final boolean animate) {
        final FinScreen top = stack.peek();
        current = top;
        renderDone = false;
        updateBackCallback();
        if (top == null || contentHost == null) return;
        final int token = ++renderToken;
        showLoading(top.title());
        contentHost.postDelayed(() -> {
            if (token != renderToken) return;
            View v;
            try {
                v = top.build();
            } catch (Throwable t) {
                FinCrash.log(AtiranFinanceActivity.this, "screen-failed", top.getClass().getSimpleName()
                        + ": " + t.getClass().getName());
                showScreenError(top, t);
                return;
            }
            contentHost.removeAllViews();
            contentHost.addView(v, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            if (animate) {
                v.setAlpha(0f);
                v.setTranslationX(ui.dp(16));
                v.animate().alpha(1f).translationX(0f).setDuration(180).start();
            }
            renderDone = true;
            try { top.load(false); } catch (Throwable t) { FinCrash.log(AtiranFinanceActivity.this, "screen-load", t.getClass().getName()); }
        }, 24L);
    }

    private void showScreenError(FinScreen screen, Throwable t) {
        renderDone = true;
        LinearLayout card = ui.cardTone(FinUi.DANGER);
        card.addView(ui.text("نمایش این بخش ممکن نشد", 14f, ui.textColor, true), ui.lp(-1, -2));
        card.addView(ui.text(screen == null ? "" : screen.title(), 12f, ui.textDim, false), ui.lp(-1, -2));
        card.addView(ui.text(t.getClass().getSimpleName() + ": " + t.getMessage(), 11.5f, ui.textDim, false), ui.lp(-1, -2));
        card.addView(ui.text("کد رویداد: " + FinCrash.eventCode(t), 11.5f, FinUi.DANGER, true), ui.lp(-1, -2));
        Button retry = ui.primaryButton("تلاش دوباره", ui.goldAccent, v -> showTab(tab, true));
        LinearLayout.LayoutParams p = ui.lp(-1, -2);
        p.topMargin = ui.dp(10);
        card.addView(retry, p);
        Button support = ui.ghostButton("گزارش پشتیبانی", ui.goldAccent, v -> showSupportLog());
        LinearLayout.LayoutParams p2 = ui.lp(-1, -2);
        p2.topMargin = ui.dp(8);
        card.addView(support, p2);
        contentHost.removeAllViews();
        ScrollView sc = ui.scroll();
        LinearLayout col = ui.column();
        ui.pad(col);
        col.addView(card, ui.lp(-1, -2));
        sc.addView(col);
        contentHost.addView(sc, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    public void back() {
        if (stack.size() > 1) {
            stack.pop();
            renderTop(false);
        } else if (!TAB_HOME.equals(tab)) {
            showTab(TAB_HOME, true);
        } else {
            finish();
        }
    }

    /**
     * Android 13+ (and required from targetSdk 36) delivers Back through OnBackInvokedCallback; the
     * callback is registered only while there is somewhere to go back to, so on the home screen the
     * system plays its own "back to home" animation. The same pattern is used by MainActivity.
     */
    private void updateBackCallback() {
        if (Build.VERSION.SDK_INT < 33) return;
        try {
            boolean needed = !showingLogin && (stack.size() > 1 || !TAB_HOME.equals(tab));
            android.window.OnBackInvokedDispatcher d = getOnBackInvokedDispatcher();
            if (needed && backCallback == null) {
                android.window.OnBackInvokedCallback cb = this::back;
                d.registerOnBackInvokedCallback(android.window.OnBackInvokedDispatcher.PRIORITY_DEFAULT, cb);
                backCallback = cb;
            } else if (!needed && backCallback != null) {
                d.unregisterOnBackInvokedCallback((android.window.OnBackInvokedCallback) backCallback);
                backCallback = null;
            }
        } catch (Throwable ignored) { }
    }

    @Override public void onBackPressed() {
        if (showingLogin) {
            finish();
            return;
        }
        back();
    }

    /** Refresh everything with fresh keys (explicit user action). */
    public void reload() {
        stack.clear();
        showTab(tab, true);
        loadServerDate();
        probeConnection();
    }

    public void showLoading(String what) {
        if (contentHost == null || ui == null) return;
        contentHost.removeAllViews();
        ScrollView sc = ui.scroll();
        LinearLayout col = ui.column();
        sc.addView(col);
        ui.pad(col);
        col.addView(loadingCard(what), ui.lp(-1, -2));
        contentHost.addView(sc, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    private LinearLayout loadingCard(String what) {
        LinearLayout card = ui.card();
        card.addView(ui.text("⏳  در حال دریافت " + what + "…", 12.5f, ui.textDim, false), ui.lp(-1, -2));
        return card;
    }

    // ==================================================================== live status

    private void loadServerDate() {
        if (db == null) return;
        db.read(key("clock"), 120_000L, c -> {
            JSONArray a = new JSONArray();
            JSONObject o = new JSONObject();
            o.put("today", FinQueries.serverToday(c));
            a.put(o);
            return a;
        }, env -> {
            JSONObject p = FinScreen.payload(env);
            String t = p.optString("today", "");
            if (!t.isEmpty()) {
                boolean changed = !t.equals(serverToday);
                serverToday = t;
                if (changed) {
                    String[] range = FinFmt.periodRange(periodKey, serverToday, null, null);
                    periodFrom = range[0];
                    periodTo = range[1];
                }
                hasCache = true;
                renderHeader(hasCache ? "متصل به سرور" : "بدون اتصال", false);
            }
        });
    }

    private void probeConnection() {
        if (db == null) return;
        db.pool().execute(() -> {
            JSONObject health = db.health();
            main.post(() -> {
                hasCache = health.optBoolean("ok", false);
                renderHeader(hasCache ? ("متصل · " + FinFmt.faNumber(health.optInt("ms", 0)) + "ms") : "آفلاین — آخرین نسخه", false);
            });
        });
    }

    public String clockLabel() {
        return serverToday == null ? "—" : serverToday;
    }

    private String key(String base) {
        return base + ":" + (periodFrom == null ? "" : periodFrom);
    }

    @Override protected void onResume() {
        super.onResume();
        FinScreen c = current;
        if (c != null && renderDone) {
            try { c.load(false); } catch (Throwable t) { FinCrash.log(this, "resume-load", t.getClass().getName()); }
        }
    }
}

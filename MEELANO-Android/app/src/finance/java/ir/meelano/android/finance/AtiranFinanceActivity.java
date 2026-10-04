package ir.meelano.android.finance;

import ir.meelano.android.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
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
 * Nothing here invents data. Screens load real rows through {@link FinQueries}; a value that has no
 * verified source in the Atiran schema is simply not displayed.
 */
public class AtiranFinanceActivity extends Activity {

    public static final String TAB_HOME = "home";
    public static final String TAB_MONEY = "money";
    public static final String TAB_CHECKS = "checks";
    public static final String TAB_RECEIVABLES = "receivables";
    public static final String TAB_MORE = "more";

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

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        FinApp.attach(this);
        if (!FinSession.isLoggedIn()) {
            buildLogin();
            return;
        }
        buildShell();
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

    private void buildLogin() {
        showingLogin = true;
        ui = new FinUi(this);
        db = new FinDb(this);
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

        TextView state = ui.text("", 12f, FinUi.DANGER, false);
        state.setPadding(0, ui.dp(8), 0, 0);
        card.addView(state, ui.lp(-1, -2));

        if (FinAuth.lockedOut(this)) {
            long left = FinAuth.lockRemainingMs(this) / 60000L + 1;
            state.setText("ورود موقتاً قفل است؛ " + FinFmt.faNumber(left) + " دقیقه دیگر تلاش کنید.");
        }

        Button login = ui.primaryButton("ورود به آتیران مالی", ui.goldAccent, null);
        LinearLayout.LayoutParams lp2 = ui.lp(-1, -2);
        lp2.topMargin = ui.dp(12);
        card.addView(login, lp2);

        Runnable attempt = () -> {
            String u = user.getText().toString().trim();
            String p = pass.getText().toString();
            if (u.isEmpty() || p.isEmpty()) {
                state.setText("نام کاربری و رمز عبور را وارد کنید.");
                return;
            }
            if (FinAuth.lockedOut(AtiranFinanceActivity.this)) {
                state.setText("ورود موقتاً قفل است. کمی بعد تلاش کنید.");
                return;
            }
            state.setTextColor(ui.textDim);
            state.setText("در حال بررسی روی سرور آتیران…");
            login.setEnabled(false);
            final String su = u, sp = p;
            db.pool().execute(() -> {
                FinAuth.Result res;
                try {
                    res = FinAuth.authenticate(AtiranFinanceActivity.this, db, su, sp);
                } catch (Exception e) {
                    res = new FinAuth.Result();
                    res.ok = false;
                    res.message = "خطای غیرمنتظره در ورود";
                }
                final FinAuth.Result r = res;
                main.post(() -> {
                    login.setEnabled(true);
                    if (r.ok && r.session != null) {
                        r.session.remember(AtiranFinanceActivity.this);
                        buildShell();
                    } else {
                        state.setTextColor(FinUi.DANGER);
                        state.setText(r.message == null || r.message.isEmpty() ? "ورود ناموفق بود." : r.message);
                        pass.setText("");
                    }
                });
            });
        };
        login.setOnClickListener(v -> attempt.run());
        pass.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) { attempt.run(); return true; }
            return false;
        });
        root.addView(card, ui.lp(-1, -2));

        TextView env = ui.text(FinEnv.describe(), 11f, ui.textFaint, false);
        env.setGravity(Gravity.CENTER);
        env.setPadding(0, ui.dp(14), 0, 0);
        root.addView(env, ui.lp(-1, -2));

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

    // ==================================================================== shell

    private void buildShell() {
        showingLogin = false;
        ui = new FinUi(this);
        db = new FinDb(this);
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
        if (headerBar == null) return;
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
        updateBackCallback();
        switch (target) {
            case TAB_MONEY: push(new FinScreenSales(this), false); break;
            case TAB_CHECKS: push(new FinScreenCheques(this), false); break;
            case TAB_RECEIVABLES: push(new FinScreenReceivables(this), false); break;
            case TAB_MORE: push(new FinScreenMore(this), false); break;
            default: push(new FinScreenHome(this), false); break;
        }
    }

    /** Drill-down: KPI → list → detail → source record. */
    public void open(FinScreen screen) {
        push(screen, true);
    }

    private void push(FinScreen screen, boolean animate) {
        stack.push(screen);
        current = screen;
        updateBackCallback();
        View v = screen.build();
        contentHost.removeAllViews();
        contentHost.addView(v, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        if (animate) {
            v.setAlpha(0f);
            v.setTranslationX(ui.dp(16));
            v.animate().alpha(1f).translationX(0f).setDuration(180).start();
        }
        screen.load(false);
    }

    public void back() {
        if (stack.size() > 1) {
            stack.pop();
            FinScreen top = stack.peek();
            current = top;
            View v = top.build();
            contentHost.removeAllViews();
            contentHost.addView(v, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            top.load(false);
        } else if (!TAB_HOME.equals(tab)) {
            showTab(TAB_HOME, true);
        } else {
            finish();
        }
        updateBackCallback();
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
        if (contentHost == null) return;
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
        if (c != null) c.load(false);
    }
}

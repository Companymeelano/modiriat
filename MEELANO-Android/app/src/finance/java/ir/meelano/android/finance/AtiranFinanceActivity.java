package ir.meelano.android.finance;

import ir.meelano.android.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
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
 *  • the shell: header with the live operator, role chip and connection state, plus the bottom
 *    navigation  خانه | مالی | چک‌ها | مطالبات | بیشتر , and a screen stack for drill-downs
 *    (KPI → list → detail → source record);
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

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
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
        ScrollView sc = ui.scroll();
        sc.addView(col);
        for (String[] p : pairs) {
            if (p == null || p.length < 2) continue;
            col.addView(kvLine(p[0], p[1]));
        }
        if (note != null && !note.isEmpty()) {
            TextView n = ui.text(note, 11.5f, ui.textDim, false);
            n.setPadding(0, ui.dp(8), 0, 0);
            col.addView(n);
        }
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setView(sc)
                .setPositiveButton("بستن", null)
                .show();
    }

    private View kvLine(String k, String v) {
        LinearLayout r = ui.row();
        r.setPadding(ui.dp(14), ui.dp(6), ui.dp(14), ui.dp(6));
        r.addView(ui.text(k, 12.5f, ui.textDim, false), ui.lp(0, -2, 1f));
        TextView t = ui.text(v, 13f, ui.textColor, true);
        t.setGravity(Gravity.END);
        r.addView(t, ui.lp(0, -2, 1f));
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
        root.setBackgroundColor(ui.bg);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(ui.dp(20), ui.dp(28), ui.dp(20), ui.dp(20));

        root.addView(ui.image(R.drawable.fin_login_logo), ui.lp(ui.dp(148), ui.dp(148)));
        TextView name = ui.text("آتیران مالی", 24f, ui.goldAccent, true);
        name.setGravity(Gravity.CENTER);
        root.addView(name, ui.lp(-1, -2));
        TextView sub = ui.text("مرکز عملیات مالی، خزانه، مطالبات و مغایرت", 12.5f, ui.textDim, false);
        sub.setGravity(Gravity.CENTER);
        root.addView(sub, ui.lp(-1, -2));
        addSpace(root, 18);

        LinearLayout card = ui.card();
        card.addView(ui.sectionTitle("ورود", "با حساب واقعی آتیران", "🔐"), ui.lp(-1, -2));

        JSONObject last = FinSession.lastUser(this);
        EditText user = ui.field(getString(R.string.fin_username));
        user.setInputType(InputType.TYPE_CLASS_TEXT);
        String lastUser = last.optString("username", "");
        if (!lastUser.isEmpty()) user.setText(lastUser);
        card.addView(user, ui.lp(-1, -2));

        EditText pass = ui.field(getString(R.string.fin_password));
        pass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        LinearLayout.LayoutParams pp = ui.lp(-1, -2);
        pp.topMargin = ui.dp(8);
        card.addView(pass, pp);

        TextView state = ui.text("", 12f, FinUi.DANGER, false);
        state.setPadding(0, ui.dp(8), 0, 0);
        card.addView(state, ui.lp(-1, -2));

        if (FinAuth.lockedOut(this)) {
            long left = FinAuth.lockRemainingMs(this) / 60000L + 1;
            state.setText("ورود موقتاً قفل است؛ " + left + " دقیقه دیگر تلاش کنید.");
        }

        Button login = ui.button("ورود", ui.goldAccent, true, null);
        LinearLayout.LayoutParams lp2 = ui.lp(-1, -2);
        lp2.topMargin = ui.dp(10);
        card.addView(login, lp2);
        login.setOnClickListener(v -> {
            String u = user.getText().toString().trim();
            String p = pass.getText().toString();
            if (u.isEmpty() || p.isEmpty()) {
                state.setText("نام کاربری و رمز عبور را وارد کنید.");
                return;
            }
            if (FinAuth.lockedOut(this)) {
                state.setText("ورود موقتاً قفل است. کمی بعد تلاش کنید.");
                return;
            }
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
                        state.setText(r.message == null || r.message.isEmpty() ? "ورود ناموفق بود." : r.message);
                        pass.setText("");
                    }
                });
            });
        });
        root.addView(card, ui.lp(-1, -2));

        TextView env = ui.text(FinEnv.describe(), 11f, ui.textFaint, false);
        env.setGravity(Gravity.CENTER);
        root.addView(env, ui.lp(-1, -2));

        ScrollView sc = ui.scroll();
        sc.addView(root);
        setContentView(sc);
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
        navBar.setBackgroundColor(ui.surface);
        navBar.setPadding(ui.dp(6), ui.dp(6), ui.dp(6), ui.dp(6));
        root.addView(navBar, ui.lp(-1, -2));

        setContentView(root);
        renderHeader("… در حال اتصال", true);
        renderNav();
        showTab(TAB_HOME, true);
        loadServerDate();
        probeConnection();
    }

    /** The header carries the operator identity, the real role and the connection state. */
    public void renderHeader(String connectionNote, boolean loading) {
        if (headerBar == null) return;
        headerBar.removeAllViews();
        headerBar.setBackgroundColor(ui.surface);
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
        meta.addView(ui.chip("تاریخ سرور: " + fa(serverToday == null ? "…" : serverToday), dateColor()), ui.lp(-2, -2));
        meta.addView(ui.spacer(6), ui.lp(ui.dp(6), -2));
        meta.addView(ui.chip("بازه: " + FinFmt.periodLabel(periodKey), ui.goldAccent), ui.lp(-2, -2));
        View filler = ui.spacer(1);
        meta.addView(filler, ui.lp(0, -2, 1f));
        meta.addView(ui.chip("نسخه " + FinSession.appVersion(), ui.textFaint), ui.lp(-2, -2));
        headerBar.addView(meta, ui.lp(-1, -2));
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
            cell.setPadding(0, ui.dp(8), 0, ui.dp(8));
            cell.setBackground(ui.rounded(active ? FinUi.mix(ui.surface, ui.goldAccent, 0.18f) : ui.surface,
                    12, active ? ui.goldAccent : 0, 1));
            TextView g = ui.text(t[2], 15f, active ? ui.goldAccent : ui.textFaint, true);
            g.setGravity(Gravity.CENTER);
            TextView l = ui.text(t[1], 11f, active ? ui.textColor : ui.textDim, active);
            l.setGravity(Gravity.CENTER);
            cell.addView(g, ui.lp(-1, -2));
            cell.addView(l, ui.lp(-1, -2));
            cell.setOnClickListener(v -> {
                if (!t[0].equals(tab)) showTab(t[0], true);
            });
            cell.setClickable(true);
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
        View v = screen.build();
        contentHost.removeAllViews();
        contentHost.addView(v, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
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
                renderHeader(hasCache ? ("متصل · " + health.optInt("ms", 0) + "ms") : "بدون اتصال", false);
                FinScreen c = current;
                if (c != null && !hasCache && !db.lastError().isEmpty()) {
                    // A failed probe does not overwrite the screen with fake numbers; the screen shows
                    // the offline card through its own stale envelope.
                    renderHeader("آفلاین — آخرین نسخه", false);
                }
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

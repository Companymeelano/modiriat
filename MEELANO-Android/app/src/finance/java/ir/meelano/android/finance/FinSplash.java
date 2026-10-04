package ir.meelano.android.finance;

import android.app.Activity;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.List;

/**
 * «آتیران مالی» — the startup screen: health and data check, in the brand's own visual language.
 *
 * It replaces the plain technical panel of 1.1.1 as the *normal* first screen (that panel stays as
 * the fallback). What the operator sees and what actually happens are the same thing:
 *
 *   1. آماده‌سازی رابط کاربری      — the theme, fonts and widget kit are ready
 *   2. اینترنت گوشی                — the phone really has a validated connection
 *   3. یافتن نام سرور (DNS)        — the server name resolves
 *   4. پورت سرور ۱۴۳۳              — the SQL port accepts a socket
 *   5. ورود به SQL Server          — the database accepts this application's login
 *   6. پایگاه داده و تاریخ سرور    — DB_NAME() and the server's own Jalali date answer
 *   7. جدول‌های مالی               — the finance tables themselves answer a sample read
 *
 * Each line fills in with its own duration, the bar shows the overall percentage, and a failure is
 * shown in red with the exact reason plus a retry — never a blank screen and never a silent wait.
 */
final class FinSplash {

    interface Listener {
        /** Every check passed: the caller continues to the sign-in screen or the desk. */
        void splashReady();
        void splashRetry();
        void splashPlainLogin();
        void splashDiagnostics();
        /** Copies the full support bundle (last crash + the steps that led to it). */
        void splashCopyReport();
    }

    private final Activity a;
    private final FinUi ui;
    private final FinDb db;
    private final Listener listener;

    private final ScrollView root;
    private final LinearLayout column;
    private final FinCharts.Progress progress;
    private final LinearLayout rowsBox;
    private final TextView status;
    private final LinearLayout actions;
    private final Button enter, retry, plain, doctor;

    private final List<FinHealth.Check> checks = FinHealth.checklist();
    private LinearLayout crashCard;
    private final LinearLayout[] rows = new LinearLayout[checks.size()];
    private final TextView[] glyphs = new TextView[checks.size()];
    private final TextView[] titles = new TextView[checks.size()];
    private final TextView[] details = new TextView[checks.size()];

    private boolean finished;
    private int startToken;
    private Thread walk;

    FinSplash(Activity activity, FinUi ui, FinDb db, Listener listener) {
        this.a = activity;
        this.ui = ui;
        this.db = db;
        this.listener = listener;

        column = ui.column();
        column.setBackground(ui.gradient(ui.bg, FinUi.mix(ui.bg, ui.goldAccent, 0.16f), 0));
        column.setGravity(Gravity.CENTER_HORIZONTAL);
        column.setPadding(ui.dp(20), ui.dp(26), ui.dp(20), ui.dp(22));

        FinCharts.Logo logo = new FinCharts.Logo(a, ui.goldAccent, ui.silver, ui.surface, ui.bg);
        LinearLayout.LayoutParams logoLp = ui.lp(ui.dp(96), ui.dp(96));
        logoLp.gravity = Gravity.CENTER_HORIZONTAL;
        column.addView(logo, logoLp);

        TextView title = ui.text("آتیران مالی", 23f, ui.goldAccent, true);
        title.setGravity(Gravity.CENTER);
        column.addView(title, ui.lp(-1, -2));

        TextView sub = ui.text("بررسی سلامت سیستم و بارگذاری اطلاعات", 12.5f, ui.textDim, false);
        sub.setGravity(Gravity.CENTER);
        column.addView(sub, ui.lp(-1, -2));

        progress = new FinCharts.Progress(a, FinUi.mix(ui.surface2, ui.goldAccent, 0.10f),
                ui.goldSoft, ui.goldAccent, FinUi.bestOn(ui.goldAccent), ui.boldFace());
        LinearLayout.LayoutParams pp = ui.lp(-1, ui.dp(26));
        pp.topMargin = ui.dp(16);
        column.addView(progress, pp);
        progress.set(0f, "۰٪");

        LinearLayout card = ui.card();
        LinearLayout.LayoutParams cp = ui.lp(-1, -2);
        cp.topMargin = ui.dp(12);
        column.addView(card, cp);
        card.addView(ui.sectionTitle("بررسی گام‌به‌گام", "هر خط، یک بررسی واقعی روی گوشی و سرور", "⚕"), ui.lp(-1, -2));

        crashCard = previousCrashCard();
        if (crashCard != null) {
            LinearLayout.LayoutParams crp = ui.lp(-1, -2);
            crp.topMargin = ui.dp(12);
            column.addView(crashCard, crp);
        }

        rowsBox = ui.column();
        card.addView(rowsBox, ui.lp(-1, -2));
        for (int i = 0; i < checks.size(); i++) {
            rowsBox.addView(buildRow(i), ui.lp(-1, -2));
        }

        status = ui.text("در حال بررسی…", 12f, ui.textDim, false);
        status.setPadding(0, ui.dp(10), 0, 0);
        card.addView(status, ui.lp(-1, -2));

        actions = ui.column();
        LinearLayout.LayoutParams ap = ui.lp(-1, -2);
        ap.topMargin = ui.dp(12);
        column.addView(actions, ap);

        enter = ui.primaryButton("ورود به برنامه", ui.goldAccent, v -> { if (listener != null) listener.splashReady(); });
        enter.setVisibility(View.GONE);
        actions.addView(enter, ui.lp(-1, -2));

        retry = ui.ghostButton("تلاش دوباره", ui.goldAccent, v -> { if (listener != null) listener.splashRetry(); });
        retry.setVisibility(View.GONE);
        LinearLayout.LayoutParams rp = ui.lp(-1, -2);
        rp.topMargin = ui.dp(8);
        actions.addView(retry, rp);

        doctor = ui.ghostButton("بررسی اتصال به سرور", ui.goldAccent, v -> { if (listener != null) listener.splashDiagnostics(); });
        doctor.setVisibility(View.GONE);
        LinearLayout.LayoutParams dp = ui.lp(-1, -2);
        dp.topMargin = ui.dp(8);
        actions.addView(doctor, dp);

        plain = ui.ghostButton("ورود با فرم ساده", ui.textDim, v -> { if (listener != null) listener.splashPlainLogin(); });
        plain.setVisibility(View.GONE);
        LinearLayout.LayoutParams lp = ui.lp(-1, -2);
        lp.topMargin = ui.dp(8);
        actions.addView(plain, lp);

        TextView env = ui.text(FinEnv.describe() + " · نسخه " + FinSession.appVersion(), 11f, ui.textFaint, false);
        env.setGravity(Gravity.CENTER);
        env.setPadding(0, ui.dp(14), 0, 0);
        column.addView(env, ui.lp(-1, -2));

        root = ui.scroll();
        root.setFillViewport(false);
        root.addView(column);
    }

    View view() { return root; }

    /** Starts (or restarts) the whole walk. */
    void start() {
        finished = false;
        enter.setVisibility(View.GONE);
        retry.setVisibility(View.GONE);
        doctor.setVisibility(View.GONE);
        plain.setVisibility(View.GONE);
        status.setTextColor(ui.textDim);
        status.setText("در حال بررسی…");
        progress.set(0f, "۰٪");
        for (int i = 0; i < checks.size(); i++) paint(i, FinHealth.PENDING);
        if (walk != null) walk.interrupt();
        final int token = ++startToken;
        // If a check is slow (a network that answers late), the operator is never trapped: after ten
        // seconds the manual entries appear while the walk keeps running.
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            if (token != startToken || finished) return;
            doctor.setVisibility(View.VISIBLE);
            plain.setVisibility(View.VISIBLE);
            status.setTextColor(FinUi.WARNING);
            status.setText("بررسی طول کشیده است؛ می‌توانید همین حالا وارد شوید یا گزارش بگیرید.");
        }, 10_000L);
        walk = FinHealth.run(a, db, checks, new FinHealth.Listener() {
            @Override public void onCheckStart(int index, FinHealth.Check check) {
                paint(index, FinHealth.RUNNING);
                status.setText("در حال بررسی: " + check.title);
            }
            @Override public void onCheckDone(int index, FinHealth.Check check) {
                paint(index, check.state);
                int done = 0;
                for (FinHealth.Check c : checks) if (c.state == FinHealth.OK || c.state == FinHealth.WARN
                        || c.state == FinHealth.FAIL) done++;
                float ratio = checks.isEmpty() ? 1f : done / (float) checks.size();
                progress.set(ratio, FinFmt.faNumber(Math.round(ratio * 100)) + "٪");
            }
            @Override public void onChecksFinished(boolean ok, int doneIndex) {
                finished = true;
                if (ok) {
                    status.setTextColor(FinUi.SUCCESS);
                    status.setText("همه‌چیز سالم است — اطلاعات آمادهٔ نمایش است.");
                    enter.setVisibility(View.VISIBLE);
                } else {
                    status.setTextColor(FinUi.DANGER);
                    status.setText("بررسی ناتمام ماند: " + failureText());
                    retry.setVisibility(View.VISIBLE);
                    doctor.setVisibility(View.VISIBLE);
                    plain.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    boolean isFinished() { return finished; }

    /** The first failed check, with its real reason. */
    private String failureText() {
        for (FinHealth.Check c : checks) {
            if (c.state == FinHealth.FAIL) return c.title + " — " + c.detail;
        }
        return "موردی ناموفق بود.";
    }

    /**
     * When the previous run ended with an uncaught exception, its reason is the first thing shown —
     * a phone in the field cannot be attached to a debugger, and "the app just closes" is not a
     * diagnosis. The card carries the exception text, the event code and a copy action.
     */
    private LinearLayout previousCrashCard() {
        String crash = FinCrash.lastCrash(a);
        if (crash == null || crash.trim().isEmpty()) return null;
        String[] lines = crash.trim().split("\n");
        StringBuilder reason = new StringBuilder();
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) continue;
            if (line.startsWith("==")) continue;                      // device/event-code header
            if (line.startsWith("\tat ")) continue;                   // stack frames
            if (reason.length() > 0) reason.append('\n');
            reason.append(line);
            if (reason.length() > 260) break;
        }
        LinearLayout card = ui.cardTone(FinUi.DANGER);
        card.addView(ui.text("⚠  اجرای قبلی برنامه با خطا بسته شد", 13f, ui.textColor, true), ui.lp(-1, -2));
        TextView body = ui.text(reason.toString(), 11f, ui.textDim, false);
        body.setMaxLines(9);
        card.addView(body, ui.lp(-1, -2));
        LinearLayout row = ui.row();
        row.addView(ui.ghostButton("کپی گزارش خطا", FinUi.DANGER, v -> {
            if (listener != null) listener.splashCopyReport();
        }), ui.lp(0, -2, 1f));
        LinearLayout.LayoutParams rp = ui.lp(-1, -2);
        rp.topMargin = ui.dp(8);
        card.addView(row, rp);
        card.addView(ui.text("همین متن را برای پشتیبانی بفرستید؛ با آن دقیقاً می‌دانیم کدام مرحله خطا داده است.",
                10.5f, ui.textFaint, false), ui.lp(-1, -2));
        return card;
    }

    // ------------------------------------------------------------------ rows

    private LinearLayout buildRow(int index) {
        LinearLayout row = ui.row();
        row.setPadding(0, ui.dp(6), 0, ui.dp(6));

        TextView glyph = ui.text("○", 13f, ui.textFaint, true);
        glyph.setGravity(Gravity.CENTER);
        row.addView(glyph, ui.lp(ui.dp(22), -2));

        TextView title = ui.text(checks.get(index).title, 12.5f, ui.textColor, true);
        row.addView(title, ui.lp(0, -2, 1f));

        TextView detail = ui.text("", 11f, ui.textFaint, false);
        detail.setGravity(Gravity.END);
        detail.setSingleLine(true);
        row.addView(detail, ui.lp(0, -2, 1.05f));

        rows[index] = row;
        glyphs[index] = glyph;
        titles[index] = title;
        details[index] = detail;
        return row;
    }

    private void paint(int index, int state) {
        if (index < 0 || index >= rows.length) return;
        int colour;
        switch (state) {
            case FinHealth.RUNNING: colour = ui.goldSoft; break;
            case FinHealth.OK: colour = FinUi.SUCCESS; break;
            case FinHealth.WARN: colour = FinUi.WARNING; break;
            case FinHealth.FAIL: colour = FinUi.DANGER; break;
            default: colour = ui.textFaint; break;
        }
        glyphs[index].setText(checks.get(index).glyph());
        glyphs[index].setTextColor(colour);
        titles[index].setTextColor(state == FinHealth.PENDING ? ui.textDim : ui.textColor);
        String detail = checks.get(index).detail;
        if (state == FinHealth.OK && checks.get(index).ms > 0) detail = FinFmt.faNumber(checks.get(index).ms) + "ms · " + detail;
        details[index].setText(detail == null ? "" : detail);
        details[index].setTextColor(state == FinHealth.FAIL ? FinUi.DANGER
                : state == FinHealth.WARN ? FinUi.WARNING : ui.textFaint);
        rows[index].setBackground(state == FinHealth.FAIL
                ? ui.rounded(FinUi.alpha(FinUi.DANGER, 28), 10, 0, 0)
                : null);
    }

    /** Full reason of the failing step, used by the caller for the support report. */
    String failingDetail() { return failureText(); }
}

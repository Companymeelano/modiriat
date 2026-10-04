package ir.meelano.android.finance;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.Locale;

/**
 * «آتیران مالی» — the first screen of the process, built only from plain platform widgets.
 *
 * Why it exists: a phone in the field cannot be attached to a debugger, and a screen that fails
 * before {@code setContentView} leaves nothing but the window colour. This panel is therefore set as
 * the content view in the very first milliseconds of {@code onCreate}, before any theme, font,
 * database or view-tree work, and it then narrates every following startup step. If a step throws or
 * hangs, the operator sees exactly which one, next to the real exception text — and the sign-in form
 * it carries keeps the app usable even when the rich UI cannot be built on that device.
 *
 * It uses the platform font, plain colours and no custom view, so nothing here can be the cause of a
 * blank screen.
 */
final class FinBoot {

    interface Listener {
        void bootLogin(String user, String pass);
        void bootDiagnostics();
        void bootSelfTest();
        void bootCopy();
        void bootContinue();
    }

    private static final int BG = 0xFF080B14;
    private static final int SURFACE = 0xFF101725;
    private static final int SURFACE2 = 0xFF162032;
    private static final int STROKE = 0xFF25334C;
    private static final int TEXT = 0xFFF2F5FA;
    private static final int DIM = 0xFF9BA8BF;
    private static final int FAINT = 0xFF6D7A93;
    private static final int GOLD = 0xFFD4AF37;
    private static final int DANGER = 0xFFD14343;
    private static final int SUCCESS = 0xFF2E9E5B;

    private final Activity a;
    private final Listener listener;
    private final float d;

    private final LinearLayout root;
    private final TextView status;
    private final TextView elapsing;
    private final ProgressBar bar;
    private final TextView logView;
    private final ScrollView logScroll;
    private LinearLayout tools;
    private final LinearLayout form;
    private final EditText user, pass;
    private final Button loginButton, continueButton;
    private final TextView message;

    private final StringBuilder log = new StringBuilder();

    FinBoot(Activity activity, Listener listener) {
        this.a = activity;
        this.listener = listener;
        this.d = activity.getResources().getDisplayMetrics().density;

        root = new LinearLayout(activity);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(BG);
        root.setPadding(dp(18), dp(22), dp(18), dp(16));

        TextView brand = new TextView(activity);
        brand.setText("آتیران مالی");
        brand.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24f);
        brand.setTextColor(GOLD);
        brand.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        brand.setTextDirection(View.TEXT_DIRECTION_RTL);
        brand.setGravity(Gravity.CENTER);
        root.addView(brand, lp(-1, -2));

        TextView sub = new TextView(activity);
        sub.setText("خزانه · مطالبات · مغایرت · POS");
        sub.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
        sub.setTextColor(DIM);
        sub.setTextDirection(View.TEXT_DIRECTION_RTL);
        sub.setGravity(Gravity.CENTER);
        root.addView(sub, lp(-1, -2));

        LinearLayout card = new LinearLayout(activity);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackground(round(SURFACE, 16, STROKE));
        card.setPadding(dp(14), dp(14), dp(14), dp(14));
        LinearLayout.LayoutParams cp = lp(-1, -2);
        cp.topMargin = dp(16);
        root.addView(card, cp);

        status = new TextView(activity);
        status.setText("در حال آماده‌سازی…");
        status.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f);
        status.setTextColor(TEXT);
        status.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        status.setTextDirection(View.TEXT_DIRECTION_RTL);
        card.addView(status, lp(-1, -2));

        elapsing = new TextView(activity);
        elapsing.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11.5f);
        elapsing.setTextColor(DIM);
        elapsing.setTextDirection(View.TEXT_DIRECTION_RTL);
        elapsing.setVisibility(View.GONE);
        card.addView(elapsing, lp(-1, -2));

        bar = new ProgressBar(activity, null, android.R.attr.progressBarStyleHorizontal);
        bar.setIndeterminate(true);
        LinearLayout.LayoutParams bp = lp(-1, dp(6));
        bp.topMargin = dp(10);
        card.addView(bar, bp);

        message = new TextView(activity);
        message.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12.5f);
        message.setTextColor(DANGER);
        message.setTextDirection(View.TEXT_DIRECTION_RTL);
        message.setVisibility(View.GONE);
        LinearLayout.LayoutParams mp = lp(-1, -2);
        mp.topMargin = dp(10);
        card.addView(message, mp);

        // ---- manual sign-in form: hidden while the rich UI can be built, one tap away otherwise
        form = new LinearLayout(activity);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setVisibility(View.GONE);

        user = field("نام کاربری", false);
        pass = field("رمز عبور", true);
        loginButton = button("ورود به آتیران مالی", GOLD, true);
        loginButton.setOnClickListener(v -> {
            String u = user.getText().toString().trim();
            String p = pass.getText().toString();
            if (u.isEmpty() || p.isEmpty()) {
                setMessage("نام کاربری و رمز عبور را وارد کنید.", true);
                return;
            }
            if (listener != null) listener.bootLogin(u, p);
        });
        LinearLayout.LayoutParams fp = lp(-1, -2);
        fp.topMargin = dp(10);
        form.addView(loginButton, fp);

        Button diag = button("بررسی اتصال به سرور", SURFACE2, false);
        diag.setOnClickListener(v -> { if (listener != null) listener.bootDiagnostics(); });
        LinearLayout.LayoutParams dp1 = lp(-1, -2);
        dp1.topMargin = dp(8);
        form.addView(diag, dp1);

        card.addView(form, lp(-1, -2));

        continueButton = button("ادامه با رابط گرافیکی برنامه", SURFACE2, false);
        continueButton.setOnClickListener(v -> { if (listener != null) listener.bootContinue(); });
        LinearLayout.LayoutParams cbp = lp(-1, -2);
        cbp.topMargin = dp(10);
        card.addView(continueButton, cbp);

        // ---- live log: what the app is doing right now
        logView = new TextView(activity);
        logView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10.5f);
        logView.setTextColor(FAINT);
        logView.setTextDirection(View.TEXT_DIRECTION_RTL);
        logView.setTypeface(android.graphics.Typeface.MONOSPACE);
        logView.setPadding(dp(10), dp(10), dp(10), dp(10));

        logScroll = new ScrollView(activity);
        logScroll.setBackground(round(SURFACE2, 12, STROKE));
        logScroll.addView(logView);
        LinearLayout.LayoutParams lp2 = lp(-1, 0, 1f);
        lp2.topMargin = dp(14);
        root.addView(logScroll, lp2);

        tools = new LinearLayout(activity);
        tools.setOrientation(LinearLayout.HORIZONTAL);
        tools.setGravity(Gravity.CENTER_VERTICAL);
        tools.setPadding(0, dp(10), 0, 0);
        tools.addView(small("آزمون فنی کامل", () -> { if (listener != null) listener.bootSelfTest(); }), lp(0, -2, 1f));
        tools.addView(small("کپی گزارش", () -> { if (listener != null) listener.bootCopy(); }), lp(0, -2, 1f));
        root.addView(tools, lp(-1, -2));

        // The technical trail stays hidden while everything works: the normal startup is the
        // designed health screen, and this panel is only the safety net behind it.
        logScroll.setVisibility(View.GONE);
        tools.setVisibility(View.GONE);
    }

    /** Shows the technical trail (log + tools); called when something needs explaining. */
    private void revealLog() {
        logScroll.setVisibility(View.VISIBLE);
        tools.setVisibility(View.VISIBLE);
    }

    View view() { return root; }

    // ------------------------------------------------------------------ narration

    /** Called *before* a risky step runs, so a hang always shows the step it hung in. */
    void step(String label) {
        status.setText("… " + label);
        status.setTextColor(TEXT);
        append("… " + label);
    }

    void ok(String label) {
        append("✓ " + label);
    }

    void note(String label) {
        append("• " + label);
    }

    void warn(String label, String detail) {
        append("▲ " + label + (detail == null || detail.isEmpty() ? "" : " — " + detail));
    }

    void fail(String label, Throwable t) {
        revealLog();
        String detail = t == null ? "" : t.getClass().getName() + ": " + String.valueOf(t.getMessage());
        append("✗ " + label + " — " + detail);
        status.setText("توقف در: " + label);
        status.setTextColor(DANGER);
        setMessage("برنامه در مرحلهٔ «" + label + "» متوقف شد.\n" + detail
                + "\nکد رویداد: " + FinCrash.eventCode(t), true);
    }

    void setMessage(String text, boolean error) {
        message.setVisibility(View.VISIBLE);
        message.setTextColor(error ? DANGER : SUCCESS);
        message.setText(text);
    }

    /** Login-flow sink used by the activity, so both UIs behave identically. */
    void setBusy(boolean busy) {
        bar.setVisibility(busy ? View.VISIBLE : View.GONE);
        loginButton.setEnabled(!busy);
        loginButton.setAlpha(busy ? 0.6f : 1f);
        if (busy) message.setVisibility(View.GONE);
    }

    void setStatus(String text) {
        status.setText(text);
        status.setTextColor(TEXT);
    }

    void setElapsed(int seconds) {
        elapsing.setVisibility(seconds <= 0 ? View.GONE : View.VISIBLE);
        if (seconds > 0) elapsing.setText("زمان سپری‌شده: " + FinFmt.faNumber(seconds) + " ثانیه");
    }

    void showLogin() {
        revealLog();
        form.setVisibility(View.VISIBLE);
        bar.setVisibility(View.GONE);
    }

    void setReport(String text) {
        append(text);
        setMessage("گزارش بررسی آماده است. با «کپی گزارش» می‌توانید آن را برای پشتیبانی بفرستید.", false);
    }

    void showCrash(String crash) {
        if (crash == null || crash.trim().isEmpty()) return;
        revealLog();
        append("—— اجرای قبلی با خطا پایان یافت ——");
        append(crash.trim());
        status.setText("اجرای قبلی کامل نشده بود؛ گزارش آن در پایین است.");
        status.setTextColor(DANGER);
    }

    String logText() { return log.toString(); }

    void append(String text) {
        if (text == null) return;
        log.append(text).append('\n');
        if (log.length() > 12000) log.delete(0, log.length() - 12000);
        logView.setText(log.toString());
        logScroll.post(() -> logScroll.fullScroll(View.FOCUS_DOWN));
    }

    // ------------------------------------------------------------------ tiny widget factory

    private EditText field(String hint, boolean password) {
        EditText e = new EditText(a);
        e.setHint(hint);
        e.setHintTextColor(FAINT);
        e.setTextColor(TEXT);
        e.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f);
        e.setTextDirection(View.TEXT_DIRECTION_RTL);
        e.setSingleLine(true);
        e.setBackground(round(SURFACE2, 12, STROKE));
        e.setPadding(dp(12), dp(12), dp(12), dp(12));
        if (password) {
            e.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            e.setTransformationMethod(PasswordTransformationMethod.getInstance());
        }
        LinearLayout.LayoutParams p = lp(-1, -2);
        p.topMargin = dp(8);
        form.addView(e, p);
        return e;
    }

    private Button button(String label, int accent, boolean primary) {
        Button b = new Button(a);
        b.setText(label);
        b.setAllCaps(false);
        b.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13.5f);
        b.setTextColor(primary ? Color.parseColor("#10131A") : TEXT);
        b.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        b.setBackground(round(primary ? accent : SURFACE2, 12, primary ? accent : STROKE));
        b.setMinHeight(dp(48));
        return b;
    }

    private Button small(String label, Runnable click) {
        Button b = button(label, SURFACE2, false);
        b.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11.5f);
        b.setMinHeight(dp(40));
        b.setOnClickListener(v -> click.run());
        return b;
    }

    private GradientDrawable round(int fill, float radiusDp, int stroke) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(fill);
        g.setCornerRadius(dp(radiusDp));
        if (stroke != 0) g.setStroke(Math.max(1, dp(1)), stroke);
        return g;
    }

    private LinearLayout.LayoutParams lp(int w, int h) { return new LinearLayout.LayoutParams(w, h); }

    private LinearLayout.LayoutParams lp(int w, int h, float weight) {
        return new LinearLayout.LayoutParams(w, h, weight);
    }

    private int dp(float v) { return Math.round(v * d); }

    @Override public String toString() {
        return String.format(Locale.US, "FinBoot(steps=%d)", log.length());
    }
}

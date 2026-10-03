package ir.meelano.android.finance;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.Locale;

/**
 * Visual language of «آتیران مالی».
 *
 * The project's Android code base is a native Java/View app with no UI library dependency, so this
 * kit follows Material 3 principles (tonal surfaces, generous spacing, 48dp touch targets, clear
 * hierarchy, and colour never used alone for status) with the same native toolkit as the rest of the
 * app. Every status carries a glyph and a word as well as a colour.
 *
 * Surfaces, strokes, text and the accent come from {@link FinTheme}, so the four enterprise themes
 * recolour the entire application; the semantic status colours below are identical in every theme.
 */
public final class FinUi {

    // ---- semantic colours (theme independent: the meaning of a colour never changes)
    public static final int SUCCESS = 0xFF2E9E5B;
    public static final int INFO    = 0xFF3B82F6;
    public static final int WARNING = 0xFFE08A1E;
    public static final int DANGER  = 0xFFD14343;
    public static final int MANAGER = 0xFF8B5CF6;
    public static final int MUTED   = 0xFF9BA8BF;

    private final Activity a;
    private final float density;
    private final FinTheme theme;
    private Typeface bold, regular;

    // ---- theme surfaces (set from FinTheme in the constructor)
    public final int bg, surface, surface2, surface3, stroke, textColor, textDim, textFaint, goldAccent, goldSoft, silver;

    public FinUi(Activity activity) {
        this.a = activity;
        this.density = activity.getResources().getDisplayMetrics().density;
        this.theme = FinTheme.get(activity);
        this.bg = theme.bg;
        this.surface = theme.surface;
        this.surface2 = theme.surface2;
        this.surface3 = theme.surface3;
        this.stroke = theme.stroke;
        this.textColor = theme.text;
        this.textDim = theme.textDim;
        this.textFaint = theme.textFaint;
        this.goldAccent = theme.gold;
        this.goldSoft = theme.goldSoft;
        this.silver = theme.silver;
        try {
            bold = Typeface.createFromAsset(activity.getAssets(), "fonts/Vazirmatn-Bold.ttf");
            regular = Typeface.createFromAsset(activity.getAssets(), "fonts/Vazirmatn-Regular.ttf");
        } catch (Exception e) {
            bold = Typeface.DEFAULT_BOLD;
            regular = Typeface.DEFAULT;
        }
    }

    public FinTheme theme() { return theme; }
    public Context ctx() { return a; }
    public int dp(float v) { return Math.round(v * density); }

    // ------------------------------------------------------------------ primitives

    public TextView text(String value, float sp, int color, boolean strong) {
        TextView t = new TextView(a);
        t.setText(value == null ? "" : value);
        t.setTextSize(sp);
        t.setTextColor(color);
        t.setTypeface(strong ? bold : regular);
        t.setTextDirection(View.TEXT_DIRECTION_RTL);
        t.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        t.setIncludeFontPadding(false);
        return t;
    }

    public LinearLayout row() {
        LinearLayout l = new LinearLayout(a);
        l.setOrientation(LinearLayout.HORIZONTAL);
        l.setGravity(Gravity.CENTER_VERTICAL);
        return l;
    }

    public LinearLayout column() {
        LinearLayout l = new LinearLayout(a);
        l.setOrientation(LinearLayout.VERTICAL);
        return l;
    }

    public LinearLayout.LayoutParams lp(int w, int h) { return new LinearLayout.LayoutParams(w, h); }
    public LinearLayout.LayoutParams lp(int w, int h, float weight) { return new LinearLayout.LayoutParams(w, h, weight); }

    public GradientDrawable rounded(int fill, float radiusDp, int strokeColor, float strokeDp) {
        GradientDrawable g = new GradientDrawable();
        g.setShape(GradientDrawable.RECTANGLE);
        g.setColor(fill);
        g.setCornerRadius(dp(radiusDp));
        if (strokeColor != 0) g.setStroke(Math.max(1, dp(strokeDp)), strokeColor);
        return g;
    }

    public GradientDrawable gradient(int from, int to, int radiusDp) {
        GradientDrawable g = new GradientDrawable(GradientDrawable.Orientation.TL_BR, new int[]{from, to});
        g.setCornerRadius(dp(radiusDp));
        return g;
    }

    /** Soft tonal card — the standard container. */
    public LinearLayout card() {
        LinearLayout c = column();
        c.setBackground(rounded(surface, 16, stroke, 1));
        c.setPadding(dp(14), dp(12), dp(14), dp(12));
        return c;
    }

    public LinearLayout cardTone(int accent) {
        LinearLayout c = column();
        c.setBackground(rounded(mix(surface, accent, 0.10f), 16, mix(stroke, accent, 0.45f), 1));
        c.setPadding(dp(14), dp(12), dp(14), dp(12));
        return c;
    }

    public View spacer(int h) {
        View v = new View(a);
        v.setLayoutParams(lp(1, dp(h)));
        return v;
    }

    public View divider() {
        View v = new View(a);
        v.setBackgroundColor(stroke);
        v.setLayoutParams(lp(-1, Math.max(1, dp(0.7f))));
        return v;
    }

    // ------------------------------------------------------------------ components

    public TextView sectionTitle(String title, String subtitle, String glyph) {
        TextView t = text((glyph == null ? "" : glyph + "  ") + title + (subtitle == null || subtitle.isEmpty() ? "" : "   ·   " + subtitle),
                14.5f, textColor, true);
        t.setPadding(0, dp(4), 0, dp(8));
        return t;
    }

    public LinearLayout chip(String label, int color) {
        LinearLayout c = row();
        c.setBackground(rounded(mix(surface2, color, 0.22f), 20, mix(stroke, color, 0.5f), 1));
        c.setPadding(dp(9), dp(4), dp(9), dp(4));
        TextView t = text(label, 11f, readableOn(color), true);
        c.addView(t);
        return c;
    }

    /** Status chip: glyph + word + colour (never colour alone). */
    public LinearLayout statusChip(String status) {
        int color = statusColor(status);
        String glyph = statusGlyph(status);
        return chip(glyph + " " + (status == null ? "" : status), color);
    }

    public static int statusColor(String status) {
        if (status == null) return MUTED;
        String s = status.toLowerCase(Locale.US);
        if (s.contains("balanced") || s.contains("متوازن") || s.contains("cleared") || s.contains("وصول")
                || s.contains("confirmed") || s.contains("synced") || s.contains("تأیید") || s.contains("approved")
                || s.contains("normal") || s.contains("عادی") || s.contains("ok") || s.contains("closed") || s.contains("بسته")
                || s.contains("پاس") || s.contains("تسویه")) return SUCCESS;
        if (s.contains("attention") || s.contains("نیازمند") || s.contains("pending") || s.contains("در انتظار")
                || s.contains("needs") || s.contains("review") || s.contains("soon") || s.contains("نزدیک")
                || s.contains("بررسی") || s.contains("وعده")) return WARNING;
        if (s.contains("critical") || s.contains("بحرانی") || s.contains("overdue") || s.contains("سررسید گذشته")
                || s.contains("returned") || s.contains("برگشتی") || s.contains("difference") || s.contains("مغایرت")
                || s.contains("failed") || s.contains("error") || s.contains("معوق")) return DANGER;
        if (s.contains("manager") || s.contains("مدیریت")) return MANAGER;
        return INFO;
    }

    public static String statusGlyph(String status) {
        if (status == null) return "•";
        String s = status.toLowerCase(Locale.US);
        if (s.contains("balanced") || s.contains("متوازن") || s.contains("ok") || s.contains("synced")) return "✓";
        if (s.contains("attention") || s.contains("نیازمند") || s.contains("pending") || s.contains("در انتظار")) return "!";
        if (s.contains("critical") || s.contains("بحرانی") || s.contains("overdue") || s.contains("سررسید گذشته")
                || s.contains("معوق")) return "▲";
        if (s.contains("difference") || s.contains("مغایرت")) return "≠";
        if (s.contains("returned") || s.contains("برگشتی")) return "↩";
        return "•";
    }

    /** A KPI tile with value, unit, secondary line and a tap target for the drill-down. */
    public LinearLayout kpiTile(String title, String value, String unit, String note, int accent, View.OnClickListener tap) {
        LinearLayout c = column();
        c.setBackground(rounded(mix(surface, accent, 0.12f), 16, mix(stroke, accent, 0.55f), 1));
        c.setPadding(dp(12), dp(10), dp(12), dp(10));
        c.setMinimumHeight(dp(92));

        LinearLayout head = row();
        head.addView(text("◆", 11f, accent, true), lp(-2, -2));
        head.addView(text("  " + title, 11.5f, textDim, false), lp(0, -2, 1f));
        c.addView(head, lp(-1, -2));

        LinearLayout valueRow = row();
        TextView v = text(value, 19f, textColor, true);
        v.setSingleLine(true);
        v.setEllipsize(TextUtils.TruncateAt.END);
        valueRow.addView(v, lp(0, -2, 1f));
        if (unit != null && !unit.isEmpty()) valueRow.addView(text(" " + unit, 10.5f, textFaint, false), lp(-2, -2));
        c.addView(valueRow, lp(-1, -2));

        if (note != null && !note.isEmpty()) {
            TextView n = text(note, 10.5f, accent == SUCCESS ? SUCCESS : textFaint, false);
            n.setMaxLines(2);
            c.addView(n, lp(-1, -2));
        }
        if (tap != null) {
            c.setOnClickListener(tap);
            c.setClickable(true);
            c.setFocusable(true);
        }
        return c;
    }

    public Button button(String label, int accent, boolean primary, View.OnClickListener click) {
        Button b = new Button(a);
        b.setText(label);
        b.setAllCaps(false);
        b.setTypeface(primary ? bold : regular);
        b.setTextSize(13f);
        b.setTextColor(primary ? bestOn(accent) : textColor);
        b.setBackground(rounded(primary ? accent : surface2, 12, primary ? 0 : stroke, 1));
        b.setPadding(dp(14), dp(10), dp(14), dp(10));
        b.setMinimumHeight(dp(48));
        b.setOnClickListener(click);
        return b;
    }

    public EditText field(String hint) {
        EditText e = new EditText(a);
        e.setHint(hint);
        e.setHintTextColor(textFaint);
        e.setTextColor(textColor);
        e.setTextSize(15f);
        e.setTypeface(regular);
        e.setBackground(rounded(surface2, 12, stroke, 1));
        e.setPadding(dp(12), dp(12), dp(12), dp(12));
        e.setMinHeight(dp(48));
        return e;
    }

    public ImageView image(int resId) {
        ImageView iv = new ImageView(a);
        iv.setImageResource(resId);
        iv.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        return iv;
    }

    /** Table row: the first cell is the label column, the rest share the width. */
    public LinearLayout tableRow(String[] cells, float[] weights, boolean header, int accent) {
        LinearLayout r = row();
        r.setPadding(0, dp(header ? 6 : 5), 0, dp(header ? 6 : 5));
        if (header) r.setBackgroundColor(surface2);
        for (int i = 0; i < cells.length; i++) {
            float w = weights != null && i < weights.length ? weights[i] : 1f;
            TextView t = text(cells[i] == null ? "—" : cells[i], header ? 11f : 12f,
                    header ? textDim : (i == 0 ? textColor : silver), header);
            t.setMaxLines(2);
            t.setEllipsize(TextUtils.TruncateAt.END);
            r.addView(t, lp(0, -2, w));
        }
        return r;
    }

    public LinearLayout tableHeader(String[] cells) { return tableRow(cells, null, true, goldAccent); }

    /** A tappable list row with a title, a subtitle line and a trailing value + status. */
    public LinearLayout listRow(String title, String sub, String trailing, String status, int accent, View.OnClickListener tap) {
        LinearLayout r = row();
        r.setBackground(rounded(surface, 13, stroke, 1));
        r.setPadding(dp(12), dp(10), dp(12), dp(10));
        r.setMinimumHeight(dp(56));

        LinearLayout left = column();
        left.addView(text(title, 13.5f, textColor, true), lp(-1, -2));
        if (sub != null && !sub.isEmpty()) left.addView(text(sub, 11f, textDim, false), lp(-1, -2));
        r.addView(left, lp(0, -2, 1f));

        LinearLayout right = column();
        right.setGravity(Gravity.END);
        if (trailing != null && !trailing.isEmpty()) {
            TextView t = text(trailing, 13f, accent, true);
            t.setGravity(Gravity.END);
            right.addView(t, lp(-1, -2));
        }
        if (status != null && !status.isEmpty()) {
            LinearLayout chip = statusChip(status);
            LinearLayout.LayoutParams cp = lp(-2, -2);
            cp.topMargin = dp(3);
            right.addView(chip, cp);
        }
        r.addView(right, lp(-2, -2));
        if (tap != null) {
            r.setOnClickListener(tap);
            r.setClickable(true);
            r.setFocusable(true);
        }
        return r;
    }

    /** Simple bar chart drawn with plain views (no chart library in this code base). */
    public LinearLayout barChart(String[] labels, double[] values, int accent) {
        LinearLayout col = column();
        double max = 0;
        for (double v : values) max = Math.max(max, Math.abs(v));
        if (max <= 0) return col;
        for (int i = 0; i < labels.length && i < values.length; i++) {
            LinearLayout r = row();
            TextView lab = text(labels[i], 10.5f, textDim, false);
            lab.setSingleLine(true);
            r.addView(lab, lp(dp(78), -2));
            LinearLayout track = row();
            track.setBackground(rounded(surface2, 6, 0, 0));
            View bar = new View(a);
            bar.setBackground(rounded(accent, 6, 0, 0));
            int widthPct = (int) Math.max(2, Math.round(Math.abs(values[i]) / max * 100));
            track.addView(bar, lp(0, dp(10), widthPct));
            r.addView(track, lp(0, -2, 1f));
            TextView val = text(FinFmt.compact(values[i]), 10.5f, silver, true);
            val.setGravity(Gravity.END);
            val.setSingleLine(true);
            r.addView(val, lp(dp(76), -2));
            LinearLayout.LayoutParams rp = lp(-1, -2);
            rp.bottomMargin = dp(4);
            col.addView(r, rp);
        }
        return col;
    }

    public static int mix(int a, int b, float ratio) {
        float r = Math.max(0f, Math.min(1f, ratio));
        int ar = (a >> 16) & 0xFF, ag = (a >> 8) & 0xFF, ab = a & 0xFF;
        int br = (b >> 16) & 0xFF, bgc = (b >> 8) & 0xFF, bb = b & 0xFF;
        return 0xFF000000 | (Math.round(ar + (br - ar) * r) << 16) | (Math.round(ag + (bgc - ag) * r) << 8)
                | Math.round(ab + (bb - ab) * r);
    }

    /** Keeps text readable on a coloured chip. */
    public static int mixColor(int accent) {
        return mix(0xFFFFFFFF, accent, 0.35f);
    }

    /** Readable foreground for an accent background. */
    public static int bestOn(int accent) {
        int r = (accent >> 16) & 0xFF, g = (accent >> 8) & 0xFF, b = accent & 0xFF;
        double luma = (0.299 * r + 0.587 * g + 0.114 * b) / 255d;
        return luma > 0.62 ? 0xFF10131A : 0xFFF7F9FC;
    }

    public static int readableOn(int accent) {
        return mix(0xFFFFFFFF, accent, 0.28f);
    }

    public ScrollView scroll() {
        ScrollView s = new ScrollView(a);
        s.setFillViewport(true);
        s.setVerticalScrollBarEnabled(false);
        return s;
    }

    public void applySystemBars() {
        if (Build.VERSION.SDK_INT >= 21) {
            a.getWindow().setStatusBarColor(bg);
            a.getWindow().setNavigationBarColor(surface);
            if (Build.VERSION.SDK_INT >= 23 && theme.light) {
                a.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }
        }
        if (Build.VERSION.SDK_INT >= 30) {
            a.getWindow().setDecorFitsSystemWindows(true);
        }
    }

    public void pad(LinearLayout root) {
        root.setPadding(dp(14), dp(12), dp(14), dp(20));
    }

    public static int density(Context c) { return Math.round(c.getResources().getDisplayMetrics().density); }

    /** Large-font safety: never allow a single row to grow unbounded. */
    public static void cap(TextView t, int lines) {
        t.setMaxLines(lines);
        t.setEllipsize(TextUtils.TruncateAt.END);
    }

    public void weight(LinearLayout parent, View child, float w) {
        parent.addView(child, lp(0, -2, w));
    }

    public LinearLayout.LayoutParams withTop(View v, int topDp) {
        LinearLayout.LayoutParams p = lp(-1, -2);
        p.topMargin = dp(topDp);
        return p;
    }

    public int gold() { return goldAccent; }

    public int bg() { return bg; }

    public int surfaceColor() { return surface; }
}

package ir.meelano.android.finance;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.List;
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
 * Charts come from {@link FinCharts} and are created here so a screen never has to know about
 * colours, fonts or numbers.
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
    public Typeface boldFace() { return bold; }
    public Typeface regularFace() { return regular; }

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

    /** A card with a gentle two-stop tonal gradient — used for the KPI row and the hero header. */
    public LinearLayout gradientCard(int accent, float radius) {
        LinearLayout c = column();
        GradientDrawable g = new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                new int[]{mix(surface, accent, 0.16f), mix(surface2, accent, 0.05f)});
        g.setCornerRadius(dp(radius));
        g.setStroke(Math.max(1, dp(1)), mix(stroke, accent, 0.45f));
        c.setBackground(g);
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

    /** Number that shrinks to fit its tile instead of being cut on a small phone. */
    public static final class FitText extends TextView {
        private final float maxSp, minSp;
        private boolean adjusting = false;

        public FitText(Context c, float maxSp, float minSp) {
            super(c);
            this.maxSp = maxSp;
            this.minSp = Math.min(minSp, maxSp);
            setTextSize(maxSp);
        }

        @Override protected void onSizeChanged(int w, int h, int oldW, int oldH) {
            super.onSizeChanged(w, h, oldW, oldH);
            fit(w - getPaddingLeft() - getPaddingRight());
        }

        @Override protected void onTextChanged(CharSequence text, int start, int before, int count) {
            super.onTextChanged(text, start, before, count);
            fit(getWidth() - getPaddingLeft() - getPaddingRight());
        }

        private void fit(int width) {
            if (width <= 0 || adjusting) return;
            adjusting = true;
            try {
                float size = maxSp;
                setTextSize(size);
                // The shrink loop is bounded: on a device where the measured width never settles (a
                // pathological font scale, for instance) the text keeps the smallest step instead of
                // spinning during layout — a spinning layout pass is what makes an app look frozen.
                if (!isSingleLine()) {
                    for (int i = 0; i < 160 && size > minSp && getLineCount() > 2; i++) {
                        size -= 0.5f;
                        setTextSize(size);
                    }
                } else {
                    String value = getText() == null ? "" : getText().toString();
                    for (int i = 0; i < 160 && size > minSp && getPaint().measureText(value) > width; i++) {
                        size -= 0.5f;
                        setTextSize(size);
                    }
                }
            } catch (Exception ignored) {
            } finally {
                adjusting = false;
            }
        }
    }

    /** One-line figure that shrinks to fit instead of being cut. */
    public TextView fitText(String value, float maxSp, float minSp, int color) {
        FitText t = new FitText(a, maxSp, Math.min(maxSp, minSp));
        t.setText(value == null ? "" : value);
        t.setTextColor(color);
        t.setTypeface(bold);
        t.setIncludeFontPadding(false);
        t.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        t.setTextDirection(View.TEXT_DIRECTION_RTL);
        t.setSingleLine(true);
        t.setEllipsize(TextUtils.TruncateAt.END);
        return t;
    }

    /**
     * A KPI tile: title, big value, unit, note, and (optionally) a sparkline of the recent shape.
     * The whole tile is a 48dp+ touch target that opens its own drill-down.
     */
    public LinearLayout kpiTile(String title, String value, String unit, String note, int accent, View.OnClickListener tap) {
        return kpiTile(title, value, unit, note, accent, null, tap);
    }

    public LinearLayout kpiTile(String title, String value, String unit, String note, int accent, double[] trend, View.OnClickListener tap) {
        LinearLayout c = column();
        GradientDrawable g = new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                new int[]{mix(surface, accent, 0.15f), mix(surface2, accent, 0.03f)});
        g.setCornerRadius(dp(16));
        g.setStroke(Math.max(1, dp(1)), mix(stroke, accent, 0.5f));
        c.setBackground(g);
        c.setPadding(dp(12), dp(10), dp(12), dp(11));
        c.setMinimumHeight(dp(96));

        LinearLayout accentBar = row();
        View bar = new View(a);
        bar.setBackground(rounded(accent, 999, 0, 0));
        accentBar.addView(bar, lp(dp(22), dp(3)));
        c.addView(accentBar, lp(-1, -2));

        LinearLayout head = row();
        head.setPadding(0, dp(6), 0, 0);
        head.addView(text("◆", 10.5f, accent, true), lp(-2, -2));
        head.addView(text("  " + title, 11.5f, textDim, false), lp(0, -2, 1f));
        c.addView(head, lp(-1, -2));

        LinearLayout valueRow = row();
        valueRow.setPadding(0, dp(2), 0, 0);
        TextView v = fitText(value, 19f, 11.5f, textColor);
        valueRow.addView(v, lp(0, -2, 1f));
        if (unit != null && !unit.isEmpty()) valueRow.addView(text(" " + unit, 10.5f, textFaint, false), lp(-2, -2));
        c.addView(valueRow, lp(-1, -2));

        if (note != null && !note.isEmpty()) {
            TextView n = text(note, 10.5f, accent == SUCCESS ? SUCCESS : textFaint, false);
            n.setMaxLines(2);
            c.addView(n, lp(-1, -2));
        }
        if (trend != null && trend.length > 1) {
            FinCharts.Spark spark = new FinCharts.Spark(a, alpha(accent, 255));
            spark.data(trend);
            LinearLayout.LayoutParams slp = lp(-1, dp(26));
            slp.topMargin = dp(6);
            c.addView(spark, slp);
        }
        if (tap != null) {
            c.setOnClickListener(tap);
            c.setClickable(true);
            c.setFocusable(true);
            applyTouch(c);
            LinearLayout foot = row();
            foot.setGravity(Gravity.END);
            TextView more = text("مشاهده جزئیات ›", 10.5f, accent, true);
            more.setGravity(Gravity.END);
            foot.addView(more, lp(-1, -2));
            c.addView(foot, lp(-1, -2));
        }
        return c;
    }

    /** Compact metric line used inside cards (label · value · optional chip). */
    public LinearLayout miniStat(String label, String value, int accent) {
        LinearLayout r = row();
        r.setPadding(0, dp(5), 0, dp(5));
        r.addView(text(label, 12f, textDim, false), lp(0, -2, 1f));
        TextView v = text(value, 12.5f, mix(accent, textColor, 0.15f), true);
        v.setGravity(Gravity.END);
        r.addView(v, lp(0, -2, 1f));
        return r;
    }

    /** Gradient primary button: the main action of a screen. */
    public Button primaryButton(String label, int accent, View.OnClickListener click) {
        Button b = new Button(a);
        b.setText(label);
        b.setAllCaps(false);
        b.setTypeface(bold);
        b.setTextSize(13.5f);
        b.setTextColor(bestOn(accent));
        GradientDrawable g = new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                new int[]{mix(accent, android.graphics.Color.WHITE, 0.16f), accent});
        g.setCornerRadius(dp(14));
        b.setBackground(g);
        b.setPadding(dp(12), dp(10), dp(12), dp(10));
        b.setMinHeight(dp(50));
        b.setSingleLine(true);
        b.setEllipsize(TextUtils.TruncateAt.END);
        if (click != null) b.setOnClickListener(click);
        applyTouch(b);
        return b;
    }

    /** Outlined secondary button: everything that is not the main action. */
    public Button ghostButton(String label, int accent, View.OnClickListener click) {
        Button b = new Button(a);
        b.setText(label);
        b.setAllCaps(false);
        b.setTypeface(regular);
        b.setTextSize(13f);
        b.setTextColor(mix(accent, textColor, 0.25f));
        b.setBackground(rounded(mix(surface2, accent, 0.08f), 14, mix(stroke, accent, 0.55f), 1));
        b.setPadding(dp(12), dp(10), dp(12), dp(10));
        b.setMinHeight(dp(48));
        b.setSingleLine(true);
        b.setEllipsize(TextUtils.TruncateAt.END);
        if (click != null) b.setOnClickListener(click);
        applyTouch(b);
        return b;
    }

    /** Legacy button entry point kept for the screens that still call it. */
    public Button button(String label, int accent, boolean primary, View.OnClickListener click) {
        return primary ? primaryButton(label, accent, click) : ghostButton(label, accent, click);
    }

    /** Filter pill: filled when active, outlined when not; a segmented row is a row of these. */
    public TextView pillChip(String label, boolean active, int accent, View.OnClickListener click) {
        TextView t = text(label, 11.5f, active ? bestOn(accent) : mix(accent, textColor, 0.2f), active);
        t.setGravity(Gravity.CENTER);
        t.setSingleLine(true);
        t.setEllipsize(TextUtils.TruncateAt.END);
        t.setPadding(dp(10), dp(8), dp(10), dp(8));
        t.setBackground(active ? rounded(accent, 999, 0, 0) : rounded(mix(surface2, accent, 0.05f), 999, mix(stroke, accent, 0.4f), 1));
        t.setMinHeight(dp(38));
        if (click != null) t.setOnClickListener(click);
        applyTouch(t);
        return t;
    }

    /** A row of equal-weight segmented filters — the period selector of every report. */
    public LinearLayout segmented(String[] labels, int selectedIndex, int accent, OnPick pick) {
        LinearLayout row = row();
        for (int i = 0; i < labels.length; i++) {
            final int index = i;
            TextView cell = pillChip(labels[i], i == selectedIndex, accent, v -> pick.pick(index));
            LinearLayout.LayoutParams p = lp(0, -2, 1f);
            p.leftMargin = dp(i == 0 ? 0 : 3);
            p.rightMargin = dp(i == labels.length - 1 ? 0 : 3);
            row.addView(cell, p);
        }
        return row;
    }

    public interface OnPick { void pick(int index); }

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
        GradientDrawable g = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{mix(surface, accent, 0.05f), surface});
        g.setCornerRadius(dp(13));
        g.setStroke(Math.max(1, dp(1)), mix(stroke, accent, 0.28f));
        r.setBackground(g);
        r.setPadding(dp(12), dp(10), dp(12), dp(10));
        r.setMinimumHeight(dp(56));

        LinearLayout left = column();
        left.addView(text(title, 13.5f, textColor, true), lp(-1, -2));
        if (sub != null && !sub.isEmpty()) left.addView(text(sub, 11f, textDim, false), lp(-1, -2));
        r.addView(left, lp(0, -2, 1f));

        LinearLayout right = column();
        right.setGravity(Gravity.END);
        if (trailing != null && !trailing.isEmpty()) {
            TextView t = text(trailing, 13f, mix(accent, textColor, 0.2f), true);
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
            applyTouch(r);
        }
        return r;
    }

    /** Legend under a donut or beside a bar chart: colour · name · value · share. */
    public LinearLayout legend(List<FinCharts.Legend> items, double total) {
        LinearLayout col = column();
        if (items == null) return col;
        for (FinCharts.Legend item : items) {
            LinearLayout r = row();
            r.setPadding(0, dp(5), 0, dp(5));
            View dot = new View(a);
            dot.setBackground(rounded(item.color, 999, 0, 0));
            LinearLayout.LayoutParams dlp = lp(dp(11), dp(11));
            dlp.leftMargin = dp(2);
            dlp.rightMargin = dp(8);
            r.addView(dot, dlp);
            TextView name = text(item.label, 11.5f, textColor, true);
            name.setMaxLines(2);
            name.setEllipsize(TextUtils.TruncateAt.END);
            r.addView(name, lp(0, -2, 1f));
            TextView value = fitText(item.value, 11.5f, 9f, textDim);
            value.setGravity(Gravity.END);
            r.addView(value, lp(-2, -2));
            if (total > 0) {
                double share = 0;
                try {
                    share = Double.parseDouble(item.value.replace(",", "").replace(" ", "")) / total * 100d;
                } catch (Exception ignored) { }
                if (share > 0 && share <= 100) {
                    TextView pc = text(FinFmt.faNumber(Math.round(share) + "٪"), 10.5f, mix(item.color, textColor, 0.2f), true);
                    pc.setGravity(Gravity.CENTER);
                    pc.setBackground(rounded(alpha(item.color, 40), 999, 0, 0));
                    pc.setPadding(dp(7), dp(3), dp(7), dp(3));
                    LinearLayout.LayoutParams pp = lp(-2, -2);
                    pp.leftMargin = dp(6);
                    r.addView(pc, pp);
                }
            }
            col.addView(r, lp(-1, -2));
        }
        return col;
    }

    /** Legend plus donut side by side on wide screens, stacked on phones. */
    public LinearLayout donutWithLegend(FinCharts.Donut donut, java.util.List<FinCharts.Legend> items, double total, int donutDp) {
        LinearLayout wrap = row();
        int widthDp = 0;
        try { widthDp = a.getResources().getConfiguration().screenWidthDp; } catch (Exception ignored) { }
        boolean wide = widthDp >= 600;
        wrap.setOrientation(wide ? LinearLayout.HORIZONTAL : LinearLayout.VERTICAL);
        wrap.setGravity(Gravity.CENTER_VERTICAL);
        int size = dp(donutDp);
        LinearLayout.LayoutParams dlp = new LinearLayout.LayoutParams(size, size);
        dlp.gravity = Gravity.CENTER_HORIZONTAL;
        wrap.addView(donut, dlp);
        LinearLayout legendBox = legend(items, total);
        LinearLayout.LayoutParams llp = wide ? lp(0, -2, 1f) : lp(-1, -2);
        if (wide) llp.leftMargin = dp(12); else llp.topMargin = dp(10);
        wrap.addView(legendBox, llp);
        return wrap;
    }

    // ------------------------------------------------------------------ charts (created here so screens stay colour-free)

    /** Distinct but theme-consistent colours for chart segments. */
    public int[] palette() {
        return new int[]{goldAccent, SUCCESS, INFO, WARNING, MANAGER, DANGER, goldSoft, silver};
    }

    private int gridColor() { return mix(surface2, textColor, 0.14f); }

    public FinCharts.Area areaChart(FormatterKind kind) {
        FinCharts.Area chart = new FinCharts.Area(a, textColor, textDim, gridColor(), bold);
        chart.formatter(formatterFor(kind));
        return chart;
    }

    public FinCharts.Columns columnsChart(FormatterKind kind) {
        FinCharts.Columns chart = new FinCharts.Columns(a, textColor, textDim, gridColor(), bold);
        chart.formatter(formatterFor(kind));
        return chart;
    }

    public FinCharts.Bars barsChart(FormatterKind kind) {
        FinCharts.Bars chart = new FinCharts.Bars(a, textColor, textDim, gridColor(), bold);
        chart.formatter(formatterFor(kind));
        return chart;
    }

    public FinCharts.Donut donutChart(FormatterKind kind) {
        FinCharts.Donut chart = new FinCharts.Donut(a, textColor, textDim, gridColor(), bold);
        chart.formatter(formatterFor(kind));
        return chart;
    }

    public FinCharts.Ring ringChart() {
        return new FinCharts.Ring(a, textColor, textDim, gridColor(), bold);
    }

    public FinCharts.Spark spark(int accent) { return new FinCharts.Spark(a, accent); }

    public enum FormatterKind { MONEY, COUNT }

    public static FinCharts.Formatter formatterFor(FormatterKind kind) {
        return kind == FormatterKind.COUNT ? FinCharts.COUNT : FinCharts.COMPACT;
    }

    /** Kept for compatibility: the old view-based bar chart delegated to the real chart engine. */
    public View barChart(String[] labels, double[] values, int accent) {
        FinCharts.Bars bars = barsChart(FormatterKind.MONEY);
        bars.data(labels, values, null, null);
        bars.setLayoutParams(lp(-1, FinCharts.Bars.heightFor(labels == null ? 0 : labels.length, density)));
        return bars;
    }

    /** Fixed height (dp) for a bars chart with {@code rows} rows. */
    public int barsHeight(int rows) { return Math.max(dp(56), FinCharts.Bars.heightFor(rows, a.getResources().getDisplayMetrics().density)); }

    // ------------------------------------------------------------------ motion & misc

    /** Small press feedback, disabled automatically when the device asks for reduced motion. */
    public void applyTouch(View v) {
        if (v == null) return;
        v.setOnTouchListener((view, event) -> {
            if (event == null) return false;
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                view.animate().scaleX(0.975f).scaleY(0.975f).alpha(0.94f).setDuration(90).start();
            } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                view.animate().scaleX(1f).scaleY(1f).alpha(1f).setDuration(130).start();
            }
            return false;
        });
    }

    public static int mix(int a, int b, float ratio) {
        float r = Math.max(0f, Math.min(1f, ratio));
        int ar = (a >> 16) & 0xFF, ag = (a >> 8) & 0xFF, ab = a & 0xFF;
        int br = (b >> 16) & 0xFF, bgc = (b >> 8) & 0xFF, bb = b & 0xFF;
        return 0xFF000000 | (Math.round(ar + (br - ar) * r) << 16) | (Math.round(ag + (bgc - ag) * r) << 8)
                | Math.round(ab + (bb - ab) * r);
    }

    public static int alpha(int color, int amount) {
        return android.graphics.Color.argb(amount, android.graphics.Color.red(color),
                android.graphics.Color.green(color), android.graphics.Color.blue(color));
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

    /** Text size in sp that respects the user's font scale (used by the charts' labels). */
    public float sp(float value) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, value, a.getResources().getDisplayMetrics())
                / a.getResources().getDisplayMetrics().density;
    }
}

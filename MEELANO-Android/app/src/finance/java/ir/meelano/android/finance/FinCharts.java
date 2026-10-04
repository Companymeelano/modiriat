package ir.meelano.android.finance;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

/**
 * Native, dependency-free charting for «آتیران مالی».
 *
 * The four enterprise themes only change surfaces, strokes and the accent, so a chart receives its
 * colours from the active {@link FinTheme} and stays readable in all of them.
 *
 * What every chart guarantees:
 *  • it draws right-to-left, like the rest of the application (the newest/oldest order is decided by
 *    the caller, exactly as the tables show it);
 *  • it animates once when the data arrives and then rests — no per-frame redraw while the screen is
 *    idle, which matters because this app runs all day on an office phone or tablet;
 *  • it is tappable: the touched point is highlighted and its exact value is shown, and the same text
 *    is exposed to accessibility services through the content description;
 *  • a chart with no data draws an honest empty line instead of a fake bar.
 *
 * Amounts stay in Latin digits with the group separator (they are copied into Excel/PDF/reports),
 * counts use Persian digits — the same convention as the rest of the finance screens.
 */
public final class FinCharts {
    private FinCharts() { }

    /** Number → display text. */
    public interface Formatter { String format(double value); }

    /** Full rial amount with grouping: 1,234,567 — the format used everywhere else in the app. */
    public static final Formatter RIAL = FinFmt::amount;
    /** Short amount for axis labels: 1.2 میلیارد. */
    public static final Formatter COMPACT = FinFmt::compact;
    /** Whole count with Persian digits: ۱٬۲۴۰ style grouping through FinFmt.count. */
    public static final Formatter COUNT = v -> FinFmt.faNumber(FinFmt.count(Math.round(v)));

    /** Colour helpers shared with the screens. */
    public static int alpha(int color, int a) { return Color.argb(a, Color.red(color), Color.green(color), Color.blue(color)); }

    public static int mix(int from, int to, float ratio) {
        float r = Math.max(0f, Math.min(1f, ratio));
        return Color.rgb(
                Math.round(Color.red(from) + (Color.red(to) - Color.red(from)) * r),
                Math.round(Color.green(from) + (Color.green(to) - Color.green(from)) * r),
                Math.round(Color.blue(from) + (Color.blue(to) - Color.blue(from)) * r));
    }

    /** A legend entry: colour + name + value text, used under donuts and beside bars. */
    public static final class Legend {
        public final String label;
        public final String value;
        public final int color;
        public Legend(String label, String value, int color) { this.label = label; this.value = value; this.color = color; }
    }

    // ------------------------------------------------------------------ base

    /** Shared drawing base: theme colours, one entrance animation, RTL and text fitting. */
    abstract static class Base extends View {
        final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        final Paint text = new Paint(Paint.ANTI_ALIAS_FLAG);
        final int textColor, muted, grid;
        final float density;
        float progress = 0f;
        Formatter formatter = COMPACT;
        String emptyText = "دادهای برای نمایش وجود ندارد.";

        final Typeface bold;
        final Typeface regular;

        Base(Context c, int textColor, int muted, int grid, Typeface font) {
            super(c);
            this.textColor = textColor;
            this.muted = muted;
            this.grid = grid;
            this.density = c.getResources().getDisplayMetrics().density;
            this.bold = font != null ? Typeface.create(font, Typeface.BOLD) : Typeface.DEFAULT_BOLD;
            this.regular = font != null ? Typeface.create(font, Typeface.NORMAL) : Typeface.DEFAULT;
            text.setTypeface(regular);
            paint.setStrokeCap(Paint.Cap.ROUND);
        }

        Base formatter(Formatter f) { if (f != null) formatter = f; return this; }

        Base empty(String t) { if (t != null && !t.isEmpty()) emptyText = t; return this; }

        /** Starts the single entrance animation; called after the data is set. */
        void animateIn() {
            ValueAnimator a = ValueAnimator.ofFloat(0f, 1f);
            a.setDuration(760);
            a.setInterpolator(new DecelerateInterpolator(1.6f));
            a.addUpdateListener(v -> { progress = (float) v.getAnimatedValue(); invalidate(); });
            a.start();
        }

        float dp(float v) { return v * density; }

        /** Font scale from the view's own width so labels stay readable on a phone and a tablet. */
        float scale() { float w = getWidth(); return w <= 0 ? 1f : Math.max(0.88f, Math.min(1.22f, w / dp(340))); }

        String fit(String s, float available) {
            if (s == null) return "";
            if (available <= 0 || text.measureText(s) <= available) return s;
            float dots = text.measureText("…");
            int k = text.breakText(s, true, Math.max(0, available - dots), null);
            return k <= 0 ? "…" : s.substring(0, k).trim() + "…";
        }

        void drawEmpty(Canvas canvas) {
            text.setColor(muted);
            text.setTextSize(dp(11) * scale());
            text.setTextAlign(Paint.Align.CENTER);
            text.setFakeBoldText(false);
            canvas.drawText(emptyText, getWidth() / 2f, getHeight() / 2f, text);
        }

        double maxOf(double[] values) {
            double m = 0;
            for (double v : values) m = Math.max(m, Math.abs(v));
            return m;
        }

        /** Rounds an axis maximum up to a value whose thirds are round too. */
        static double niceMax(double v) {
            if (v <= 0) return 1;
            double e = Math.pow(10, Math.floor(Math.log10(v)));
            double f = v / e;
            double[] steps = {1.2, 1.5, 2, 3, 4.5, 6, 8, 9, 12};
            for (double st : steps) if (f <= st + 1e-9) return st * e;
            return 12 * e;
        }

        /** Monotone cubic tangents (Fritsch–Butland): the curve never overshoots below zero. */
        static float[] monotone(float[] y) {
            int n = y.length;
            float[] m = new float[n];
            if (n < 2) return m;
            float[] d = new float[n - 1];
            for (int i = 0; i < n - 1; i++) d[i] = y[i + 1] - y[i];
            m[0] = d[0];
            m[n - 1] = d[n - 2];
            for (int i = 1; i < n - 1; i++) m[i] = d[i - 1] * d[i] <= 0 ? 0f : 2f / (1f / d[i - 1] + 1f / d[i]);
            return m;
        }

        void announce(String message) { setContentDescription(message == null ? "" : message); }
    }

    // ------------------------------------------------------------------ brand emblem

    /**
     * The «آتیران مالی» emblem: a gold diamond ring around an ascending curve — the financial
     * language of the app (value over time) in the brand colours. It breathes once when attached.
     */
    public static final class Logo extends View {
        private final int gold, silver, surface, deep;
        private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final float density;
        private ValueAnimator anim;

        public Logo(Context c, int gold, int silver, int surface, int deep) {
            super(c);
            this.gold = gold;
            this.silver = silver;
            this.surface = surface;
            this.deep = deep;
            this.density = c.getResources().getDisplayMetrics().density;
            setContentDescription("نشان آتیران مالی");
        }

        @Override protected void onAttachedToWindow() {
            super.onAttachedToWindow();
            if (anim != null) return;
            anim = ValueAnimator.ofFloat(0f, 1f);
            anim.setDuration(900);
            anim.setInterpolator(new DecelerateInterpolator(1.4f));
            anim.addUpdateListener(v -> invalidate());
            anim.start();
        }

        @Override protected void onDetachedFromWindow() {
            if (anim != null) { anim.cancel(); anim = null; }
            super.onDetachedFromWindow();
        }

        @Override protected void onDraw(Canvas canvas) {
            float w = getWidth(), h = getHeight();
            if (w <= 0 || h <= 0) return;
            float t = anim == null ? 1f : (float) anim.getAnimatedValue();
            float s = Math.min(w, h);
            float cx = w / 2f, cy = h / 2f;
            p.setStyle(Paint.Style.FILL);
            p.setShader(new LinearGradient(0, 0, w, h, mix(surface, gold, 0.10f), mix(surface, silver, 0.06f), Shader.TileMode.CLAMP));
            canvas.drawRoundRect(new RectF(0, 0, w, h), s * 0.28f, s * 0.28f, p);
            p.setShader(null);

            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(Math.max(1.2f, s * 0.045f));
            p.setColor(alpha(gold, (int) (90 + 140 * t)));
            canvas.drawCircle(cx, cy, s * 0.345f, p);

            p.setStrokeWidth(Math.max(1f, s * 0.018f));
            p.setColor(alpha(mix(silver, gold, 0.35f), 200));
            RectF orbit = new RectF(cx - s * 0.40f, cy - s * 0.40f, cx + s * 0.40f, cy + s * 0.40f);
            canvas.drawArc(orbit, -90f + 40f * (1f - t), 120f, false, p);

            // Ascending value curve inside the ring.
            Path line = new Path();
            line.moveTo(cx - s * 0.20f, cy + s * 0.13f);
            line.cubicTo(cx - s * 0.08f, cy + s * 0.06f, cx - s * 0.02f, cy - s * 0.02f, cx + s * 0.06f, cy - s * 0.05f);
            line.cubicTo(cx + s * 0.12f, cy - s * 0.08f, cx + s * 0.15f, cy - s * 0.15f, cx + s * 0.21f, cy - s * 0.19f);
            p.setShader(null);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(Math.max(1.5f, s * 0.055f));
            p.setColor(silver);
            canvas.drawPath(line, p);
            p.setStrokeWidth(Math.max(1f, s * 0.022f));
            p.setColor(gold);
            canvas.drawPath(line, p);
            p.setStyle(Paint.Style.FILL);
            p.setColor(gold);
            canvas.drawCircle(cx + s * 0.21f, cy - s * 0.19f, Math.max(1.4f, s * 0.045f), p);

            // Two small columns: value stored next to value earned.
            p.setColor(alpha(mix(deep, silver, 0.5f), 210));
            canvas.drawRoundRect(new RectF(cx - s * 0.24f, cy - s * 0.02f, cx - s * 0.16f, cy + s * 0.17f), s * 0.02f, s * 0.02f, p);
            p.setColor(alpha(gold, 220));
            canvas.drawRoundRect(new RectF(cx - s * 0.12f, cy + s * 0.05f, cx - s * 0.04f, cy + s * 0.17f), s * 0.02f, s * 0.02f, p);
        }
    }

    // ------------------------------------------------------------------ area / line chart

    /**
     * Line/area chart with an optional second series, a value axis and a tap-to-inspect tip.
     * The first series is filled with a soft gradient, the second is drawn only as a line so the two
     * can be compared without hiding one behind the other.
     */
    public static final class Area extends Base {
        private String[] labels = new String[0];
        private double[][] series = new double[0][];
        private int[] colors = new int[0];
        private String[] names = new String[0];
        private int selected = -1;
        private float plotLeft = 0, plotRight = 0;
        private boolean dual;

        public Area(Context c, int textColor, int muted, int grid, Typeface font) { super(c, textColor, muted, grid, font); }

        public Area data(String[] labels, double[][] series, int[] colors, String[] names, boolean dual) {
            this.labels = labels == null ? new String[0] : labels;
            this.series = series == null ? new double[0][] : series;
            this.colors = colors == null ? new int[0] : colors;
            this.names = names == null ? new String[0] : names;
            this.dual = dual;
            this.selected = -1;
            StringBuilder cd = new StringBuilder();
            for (int i = 0; i < this.labels.length; i++) {
                cd.append(this.labels[i]);
                for (int sIdx = 0; sIdx < this.series.length; sIdx++) {
                    double[] row = this.series[sIdx];
                    if (row != null && i < row.length) cd.append(' ').append(formatter.format(row[i]));
                }
                cd.append("، ");
            }
            announce(cd.toString());
            animateIn();
            return this;
        }

        @Override public boolean onTouchEvent(MotionEvent e) {
            if (labels.length < 1 || plotRight <= plotLeft) return false;
            if (e.getAction() == MotionEvent.ACTION_DOWN || e.getAction() == MotionEvent.ACTION_MOVE) {
                int n = labels.length;
                float step = n > 1 ? (plotRight - plotLeft) / (n - 1) : 1f;
                int i = n > 1 ? Math.round((plotRight - e.getX()) / step) : 0;
                selected = Math.max(0, Math.min(n - 1, i));
                invalidate();
                return true;
            }
            return super.onTouchEvent(e);
        }

        @Override protected void onDraw(Canvas canvas) {
            float w = getWidth(), h = getHeight();
            if (w <= 0 || h <= 0) return;
            int n = labels.length;
            if (n == 0 || series.length == 0) { drawEmpty(canvas); return; }

            float sc = scale();
            double max = 0;
            for (double[] row : series) if (row != null) max = Math.max(max, maxOf(row));
            max = niceMax(max);

            float axis = dp(9.5f) * sc;
            text.setTypeface(regular);
            text.setTextSize(axis);
            text.setColor(muted);
            String[] yLabels = new String[4];
            float yw = 0;
            for (int g = 0; g <= 3; g++) {
                yLabels[g] = g == 3 ? "0" : formatter.format(max * (3 - g) / 3.0);
                text.setTypeface(regular);
                yw = Math.max(yw, text.measureText(yLabels[g]));
            }
            float left = Math.min(w * 0.30f, yw + dp(10)), right = w - dp(10);
            float tipH = dp(12) * sc + dp(12);
            float top = tipH + dp(10), bottom = h - (axis + dp(14));
            plotLeft = left; plotRight = right;
            if (bottom <= top) return;

            paint.setShader(null);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(Math.max(1f, dp(0.8f)));
            paint.setColor(grid);
            text.setTextAlign(Paint.Align.RIGHT);
            for (int g = 0; g <= 3; g++) {
                float y = top + (bottom - top) * g / 3f;
                canvas.drawLine(left, y, right, y, paint);
                canvas.drawText(yLabels[g], left - dp(5), y + axis * 0.35f, text);
            }

            float step = n > 1 ? (right - left) / (n - 1) : 0f;
            for (int sIdx = 0; sIdx < series.length; sIdx++) {
                double[] row = series[sIdx];
                if (row == null || row.length == 0) continue;
                int color = sIdx < colors.length ? colors[sIdx] : textColor;
                float[] xs = new float[n], ys = new float[n];
                for (int i = 0; i < n; i++) {
                    double v = i < row.length ? row[i] : 0;
                    xs[i] = n > 1 ? right - step * i : (left + right) / 2f;
                    ys[i] = bottom - (float) (Math.max(0, v) / max) * (bottom - top) * progress;
                }
                if (sIdx == 0) {
                    float[] m = monotone(ys);
                    Path line = new Path(), fill = new Path();
                    line.moveTo(xs[0], ys[0]);
                    fill.moveTo(xs[0], bottom);
                    fill.lineTo(xs[0], ys[0]);
                    for (int i = 0; i < n - 1; i++) {
                        float dx = (xs[i + 1] - xs[i]) / 3f;
                        float c1x = xs[i] + dx, c1y = Math.min(bottom, ys[i] + m[i] / 3f);
                        float c2x = xs[i + 1] - dx, c2y = Math.min(bottom, ys[i + 1] - m[i + 1] / 3f);
                        line.cubicTo(c1x, c1y, c2x, c2y, xs[i + 1], ys[i + 1]);
                        fill.cubicTo(c1x, c1y, c2x, c2y, xs[i + 1], ys[i + 1]);
                    }
                    fill.lineTo(xs[n - 1], bottom);
                    fill.close();
                    paint.setStyle(Paint.Style.FILL);
                    paint.setShader(new LinearGradient(0, top, 0, bottom, alpha(color, 120), alpha(color, 4), Shader.TileMode.CLAMP));
                    canvas.drawPath(fill, paint);
                    paint.setShader(null);
                    paint.setStyle(Paint.Style.STROKE);
                    paint.setStrokeWidth(dp(2.4f) * sc);
                    paint.setColor(color);
                    canvas.drawPath(line, paint);
                } else {
                    paint.setStyle(Paint.Style.STROKE);
                    paint.setStrokeWidth(dp(1.8f) * sc);
                    paint.setColor(color);
                    Path line = new Path();
                    line.moveTo(xs[0], ys[0]);
                    for (int i = 1; i < n; i++) line.lineTo(xs[i], ys[i]);
                    canvas.drawPath(line, paint);
                    paint.setStyle(Paint.Style.FILL);
                    for (int i = 0; i < n; i++) canvas.drawCircle(xs[i], ys[i], dp(2.4f) * sc, paint);
                }
                if (!dual || sIdx == 0) {
                    paint.setStyle(Paint.Style.STROKE);
                    paint.setColor(mix(color, Color.WHITE, 0.15f));
                    paint.setStrokeWidth(Math.max(2f, dp(3.4f) * sc));
                    paint.setStyle(Paint.Style.FILL);
                    paint.setColor(color);
                    canvas.drawCircle(xs[0], ys[0], dp(3.4f) * sc, paint);
                }
            }

            // X labels: as many as fit without touching each other.
            text.setTextSize(axis);
            text.setColor(muted);
            text.setTextAlign(Paint.Align.CENTER);
            text.setTypeface(regular);
            float lw = 0;
            for (String label : labels) lw = Math.max(lw, text.measureText(label));
            int fit = Math.max(2, (int) ((right - left) / (lw + dp(12))) + 1);
            int every = Math.max(1, (int) Math.ceil((n - 1) / (double) Math.max(1, fit - 1)));
            for (int i = 0; i < n; i += every) {
                String label = labels[i];
                float half = text.measureText(label) / 2f;
                float x = Math.max(half + dp(2), Math.min(w - half - dp(2), n > 1 ? right - step * i : (left + right) / 2f));
                canvas.drawText(label, x, h - dp(4), text);
            }

            // Selected (or highest) point tip.
            int sel = selected >= 0 ? selected : indexOfMax(series[0]);
            float sx = n > 1 ? right - step * sel : (left + right) / 2f;
            paint.setStyle(Paint.Style.FILL);
            for (int sIdx = 0; sIdx < series.length; sIdx++) {
                double[] row = series[sIdx];
                if (row == null || sel >= row.length) continue;
                int color = sIdx < colors.length ? colors[sIdx] : textColor;
                float y = bottom - (float) (Math.max(0, row[sel]) / max) * (bottom - top) * progress;
                paint.setColor(alpha(color, 60));
                canvas.drawCircle(sx, y, dp(7) * sc, paint);
                paint.setColor(color);
                canvas.drawCircle(sx, y, dp(3.6f) * sc, paint);
                paint.setColor(Color.WHITE);
                canvas.drawCircle(sx, y, dp(1.5f) * sc, paint);
            }

            StringBuilder tip = new StringBuilder(labels[sel]);
            for (int sIdx = 0; sIdx < series.length; sIdx++) {
                double[] row = series[sIdx];
                if (row == null || sel >= row.length) continue;
                if (sIdx < names.length && !names[sIdx].isEmpty()) tip.append(" · ").append(names[sIdx]);
                tip.append(' ').append(formatter.format(row[sel]));
            }
            text.setTextSize(dp(11) * sc);
            text.setColor(textColor);
            text.setTypeface(bold);
            String tipText = fit(tip.toString(), w - dp(26));
            float tw = text.measureText(tipText);
            float bx = Math.max(tw / 2 + dp(10), Math.min(w - tw / 2 - dp(10), sx));
            paint.setColor(alpha(colors.length > 0 ? colors[0] : textColor, 34));
            canvas.drawRoundRect(new RectF(bx - tw / 2 - dp(9), dp(2), bx + tw / 2 + dp(9), dp(2) + tipH), dp(11), dp(11), paint);
            canvas.drawText(tipText, bx, dp(2) + tipH / 2f + dp(11) * sc * 0.36f, text);
            text.setTypeface(regular);
        }

        private int indexOfMax(double[] values) {
            if (values == null || values.length == 0) return 0;
            int m = 0;
            for (int i = 1; i < values.length; i++) if (values[i] > values[m]) m = i;
            return m;
        }
    }

    // ------------------------------------------------------------------ grouped columns

    /** Grouped columns (one or two series per label) with rounded tops and a tap tip. */
    public static final class Columns extends Base {
        private String[] labels = new String[0];
        private double[][] series = new double[0][];
        private int[] colors = new int[0];
        private String[] names = new String[0];
        private int selected = -1;
        private float firstLeft = 0, firstWidth = 0, plotLeft = 0, plotRight = 0;

        public Columns(Context c, int textColor, int muted, int grid, Typeface font) { super(c, textColor, muted, grid, font); }

        public Columns data(String[] labels, double[][] series, int[] colors, String[] names) {
            this.labels = labels == null ? new String[0] : labels;
            this.series = series == null ? new double[0][] : series;
            this.colors = colors == null ? new int[0] : colors;
            this.names = names == null ? new String[0] : names;
            this.selected = -1;
            StringBuilder cd = new StringBuilder();
            for (int i = 0; i < this.labels.length; i++) {
                cd.append(this.labels[i]);
                for (double[] row : this.series) if (row != null && i < row.length) cd.append(' ').append(formatter.format(row[i]));
                cd.append("، ");
            }
            announce(cd.toString());
            animateIn();
            return this;
        }

        @Override public boolean onTouchEvent(MotionEvent e) {
            if (labels.length == 0 || firstWidth <= 0) return false;
            if (e.getAction() == MotionEvent.ACTION_DOWN || e.getAction() == MotionEvent.ACTION_MOVE) {
                int i = (int) ((e.getX() - firstLeft) / firstWidth);
                selected = Math.max(0, Math.min(labels.length - 1, i));
                // No requestDisallowInterceptTouchEvent: the chart still selects on tap, while a
                // vertical drag stays a scroll of the whole screen (long finance screens matter more).
                invalidate();
                return true;
            }
            return super.onTouchEvent(e);
        }

        @Override protected void onDraw(Canvas canvas) {
            float w = getWidth(), h = getHeight();
            if (w <= 0 || h <= 0) return;
            int n = labels.length;
            if (n == 0 || series.length == 0) { drawEmpty(canvas); return; }
            float sc = Math.min(1.05f, scale());
            double max = 0;
            for (double[] row : series) if (row != null) max = Math.max(max, maxOf(row));
            max = niceMax(max);
            float axis = dp(9.5f) * sc;

            // Legend line on top: which colour is which series.
            text.setTextSize(dp(10) * sc);
            text.setTypeface(bold);
            float lx = w - dp(2);
            for (int sIdx = series.length - 1; sIdx >= 0; sIdx--) {
                String name = sIdx < names.length && !names[sIdx].isEmpty() ? names[sIdx] : ("سری " + (sIdx + 1));
                float tw = text.measureText(name);
                int color = sIdx < colors.length ? colors[sIdx] : textColor;
                text.setColor(muted);
                canvas.drawText(name, lx - tw, dp(11), text);
                paint.setColor(color);
                canvas.drawCircle(lx - tw - dp(8), dp(7.5f), dp(3.4f), paint);
                lx -= tw + dp(22);
            }

            float top = dp(22), bottom = h - (axis + dp(12));
            if (bottom <= top) return;
            float yw = 0;
            text.setTypeface(regular);
            text.setTextSize(axis);
            String[] yLabels = new String[4];
            for (int g = 0; g <= 3; g++) {
                yLabels[g] = g == 3 ? "0" : formatter.format(max * (3 - g) / 3.0);
                yw = Math.max(yw, text.measureText(yLabels[g]));
            }
            float left = Math.min(w * 0.28f, yw + dp(8)), right = w - dp(6);
            plotLeft = left; plotRight = right;
            paint.setShader(null);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(Math.max(1f, dp(0.8f)));
            paint.setColor(grid);
            text.setColor(muted);
            text.setTextAlign(Paint.Align.RIGHT);
            for (int g = 0; g <= 3; g++) {
                float y = top + (bottom - top) * g / 3f;
                canvas.drawLine(left, y, right, y, paint);
                canvas.drawText(yLabels[g], left - dp(4), y + axis * 0.35f, text);
            }

            float slot = (right - left) / n;
            firstLeft = left; firstWidth = slot;
            float barW = Math.max(dp(6), (slot - dp(9)) / Math.max(1, series.length));
            for (int i = 0; i < n; i++) {
                float centre = right - slot * (i + 0.5f);
                float groupLeft = centre - (barW * series.length + dp(3) * (series.length - 1)) / 2f;
                for (int sIdx = 0; sIdx < series.length; sIdx++) {
                    double[] row = series[sIdx];
                    if (row == null || i >= row.length) continue;
                    int color = sIdx < colors.length ? colors[sIdx] : textColor;
                    float bh = (float) (Math.max(0, row[i]) / max) * (bottom - top) * progress;
                    float x = groupLeft + sIdx * (barW + dp(3));
                    RectF rect = new RectF(x, bottom - bh, x + barW, bottom);
                    paint.setShader(new LinearGradient(0, bottom - bh, 0, bottom, mix(color, Color.WHITE, 0.22f), color, Shader.TileMode.CLAMP));
                    paint.setStyle(Paint.Style.FILL);
                    canvas.drawRoundRect(rect, dp(5), dp(5), paint);
                    paint.setShader(null);
                }
                if (selected == i) {
                    paint.setColor(alpha(colors.length > 0 ? colors[0] : textColor, 34));
                    canvas.drawRoundRect(new RectF(centre - slot / 2f + dp(2), top, centre + slot / 2f - dp(2), bottom), dp(8), dp(8), paint);
                }
            }

            text.setTextSize(axis);
            text.setColor(muted);
            text.setTextAlign(Paint.Align.CENTER);
            float lw = 0;
            for (String label : labels) lw = Math.max(lw, text.measureText(label));
            int fit = Math.max(2, (int) ((right - left) / (lw + dp(10))) + 1);
            int every = Math.max(1, (int) Math.ceil((n - 1) / (double) Math.max(1, fit - 1)));
            for (int i = 0; i < n; i += every) {
                float centre = right - slot * (i + 0.5f);
                canvas.drawText(labels[i], centre, h - dp(3), text);
            }

            int sel = selected >= 0 ? selected : indexOfMax(series[0]);
            StringBuilder tip = new StringBuilder(labels[sel]);
            for (int sIdx = 0; sIdx < series.length; sIdx++) {
                double[] row = series[sIdx];
                if (row == null || sel >= row.length) continue;
                if (sIdx < names.length && !names[sIdx].isEmpty()) tip.append(" · ").append(names[sIdx]);
                tip.append(' ').append(formatter.format(row[sel]));
            }
            text.setTextSize(dp(10.5f) * sc);
            text.setTypeface(bold);
            text.setColor(textColor);
            String tipText = fit(tip.toString(), w - dp(18));
            float tw = text.measureText(tipText);
            float cx = Math.max(tw / 2 + dp(8), Math.min(w - tw / 2 - dp(8), right - slot * (sel + 0.5f)));
            paint.setColor(alpha(colors.length > 0 ? colors[0] : textColor, 30));
            canvas.drawRoundRect(new RectF(cx - tw / 2 - dp(8), top + dp(4), cx + tw / 2 + dp(8), top + dp(4) + dp(22) * sc), dp(10), dp(10), paint);
            canvas.drawText(tipText, cx, top + dp(4) + dp(22) * sc / 2f + dp(10.5f) * sc * 0.36f, text);
            text.setTypeface(regular);
        }

        private int indexOfMax(double[] values) {
            if (values == null || values.length == 0) return 0;
            int m = 0;
            for (int i = 1; i < values.length; i++) if (values[i] > values[m]) m = i;
            return m;
        }
    }

    // ------------------------------------------------------------------ horizontal bars

    /** Horizontal bars: name on the right, amount on the left, bars grow right → left. */
    public static final class Bars extends Base {
        private String[] labels = new String[0];
        private double[] values = new double[0];
        private int[] colors = new int[0];
        private String[] notes = new String[0];

        public Bars(Context c, int textColor, int muted, int grid, Typeface font) { super(c, textColor, muted, grid, font); }

        public Bars data(String[] labels, double[] values, int[] colors, String[] notes) {
            this.labels = labels == null ? new String[0] : labels;
            this.values = values == null ? new double[0] : values;
            this.colors = colors == null ? new int[0] : colors;
            this.notes = notes == null ? new String[0] : notes;
            StringBuilder cd = new StringBuilder();
            for (int i = 0; i < this.labels.length; i++) {
                cd.append(this.labels[i]).append(' ').append(formatter.format(i < this.values.length ? this.values[i] : 0)).append("، ");
            }
            announce(cd.toString());
            animateIn();
            return this;
        }

        /** Height that fits {@code rows} rows at the standard row height. */
        public static int heightFor(int rows, float density) { return (int) ((Math.max(1, rows) * 40 + 8) * density); }

        @Override protected void onDraw(Canvas canvas) {
            float w = getWidth();
            if (w <= 0) return;
            if (labels.length == 0) { drawEmpty(canvas); return; }
            float sc = Math.min(1.1f, scale());
            double max = maxOf(values);
            if (max <= 0) max = 1;

            // Reserve the widest value text so bars share one common start line.
            text.setTypeface(bold);
            text.setTextSize(dp(10.5f) * sc);
            float valueW = 0;
            for (int i = 0; i < labels.length; i++) valueW = Math.max(valueW, text.measureText(formatter.format(i < values.length ? values[i] : 0)));
            valueW = Math.min(valueW, w * 0.38f);
            float rowH = dp(40) * Math.min(1.1f, Math.max(0.86f, sc));
            for (int i = 0; i < labels.length; i++) {
                float y = i * rowH;
                int color = i < colors.length && colors[i] != 0 ? colors[i] : textColor;
                String value = formatter.format(i < values.length ? values[i] : 0);
                String note = i < notes.length ? notes[i] : null;

                text.setTypeface(bold);
                text.setTextSize(dp(11f) * sc);
                text.setColor(textColor);
                text.setTextAlign(Paint.Align.RIGHT);
                canvas.drawText(fit(labels[i], w - dp(6)), w - dp(2), y + rowH * 0.42f, text);

                text.setTextSize(dp(9.5f) * sc);
                text.setTypeface(regular);
                text.setColor(muted);
                if (note != null && !note.isEmpty()) canvas.drawText(fit(note, w * 0.55f), w - dp(2), y + rowH * 0.72f, text);

                text.setTextSize(dp(10.5f) * sc);
                text.setTypeface(bold);
                text.setColor(mix(color, textColor, 0.25f));
                text.setTextAlign(Paint.Align.LEFT);
                text.setTextSize(dp(10.5f) * sc);
                float shown = Math.min(valueW, w * 0.4f);
                canvas.drawText(fit(value, shown), dp(2), y + rowH * 0.42f, text);

                float barTop = y + rowH * 0.50f, barBottom = y + rowH * 0.86f;
                paint.setShader(null);
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(alpha(color, 26));
                canvas.drawRoundRect(new RectF(dp(2), barTop, w - dp(2), barBottom), dp(6), dp(6), paint);
                float track = w - dp(4);
                float len = (float) (Math.abs(i < values.length ? values[i] : 0) / max) * track * progress;
                if (len > 0.6f) {
                    paint.setShader(new LinearGradient(w - dp(2) - len, 0, w - dp(2), 0, alpha(color, 165), color, Shader.TileMode.CLAMP));
                    canvas.drawRoundRect(new RectF(w - dp(2) - len, barTop, w - dp(2), barBottom), dp(6), dp(6), paint);
                }
                paint.setShader(null);
            }
        }
    }

    // ------------------------------------------------------------------ donut

    /** Donut with the total in the middle; the legend is rendered by the screen next to it. */
    public static final class Donut extends Base {
        private String[] labels = new String[0];
        private double[] values = new double[0];
        private int[] colors = new int[0];
        private String centerTitle = "";
        private Formatter centerFormatter = COMPACT;

        public Donut(Context c, int textColor, int muted, int grid, Typeface font) { super(c, textColor, muted, grid, font); }

        public Donut data(String[] labels, double[] values, int[] colors, String centerTitle) {
            this.labels = labels == null ? new String[0] : labels;
            this.values = values == null ? new double[0] : values;
            this.colors = colors == null ? new int[0] : colors;
            this.centerTitle = centerTitle == null ? "" : centerTitle;
            StringBuilder cd = new StringBuilder(this.centerTitle).append(": ");
            for (int i = 0; i < this.labels.length; i++) {
                cd.append(this.labels[i]).append(' ').append(centerFormatter.format(i < this.values.length ? this.values[i] : 0)).append("، ");
            }
            announce(cd.toString());
            animateIn();
            return this;
        }

        public Donut centerFormatter(Formatter f) { if (f != null) centerFormatter = f; return this; }

        @Override protected void onDraw(Canvas canvas) {
            float w = getWidth(), h = getHeight();
            if (w <= 0 || h <= 0) return;
            if (labels.length == 0 || values.length == 0) { drawEmpty(canvas); return; }
            float size = Math.min(w, h);
            float stroke = size * 0.14f;
            float cx = w / 2f, cy = h / 2f, r = size / 2f - stroke / 2f - dp(2);
            RectF box = new RectF(cx - r, cy - r, cx + r, cy + r);
            double total = 0;
            for (double v : values) total += Math.max(0, v);

            paint.setShader(null);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(stroke);
            paint.setStrokeCap(Paint.Cap.BUTT);
            paint.setColor(grid);
            canvas.drawArc(box, 0, 360, false, paint);
            float start = -90;
            if (total > 0) {
                for (int i = 0; i < labels.length && i < values.length; i++) {
                    float sweep = (float) (Math.max(0, values[i]) / total * 360f) * progress;
                    paint.setColor(i < colors.length ? colors[i] : textColor);
                    canvas.drawArc(box, start, Math.max(0, sweep - 1.4f), false, paint);
                    start += sweep;
                }
            }

            text.setTextAlign(Paint.Align.CENTER);
            float inner = (2 * r - stroke) * 0.84f;
            String value = centerFormatter.format(total * progress);
            text.setColor(textColor);
            text.setTypeface(bold);
            float ts = size * 0.13f;
            text.setTextSize(ts);
            while (ts > dp(9) && text.measureText(value) > inner) { ts -= dp(0.5f); text.setTextSize(ts); }
            canvas.drawText(value, cx, cy + ts * 0.28f, text);
            text.setTypeface(regular);
            text.setColor(muted);
            float ss = Math.max(dp(9), size * 0.078f);
            text.setTextSize(ss);
            canvas.drawText(fit(centerTitle, inner), cx, cy + ts * 0.28f + ss * 1.5f, text);
        }
    }

    // ------------------------------------------------------------------ ring / gauge

    /** Progress ring: collection ratio, delivery ratio, plan completion. */
    public static final class Ring extends Base {
        private double value, total = 1;
        private String title = "";
        private String note = "";
        private int color;

        public Ring(Context c, int textColor, int muted, int grid, Typeface font) { super(c, textColor, muted, grid, font); }

        public Ring data(double value, double total, String title, int color) {
            this.value = value;
            this.total = total <= 0 ? 1 : total;
            this.title = title == null ? "" : title;
            this.color = color;
            announce(title + ": " + FinFmt.percent(value, total));
            animateIn();
            return this;
        }

        public Ring note(String n) { this.note = n == null ? "" : n; return this; }

        @Override protected void onDraw(Canvas canvas) {
            float w = getWidth(), h = getHeight();
            if (w <= 0 || h <= 0) return;
            float size = Math.min(w, h);
            float stroke = size * 0.13f;
            float cx = w / 2f, cy = h / 2f, r = size / 2f - stroke / 2f - dp(2);
            float ratio = (float) Math.max(0, Math.min(1, value / total));
            float sweep = 360f * ratio * progress;
            RectF box = new RectF(cx - r, cy - r, cx + r, cy + r);
            paint.setShader(null);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(stroke);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setColor(grid);
            canvas.drawArc(box, -90, 360, false, paint);
            paint.setColor(color);
            canvas.drawArc(box, -90, sweep, false, paint);

            text.setTextAlign(Paint.Align.CENTER);
            String percent = FinFmt.faNumber(Math.round(ratio * 100) + "٪");
            float ts = size * 0.2f;
            text.setTextSize(ts);
            text.setColor(textColor);
            text.setTypeface(bold);
            float inner = (2 * r - stroke) * 0.86f;
            while (ts > dp(9) && text.measureText(percent) > inner) { ts -= dp(0.5f); text.setTextSize(ts); }
            canvas.drawText(percent, cx, cy + ts * 0.3f, text);
            text.setTypeface(regular);
            text.setColor(muted);
            float ss = Math.max(dp(9), size * 0.095f);
            text.setTextSize(ss);
            float y = cy + ts * 0.42f + ss * 1.1f;
            canvas.drawText(fit(title, inner), cx, y, text);
            if (!note.isEmpty()) {
                text.setTextSize(ss * 0.92f);
                canvas.drawText(fit(note, inner), cx, y + ss * 1.25f, text);
            }
        }
    }

    /** Tiny line used inside a KPI tile to show the shape of the last days at a glance. */
    public static final class Spark extends Base {
        private double[] values = new double[0];
        private int color = Color.WHITE;

        public Spark(Context c, int color) {
            super(c, color, color, alpha(color, 40), null);
            this.color = color;
            setContentDescription("");
        }

        public Spark data(double[] values) {
            this.values = values == null ? new double[0] : values;
            announce(FinFmt.faNumber(this.values.length) + " نقطه داده");
            animateIn();
            return this;
        }

        @Override protected void onDraw(Canvas canvas) {
            float w = getWidth(), h = getHeight();
            if (w <= 0 || h <= 0 || values.length < 2) return;
            double max = 0, min = Double.MAX_VALUE;
            for (double v : values) { max = Math.max(max, v); min = Math.min(min, v); }
            double range = Math.max(1e-6, max - min);
            float step = w / (values.length - 1f);
            float[] xs = new float[values.length], ys = new float[values.length];
            for (int i = 0; i < values.length; i++) {
                xs[i] = w - step * i;                                   // newest on the right, like the big charts
                ys[i] = h - dp(2) - (float) ((values[i] - min) / range) * (h - dp(5)) * progress;
            }
            Path line = new Path(), fill = new Path();
            line.moveTo(xs[0], ys[0]);
            fill.moveTo(xs[0], h);
            fill.lineTo(xs[0], ys[0]);
            for (int i = 1; i < values.length; i++) { line.lineTo(xs[i], ys[i]); fill.lineTo(xs[i], ys[i]); }
            fill.lineTo(xs[values.length - 1], h);
            fill.close();
            paint.setStyle(Paint.Style.FILL);
            paint.setShader(new LinearGradient(0, 0, 0, h, alpha(color, 90), alpha(color, 0), Shader.TileMode.CLAMP));
            canvas.drawPath(fill, paint);
            paint.setShader(null);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(Math.max(1.4f, dp(1.8f)));
            paint.setColor(color);
            canvas.drawPath(line, paint);
            paint.setStyle(Paint.Style.FILL);
            canvas.drawCircle(xs[values.length - 1], ys[values.length - 1], Math.max(1.6f, dp(2.4f)), paint);
        }
    }
}

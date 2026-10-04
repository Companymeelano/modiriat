package ir.meelano.android.finance;

import android.graphics.Typeface;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Locale;

/**
 * Base class of every «آتیران مالی» screen.
 *
 * A screen owns one column, builds its structure once, and loads its data through {@link FinDb#read}
 * (memory cache → database → last saved copy when offline). No screen ever prints a SQL message: an
 * unexpected error becomes a generic Persian line plus a short event code that support can match
 * against the log, which is the only place the technical detail is kept.
 */
public abstract class FinScreen {

    protected final AtiranFinanceActivity host;
    protected final FinUi ui;
    protected final FinDb db;
    protected final LinearLayout box;
    private int refreshNonce = 0;

    protected FinScreen(AtiranFinanceActivity host) {
        this.host = host;
        this.ui = host.ui();
        this.db = host.db();
        this.box = ui.column();
    }

    public abstract String title();

    public String subtitle() { return ""; }

    public String glyph() { return "◆"; }

    /** Build the structure (headings, filter rows, empty containers). */
    public View build() {
        FinCrash.step(host, "screen-build:" + getClass().getSimpleName());
        box.removeAllViews();
        box.setBackgroundColor(ui.bg);
        ui.pad(box);
        populate();
        return box;
    }

    protected abstract void populate();

    /** How many blocks the current screen has drawn — used by the host's empty-screen check. */
    public int contentChildCount() {
        return box.getChildCount();
    }

    /** Rebuilds the whole screen (structure + data) in place — used by the retry action. */
    public final void rerun() {
        try {
            box.removeAllViews();
            ui.pad(box);
            populate();
            fetch();
        } catch (Throwable t) {
            // A retry that fails again reports itself; it never closes the application.
            host.reportUiError(t);
        }
    }

    public final void load(boolean force) {
        if (force) refreshNonce++;
        FinCrash.step(host, "screen-load:" + getClass().getSimpleName());
        try {
            fetch();
        } catch (Throwable t) {
            // A screen that cannot even start its query reports itself instead of taking the app down.
            host.reportUiError(t);
        }
    }

    protected abstract void fetch();

    /** Cache key; screens append their filters. */
    protected abstract String cacheKey();

    /** A manual refresh reads a fresh key so the cached copy is not returned again. */
    protected String key(String base) {
        return refreshNonce == 0 ? base : base + ":r" + refreshNonce;
    }

    // ------------------------------------------------------------------ shared helpers

    protected void add(View v) { box.addView(v, ui.lp(-1, -2)); }

    protected void addSpace(int dp) { box.addView(ui.spacer(dp)); }

    /** Layout params with a top margin (1dp = 2px on a 2x device). */
    protected LinearLayout.LayoutParams top(int dp) {
        LinearLayout.LayoutParams p = ui.lp(-1, -2);
        p.topMargin = ui.dp(dp);
        return p;
    }

    protected void addCard(LinearLayout card, int topDp) {
        LinearLayout.LayoutParams p = ui.lp(-1, -2);
        p.topMargin = ui.dp(topDp);
        box.addView(card, p);
    }

    /** Section card with a title line; the caller fills it. */
    protected LinearLayout section(String glyph, String title, String subtitle) {
        LinearLayout card = ui.card();
        card.addView(ui.sectionTitle(title, subtitle, glyph), ui.lp(-1, -2));
        return card;
    }

    /**
     * Hero header of a screen: the brand emblem, the screen title, a one-line explanation and the
     * server date. It is the first thing an operator sees and it carries the live date, so the whole
     * screen can be trusted to talk about the same day.
     */
    protected LinearLayout hero(String title, String subtitle, int accent) {
        LinearLayout card = ui.gradientCard(accent, 18);
        LinearLayout row = ui.row();
        FinCharts.Logo logo = new FinCharts.Logo(ui.ctx(), accent, ui.silver, ui.surface, ui.bg);
        row.addView(logo, new LinearLayout.LayoutParams(ui.dp(52), ui.dp(52)));
        LinearLayout copy = ui.column();
        copy.setPadding(ui.dp(11), 0, 0, 0);
        copy.addView(ui.text(title, 16.5f, ui.textColor, true), ui.lp(-1, -2));
        if (subtitle != null && !subtitle.isEmpty()) {
            TextView s = ui.text(subtitle, 11.5f, ui.textDim, false);
            s.setMaxLines(2);
            copy.addView(s, ui.lp(-1, -2));
        }
        row.addView(copy, ui.lp(0, -2, 1f));
        card.addView(row, ui.lp(-1, -2));
        LinearLayout meta = ui.row();
        meta.setPadding(0, ui.dp(9), 0, 0);
        meta.addView(ui.chip("تاریخ سرور " + FinFmt.faNumber(host.clockLabel()), accent), ui.lp(-2, -2));
        meta.addView(ui.spacer(6), ui.lp(ui.dp(6), -2));
        meta.addView(ui.chip(FinFmt.periodLabel(host.periodKey()), ui.textFaint), ui.lp(-2, -2));
        card.addView(meta, ui.lp(-1, -2));
        return card;
    }

    /** Adds a chart to a card with a responsive height (taller on a tablet, never cramped on a phone). */
    protected void addChart(LinearLayout card, View chart, int heightDp) {
        int widthDp = 0;
        try { widthDp = host.getResources().getConfiguration().screenWidthDp; } catch (Exception ignored) { }
        int height = widthDp >= 600 ? Math.round(heightDp * 1.2f) : (widthDp > 0 && widthDp < 340 ? Math.round(heightDp * 0.9f) : heightDp);
        LinearLayout.LayoutParams lp = ui.lp(-1, ui.dp(height));
        lp.topMargin = ui.dp(8);
        card.addView(chart, lp);
    }

    /** Adds a horizontal bar chart that is exactly as tall as its rows. */
    protected void addBars(LinearLayout card, View bars, int rows) {
        LinearLayout.LayoutParams lp = ui.lp(-1, ui.barsHeight(Math.max(1, rows)));
        lp.topMargin = ui.dp(8);
        card.addView(bars, lp);
    }

    /** A row of filter values rendered as the segmented pills of the design kit. */
    protected LinearLayout filters(String[][] spec, String active, int accent, final Pick pick) {
        LinearLayout row = ui.row();
        for (int i = 0; i < spec.length; i++) {
            final String id = spec[i][0];
            boolean on = id.equals(active);
            TextView cell = ui.pillChip(spec[i][1], on, accent, v -> pick.pick(id));
            cell.setMaxLines(1);
            LinearLayout.LayoutParams lp = ui.lp(0, -2, 1f);
            lp.leftMargin = ui.dp(3);
            lp.rightMargin = ui.dp(3);
            row.addView(cell, lp);
        }
        return row;
    }

    protected interface Pick { void pick(String id); }

    protected TextView stateText(String text, int color) {
        TextView t = ui.text(text, 12.5f, color, false);
        t.setPadding(0, ui.dp(8), 0, ui.dp(8));
        t.setGravity(Gravity.START);
        return t;
    }

    protected LinearLayout loading(String what) {
        LinearLayout card = ui.card();
        card.addView(stateText("⏳  در حال دریافت " + what + "…", ui.textDim));
        return card;
    }

    protected LinearLayout empty(String what) {
        LinearLayout card = ui.card();
        card.addView(stateText("هیچ رکوردی برای " + what + " وجود ندارد.", ui.textDim));
        return card;
    }

    /**
     * Offline / error card. {@code detail} is the technical message from the data layer; it is
     * converted into an event code and not displayed.
     */
    protected LinearLayout problem(String detail, Runnable retry) {
        LinearLayout card = ui.cardTone(FinUi.WARNING);
        card.addView(ui.text("⚠  دریافت اطلاعات از سرور انجام نشد", 13f, ui.textColor, true), ui.lp(-1, -2));
        card.addView(stateText(host.hasCache() ? "نمایش آخرین نسخه ذخیرهشده روی دستگاه." : "اتصال شبکه/سرور را بررسی کنید.", ui.textDim));
        if (detail != null && !detail.isEmpty()) {
            card.addView(ui.text("کد رویداد: " + FinFmt.eventCode(detail), 11f, ui.textFaint, false), ui.lp(-1, -2));
        }
        if (retry != null) {
            LinearLayout actions = ui.row();
            actions.addView(ui.button("تلاش دوباره", ui.goldAccent, true, v -> rerun()), ui.lp(0, -2, 1f));
            card.addView(actions, ui.lp(-1, -2));
        }
        return card;
    }

    protected LinearLayout staleTag(JSONObject envelope) {
        if (envelope == null || !envelope.optBoolean("stale", false)) return null;
        LinearLayout card = ui.cardTone(FinUi.WARNING);
        card.addView(ui.text("⌛  حالت آفلاین — آخرین داده ذخیرهشده نمایش داده میشود", 12f, FinUi.WARNING, true), ui.lp(-1, -2));
        return card;
    }

    // ------------------------------------------------------------------ envelope helpers

    protected static JSONArray rowsOf(JSONObject envelope) {
        if (envelope == null) return new JSONArray();
        JSONArray r = envelope.optJSONArray("rows");
        return r == null ? new JSONArray() : r;
    }

    /** First row of an envelope (used for payload screens that return a single object). */
    protected static JSONObject payload(JSONObject envelope) {
        JSONArray rows = rowsOf(envelope);
        JSONObject o = rows.optJSONObject(0);
        return o == null ? new JSONObject() : o;
    }

    protected static JSONArray arr(JSONObject o, String name) {
        JSONArray a = o == null ? null : o.optJSONArray(name);
        return a == null ? new JSONArray() : a;
    }

    protected static String s(JSONObject o, String name, String fallback) {
        if (o == null || o.isNull(name)) return fallback;
        String v = o.optString(name, fallback);
        return v == null ? fallback : v;
    }

    protected static double d(JSONObject o, String name) {
        return o == null ? 0d : o.optDouble(name, 0d);
    }

    protected static int i(JSONObject o, String name) {
        return o == null ? 0 : o.optInt(name, 0);
    }

    protected static String money(double v) { return FinFmt.amount(v); }

    protected static String compact(double v) { return FinFmt.compact(v); }

    /** Key/value line used inside detail cards. */
    protected View kv(String key, String value, int valueColor) {
        LinearLayout r = ui.row();
        r.setPadding(0, ui.dp(3), 0, ui.dp(3));
        TextView k = ui.text(key, 12f, ui.textDim, false);
        r.addView(k, ui.lp(0, -2, 1f));
        TextView v = ui.text(value, 12.5f, valueColor, true);
        v.setGravity(Gravity.END);
        v.setMaxLines(2);
        v.setEllipsize(TextUtils.TruncateAt.END);
        r.addView(v, ui.lp(0, -2, 1.3f));
        return r;
    }

    protected View kv(String key, String value) { return kv(key, value, ui.textColor); }

    protected View kvMoney(String key, double amount) {
        return kv(key, money(amount) + " " + FinFmt.CURRENCY, amount < 0 ? FinUi.DANGER : ui.textColor);
    }

    protected Typeface bold() { return Typeface.DEFAULT_BOLD; }

    protected String fa(Object v) { return FinFmt.faNumber(v); }

    protected String label(Object v) { return v == null ? "" : String.valueOf(v); }

    protected static String trimTo(String v, int max) {
        if (v == null) return "";
        if (max <= 0) return "";
        return v.length() <= max ? v : v.substring(0, Math.max(0, max - 1)) + "…";
    }

    protected static int statusOfName(String status) { return FinUi.statusColor(status); }

    protected static String lower(String v) { return v == null ? "" : v.toLowerCase(Locale.US); }
}

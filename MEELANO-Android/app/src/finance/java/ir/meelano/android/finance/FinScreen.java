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
        box.removeAllViews();
        box.setBackgroundColor(ui.bg);
        ui.pad(box);
        populate();
        return box;
    }

    protected abstract void populate();

    /** Rebuilds the whole screen (structure + data) in place — used by the retry action. */
    public final void rerun() {
        box.removeAllViews();
        ui.pad(box);
        populate();
        fetch();
    }

    public final void load(boolean force) {
        if (force) refreshNonce++;
        fetch();
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
        return v.length() <= max ? v : v.substring(0, max - 1) + "…";
    }

    protected static int statusOfName(String status) { return FinUi.statusColor(status); }

    protected static String lower(String v) { return v == null ? "" : v.toLowerCase(Locale.US); }
}

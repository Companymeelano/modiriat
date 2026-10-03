package ir.meelano.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;

/**
 * «آتیران انبار» — Warehouse & Dispatch operations screen for اسما حمدانی.
 *
 * Responsive/RTL fixes over the first cut:
 *  - all paddings/margins go through dp() so layouts hold on every screen density;
 *  - explicit light surface + white row cards guarantee contrast regardless of theme;
 *  - buttons get a 48dp min touch height and weighted rows so they never overflow;
 *  - ScrollView fillViewport; rows are tappable (invoice/product drill-down dialogs);
 *  - error state offers a Retry button; previously-dead sales/inventory lists are rendered.
 *
 * View-based (no AndroidX/Compose); data only from {@link MeelanoWarehouse} (verified schema).
 */
public class MeelanoWarehouseActivity extends Activity {
    private static final int GOLD = Color.rgb(176, 132, 34);
    private static final int INK = Color.rgb(24, 27, 32);
    private static final int SUB = Color.rgb(108, 114, 124);
    private static final int RED = Color.rgb(178, 41, 41);
    private static final int GREEN = Color.rgb(28, 122, 63);
    private static final int BG = Color.rgb(245, 246, 248);
    private static final int CARD = Color.rgb(255, 255, 255);

    private final Handler main = new Handler(Looper.getMainLooper());
    private LinearLayout body;
    private TextView status;
    private EditText searchBox;

    private int dp(int v) { return Math.round(v * getResources().getDisplayMetrics().density); }

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        setTitle("آتیران انبار");
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(BG);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(16), dp(16), dp(16), dp(24));
        root.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);

        TextView title = new TextView(this);
        title.setText("آتیران انبار");
        title.setTextSize(24); title.setTextColor(GOLD); title.setTypeface(null, Typeface.BOLD);
        root.addView(title);

        TextView user = new TextView(this);
        user.setText("خوش آمدید، اسما حمدانی — مرکز عملیات انبار و تحویل");
        user.setTextSize(13); user.setTextColor(SUB);
        root.addView(user);

        status = new TextView(this);
        status.setText("در حال دریافت اطلاعات…");
        status.setTextSize(12); status.setTextColor(SUB);
        status.setPadding(0, dp(8), 0, dp(8));
        root.addView(status);

        LinearLayout actions = row();
        actions.addView(button("بروزرسانی", v -> load(), 1f));
        actions.addView(button("خروجی CSV", v -> exportCsv(), 1f));
        root.addView(actions);

        LinearLayout searchRow = row();
        searchBox = new EditText(this);
        searchBox.setHint("جستجو: کالا / فاکتور / مشتری / کد");
        searchBox.setTextSize(14);
        searchBox.setBackgroundColor(CARD);
        searchBox.setPadding(dp(10), dp(8), dp(10), dp(8));
        searchBox.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        searchRow.addView(searchBox);
        searchRow.addView(button("جستجو", v -> doSearch(searchBox.getText().toString().trim()), 0f));
        root.addView(searchRow);

        body = new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);
        root.addView(body);

        scroll.addView(root);
        setContentView(scroll);
        load();
    }

    private LinearLayout row() {
        LinearLayout r = new LinearLayout(this);
        r.setOrientation(LinearLayout.HORIZONTAL);
        r.setPadding(0, dp(6), 0, dp(6));
        return r;
    }

    private Button button(String text, View.OnClickListener l, float weight) {
        Button b = new Button(this);
        b.setText(text);
        b.setMinimumHeight(dp(48));
        b.setOnClickListener(l);
        LinearLayout.LayoutParams p = weight > 0
                ? new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, weight)
                : new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        p.setMargins(dp(4), 0, dp(4), 0);
        b.setLayoutParams(p);
        return b;
    }

    // ---------------------------------------------------------------- loading --
    private void load() {
        status.setText("در حال دریافت اطلاعات…");
        body.removeAllViews();
        new Thread(() -> {
            try (Connection c = MainActivity.backgroundConnection(this)) {
                MeelanoWarehouse.ensureTables(c);
                final JSONObject tower = MeelanoWarehouse.controlTower(c);
                final JSONArray stages = MeelanoWarehouse.invoiceStages(c, 30);
                final JSONArray critical = MeelanoWarehouse.criticalStock(c, 15);
                final JSONArray tasks = MeelanoWarehouse.taskBoard(c, 15);
                final JSONArray deliveries = MeelanoWarehouse.deliveryList(c, 15);
                final JSONArray sales = MeelanoWarehouse.salesList(c, 15);
                final JSONArray inv = MeelanoWarehouse.inventoryList(c, 15);
                final JSONArray workers = MeelanoWarehouse.workers(c);
                final JSONObject report = MeelanoWarehouse.dailyReport(c, "");
                main.post(() -> render(tower, stages, critical, tasks, deliveries, sales, inv, workers, report));
            } catch (Exception e) {
                main.post(this::showError);
            }
        }).start();
    }

    private void showError() {
        status.setText("دریافت اطلاعات با مشکل مواجه شد.");
        body.removeAllViews();
        body.addView(card(label("اتصال به پایگاه داده برقرار نشد. لطفاً دوباره تلاش کنید.", RED), true));
        LinearLayout r = row();
        r.addView(button("تلاش دوباره", v -> load(), 1f));
        body.addView(r);
    }

    // ------------------------------------------------------------------ export --
    private void exportCsv() {
        status.setText("در حال ساخت خروجی CSV…");
        new Thread(() -> {
            try (Connection c = MainActivity.backgroundConnection(this)) {
                String inv = MeelanoWarehouse.inventoryCsv(c);
                String sales = MeelanoWarehouse.salesCsv(c);
                File dir = getExternalFilesDir(null);
                if (dir == null) dir = getFilesDir();
                File f1 = new File(dir, "atiran-inventory.csv");
                File f2 = new File(dir, "atiran-sales.csv");
                write(f1, inv); write(f2, sales);
                final String path = f1.getAbsolutePath();
                main.post(() -> status.setText("خروجی ذخیره شد: " + path));
            } catch (Exception e) {
                main.post(() -> status.setText("خروجی CSV با مشکل مواجه شد."));
            }
        }).start();
    }

    private static void write(File f, String content) throws Exception {
        try (OutputStreamWriter w = new OutputStreamWriter(new FileOutputStream(f), StandardCharsets.UTF_8)) {
            w.write(content);
        }
    }

    // ------------------------------------------------------------------ search --
    private void doSearch(String q) {
        if (TextUtils.isEmpty(q)) { load(); return; }
        status.setText("جستجو برای: " + q);
        body.removeAllViews();
        new Thread(() -> {
            try (Connection c = MainActivity.backgroundConnection(this)) {
                final JSONObject res = MeelanoWarehouse.search(c, q, 20);
                main.post(() -> renderSearch(q, res));
            } catch (Exception e) {
                main.post(() -> { status.setText("جستجو با مشکل مواجه شد."); body.addView(button("تلاش دوباره", v -> doSearch(q), 0f)); });
            }
        }).start();
    }

    private void renderSearch(String q, JSONObject res) {
        section("نتایج جستجو: " + q);
        body.addView(label("کالاها:", GOLD));
        JSONArray prods = res.optJSONArray("products");
        if (prods != null && prods.length() > 0)
            for (int i = 0; i < prods.length(); i++) { final JSONObject o = prods.optJSONObject(i); if (o != null)
                body.addView(card(tap(o.optString("name") + "  •  کد " + o.optLong("shka") + "  •  موجودی " + fmt(o.optDouble("stock")), INK,
                        v -> showProduct(o.optLong("shka"), o.optString("name"))), false)); }
        else body.addView(card(label("موردی برای نمایش وجود ندارد.", SUB), true));
        body.addView(label("فاکتورها:", GOLD));
        JSONArray invs = res.optJSONArray("invoices");
        if (invs != null && invs.length() > 0)
            for (int i = 0; i < invs.length(); i++) { final JSONObject o = invs.optJSONObject(i); if (o != null)
                body.addView(card(tap("#" + o.optLong("shfacfo") + "  •  " + o.optString("date"), INK,
                        v -> showInvoice(o.optLong("shfacfo"))), false)); }
        else body.addView(card(label("موردی برای نمایش وجود ندارد.", SUB), true));
        body.addView(label("مشتری‌ها:", GOLD));
        JSONArray custs = res.optJSONArray("customers");
        if (custs != null && custs.length() > 0)
            for (int i = 0; i < custs.length(); i++) { JSONObject o = custs.optJSONObject(i); if (o != null)
                body.addView(card(label(o.optString("name") + "  •  کد " + o.optLong("shmo"), INK), false)); }
        else body.addView(card(label("موردی برای نمایش وجود ندارد.", SUB), true));
        body.addView(button("بازگشت به داشبورد", v -> load(), 0f));
    }

    // ------------------------------------------------------------------ render --
    private void render(JSONObject tower, JSONArray stages, JSONArray critical, JSONArray tasks, JSONArray deliveries,
                        JSONArray sales, JSONArray inv, JSONArray workers, JSONObject report) {
        status.setText("آخرین بروزرسانی: هم‌اکنون");

        section("برج کنترل — وضعیت فاکتورها");
        body.addView(card(label("جدید " + tower.optInt("new") + "  →  برداشت " + tower.optInt("picking")
                + "  →  آماده " + tower.optInt("ready") + "  →  تحویل‌شده " + tower.optInt("delivered"), INK), true));
        body.addView(card(label("ناقص " + tower.optInt("incomplete") + "  •  مغایرت دریافت " + tower.optLong("receive_diff")
                + "  •  تحویل ناقص " + tower.optLong("delivery_partial"), tower.optInt("incomplete") > 0 ? RED : SUB), true));

        section("صف وضعیت فاکتورهای فروش (برای جزئیات لمس کنید)");
        if (stages.length() == 0) body.addView(card(label("موردی برای نمایش وجود ندارد.", SUB), true));
        for (int i = 0; i < stages.length(); i++) { final JSONObject o = stages.optJSONObject(i); if (o == null) continue;
            body.addView(card(tap("#" + o.optLong("shfacfo") + "  •  " + o.optString("customer")
                    + "  •  " + stageFa(o.optString("stage")), stageColor(o.optString("stage")),
                    v -> showInvoice(o.optLong("shfacfo"))), false)); }

        section("کالاهای بحرانی / کم‌موجود");
        if (critical.length() == 0) body.addView(card(label("موردی برای نمایش وجود ندارد.", SUB), true));
        for (int i = 0; i < critical.length(); i++) { final JSONObject o = critical.optJSONObject(i); if (o == null) continue;
            body.addView(card(tap(o.optString("name") + "  •  موجودی " + fmt(o.optDouble("stock"))
                    + (o.optBoolean("critical") ? "  (بحرانی)" : ""), o.optBoolean("critical") ? RED : INK,
                    v -> showProduct(o.optLong("shka"), o.optString("name"))), false)); }

        section("تابلوی برداشت کارگران");
        if (tasks.length() == 0) body.addView(card(label("موردی برای نمایش وجود ندارد.", SUB), true));
        for (int i = 0; i < tasks.length(); i++) { JSONObject o = tasks.optJSONObject(i); if (o == null) continue;
            body.addView(card(label("فاکتور #" + o.optLong("shfacfo") + "  •  " + o.optString("name")
                    + "  •  " + fmt(o.optDouble("picked")) + "/" + fmt(o.optDouble("requested"))
                    + "  •  " + o.optString("assignee"), "done".equals(o.optString("state")) ? GREEN : INK), false)); }

        section("تحویل‌ها");
        if (deliveries.length() == 0) body.addView(card(label("موردی برای نمایش وجود ندارد.", SUB), true));
        for (int i = 0; i < deliveries.length(); i++) { JSONObject o = deliveries.optJSONObject(i); if (o == null) continue;
            body.addView(card(label("#" + o.optLong("shfacfo") + "  •  " + o.optString("customer")
                    + "  •  " + deliveryFa(o.optString("status")) + "  •  " + o.optString("assignee"),
                    "delivered".equals(o.optString("status")) ? GREEN : INK), false)); }

        section("فاکتورهای فروش");
        if (sales.length() == 0) body.addView(card(label("موردی برای نمایش وجود ندارد.", SUB), true));
        for (int i = 0; i < sales.length(); i++) { final JSONObject o = sales.optJSONObject(i); if (o == null) continue;
            body.addView(card(tap("#" + o.optLong("shfacfo") + "  •  " + o.optString("customer")
                    + "  •  " + o.optLong("lines") + " قلم", INK, v -> showInvoice(o.optLong("shfacfo"))), false)); }

        section("موجودی انبار (از دفتر گردش آتیران)");
        if (inv.length() == 0) body.addView(card(label("موردی برای نمایش وجود ندارد.", SUB), true));
        for (int i = 0; i < inv.length(); i++) { final JSONObject o = inv.optJSONObject(i); if (o == null) continue;
            body.addView(card(tap(o.optString("name") + "  •  کد " + o.optLong("shka")
                    + "  •  موجودی " + fmt(o.optDouble("stock")), INK,
                    v -> showProduct(o.optLong("shka"), o.optString("name"))), false)); }

        section("گزارش عملکرد امروز");
        body.addView(card(label("فروش " + report.optLong("sales") + "  •  تحویل‌شده " + report.optLong("delivered")
                + "  •  دریافت خرید " + report.optLong("received") + "  •  مغایرت دریافت " + report.optLong("receive_diff"), INK), true));

        section("کارگران انبار (از کنترل دسترسی)");
        if (workers.length() == 0) body.addView(card(label("موردی برای نمایش وجود ندارد.", SUB), true));
        for (int i = 0; i < workers.length(); i++) { JSONObject o = workers.optJSONObject(i); if (o == null) continue;
            body.addView(card(label(o.optString("name") + "  •  نقش " + o.optString("role"), INK), false)); }
    }

    // ------------------------------------------------------------- drill-down --
    private void showInvoice(final long shfacfo) {
        new Thread(() -> {
            try (Connection c = MainActivity.backgroundConnection(this)) {
                final JSONArray items = MeelanoWarehouse.salesDetail(c, shfacfo);
                main.post(() -> {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < items.length(); i++) {
                        JSONObject o = items.optJSONObject(i); if (o == null) continue;
                        sb.append(o.optString("name")).append("\nدرخواست ")
                          .append(fmt(o.optDouble("requested"))).append(" | موجودی ")
                          .append(fmt(o.optDouble("stock"))).append(" | کسری ")
                          .append(fmt(o.optDouble("shortage"))).append("\n\n");
                    }
                    dialog("فاکتور #" + shfacfo, sb.length() == 0 ? "موردی برای نمایش وجود ندارد." : sb.toString());
                });
            } catch (Exception e) { main.post(() -> dialog("خطا", "دریافت اطلاعات با مشکل مواجه شد.")); }
        }).start();
    }

    private void showProduct(final long shka, final String name) {
        new Thread(() -> {
            try (Connection c = MainActivity.backgroundConnection(this)) {
                final JSONArray mv = MeelanoWarehouse.movements(c, shka, 30);
                main.post(() -> {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < mv.length(); i++) {
                        JSONObject o = mv.optJSONObject(i); if (o == null) continue;
                        sb.append("out".equals(o.optString("direction")) ? "خروج" : "ورود")
                          .append(" | tedvah ").append(fmt(o.optDouble("tedvah")))
                          .append(" | tedjoz ").append(fmt(o.optDouble("tedjoz")))
                          .append(" | act ").append(o.optInt("act_id")).append("\n");
                    }
                    dialog(name + " — گردش کالا", sb.length() == 0 ? "گردشی ثبت نشده است." : sb.toString());
                });
            } catch (Exception e) { main.post(() -> dialog("خطا", "دریافت اطلاعات با مشکل مواجه شد.")); }
        }).start();
    }

    private void dialog(String title, String msg) {
        new AlertDialog.Builder(this).setTitle(title).setMessage(msg)
                .setPositiveButton("بستن", null).show();
    }

    // ------------------------------------------------------------------ views --
    private void section(String t) {
        TextView v = new TextView(this);
        v.setText(t); v.setTextSize(16); v.setTextColor(GOLD); v.setTypeface(null, Typeface.BOLD);
        v.setPadding(0, dp(14), 0, dp(6));
        body.addView(v);
    }

    private TextView label(String t, int color) {
        TextView v = new TextView(this);
        v.setText(t); v.setTextSize(13); v.setTextColor(color);
        return v;
    }

    private TextView tap(String t, int color, View.OnClickListener l) {
        TextView v = label(t, color);
        v.setOnClickListener(l);
        return v;
    }

    private LinearLayout card(View child, boolean fill) {
        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setBackgroundColor(CARD);
        c.setPadding(dp(12), dp(10), dp(12), dp(10));
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        p.setMargins(0, dp(3), 0, dp(3));
        c.setLayoutParams(p);
        c.addView(child, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return c;
    }

    private static String stageFa(String s) {
        switch (s) { case "new": return "جدید"; case "picking": return "در برداشت";
            case "ready": return "آماده تحویل"; case "incomplete": return "ناقص";
            case "delivered": return "تحویل‌شده"; default: return s; }
    }
    private static int stageColor(String s) {
        switch (s) { case "incomplete": return RED; case "delivered": return GREEN;
            case "ready": return GOLD; default: return INK; }
    }
    private static String deliveryFa(String s) {
        switch (s) { case "open": return "باز"; case "claimed": return "در حال تحویل";
            case "delivered": return "تحویل‌شده"; case "partial": return "ناقص"; default: return s; }
    }

    private static String fmt(double d) {
        if (d == Math.floor(d)) return String.valueOf((long) d);
        return String.valueOf(d);
    }
}

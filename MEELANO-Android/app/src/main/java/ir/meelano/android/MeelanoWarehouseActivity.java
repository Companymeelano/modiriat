package ir.meelano.android;

import android.app.Activity;
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

import java.sql.Connection;

/**
 * «آتیران انبار» — Warehouse & Dispatch operations screen for اسما حمدانی.
 * View-based (no AndroidX/Compose), RTL, Persian-first. All data from {@link MeelanoWarehouse}
 * (verified schema only). Layout built programmatically so the module stays self-contained.
 */
public class MeelanoWarehouseActivity extends Activity {
    private static final int GOLD = Color.rgb(184, 140, 41);
    private static final int INK = Color.rgb(24, 27, 32);
    private static final int SUB = Color.rgb(110, 116, 126);
    private static final int RED = Color.rgb(178, 41, 41);
    private static final int GREEN = Color.rgb(28, 122, 63);

    private final Handler main = new Handler(Looper.getMainLooper());
    private LinearLayout body;
    private TextView status;
    private EditText searchBox;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        setTitle("آتیران انبار");
        ScrollView scroll = new ScrollView(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(40, 40, 40, 40);
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
        status.setPadding(0, 16, 0, 8);
        root.addView(status);

        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        Button refresh = new Button(this);
        refresh.setText("بروزرسانی");
        refresh.setOnClickListener(v -> load());
        actions.addView(refresh);
        root.addView(actions);

        LinearLayout searchRow = new LinearLayout(this);
        searchRow.setOrientation(LinearLayout.HORIZONTAL);
        searchBox = new EditText(this);
        searchBox.setHint("جستجو: کالا / فاکتور / مشتری / کد");
        searchBox.setTextSize(13);
        searchBox.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        Button go = new Button(this);
        go.setText("جستجو");
        go.setOnClickListener(v -> doSearch(searchBox.getText().toString().trim()));
        searchRow.addView(searchBox); searchRow.addView(go);
        root.addView(searchRow);

        body = new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);
        root.addView(body);

        scroll.addView(root);
        setContentView(scroll);
        load();
    }

    private void load() {
        status.setText("در حال دریافت اطلاعات…");
        body.removeAllViews();
        new Thread(() -> {
            try (Connection c = MainActivity.backgroundConnection(this)) {
                MeelanoWarehouse.ensureTables(c);
                JSONObject tower = MeelanoWarehouse.controlTower(c);
                JSONArray stages = MeelanoWarehouse.invoiceStages(c, 30);
                JSONArray critical = MeelanoWarehouse.criticalStock(c, 15);
                JSONArray tasks = MeelanoWarehouse.taskBoard(c, 15);
                JSONArray deliveries = MeelanoWarehouse.deliveryList(c, 15);
                JSONArray sales = MeelanoWarehouse.salesList(c, 15);
                JSONArray inv = MeelanoWarehouse.inventoryList(c, 15);
                JSONArray workers = MeelanoWarehouse.workers(c);
                JSONObject report = MeelanoWarehouse.dailyReport(c, "");
                main.post(() -> render(tower, stages, critical, tasks, deliveries, sales, inv, workers, report));
            } catch (Exception e) {
                main.post(() -> {
                    status.setText("دریافت اطلاعات با مشکل مواجه شد.");
                    body.addView(label("خطا: اتصال به پایگاه داده برقرار نشد. دوباره تلاش کنید.", SUB));
                });
            }
        }).start();
    }

    private void doSearch(String q) {
        if (TextUtils.isEmpty(q)) { load(); return; }
        status.setText("جستجو برای: " + q);
        body.removeAllViews();
        new Thread(() -> {
            try (Connection c = MainActivity.backgroundConnection(this)) {
                JSONObject res = MeelanoWarehouse.search(c, q, 20);
                main.post(() -> renderSearch(q, res));
            } catch (Exception e) {
                main.post(() -> { status.setText("جستجو با مشکل مواجه شد."); });
            }
        }).start();
    }

    private void renderSearch(String q, JSONObject res) {
        section("نتایج جستجو: " + q);
        body.addView(label("کالاها:", GOLD));
        JSONArray prods = res.optJSONArray("products");
        if (prods != null && prods.length() > 0)
            for (int i = 0; i < prods.length(); i++) { JSONObject o = prods.optJSONObject(i); if (o != null)
                body.addView(label(o.optString("name") + "  •  کد " + o.optLong("shka") + "  •  موجودی " + fmt(o.optDouble("stock")), INK)); }
        else body.addView(label("موردی برای نمایش وجود ندارد.", SUB));
        body.addView(label("فاکتورها:", GOLD));
        JSONArray invs = res.optJSONArray("invoices");
        if (invs != null && invs.length() > 0)
            for (int i = 0; i < invs.length(); i++) { JSONObject o = invs.optJSONObject(i); if (o != null)
                body.addView(label("#" + o.optLong("shfacfo") + "  •  " + o.optString("date"), INK)); }
        else body.addView(label("موردی برای نمایش وجود ندارد.", SUB));
        body.addView(label("مشتری‌ها:", GOLD));
        JSONArray custs = res.optJSONArray("customers");
        if (custs != null && custs.length() > 0)
            for (int i = 0; i < custs.length(); i++) { JSONObject o = custs.optJSONObject(i); if (o != null)
                body.addView(label(o.optString("name") + "  •  کد " + o.optLong("shmo"), INK)); }
        else body.addView(label("موردی برای نمایش وجود ندارد.", SUB));
        Button back = new Button(this);
        back.setText("بازگشت به داشبورد");
        back.setOnClickListener(v -> load());
        body.addView(back);
    }

    private void render(JSONObject tower, JSONArray stages, JSONArray critical, JSONArray tasks, JSONArray deliveries,
                        JSONArray sales, JSONArray inv, JSONArray workers, JSONObject report) {
        status.setText("آخرین بروزرسانی: هم‌اکنون");

        section("برج کنترل — وضعیت فاکتورها");
        body.addView(label("جدید " + tower.optInt("new") + "  →  برداشت " + tower.optInt("picking")
                + "  →  آماده " + tower.optInt("ready") + "  →  تحویل‌شده " + tower.optInt("delivered"), INK));
        body.addView(label("ناقص " + tower.optInt("incomplete") + "  •  مغایرت دریافت " + tower.optLong("receive_diff")
                + "  •  تحویل ناقص " + tower.optLong("delivery_partial"), tower.optInt("incomplete") > 0 ? RED : SUB));

        section("صف وضعیت فاکتورهای فروش");
        for (int i = 0; i < stages.length(); i++) {
            JSONObject o = stages.optJSONObject(i); if (o == null) continue;
            body.addView(label("#" + o.optLong("shfacfo") + "  •  " + o.optString("customer")
                    + "  •  " + stageFa(o.optString("stage")), stageColor(o.optString("stage"))));
        }

        section("کالاهای بحرانی / کم‌موجود");
        if (critical.length() == 0) body.addView(label("موردی برای نمایش وجود ندارد.", SUB));
        for (int i = 0; i < critical.length(); i++) {
            JSONObject o = critical.optJSONObject(i); if (o == null) continue;
            body.addView(label(o.optString("name") + "  •  موجودی " + fmt(o.optDouble("stock"))
                    + (o.optBoolean("critical") ? "  (بحرانی)" : ""), o.optBoolean("critical") ? RED : INK));
        }

        section("تابلوی برداشت کارگران");
        if (tasks.length() == 0) body.addView(label("موردی برای نمایش وجود ندارد.", SUB));
        for (int i = 0; i < tasks.length(); i++) {
            JSONObject o = tasks.optJSONObject(i); if (o == null) continue;
            body.addView(label("فاکتور #" + o.optLong("shfacfo") + "  •  " + o.optString("name")
                    + "  •  " + fmt(o.optDouble("picked")) + "/" + fmt(o.optDouble("requested"))
                    + "  •  " + o.optString("assignee"), "done".equals(o.optString("state")) ? GREEN : INK));
        }

        section("تحویل‌ها");
        for (int i = 0; i < deliveries.length(); i++) {
            JSONObject o = deliveries.optJSONObject(i); if (o == null) continue;
            body.addView(label("#" + o.optLong("shfacfo") + "  •  " + o.optString("customer")
                    + "  •  " + deliveryFa(o.optString("status")) + "  •  " + o.optString("assignee"),
                    "delivered".equals(o.optString("status")) ? GREEN : INK));
        }

        section("گزارش عملکرد امروز");
        body.addView(label("فروش " + report.optLong("sales") + "  •  تحویل‌شده " + report.optLong("delivered")
                + "  •  دریافت خرید " + report.optLong("received") + "  •  مغایرت دریافت " + report.optLong("receive_diff"), INK));

        section("کارگران انبار (از کنترل دسترسی)");
        if (workers.length() == 0) body.addView(label("موردی برای نمایش وجود ندارد.", SUB));
        for (int i = 0; i < workers.length(); i++) {
            JSONObject o = workers.optJSONObject(i); if (o == null) continue;
            body.addView(label(o.optString("name") + "  •  نقش " + o.optString("role"), INK));
        }
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

    private void section(String t) {
        TextView v = new TextView(this);
        v.setText(t); v.setTextSize(16); v.setTextColor(GOLD); v.setTypeface(null, Typeface.BOLD);
        v.setPadding(0, 28, 0, 8);
        body.addView(v);
    }

    private TextView label(String t, int color) {
        TextView v = new TextView(this);
        v.setText(t); v.setTextSize(12.5f); v.setTextColor(color);
        v.setPadding(0, 6, 0, 6);
        return v;
    }

    private static String fmt(double d) {
        if (d == Math.floor(d)) return String.valueOf((long) d);
        return String.valueOf(d);
    }
}

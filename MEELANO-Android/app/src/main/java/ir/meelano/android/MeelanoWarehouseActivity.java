package ir.meelano.android;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.Connection;

/**
 * «آتیران انبار» — Warehouse & Dispatch operations screen for اسما حمدانی.
 * View-based (no AndroidX/Compose), RTL, Persian-first; data comes exclusively from
 * {@link MeelanoWarehouse} (verified schema). Layout is built programmatically so the
 * module stays self-contained.
 */
public class MeelanoWarehouseActivity extends Activity {
    private static final int GOLD = Color.rgb(184, 140, 41);
    private static final int INK = Color.rgb(24, 27, 32);
    private static final int SUB = Color.rgb(110, 116, 126);

    private final Handler main = new Handler(Looper.getMainLooper());
    private LinearLayout body;
    private TextView status;

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

        Button refresh = new Button(this);
        refresh.setText("بروزرسانی");
        refresh.setOnClickListener(v -> load());
        root.addView(refresh);

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
                JSONObject dash = MeelanoWarehouse.dashboard(c);
                JSONArray sales = MeelanoWarehouse.salesList(c, 20);
                JSONArray inv = MeelanoWarehouse.inventoryList(c, 20);
                main.post(() -> render(dash, sales, inv));
            } catch (Exception e) {
                main.post(() -> {
                    status.setText("دریافت اطلاعات با مشکل مواجه شد.");
                    body.addView(label("خطا: اتصال به پایگاه داده برقرار نشد. دوباره تلاش کنید.", SUB));
                });
            }
        }).start();
    }

    private void render(JSONObject dash, JSONArray sales, JSONArray inv) {
        status.setText("آخرین بروزرسانی: هم‌اکنون");
        section("امروز — " + dash.optString("today", ""));
        kpiRow("فاکتور فروش امروز", dash.optLong("sales_today"));
        kpiRow("تحویل باز", dash.optLong("delivery_open"));
        kpiRow("در حال تحویل", dash.optLong("delivery_claimed"));
        kpiRow("فاکتور خرید", dash.optLong("purchase_count"));
        kpiRow("کالای دارای کسری", dash.optLong("shortage_products"));
        kpiRow("ردیف تحویل ناقص", dash.optLong("partial_lines"));

        section("فاکتورهای فروش");
        for (int i = 0; i < sales.length(); i++) {
            JSONObject o = sales.optJSONObject(i);
            if (o == null) continue;
            body.addView(label("#" + o.optLong("shfacfo") + "  •  " + o.optString("customer")
                    + "  •  " + o.optLong("lines") + " قلم  •  وضعیت " + o.optInt("status"), INK));
        }

        section("موجودی انبار (از دفتر گردش آتیران)");
        for (int i = 0; i < inv.length(); i++) {
            JSONObject o = inv.optJSONObject(i);
            if (o == null) continue;
            body.addView(label(o.optString("name") + "  •  کد " + o.optLong("shka")
                    + "  •  موجودی " + fmt(o.optDouble("stock")), INK));
        }
    }

    private void section(String t) {
        TextView v = new TextView(this);
        v.setText(t); v.setTextSize(16); v.setTextColor(GOLD); v.setTypeface(null, Typeface.BOLD);
        v.setPadding(0, 28, 0, 8);
        body.addView(v);
    }

    private void kpiRow(String name, long value) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        TextView n = new TextView(this);
        n.setText(name); n.setTextSize(13); n.setTextColor(SUB);
        n.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        TextView val = new TextView(this);
        val.setText(String.valueOf(value)); val.setTextSize(16); val.setTextColor(INK); val.setTypeface(null, Typeface.BOLD);
        row.addView(n); row.addView(val);
        body.addView(row);
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

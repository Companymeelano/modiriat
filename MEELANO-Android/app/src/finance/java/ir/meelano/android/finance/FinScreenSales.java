package ir.meelano.android.finance;

import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * «فروش در برابر وصول» — the financial tab.
 *
 * The screen answers one question honestly: how much did we sell in the period, how much of it was
 * received, and in which form (cash / cheque / POS)? Every column is an aggregate of real rows, and
 * the receipts that do not reconcile with their own components are listed as cases instead of being
 * absorbed into a total.
 *
 * Period presets come from {@link FinFmt#periodRange} and always end on the server's Jalali today.
 */
public class FinScreenSales extends FinScreen {

    public FinScreenSales(AtiranFinanceActivity host) { super(host); }

    @Override public String title() { return "فروش و وصول"; }

    @Override public String glyph() { return "₪"; }

    @Override protected String cacheKey() { return "sales:" + host.periodFrom() + ":" + host.periodTo(); }

    @Override protected void populate() {
        add(hero("فروش در برابر وصول", "هر عدد جمع واقعی فاکتورهای sailfact و قبض‌های dar است", ui.goldAccent));
        periodRow();
        card = ui.column();
        add(card);
    }

    private LinearLayout card;

    private void periodRow() {
        final String[] keys = {"today", "yesterday", "7d", "30d", "month", "lastmonth"};
        String[] labels = new String[keys.length];
        int active = 0;
        for (int i = 0; i < keys.length; i++) {
            labels[i] = FinFmt.periodLabel(keys[i]);
            if (keys[i].equals(host.periodKey())) active = i;
        }
        addCard(ui.segmented(labels, active, ui.goldAccent, index -> host.setPeriod(keys[index])), 6);
    }

    @Override protected void fetch() {
        db.read(key(cacheKey()), 90_000L, c -> {
            String today = FinQueries.serverToday(c);
            JSONObject payload = FinQueries.salesVsCollection(c, host.periodFrom(), host.periodTo());
            payload.put("today", today);
            payload.put("mismatches", FinQueries.receiptMismatches(c, host.periodFrom(), host.periodTo(), 15));
            JSONArray a = new JSONArray();
            a.put(payload);
            return a;
        }, this::render);
    }

    private void render(JSONObject env) {
        card.removeAllViews();
        if (env.optBoolean("stale", false)) {
            LinearLayout st = staleTag(env);
            if (st != null) card.addView(st, ui.lp(-1, -2));
        }
        JSONObject p = payload(env);
        if (p.length() == 0) {
            card.addView(problem(env.optString("error", ""), () -> load(true)), ui.lp(-1, -2));
            return;
        }

        double sales = d(p, "sales");
        double receipts = d(p, "receiptsTotal");
        double cash = d(p, "cash");
        double check = d(p, "check");
        double pos = d(p, "pos");
        double uncollected = d(p, "uncollected");

        LinearLayout k = ui.row();
        k.addView(ui.kpiTile("فروش دوره", compact(sales), "ریال", i(p, "salesCount") + " فاکتور", ui.goldAccent, v -> showInvoices()), ui.lp(0, -2, 1f));
        k.addView(ui.spacer(8), ui.lp(ui.dp(8), -2));
        k.addView(ui.kpiTile("وصول دوره", compact(receipts), "ریال", i(p, "receiptsCount") + " قبض", FinUi.SUCCESS, null), ui.lp(0, -2, 1f));
        card.addView(k, ui.lp(-1, -2));

        LinearLayout k2 = ui.row();
        k2.addView(ui.kpiTile("نسبت وصول", FinFmt.percent(receipts, sales), "", "وصول ÷ فروش دوره", FinUi.INFO,
                trend(p, "receipts", "byDay"), null), ui.lp(0, -2, 1f));
        k2.addView(ui.spacer(8), ui.lp(ui.dp(8), -2));
        k2.addView(ui.kpiTile("وصول‌نشده", compact(uncollected), "ریال", i(p, "uncollectedCount") + " فاکتور باز", FinUi.WARNING,
                trend(p, "sales", "byDay"), v -> host.open(new FinScreenReceivables(host))), ui.lp(0, -2, 1f));
        LinearLayout.LayoutParams top = ui.lp(-1, -2);
        top.topMargin = ui.dp(8);
        card.addView(k2, top);

        mixture(cash, check, pos, receipts, p);
        byDay(p);
        byVisitor(p);
        mismatches(p);
    }

    /** Sparkline series for a KPI tile: the per-day column of the period table. */
    private double[] trend(JSONObject p, String key, String rowsKey) {
        JSONArray rows = arr(p, rowsKey);
        if (rows.length() < 2) return null;
        double[] v = new double[rows.length()];
        for (int i = 0; i < rows.length(); i++) v[i] = rows.optJSONObject(i) == null ? 0 : rows.optJSONObject(i).optDouble(key, 0d);
        return v;
    }

    private void mixture(double cash, double check, double pos, double receipts, JSONObject p) {
        LinearLayout mix = section("⇄", "ترکیب وصول", "نقد / چک / POS در برابر جمع قبض‌ها");
        List<FinCharts.Legend> items = new ArrayList<>();
        int[] palette = ui.palette();
        items.add(new FinCharts.Legend("نقد (dar.naghd)", money(cash), palette[1]));
        items.add(new FinCharts.Legend("چک (dar.mabcheck)", money(check), palette[0]));
        items.add(new FinCharts.Legend("POS (PosDetails)", money(pos), palette[2]));

        FinCharts.Donut donut = ui.donutChart(FinUi.FormatterKind.MONEY);
        donut.data(new String[]{"نقد", "چک", "POS"}, new double[]{cash, check, pos}, new int[]{palette[1], palette[0], palette[2]}, "ترکیب وصول");
        double componentSum = cash + check + pos;
        mix.addView(ui.donutWithLegend(donut, items, componentSum, 168), ui.lp(-1, -2));

        mix.addView(ui.divider(), top(6));
        mix.addView(ui.miniStat("جمع اجزا (نقد + چک + POS)", money(componentSum) + " ریال", ui.goldAccent), ui.lp(-1, -2));
        mix.addView(ui.miniStat("جمع قبض‌ها (dar.mab)", money(receipts) + " ریال", FinUi.INFO), ui.lp(-1, -2));
        mix.addView(ui.miniStat("تفاوت", money(componentSum - receipts) + " ریال",
                Math.abs(componentSum - receipts) > 1 ? FinUi.WARNING : FinUi.SUCCESS), ui.lp(-1, -2));
        mix.addView(ui.text("نقد + چک + POS معمولاً کمی با جمع قبض تفاوت دارد؛ موارد واقعی در بخش مغایرت‌ها فهرست می‌شود.",
                11f, ui.textFaint, false), ui.lp(-1, -2));
        addCard(mix, 12);
    }

    private void byDay(JSONObject p) {
        JSONArray rows = arr(p, "byDay");
        LinearLayout card5 = section("▤", "روند روزانه فروش و وصول", "روزهای بازه انتخاب‌شده — برای دیدن مقدار هر روز روی نمودار بزنید");
        if (rows.length() < 2) {
            card5.addView(stateText("برای این بازه روند روزانه‌ای وجود ندارد (بازه یک‌روزه است یا داده‌ای ثبت نشده).", ui.textDim));
            addCard(card5, 12);
            return;
        }
        int n = Math.min(31, rows.length());
        String[] labels = new String[n];
        double[] salesV = new double[n];
        double[] receiptV = new double[n];
        // Oldest on the left of the array; the chart itself draws right → left (RTL).
        for (int i = 0; i < n; i++) {
            JSONObject r = rows.optJSONObject(n - 1 - i);   // rows are newest-first
            if (r == null) r = new JSONObject();
            labels[i] = FinFmt.faNumber(s(r, "date", "").replace("1405/", "").replace("1406/", "").replace("1404/", ""));
            salesV[i] = d(r, "sales");
            receiptV[i] = d(r, "receipts");
        }
        FinCharts.Columns cols = ui.columnsChart(FinUi.FormatterKind.MONEY);
        cols.data(labels, new double[][]{salesV, receiptV}, new int[]{ui.goldAccent, FinUi.SUCCESS}, new String[]{"فروش", "وصول"});
        addChart(card5, cols, 220);
        addCard(card5, 12);
    }

    private void byVisitor(JSONObject p) {
        JSONArray rows = arr(p, "byVisitor");
        LinearLayout card5 = section("☺", "عملکرد اپراتورها", "فاکتور و وصول به تفکیک ویزیتور");
        if (rows.length() == 0) {
            card5.addView(stateText("در این بازه فروشی برای اپراتوری ثبت نشده است.", ui.textDim));
            addCard(card5, 12);
            return;
        }
        List<String[]> active = new ArrayList<>();
        for (int x = 0; x < rows.length(); x++) {
            JSONObject r = rows.optJSONObject(x);
            if (r == null) continue;
            if (d(r, "sales") <= 0 && d(r, "receipts") <= 0) continue;
            active.add(new String[]{s(r, "vis_name", "—"), fa(i(r, "invoices")), String.valueOf(d(r, "sales")), String.valueOf(d(r, "receipts"))});
        }
        if (active.isEmpty()) {
            card5.addView(stateText("در این بازه فروشی برای اپراتوری ثبت نشده است.", ui.textDim));
            addCard(card5, 12);
            return;
        }
        int n = Math.min(7, active.size());
        String[] labels = new String[n];
        double[] values = new double[n];
        String[] notes = new String[n];
        int[] palette = ui.palette();
        int[] colors = new int[n];
        for (int x = 0; x < n; x++) {
            String[] r = active.get(x);
            labels[x] = r[0];
            values[x] = Double.parseDouble(r[2]);
            notes[x] = r[1] + " فاکتور · وصول " + FinFmt.compact(Double.parseDouble(r[3])) + " ریال";
            colors[x] = palette[x % palette.length];
        }
        FinCharts.Bars bars = ui.barsChart(FinUi.FormatterKind.MONEY);
        bars.data(labels, values, colors, notes);
        addBars(card5, bars, n);

        card5.addView(ui.tableHeader(new String[]{"اپراتور", "فاکتور", "فروش", "وصول"}), top(10));
        for (int x = 0; x < active.size() && x < 12; x++) {
            String[] r = active.get(x);
            double sales = Double.parseDouble(r[2]);
            double receipts = Double.parseDouble(r[3]);
            // The row is tinted by how much of that operator's sale has actually been collected.
            int accent = sales <= 0 ? ui.textDim : (receipts >= sales ? FinUi.SUCCESS : FinUi.WARNING);
            card5.addView(ui.tableRow(new String[]{r[0], r[1], compact(sales), compact(receipts), FinFmt.percent(receipts, sales)},
                    new float[]{1.5f, 0.6f, 1f, 1f, 0.7f}, false, accent), ui.lp(-1, -2));
        }
        addCard(card5, 12);
    }

    private void mismatches(JSONObject p) {
        JSONArray rows = arr(p, "mismatches");
        LinearLayout card6 = section("≠", "مغایرت اجزای دریافت", i(p, "mismatchCount") + " مورد در این بازه");
        if (rows.length() == 0) {
            card6.addView(stateText("همه قبض‌های این بازه با اجزای خود می‌خوانند.", FinUi.SUCCESS));
        } else {
            for (int x = 0; x < rows.length(); x++) {
                JSONObject r = rows.optJSONObject(x);
                if (r == null) continue;
                double diff = d(r, "difference");
                String sub = s(r, "name", "—") + " · " + s(r, "date", "") + " · قبض " + fa(i(r, "ghno"));
                card6.addView(ui.listRow("تفاوت " + money(diff) + " " + FinFmt.CURRENCY, sub,
                        money(d(r, "mab")), diff > 0 ? "نیازمند بررسی" : "مغایرت", FinUi.DANGER, v -> openMismatch(r)), ui.lp(-1, -2));
            }
            card6.addView(ui.ghostButton("≠  مشاهده همه در مرکز مغایرت‌ها", ui.goldAccent, v -> host.open(new FinScreenProblems(host))), top(8));
        }
        addCard(card6, 12);
    }

    private void openMismatch(JSONObject r) {
        host.details("قبض " + fa(i(r, "ghno")), new String[][]{
                {"تاریخ", s(r, "date", "—")},
                {"مشتری", s(r, "name", "—") + " (" + fa(i(r, "shmo")) + ")"},
                {"جمع قبض", money(d(r, "mab"))},
                {"نقد", money(d(r, "naghd"))},
                {"چک", money(d(r, "mabcheck"))},
                {"POS", money(d(r, "pos"))},
                {"تفاوت", money(d(r, "difference"))},
                {"اپراتور", s(r, "visitor_name", "ثبت‌نشده روی قبض")}
        }, "منبع: dbo.dar (p=0) و dbo.PosDetails از طریق ghno. تفاوت بزرگ‌تر از ۱ ریال نمایش داده می‌شود.");
    }

    private void showInvoices() {
        host.details("فاکتورهای دوره", new String[][]{
                {"از تاریخ", host.periodFrom()},
                {"تا تاریخ", host.periodTo()},
                {"منبع", "dbo.sailfact با active='t'"},
                {"توضیح", "برای دیدن فاکتورها وارد پرونده مشتری یا مطالبات شوید."}
        }, null);
    }
}

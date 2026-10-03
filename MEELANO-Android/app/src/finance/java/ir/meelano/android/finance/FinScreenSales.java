package ir.meelano.android.finance;

import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;

import org.json.JSONArray;
import org.json.JSONObject;

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
        header();
        card = ui.column();
        add(card);
    }

    private LinearLayout card;

    private void header() {
        LinearLayout head = ui.row();
        head.addView(ui.text("فروش در برابر وصول", 17f, ui.textColor, true), ui.lp(0, -2, 1f));
        head.addView(ui.chip(FinFmt.periodLabel(host.periodKey()), ui.goldAccent), ui.lp(-2, -2));
        add(head);
        LinearLayout periods = ui.row();
        periods.setPadding(0, ui.dp(6), 0, 0);
        String[] keys = {"today", "yesterday", "7d", "30d", "month", "lastmonth"};
        for (String k : keys) {
            boolean active = k.equals(host.periodKey());
            LinearLayout cell = ui.chip(FinFmt.periodLabel(k), active ? ui.goldAccent : ui.textFaint);
            cell.setOnClickListener(v -> host.setPeriod(k));
            LinearLayout.LayoutParams p = ui.lp(-2, -2);
            p.leftMargin = ui.dp(4);
            periods.addView(cell, p);
        }
        addCard(periods, 6);
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
        k.addView(ui.kpiTile("فروش دوره", compact(sales), FinFmt.CURRENCY, i(p, "salesCount") + " فاکتور", ui.goldAccent, v -> showInvoices()), ui.lp(0, -2, 1f));
        k.addView(ui.spacer(8), ui.lp(ui.dp(8), -2));
        k.addView(ui.kpiTile("وصول دوره", compact(receipts), FinFmt.CURRENCY, i(p, "receiptsCount") + " قبض", FinUi.SUCCESS, null), ui.lp(0, -2, 1f));
        card.addView(k, ui.lp(-1, -2));

        LinearLayout k2 = ui.row();
        LinearLayout.LayoutParams top = ui.lp(-1, -2);
        top.topMargin = ui.dp(8);
        k2.addView(ui.kpiTile("نسبت وصول", FinFmt.percent(receipts, sales), "", "وصول ÷ فروش دوره", FinUi.INFO, null), ui.lp(0, -2, 1f));
        k2.addView(ui.spacer(8), ui.lp(ui.dp(8), -2));
        k2.addView(ui.kpiTile("وصول‌نشده", compact(uncollected), FinFmt.CURRENCY, i(p, "uncollectedCount") + " فاکتور باز", FinUi.WARNING, v -> host.open(new FinScreenReceivables(host))), ui.lp(0, -2, 1f));
        card.addView(k2, top);

        LinearLayout mix = section("⇄", "ترکیب وصول", "نقد / چک / POS");
        mix.addView(ui.tableHeader(new String[]{"روش", "مبلغ", "تعداد"}), ui.lp(-1, -2));
        mix.addView(table(new String[]{"نقد (dar.naghd)", money(cash), fa(i(p, "cashCount"))}), ui.lp(-1, -2));
        mix.addView(table(new String[]{"چک (dar.mabcheck)", money(check), fa(i(p, "checkFiles"))}), ui.lp(-1, -2));
        mix.addView(table(new String[]{"POS (PosDetails)", money(pos), fa(i(p, "posCount"))}), ui.lp(-1, -2));
        mix.addView(ui.divider());
        mix.addView(table(new String[]{"جمع قبض‌ها (dar.mab)", money(receipts), fa(i(p, "receiptsCount"))}), ui.lp(-1, -2));
        mix.addView(ui.text("نقد + چک + POS معمولاً کمی با جمع قبض تفاوت دارد؛ موارد واقعی در بخش مغایرت‌ها فهرست می‌شود.",
                11f, ui.textFaint, false), ui.lp(-1, -2));
        addCard(mix, 12);

        byVisitor(p);
        mismatches(p);
    }

    private void byVisitor(JSONObject p) {
        JSONArray rows = arr(p, "byVisitor");
        LinearLayout card5 = section("☺", "عملکرد اپراتورها", "فاکتور و وصول به تفکیک ویزیتور");
        if (rows.length() == 0) {
            card5.addView(stateText("در این بازه فروشی برای اپراتوری ثبت نشده است.", ui.textDim));
        } else {
            card5.addView(ui.tableHeader(new String[]{"اپراتور", "فاکتور", "فروش", "وصول"}), ui.lp(-1, -2));
            for (int x = 0; x < rows.length() && x < 12; x++) {
                JSONObject r = rows.optJSONObject(x);
                if (r == null) continue;
                double sales = d(r, "sales");
                double receipts = d(r, "receipts");
                card5.addView(table(new String[]{s(r, "vis_name", "—"), fa(i(r, "invoices")), compact(sales), compact(receipts)}), ui.lp(-1, -2));
            }
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
            card6.addView(ui.button("مشاهده همه در مرکز مغایرت‌ها", ui.goldAccent, false, v -> host.open(new FinScreenProblems(host))), ui.lp(-1, -2));
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

    private View table(String[] cells) {
        return ui.tableRow(cells, new float[]{1.6f, 1f, 0.7f}, false, ui.goldAccent);
    }
}

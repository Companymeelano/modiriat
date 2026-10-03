package ir.meelano.android.finance;

import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * «مرکز کنترل مالی» — the home screen.
 *
 * Eight KPI tiles, each one a real aggregate of Atiran data, and every tile opens its own drill-down
 * (KPI → list → detail). Below them the alerts list contains only lines that a query produced, and
 * the 14-day chart contrasts daily sales (sailfact) with daily receipts (dar).
 *
 * Data sources: {@link FinQueries#home} (documented per figure) and {@link FinQueries#alerts}.
 */
public class FinScreenHome extends FinScreen {

    public FinScreenHome(AtiranFinanceActivity host) { super(host); }

    @Override public String title() { return "مرکز کنترل مالی"; }

    @Override public String glyph() { return "⌂"; }

    @Override protected String cacheKey() { return "home:" + host.today(); }

    @Override protected void populate() {
        LinearLayout head = ui.row();
        head.addView(ui.text("مرکز کنترل مالی", 17f, ui.textColor, true), ui.lp(0, -2, 1f));
        head.addView(ui.chip("امروز " + fa(host.today()), ui.goldAccent), ui.lp(-2, -2));
        add(head);
        LinearLayout actions = ui.row();
        actions.addView(ui.button("به‌روزرسانی", ui.goldAccent, false, v -> host.reload()), ui.lp(0, -2, 1f));
        actions.addView(ui.spacer(6), ui.lp(ui.dp(6), -2));
        actions.addView(ui.button("گزارش روز", ui.silver, false, v -> host.open(new FinScreenDaily(host))), ui.lp(0, -2, 1f));
        addCard(actions, 8);

        addCard(loading("شاخص‌های مالی"), 10);
        alertBox = ui.column();
        box.addView(alertBox, top(10));
        chartBox = ui.column();
        box.addView(chartBox, ui.lp(-1, -2));
        listBox = ui.column();
        box.addView(listBox, ui.lp(-1, -2));
    }

    private LinearLayout alertBox, chartBox, listBox;

    @Override protected void fetch() {
        final String base = "home:" + host.today();
        db.read(key(base), 60_000L, c -> {
            JSONObject payload = FinQueries.home(c, FinQueries.serverToday(c));
            payload.put("alerts", FinQueries.alerts(c, FinQueries.serverToday(c)));
            payload.put("mismatchList", FinQueries.receiptMismatches(c, MeelanoJalaliMonthStart(payload), FinQueries.serverToday(c), 12));
            JSONArray a = new JSONArray();
            a.put(payload);
            return a;
        }, env -> render(env));
    }

    private static String MeelanoJalaliMonthStart(JSONObject payload) {
        String t = payload.optString("today", FinFmt.todayLocal());
        return ir.meelano.android.MeelanoJalali.monthStart(t);
    }

    private void render(JSONObject env) {
        box.removeAllViews();
        ui.pad(box);
        // rebuild the fixed structure, then fill the three dynamic areas
        LinearLayout head = ui.row();
        head.addView(ui.text("مرکز کنترل مالی", 17f, ui.textColor, true), ui.lp(0, -2, 1f));
        head.addView(ui.chip("امروز " + fa(host.today()), ui.goldAccent), ui.lp(-2, -2));
        add(head);
        LinearLayout actions = ui.row();
        actions.addView(ui.button("به‌روزرسانی", ui.goldAccent, false, v -> host.reload()), ui.lp(0, -2, 1f));
        actions.addView(ui.spacer(6), ui.lp(ui.dp(6), -2));
        actions.addView(ui.button("گزارش روز", ui.silver, false, v -> host.open(new FinScreenDaily(host))), ui.lp(0, -2, 1f));
        addCard(actions, 8);

        if (env.optBoolean("stale", false)) {
            LinearLayout st = staleTag(env);
            if (st != null) addCard(st, 10);
        }

        JSONObject p = payload(env);
        if (p.length() == 0) {
            addCard(problem(env.optString("error", ""), () -> load(true)), 10);
            return;
        }

        addCard(kpis(p), 12);
        alerts(p);
        chart(p);
        worklist(p);
        legend();
    }

    // ------------------------------------------------------------------ KPI tiles (max 8)

    private LinearLayout kpis(JSONObject p) {
        LinearLayout wrap = ui.column();
        wrap.addView(ui.sectionTitle("شاخص‌های کلیدی", "هر کارت به ریزمحاسبه باز می‌شود", "◆"), ui.lp(-1, -2));

        String[][] row1 = new String[][]{
                {"فروش امروز", compact(d(p, "salesToday")), i(p, "salesCountToday") + " فاکتور"},
                {"وصول امروز", compact(d(p, "receivedToday")), i(p, "receiptsToday") + " قبض"},
                {"نقد امروز", compact(d(p, "cashToday")), "صندوق: " + compact(d(p, "cashBoxBalance"))},
                {"چک امروز", compact(d(p, "checkToday")), i(p, "checksDueToday") + " سررسید امروز"}
        };
        wrap.addView(kpiRow(row1, p), ui.lp(-1, -2));

        String[][] row2 = new String[][]{
                {"POS امروز", compact(d(p, "posToday")), i(p, "posCountToday") + " تراکنش"},
                {"مطالبات", compact(d(p, "receivableTotal")), i(p, "receivableCustomers") + " مشتری بدهکار"},
                {"بانک‌ها", compact(d(p, "bankTotal")), i(p, "bankCount") + " حساب فعال"},
                {"مغایرت‌ها", fa(i(p, "mismatchMonth")) + " مورد", "POS بدون قبض: " + fa(i(p, "posWithoutReceipt"))}
        };
        LinearLayout r2 = kpiRow(row2, p);
        LinearLayout.LayoutParams p2 = ui.lp(-1, -2);
        p2.topMargin = ui.dp(8);
        wrap.addView(r2, p2);
        return wrap;
    }

    private LinearLayout kpiRow(String[][] specs, JSONObject p) {
        LinearLayout r = ui.row();
        for (int index = 0; index < specs.length; index++) {
            final int idx = index;
            String[] row = specs[index];
            LinearLayout tile = ui.kpiTile(row[0], row[1], FinFmt.CURRENCY, row[2], accentFor(idx), v -> {
                switch (idx) {
                    case 0: host.open(new FinScreenSales(host)); break;
                    case 1: host.setPeriod("today"); host.open(new FinScreenSales(host)); break;
                    case 2: host.open(new FinScreenCash(host)); break;
                    case 3: host.open(new FinScreenCheques(host)); break;
                    case 4: host.open(new FinScreenPos(host)); break;
                    case 5: host.open(new FinScreenReceivables(host)); break;
                    case 6: host.open(new FinScreenBanks(host)); break;
                    default: host.open(new FinScreenProblems(host)); break;
                }
            });
            LinearLayout.LayoutParams lp = ui.lp(0, -2, 1f);
            lp.leftMargin = ui.dp(index == 0 ? 0 : 4);
            r.addView(tile, lp);
        }
        return r;
    }

    private int accentFor(int index) {
        switch (index) {
            case 1: return FinUi.SUCCESS;
            case 2: return FinUi.INFO;
            case 3: return ui.goldAccent;
            case 4: return FinUi.MANAGER;
            case 5: return FinUi.WARNING;
            case 6: return ui.goldSoft;
            default: return FinUi.DANGER;
        }
    }

    // ------------------------------------------------------------------ alerts

    private void alerts(JSONObject p) {
        JSONArray alerts = arr(p, "alerts");
        LinearLayout card = section("⚠", "هشدارها و کارهای باز", alerts.length() + " مورد از داده واقعی");
        if (alerts.length() == 0) {
            card.addView(stateText("هیچ هشدار بازی وجود ندارد؛ همه چیز در وضعیت عادی است.", FinUi.SUCCESS));
        } else {
            for (int a = 0; a < alerts.length(); a++) {
                JSONObject alert = alerts.optJSONObject(a);
                if (alert == null) continue;
                LinearLayout row = ui.row();
                int color = alert.optInt("color", FinUi.INFO);
                LinearLayout left = ui.column();
                left.addView(ui.text(alert.optString("title"), 13f, ui.textColor, true), ui.lp(-1, -2));
                left.addView(ui.text(alert.optString("body"), 11.5f, ui.textDim, false), ui.lp(-1, -2));
                row.addView(left, ui.lp(0, -2, 1f));
                row.addView(ui.chip("مشاهده", color), ui.lp(-2, -2));
                row.setOnClickListener(v -> openAlert(alert.optString("key")));
                row.setClickable(true);
                LinearLayout.LayoutParams rp = ui.lp(-1, -2);
                rp.topMargin = ui.dp(6);
                card.addView(row, rp);
            }
        }
        addCard(card, 12);
    }

    private void openAlert(String alertKey) {
        if ("checks_due_today".equals(alertKey) || "checks_next7".equals(alertKey)) {
            host.open(new FinScreenCheques(host));
        } else if ("checks_overdue".equals(alertKey) || "paid_overdue".equals(alertKey)) {
            host.open(new FinScreenCheques(host));
        } else if ("receipt_mismatch".equals(alertKey) || "pos_unlinked".equals(alertKey)) {
            host.open(new FinScreenProblems(host));
        } else if ("negative_balance".equals(alertKey)) {
            host.open(new FinScreenReceivables(host));
        } else if ("recon_open".equals(alertKey)) {
            host.open(new FinScreenRecon(host));
        } else {
            host.open(new FinScreenMore(host));
        }
    }

    // ------------------------------------------------------------------ chart

    private void chart(JSONObject p) {
        JSONArray labels = arr(p, "labels");
        JSONArray sales = arr(p, "salesSeries");
        JSONArray receipts = arr(p, "receiptSeries");
        String[] lab = new String[labels.length()];
        double[] salesV = new double[labels.length()];
        double[] receiptV = new double[labels.length()];
        for (int x = 0; x < labels.length(); x++) {
            lab[x] = labels.optString(x);
            salesV[x] = sales.optDouble(x, 0d);
            receiptV[x] = receipts.optDouble(x, 0d);
        }
        LinearLayout card = section("▤", "فروش و وصول ۱۴ روز گذشته", "sailfact در برابر dar");
        if (lab.length == 0) {
            card.addView(stateText("داده‌ای در بازه دو هفته اخیر ثبت نشده است.", ui.textDim));
        } else {
            card.addView(ui.text("فروش روزانه", 12f, ui.goldAccent, true), ui.lp(-1, -2));
            card.addView(ui.barChart(lab, salesV, ui.goldAccent), ui.lp(-1, -2));
            card.addView(ui.spacer(8));
            card.addView(ui.text("وصول روزانه", 12f, FinUi.SUCCESS, true), ui.lp(-1, -2));
            card.addView(ui.barChart(lab, receiptV, FinUi.SUCCESS), ui.lp(-1, -2));
        }
        addCard(card, 12);
    }

    // ------------------------------------------------------------------ work list

    private void worklist(JSONObject p) {
        LinearLayout card = section("☑", "کارهای نیازمند بررسی", "از داده واقعی، هر ردیف قابل بازکردن است");
        card.addView(workRow("مغایرت اجزای دریافت", i(p, "mismatchMonth") + " قبض", FinUi.DANGER, v -> host.open(new FinScreenProblems(host))));
        card.addView(workRow("تراکنش POS بدون قبض", i(p, "posWithoutReceipt") + " تراکنش", FinUi.WARNING, v -> host.open(new FinScreenPos(host))));
        card.addView(workRow("چک‌های سررسید گذشته", i(p, "checksOverdue") + " چک · " + money(d(p, "checksOverdueAmount")), FinUi.DANGER, v -> host.open(new FinScreenCheques(host))));
        card.addView(workRow("چک پرداختی معوق", i(p, "paidChecksOverdue") + " چک", FinUi.WARNING, v -> host.open(new FinScreenCheques(host))));
        addCard(card, 12);
    }

    private View workRow(String title, String value, int color, View.OnClickListener tap) {
        LinearLayout r = ui.row();
        r.setPadding(0, ui.dp(8), 0, ui.dp(8));
        r.addView(ui.text(title, 13f, ui.textColor, false), ui.lp(0, -2, 1f));
        TextView v = ui.text(value, 12.5f, color, true);
        v.setGravity(Gravity.END);
        r.addView(v, ui.lp(0, -2, 1f));
        r.addView(ui.text("›", 16f, ui.textFaint, true), ui.lp(-2, -2));
        r.setOnClickListener(tap);
        r.setClickable(true);
        return r;
    }

    private void legend() {
        LinearLayout card = section("ℹ", "مبنای محاسبه", "هر عدد از یک منبع مشخص می‌آید");
        List<String> lines = new ArrayList<>();
        lines.add("فروش: SUM(sailfact.[all]) با active='t' — بازه ثبت فاکتور");
        lines.add("وصول: SUM(dar.mab) با p=0 — قبض دریافت");
        lines.add("POS: SUM(PosDetails.MabPos) متصل به قبض از طریق ghno");
        lines.add("مطالبات: مانده واقعی CUSTOMERS.man (با گردش cust_act تطبیق داده شده)");
        lines.add("چک‌ها: getchk برای دریافتی و putchk برای پرداختی، با تاریخ شمسی char(10)");
        lines.add("موجودی بانک: SUM(BANK.MAN) برای حساب‌های فعال؛ گردش از ban_act");
        lines.add("صندوق: SUM(COW.BED-COW.BES) برای ردیف‌های فعال");
        for (String line : lines) {
            TextView t = ui.text("• " + line, 11.5f, ui.textDim, false);
            t.setPadding(0, ui.dp(3), 0, 0);
            card.addView(t, ui.lp(-1, -2));
        }
        addCard(card, 12);
    }
}

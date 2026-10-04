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
 * «مرکز کنترل مالی» — the home screen.
 *
 * Eight KPI tiles, each one a real aggregate of Atiran data, and every tile opens its own drill-down
 * (KPI → list → detail). Below them the alerts list contains only lines that a query produced, and the
 * charts contrast daily sales (sailfact) with daily receipts (dar), show the shape of the collection
 * mix and rank the largest debts.
 *
 * Data sources: {@link FinQueries#home} (documented per figure), {@link FinQueries#alerts} and
 * {@link FinQueries#topDebtors}.
 */
public class FinScreenHome extends FinScreen {

    public FinScreenHome(AtiranFinanceActivity host) { super(host); }

    @Override public String title() { return "مرکز کنترل مالی"; }

    @Override public String glyph() { return "⌂"; }

    @Override protected String cacheKey() { return "home:" + host.today(); }

    @Override protected void populate() {
        add(hero("مرکز کنترل مالی", "فروش، وصول، چک، مطالبات و بانک — همه از داده واقعی آتیران", ui.goldAccent));
        LinearLayout actions = ui.row();
        actions.addView(ui.primaryButton("⟳  به‌روزرسانی", ui.goldAccent, v -> host.reload()), ui.lp(0, -2, 1f));
        actions.addView(ui.spacer(6), ui.lp(ui.dp(6), -2));
        actions.addView(ui.ghostButton("☑  گزارش روز", ui.silver, v -> host.open(new FinScreenDaily(host))), ui.lp(0, -2, 1f));
        addCard(actions, 10);

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
        db.read(key(base), 60_000L, c -> payload(c), env -> render(env));
    }

    /**
     * The complete home payload, read from the database. The post-login preload calls this same
     * method, so a screen can never show something different from what the prefetch read.
     */
    public static JSONArray payload(Connection c) throws Exception {
        String today = FinQueries.serverToday(c);
        JSONObject payload = FinQueries.home(c, today);
        payload.put("alerts", FinQueries.alerts(c, today));
        payload.put("mismatchList", FinQueries.receiptMismatches(c, MeelanoJalaliMonthStart(payload), today, 12));
        payload.put("topDebtors", FinQueries.topDebtors(c, 6));
        JSONArray a = new JSONArray();
        a.put(payload);
        return a;
    }

    private static String MeelanoJalaliMonthStart(JSONObject payload) {
        String t = payload.optString("today", FinFmt.todayLocal());
        return ir.meelano.android.MeelanoJalali.monthStart(t);
    }

    private void render(JSONObject env) {
        box.removeAllViews();
        ui.pad(box);
        add(hero("مرکز کنترل مالی", "فروش، وصول، چک، مطالبات و بانک — همه از داده واقعی آتیران", ui.goldAccent));
        LinearLayout actions = ui.row();
        actions.addView(ui.primaryButton("⟳  به‌روزرسانی", ui.goldAccent, v -> host.reload()), ui.lp(0, -2, 1f));
        actions.addView(ui.spacer(6), ui.lp(ui.dp(6), -2));
        actions.addView(ui.ghostButton("☑  گزارش روز", ui.silver, v -> host.open(new FinScreenDaily(host))), ui.lp(0, -2, 1f));
        addCard(actions, 10);

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
        FinCrash.step(host, "home:kpis");
        // The chart sections are added one frame later: the first real numbers appear immediately and
        // the cost of building every canvas is spread over frames instead of one large spike.
        box.postDelayed(() -> {
            safe("وضعیت وصول و ترکیب دریافت", () -> collectionCard(p));
            safe("هشدارها و کارهای باز", () -> alerts(p));
            safe("روند فروش و وصول", () -> trend(p));
            safe("بزرگ‌ترین بدهکاران", () -> debtors(p));
            safe("فهرست کارها", () -> worklist(p));
            safe("راهنمای نمودارها", () -> legend());
            FinCrash.step(host, "home:sections-done");
        }, 32L);
    }

    /**
     * Runs one section of the home screen. A section that fails shows its own red note with the
     * exception and does not cost the operator the other seven sections.
     */
    private void safe(String label, Runnable section) {
        try {
            section.run();
        } catch (Throwable t) {
            FinCrash.log(host, "home-section", label + ": " + t.getClass().getName() + ": " + t.getMessage());
            LinearLayout card = ui.cardTone(FinUi.DANGER);
            card.addView(ui.text("⚠  نمایش «" + label + "» ممکن نشد", 12.5f, ui.textColor, true), ui.lp(-1, -2));
            card.addView(ui.text(FinHealth.shortError(t), 11f, ui.textDim, false), ui.lp(-1, -2));
            card.addView(ui.text("کد رویداد: " + FinCrash.eventCode(t), 11f, FinUi.DANGER, true), ui.lp(-1, -2));
            addCard(card, 12);
        }
    }

    // ------------------------------------------------------------------ KPI tiles (max 8)

    private LinearLayout kpis(JSONObject p) {
        LinearLayout wrap = ui.column();
        wrap.addView(ui.sectionTitle("شاخص‌های کلیدی", "هر کارت به ریزمحاسبه باز می‌شود", "◆"), ui.lp(-1, -2));
        wrap.addView(kpiRow(new String[][]{
                {"فروش امروز", compact(d(p, "salesToday")), "ریال", i(p, "salesCountToday") + " فاکتور", null},
                {"وصول امروز", compact(d(p, "receivedToday")), "ریال", i(p, "receiptsToday") + " قبض", null}
        }, p, 0, true), ui.lp(-1, -2));
        wrap.addView(kpiRow(new String[][]{
                {"نقد امروز", compact(d(p, "cashToday")), "ریال", "صندوق: " + compact(d(p, "cashBoxBalance")), null},
                {"چک امروز", compact(d(p, "checkToday")), "ریال", i(p, "checksDueToday") + " سررسید امروز", null}
        }, p, 2, true), top(8));
        wrap.addView(kpiRow(new String[][]{
                {"POS امروز", compact(d(p, "posToday")), "ریال", i(p, "posCountToday") + " تراکنش", null},
                {"مطالبات", compact(d(p, "receivableTotal")), "ریال", i(p, "receivableCustomers") + " مشتری بدهکار", null}
        }, p, 4, true), top(8));
        wrap.addView(kpiRow(new String[][]{
                {"بانک‌ها", compact(d(p, "bankTotal")), "ریال", i(p, "bankCount") + " حساب فعال", null},
                {"مغایرت‌ها", fa(i(p, "mismatchMonth")), "مورد", "POS بدون قبض: " + fa(i(p, "posWithoutReceipt")), null}
        }, p, 6, true), top(8));
        return wrap;
    }

    /**
     * One row of two KPI tiles. The accent and the drill-down of every tile are decided here from its
     * index in the whole grid, so a tile never opens the wrong screen and never changes colour by
     * accident (the previous version coloured the first tile red).
     */
    private LinearLayout kpiRow(String[][] specs, JSONObject p, int indexBase, boolean withTrend) {
        LinearLayout r = ui.row();
        for (int k = 0; k < specs.length; k++) {
            final int index = indexBase + k;
            String[] spec = specs[k];
            int accent = accentFor(index);
            double[] trend = withTrend ? trendFor(p, index) : null;
            LinearLayout tile = ui.kpiTile(spec[0], spec[1], spec[2], spec[3], accent, trend, v -> openKpi(index));
            LinearLayout.LayoutParams lp = ui.lp(0, -2, 1f);
            lp.leftMargin = ui.dp(k == 0 ? 0 : 8);
            r.addView(tile, lp);
        }
        return r;
    }

    private int accentFor(int index) {
        switch (index) {
            case 1: return FinUi.SUCCESS;        // وصول
            case 2: return FinUi.INFO;           // نقد
            case 3: return ui.goldAccent;        // چک
            case 4: return FinUi.MANAGER;        // POS
            case 5: return FinUi.WARNING;        // مطالبات
            case 6: return ui.goldSoft;          // بانک‌ها
            case 7: return FinUi.DANGER;         // مغایرت‌ها
            default: return ui.goldAccent;       // فروش
        }
    }

    private void openKpi(int index) {
        switch (index) {
            case 0: host.open(new FinScreenSales(host)); break;
            case 1: host.setPeriod("today"); host.open(new FinScreenSales(host)); break;
            case 2: host.open(new FinScreenCash(host)); break;
            case 3: host.open(new FinScreenCheques(host)); break;
            case 4: host.open(new FinScreenPos(host)); break;
            case 5: host.open(new FinScreenReceivables(host)); break;
            case 6: host.open(new FinScreenBanks(host)); break;
            default: host.open(new FinScreenProblems(host)); break;
        }
    }

    /** The last 14 days as a tile sparkline — only for the tiles that have a daily series. */
    private double[] trendFor(JSONObject p, int index) {
        JSONArray sales = arr(p, "salesSeries"), receipts = arr(p, "receiptSeries");
        JSONArray source = index == 1 ? receipts : (index == 0 ? sales : null);
        if (source == null || source.length() == 0) return null;
        double[] v = new double[source.length()];
        for (int i = 0; i < source.length(); i++) v[i] = source.optDouble(i, 0d);
        return v;
    }

    // ------------------------------------------------------------------ collection ring + mix donut

    private void collectionCard(JSONObject p) {
        LinearLayout card = section("◎", "وضعیت وصول امروز", "نسبت وصول به فروش و ترکیب مبلغ دریافت‌شده");
        double sales = d(p, "salesToday"), received = d(p, "receivedToday");
        LinearLayout row = ui.row();
        FinCharts.Ring ring = ui.ringChart();
        ring.data(received, sales <= 0 ? Math.max(1, received) : sales, "نسبت وصول", received >= sales ? FinUi.SUCCESS : FinUi.INFO);
        ring.note("فروش " + compact(sales));
        LinearLayout.LayoutParams rlp = new LinearLayout.LayoutParams(ui.dp(148), ui.dp(148));
        rlp.gravity = Gravity.CENTER_HORIZONTAL;
        row.addView(ring, rlp);

        double cash = d(p, "cashToday"), check = d(p, "checkToday"), pos = d(p, "posToday");
        List<FinCharts.Legend> mix = new ArrayList<>();
        int[] palette = ui.palette();
        mix.add(new FinCharts.Legend("نقد (dar.naghd)", money(cash), palette[1]));
        mix.add(new FinCharts.Legend("چک (dar.mabcheck)", money(check), palette[0]));
        mix.add(new FinCharts.Legend("POS", money(pos), palette[2]));
        mix.add(new FinCharts.Legend("جمع وصول", money(received), palette[5]));
        LinearLayout legend = ui.legend(mix, received);
        LinearLayout.LayoutParams llp = ui.lp(0, -2, 1f);
        llp.leftMargin = ui.dp(10);
        row.addView(legend, llp);
        card.addView(row, ui.lp(-1, -2));

        double componentSum = cash + check + pos;
        if (received > 0 && Math.abs(componentSum - received) > 1) {
            TextView warn = ui.text("⚠ اجزای امروز (نقد + چک + POS) با جمع قبض‌های امروز " + money(Math.abs(componentSum - received))
                    + " ریال تفاوت دارد؛ موارد در «کارهای باز» فهرست می‌شود.", 10.5f, FinUi.WARNING, false);
            warn.setPadding(0, ui.dp(6), 0, 0);
            card.addView(warn, ui.lp(-1, -2));
        }
        addCard(card, 12);
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
                ui.applyTouch(row);
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

    // ------------------------------------------------------------------ 14-day trend

    private void trend(JSONObject p) {
        JSONArray labels = arr(p, "labels");
        JSONArray sales = arr(p, "salesSeries");
        JSONArray receipts = arr(p, "receiptSeries");
        int n = labels.length();
        LinearLayout card = section("▤", "فروش و وصول ۱۴ روز گذشته", "sailfact در برابر dar — برای دیدن عدد هر روز، روی نمودار بزنید");
        if (n == 0) {
            card.addView(stateText("داده‌ای در بازه دو هفته اخیر ثبت نشده است.", ui.textDim));
            addCard(card, 12);
            return;
        }
        String[] lab = new String[n];
        double[] salesV = new double[n];
        double[] receiptV = new double[n];
        for (int x = 0; x < n; x++) {
            lab[x] = labels.optString(x);
            salesV[x] = sales.optDouble(x, 0d);
            receiptV[x] = receipts.optDouble(x, 0d);
        }
        FinCharts.Area area = ui.areaChart(FinUi.FormatterKind.MONEY);
        area.data(lab, new double[][]{salesV, receiptV},
                new int[]{ui.goldAccent, FinUi.SUCCESS},
                new String[]{"فروش", "وصول"}, true);
        addChart(card, area, 210);

        double sumSales = 0, sumReceipts = 0;
        for (int x = 0; x < n; x++) { sumSales += salesV[x]; sumReceipts += receiptV[x]; }
        card.addView(ui.miniStat("جمع فروش ۱۴ روز", compact(sumSales) + " ریال", ui.goldAccent), ui.lp(-1, -2));
        card.addView(ui.divider(), ui.lp(-1, -2));
        card.addView(ui.miniStat("جمع وصول ۱۴ روز", compact(sumReceipts) + " ریال", FinUi.SUCCESS), ui.lp(-1, -2));
        card.addView(ui.divider(), ui.lp(-1, -2));
        card.addView(ui.miniStat("نسبت وصول به فروش", FinFmt.percent(sumReceipts, sumSales), FinUi.INFO), ui.lp(-1, -2));
        addCard(card, 12);
    }

    // ------------------------------------------------------------------ biggest debts

    private void debtors(JSONObject p) {
        JSONArray rows = arr(p, "topDebtors");
        LinearLayout card = section("☰", "بزرگ‌ترین بدهکاران", "مانده واقعی CUSTOMERS.man — برای پرونده مشتری روی ردیف بزنید");
        if (rows.length() == 0) {
            card.addView(stateText("مشتری بدهکاری ثبت نشده است.", FinUi.SUCCESS));
            addCard(card, 12);
            return;
        }
        int n = Math.min(6, rows.length());
        String[] labels = new String[n];
        double[] values = new double[n];
        int[] colors = new int[n];
        String[] notes = new String[n];
        int[] palette = ui.palette();
        for (int x = 0; x < n; x++) {
            JSONObject r = rows.optJSONObject(x);
            labels[x] = s(r, "MONAME", "—");
            values[x] = d(r, "man");
            colors[x] = palette[x % palette.length];
            notes[x] = "کد " + fa(i(r, "SHMO")) + " · " + fa(i(r, "open_invoices")) + " فاکتور باز";
        }
        FinCharts.Bars bars = ui.barsChart(FinUi.FormatterKind.MONEY);
        bars.data(labels, values, colors, notes);
        addBars(card, bars, n);
        card.addView(ui.ghostButton("⇄  فهرست کامل مطالبات", ui.goldAccent, v -> host.open(new FinScreenReceivables(host))), top(8));
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
        ui.applyTouch(r);
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

package ir.meelano.android.finance;

import android.widget.LinearLayout;

import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.Connection;

/**
 * «مرکز کارهای باز» — the three real sources of open work, each with its own query:
 * receipts whose components do not add up, POS transactions without a receipt, and bank movements
 * without a receipt. Nothing is inferred: a row appears here only if a query returned it.
 */
public class FinScreenProblems extends FinScreen {

    public FinScreenProblems(AtiranFinanceActivity host) { super(host); }

    @Override public String title() { return "کارهای باز"; }

    @Override public String glyph() { return "⚠"; }

    @Override protected String cacheKey() { return "problems:" + host.periodFrom() + ":" + host.periodTo(); }

    private LinearLayout body;

    @Override protected void populate() {
        add(hero("کارهای باز و مغایرت‌ها", "سه منبع واقعی باز: اجزای قبض، POS بدون قبض و گردش بانکی بدون قبض", FinUi.DANGER));
        body = ui.column();
        add(body);
    }

    @Override protected void fetch() {
        db.read(key(cacheKey()), 60_000L, c -> queryPayload(c, host.periodFrom(), host.periodTo()), this::render);
    }

    /** Open reconciliation work of the period, read from the database. The post-login preload calls this same method. */
    public static JSONArray queryPayload(Connection c, String from, String to) throws Exception {
        String today = FinQueries.serverToday(c);
        JSONObject payload = new JSONObject();
        payload.put("today", today);
        payload.put("mismatches", FinQueries.receiptMismatches(c, from, to, 40));
        payload.put("mismatchCount", FinQueries.mismatchCount(c, from, to));
        payload.put("pos", FinQueries.posWithoutReceipt(c, 40));
        payload.put("bank", FinQueries.bankWithoutReceipt(c, from, to, 40));
        JSONArray a = new JSONArray();
        a.put(payload);
        return a;
    }

    /** Open work at a glance: how much of every kind, and how much money is behind it. */
    private void openWork(JSONObject p, JSONArray mismatches, JSONArray pos, JSONArray bank) {
        double mismatchAmount = 0;
        for (int x = 0; x < mismatches.length(); x++) {
            JSONObject r = mismatches.optJSONObject(x);
            if (r != null) mismatchAmount += Math.abs(d(r, "difference"));
        }
        double posAmount = 0;
        for (int x = 0; x < pos.length(); x++) {
            JSONObject r = pos.optJSONObject(x);
            if (r != null) posAmount += d(r, "MabPos");
        }
        double bankAmount = 0;
        for (int x = 0; x < bank.length(); x++) {
            JSONObject r = bank.optJSONObject(x);
            if (r != null) bankAmount += Math.abs(d(r, "act_bed") - d(r, "act_bes"));
        }
        LinearLayout card = section("◎", "نمای کلی کارهای باز", "تعداد موارد و مبلغ درگیر");
        int[] palette = ui.palette();
        double[] counts = new double[]{i(p, "mismatchCount"), pos.length(), bank.length()};
        String[] labels = new String[]{"مغایرت قبض", "POS بدون قبض", "بانک بدون قبض"};
        int[] colors = new int[]{palette[5], palette[3], palette[2]};
        FinCharts.Donut donut = ui.donutChart(FinUi.FormatterKind.COUNT);
        donut.data(labels, counts, colors, "موارد باز");
        donut.empty("هیچ کار بازی وجود ندارد.");
        java.util.List<FinCharts.Legend> items = new java.util.ArrayList<>();
        items.add(new FinCharts.Legend("مغایرت قبض", FinFmt.count(Math.round(counts[0])), colors[0]));
        items.add(new FinCharts.Legend("POS بدون قبض", FinFmt.count(Math.round(counts[1])), colors[1]));
        items.add(new FinCharts.Legend("بانک بدون قبض", FinFmt.count(Math.round(counts[2])), colors[2]));
        card.addView(ui.donutWithLegend(donut, items, counts[0] + counts[1] + counts[2], 158), ui.lp(-1, -2));
        card.addView(ui.miniStat("مبلغ درگیر در مغایرت‌های قبض", money(mismatchAmount) + " ریال", palette[5]), top(6));
        card.addView(ui.miniStat("مبلغ POS بدون قبض (نمونه)", money(posAmount) + " ریال", palette[3]), ui.lp(-1, -2));
        card.addView(ui.miniStat("مبلغ گردش بانکی بدون قبض (نمونه)", money(bankAmount) + " ریال", palette[2]), ui.lp(-1, -2));
        body.addView(card, top(12));
    }

    private void render(JSONObject env) {
        body.removeAllViews();
        if (env.optBoolean("stale", false)) {
            LinearLayout st = staleTag(env);
            if (st != null) body.addView(st, ui.lp(-1, -2));
        }
        JSONObject p = payload(env);
        if (p.length() == 0) {
            body.addView(problem(env.optString("error", ""), () -> load(true)), ui.lp(-1, -2));
            return;
        }

        JSONArray mismatches = arr(p, "mismatches");
        JSONArray pos = arr(p, "pos");
        JSONArray bank = arr(p, "bank");

        LinearLayout k = ui.row();
        k.addView(ui.kpiTile("مغایرت قبض", fa(i(p, "mismatchCount")), "مورد", "جمع ≠ نقد+چک+POS", FinUi.DANGER, null), ui.lp(0, -2, 1f));
        k.addView(ui.spacer(6), ui.lp(ui.dp(6), -2));
        k.addView(ui.kpiTile("POS بدون قبض", fa(pos.length()), "مورد", "نمونه ۴۰ مورد اول", FinUi.WARNING, null), ui.lp(0, -2, 1f));
        k.addView(ui.spacer(6), ui.lp(ui.dp(6), -2));
        k.addView(ui.kpiTile("گردش بانکی بدون قبض", fa(bank.length()), "مورد", "ban_act", FinUi.INFO, null), ui.lp(0, -2, 1f));
        body.addView(k, ui.lp(-1, -2));

        openWork(p, mismatches, pos, bank);

        LinearLayout c1 = section("≠", "مغایرت اجزای دریافت", "از قبض‌های بازه انتخاب‌شده");
        if (mismatches.length() == 0) {
            c1.addView(stateText("همه قبض‌های این بازه با اجزای خود می‌خوانند.", FinUi.SUCCESS));
        } else {
            for (int x = 0; x < mismatches.length() && x < 25; x++) {
                JSONObject r = mismatches.optJSONObject(x);
                if (r == null) continue;
                c1.addView(ui.listRow(s(r, "name", "—") + " · قبض " + fa(i(r, "ghno")),
                        s(r, "date", "—") + " · نقد " + money(d(r, "naghd")) + " · چک " + money(d(r, "mabcheck"))
                                + " · POS " + money(d(r, "pos")),
                        money(d(r, "difference")), "مغایرت", FinUi.DANGER,
                        v -> host.details("مغایرت قبض " + fa(i(r, "ghno")), new String[][]{
                                {"تاریخ", s(r, "date", "—")},
                                {"مشتری", s(r, "name", "—")},
                                {"جمع قبض", money(d(r, "mab"))},
                                {"نقد", money(d(r, "naghd"))},
                                {"چک", money(d(r, "mabcheck"))},
                                {"POS", money(d(r, "pos"))},
                                {"تفاوت", money(d(r, "difference"))},
                                {"اپراتور روی قبض", s(r, "visitor_name", "ثبت‌نشده")}
                        }, "منبع: dar + PosDetails. برای ثبت پرونده، مرکز مغایرت را باز کنید.")), ui.lp(-1, -2));
            }
        }
        c1.addView(ui.button("ثبت و پیگیری در مرکز مغایرت", ui.goldAccent, false, v -> host.open(new FinScreenRecon(host))), ui.lp(-1, -2));
        body.addView(c1, top(12));

        LinearLayout c2 = section("▣", "POS بدون قبض", "تراکنش‌هایی که ردیف متناظر در dar ندارند");
        if (pos.length() == 0) {
            c2.addView(stateText("همه تراکنش‌های POS به قبض متصل هستند.", FinUi.SUCCESS));
        } else {
            for (int x = 0; x < pos.length() && x < 20; x++) {
                JSONObject r = pos.optJSONObject(x);
                if (r == null) continue;
                c2.addView(ui.listRow("تراکنش " + fa(i(r, "ID")) + " · قبض " + fa(i(r, "ghno")),
                        s(r, "bank_name", "—") + " · اپراتور " + fa(i(r, "UserID")) + " · پیگیری " + s(r, "ShPeigiri", "—"),
                        money(d(r, "MabPos")), "بدون قبض", FinUi.WARNING, null), ui.lp(-1, -2));
            }
        }
        body.addView(c2, top(12));

        LinearLayout c3 = section("▤", "گردش بانکی بدون قبض", "ban_act بدون پیوند به dar");
        if (bank.length() == 0) {
            c3.addView(stateText("همه گردش‌های این بازه به قبض متصل هستند.", FinUi.SUCCESS));
        } else {
            for (int x = 0; x < bank.length() && x < 20; x++) {
                JSONObject r = bank.optJSONObject(x);
                if (r == null) continue;
                c3.addView(ui.listRow(FinQueries.bankKindLabel(i(r, "act_id")) + " · " + s(r, "act_date", "—"),
                        s(r, "bank_name", "—") + " · کد " + fa(i(r, "rdf")) + " · قبض " + fa(i(r, "Ghno")),
                        money(d(r, "act_bed") - d(r, "act_bes")), "بدون قبض", FinUi.WARNING, null), ui.lp(-1, -2));
            }
        }
        body.addView(c3, top(12));
    }
}

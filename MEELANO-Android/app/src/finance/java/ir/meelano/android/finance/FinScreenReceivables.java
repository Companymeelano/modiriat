package ir.meelano.android.finance;

import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.Connection;

import java.sql.PreparedStatement;

/**
 * «مطالبات و سنی‌بندی» — receivables, aging bands and collection follow-up.
 *
 * Balance is Atiran's own authoritative {@code CUSTOMERS.man}: the validation run compared it with
 * the customer ledger ({@code cust_act}) for every one of the 2724 customers and they agreed, so the
 * app never recomputes a balance and never writes one back.
 *
 * The aging bands (0-7 / 8-30 / 31-60 / 61-90 / 90+) are computed from open invoices that still carry
 * a remaining amount ({@code bamandeh <> 0}); a band that has no rows is shown as zero — the live
 * data currently spans about one month, so the older bands are legitimately empty.
 *
 * Follow-up entries and promises to pay are stored in the app's own table
 * ({@code meelano_fin_followup}) inside a guarded transaction with an audit row and duplicate
 * protection — the main Atiran tables are never written to.
 */
public class FinScreenReceivables extends FinScreen {

    public FinScreenReceivables(AtiranFinanceActivity host) { super(host); }

    @Override public String title() { return "مطالبات"; }

    @Override public String glyph() { return "⇄"; }

    @Override protected String cacheKey() { return "recv:" + host.today(); }

    private LinearLayout body;

    @Override protected void populate() {
        add(hero("مطالبات و سنی‌بندی", "مانده واقعی CUSTOMERS.man که با دفتر cust_act تطبیق داده شده است", FinUi.WARNING));
        body = ui.column();
        add(body);
    }

    @Override protected void fetch() {
        db.read(key(cacheKey()), 120_000L, c -> queryPayload(c), this::render);
    }

    /** Receivable totals, aging bands, debtors and follow-ups, read from the database. The post-login preload calls this same method. */
    public static JSONArray queryPayload(Connection c) throws Exception {
        String today = FinQueries.serverToday(c);
        JSONObject payload = FinQueries.receivableTotals(c, today);
        payload.put("today", today);
        payload.put("bands", FinQueries.agingBands(c, today));
        payload.put("debtors", FinQueries.receivables(c, today, 40, 0));
        payload.put("queue", FinQueries.openInvoiceQueue(c, 25));
        payload.put("topDebtors", FinQueries.topDebtors(c, 8));
        payload.put("followups", FinQueries.followUps(c, 0, 25));
        JSONArray a = new JSONArray();
        a.put(payload);
        return a;
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

        LinearLayout k = ui.row();
        k.addView(ui.kpiTile("جمع مطالبات", compact(d(p, "balanceTotal")), FinFmt.CURRENCY, i(p, "customers") + " مشتری", ui.goldAccent, null), ui.lp(0, -2, 1f));
        k.addView(ui.spacer(8), ui.lp(ui.dp(8), -2));
        k.addView(ui.kpiTile("بستانکاری", compact(d(p, "creditTotal")), FinFmt.CURRENCY, i(p, "creditCustomers") + " مشتری", FinUi.INFO, null), ui.lp(0, -2, 1f));
        body.addView(k, ui.lp(-1, -2));

        LinearLayout k2 = ui.row();
        k2.addView(ui.kpiTile("فاکتور باز", compact(d(p, "openAmount")), FinFmt.CURRENCY, i(p, "openCount") + " فاکتور", FinUi.WARNING, null), ui.lp(0, -2, 1f));
        k2.addView(ui.spacer(8), ui.lp(ui.dp(8), -2));
        k2.addView(ui.kpiTile("چک‌های در دست", compact(d(p, "checksSecuring")), FinFmt.CURRENCY, "سررسیدنشده", FinUi.SUCCESS, v -> host.open(new FinScreenCheques(host))), ui.lp(0, -2, 1f));
        LinearLayout.LayoutParams top = ui.lp(-1, -2);
        top.topMargin = ui.dp(8);
        body.addView(k2, top);

        overview(p);
        bands(p);
        debtors(p);
        queue(p);
        followups(p);
    }

    /** Aging as bars plus a debt/credit donut — the shape of the portfolio at a glance. */
    private void overview(JSONObject p) {
        LinearLayout card = section("◎", "نمای کلی مطالبات", "مانده بدهکاران در برابر بستانکاران");
        double debt = d(p, "balanceTotal"), credit = Math.abs(d(p, "creditTotal"));
        int[] palette = ui.palette();
        FinCharts.Donut donut = ui.donutChart(FinUi.FormatterKind.MONEY);
        donut.data(new String[]{"بدهکار", "بستانکار"}, new double[]{debt, credit}, new int[]{palette[5], palette[1]}, "جمع مانده");
        java.util.List<FinCharts.Legend> items = new java.util.ArrayList<>();
        items.add(new FinCharts.Legend("بدهکاران", money(debt), palette[5]));
        items.add(new FinCharts.Legend("بستانکاران", money(credit), palette[1]));
        card.addView(ui.donutWithLegend(donut, items, debt + credit, 164), ui.lp(-1, -2));
        card.addView(ui.miniStat("چک‌های در دست (سررسیدنشده)", money(d(p, "checksSecuring")) + " ریال", FinUi.SUCCESS), top(6));
        card.addView(ui.miniStat("چک‌های سررسیدشده", money(d(p, "checksDueNow")) + " ریال", FinUi.WARNING), ui.lp(-1, -2));
        card.addView(ui.miniStat("پیگیری‌های باز", fa(i(p, "followupsOpen")) + " مورد", ui.goldAccent), ui.lp(-1, -2));
        addCard(card, 12);
    }

    private void bands(JSONObject p) {
        JSONArray rows = arr(p, "bands");
        LinearLayout card = section("▤", "سنی‌بندی مطالبات", "بر پایه تاریخ فاکتور و مانده هر فاکتور");
        card.addView(ui.tableHeader(new String[]{"بازه", "تعداد", "مبلغ"}), ui.lp(-1, -2));
        String[] order = {"0-7", "8-30", "31-60", "61-90", "90+"};
        String[] labels = {"۰ تا ۷ روز", "۸ تا ۳۰ روز", "۳۱ تا ۶۰ روز", "۶۱ تا ۹۰ روز", "بیش از ۹۰ روز"};
        double[] amounts = new double[order.length];
        String[] notes = new String[order.length];
        int[] colors = new int[order.length];
        int[] palette = ui.palette();
        for (int x = 0; x < order.length; x++) {
            int n = 0; double amount = 0;
            for (int y = 0; y < rows.length(); y++) {
                JSONObject r = rows.optJSONObject(y);
                if (r != null && order[x].equals(r.optString("band"))) { n = i(r, "n"); amount = d(r, "amount"); }
            }
            amounts[x] = amount;
            notes[x] = FinFmt.count(n) + " فاکتور باز";
            colors[x] = n == 0 ? ui.textFaint : (x >= 3 ? FinUi.DANGER : (x == 2 ? FinUi.WARNING : palette[x % palette.length]));
        }
        FinCharts.Bars bars = ui.barsChart(FinUi.FormatterKind.MONEY);
        bars.data(labels, amounts, colors, notes);
        bars.empty("در حال حاضر فاکتور بازی برای سنی‌بندی وجود ندارد.");
        addBars(card, bars, order.length);
        for (int x = 0; x < order.length; x++) {
            int n = 0;
            double amount = 0;
            for (int y = 0; y < rows.length(); y++) {
                JSONObject r = rows.optJSONObject(y);
                if (r != null && order[x].equals(r.optString("band"))) {
                    n = i(r, "n");
                    amount = d(r, "amount");
                }
            }
            int accent = n == 0 ? ui.textFaint : (x >= 3 ? FinUi.DANGER : (x == 2 ? FinUi.WARNING : ui.goldAccent));
            card.addView(ui.tableRow(new String[]{labels[x], FinFmt.count(n), money(amount)},
                    new float[]{1.4f, 0.8f, 1.4f}, false, accent), ui.lp(-1, -2));
        }
        card.addView(ui.text("بازه‌ها بر اساس فاصله تاریخ فاکتور تا تاریخ سرور محاسبه می‌شود؛ ردیف صفر یعنی در این بازه فاکتور بازی وجود ندارد.",
                11f, ui.textFaint, false), ui.lp(-1, -2));
        body.addView(card, top(12));
    }

    private void debtors(JSONObject p) {
        JSONArray rows = arr(p, "debtors");
        LinearLayout card = section("☰", "بدهکاران بزرگ", "بزرگ‌ترین مانده‌ها؛ ۴۰ مشتری اول از راست به چپ مرتب شده‌اند");
        if (rows.length() == 0) {
            card.addView(stateText("مشتری بدهکاری وجود ندارد.", ui.textDim));
        } else {
            int n = Math.min(8, rows.length());
            String[] labels = new String[n];
            double[] values = new double[n];
            String[] notes = new String[n];
            int[] colors = new int[n];
            int[] palette = ui.palette();
            for (int x = 0; x < n; x++) {
                JSONObject r = rows.optJSONObject(x);
                labels[x] = s(r, "MONAME", "—");
                values[x] = d(r, "man");
                notes[x] = "کد " + fa(i(r, "SHMO")) + " · " + fa(i(r, "open_count")) + " فاکتور باز";
                colors[x] = palette[x % palette.length];
            }
            FinCharts.Bars bars = ui.barsChart(FinUi.FormatterKind.MONEY);
            bars.data(labels, values, colors, notes);
            addBars(card, bars, n);
            for (int x = 0; x < rows.length() && x < 25; x++) {
                JSONObject r = rows.optJSONObject(x);
                if (r == null) continue;
                final int shmo = i(r, "SHMO");
                double man = d(r, "man");
                String sub = "کد " + fa(shmo) + " · فاکتور باز " + fa(i(r, "open_count"))
                        + " · آخرین وصول " + s(r, "last_receipt_date", "—")
                        + (i(r, "black_list") == 1 ? " · در فهرست سیاه" : "");
                card.addView(ui.listRow(s(r, "MONAME", "—"), sub, money(man),
                        i(r, "due_check_amount") > 0 ? "چک سررسیدشده" : "بدهکار",
                        i(r, "black_list") == 1 ? FinUi.DANGER : ui.goldAccent, v -> host.open(new FinScreenCustomer(host, shmo))), ui.lp(-1, -2));
            }
        }
        body.addView(card, top(12));
    }

    private void queue(JSONObject p) {
        JSONArray rows = arr(p, "queue");
        LinearLayout card = section("◷", "صف فاکتورهای باز", "قدیمی‌ترین فاکتورها برای پیگیری");
        if (rows.length() == 0) {
            card.addView(stateText("فاکتور بازی برای پیگیری وجود ندارد.", FinUi.SUCCESS));
        } else {
            for (int x = 0; x < rows.length() && x < 15; x++) {
                JSONObject r = rows.optJSONObject(x);
                if (r == null) continue;
                final int shmo = i(r, "shmo");
                card.addView(ui.listRow(s(r, "name", "—"), "فاکتور " + fa(i(r, "shfacfo")) + " · " + s(r, "date", "")
                        + " · وصول‌شده " + money(d(r, "received")), money(d(r, "remaining")),
                        "تسویه‌نشده", FinUi.WARNING, v -> host.open(new FinScreenCustomer(host, shmo))), ui.lp(-1, -2));
            }
        }
        body.addView(card, top(12));
    }

    private void followups(JSONObject p) {
        JSONArray rows = arr(p, "followups");
        LinearLayout card = section("✎", "پیگیری و وعده پرداخت", "ثبت‌شده در جدول اختصاصی اپ با Audit");
        if (rows.length() == 0) {
            card.addView(stateText("هنوز پیگیری‌ای ثبت نشده است.", ui.textDim));
        } else {
            for (int x = 0; x < rows.length() && x < 15; x++) {
                JSONObject r = rows.optJSONObject(x);
                if (r == null) continue;
                String sub = s(r, "action", "") + " · " + s(r, "created_at", "")
                        + (s(r, "promise_date", "").isEmpty() ? "" : (" · وعده " + s(r, "promise_date", "")));
                card.addView(ui.listRow(s(r, "name", "مشتری " + fa(i(r, "shmo"))), sub,
                        d(r, "promise_amount") > 0 ? money(d(r, "promise_amount")) : "—",
                        s(r, "status", "pending"), ui.goldAccent, v -> openFollowUp(r)), ui.lp(-1, -2));
            }
        }
        LinearLayout actions = ui.row();
        actions.addView(ui.button("ثبت پیگیری جدید", ui.goldAccent, true, v -> newFollowUp()), ui.lp(0, -2, 1f));
        LinearLayout.LayoutParams ap = ui.lp(-1, -2);
        ap.topMargin = ui.dp(6);
        card.addView(actions, ap);
        body.addView(card, top(12));
    }

    private void openFollowUp(JSONObject r) {
        host.details("پیگیری مشتری " + fa(i(r, "shmo")), new String[][]{
                {"اقدام", s(r, "action", "—")},
                {"یادداشت", s(r, "note", "—")},
                {"مبلغ وعده", d(r, "promise_amount") > 0 ? money(d(r, "promise_amount")) : "—"},
                {"تاریخ وعده", s(r, "promise_date", "—")},
                {"اقدام بعدی", s(r, "next_action", "—")},
                {"تاریخ اقدام بعدی", s(r, "next_date", "—")},
                {"مسئول", s(r, "assigned_to", "—")},
                {"وضعیت", s(r, "status", "—")},
                {"ثبت‌کننده", s(r, "created_by", "—") + " · " + s(r, "created_at", "")}
        }, "منبع: dbo.meelano_fin_followup (جدول اختصاصی «آتیران مالی»). هیچ داده‌ای در جدول‌های اصلی آتیران نوشته نمی‌شود.");
    }

    /** Guarded write: permission + transaction + audit + duplicate protection. */
    private void newFollowUp() {
        if (!host.can("finance_create")) {
            host.toast("برای ثبت پیگیری مجوز finance_create لازم است.");
            return;
        }
        final EditText shmoField = ui.field("کد مشتری (SHMO)");
        shmoField.setInputType(InputType.TYPE_CLASS_NUMBER);
        final EditText note = ui.field("یادداشت پیگیری");
        final EditText promise = ui.field("مبلغ وعده پرداخت (ریال، اختیاری)");
        promise.setInputType(InputType.TYPE_CLASS_NUMBER);
        final EditText promiseDate = ui.field("تاریخ وعده به شمسی ۱۴۰۵/۰۷/۲۰ (اختیاری)");
        LinearLayout col = ui.column();
        col.setPadding(ui.dp(16), ui.dp(8), ui.dp(16), ui.dp(8));
        col.addView(shmoField, ui.lp(-1, -2));
        col.addView(note, ui.lp(-1, -2));
        col.addView(promise, ui.lp(-1, -2));
        col.addView(promiseDate, ui.lp(-1, -2));
        col.addView(ui.text("این عملیات در جدول meelano_fin_followup ثبت و در Audit درج می‌شود.",
                11f, ui.textDim, false), ui.lp(-1, -2));

        new android.app.AlertDialog.Builder(host)
                .setTitle("ثبت پیگیری مطالبات")
                .setView(col)
                .setNegativeButton("انصراف", null)
                .setPositiveButton("ثبت", (dlg, which) -> {
                    int shmo;
                    try {
                        shmo = Integer.parseInt(FinFmt.faNumber(shmoField.getText().toString()).replaceAll("\\D", ""));
                    } catch (Exception e) {
                        host.toast("کد مشتری نامعتبر است.");
                        return;
                    }
                    String n = note.getText().toString().trim();
                    if (shmo <= 0 || n.isEmpty()) {
                        host.toast("کد مشتری و یادداشت لازم است.");
                        return;
                    }
                    String pd = promiseDate.getText().toString().trim();
                    if (!pd.isEmpty() && !pd.matches("\\d{4}/\\d{2}/\\d{2}")) {
                        host.toast("تاریخ وعده باید به شکل ۱۴۰۵/۰۷/۲۰ باشد.");
                        return;
                    }
                    double amount = 0;
                    try { amount = Double.parseDouble(promise.getText().toString().trim()); } catch (Exception ignored) { }
                    final String noteText = n;
                    final String promiseOn = pd;
                    final double promiseAmount = amount;
                    JSONObject args = new JSONObject();
                    try {
                        args.put("shmo", shmo);
                        args.put("amount", promiseAmount);
                        args.put("reference", "followup:" + shmo);
                    } catch (Exception ignored) { }
                    String opKey = FinDb.opKey("receivables", "followup", shmo, noteText, promiseOn, promiseAmount,
                            System.currentTimeMillis() / 60000L);
                    db.runGuarded(opKey, "receivables", "ثبت پیگیری", args, (c, a) -> {
                        JSONObject out = new JSONObject();
                        try (PreparedStatement ps = c.prepareStatement(
                                "INSERT INTO dbo.meelano_fin_followup(shmo, action, note, promise_amount, promise_date, status, created_by) "
                                        + "VALUES(?,?,?,?,?,N'pending',?)", new String[]{"id"})) {
                            FinDb.bind(ps, new Object[]{shmo, "تماس/پیگیری", noteText,
                                    promiseAmount > 0 ? promiseAmount : null,
                                    promiseOn.isEmpty() ? null : promiseOn, FinSession.username()});
                            ps.executeUpdate();
                        }
                        out.put("shmo", shmo);
                        return out;
                    }, res -> {
                        if (res.optBoolean("ok", false)) {
                            host.toast(res.optBoolean("duplicate", false)
                                    ? "این پیگیری قبلاً ثبت شده بود (بدون تکرار)."
                                    : "پیگیری ثبت شد.");
                            load(true);
                        } else {
                            host.toast("ثبت پیگیری انجام نشد. کد رویداد: " + FinFmt.eventCode(res.optString("error", "")));
                        }
                    });
                })
                .show();
    }
}

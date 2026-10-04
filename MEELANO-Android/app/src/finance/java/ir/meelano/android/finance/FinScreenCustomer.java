package ir.meelano.android.finance;

import android.widget.LinearLayout;

import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.Connection;

/**
 * «پرونده مشتری» — account file with balance, statement, open invoices, cheques and follow-ups.
 *
 * The statement is built from {@code dbo.cust_act} (the ledger that matches {@code CUSTOMERS.man}
 * for every customer in the validation run) and the running balance is only ever displayed, never
 * stored: balances are owned by Atiran, not by this app.
 */
public class FinScreenCustomer extends FinScreen {

    private final int shmo;
    private LinearLayout body;

    public FinScreenCustomer(AtiranFinanceActivity host, int shmo) {
        super(host);
        this.shmo = shmo;
    }

    @Override public String title() { return "پرونده مشتری"; }

    @Override public String glyph() { return "☺"; }

    @Override protected String cacheKey() { return "cust:" + shmo + ":" + host.periodFrom(); }

    @Override protected void populate() {
        add(hero("پرونده مشتری", "کد " + fa(shmo) + " — مانده، صورت‌حساب، فاکتورهای باز و چک‌ها", ui.goldAccent));
        body = ui.column();
        add(body);
    }

    @Override protected void fetch() {
        db.read(key(cacheKey()), 90_000L, c -> queryPayload(c, shmo), this::render);
    }

    /** One customer file: balance, statement, open invoices and cheques — read from the database. */
    public static JSONArray queryPayload(Connection c, int shmo) throws Exception {
        String today = FinQueries.serverToday(c);
        JSONObject payload = FinQueries.customerSummary(c, shmo, today);
        payload.put("today", today);
        payload.put("statement", FinQueries.customerStatement(c, shmo, FinFmt.addDays(today, -120), today, 120));
        payload.put("invoices", FinQueries.openInvoices(c, shmo));
        payload.put("cheques", FinQueries.customerCheques(c, shmo, today));
        payload.put("activity", FinQueries.customerActivity(c, shmo, 40));
        payload.put("followups", FinQueries.followUps(c, shmo, 20));
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

        String name = s(p, "MONAME", "—");

        LinearLayout k = ui.row();
        k.addView(ui.kpiTile("مانده حساب", compact(d(p, "man")), FinFmt.CURRENCY,
                d(p, "man") < 0 ? "بستانکار" : "بدهکار", d(p, "man") < 0 ? FinUi.INFO : ui.goldAccent, null), ui.lp(0, -2, 1f));
        k.addView(ui.spacer(8), ui.lp(ui.dp(8), -2));
        k.addView(ui.kpiTile("نظارت داخلی (Sys_Mandeh)", compact(d(p, "sys_mandeh")), FinFmt.CURRENCY,
                d(p, "sys_etebar") != 0 ? ("اعتبار " + compact(d(p, "sys_etebar"))) : "بدون اعتبار ثبت‌شده", ui.silver, null), ui.lp(0, -2, 1f));
        body.addView(k, ui.lp(-1, -2));

        LinearLayout card = section("☺", name + "  (" + fa(shmo) + ")", "مشخصات و جمع‌بندی مالی");
        card.addView(kv("تلفن", s(p, "cell", "—") + "  " + s(p, "tell1", "")), ui.lp(-1, -2));
        card.addView(kv("ویزیتور", s(p, "visitor_name", "ثبت‌نشده")), ui.lp(-1, -2));
        card.addView(kv("فروش کل", money(d(p, "sales_total")) + "  (" + fa(i(p, "invoice_count")) + " فاکتور)"), ui.lp(-1, -2));
        card.addView(kv("وصول کل", money(d(p, "received_total")) + "  (" + fa(i(p, "receipt_count")) + " قبض)"), ui.lp(-1, -2));
        card.addView(kv("از این مبلغ نقد", money(d(p, "received_cash"))), ui.lp(-1, -2));
        card.addView(kv("از این مبلغ چک", money(d(p, "received_check"))), ui.lp(-1, -2));
        card.addView(kvMoney("بدهی گردش حساب (بدهکار)", d(p, "debit_total")));
        card.addView(kvMoney("بستانکار گردش حساب", d(p, "credit_total")));
        card.addView(kv("ردیف‌های گردش حساب", fa(i(p, "ledger_rows"))), ui.lp(-1, -2));
        card.addView(kv("فاکتورهای باز", fa(i(p, "open_invoice_count")) + " · " + money(d(p, "open_invoice_amount"))), ui.lp(-1, -2));
        card.addView(kv("چک‌های مشتری", fa(i(p, "check_count")) + " · در دست " + money(d(p, "check_open_amount"))
                + (i(p, "check_overdue_count") > 0 ? (" · معوق " + fa(i(p, "check_overdue_count"))) : "")), ui.lp(-1, -2));
        card.addView(kv("آخرین وصول", s(p, "last_receipt_date", "—")), ui.lp(-1, -2));
        if (i(p, "black_list") == 1) card.addView(ui.chip("در فهرست سیاه مشتریان", FinUi.DANGER), ui.lp(-2, -2));
        if (i(p, "hesab_status") != 0) card.addView(ui.chip("وضعیت حساب: کد " + fa(i(p, "hesab_status")), FinUi.WARNING), ui.lp(-2, -2));
        body.addView(card, top(12));

        movementChart(arr(p, "statement"));

        body.addView(block("◷", "فاکتورهای باز", "sailfact با bamandeh <> 0", arr(p, "invoices"), 20, (r) -> new String[]{
                "فاکتور " + fa(i(r, "shfacfo")),
                "تاریخ " + s(r, "date", "—") + " · مانده " + money(d(r, "remaining")),
                money(d(r, "total")),
                "تسویه‌نشده"
        }, FinUi.WARNING, "invoice"), top(12));

        body.addView(block("◫", "چک‌های مشتری", "getchk", arr(p, "cheques"), 25, (r) -> new String[]{
                s(r, "shgetchk", "—") + " · " + s(r, "getchbank", "—"),
                "دریافت " + s(r, "getdate", "—") + " · سررسید " + s(r, "sardate", "—"),
                money(d(r, "getchkmab")),
                i(r, "is_overdue") == 1 ? "سررسید گذشته" : s(r, "status_name", "")
        }, ui.goldAccent, "cheque"), top(12));

        body.addView(block("☰", "گردش حساب (صورت‌حساب)", "cust_act — ۱۲۰ روز گذشته", arr(p, "statement"), 40, (r) -> new String[]{
                s(r, "date", "—") + " · " + FinQueries.ledgerKindLabel(i(r, "act_id")),
                s(r, "act_dis", "—"),
                money(d(r, "act_bed") - d(r, "act_bes")),
                s(r, "isActive", "1").equalsIgnoreCase("f") ? "ابطال" : "معتبر"
        }, ui.silver, "ledger"), top(12));

        body.addView(block("✎", "پیگیری‌ها", "meelano_fin_followup", arr(p, "followups"), 15, (r) -> new String[]{
                s(r, "action", "—"),
                s(r, "note", "—") + (s(r, "promise_date", "").isEmpty() ? "" : (" · وعده " + s(r, "promise_date", ""))),
                d(r, "promise_amount") > 0 ? money(d(r, "promise_amount")) : "—",
                s(r, "status", "")
        }, ui.goldAccent, "followup"), top(12));

        body.addView(block("↻", "آخرین رویدادها", "فاکتور و قبض دریافت", arr(p, "activity"), 20, (r) -> new String[]{
                s(r, "kind", "—") + " " + s(r, "ref", ""),
                s(r, "jalali_date", "—") + " · " + s(r, "detail", ""),
                money(d(r, "amount")),
                s(r, "status", "")
        }, ui.textDim, "activity"), top(12));
    }

    /** Daily debit/credit of this customer's ledger (cust_act) for the last 120 days. */
    private void movementChart(JSONArray statement) {
        if (statement.length() < 2) return;
        java.util.LinkedHashMap<String, double[]> byDate = new java.util.LinkedHashMap<>();
        for (int x = 0; x < statement.length(); x++) {
            JSONObject r = statement.optJSONObject(x);
            if (r == null) continue;
            String date = s(r, "date", "");
            if (date.isEmpty()) continue;
            double[] cell = byDate.get(date);
            if (cell == null) { cell = new double[2]; byDate.put(date, cell); }
            cell[0] += d(r, "act_bed");
            cell[1] += d(r, "act_bes");
        }
        if (byDate.size() < 2) return;
        java.util.List<String> dates = new java.util.ArrayList<>(byDate.keySet());
        java.util.Collections.sort(dates);
        int n = Math.min(30, dates.size());
        String[] labels = new String[n];
        double[] debit = new double[n], credit = new double[n];
        for (int i = 0; i < n; i++) {
            String date = dates.get(n - 1 - i);           // oldest first; the chart draws right → left
            double[] cell = byDate.get(date);
            labels[i] = FinFmt.faNumber(date.length() >= 5 ? date.substring(date.length() - 5) : date);
            debit[i] = cell[0];
            credit[i] = cell[1];
        }
        LinearLayout card = section("▤", "گردش روزانه حساب", "بدهکار و بستانکار هر روز از دفتر cust_act");
        FinCharts.Columns cols = ui.columnsChart(FinUi.FormatterKind.MONEY);
        cols.data(labels, new double[][]{debit, credit}, new int[]{FinUi.WARNING, FinUi.SUCCESS}, new String[]{"بدهکار", "بستانکار"});
        addChart(card, cols, 190);
        body.addView(card, top(12));
    }

    private interface Row {
        String[] cells(JSONObject r);
    }

    private LinearLayout block(String glyph, String title, String source, JSONArray rows, int max, Row mapper,
                               int accent, String kind) {
        LinearLayout card = section(glyph, title, source);
        if (rows.length() == 0) {
            card.addView(stateText("موردی ثبت نشده است.", ui.textDim));
            return card;
        }
        for (int x = 0; x < rows.length() && x < max; x++) {
            JSONObject r = rows.optJSONObject(x);
            if (r == null) continue;
            String[] cells = mapper.cells(r);
            card.addView(ui.listRow(cells[0], cells[1], cells[2], cells[3], accent, v -> detail(kind, r)), ui.lp(-1, -2));
        }
        return card;
    }

    private void detail(String kind, JSONObject r) {
        if ("invoice".equals(kind)) {
            host.details("فاکتور " + fa(i(r, "shfacfo")), new String[][]{
                    {"تاریخ", s(r, "date", "—")},
                    {"مبلغ کل", money(d(r, "total"))},
                    {"وصول‌شده روی فاکتور", money(d(r, "received"))},
                    {"مانده", money(d(r, "remaining"))},
                    {"تسویه", s(r, "tasvieh", "—")},
                    {"وضعیت تأیید", fa(i(r, "Status")) + " · " + s(r, "TaeedDate", "—")},
                    {"راننده", s(r, "driver_name", "—")}
            }, "منبع: dbo.sailfact (MabDaryaftFactor مبلغ وصول‌شده روی فاکتور است).");
            return;
        }
        if ("cheque".equals(kind)) {
            host.details("چک " + s(r, "shgetchk", "—"), new String[][]{
                    {"دریافت", s(r, "getdate", "—")},
                    {"سررسید", s(r, "sardate", "—")},
                    {"بانک", s(r, "getchbank", "—")},
                    {"مبلغ", money(d(r, "getchkmab"))},
                    {"وضعیت", s(r, "status_name", "—")},
                    {"برگشتی", s(r, "back", "f")}
            }, "منبع: dbo.getchk و getcheckhistorystatus.");
            return;
        }
        if ("ledger".equals(kind)) {
            host.details("گردش حساب", new String[][]{
                    {"تاریخ", s(r, "date", "—")},
                    {"نوع سند", FinQueries.ledgerKindLabel(i(r, "act_id"))},
                    {"شرح", s(r, "act_dis", "—")},
                    {"بدهکار", money(d(r, "act_bed"))},
                    {"بستانکار", money(d(r, "act_bes"))},
                    {"قبض/سند", fa(i(r, "ghno"))},
                    {"شماره سند", s(r, "DocNumber", "—") + " / " + s(r, "AccDocNumber", "—")},
                    {"وضعیت", s(r, "isActive", "1").equalsIgnoreCase("f") ? "ابطال‌شده" : "معتبر"},
                    {"نمایش در گزارش", s(r, "ShowInReport", "—")}
            }, "منبع: dbo.cust_act — همان دفتری که جمع آن با CUSTOMERS.man تطبیق داده شده است.");
            return;
        }
        if ("followup".equals(kind)) {
            host.details("پیگیری", new String[][]{
                    {"اقدام", s(r, "action", "—")},
                    {"یادداشت", s(r, "note", "—")},
                    {"مبلغ وعده", money(d(r, "promise_amount"))},
                    {"تاریخ وعده", s(r, "promise_date", "—")},
                    {"اقدام بعدی", s(r, "next_action", "—")},
                    {"تاریخ بعدی", s(r, "next_date", "—")},
                    {"مسئول", s(r, "assigned_to", "—")},
                    {"ثبت", s(r, "created_by", "—") + " · " + s(r, "created_at", "")}
            }, "منبع: dbo.meelano_fin_followup.");
            return;
        }
        host.details("رویداد", new String[][]{
                {"نوع", s(r, "kind", "—")},
                {"مرجع", s(r, "ref", "—")},
                {"تاریخ", s(r, "jalali_date", "—")},
                {"مبلغ", money(d(r, "amount"))},
                {"شرح", s(r, "detail", "—")},
                {"وضعیت", s(r, "status", "—")}
        }, null);
    }
}

package ir.meelano.android.finance;

import android.view.View;
import android.widget.LinearLayout;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * «مرکز چک» — received and issued cheques with the real Atiran status names.
 *
 * The status wording is read from {@code dbo.getcheckhistorystatus} (getchk) and from
 * {@code putchk_status} for issued cheques; no status text is hard-coded in the app. The due
 * windows (today / overdue / next 7 days) are computed with the server's Jalali date and the
 * cheque due date, which Atiran stores as comparable {@code char(10)} text.
 */
public class FinScreenCheques extends FinScreen {

    public FinScreenCheques(AtiranFinanceActivity host) { super(host); }

    @Override public String title() { return "مرکز چک"; }

    @Override public String glyph() { return "◫"; }

    @Override protected String cacheKey() { return "checks:" + mode + ":" + host.periodFrom(); }

    private String mode = FinQueries.MODE_ALL;
    private boolean issuedView;
    private LinearLayout body;

    @Override protected void populate() {
        LinearLayout head = ui.row();
        head.addView(ui.text("مرکز چک", 17f, ui.textColor, true), ui.lp(0, -2, 1f));
        head.addView(ui.chip("دریافتی", ui.goldAccent), ui.lp(-2, -2));
        add(head);

        LinearLayout tabs = ui.row();
        tabs.setPadding(0, ui.dp(6), 0, 0);
        LinearLayout rec = ui.chip("چک‌های دریافتی", !issuedView ? ui.goldAccent : ui.textFaint);
        rec.setOnClickListener(v -> { if (issuedView) { issuedView = false; mode = FinQueries.MODE_ALL; rebuild(); } });
        LinearLayout iss = ui.chip("چک‌های پرداختی", issuedView ? ui.goldAccent : ui.textFaint);
        iss.setOnClickListener(v -> { if (!issuedView) { issuedView = true; rebuild(); } });
        tabs.addView(rec, ui.lp(-2, -2));
        LinearLayout.LayoutParams ip = ui.lp(-2, -2);
        ip.leftMargin = ui.dp(6);
        tabs.addView(iss, ip);
        addCard(tabs, 6);

        if (!issuedView) {
            LinearLayout modes = ui.row();
            String[][] spec = {
                    {FinQueries.MODE_ALL, "همه"},
                    {FinQueries.MODE_DUE_TODAY, "سررسید امروز"},
                    {FinQueries.MODE_OVERDUE, "معوق"},
                    {FinQueries.MODE_NEXT7, "۷ روز آینده"},
                    {FinQueries.MODE_TREASURY, "در خزانه"},
                    {FinQueries.MODE_RETURNED, "برگشتی"}
            };
            for (String[] m : spec) {
                boolean active = m[0].equals(mode);
                LinearLayout chip = ui.chip(m[1], active ? ui.goldAccent : ui.textFaint);
                chip.setOnClickListener(v -> { mode = m[0]; rebuild(); });
                LinearLayout.LayoutParams p = ui.lp(-2, -2);
                p.leftMargin = ui.dp(4);
                modes.addView(chip, p);
            }
            addCard(modes, 6);
        }

        body = ui.column();
        add(body);
    }

    private void rebuild() {
        box.removeAllViews();
        ui.pad(box);
        populate();
        load(false);
    }

    @Override protected void fetch() {
        final String m = mode;
        final boolean issued = issuedView;
        db.read(key(cacheKey() + (issued ? ":out" : ":in")), 120_000L, c -> {
            String today = FinQueries.serverToday(c);
            JSONObject payload = new JSONObject();
            payload.put("today", today);
            payload.put("issuedView", issued);
            if (issued) {
                payload.put("census", FinQueries.paidChequeCensus(c));
                payload.put("rows", FinQueries.paidCheques(c, host.periodFrom(), host.periodTo(), "", 60, 0));
                payload.put("statusNames", new JSONArray());
            } else {
                payload.put("census", FinQueries.chequeStatusCensus(c, true, today));
                payload.put("rows", FinQueries.cheques(c, host.periodFrom(), host.periodTo(), m, "", 60, 0));
                payload.put("statusNames", FinQueries.checkStatusNames(c));
                payload.put("calendar", FinQueries.calendar(c, today, FinFmt.addDays(today, 30), today, 40));
            }
            JSONArray a = new JSONArray();
            a.put(payload);
            return a;
        }, env -> render(env, issued));
    }

    private void render(JSONObject env, boolean issued) {
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

        LinearLayout census = section("▦", issued ? "وضعیت چک‌های پرداختی" : "وضعیت چک‌های دریافتی",
                "نام‌ها از جدول‌های وضعیت خود آتیران خوانده می‌شود");
        census.addView(ui.tableHeader(new String[]{"وضعیت", "تعداد", "مبلغ", "معوق"}), ui.lp(-1, -2));
        JSONArray rows = arr(p, "census");
        if (rows.length() == 0) {
            census.addView(stateText("چکی با وضعیت ثبت‌شده وجود ندارد.", ui.textDim));
        } else {
            for (int x = 0; x < rows.length(); x++) {
                JSONObject r = rows.optJSONObject(x);
                if (r == null) continue;
                String label = issued ? ("وضعیت " + fa(i(r, "status_id"))) : s(r, "status_name", "بدون نام");
                census.addView(ui.tableRow(new String[]{label, fa(i(r, "n")), compact(d(r, "total")), fa(i(r, "overdue"))},
                        new float[]{1.8f, 0.6f, 1f, 0.6f}, false, ui.goldAccent), ui.lp(-1, -2));
            }
        }
        body.addView(census, top(12));

        LinearLayout list = section("◫", issued ? "چک‌های پرداختی دوره" : "چک‌های دریافتی", 
                issued ? "putdate در بازه انتخاب‌شده" : ("فیلتر: " + modeLabel()));
        JSONArray items = arr(p, "rows");
        if (items.length() == 0) {
            list.addView(stateText("چکی با این فیلتر در بازه انتخابی وجود ندارد.", ui.textDim));
        } else {
            for (int x = 0; x < items.length(); x++) {
                JSONObject r = items.optJSONObject(x);
                if (r == null) continue;
                if (issued) {
                    String sub = "سررسید " + s(r, "sardate", "—") + " · " + s(r, "bank_name", "—") + " · گیرنده " + s(r, "girande", "—");
                    list.addView(ui.listRow(money(d(r, "putchkmab")) + " " + FinFmt.CURRENCY, sub,
                            s(r, "shputchk", "—"), i(r, "is_overdue") == 1 ? "سررسید گذشته" : "در جریان",
                            i(r, "is_overdue") == 1 ? FinUi.DANGER : ui.goldAccent, v -> chequeDetails(r, true)), ui.lp(-1, -2));
                } else {
                    String sub = "سررسید " + s(r, "sardate", "—") + " · " + s(r, "getchbank", "—")
                            + " · " + s(r, "customer_name", "—");
                    int accent = i(r, "is_overdue") == 1 ? FinUi.DANGER
                            : ("t".equalsIgnoreCase(s(r, "back", "f")) ? FinUi.WARNING : ui.goldAccent);
                    list.addView(ui.listRow(money(d(r, "getchkmab")) + " " + FinFmt.CURRENCY, sub,
                            s(r, "shgetchk", "—"), i(r, "is_overdue") == 1 ? "سررسید گذشته" : s(r, "status_name", ""),
                            accent, v -> chequeDetails(r, false)), ui.lp(-1, -2));
                }
            }
        }
        body.addView(list, top(12));

        if (!issued) calendar(p);
        statusLegend(p);
    }

    private void calendar(JSONObject p) {
        JSONArray rows = arr(p, "calendar");
        LinearLayout card = section("▤", "تقویم ۳۰ روز آینده", "چک، فاکتور، وعده پرداخت، تسویه و مغایرت");
        if (rows.length() == 0) {
            card.addView(stateText("موردی با تاریخ سررسید در ۳۰ روز آینده ثبت نشده است.", ui.textDim));
        } else {
            for (int x = 0; x < rows.length() && x < 25; x++) {
                JSONObject r = rows.optJSONObject(x);
                if (r == null) continue;
                card.addView(ui.listRow(s(r, "detail", "—"), s(r, "kind", "") + " · " + s(r, "jalali_date", ""),
                        money(d(r, "amount")), s(r, "status", ""), ui.goldAccent, v -> host.details("تقویم مالی", new String[][]{
                        {"نوع", s(r, "kind", "—")},
                        {"تاریخ", s(r, "jalali_date", "—")},
                        {"مرجع", s(r, "ref", "—")},
                        {"مبلغ", money(d(r, "amount")) + " " + FinFmt.CURRENCY},
                        {"شرح", s(r, "detail", "—")},
                        {"وضعیت", s(r, "status", "—")}
                }, "منبع: getchk / putchk / sailfact / meelano_fin_followup / meelano_fin_settlement / meelano_fin_recon")), ui.lp(-1, -2));
            }
        }
        body.addView(card, top(12));
    }

    private void statusLegend(JSONObject p) {
        JSONArray names = arr(p, "statusNames");
        if (names.length() == 0) return;
        LinearLayout card = section("ℹ", "راهنمای وضعیت‌ها", "متن دقیق جدول getcheckhistorystatus");
        for (int x = 0; x < names.length(); x++) {
            JSONObject r = names.optJSONObject(x);
            if (r == null) continue;
            card.addView(table2(new String[]{fa(i(r, "GetStatusID")), s(r, "StatusName", "—")}), ui.lp(-1, -2));
        }
        body.addView(card, top(12));
    }

    private View table2(String[] cells) {
        return ui.tableRow(cells, new float[]{0.4f, 2.6f}, false, ui.goldAccent);
    }

    private String modeLabel() {
        if (FinQueries.MODE_DUE_TODAY.equals(mode)) return "سررسید امروز";
        if (FinQueries.MODE_OVERDUE.equals(mode)) return "سررسید گذشته";
        if (FinQueries.MODE_NEXT7.equals(mode)) return "۷ روز آینده";
        if (FinQueries.MODE_TREASURY.equals(mode)) return "در خزانه (وضعیت ۸/۹/۱۰)";
        if (FinQueries.MODE_RETURNED.equals(mode)) return "برگشتی";
        if (FinQueries.MODE_DEPOSITED.equals(mode)) return "سپرده‌شده در بانک";
        return "ورود در بازه " + host.periodFrom() + " تا " + host.periodTo();
    }

    private void chequeDetails(JSONObject r, boolean issued) {
        if (issued) {
            host.details("چک پرداختی " + s(r, "shputchk", "—"), new String[][]{
                    {"تاریخ ثبت", s(r, "putdate", "—")},
                    {"سررسید", s(r, "sardate", "—")},
                    {"بانک", s(r, "bank_name", "—")},
                    {"گیرنده", s(r, "girande", "—")},
                    {"مبلغ", money(d(r, "putchkmab")) + " " + FinFmt.CURRENCY},
                    {"نوع چک", s(r, "check_type", "—")},
                    {"شناسه صیاد", s(r, "ShenaseSayad", "—")},
                    {"وضعیت", "کد " + fa(i(r, "putchk_status")) + " (" + s(r, "putchkdis", "بدون شرح") + ")"},
                    {"قبض مرتبط", fa(i(r, "ghno"))}
            }, "منبع: dbo.putchk با پیوند به BANK از طریق bankrdf.");
            return;
        }
        host.details("چک دریافتی " + s(r, "shgetchk", "—"), new String[][]{
                {"تاریخ دریافت", s(r, "getdate", "—")},
                {"سررسید", s(r, "sardate", "—")},
                {"بانک/شعبه", s(r, "getchbank", "—") + " / " + s(r, "getchkshobe", "—")},
                {"حساب (شبا/شماره)", s(r, "getchkshhes", "—")},
                {"مشتری", s(r, "customer_name", "—") + " (" + fa(i(r, "shmo")) + ")"},
                {"مبلغ", money(d(r, "getchkmab")) + " " + FinFmt.CURRENCY},
                {"وضعیت", s(r, "status_name", "—") + " (کد " + fa(i(r, "chk_satus")) + ")"},
                {"برگشتی", s(r, "back", "f")},
                {"نوع چک", s(r, "check_type", "—")},
                {"شناسه صیاد", s(r, "ShenaseSayad", "—")},
                {"قبض/سند", "قبض " + fa(i(r, "ghno"))},
                {"اپراتور", s(r, "visitor_name", "روی چک ثبت نشده")},
                {"بانک خودی (در خزانه)", i(r, "our_bankrdf") == 0 ? "—" : fa(i(r, "our_bankrdf"))}
        }, "منبع: dbo.getchk با پیوند CUSTOMERS، getcheckhistorystatus و CheckTypes. اگر اپراتور روی چک ثبت نشده باشد، انتساب انجام نمی‌شود.");
    }
}

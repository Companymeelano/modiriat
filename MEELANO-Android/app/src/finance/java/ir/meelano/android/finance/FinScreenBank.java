package ir.meelano.android.finance;

import android.widget.LinearLayout;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * «جزئیات حساب بانکی» — movements of one account with the real ban_act kinds, plus the movement
 * outside the app that a reconciliation case can be opened for.
 *
 * Bank reconciliation itself (Statement → Match → Resolve → Confirm) stores its cases in
 * {@code meelano_fin_recon} through the guarded write path; this screen shows the source rows the
 * matching works on.
 */
public class FinScreenBank extends FinScreen {

    private final int bankRdf;
    private final String bankName;
    private LinearLayout body;

    public FinScreenBank(AtiranFinanceActivity host, int bankRdf, String bankName) {
        super(host);
        this.bankRdf = bankRdf;
        this.bankName = bankName == null ? "" : bankName;
    }

    @Override public String title() { return "جزئیات حساب"; }

    @Override public String glyph() { return "▦"; }

    @Override protected String cacheKey() { return "bank:" + bankRdf + ":" + host.periodFrom(); }

    @Override protected void populate() {
        LinearLayout head = ui.row();
        head.addView(ui.text("جزئیات حساب", 17f, ui.textColor, true), ui.lp(0, -2, 1f));
        head.addView(ui.chip(bankName.isEmpty() ? ("حساب " + fa(bankRdf)) : bankName, ui.goldAccent), ui.lp(-2, -2));
        add(head);
        body = ui.column();
        add(body);
    }

    @Override protected void fetch() {
        db.read(key(cacheKey()), 120_000L, c -> {
            String today = FinQueries.serverToday(c);
            JSONObject payload = new JSONObject();
            payload.put("today", today);
            payload.put("movements", FinQueries.bankMovements(c, bankRdf, host.periodFrom(), host.periodTo(), 120));
            payload.put("unlinked", FinQueries.bankWithoutReceipt(c, host.periodFrom(), host.periodTo(), 40));
            payload.put("daily", FinQueries.bankDaily(c, host.periodFrom(), host.periodTo()));
            payload.put("cases", FinQueries.reconCases(c, 40));
            JSONArray a = new JSONArray();
            a.put(payload);
            return a;
        }, this::render);
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

        JSONArray movements = arr(p, "movements");
        double in = 0, out = 0;
        for (int x = 0; x < movements.length(); x++) {
            JSONObject m = movements.optJSONObject(x);
            if (m == null) continue;
            in += d(m, "act_bed");
            out += d(m, "act_bes");
        }

        LinearLayout k = ui.row();
        k.addView(ui.kpiTile("واریز بازه", compact(in), FinFmt.CURRENCY, "ban_act.act_bed (۱۲۰ ردیف آخر)", FinUi.SUCCESS, null), ui.lp(0, -2, 1f));
        k.addView(ui.spacer(8), ui.lp(ui.dp(8), -2));
        k.addView(ui.kpiTile("برداشت بازه", compact(out), FinFmt.CURRENCY, "ban_act.act_bes (۱۲۰ ردیف آخر)", FinUi.DANGER, null), ui.lp(0, -2, 1f));
        body.addView(k, ui.lp(-1, -2));

        LinearLayout list = section("☰", "گردش حساب", host.periodFrom() + " تا " + host.periodTo());
        if (movements.length() == 0) {
            list.addView(stateText("گردشی در این بازه ثبت نشده است.", ui.textDim));
        } else {
            list.addView(ui.text("برای دیدن شرح کامل و شماره سند هر ردیف، روی آن ضربه بزنید.", 11f, ui.textFaint, false), ui.lp(-1, -2));
            for (int x = 0; x < movements.length() && x < 60; x++) {
                JSONObject m = movements.optJSONObject(x);
                if (m == null) continue;
                list.addView(ui.listRow(FinQueries.bankKindLabel(i(m, "act_id")) + " · " + s(m, "act_date", ""),
                        s(m, "act_dis", "بدون شرح") + " · سند " + s(m, "DocNumber", "—") + " · قبض " + fa(i(m, "Ghno")),
                        money(d(m, "act_bed") - d(m, "act_bes")), d(m, "act_bes") > 0 ? "برداشت" : "واریز",
                        ui.goldAccent, v -> host.details("گردش بانکی", new String[][]{
                                {"تاریخ", s(m, "act_date", "—")},
                                {"نوع", FinQueries.bankKindLabel(i(m, "act_id"))},
                                {"شرح", s(m, "act_dis", "—")},
                                {"واریز (act_bed)", money(d(m, "act_bed"))},
                                {"برداشت (act_bes)", money(d(m, "act_bes"))},
                                {"قبض (Ghno)", fa(i(m, "Ghno"))},
                                {"سند", s(m, "DocNumber", "—") + " / " + s(m, "AccDocNumber", "—")},
                                {"تاریخ انجام", s(m, "done_act", "—")},
                                {"وضعیت", "1".equalsIgnoreCase(s(m, "isActive", "1")) ? "فعال" : "غیرفعال"}
                        }, "منبع: dbo.ban_act — ردیف خام همان چیزی است که مغایرت‌گیری روی آن انجام می‌شود.")), ui.lp(-1, -2));
            }
        }
        body.addView(list, top(12));

        JSONArray unlinked = arr(p, "unlinked");
        LinearLayout ul = section("≠", "گردش بدون قبض مرتبط", unlinked.length() + " ردیف (منبع مغایرت)");
        if (unlinked.length() == 0) {
            ul.addView(stateText("همه گردش‌های این بازه به یک قبض متصل هستند.", FinUi.SUCCESS));
        } else {
            for (int x = 0; x < unlinked.length() && x < 20; x++) {
                JSONObject m = unlinked.optJSONObject(x);
                if (m == null) continue;
                ul.addView(ui.listRow(FinQueries.bankKindLabel(i(m, "act_id")) + " · " + s(m, "act_date", ""),
                        "کد گردش " + fa(i(m, "rdf")) + " · " + s(m, "act_dis", "بدون شرح"),
                        money(d(m, "act_bed") - d(m, "act_bes")), "بدون قبض", FinUi.WARNING,
                        v -> host.open(new FinScreenRecon(host))), ui.lp(-1, -2));
            }
        }
        body.addView(ul, top(12));

        JSONArray cases = arr(p, "cases");
        LinearLayout cs = section("◫", "پرونده‌های مغایرت ثبت‌شده", "meelano_fin_recon");
        if (cases.length() == 0) {
            cs.addView(stateText("پرونده مغایرتی برای این حساب ثبت نشده است.", ui.textDim));
        } else {
            for (int x = 0; x < cases.length() && x < 12; x++) {
                JSONObject r = cases.optJSONObject(x);
                if (r == null) continue;
                cs.addView(ui.listRow(s(r, "kind", "—") + " · " + s(r, "case_key", ""),
                        s(r, "jalali_date", "—") + " · " + s(r, "reason", "بدون توضیح"),
                        money(d(r, "amount")), s(r, "status", "open"), FinUi.statusColor(s(r, "status", "open")),
                        v -> host.open(new FinScreenRecon(host))), ui.lp(-1, -2));
            }
        }
        body.addView(cs, top(12));
    }
}

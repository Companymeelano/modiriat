package ir.meelano.android.finance;

import android.view.View;
import android.widget.LinearLayout;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * «بانک‌ها» — every real bank account with its balance, movement totals, POS volume and cheque counts.
 *
 * The ShabaNumber column stores only the literal text "IR" in the live database, and HaveEChecks is
 * false for all accounts, so this screen deliberately does not print an IBAN and does not offer
 * electronic cheques: it shows the account text (SHHE) and the branch, and says that the IBAN field is
 * unusable until it is filled in Atiran.
 */
public class FinScreenBanks extends FinScreen {

    public FinScreenBanks(AtiranFinanceActivity host) { super(host); }

    @Override public String title() { return "بانک‌ها"; }

    @Override public String glyph() { return "▤"; }

    @Override protected String cacheKey() { return "banks"; }

    private LinearLayout body;

    @Override protected void populate() {
        LinearLayout head = ui.row();
        head.addView(ui.text("بانک‌ها و حساب‌ها", 17f, ui.textColor, true), ui.lp(0, -2, 1f));
        head.addView(ui.chip("BANK.MAN", ui.goldAccent), ui.lp(-2, -2));
        add(head);
        body = ui.column();
        add(body);
    }

    @Override protected void fetch() {
        db.read(key(cacheKey()), 300_000L, c -> {
            String today = FinQueries.serverToday(c);
            JSONObject payload = FinQueries.home(c, today);
            payload.put("today", today);
            payload.put("banks", FinQueries.banks(c));
            payload.put("daily", FinQueries.bankDaily(c, FinFmt.addDays(today, -30), today));
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

        LinearLayout k = ui.row();
        k.addView(ui.kpiTile("موجودی بانک‌ها", compact(d(p, "bankTotal")), FinFmt.CURRENCY, i(p, "bankCount") + " حساب فعال", ui.goldAccent, null), ui.lp(0, -2, 1f));
        k.addView(ui.spacer(8), ui.lp(ui.dp(8), -2));
        k.addView(ui.kpiTile("گردش امروز", fa(i(p, "bankMovementToday")) + " ردیف", "", "ban_act امروز", FinUi.INFO, null), ui.lp(0, -2, 1f));
        body.addView(k, ui.lp(-1, -2));

        JSONArray banks = arr(p, "banks");
        LinearLayout card = section("▤", "حساب‌های بانکی", banks.length() + " حساب ثبت‌شده");
        if (banks.length() == 0) {
            card.addView(stateText("حساب بانکی ثبت نشده است.", ui.textDim));
        } else {
            for (int x = 0; x < banks.length(); x++) {
                JSONObject b = banks.optJSONObject(x);
                if (b == null) continue;
                String sub = s(b, "OWNER", "—") + " · شعبه " + s(b, "SHOBE", "—")
                        + " · گردش " + fa(i(b, "movement_count")) + " ردیف"
                        + (s(b, "last_date", "").isEmpty() ? "" : (" · آخرین " + s(b, "last_date", "")));
                boolean active = !"f".equalsIgnoreCase(s(b, "Active", "1"));
                card.addView(ui.listRow(s(b, "BANKNAME", "—"), sub, money(d(b, "MAN")),
                        active ? "فعال" : "غیرفعال", active ? ui.goldAccent : FinUi.WARNING,
                        v -> host.open(new FinScreenBank(host, i(b, "RDF"), s(b, "BANKNAME", "")))), ui.lp(-1, -2));
            }
        }
        body.addView(card, top(12));

        LinearLayout notes = section("ℹ", "نکات داده‌ای", "از بررسی مستقیم جدول BANK");
        notes.addView(stateText("• ستون ShabaNumber در حال حاضر فقط متن «IR» را نگه می‌دارد؛ بنابراین شبا نمایش داده نمی‌شود.", ui.textDim));
        notes.addView(stateText("• HaveEChecks برای همه حساب‌ها false است؛ بخش چک الکترونیک ارائه نمی‌شود.", ui.textDim));
        notes.addView(stateText("• موجودی همان BANK.MAN است و از اپ قابل تغییر نیست (بدون دست‌کاری مستقیم مانده).", ui.textDim));
        body.addView(notes, top(12));

        JSONArray daily = arr(p, "daily");
        LinearLayout day = section("▦", "گردش روزانه ۳۰ روز", "جمع واریز و برداشت از ban_act");
        day.addView(ui.tableHeader(new String[]{"تاریخ", "واریز", "برداشت", "ردیف"}), ui.lp(-1, -2));
        if (daily.length() == 0) {
            day.addView(stateText("گردشی در ۳۰ روز گذشته ثبت نشده است.", ui.textDim));
        } else {
            for (int x = 0; x < daily.length(); x++) {
                JSONObject r = daily.optJSONObject(x);
                if (r == null) continue;
                day.addView(ui.tableRow(new String[]{s(r, "act_date", "—"), compact(d(r, "in_amount")),
                        compact(d(r, "out_amount")), fa(i(r, "n"))}, new float[]{1f, 1f, 1f, 0.5f}, false,
                        ui.goldAccent), ui.lp(-1, -2));
            }
        }
        body.addView(day, top(12));
    }
}

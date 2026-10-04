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
        add(hero("بانک‌ها و حساب‌ها", "موجودی واقعی BANK.MAN و گردش ban_act — بدون دست‌کاری مانده", ui.goldAccent));
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
            payload.put("posBanks", FinDb.select(c, "SELECT pd.PosBankRdf AS bank, ISNULL(b.BANKNAME, N'(بینام)') AS bank_name, "
                    + "COUNT(*) AS n, ISNULL(SUM(pd.MabPos),0) AS total FROM dbo.PosDetails pd WITH (NOLOCK) "
                    + "INNER JOIN dbo.dar d WITH (NOLOCK) ON d.ghno=pd.ghno AND d.p=0 "
                    + "LEFT JOIN dbo.BANK b WITH (NOLOCK) ON b.RDF=pd.PosBankRdf "
                    + "WHERE d.[date]>=? AND d.[date]<=? GROUP BY pd.PosBankRdf, b.BANKNAME ORDER BY total DESC",
                    FinFmt.addDays(today, -30), today));
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
        balances(banks);
        dailyChart(p);
        posChart(p);

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

    /** Balance of every active account as a ranked bar chart. */
    private void balances(JSONArray banks) {
        if (banks.length() == 0) return;
        LinearLayout card = section("◎", "موجودی حساب‌ها", "BANK.MAN — بزرگ‌ترین موجودی بالا");
        int n = Math.min(9, banks.length());
        String[] labels = new String[n];
        double[] values = new double[n];
        String[] notes = new String[n];
        int[] colors = new int[n];
        int[] palette = ui.palette();
        for (int x = 0; x < n; x++) {
            JSONObject b = banks.optJSONObject(x);
            if (b == null) b = new JSONObject();
            labels[x] = s(b, "BANKNAME", "—");
            values[x] = d(b, "MAN");
            notes[x] = "شعبه " + s(b, "SHOBE", "—") + " · گردش " + fa(i(b, "movement_count")) + " ردیف";
            colors[x] = palette[x % palette.length];
        }
        FinCharts.Bars bars = ui.barsChart(FinUi.FormatterKind.MONEY);
        bars.data(labels, values, colors, notes);
        addBars(card, bars, n);

        double posSum = 0;
        for (int x = 0; x < banks.length(); x++) posSum += banks.optJSONObject(x) == null ? 0 : d(banks.optJSONObject(x), "pos_total");
        if (posSum > 0) card.addView(ui.miniStat("جمع تراکنش‌های POS روی این حساب‌ها", money(posSum) + " ریال", FinUi.MANAGER), top(4));
        addCard(card, 12);
    }

    /** Daily in/out of the last 30 days as grouped columns. */
    private void dailyChart(JSONObject p) {
        JSONArray daily = arr(p, "daily");
        if (daily.length < 2) return;
        LinearLayout card = section("▤", "روند واریز و برداشت", "ban_act — ۳۰ روز گذشته؛ واریز و برداشت کنار هم");
        int n = Math.min(30, daily.length);
        String[] labels = new String[n];
        double[] in = new double[n], out = new double[n];
        for (int i = 0; i < n; i++) {
            JSONObject r = daily.optJSONObject(n - 1 - i);   // rows are newest-first
            if (r == null) r = new JSONObject();
            String date = s(r, "act_date", "");
            labels[i] = FinFmt.faNumber(date.length() >= 5 ? date.substring(date.length() - 5) : date);
            in[i] = d(r, "in_amount");
            out[i] = d(r, "out_amount");
        }
        FinCharts.Columns cols = ui.columnsChart(FinUi.FormatterKind.MONEY);
        cols.data(labels, new double[][]{in, out}, new int[]{FinUi.SUCCESS, FinUi.DANGER}, new String[]{"واریز", "برداشت"});
        addChart(card, cols, 210);
        addCard(card, 12);
    }

    /** POS settlement per bank over the same 30 days. */
    private void posChart(JSONObject p) {
        JSONArray rows = arr(p, "posBanks");
        if (rows.length() == 0) return;
        LinearLayout card = section("▣", "POS به تفکیک بانک", "تراکنش‌های کارت‌خوان در ۳۰ روز گذشته");
        int n = Math.min(6, rows.length());
        String[] labels = new String[n];
        double[] values = new double[n];
        int[] colors = new int[n];
        int[] palette = ui.palette();
        double total = 0;
        for (int x = 0; x < n; x++) {
            JSONObject r = rows.optJSONObject(x);
            if (r == null) r = new JSONObject();
            labels[x] = s(r, "bank_name", "—");
            values[x] = d(r, "total");
            colors[x] = palette[(x + 2) % palette.length];
            total += values[x];
        }
        FinCharts.Donut donut = ui.donutChart(FinUi.FormatterKind.MONEY);
        donut.data(labels, values, colors, "POS");
        java.util.List<FinCharts.Legend> items = new java.util.ArrayList<>();
        for (int x = 0; x < n; x++) items.add(new FinCharts.Legend(labels[x], money(values[x]), colors[x]));
        card.addView(ui.donutWithLegend(donut, items, total, 164), ui.lp(-1, -2));
        addCard(card, 12);
    }
}

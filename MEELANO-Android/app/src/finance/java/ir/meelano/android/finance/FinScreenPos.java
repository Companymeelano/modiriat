package ir.meelano.android.finance;

import android.widget.LinearLayout;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * «مرکز POS» — card-reader transactions, their settlement per bank and per operator, and the
 * transactions that have no receipt behind them.
 *
 * PosDetails.UserID is verified to be the visitor id ({@code visitors.vis_rdf}), and every
 * transaction is tied to its receipt through {@code dar.ghno}; a transaction without that link is
 * listed separately and never added to a total.
 */
public class FinScreenPos extends FinScreen {

    public FinScreenPos(AtiranFinanceActivity host) { super(host); }

    @Override public String title() { return "مرکز POS"; }

    @Override public String glyph() { return "▣"; }

    @Override protected String cacheKey() { return "pos:" + host.periodFrom() + ":" + host.periodTo(); }

    private LinearLayout body;

    @Override protected void populate() {
        add(hero("مرکز POS", "تراکنش‌های کارت‌خوان، تسویه هر بانک و هر اپراتور، و موارد بدون قبض", FinUi.MANAGER));
        final String[] keys = {"today", "7d", "30d", "month"};
        String[] labels = new String[keys.length];
        int active = 0;
        for (int i = 0; i < keys.length; i++) { labels[i] = FinFmt.periodLabel(keys[i]); if (keys[i].equals(host.periodKey())) active = i; }
        addCard(ui.segmented(labels, active, FinUi.MANAGER, index -> host.setPeriod(keys[index])), 6);
        body = ui.column();
        add(body);
    }

    @Override protected void fetch() {
        db.read(key(cacheKey()), 90_000L, c -> {
            String today = FinQueries.serverToday(c);
            JSONObject payload = FinQueries.posSummary(c, host.periodFrom(), host.periodTo());
            payload.put("today", today);
            payload.put("rows", FinQueries.posTransactions(c, host.periodFrom(), host.periodTo(), "", "", 60, 0));
            payload.put("unlinked", FinQueries.posWithoutReceipt(c, 30));
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
        k.addView(ui.kpiTile("جمع POS", compact(d(p, "total")), FinFmt.CURRENCY, i(p, "count") + " تراکنش", ui.goldAccent, null), ui.lp(0, -2, 1f));
        k.addView(ui.spacer(8), ui.lp(ui.dp(8), -2));
        k.addView(ui.kpiTile("کارمزد", compact(d(p, "fee")), FinFmt.CURRENCY, "PosDetails.Karmozd", FinUi.WARNING, null), ui.lp(0, -2, 1f));
        body.addView(k, ui.lp(-1, -2));

        LinearLayout k2 = ui.row();
        k2.addView(ui.kpiTile("حواله‌ای", compact(d(p, "havaleh")), FinFmt.CURRENCY, "IsHavaleh = 1", FinUi.INFO, null), ui.lp(0, -2, 1f));
        k2.addView(ui.spacer(8), ui.lp(ui.dp(8), -2));
        k2.addView(ui.kpiTile("بدون قبض", fa(i(p, "withoutReceipt")), "تراکنش", "نیازمند مغایرت‌گیری", FinUi.DANGER, v -> host.open(new FinScreenProblems(host))), ui.lp(0, -2, 1f));
        body.addView(k2, top(8));

        posCharts(p);

        LinearLayout list = section("☰", "تراکنش‌های POS", "PosDetails متصل به قبض، به ترتیب جدیدترین");
        JSONArray rows = arr(p, "rows");
        if (rows.length() == 0) {
            list.addView(stateText("تراکنش POS در این بازه ثبت نشده است؛ یا هیچ تراکنشی به قبض متصل نیست.", ui.textDim));
        } else {
            for (int x = 0; x < rows.length() && x < 40; x++) {
                JSONObject r = rows.optJSONObject(x);
                if (r == null) continue;
                String sub = s(r, "jalali_date", "—") + " · " + s(r, "bank_name", "—")
                        + " · " + (s(r, "user_name", "").isEmpty() ? "اپراتور نامشخص" : s(r, "user_name", ""))
                        + " · پیگیری " + s(r, "ShPeigiri", "—");
                list.addView(ui.listRow(s(r, "customer_name", "—") + "  (قبض " + fa(i(r, "ghno")) + ")",
                        sub, money(d(r, "MabPos")), i(r, "IsHavaleh") == 1 ? "حواله‌ای" : "کارتی",
                        ui.goldAccent, v -> host.details("تراکنش POS", new String[][]{
                                {"شناسه", fa(i(r, "ID"))},
                                {"قبض", fa(i(r, "ghno"))},
                                {"تاریخ", s(r, "jalali_date", "—")},
                                {"مبلغ", money(d(r, "MabPos"))},
                                {"کارمزد", money(d(r, "Karmozd"))},
                                {"بانک", s(r, "bank_name", "—")},
                                {"پیگیری", s(r, "ShPeigiri", "—")},
                                {"پایانه", fa(i(r, "TerminalID"))},
                                {"اپراتور", s(r, "user_name", "—") + " (" + s(r, "username", "—") + ")"},
                                {"مشتری", s(r, "customer_name", "—")},
                                {"جمع قبض", money(d(r, "receipt_total"))},
                                {"ویرایش‌شده", s(r, "isEdited", "f")}
                        }, "منبع: dbo.PosDetails (پیوند به dar از طریق ghno و به visitors از طریق UserID).")), ui.lp(-1, -2));
            }
        }
        body.addView(list, top(12));

        JSONArray unlinked = arr(p, "unlinked");
        LinearLayout ul = section("≠", "POS بدون قبض", unlinked.length() + " تراکنش بدون پیوند به dar");
        if (unlinked.length() == 0) {
            ul.addView(stateText("همه تراکنش‌های POS به قبض متصل هستند.", FinUi.SUCCESS));
        } else {
            for (int x = 0; x < unlinked.length() && x < 20; x++) {
                JSONObject r = unlinked.optJSONObject(x);
                if (r == null) continue;
                ul.addView(ui.listRow("تراکنش " + fa(i(r, "ID")) + " · قبض " + fa(i(r, "ghno")),
                        s(r, "bank_name", "—") + " · اپراتور " + fa(i(r, "UserID")) + " · پیگیری " + s(r, "ShPeigiri", "—"),
                        money(d(r, "MabPos")), "بدون قبض", FinUi.DANGER, v -> host.open(new FinScreenRecon(host))), ui.lp(-1, -2));
            }
        }
        body.addView(ul, top(12));
    }

    /** Donut by bank, bars by operator and a two-series column chart of the daily volume. */
    private void posCharts(JSONObject p) {
        JSONArray byBank = arr(p, "byBank");
        int[] palette = ui.palette();
        if (byBank.length() > 0) {
            LinearLayout card = section("▤", "به تفکیک بانک", "جمع و تعداد تراکنش‌های متصل به قبض");
            int n = Math.min(7, byBank.length());
            String[] labels = new String[n];
            double[] values = new double[n];
            int[] colors = new int[n];
            double total = 0;
            for (int x = 0; x < n; x++) {
                JSONObject r = byBank.optJSONObject(x);
                if (r == null) r = new JSONObject();
                labels[x] = s(r, "bank_name", "—");
                values[x] = d(r, "total");
                colors[x] = palette[x % palette.length];
                total += values[x];
            }
            FinCharts.Donut donut = ui.donutChart(FinUi.FormatterKind.MONEY);
            donut.data(labels, values, colors, "POS");
            java.util.List<FinCharts.Legend> items = new java.util.ArrayList<>();
            for (int x = 0; x < n; x++) items.add(new FinCharts.Legend(labels[x], money(values[x]), colors[x]));
            card.addView(ui.donutWithLegend(donut, items, total, 164), ui.lp(-1, -2));
            addCard(card, 12);
        }

        JSONArray byUser = arr(p, "byUser");
        if (byUser.length() > 0) {
            LinearLayout card = section("☺", "به تفکیک اپراتور", "PosDetails.UserID = visitors.vis_rdf");
            int n = Math.min(8, byUser.length());
            String[] labels = new String[n];
            double[] values = new double[n];
            String[] notes = new String[n];
            int[] colors = new int[n];
            for (int x = 0; x < n; x++) {
                JSONObject r = byUser.optJSONObject(x);
                if (r == null) r = new JSONObject();
                String name = s(r, "user_name", "—");
                labels[x] = name.trim().isEmpty() ? "ثبت‌نشده" : name;
                values[x] = d(r, "total");
                notes[x] = fa(i(r, "n")) + " تراکنش";
                colors[x] = palette[(x + 3) % palette.length];
            }
            FinCharts.Bars bars = ui.barsChart(FinUi.FormatterKind.MONEY);
            bars.data(labels, values, colors, notes);
            addBars(card, bars, n);
            addCard(card, 12);
        }

        JSONArray byDay = arr(p, "byDay");
        if (byDay.length >= 2) {
            LinearLayout card = section("▦", "روند روزانه POS", "جمع تراکنش‌های متصل به قبض در هر روز");
            int n = Math.min(30, byDay.length);
            String[] labels = new String[n];
            double[] values = new double[n];
            double[] counts = new double[n];
            for (int i = 0; i < n; i++) {
                JSONObject r = byDay.optJSONObject(n - 1 - i);   // newest-first → oldest-first
                if (r == null) r = new JSONObject();
                String date = s(r, "jalali_date", "");
                labels[i] = FinFmt.faNumber(date.length() >= 5 ? date.substring(date.length() - 5) : date);
                values[i] = d(r, "total");
                counts[i] = d(r, "n");
            }
            FinCharts.Columns cols = ui.columnsChart(FinUi.FormatterKind.MONEY);
            cols.data(labels, new double[][]{values}, new int[]{FinUi.MANAGER}, new String[]{"POS"});
            addChart(card, cols, 200);
            addCard(card, 12);
        }
    }

    private void group(String glyph, String title, JSONArray rows, String nameKey, String amountKey, String countKey) {
        LinearLayout card = section(glyph, title, "جمع و تعداد تراکنش‌های متصل به قبض");
        if (rows.length() == 0) {
            card.addView(stateText("داده‌ای در این بازه وجود ندارد.", ui.textDim));
            body.addView(card, top(12));
            return;
        }
        card.addView(ui.tableHeader(new String[]{"نام", "تعداد", "مبلغ"}), ui.lp(-1, -2));
        for (int x = 0; x < rows.length(); x++) {
            JSONObject r = rows.optJSONObject(x);
            if (r == null) continue;
            String name = s(r, nameKey, "—");
            if (name.trim().isEmpty()) name = "ثبت‌نشده";
            card.addView(ui.tableRow(new String[]{name, fa(i(r, countKey)), compact(d(r, amountKey))},
                    new float[]{1.6f, 0.7f, 1.2f}, false, ui.goldAccent), ui.lp(-1, -2));
        }
        body.addView(card, top(12));
    }
}

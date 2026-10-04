package ir.meelano.android.finance;

import android.text.InputType;
import android.widget.EditText;
import android.widget.LinearLayout;

import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.Connection;

import java.sql.PreparedStatement;

/**
 * «صندوق و تسویه کاربران» — the cash journal (COW) and the settlement of every operator.
 *
 * Expected vs Actual is computed only from data that really exists:
 *   Expected = cash + cheques + POS collected by that operator in the period (dar.rdf_vis and
 *              PosDetails.UserID), which is the money the operator has taken;
 *   Actual   = what was delivered, i.e. the sum of the settlement rows recorded in this app
 *              (meelano_fin_settlement), which is the only place the delivery actually exists;
 *   Difference = Expected − Actual, shown as it is — never adjusted by hand, never hidden.
 *
 * Cheque rows carry no operator in this data set (getchk.vis_rdf is 0 for all 118 rows), so the
 * cheque column stays empty and the screen says so instead of guessing an owner.
 */
public class FinScreenCash extends FinScreen {

    public FinScreenCash(AtiranFinanceActivity host) { super(host); }

    @Override public String title() { return "صندوق و تسویه"; }

    @Override public String glyph() { return "▩"; }

    @Override protected String cacheKey() { return "cash:" + host.periodFrom() + ":" + host.periodTo(); }

    private LinearLayout body;

    @Override protected void populate() {
        add(hero("صندوق و تسویه کاربران", "دفتر صندوق COW و تسویه هر اپراتور: مورد انتظار در برابر تحویل‌شده", FinUi.INFO));
        final String[] keys = {"today", "7d", "30d", "month"};
        String[] labels = new String[keys.length];
        int active = 0;
        for (int i = 0; i < keys.length; i++) { labels[i] = FinFmt.periodLabel(keys[i]); if (keys[i].equals(host.periodKey())) active = i; }
        addCard(ui.segmented(labels, active, FinUi.INFO, index -> host.setPeriod(keys[index])), 6);
        body = ui.column();
        add(body);
    }

    @Override protected void fetch() {
        db.read(key(cacheKey()), 90_000L, c -> queryPayload(c, host.periodFrom(), host.periodTo()), this::render);
    }

    /** The cash book, its hand-overs and per-operator settlement — all from the database. The post-login preload calls this same method. */
    public static JSONArray queryPayload(Connection c, String from, String to) throws Exception {
        String today = FinQueries.serverToday(c);
        JSONObject payload = FinQueries.cashSummary(c, today);
        payload.put("today", today);
        payload.put("movements", FinQueries.cashMovements(c, from, to, 80, 0));
        payload.put("settlements", FinQueries.settlements(c, from, to, 60));
        payload.put("operators", FinQueries.userSettlement(c, from, to));
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
        k.addView(ui.kpiTile("موجودی صندوق", compact(d(p, "balance")), FinFmt.CURRENCY, "COW.BED − COW.BES", ui.goldAccent, null), ui.lp(0, -2, 1f));
        k.addView(ui.spacer(8), ui.lp(ui.dp(8), -2));
        k.addView(ui.kpiTile("ورود امروز", compact(d(p, "inToday")), FinFmt.CURRENCY, i(p, "rowsToday") + " ردیف", FinUi.SUCCESS, null), ui.lp(0, -2, 1f));
        body.addView(k, ui.lp(-1, -2));

        LinearLayout k2 = ui.row();
        k2.addView(ui.kpiTile("خروج امروز", compact(d(p, "outToday")), FinFmt.CURRENCY, "COW.BES", FinUi.DANGER, null), ui.lp(0, -2, 1f));
        k2.addView(ui.spacer(8), ui.lp(ui.dp(8), -2));
        k2.addView(ui.kpiTile("موجودی اولیه", compact(d(p, "opening")), FinFmt.CURRENCY,
                "بازه داده: " + s(p, "firstDate", "—") + " تا " + s(p, "lastDate", "—"), ui.silver, null), ui.lp(0, -2, 1f));
        body.addView(k2, top(8));

        LinearLayout month = section("▦", "جمع ماه جاری صندوق", "ورود/خروج از ابتدای ماه شمسی");
        FinCharts.Ring ring = ui.ringChart();
        double inMonth = d(p, "inMonth"), outMonth = d(p, "outMonth");
        ring.data(outMonth, inMonth <= 0 ? Math.max(1, outMonth) : inMonth, "نسبت خروج به ورود ماه", outMonth > inMonth ? FinUi.WARNING : FinUi.SUCCESS);
        ring.note("ورود " + compact(inMonth));
        LinearLayout ringRow = ui.row();
        LinearLayout.LayoutParams rlp = new LinearLayout.LayoutParams(ui.dp(148), ui.dp(148));
        rlp.gravity = android.view.Gravity.CENTER_HORIZONTAL;
        ringRow.addView(ring, rlp);
        java.util.List<FinCharts.Legend> items = new java.util.ArrayList<>();
        items.add(new FinCharts.Legend("ورود ماه (BED)", money(inMonth), FinUi.SUCCESS));
        items.add(new FinCharts.Legend("خروج ماه (BES)", money(outMonth), FinUi.DANGER));
        LinearLayout legend = ui.legend(items, inMonth + outMonth);
        LinearLayout.LayoutParams llp = ui.lp(0, -2, 1f);
        llp.leftMargin = ui.dp(10);
        ringRow.addView(legend, llp);
        month.addView(ringRow, ui.lp(-1, -2));
        month.addView(ui.tableRow(new String[]{"ورود", money(inMonth), "خروج", money(outMonth)},
                new float[]{0.7f, 1.3f, 0.7f, 1.3f}, false, ui.goldAccent), top(6));
        body.addView(month, top(12));

        kinds(p);
        operators(p);
        settlements(p);
        movements(p);
    }

    private void kinds(JSONObject p) {
        JSONArray rows = arr(p, "byKind");
        LinearLayout card = section("◫", "ترکیب گردش صندوق", "نوع واقعی ردیف‌ها (COW.act_id)");
        if (rows.length() == 0) {
            card.addView(stateText("گردشی در صندوق ثبت نشده است.", ui.textDim));
            body.addView(card, top(12));
            return;
        }
        int n = Math.min(7, rows.length());
        String[] labels = new String[n];
        double[] values = new double[n];
        String[] notes = new String[n];
        int[] colors = new int[n];
        int[] palette = ui.palette();
        for (int x = 0; x < n; x++) {
            JSONObject r = rows.optJSONObject(x);
            if (r == null) r = new JSONObject();
            labels[x] = FinQueries.cashKindLabel(i(r, "act_id"));
            values[x] = d(r, "in_amount") + d(r, "out_amount");
            notes[x] = fa(i(r, "n")) + " ردیف · ورود " + compact(d(r, "in_amount")) + " · خروج " + compact(d(r, "out_amount"));
            colors[x] = palette[x % palette.length];
        }
        FinCharts.Bars bars = ui.barsChart(FinUi.FormatterKind.MONEY);
        bars.data(labels, values, colors, notes);
        addBars(card, bars, n);
        card.addView(ui.tableHeader(new String[]{"نوع", "تعداد", "ورود", "خروج"}), top(8));
        for (int x = 0; x < rows.length(); x++) {
            JSONObject r = rows.optJSONObject(x);
            if (r == null) continue;
            card.addView(ui.tableRow(new String[]{FinQueries.cashKindLabel(i(r, "act_id")), fa(i(r, "n")),
                    compact(d(r, "in_amount")), compact(d(r, "out_amount"))},
                    new float[]{1.4f, 0.6f, 1f, 1f}, false, ui.goldAccent), ui.lp(-1, -2));
        }
        body.addView(card, top(12));
    }

    private void operators(JSONObject p) {
        JSONArray rows = arr(p, "operators");
        LinearLayout card = section("☺", "تسویه اپراتورها", "Expected = نقد + چک + POS هر اپراتور · Actual = تحویل‌های ثبت‌شده");
        if (rows.length() == 0) {
            card.addView(stateText("اپراتوری با فعالیت در این بازه وجود ندارد.", ui.textDim));
            body.addView(card, top(12));
            return;
        }
        int on = Math.min(8, rows.length());
        String[] oLabels = new String[on];
        double[] oValues = new double[on];
        String[] oNotes = new String[on];
        int[] oColors = new int[on];
        int[] oPalette = ui.palette();
        for (int x = 0; x < on; x++) {
            JSONObject r = rows.optJSONObject(x);
            if (r == null) r = new JSONObject();
            double expected = d(r, "cash") + d(r, "checks") + d(r, "pos");
            double actual = d(r, "delivered");
            oLabels[x] = s(r, "vis_name", "—");
            oValues[x] = expected;
            oNotes[x] = "تحویل‌شده " + compact(actual) + " ریال · تفاوت " + ((expected - actual) > 0 ? "+" : "") + compact(expected - actual);
            oColors[x] = Math.abs(expected - actual) < 1000 ? FinUi.SUCCESS : oPalette[(x + 1) % oPalette.length];
        }
        FinCharts.Bars operatorBars = ui.barsChart(FinUi.FormatterKind.MONEY);
        operatorBars.data(oLabels, oValues, oColors, oNotes);
        addBars(card, operatorBars, on);
        card.addView(ui.tableHeader(new String[]{"اپراتور", "نقد", "چک", "POS", "تحویل", "تفاوت"}), top(8));
        for (int x = 0; x < rows.length(); x++) {
            JSONObject r = rows.optJSONObject(x);
            if (r == null) continue;
            double expected = d(r, "cash") + d(r, "checks") + d(r, "pos");
            double actual = d(r, "delivered");
            double diff = expected - actual;
            int color = Math.abs(diff) < 1000 ? FinUi.SUCCESS : FinUi.WARNING;
            LinearLayout row = ui.tableRow(new String[]{s(r, "vis_name", "—"), compact(d(r, "cash")), compact(d(r, "checks")),
                    compact(d(r, "pos")), compact(actual), (diff > 0 ? "+" : "") + compact(diff)},
                    new float[]{1.4f, 0.9f, 0.8f, 0.9f, 0.9f, 0.9f}, false, color);
            row.setOnClickListener(v -> settleDetails(r, expected, actual, diff));
            row.setClickable(true);
            card.addView(row, ui.lp(-1, -2));
        }
        card.addView(ui.text("چک‌های دریافتی در داده فعلی اپراتور ندارند (ستون vis_rdf روی چک خالی است)، پس ستون چک جمع عملیاتی را نشان می‌دهد و به حساب شخص نسبت داده نمی‌شود.",
                11f, ui.textFaint, false), ui.lp(-1, -2));
        body.addView(card, top(12));
    }

    private void settleDetails(JSONObject r, double expected, double actual, double diff) {
        host.details("تسویه " + s(r, "vis_name", "—"), new String[][]{
                {"شناسه ویزیتور", fa(i(r, "vis_rdf"))},
                {"نام کاربری", s(r, "username", "—")},
                {"فاکتور دوره", fa(i(r, "invoices"))},
                {"فروش دوره", money(d(r, "sales"))},
                {"وصول دوره (dar.rdf_vis)", money(d(r, "receipts"))},
                {"از این مبلغ نقد", money(d(r, "cash"))},
                {"از این مبلغ چک", money(d(r, "checks"))},
                {"POS", money(d(r, "pos"))},
                {"تعداد تحویل ثبت‌شده", fa(i(r, "deliveries"))},
                {"جمع تحویل‌شده (Actual)", money(actual)},
                {"مورد انتظار (Expected)", money(expected)},
                {"تفاوت", (diff > 0 ? "+" : "") + money(diff)},
                {"ثبت‌شده در", "meelano_fin_settlement"}
        }, "تحویل‌ها فقط از رکوردهای همین اپ خوانده می‌شود؛ اگر اپراتور تحویل نداده باشد، Actual صفر است و تفاوت واقعی نمایش داده می‌شود.");
    }

    private void settlements(JSONObject p) {
        JSONArray rows = arr(p, "settlements");
        LinearLayout card = section("⇄", "تحویل‌های ثبت‌شده", rows.length() + " رکورد در بازه");
        if (rows.length() == 0) {
            card.addView(stateText("در این بازه تحویلی ثبت نشده است.", ui.textDim));
        } else {
            for (int x = 0; x < rows.length() && x < 25; x++) {
                JSONObject r = rows.optJSONObject(x);
                if (r == null) continue;
                card.addView(ui.listRow(s(r, "user_login", "—") + " · " + s(r, "kind", ""),
                        s(r, "jalali_date", "—") + " · گیرنده " + s(r, "receiver", "—")
                                + " · ثبت " + s(r, "created_by", "—")
                                + (i(r, "has_signature") == 1 ? " · امضا ثبت شده" : ""),
                        money(d(r, "amount")), s(r, "status", "pending"),
                        FinUi.statusColor(s(r, "status", "pending")),
                        v -> host.details("تحویل وجه", new String[][]{
                                {"کاربر", s(r, "user_login", "—")},
                                {"تاریخ", s(r, "jalali_date", "—")},
                                {"نوع", s(r, "kind", "—")},
                                {"مبلغ", money(d(r, "amount"))},
                                {"مورد انتظار", money(d(r, "expected"))},
                                {"تفاوت", money(d(r, "difference"))},
                                {"مرجع", s(r, "reference", "—")},
                                {"گیرنده", s(r, "receiver", "—")},
                                {"وضعیت", s(r, "status", "—")},
                                {"تأییدکننده", s(r, "confirmed_by", "—") + " · " + s(r, "confirmed_at", "")},
                                {"ثبت‌کننده", s(r, "created_by", "—") + " · " + s(r, "created_at", "")},
                                {"امضای دیجیتال", i(r, "has_signature") == 1 ? "ثبت شده" : "ثبت نشده"},
                                {"شناسه عملیات (Idempotency)", s(r, "op_key", "—")}
                        }, "منبع: dbo.meelano_fin_settlement (جدول اختصاصی اپ).")), ui.lp(-1, -2));
            }
        }
        LinearLayout actions = ui.row();
        actions.addView(ui.button("ثبت تحویل وجه", ui.goldAccent, true, v -> newSettlement()), ui.lp(0, -2, 1f));
        LinearLayout.LayoutParams ap = ui.lp(-1, -2);
        ap.topMargin = ui.dp(6);
        card.addView(actions, ap);
        body.addView(card, top(12));
    }

    private void movements(JSONObject p) {
        JSONArray rows = arr(p, "movements");
        LinearLayout card = section("☰", "دفتر صندوق", "COW در بازه انتخاب‌شده");
        if (rows.length() == 0) {
            card.addView(stateText("گردشی در این بازه ثبت نشده است.", ui.textDim));
            body.addView(card, top(12));
            return;
        }
        for (int x = 0; x < rows.length() && x < 40; x++) {
            JSONObject r = rows.optJSONObject(x);
            if (r == null) continue;
            double bed = d(r, "BED");
            double bes = d(r, "BES");
            card.addView(ui.listRow(FinQueries.cashKindLabel(i(r, "act_id")) + " · " + s(r, "DATE", ""),
                    s(r, "DIS", "بدون شرح") + " · سند " + s(r, "DocNumber", "—") + " · قبض " + fa(i(r, "Ghno")),
                    money(bed - bes), bes > 0 ? "خروج" : "ورود", bes > 0 ? FinUi.WARNING : FinUi.SUCCESS,
                    v -> host.details("گردش صندوق", new String[][]{
                            {"تاریخ", s(r, "DATE", "—")},
                            {"نوع", FinQueries.cashKindLabel(i(r, "act_id"))},
                            {"شرح", s(r, "DIS", "—")},
                            {"ورود (BED)", money(bed)},
                            {"خروج (BES)", money(bes)},
                            {"قبض", fa(i(r, "Ghno"))},
                            {"سند", s(r, "DocNumber", "—") + " / " + s(r, "AccDocNumber", "—")},
                            {"بانک مرتبط", fa(i(r, "bank_rdf"))},
                            {"کاربر", fa(i(r, "UserID"))}
                    }, "منبع: dbo.COW (isActive=1).")), ui.lp(-1, -2));
        }
        body.addView(card, top(12));
    }

    /** Guarded write of a delivery: permission + transaction + audit + duplicate protection. */
    private void newSettlement() {
        if (!FinDb.tableAvailable("meelano_fin_settlement")) {
            host.toast("این قابلیت به جدول اختصاصی «آتیران مالی» نیاز دارد که در این دیتابیس ساخته نشده است. خواندن همهٔ بخش‌ها از جدول‌های خود آتیران کار می‌کند و برنامه هیچ جدولی نمی‌سازد.");
            return;
        }
        if (!host.can("finance_create")) {
            host.toast("برای ثبت تحویل وجه مجوز finance_create لازم است.");
            return;
        }
        final EditText user = ui.field("نام کاربری تحویل‌دهنده (visitors.Username)");
        final EditText kind = ui.field("نوع: نقد / چک / POS");
        final EditText amount = ui.field("مبلغ تحویل‌شده (ریال)");
        amount.setInputType(InputType.TYPE_CLASS_NUMBER);
        final EditText expected = ui.field("مبلغ مورد انتظار (اختیاری)");
        expected.setInputType(InputType.TYPE_CLASS_NUMBER);
        final EditText receiver = ui.field("گیرنده وجه");
        final EditText reference = ui.field("مرجع (اختیاری)");

        LinearLayout col = ui.column();
        col.setPadding(ui.dp(16), ui.dp(8), ui.dp(16), ui.dp(8));
        for (EditText f : new EditText[]{user, kind, amount, expected, receiver, reference}) col.addView(f, ui.lp(-1, -2));
        col.addView(ui.text("مبلغ تحویل باید با مبلغ مورد انتظار مقایسه شود؛ تفاوت به‌صورت خودکار محاسبه و ذخیره می‌گردد. تاریخ از تاریخ سرور گرفته می‌شود.",
                11f, ui.textDim, false), ui.lp(-1, -2));

        new android.app.AlertDialog.Builder(host)
                .setTitle("ثبت تحویل وجه")
                .setView(col)
                .setNegativeButton("انصراف", null)
                .setPositiveButton("ثبت", (dlg, which) -> {
                    String u = user.getText().toString().trim();
                    String k = kind.getText().toString().trim();
                    if (u.isEmpty()) { host.toast("نام کاربری تحویل‌دهنده لازم است."); return; }
                    if (k.isEmpty()) k = "نقد";
                    double a = parse(amount.getText().toString());
                    double ex = parse(expected.getText().toString());
                    if (a <= 0) { host.toast("مبلغ تحویل باید بزرگ‌تر از صفر باشد."); return; }
                    final double amountValue = a;
                    final double expectedValue = ex;
                    final String login = u;
                    final String kindValue = k;
                    final String receiverValue = receiver.getText().toString().trim();
                    final String referenceValue = reference.getText().toString().trim();
                    JSONObject args = new JSONObject();
                    try {
                        args.put("amount", amountValue);
                        args.put("reference", referenceValue.isEmpty() ? login : referenceValue);
                    } catch (Exception ignored) { }
                    String opKey = FinDb.opKey("cash", "settlement", login, kindValue, amountValue, host.today(), receiverValue);
                    db.runGuarded(opKey, "cash", "ثبت تحویل وجه", args, (c, arg) -> {
                        JSONObject out = new JSONObject();
                        String jdate = FinQueries.serverToday(c);
                        try (PreparedStatement ps = c.prepareStatement(
                                "INSERT INTO dbo.meelano_fin_settlement(op_key, user_login, jalali_date, kind, amount, expected, "
                                        + "difference, reference, receiver, status, created_by) VALUES(?,?,?,?,?,?,?,?,?,N'pending',?)",
                                new String[]{"id"})) {
                            FinDb.bind(ps, new Object[]{opKey, login, jdate, kindValue, amountValue,
                                    expectedValue > 0 ? expectedValue : null,
                                    expectedValue > 0 ? (expectedValue - amountValue) : null,
                                    referenceValue.isEmpty() ? null : referenceValue,
                                    receiverValue.isEmpty() ? null : receiverValue,
                                    FinSession.username()});
                            ps.executeUpdate();
                        }
                        out.put("jalali_date", jdate);
                        out.put("amount", amountValue);
                        return out;
                    }, res -> {
                        if (res.optBoolean("ok", false)) {
                            host.toast(res.optBoolean("duplicate", false)
                                    ? "این تحویل قبلاً ثبت شده بود (بدون تکرار)."
                                    : "تحویل وجه ثبت شد.");
                            load(true);
                        } else {
                            host.toast("ثبت انجام نشد. کد رویداد: " + FinFmt.eventCode(res.optString("error", "")));
                        }
                    });
                })
                .show();
    }

    private double parse(String v) {
        try {
            return Double.parseDouble(FinFmt.faNumber(v).replaceAll("[^0-9.]", ""));
        } catch (Exception e) {
            return 0d;
        }
    }
}

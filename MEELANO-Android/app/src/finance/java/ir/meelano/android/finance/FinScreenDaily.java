package ir.meelano.android.finance;

import android.text.InputType;
import android.widget.EditText;
import android.widget.LinearLayout;

import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.PreparedStatement;

/**
 * «گزارش روزانه و بستن روز» — the operational close of one Jalali day.
 *
 * The report gathers the day's real figures (sales, receipts, cash, POS, due cheques, mismatches) and
 * the closing itself is stored in {@code meelano_fin_dayclose} with a snapshot of those figures inside
 * the checklist JSON, so a later audit can see exactly what was on screen when the day was closed.
 * Closing the day does not change any balance in Atiran; it only records that the day was reviewed.
 */
public class FinScreenDaily extends FinScreen {

    public FinScreenDaily(AtiranFinanceActivity host) { super(host); }

    @Override public String title() { return "گزارش روزانه"; }

    @Override public String glyph() { return "☑"; }

    @Override protected String cacheKey() { return "daily:" + host.today(); }

    private LinearLayout body;

    @Override protected void populate() {
        LinearLayout head = ui.row();
        head.addView(ui.text("گزارش و بستن روز", 17f, ui.textColor, true), ui.lp(0, -2, 1f));
        head.addView(ui.chip("امروز " + fa(host.today()), ui.goldAccent), ui.lp(-2, -2));
        add(head);
        body = ui.column();
        add(body);
    }

    @Override protected void fetch() {
        db.read(key(cacheKey()), 60_000L, c -> {
            String today = FinQueries.serverToday(c);
            JSONObject payload = FinQueries.dailyReport(c, today);
            payload.put("today", today);
            payload.put("closes", FinDb.select(c, "SELECT TOP (20) id, jalali_date, jdate_key, open_issues, status, note, "
                    + "closed_by, created_by, CONVERT(nvarchar(19), closed_at, 120) AS closed_at, "
                    + "CONVERT(nvarchar(19), created_at, 120) AS created_at "
                    + "FROM dbo.meelano_fin_dayclose WITH (NOLOCK) ORDER BY id DESC"));
            payload.put("mismatchCount", FinQueries.mismatchCount(c, today, today));
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
        k.addView(ui.kpiTile("فروش امروز", compact(d(p, "salesToday")), FinFmt.CURRENCY, i(p, "salesCountToday") + " فاکتور", ui.goldAccent, null), ui.lp(0, -2, 1f));
        k.addView(ui.spacer(8), ui.lp(ui.dp(8), -2));
        k.addView(ui.kpiTile("وصول امروز", compact(d(p, "receivedToday")), FinFmt.CURRENCY, i(p, "receiptsToday") + " قبض", FinUi.SUCCESS, null), ui.lp(0, -2, 1f));
        body.addView(k, ui.lp(-1, -2));

        LinearLayout k2 = ui.row();
        k2.addView(ui.kpiTile("نقد", compact(d(p, "cashToday")), FinFmt.CURRENCY, "dar.naghd", FinUi.INFO, null), ui.lp(0, -2, 1f));
        k2.addView(ui.spacer(8), ui.lp(ui.dp(8), -2));
        k2.addView(ui.kpiTile("POS", compact(d(p, "posToday")), FinFmt.CURRENCY, i(p, "posCountToday") + " تراکنش", FinUi.MANAGER, null), ui.lp(0, -2, 1f));
        body.addView(k2, top(8));

        LinearLayout checks = section("◫", "چک‌های سررسیدشده تا امروز", arr(p, "checksDueList").length() + " ردیف");
        JSONArray due = arr(p, "checksDueList");
        if (due.length() == 0) {
            checks.addView(stateText("چکی تا امروز سررسید نشده است.", FinUi.SUCCESS));
        } else {
            for (int x = 0; x < due.length() && x < 20; x++) {
                JSONObject r = due.optJSONObject(x);
                if (r == null) continue;
                boolean overdue = s(r, "sardate", "").compareTo(host.today()) < 0;
                checks.addView(ui.listRow(s(r, "name", "—") + " · " + s(r, "shgetchk", ""),
                        "سررسید " + s(r, "sardate", "—") + " · " + s(r, "status_name", ""),
                        money(d(r, "getchkmab")), overdue ? "سررسید گذشته" : "سررسید امروز",
                        overdue ? FinUi.DANGER : FinUi.WARNING, null), ui.lp(-1, -2));
            }
        }
        body.addView(checks, top(12));

        LinearLayout receipts = section("⇄", "قبض‌های امروز", arr(p, "receiptsToday").length() + " ردیف");
        JSONArray receiptsRows = arr(p, "receiptsToday");
        if (receiptsRows.length() == 0) {
            receipts.addView(stateText("امروز قبضی ثبت نشده است.", ui.textDim));
        } else {
            for (int x = 0; x < receiptsRows.length() && x < 20; x++) {
                JSONObject r = receiptsRows.optJSONObject(x);
                if (r == null) continue;
                receipts.addView(ui.listRow(s(r, "name", "—") + " · قبض " + fa(i(r, "ghno")),
                        "نقد " + money(d(r, "naghd")) + " · چک " + money(d(r, "mabcheck")) + " · POS " + money(d(r, "pos")),
                        money(d(r, "mab")), "", ui.goldAccent, null), ui.lp(-1, -2));
            }
        }
        body.addView(receipts, top(12));

        JSONArray mismatches = arr(p, "mismatchToday");
        LinearLayout problems = section("≠", "کارهای باز امروز", "مغایرت قبض و پرونده‌های باز");
        problems.addView(stateText("مغایرت اجزای دریافت امروز: " + fa(mismatches.length()) + " مورد", mismatches.length() > 0 ? FinUi.DANGER : FinUi.SUCCESS));
        problems.addView(stateText("پرونده‌های باز مغایرت (کل): " + fa(arr(p, "openRecon").length()) + " مورد", ui.textDim));
        problems.addView(stateText("POS بدون قبض (کل): " + fa(i(p, "posWithoutReceipt")) + " تراکنش", ui.textDim));
        problems.addView(ui.button("ورود به مرکز مغایرت‌ها", ui.goldAccent, false, v -> host.open(new FinScreenRecon(host))), ui.lp(-1, -2));
        body.addView(problems, top(12));

        closes(p);
    }

    private void closes(JSONObject p) {
        JSONArray rows = arr(p, "closes");
        LinearLayout card = section("▣", "بستن روز", rows.length() + " سابقه ثبت‌شده");
        if (rows.length() == 0) {
            card.addView(stateText("تا امروز روزی بسته نشده است.", ui.textDim));
        } else {
            for (int x = 0; x < rows.length() && x < 10; x++) {
                JSONObject r = rows.optJSONObject(x);
                if (r == null) continue;
                card.addView(ui.listRow(s(r, "jalali_date", "—") + " · " + s(r, "status", "draft"),
                        "کارهای باز " + fa(i(r, "open_issues")) + " · " + s(r, "note", "بدون یادداشت")
                                + " · ثبت " + s(r, "created_by", "—"),
                        "", s(r, "status", "draft"), FinUi.statusColor(s(r, "status", "draft")), null), ui.lp(-1, -2));
            }
        }
        card.addView(ui.button("بستن روز " + host.today(), ui.goldAccent, true, v -> closeDay(p)), ui.lp(-1, -2));
        body.addView(card, top(12));
    }

    /** Records the closing together with the exact snapshot of that day's figures. */
    private void closeDay(JSONObject p) {
        if (!host.can("finance_daily_close")) {
            host.toast("برای بستن روز مجوز finance_daily_close لازم است.");
            return;
        }
        final EditText note = ui.field("یادداشت بستن روز (اختیاری)");
        note.setInputType(InputType.TYPE_CLASS_TEXT);
        LinearLayout col = ui.column();
        col.setPadding(ui.dp(16), ui.dp(8), ui.dp(16), ui.dp(8));
        col.addView(ui.text("گزارش زیر همراه با همین اعداد به‌عنوان نسخه زمان بستن ثبت می‌شود:", 12f, ui.textDim, false), ui.lp(-1, -2));
        col.addView(ui.text("فروش " + money(d(p, "salesToday")) + " · وصول " + money(d(p, "receivedToday"))
                + " · نقد " + money(d(p, "cashToday")) + " · POS " + money(d(p, "posToday")), 12f, ui.textColor, false), ui.lp(-1, -2));
        col.addView(note, ui.lp(-1, -2));

        new android.app.AlertDialog.Builder(host)
                .setTitle("بستن روز " + host.today())
                .setView(col)
                .setNegativeButton("انصراف", null)
                .setPositiveButton("ثبت بستن روز", (dlg, which) -> {
                    final String noteText = note.getText().toString().trim();
                    int openIssues = arr(p, "mismatchToday").length() + arr(p, "openRecon").length();
                    JSONObject args = new JSONObject();
                    try {
                        args.put("amount", d(p, "receivedToday"));
                        args.put("reference", host.today());
                    } catch (Exception ignored) { }
                    String opKey = FinDb.opKey("daily", "close", host.today());
                    final int issues = openIssues;
                    final JSONObject snapshot = p;
                    db.runGuarded(opKey, "daily", "بستن روز", args, (c, a) -> {
                        JSONObject out = new JSONObject();
                        try (PreparedStatement ps = c.prepareStatement(
                                "IF NOT EXISTS (SELECT 1 FROM dbo.meelano_fin_dayclose WHERE jdate_key = ?) "
                                        + "INSERT INTO dbo.meelano_fin_dayclose(jalali_date, jdate_key, checklist, open_issues, status, note, closed_by, closed_at, created_by) "
                                        + "VALUES(?,?,?,?,N'closed',?,?,SYSDATETIME(),?)", new String[]{"id"})) {
                            FinDb.bind(ps, new Object[]{host.today(), host.today(),
                                    trimTo(snapshot.toString(), 4000), issues,
                                    noteText.isEmpty() ? null : noteText, FinSession.username(), FinSession.username()});
                            ps.executeUpdate();
                        }
                        out.put("jalali_date", host.today());
                        return out;
                    }, res -> {
                        if (res.optBoolean("ok", false)) {
                            host.toast(res.optBoolean("duplicate", false) ? "این روز قبلاً بسته شده بود." : "روز بسته شد و گزارش ثبت گردید.");
                            load(true);
                        } else {
                            host.toast("بستن روز انجام نشد. کد رویداد: " + FinFmt.eventCode(res.optString("error", "")));
                        }
                    });
                })
                .show();
    }
}

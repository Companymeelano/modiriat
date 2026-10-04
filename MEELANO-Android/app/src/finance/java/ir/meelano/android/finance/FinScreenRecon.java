package ir.meelano.android.finance;

import android.widget.LinearLayout;

import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.Connection;

import java.sql.PreparedStatement;

/**
 * «مغایرت‌گیری بانکی» — Statement → Match → Resolve → Confirm.
 *
 * The screen works on real rows only:
 *   • statement side : {@code ban_act} (what the bank/our books recorded),
 *   • document side  : {@code dar} receipts and {@code PosDetails} transactions,
 *   • open differences: a receipt whose components do not equal its total, a POS transaction with no
 *     receipt, and a bank movement with no receipt.
 *
 * A case is opened from one of those rows with its real reference and amount, then resolved and
 * confirmed. Every state change goes through {@link FinDb#runGuarded}: permission, transaction,
 * audit row and duplicate protection. Balances are never touched.
 */
public class FinScreenRecon extends FinScreen {

    public FinScreenRecon(AtiranFinanceActivity host) { super(host); }

    @Override public String title() { return "مغایرت‌گیری"; }

    @Override public String glyph() { return "≠"; }

    @Override protected String cacheKey() { return "recon:" + host.periodFrom() + ":" + host.periodTo(); }

    private LinearLayout body;

    @Override protected void populate() {
        add(hero("مغایرت‌گیری بانکی", "Statement → Match → Resolve → Confirm روی ردیف‌های واقعی بانک و قبض", FinUi.MANAGER));
        body = ui.column();
        add(body);
    }

    @Override protected void fetch() {
        db.read(key(cacheKey()), 60_000L, c -> queryPayload(c, host.periodFrom(), host.periodTo()), this::render);
    }

    /** Bank reconciliation cases of the period, read from the database. The post-login preload calls this same method. */
    public static JSONArray queryPayload(Connection c, String from, String to) throws Exception {
        String today = FinQueries.serverToday(c);
        JSONObject payload = new JSONObject();
        payload.put("today", today);
        payload.put("cases", FinQueries.reconCases(c, 80));
        payload.put("mismatches", FinQueries.receiptMismatches(c, from, to, 20));
        payload.put("posUnlinked", FinQueries.posWithoutReceipt(c, 20));
        payload.put("bankUnlinked", FinQueries.bankWithoutReceipt(c, from, to, 20));
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

        JSONArray cases = arr(p, "cases");
        JSONArray mismatches = arr(p, "mismatches");
        JSONArray posUnlinked = arr(p, "posUnlinked");
        JSONArray bankUnlinked = arr(p, "bankUnlinked");

        int open = 0, review = 0, resolved = 0;
        for (int x = 0; x < cases.length(); x++) {
            JSONObject r = cases.optJSONObject(x);
            if (r == null) continue;
            String st = s(r, "status", "open");
            if ("open".equals(st)) open++;
            else if ("review".equals(st)) review++;
            else resolved++;
        }

        LinearLayout chart = section("◎", "وضعیت پرونده‌ها", "پرونده‌های ثبت‌شده در meelano_fin_recon");
        int[] palette = ui.palette();
        FinCharts.Donut donut = ui.donutChart(FinUi.FormatterKind.COUNT);
        donut.data(new String[]{"باز", "در بررسی", "بسته‌شده"}, new double[]{open, review, Math.max(0, resolved)},
                new int[]{palette[5], palette[3], palette[1]}, "پرونده‌ها");
        donut.empty("هنوز پرونده مغایرتی ثبت نشده است.");
        java.util.List<FinCharts.Legend> items = new java.util.ArrayList<>();
        items.add(new FinCharts.Legend("باز", FinFmt.count(open), palette[5]));
        items.add(new FinCharts.Legend("در بررسی", FinFmt.count(review), palette[3]));
        items.add(new FinCharts.Legend("بسته‌شده", FinFmt.count(Math.max(0, resolved)), palette[1]));
        chart.addView(ui.donutWithLegend(donut, items, open + review + resolved, 152), ui.lp(-1, -2));
        chart.addView(ui.miniStat("منبع بازها: مغایرت قبض", FinFmt.count(mismatches.length()) + " مورد", palette[5]), top(6));
        chart.addView(ui.miniStat("منبع بازها: POS بدون قبض", FinFmt.count(posUnlinked.length()) + " مورد", palette[3]), ui.lp(-1, -2));
        chart.addView(ui.miniStat("منبع بازها: گردش بانکی بدون قبض", FinFmt.count(bankUnlinked.length()) + " مورد", palette[2]), ui.lp(-1, -2));
        body.addView(chart, top(12));

        LinearLayout k = ui.row();
        k.addView(ui.kpiTile("باز", fa(open), "پرونده", "در انتظار بررسی", FinUi.DANGER, null), ui.lp(0, -2, 1f));
        k.addView(ui.spacer(6), ui.lp(ui.dp(6), -2));
        k.addView(ui.kpiTile("در بررسی", fa(review), "پرونده", "نیازمند تصمیم", FinUi.WARNING, null), ui.lp(0, -2, 1f));
        k.addView(ui.spacer(6), ui.lp(ui.dp(6), -2));
        k.addView(ui.kpiTile("بسته‌شده", fa(resolved), "پرونده", "تأییدشده", FinUi.SUCCESS, null), ui.lp(0, -2, 1f));
        body.addView(k, ui.lp(-1, -2));

        LinearLayout sources = section("⌕", "منابع مغایرت در بازه", "هر ردیف یک مورد واقعی است");
        sources.addView(ui.listRow("مغایرت اجزای قبض", "قبض‌هایی که نقد+چک+POS آن‌ها با جمع نمی‌خواند",
                fa(mismatches.length()) + " مورد", mismatches.length() > 0 ? "نیازمند بررسی" : "سالم",
                mismatches.length() > 0 ? FinUi.DANGER : FinUi.SUCCESS, null), ui.lp(-1, -2));
        sources.addView(ui.listRow("POS بدون قبض", "تراکنش PosDetails بدون ردیف در dar",
                fa(posUnlinked.length()) + " مورد", posUnlinked.length() > 0 ? "باز" : "سالم",
                posUnlinked.length() > 0 ? FinUi.WARNING : FinUi.SUCCESS, null), ui.lp(-1, -2));
        sources.addView(ui.listRow("گردش بانکی بدون قبض", "ردیف ban_act بدون قبض مرتبط",
                fa(bankUnlinked.length()) + " مورد", bankUnlinked.length() > 0 ? "باز" : "سالم",
                bankUnlinked.length() > 0 ? FinUi.WARNING : FinUi.SUCCESS, null), ui.lp(-1, -2));
        body.addView(sources, top(12));

        LinearLayout openCases = section("◫", "پرونده‌های مغایرت", "meelano_fin_recon — با امتیازدهی و تأیید");
        if (cases.length() == 0) {
            openCases.addView(stateText("هنوز پرونده مغایرتی ثبت نشده است. از ردیف‌های زیر یک مورد باز کنید.", ui.textDim));
        } else {
            for (int x = 0; x < cases.length() && x < 30; x++) {
                JSONObject r = cases.optJSONObject(x);
                if (r == null) continue;
                final JSONObject row = r;
                String st = s(r, "status", "open");
                openCases.addView(ui.listRow(s(r, "kind", "—") + " · " + s(r, "case_key", ""),
                        s(r, "jalali_date", "—") + " · " + s(r, "reason", "بدون توضیح")
                                + " · مسئول " + s(r, "assigned_to", "—"),
                        money(d(r, "amount")), statusWord(st), FinUi.statusColor(st),
                        v -> caseActions(row)), ui.lp(-1, -2));
            }
        }
        body.addView(openCases, top(12));

        candidates("مغایرت قبض", mismatches, "receipt");
        candidates("POS بدون قبض", posUnlinked, "pos");
        candidates("گردش بانکی بدون قبض", bankUnlinked, "bank");
    }

    private String statusWord(String status) {
        if ("open".equals(status)) return "باز";
        if ("review".equals(status)) return "در بررسی";
        if ("resolved".equals(status)) return "رفع‌شده";
        if ("confirmed".equals(status)) return "تأییدشده";
        if ("rejected".equals(status)) return "رد‌شده";
        return status;
    }

    private void candidates(String title, JSONArray rows, String kind) {
        LinearLayout card = section("⌕", title, rows.length() + " ردیف قابل تبدیل به پرونده");
        if (rows.length() == 0) {
            card.addView(stateText("موردی در این بخش وجود ندارد.", ui.textFaint));
            body.addView(card, top(12));
            return;
        }
        for (int x = 0; x < rows.length() && x < 12; x++) {
            JSONObject r = rows.optJSONObject(x);
            if (r == null) continue;
            String main;
            String sub;
            double amount;
            String ref;
            if ("receipt".equals(kind)) {
                main = "قبض " + fa(i(r, "ghno")) + " · " + s(r, "name", "—");
                sub = s(r, "date", "—") + " · تفاوت " + money(d(r, "difference"));
                amount = d(r, "mab");
                ref = "dar:" + i(r, "ghno");
            } else if ("pos".equals(kind)) {
                main = "تراکنش POS " + fa(i(r, "ID")) + " · قبض " + fa(i(r, "ghno"));
                sub = s(r, "bank_name", "—") + " · اپراتور " + fa(i(r, "UserID"));
                amount = d(r, "MabPos");
                ref = "pos:" + i(r, "ID");
            } else {
                main = FinQueries.bankKindLabel(i(r, "act_id")) + " · " + s(r, "act_date", "—");
                sub = s(r, "act_dis", "بدون شرح") + " · قبض " + fa(i(r, "Ghno"));
                amount = d(r, "act_bed") - d(r, "act_bes");
                ref = "ban:" + i(r, "rdf");
            }
            final String reference = ref;
            final String titleText = main;
            final double money = amount;
            card.addView(ui.listRow(main, sub + " · " + reference, money(amount), "باز کردن پرونده", ui.goldAccent,
                    v -> openCase(kind, reference, titleText, money, sub)), ui.lp(-1, -2));
        }
        body.addView(card, top(12));
    }

    /** Opens (or re-uses) a reconciliation case: idempotent through the case_key unique index. */
    private void openCase(String kind, String reference, String title, double amount, String reason) {
        if (!FinDb.tableAvailable("meelano_fin_recon")) {
            host.toast("این قابلیت به جدول اختصاصی «آتیران مالی» نیاز دارد که در این دیتابیس ساخته نشده است. خواندن همهٔ بخش‌ها از جدول‌های خود آتیران کار می‌کند و برنامه هیچ جدولی نمی‌سازد.");
            return;
        }
        if (!host.can("finance_reconcile")) {
            host.toast("برای ثبت پرونده مغایرت مجوز finance_reconcile لازم است.");
            return;
        }
        if (!host.can("finance_create") && !host.can("finance_reconcile")) {
            host.toast("دسترسی کافی برای این عملیات وجود ندارد.");
            return;
        }
        String caseKey = kind + "|" + reference + "|" + host.today();
        JSONObject args = new JSONObject();
        try {
            args.put("amount", amount);
            args.put("reference", reference);
        } catch (Exception ignored) { }
        String opKey = FinDb.opKey("recon", "open", caseKey);
        db.runGuarded(opKey, "recon", "باز کردن پرونده مغایرت", args, (c, a) -> {
            JSONObject out = new JSONObject();
            String jdate = FinQueries.serverToday(c);
            try (PreparedStatement ps = c.prepareStatement(
                    "IF NOT EXISTS (SELECT 1 FROM dbo.meelano_fin_recon WHERE case_key = ?) "
                            + "INSERT INTO dbo.meelano_fin_recon(case_key, kind, jalali_date, amount, system_ref, reason, status, created_by) "
                            + "VALUES(?,?,?,?,?,N'open',?)", new String[]{"id"})) {
                FinDb.bind(ps, new Object[]{caseKey, caseKey, kind, jdate, amount, reference,
                        reason == null ? "" : trimTo(reason, 380), FinSession.username()});
                ps.executeUpdate();
            }
            out.put("case_key", caseKey);
            return out;
        }, res -> {
            if (res.optBoolean("ok", false)) {
                host.toast("پرونده مغایرت " + (res.optBoolean("duplicate", false) ? "قبلاً ثبت شده بود" : "ثبت شد") + ".");
                load(true);
            } else {
                host.toast("ثبت پرونده انجام نشد. کد رویداد: " + FinFmt.eventCode(res.optString("error", "")));
            }
        });
    }

    private void caseActions(JSONObject c) {
        final String caseKey = s(c, "case_key", "");
        final String status = s(c, "status", "open");
        final double amount = d(c, "amount");
        String[][] info = {
                {"شناسه", caseKey},
                {"نوع", s(c, "kind", "—")},
                {"تاریخ", s(c, "jalali_date", "—")},
                {"مبلغ", money(amount) + " " + FinFmt.CURRENCY},
                {"مرجع سیستم", s(c, "system_ref", "—")},
                {"مرجع بانک", s(c, "bank_ref", "—")},
                {"شرح", s(c, "reason", "—")},
                {"وضعیت", statusWord(status)},
                {"مسئول", s(c, "assigned_to", "—")},
                {"راه‌حل", s(c, "resolution", "—")},
                {"تأییدکننده", s(c, "approved_by", "—")},
                {"ثبت‌کننده", s(c, "created_by", "—") + " · " + s(c, "created_at", "")}
        };
        StringBuilder actions = new StringBuilder();
        if ("open".equals(status)) actions.append("→ در بررسی");
        else if ("review".equals(status)) actions.append("→ رفع شد");
        else if ("resolved".equals(status)) actions.append("→ تأیید نهایی");
        else actions.append("پرونده بسته است");
        android.app.AlertDialog.Builder b = new android.app.AlertDialog.Builder(host);
        b.setTitle("پرونده مغایرت");
        StringBuilder text = new StringBuilder();
        for (String[] line : info) text.append(line[0]).append(": ").append(line[1]).append('\n');
        text.append("\nگام بعدی: ").append(actions);
        b.setMessage(text.toString());
        if (!"resolved".equals(status) && !"confirmed".equals(status) && !"rejected".equals(status)) {
            b.setPositiveButton(actions.toString(), (dlg, which) -> advance(c, status));
        }
        b.setNeutralButton("بستن", null);
        b.show();
    }

    private void advance(JSONObject c, String status) {
        String next = "open".equals(status) ? "review" : ("review".equals(status) ? "resolved" : "confirmed");
        boolean needsApprove = "confirmed".equals(next);
        if (needsApprove && !host.can("finance_approve")) {
            host.toast("تأیید نهایی نیازمند مجوز finance_approve است.");
            return;
        }
        if (!needsApprove && !host.can("finance_reconcile")) {
            host.toast("این گام نیازمند مجوز finance_reconcile است.");
            return;
        }
        final String caseKey = s(c, "case_key", "");
        final String nextStatus = next;
        JSONObject args = new JSONObject();
        try {
            args.put("amount", d(c, "amount"));
            args.put("reference", caseKey);
        } catch (Exception ignored) { }
        String opKey = FinDb.opKey("recon", "advance", caseKey, nextStatus, FinSession.username());
        db.runGuarded(opKey, "recon", "تغییر وضعیت مغایرت به " + nextStatus, args, (conn, a) -> {
            JSONObject out = new JSONObject();
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE dbo.meelano_fin_recon SET status=?, resolution=ISNULL(resolution, ?), "
                            + "assigned_to=ISNULL(assigned_to, ?), approved_by=CASE WHEN ?=N'confirmed' THEN ? ELSE approved_by END, "
                            + "updated_at=SYSDATETIME() WHERE case_key=?")) {
                FinDb.bind(ps, new Object[]{nextStatus, "به‌روزرسانی از اپ آتیران مالی", FinSession.username(),
                        nextStatus, FinSession.username(), caseKey});
                out.put("rows", ps.executeUpdate());
            }
            out.put("status", nextStatus);
            return out;
        }, res -> {
            if (res.optBoolean("ok", false)) {
                host.toast("وضعیت پرونده: " + statusWord(nextStatus));
                load(true);
            } else {
                host.toast("تغییر وضعیت انجام نشد. کد رویداد: " + FinFmt.eventCode(res.optString("error", "")));
            }
        });
    }
}

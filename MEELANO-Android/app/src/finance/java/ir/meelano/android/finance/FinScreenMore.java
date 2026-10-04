package ir.meelano.android.finance;

import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;

import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.Connection;

/**
 * «بیشتر» — the module directory, the theme switcher, the activity log and the about box.
 *
 * Every entry is gated by the real finance permission of the signed-in role, so an operator without
 * {@code finance_reconcile} simply does not see the reconciliation entry. The activity log reads the
 * audit rows written by this app (meelano_fin_audit) and falls back to real Atiran receipts when the
 * app itself has not recorded anything yet.
 */
public class FinScreenMore extends FinScreen {

    public FinScreenMore(AtiranFinanceActivity host) { super(host); }

    @Override public String title() { return "بیشتر"; }

    @Override public String glyph() { return "⋯"; }

    @Override protected String cacheKey() { return "more:" + host.today(); }

    private LinearLayout body;

    @Override protected void populate() {
        add(hero("همه بخش‌ها", "دسترسی هر بخش بر پایه نقش واقعی خوانده‌شده از جدول دسترسی است", ui.goldAccent));
        body = ui.column();
        add(body);
    }

    @Override protected void fetch() {
        db.read(key(cacheKey()), 120_000L, c -> queryPayload(c), this::render);
    }

    /** The audit trail of what this app wrote, read from the database. The post-login preload calls this same method. */
    public static JSONArray queryPayload(Connection c) throws Exception {
        JSONObject payload = new JSONObject();
        payload.put("today", FinQueries.serverToday(c));
        payload.put("activity", FinQueries.activity(c, 30));
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

        LinearLayout modules = section("☰", "بخش‌های مالی", "دسترسی بر پایه نقش واقعی از جدول دسترسی");
        modules.addView(entry("💰", "فروش و وصول", "فروش، وصول، ترکیب نقد/چک/POS", "finance_sales",
                v -> host.open(new FinScreenSales(host))), ui.lp(-1, -2));
        modules.addView(entry("◫", "مرکز چک", "دریافتی، پرداختی، تقویم سررسید", "finance_checks",
                v -> host.open(new FinScreenCheques(host))), ui.lp(-1, -2));
        modules.addView(entry("⇄", "مطالبات و سنی‌بندی", "مانده مشتریان، Aging، پیگیری", "finance_receivables",
                v -> host.open(new FinScreenReceivables(host))), ui.lp(-1, -2));
        modules.addView(entry("▤", "بانک‌ها", "حساب‌ها، گردش، موجودی", "finance_banks",
                v -> host.open(new FinScreenBanks(host))), ui.lp(-1, -2));
        modules.addView(entry("≠", "مغایرت‌گیری بانکی", "Statement → Match → Resolve → Confirm", "finance_reconcile",
                v -> host.open(new FinScreenRecon(host))), ui.lp(-1, -2));
        modules.addView(entry("▣", "مرکز POS", "تراکنش‌ها، تسویه به بانک و اپراتور", "finance_pos",
                v -> host.open(new FinScreenPos(host))), ui.lp(-1, -2));
        modules.addView(entry("▩", "صندوق و تسویه", "دفتر صندوق، تحویل وجه، تفاوت‌ها", "finance_cash",
                v -> host.open(new FinScreenCash(host))), ui.lp(-1, -2));
        modules.addView(entry("☑", "گزارش و بستن روز", "گزارش روزانه و بستن روز کاری", "finance_daily_close",
                v -> host.open(new FinScreenDaily(host))), ui.lp(-1, -2));
        modules.addView(entry("⚠", "کارهای باز", "مغایرت قبض، POS و بانک بدون قبض", "finance_reports",
                v -> host.open(new FinScreenProblems(host))), ui.lp(-1, -2));
        body.addView(modules, ui.lp(-1, -2));

        account();
        viewState();
        themes();
        support();
        activity(p);
        about();
    }

    /**
     * Reading is always allowed for a signed-in operator — the data of every module is real and the
     * whole point of the app is to show it. What a role controls is <b>writing</b>: the row simply says
     * so instead of hiding the module.
     */
    private android.view.View entry(String glyph, String title, String sub, String permission, final android.view.View.OnClickListener tap) {
        boolean write = host.can(permission);
        return ui.listRow(glyph + "  " + title,
                sub + (write ? "" : " · این نقش فقط می‌تواند بخواند"),
                write ? "" : "فقط خواندن", write ? "" : "بدون دسترسی نوشتن",
                write ? ui.goldAccent : FinUi.INFO, tap);
    }

    /**
     * The signed-in account, exactly as the database described it at sign-in: where the row was found,
     * the real display name, the ids that Atiran rows point at, the role and its source. Nothing on
     * this card is derived from the typed username alone.
     */
    private void account() {
        LinearLayout card = section("☺", "حساب کاربری",
                "همهٔ این مقدارها هنگام ورود از دیتابیس آتیران خوانده شده‌اند");
        FinSession s = FinSession.get();
        if (s == null) {
            card.addView(ui.text("حسابی وارد نشده است.", 12f, ui.textDim, false), ui.lp(-1, -2));
            body.addView(card, ui.lp(-1, -2));
            return;
        }
        String source = "visitors".equals(s.source) ? "جدول اپراتورها (visitors)"
                : "sys_users".equals(s.source) ? "کاربران آتیران (sys_users)" : s.source;
        card.addView(row2("نام کاربری", FinFmt.faNumber(s.username)), ui.lp(-1, -2));
        card.addView(row2("نام نمایشی", FinFmt.faNumber(s.displayName)), ui.lp(-1, -2));
        card.addView(row2("جدول حساب", source), ui.lp(-1, -2));
        card.addView(row2("شناسه اپراتور (vis_rdf)", s.visitorId == null ? "—" : FinFmt.faNumber(s.visitorId)), ui.lp(-1, -2));
        card.addView(row2("شناسه کاربر آتیران", s.atiranUserId == null ? "—" : FinFmt.faNumber(s.atiranUserId)), ui.lp(-1, -2));
        card.addView(row2("نقش", s.roleLabel + " (" + s.roleKey + ")"), ui.lp(-1, -2));
        card.addView(row2("منبع نقش", s.roleFromDatabase ? "جدول‌های دسترسی دیتابیس"
                : (s.isManager() ? "نام کاربری مدیریتی (admin / modir)" : "قاعدهٔ هویت/پیش‌فرض")), ui.lp(-1, -2));
        card.addView(row2("سطح دسترسی", s.isManager() ? "کامل — همهٔ بخش‌ها و گزارش‌ها باز است" : "محدود به نقش"), ui.lp(-1, -2));
        card.addView(row2("تعداد دسترسی‌ها", FinFmt.faNumber(s.permissions.size())), ui.lp(-1, -2));
        card.addView(row2("زمان ورود", clock(s.loginAt)), ui.lp(-1, -2));
        card.addView(ui.text("پیش‌بارگذاری: پس از ورود، همهٔ بخش‌ها یک‌بار از دیتابیس خوانده و ذخیره می‌شوند "
                + "تا هر صفحه بلافاصله با داده واقعی باز شود.", 11f, ui.textFaint, false), ui.lp(-1, -2));
        body.addView(card, ui.lp(-1, -2));
    }

    /**
     * Live state of the operator desk: what the host is really showing right now, plus the last steps
     * of the run. If a screen ever comes up empty, this card is the fastest way to say why.
     */
    private void viewState() {
        LinearLayout card = section("◉", "وضعیت نمایش", "بررسی زندهٔ میزکار و آخرین مرحله‌های اجرا");
        card.addView(row2("تب فعال", host.hostTab()), ui.lp(-1, -2));
        card.addView(row2("صفحه‌های باز", FinFmt.faNumber(host.hostStackSize())), ui.lp(-1, -2));
        card.addView(row2("بلوک‌های صفحه", FinFmt.faNumber(box.getChildCount())), ui.lp(-1, -2));
        card.addView(row2("فرزندان میزبان", FinFmt.faNumber(host.hostContentChildren())), ui.lp(-1, -2));
        card.addView(row2("دکمه‌های نوار پایین", FinFmt.faNumber(host.hostNavChildren())), ui.lp(-1, -2));
        card.addView(row2("جدول‌های اختیاری", !FinDb.tablesProbed()
                ? "در حال تشخیص…" : (FinDb.optionalTablesAvailable()
                ? "موجود — ثبت‌های برنامه فعال است" : "ساخته نشده — فقط خواندن از جدول‌های آتیران")), ui.lp(-1, -2));
        android.widget.TextView trail = ui.text(FinCrash.lastSteps(host, 8), 10f, ui.textFaint, false);
        trail.setTypeface(android.graphics.Typeface.MONOSPACE);
        card.addView(trail, ui.lp(-1, -2));
        card.addView(ui.text("دکمهٔ بازگشت به‌تنهایی برنامه را نمی‌بندد؛ اول به خانه برمی‌گردد و برای خروج باید دو بار پشت سر هم زده شود.",
                11f, ui.textFaint, false), ui.lp(-1, -2));
        body.addView(card, ui.lp(-1, -2));
    }

    private android.view.View row2(String label, String value) {
        LinearLayout row = ui.row();
        row.setPadding(0, ui.dp(4), 0, ui.dp(4));
        row.addView(ui.text(label, 11.5f, ui.textDim, false), ui.lp(0, -2, 1f));
        row.addView(ui.text(value == null || value.isEmpty() ? "—" : value, 12f, ui.textColor, true), ui.lp(-2, -2));
        return row;
    }

    private String clock(long at) {
        try {
            if (at <= 0) return "—";
            String time = new java.text.SimpleDateFormat("HH:mm", java.util.Locale.US).format(new java.util.Date(at));
            String date = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(new java.util.Date(at));
            return FinFmt.faNumber(time) + " · " + FinFmt.faNumber(date);
        } catch (Throwable t) {
            return "—";
        }
    }

    /** Theme chooser with a live colour preview of every palette. */
    private void themes() {
        LinearLayout card = section("◐", "پوسته برنامه", "پوسته فعلی: " + host.ui().theme().labelFa);
        LinearLayout grid = ui.column();
        LinearLayout row = null;
        FinTheme[] all = FinTheme.all();
        for (int i = 0; i < all.length; i++) {
            if (i % 2 == 0) { row = ui.row(); grid.addView(row, ui.lp(-1, -2)); }
            FinTheme t = all[i];
            boolean active = t.key.equals(host.ui().theme().key);
            LinearLayout cell = ui.column();
            GradientDrawable bg = new GradientDrawable(GradientDrawable.Orientation.TL_BR, new int[]{t.surface, t.surface2});
            bg.setCornerRadius(ui.dp(14));
            bg.setStroke(Math.max(1, ui.dp(active ? 2 : 1)), active ? t.gold : ui.stroke);
            cell.setBackground(bg);
            cell.setPadding(ui.dp(10), ui.dp(10), ui.dp(10), ui.dp(10));
            cell.addView(ui.text((active ? "✓  " : "") + t.labelFa, 12.5f, t.text, true), ui.lp(-1, -2));
            LinearLayout swatches = ui.row();
            swatches.setPadding(0, ui.dp(6), 0, 0);
            int[] dots = {t.gold, t.surface2, t.textDim, FinUi.SUCCESS, FinUi.WARNING, FinUi.DANGER};
            for (int d = 0; d < dots.length; d++) {
                View dot = new View(host);
                dot.setBackground(ui.rounded(dots[d], 999, 0, 0));
                LinearLayout.LayoutParams dlp = ui.lp(ui.dp(16), ui.dp(16));
                dlp.leftMargin = ui.dp(d == 0 ? 0 : 4);
                swatches.addView(dot, dlp);
            }
            cell.addView(swatches, ui.lp(-1, -2));
            cell.setOnClickListener(v -> {
                if (t.key.equals(host.ui().theme().key)) return;
                FinTheme.set(host, t.key);
                host.toast("پوسته «" + t.labelFa + "» فعال شد.");
                host.reload();
            });
            ui.applyTouch(cell);
            LinearLayout.LayoutParams lp = ui.lp(0, -2, 1f);
            lp.leftMargin = ui.dp(i % 2 == 0 ? 0 : 5);
            if (row != null) row.addView(cell, lp);
            LinearLayout.LayoutParams gap = ui.lp(-1, ui.dp(5));
            grid.addView(new View(host), gap);
        }
        card.addView(grid, ui.lp(-1, -2));
        card.addView(ui.text("رنگ‌های معنایی (موفق/هشدار/خطا/مدیریت) در همه پوسته‌ها یکسان می‌مانند؛ فقط سطوح و رنگ تأکیدی تغییر می‌کند.",
                11f, ui.textFaint, false), ui.lp(-1, -2));
        body.addView(card, top(12));
    }

    /**
     * «تشخیص و پشتیبانی» — the three actions an operator needs when something does not work on the
     * phone: the connection doctor (network → port → SQL login → server date), the full technical
     * self test against the real database, and the startup/crash log of this installation.
     */
    private void support() {
        LinearLayout card = section("⚕", "تشخیص و پشتیبانی",
                "اگر داده‌ای نمی‌آید یا ورود ناموفق است، از این سه ابزار استفاده کنید");
        card.addView(entry("⇅", "بررسی اتصال به سرور",
                "اینترنت، نام سرور، پورت، ورود به SQL و تاریخ سرور", "",
                v -> host.showDiagnostics()), ui.lp(-1, -2));
        card.addView(entry("⚙", "آزمون فنی کامل",
                "خواندن واقعی همه جدول‌های مالی روی سرور آتیران", "",
                v -> host.runSelfTest()), ui.lp(-1, -2));
        card.addView(entry("▤", "گزارش راه‌اندازی و خطاها",
                "مرحله‌های شروع برنامه و آخرین خطای ثبت‌شده", "",
                v -> host.showSupportLog()), ui.lp(-1, -2));

        final boolean software = FinCharts.Base.softwareRendering(host);
        card.addView(entry("◫", software ? "نمودارها: حالت سازگاری فعال" : "نمودارها: شتاب سخت‌افزاری",
                software ? "برای این گوشی نمودارها نرم‌افزاری رسم می‌شوند (پایدارتر)" : "برای مشکل‌های نمایشی، حالت سازگاری را روشن کنید",
                "", v -> {
                    try {
                        // getSharedPreferences lives on Context; the activity is one, so call it as such.
                        android.content.Context ctx = host;
                        ctx.getSharedPreferences("atiran_finance", android.content.Context.MODE_PRIVATE)
                                .edit().putBoolean(FinCharts.Base.PREF_SOFTWARE, !software).apply();
                        host.toast(software ? "حالت سازگاری خاموش شد." : "حالت سازگاری نمودارها روشن شد.");
                        host.reload();
                    } catch (Throwable t) {
                        host.toast("تغییر حالت ممکن نشد · کد رویداد " + FinCrash.eventCode(t));
                    }
                }), ui.lp(-1, -2));
        if (FinCharts.Base.anyFailure(host)) {
            card.addView(ui.text("⚠  در این نصب حداقل یک نمودار رسم نشده است؛ گزارش «راه‌اندازی و خطاها» جزئیات را دارد.",
                    11f, FinUi.WARNING, false), ui.lp(-1, -2));
        }
        body.addView(card, top(12));
    }

    private void activity(JSONObject p) {
        JSONArray rows = arr(p, "activity");
        LinearLayout card = section("↻", "فعالیت‌ها", rows.length() == 0 ? "بدون رکورد" : "آخرین رویدادهای ثبت‌شده");
        if (rows.length() == 0) {
            card.addView(stateText("فعالیتی ثبت نشده است.", ui.textDim));
        } else {
            for (int x = 0; x < rows.length() && x < 15; x++) {
                JSONObject r = rows.optJSONObject(x);
                if (r == null) continue;
                card.addView(ui.listRow(s(r, "module", "—") + " · " + s(r, "action", ""),
                        s(r, "at", "") + " · " + s(r, "username", "") + " · " + s(r, "reference", ""),
                        d(r, "amount") == 0 ? "" : money(d(r, "amount")), "", ui.silver, null), ui.lp(-1, -2));
            }
        }
        body.addView(card, top(12));
    }

    private void about() {
        LinearLayout card = section("ℹ", "درباره «آتیران مالی»", FinSession.appVersion() + " — بسته ir.meelano.atiran.finance");
        card.addView(kv("کاربر جاری", FinSession.displayOrUser()), ui.lp(-1, -2));
        card.addView(kv("نقش", FinSession.roleLabelFor(FinSession.roleKey()) + (host.session() != null && host.session().roleFromDatabase ? " (از جدول دسترسی)" : " (پیش‌فرض)")), ui.lp(-1, -2));
        card.addView(kv("منبع نقش", host.session() == null ? "—" : host.session().source), ui.lp(-1, -2));
        card.addView(kv("اتصال", FinEnv.describe()), ui.lp(-1, -2));
        card.addView(kv("تاریخ سرور", host.clockLabel()), ui.lp(-1, -2));
        card.addView(kv("دستگاه", FinSession.deviceLabel()), ui.lp(-1, -2));
        card.addView(ui.text("این نسخه هیچ جدولی نمی‌سازد و هیچ مقدار مالی در جدول‌های اصلی آتیران را تغییر نمی‌دهد؛ همهٔ اطلاعات هر بخش از همان جدول‌های موجود دیتابیس آتیران خوانده می‌شود. اگر جدول‌های اختیاری «آتیران مالی» از قبل وجود داشته باشند، ثبت‌های خود برنامه (پیگیری، تسویه، بستن روز، مغایرت) در همان‌ها نگه‌داری می‌شود.",
                11.5f, ui.textDim, false), ui.lp(-1, -2));

        LinearLayout tools = ui.row();
        tools.addView(ui.button("خروج از حساب", FinUi.DANGER, false, v -> {
            FinSession.clear();
            host.toast("از حساب خارج شدید.");
            Intent i = new Intent(host, AtiranFinanceActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            host.startActivity(i);
            host.finish();
        }), ui.lp(0, -2, 1f));
        LinearLayout.LayoutParams tp = ui.lp(-1, -2);
        tp.topMargin = ui.dp(6);
        card.addView(tools, tp);
        body.addView(card, top(12));
    }
}

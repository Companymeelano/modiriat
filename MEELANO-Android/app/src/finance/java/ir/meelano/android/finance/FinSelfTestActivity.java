package ir.meelano.android.finance;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONObject;

import java.sql.Connection;

/**
 * Technical self test (not part of the operator flow).
 *
 * It proves on the device, against the real database, that
 *   • the server is reachable and returns its own Jalali date,
 *   • the finance tables this app reads really exist and answer,
 *   • the documented agreements hold (CUSTOMERS.man vs the cust_act ledger, cheque due windows,
 *     POS → receipt linkage).
 *
 * It is the screen used when a new installation has to be verified; it is declared with
 * {@code exported=false} in the finance manifest and never shows up in the launcher.
 */
public class FinSelfTestActivity extends Activity {

    private LinearLayout log;
    private FinDb db;
    private FinUi ui;
    private final Handler main = new Handler(Looper.getMainLooper());

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        ui = new FinUi(this);
        db = new FinDb(this);
        ui.applySystemBars();
        ScrollView sc = ui.scroll();
        log = ui.column();
        sc.addView(log);
        setContentView(sc);
        ui.pad(log);
        log.addView(ui.text("آزمون فنی آتیران مالی", 18f, ui.goldAccent, true), ui.lp(-1, -2));
        log.addView(ui.text(FinEnv.describe() + " · نسخه " + FinSession.appVersion(), 11.5f, ui.textDim, false), ui.lp(-1, -2));
        run();
    }

    private void add(String label, String value, boolean ok) {
        LinearLayout card = ui.cardTone(ok ? FinUi.SUCCESS : FinUi.DANGER);
        card.addView(ui.text((ok ? "✓  " : "▲  ") + label, 12.5f, ui.textColor, true), ui.lp(-1, -2));
        TextView v = ui.text(value, 11.5f, ui.textDim, false);
        card.addView(v, ui.lp(-1, -2));
        LinearLayout.LayoutParams p = ui.lp(-1, -2);
        p.topMargin = ui.dp(8);
        log.addView(card, p);
    }

    private void run() {
        db.pool().execute(() -> {
            long start = System.currentTimeMillis();
            try (Connection c = db.open()) {
                db.ensureSchema(c);
                long connectMs = System.currentTimeMillis() - start;
                main.post(() -> add("اتصال به SQL Server", "زمان اتصال " + connectMs + " میلی‌ثانیه · " + FinEnv.describe(), true));

                String today = FinQueries.serverToday(c);
                main.post(() -> add("تاریخ سرور (ReturnDateServer)", today, today.matches("\\d{4}/\\d{2}/\\d{2}")));

                check(c, "فاکتورهای فروش (sailfact)", "SELECT COUNT(*) FROM dbo.sailfact WITH (NOLOCK) WHERE active='t'", true);
                check(c, "قبض‌های دریافت (dar)", "SELECT COUNT(*) FROM dbo.dar WITH (NOLOCK) WHERE p=0", true);
                check(c, "چک‌های دریافتی (getchk)", "SELECT COUNT(*) FROM dbo.getchk WITH (NOLOCK)", true);
                check(c, "چک‌های پرداختی (putchk)", "SELECT COUNT(*) FROM dbo.putchk WITH (NOLOCK)", true);
                check(c, "تراکنش‌های POS (PosDetails)", "SELECT COUNT(*) FROM dbo.PosDetails WITH (NOLOCK)", true);
                check(c, "بانک‌ها (BANK)", "SELECT COUNT(*) FROM dbo.BANK WITH (NOLOCK) WHERE ISNULL(Active,1)=1", true);
                check(c, "صندوق (COW)", "SELECT COUNT(*) FROM dbo.COW WITH (NOLOCK) WHERE ISNULL(isActive,1)=1", true);
                check(c, "مشتریان (CUSTOMERS)", "SELECT COUNT(*) FROM dbo.CUSTOMERS WITH (NOLOCK)", true);
                check(c, "گردش بانکی (ban_act)", "SELECT COUNT(*) FROM dbo.ban_act WITH (NOLOCK)", true);
                check(c, "اپراتورها (visitors)", "SELECT COUNT(*) FROM dbo.visitors WITH (NOLOCK)", true);

                // agreement 1: CUSTOMERS.man equals the cust_act ledger for the customers that have rows
                String agreeSql = "SELECT COUNT(*) AS compared, SUM(CASE WHEN ABS(ISNULL(m.suma,0) - ISNULL(c.man,0)) < 1 THEN 1 ELSE 0 END) AS agreeing "
                        + "FROM dbo.CUSTOMERS c WITH (NOLOCK) "
                        + "CROSS APPLY (SELECT SUM(ISNULL(a.act_bed,0) - ISNULL(a.act_bes,0)) AS suma FROM dbo.cust_act a WITH (NOLOCK) WHERE a.shmo=c.SHMO) m "
                        + "WHERE m.suma IS NOT NULL";
                int compared = 0, agreeing = 0;
                try (java.sql.PreparedStatement ps = c.prepareStatement(agreeSql);
                     java.sql.ResultSet r = ps.executeQuery()) {
                    if (r.next()) {
                        compared = r.getInt("compared");
                        agreeing = r.getInt("agreeing");
                    }
                }
                final int cmp = compared, agr = agreeing;
                main.post(() -> add("تطبیق مانده مشتری با دفتر گردش (cust_act)",
                        agr + " از " + cmp + " مشتری مطابق", cmp > 0 && agr == cmp));

                // agreement 2: cheque due windows are comparable as char(10) Jalali text
                int all = FinDb.count(c, "SELECT COUNT(*) AS v FROM dbo.getchk WITH (NOLOCK)");
                int overdue = FinDb.count(c, "SELECT COUNT(*) AS v FROM dbo.getchk WITH (NOLOCK) WHERE sardate<?", today);
                int future = FinDb.count(c, "SELECT COUNT(*) AS v FROM dbo.getchk WITH (NOLOCK) WHERE sardate>?", today);
                final int a2 = all, o2 = overdue, f2 = future;
                main.post(() -> add("پنجره سررسید چک‌ها", "کل " + a2 + " · معوق " + o2 + " · آینده " + f2, a2 == o2 + f2));

                // agreement 3: POS rows are linked to receipts through ghno
                int posAll = FinDb.count(c, "SELECT COUNT(*) AS v FROM dbo.PosDetails WITH (NOLOCK)");
                int posLinked = FinDb.count(c, "SELECT COUNT(*) AS v FROM dbo.PosDetails pd WITH (NOLOCK) "
                        + "WHERE EXISTS (SELECT 1 FROM dbo.dar d WITH (NOLOCK) WHERE d.ghno=pd.ghno AND d.p=0)");
                final int pAll = posAll, pLinked = posLinked;
                main.post(() -> add("اتصال POS به قبض", pLinked + " از " + pAll + " تراکنش متصل", pAll == pLinked));

                // the app's own tables
                int recon = FinDb.count(c, "SELECT COUNT(*) AS v FROM dbo.meelano_fin_recon WITH (NOLOCK)");
                int settle = FinDb.count(c, "SELECT COUNT(*) AS v FROM dbo.meelano_fin_settlement WITH (NOLOCK)");
                int follow = FinDb.count(c, "SELECT COUNT(*) AS v FROM dbo.meelano_fin_followup WITH (NOLOCK)");
                int audit = FinDb.count(c, "SELECT COUNT(*) AS v FROM dbo.meelano_fin_audit WITH (NOLOCK)");
                final int rc = recon, st = settle, fu = follow, au = audit;
                main.post(() -> add("جدول‌های اختصاصی اپ", "مغایرت " + rc + " · تسویه " + st + " · پیگیری " + fu + " · Audit " + au, true));

                JSONObject home = FinQueries.home(c, today);
                final String summary = "فروش امروز " + FinFmt.amount(home.optDouble("salesToday"))
                        + " · وصول امروز " + FinFmt.amount(home.optDouble("receivedToday"))
                        + " · مطالبات " + FinFmt.amount(home.optDouble("receivableTotal"))
                        + " · بانک‌ها " + FinFmt.amount(home.optDouble("bankTotal"));
                main.post(() -> add("شاخص‌های خانه از داده واقعی", summary, true));
                main.post(() -> add("پایان آزمون", "همه بررسی‌ها روی دیتابیس واقعی اجرا شد.", true));
            } catch (Exception e) {
                String code = FinFmt.eventCode(FinDb.safeMessage(e));
                main.post(() -> add("خطا در آزمون", "اجرای آزمون کامل نشد. کد رویداد: " + code, false));
            }
        });
    }

    private void check(Connection c, String label, String sql, boolean expectRows) {
        try {
            long started = System.currentTimeMillis();
            int n = FinDb.count(c, sql);
            long ms = System.currentTimeMillis() - started;
            main.post(() -> add(label, n + " ردیف · " + ms + " میلی‌ثانیه", n > 0 || !expectRows));
        } catch (Exception e) {
            main.post(() -> add(label, "پاسخ نگرفت — کد رویداد: " + FinFmt.eventCode(FinDb.safeMessage(e)), false));
        }
    }
}

package ir.meelano.android.finance;

import ir.meelano.android.MeelanoJalali;

import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import static ir.meelano.android.finance.FinDb.count;
import static ir.meelano.android.finance.FinDb.select;
import static ir.meelano.android.finance.FinDb.sum;
import static ir.meelano.android.finance.FinDb.text;
import static ir.meelano.android.finance.FinDb.value;

/**
 * Every query of «آتیران مالی» in one place, together with the documented source of each number.
 * This class is the code counterpart of {@code docs/finance/DataSourceRegistry.md}.
 *
 * Rules verified against the real database (read-only probes on 1405/07/12) are marked ✔; anything
 * that could not be verified stays UNKNOWN and never becomes a screen value.
 *
 * Dates: Atiran stores Jalali dates as fixed {@code char(10)} text ("1405/07/12"), so range
 * comparisons work directly on the server and "today" always comes from
 * {@code dbo.ReturnDateServer()} — the device clock never decides a due date.
 *
 * Money: the money columns hold rial values (sailfact.[all], dar.mab, getchk.getchkmab, BANK.MAN,
 * ban_act.act_bes/act_bed, COW.BES/BED, PosDetails.MabPos).
 *
 * All ids and dates are passed as JDBC parameters; the only string concatenation is for integer
 * paging values produced by {@link #clamp(int, int, int)}.
 */
public final class FinQueries {
    private FinQueries() { }

    /** Real cust_act kinds, sampled from Atiran's own rows. */
    public static String ledgerKindLabel(int actId) {
        switch (actId) {
            case 0: return "مانده اول دوره";
            case 1: return "قبض دریافت";
            case 2: return "قبض پرداخت";
            case 3: return "چک";
            case 9: return "سند حسابداری";
            case 20: return "فاکتور فروش";
            case 44: return "تخفیف/تعدیل";
            case 55: return "صورتحساب/تسویه";
            default: return "سند " + actId;
        }
    }

    /** Real ban_act kinds, sampled: 8 حواله، 78 انتقال از حساب، 80 کارت به کارت، 50 از صندوق. */
    public static String bankKindLabel(int actId) {
        switch (actId) {
            case 8: return "حواله/واریز";
            case 9: return "حواله به شماره";
            case 50: return "از صندوق (فیش)";
            case 75: return "چک";
            case 76: return "موجودی قبلی";
            case 77: return "ابطال";
            case 78: return "انتقال از حساب جاری";
            case 80: return "کارت به کارت";
            default: return "گردش " + actId;
        }
    }

    /** Real COW kinds: 1 قبض دریافت، 2 قبض پرداخت، 50 واریز به بانک، 62 ابطال، 71 موجودی اولیه. */
    public static String cashKindLabel(int actId) {
        switch (actId) {
            case 1: return "قبض دریافت";
            case 2: return "قبض پرداخت";
            case 50: return "واریز به بانک";
            case 62: return "ابطال";
            case 71: return "موجودی اولیه";
            default: return "گردش " + actId;
        }
    }

    // ================================================================ clock

    /** ✔ the database's own Jalali "today"; the device date is only a fallback when offline. */
    public static String serverToday(Connection c) {
        try {
            String v = text(c, "SELECT CONVERT(char(10), dbo.ReturnDateServer()) AS v");
            return v == null || v.isEmpty() ? FinFmt.todayLocal() : v;
        } catch (Exception e) {
            return FinFmt.todayLocal();
        }
    }

    // ================================================================ home

    /**
     * Control figures of the home screen. Every value is a real aggregate:
     *   sales      SUM(sailfact.[all])                 active='t'            ✔ 970 invoices
     *   receipts   SUM(dar.mab)                        p=0, Active=1         ✔ 812 receipts
     *   pos        SUM(PosDetails.MabPos)              via dar.ghno          ✔ 1083 rows
     *   cash       SUM(dar.naghd)                                            ✔ 27 receipts
     *   cheques    getchk.getchkmab / putchk.putchkmab                       ✔ 118 / 202 cheques
     *   balance    SUM(CUSTOMERS.man)                  >0 / <0               ✔ equals ledger sum 2724/2724
     *   banks      SUM(BANK.MAN)                       Active=1              ✔ 8 banks
     *   cashbox    SUM(COW.BED-COW.BES)                isActive=1            ✔ 77 rows
     */
    public static JSONObject home(Connection c, String today) throws Exception {
        String monthStart = MeelanoJalali.monthStart(today);
        JSONObject o = new JSONObject();
        o.put("today", today);

        o.put("salesToday", sum(c, "SELECT ISNULL(SUM([all]),0) AS v FROM dbo.sailfact WITH (NOLOCK) WHERE active='t' AND [date]=?", today));
        o.put("salesCountToday", count(c, "SELECT COUNT(*) AS v FROM dbo.sailfact WITH (NOLOCK) WHERE active='t' AND [date]=?", today));
        o.put("salesMonth", sum(c, "SELECT ISNULL(SUM([all]),0) AS v FROM dbo.sailfact WITH (NOLOCK) WHERE active='t' AND [date]>=? AND [date]<=?", monthStart, today));
        o.put("salesMonthCount", count(c, "SELECT COUNT(*) AS v FROM dbo.sailfact WITH (NOLOCK) WHERE active='t' AND [date]>=? AND [date]<=?", monthStart, today));

        o.put("receivedToday", sum(c, "SELECT ISNULL(SUM(mab),0) AS v FROM dbo.dar WITH (NOLOCK) WHERE p=0 AND ISNULL(Active,1)=1 AND [date]=?", today));
        o.put("receiptsToday", count(c, "SELECT COUNT(*) AS v FROM dbo.dar WITH (NOLOCK) WHERE p=0 AND ISNULL(Active,1)=1 AND [date]=?", today));
        o.put("cashToday", sum(c, "SELECT ISNULL(SUM(naghd),0) AS v FROM dbo.dar WITH (NOLOCK) WHERE p=0 AND ISNULL(Active,1)=1 AND [date]=?", today));
        o.put("checkToday", sum(c, "SELECT ISNULL(SUM(mabcheck),0) AS v FROM dbo.dar WITH (NOLOCK) WHERE p=0 AND ISNULL(Active,1)=1 AND [date]=?", today));
        o.put("posToday", sum(c, "SELECT ISNULL(SUM(pd.MabPos),0) AS v FROM dbo.PosDetails pd WITH (NOLOCK) "
                + "INNER JOIN dbo.dar d WITH (NOLOCK) ON d.ghno=pd.ghno AND d.p=0 WHERE d.[date]=?", today));
        o.put("posCountToday", count(c, "SELECT COUNT(*) AS v FROM dbo.PosDetails pd WITH (NOLOCK) "
                + "INNER JOIN dbo.dar d WITH (NOLOCK) ON d.ghno=pd.ghno AND d.p=0 WHERE d.[date]=?", today));

        o.put("receivedMonth", sum(c, "SELECT ISNULL(SUM(mab),0) AS v FROM dbo.dar WITH (NOLOCK) WHERE p=0 AND ISNULL(Active,1)=1 AND [date]>=? AND [date]<=?", monthStart, today));
        o.put("receiptsMonth", count(c, "SELECT COUNT(*) AS v FROM dbo.dar WITH (NOLOCK) WHERE p=0 AND ISNULL(Active,1)=1 AND [date]>=? AND [date]<=?", monthStart, today));
        o.put("cashMonth", sum(c, "SELECT ISNULL(SUM(naghd),0) AS v FROM dbo.dar WITH (NOLOCK) WHERE p=0 AND ISNULL(Active,1)=1 AND [date]>=? AND [date]<=?", monthStart, today));
        o.put("checkMonth", sum(c, "SELECT ISNULL(SUM(mabcheck),0) AS v FROM dbo.dar WITH (NOLOCK) WHERE p=0 AND ISNULL(Active,1)=1 AND [date]>=? AND [date]<=?", monthStart, today));
        o.put("posMonth", sum(c, "SELECT ISNULL(SUM(pd.MabPos),0) AS v FROM dbo.PosDetails pd WITH (NOLOCK) "
                + "INNER JOIN dbo.dar d WITH (NOLOCK) ON d.ghno=pd.ghno AND d.p=0 WHERE d.[date]>=? AND d.[date]<=?", monthStart, today));

        o.put("receivableTotal", sum(c, "SELECT ISNULL(SUM(man),0) AS v FROM dbo.CUSTOMERS WITH (NOLOCK) WHERE ISNULL(man,0)>0"));
        o.put("receivableCustomers", count(c, "SELECT COUNT(*) AS v FROM dbo.CUSTOMERS WITH (NOLOCK) WHERE ISNULL(man,0)>0"));
        o.put("creditTotal", sum(c, "SELECT ISNULL(SUM(man),0) AS v FROM dbo.CUSTOMERS WITH (NOLOCK) WHERE ISNULL(man,0)<0"));
        o.put("creditCustomers", count(c, "SELECT COUNT(*) AS v FROM dbo.CUSTOMERS WITH (NOLOCK) WHERE ISNULL(man,0)<0"));
        o.put("openInvoices", count(c, "SELECT COUNT(*) AS v FROM dbo.sailfact WITH (NOLOCK) WHERE active='t' AND ISNULL(bamandeh,0)<>0"));
        o.put("openInvoiceAmount", sum(c, "SELECT ISNULL(SUM([all]-ISNULL(MabDaryaftFactor,0)),0) AS v FROM dbo.sailfact WITH (NOLOCK) WHERE active='t' AND ISNULL(bamandeh,0)<>0"));

        o.put("checksTotal", count(c, "SELECT COUNT(*) AS v FROM dbo.getchk WITH (NOLOCK)"));
        o.put("checksDueToday", count(c, "SELECT COUNT(*) AS v FROM dbo.getchk WITH (NOLOCK) WHERE sardate=?", today));
        o.put("checksOverdue", count(c, "SELECT COUNT(*) AS v FROM dbo.getchk WITH (NOLOCK) WHERE sardate<?", today));
        o.put("checksOverdueAmount", sum(c, "SELECT ISNULL(SUM(getchkmab),0) AS v FROM dbo.getchk WITH (NOLOCK) WHERE sardate<?", today));
        o.put("checksNext7", count(c, "SELECT COUNT(*) AS v FROM dbo.getchk WITH (NOLOCK) WHERE sardate>? AND sardate<=?", today, FinFmt.addDays(today, 7)));
        o.put("checksTreasury", count(c, "SELECT COUNT(*) AS v FROM dbo.getchk WITH (NOLOCK) WHERE chk_satus IN (8,9,10)"));
        o.put("checksWithdrawn", count(c, "SELECT COUNT(*) AS v FROM dbo.getchk WITH (NOLOCK) WHERE chk_satus IN (4,5,6,7)"));
        o.put("paidChecks", count(c, "SELECT COUNT(*) AS v FROM dbo.putchk WITH (NOLOCK)"));
        o.put("paidChecksOpen", count(c, "SELECT COUNT(*) AS v FROM dbo.putchk WITH (NOLOCK) WHERE putchk_status IN (1,7)"));
        o.put("paidChecksOverdue", count(c, "SELECT COUNT(*) AS v FROM dbo.putchk WITH (NOLOCK) WHERE putchk_status IN (1,7) AND sardate<?", today));
        o.put("paidChecksOpenAmount", sum(c, "SELECT ISNULL(SUM(putchkmab),0) AS v FROM dbo.putchk WITH (NOLOCK) WHERE putchk_status IN (1,7)"));

        o.put("bankTotal", sum(c, "SELECT ISNULL(SUM(MAN),0) AS v FROM dbo.BANK WITH (NOLOCK) WHERE ISNULL(Active,1)=1"));
        o.put("bankCount", count(c, "SELECT COUNT(*) AS v FROM dbo.BANK WITH (NOLOCK) WHERE ISNULL(Active,1)=1"));
        o.put("cashBoxBalance", sum(c, "SELECT ISNULL(SUM(ISNULL(BED,0)-ISNULL(BES,0)),0) AS v FROM dbo.COW WITH (NOLOCK) WHERE ISNULL(isActive,1)=1"));
        o.put("cashBoxIn", sum(c, "SELECT ISNULL(SUM(BED),0) AS v FROM dbo.COW WITH (NOLOCK) WHERE ISNULL(isActive,1)=1 AND DATE=?", today));
        o.put("cashBoxOut", sum(c, "SELECT ISNULL(SUM(BES),0) AS v FROM dbo.COW WITH (NOLOCK) WHERE ISNULL(isActive,1)=1 AND DATE=?", today));
        o.put("bankMovementToday", count(c, "SELECT COUNT(*) AS v FROM dbo.ban_act WITH (NOLOCK) WHERE act_date=?", today));
        o.put("customersTotal", count(c, "SELECT COUNT(*) AS v FROM dbo.CUSTOMERS WITH (NOLOCK)"));
        o.put("operatorsTotal", count(c, "SELECT COUNT(*) AS v FROM dbo.visitors WITH (NOLOCK) WHERE Username IS NOT NULL AND LTRIM(RTRIM(Username))<>''"));
        o.put("mismatchMonth", mismatchCount(c, monthStart, today));
        o.put("posWithoutReceipt", count(c, "SELECT COUNT(*) AS v FROM dbo.PosDetails pd WITH (NOLOCK) "
                + "WHERE NOT EXISTS (SELECT 1 FROM dbo.dar d WITH (NOLOCK) WHERE d.ghno=pd.ghno AND d.p=0)"));

        JSONArray labels = new JSONArray(), salesSeries = new JSONArray(), receiptSeries = new JSONArray();
        for (int i = 13; i >= 0; i--) {
            String day = FinFmt.addDays(today, -i);
            labels.put(FinFmt.shortDate(day));
            salesSeries.put(sum(c, "SELECT ISNULL(SUM([all]),0) AS v FROM dbo.sailfact WITH (NOLOCK) WHERE active='t' AND [date]=?", day));
            receiptSeries.put(sum(c, "SELECT ISNULL(SUM(mab),0) AS v FROM dbo.dar WITH (NOLOCK) WHERE p=0 AND ISNULL(Active,1)=1 AND [date]=?", day));
        }
        o.put("labels", labels);
        o.put("salesSeries", salesSeries);
        o.put("receiptSeries", receiptSeries);
        return o;
    }

    /** Real alerts — every line is backed by a query against live data, none is decorative. */
    public static JSONArray alerts(Connection c, String today) throws Exception {
        JSONArray out = new JSONArray();
        int dueToday = count(c, "SELECT COUNT(*) AS v FROM dbo.getchk WITH (NOLOCK) WHERE sardate=?", today);
        if (dueToday > 0) out.put(alert("checks_due_today", "چکهای سررسید امروز", dueToday + " چک امروز سررسید میشود.", FinUi.WARNING));
        int overdue = count(c, "SELECT COUNT(*) AS v FROM dbo.getchk WITH (NOLOCK) WHERE sardate<?", today);
        if (overdue > 0) out.put(alert("checks_overdue", "چکهای سررسید گذشته", overdue + " چک از موعد گذشته است.", FinUi.DANGER));
        int next7 = count(c, "SELECT COUNT(*) AS v FROM dbo.getchk WITH (NOLOCK) WHERE sardate>? AND sardate<=?", today, FinFmt.addDays(today, 7));
        if (next7 > 0) out.put(alert("checks_next7", "سررسید ۷ روز آینده", next7 + " چک در هفته آینده سررسید میشود.", FinUi.INFO));
        int paidOverdue = count(c, "SELECT COUNT(*) AS v FROM dbo.putchk WITH (NOLOCK) WHERE putchk_status IN (1,7) AND sardate<?", today);
        if (paidOverdue > 0) out.put(alert("paid_overdue", "چک پرداختی معوق", paidOverdue + " چک پرداختی از موعد گذشته است.", FinUi.DANGER));
        int mismatched = mismatchCount(c, MeelanoJalali.monthStart(today), today);
        if (mismatched > 0) out.put(alert("receipt_mismatch", "مغایرت اجزای دریافت", mismatched + " قبض دریافت با اجزای پرداخت نمیخواند.", FinUi.DANGER));
        int unlinkedPos = count(c, "SELECT COUNT(*) AS v FROM dbo.PosDetails pd WITH (NOLOCK) "
                + "WHERE NOT EXISTS (SELECT 1 FROM dbo.dar d WITH (NOLOCK) WHERE d.ghno=pd.ghno AND d.p=0)");
        if (unlinkedPos > 0) out.put(alert("pos_unlinked", "POS بیدریافت", unlinkedPos + " تراکنش POS به قبض دریافت متصل نیست.", FinUi.WARNING));
        int negative = count(c, "SELECT COUNT(*) AS v FROM dbo.CUSTOMERS WITH (NOLOCK) WHERE ISNULL(man,0)<0");
        if (negative > 0) out.put(alert("negative_balance", "مانده بستانکار مشتری", negative + " مشتری مانده منفی (بستانکار) دارند.", FinUi.INFO));
        int openRecon = count(c, "SELECT COUNT(*) AS v FROM dbo.meelano_fin_recon WITH (NOLOCK) WHERE status IN (N'open',N'review')");
        if (openRecon > 0) out.put(alert("recon_open", "مغایرت بانکی باز", openRecon + " پرونده مغایرت در انتظار بررسی است.", FinUi.WARNING));
        return out;
    }

    private static JSONObject alert(String key, String title, String body, int color) throws Exception {
        JSONObject o = new JSONObject();
        o.put("key", key);
        o.put("title", title);
        o.put("body", body);
        o.put("color", color);
        return o;
    }

    // ================================================================ sales vs collection

    /**
     * Sale against receipt for a period.
     *
     * Sale        = SUM(sailfact.[all])                       active='t'        ✔ 970 rows
     * POS         = SUM(PosDetails.MabPos) via ghno → dar     p=0               ✔ 1083 rows
     * Cash        = SUM(dar.naghd)                                              ✔ 27 receipts
     * Cheque      = SUM(dar.mabcheck)                                           ✔ 53 receipts
     * Uncollected = SUM(sailfact.[all] − MabDaryaftFactor)    bamandeh <> 0     ✔ 967 invoices
     *
     * The receipts whose components do not add up to dar.mab are listed by
     * {@link #receiptMismatches} as reconciliation cases — never silently absorbed.
     */
    public static JSONObject salesVsCollection(Connection c, String from, String to) throws Exception {
        JSONObject o = new JSONObject();
        o.put("from", from);
        o.put("to", to);
        o.put("sales", sum(c, "SELECT ISNULL(SUM([all]),0) AS v FROM dbo.sailfact WITH (NOLOCK) WHERE active='t' AND [date]>=? AND [date]<=?", from, to));
        o.put("salesCount", count(c, "SELECT COUNT(*) AS v FROM dbo.sailfact WITH (NOLOCK) WHERE active='t' AND [date]>=? AND [date]<=?", from, to));
        o.put("pos", sum(c, "SELECT ISNULL(SUM(pd.MabPos),0) AS v FROM dbo.PosDetails pd WITH (NOLOCK) "
                + "INNER JOIN dbo.dar d WITH (NOLOCK) ON d.ghno=pd.ghno AND d.p=0 WHERE d.[date]>=? AND d.[date]<=?", from, to));
        o.put("posCount", count(c, "SELECT COUNT(*) AS v FROM dbo.PosDetails pd WITH (NOLOCK) "
                + "INNER JOIN dbo.dar d WITH (NOLOCK) ON d.ghno=pd.ghno AND d.p=0 WHERE d.[date]>=? AND d.[date]<=?", from, to));
        o.put("cash", sum(c, "SELECT ISNULL(SUM(naghd),0) AS v FROM dbo.dar WITH (NOLOCK) WHERE p=0 AND ISNULL(Active,1)=1 AND [date]>=? AND [date]<=?", from, to));
        o.put("cashCount", count(c, "SELECT COUNT(*) AS v FROM dbo.dar WITH (NOLOCK) WHERE p=0 AND ISNULL(Active,1)=1 AND ISNULL(naghd,0)<>0 AND [date]>=? AND [date]<=?", from, to));
        o.put("check", sum(c, "SELECT ISNULL(SUM(mabcheck),0) AS v FROM dbo.dar WITH (NOLOCK) WHERE p=0 AND ISNULL(Active,1)=1 AND [date]>=? AND [date]<=?", from, to));
        o.put("checkFiles", sum(c, "SELECT ISNULL(SUM(ted_chk),0) AS v FROM dbo.dar WITH (NOLOCK) WHERE p=0 AND ISNULL(Active,1)=1 AND [date]>=? AND [date]<=?", from, to));
        o.put("receiptsTotal", sum(c, "SELECT ISNULL(SUM(mab),0) AS v FROM dbo.dar WITH (NOLOCK) WHERE p=0 AND ISNULL(Active,1)=1 AND [date]>=? AND [date]<=?", from, to));
        o.put("receiptsCount", count(c, "SELECT COUNT(*) AS v FROM dbo.dar WITH (NOLOCK) WHERE p=0 AND ISNULL(Active,1)=1 AND [date]>=? AND [date]<=?", from, to));
        o.put("uncollected", sum(c, "SELECT ISNULL(SUM([all]-ISNULL(MabDaryaftFactor,0)),0) AS v FROM dbo.sailfact WITH (NOLOCK) "
                + "WHERE active='t' AND ISNULL(bamandeh,0)<>0 AND [date]>=? AND [date]<=?", from, to));
        o.put("uncollectedCount", count(c, "SELECT COUNT(*) AS v FROM dbo.sailfact WITH (NOLOCK) "
                + "WHERE active='t' AND ISNULL(bamandeh,0)<>0 AND [date]>=? AND [date]<=?", from, to));
        o.put("mismatchCount", mismatchCount(c, from, to));
        o.put("byDay", select(c, "SELECT [date], ISNULL(SUM(CASE WHEN [date] IS NOT NULL THEN 1 ELSE 0 END),0) AS n, "
                + "(SELECT ISNULL(SUM(s2.[all]),0) FROM dbo.sailfact s2 WITH (NOLOCK) WHERE s2.active='t' AND s2.[date]=d.[date]) AS sales, "
                + "ISNULL(SUM(mab),0) AS receipts FROM dbo.dar d WITH (NOLOCK) "
                + "WHERE d.p=0 AND ISNULL(d.Active,1)=1 AND d.[date]>=? AND d.[date]<=? GROUP BY d.[date] ORDER BY d.[date] DESC", from, to));
        o.put("byVisitor", select(c, "SELECT v.vis_rdf, v.vis_name, "
                + "(SELECT COUNT(*) FROM dbo.sailfact s WITH (NOLOCK) WHERE s.vis_rdf=v.vis_rdf AND s.active='t' AND s.[date]>=? AND s.[date]<=?) AS invoices, "
                + "(SELECT ISNULL(SUM(s.[all]),0) FROM dbo.sailfact s WITH (NOLOCK) WHERE s.vis_rdf=v.vis_rdf AND s.active='t' AND s.[date]>=? AND s.[date]<=?) AS sales, "
                + "(SELECT ISNULL(SUM(dr.mab),0) FROM dbo.dar dr WITH (NOLOCK) WHERE dr.rdf_vis=v.vis_rdf AND dr.p=0 AND dr.[date]>=? AND dr.[date]<=?) AS receipts "
                + "FROM dbo.visitors v WITH (NOLOCK) WHERE v.Username IS NOT NULL ORDER BY v.vis_rdf", from, to, from, to, from, to));
        return o;
    }

    /**
     * Receipts whose components do not equal the receipt total — the real difference list, with the
     * difference amount and the collecting operator so a case can be assigned to a person.
     */
    public static JSONArray receiptMismatches(Connection c, String from, String to, int limit) throws Exception {
        return select(c, "SELECT TOP (" + clamp(limit, 1, 300) + ") d.ghno, d.[date], d.shmo, ISNULL(c.MONAME, N'') AS name, "
                + "ISNULL(d.mab,0) AS mab, ISNULL(d.naghd,0) AS naghd, ISNULL(d.mabcheck,0) AS mabcheck, "
                + "ISNULL((SELECT SUM(pd.MabPos) FROM dbo.PosDetails pd WITH (NOLOCK) WHERE pd.ghno=d.ghno),0) AS pos, "
                + "(ISNULL(d.mab,0) - (ISNULL(d.naghd,0)+ISNULL(d.mabcheck,0)+ISNULL((SELECT SUM(pd.MabPos) "
                + "FROM dbo.PosDetails pd WITH (NOLOCK) WHERE pd.ghno=d.ghno),0))) AS difference, "
                + "ISNULL(d.rdf_vis,0) AS rdf_vis, ISNULL(v.vis_name, N'') AS visitor_name, ISNULL(d.UserID,0) AS user_id "
                + "FROM dbo.dar d WITH (NOLOCK) LEFT JOIN dbo.CUSTOMERS c WITH (NOLOCK) ON c.SHMO=d.shmo "
                + "LEFT JOIN dbo.visitors v WITH (NOLOCK) ON v.vis_rdf=d.rdf_vis "
                + "WHERE d.p=0 AND ISNULL(d.Active,1)=1 AND d.[date]>=? AND d.[date]<=? "
                + "AND ABS(ISNULL(d.mab,0) - (ISNULL(d.naghd,0)+ISNULL(d.mabcheck,0)+ISNULL((SELECT SUM(pd.MabPos) "
                + "FROM dbo.PosDetails pd WITH (NOLOCK) WHERE pd.ghno=d.ghno),0))) > 1 "
                + "ORDER BY d.ghno DESC", from, to);
    }

    public static int mismatchCount(Connection c, String from, String to) throws Exception {
        return count(c, "SELECT COUNT(*) AS v FROM dbo.dar d WITH (NOLOCK) WHERE d.p=0 AND ISNULL(d.Active,1)=1 "
                + "AND d.[date]>=? AND d.[date]<=? AND ABS(ISNULL(d.mab,0) - (ISNULL(d.naghd,0)+ISNULL(d.mabcheck,0)"
                + "+ISNULL((SELECT SUM(pd.MabPos) FROM dbo.PosDetails pd WITH (NOLOCK) WHERE pd.ghno=d.ghno),0))) > 1", from, to);
    }

    public static JSONArray posWithoutReceipt(Connection c, int limit) throws Exception {
        return select(c, "SELECT TOP (" + clamp(limit, 1, 300) + ") pd.ID, pd.ghno, ISNULL(pd.MabPos,0) AS MabPos, pd.PosBankRdf, "
                + "pd.UserID, pd.ShPeigiri, pd.IsHavaleh, ISNULL(b.BANKNAME, N'(بینام)') AS bank_name "
                + "FROM dbo.PosDetails pd WITH (NOLOCK) LEFT JOIN dbo.BANK b WITH (NOLOCK) ON b.RDF=pd.PosBankRdf "
                + "WHERE NOT EXISTS (SELECT 1 FROM dbo.dar d WITH (NOLOCK) WHERE d.ghno=pd.ghno AND d.p=0) "
                + "ORDER BY pd.ID DESC");
    }

    public static JSONArray bankWithoutReceipt(Connection c, String from, String to, int limit) throws Exception {
        return select(c, "SELECT TOP (" + clamp(limit, 1, 300) + ") ba.rdf, ba.bank_rdf, ba.act_id, "
                + "ISNULL(ba.act_bes,0) AS act_bes, ISNULL(ba.act_bed,0) AS act_bed, ba.act_date, ba.Ghno, ba.act_dis, "
                + "ISNULL(b.BANKNAME, N'(بینام)') AS bank_name "
                + "FROM dbo.ban_act ba WITH (NOLOCK) LEFT JOIN dbo.BANK b WITH (NOLOCK) ON b.RDF=ba.bank_rdf "
                + "WHERE ISNULL(ba.isActive,1)=1 AND ba.act_date>=? AND ba.act_date<=? "
                + "AND NOT EXISTS (SELECT 1 FROM dbo.dar d WITH (NOLOCK) WHERE d.ghno=ba.Ghno AND d.p=0) "
                + "ORDER BY ba.rdf DESC", from, to);
    }

    /** Reconciliation cases recorded by this app (meelano_fin_recon). */
    public static JSONArray reconCases(Connection c, int limit) throws Exception {
        return select(c, "SELECT TOP (" + clamp(limit, 1, 200) + ") id, case_key, kind, bank_rdf, jalali_date, amount, "
                + "system_ref, bank_ref, reason, status, assigned_to, resolution, approved_by, created_by, "
                + "CONVERT(nvarchar(19), created_at, 120) AS created_at "
                + "FROM dbo.meelano_fin_recon WITH (NOLOCK) ORDER BY id DESC");
    }

    // ================================================================ banks

    /**
     * BANK rows with the real columns.
     *
     * IBAN is only usable when {@code ShabaNumber} really holds a value: in the live data every bank
     * stores the literal text "IR", so the app shows the account text (SHHE) and marks Shaba as
     * unusable instead of printing an empty IBAN. {@code HaveEChecks} is false for all 8 banks, so no
     * electronic-cheque feature is offered.
     */
    public static JSONArray banks(Connection c) throws Exception {
        return select(c, "SELECT b.RDF, b.BANKNAME, b.SHHE, b.OWNER, b.SHOBE, b.TELL1, ISNULL(b.MAN,0) AS MAN, b.IsPos, b.IsCard, "
                + "ISNULL(b.Active,1) AS Active, b.BankRdf, b.AccountType, b.HaveEChecks, b.ShabaNumber, b.CardNumber, b.BlackList, "
                + "(SELECT COUNT(*) FROM dbo.ban_act ba WITH (NOLOCK) WHERE ba.bank_rdf=b.RDF AND ISNULL(ba.isActive,1)=1) AS movement_count, "
                + "(SELECT ISNULL(SUM(ISNULL(ba.act_bed,0)-ISNULL(ba.act_bes,0)),0) FROM dbo.ban_act ba WITH (NOLOCK) WHERE ba.bank_rdf=b.RDF AND ISNULL(ba.isActive,1)=1) AS movement_net, "
                + "(SELECT MAX(ba.act_date) FROM dbo.ban_act ba WITH (NOLOCK) WHERE ba.bank_rdf=b.RDF AND ISNULL(ba.isActive,1)=1) AS last_date, "
                + "(SELECT ISNULL(SUM(pd.MabPos),0) FROM dbo.PosDetails pd WITH (NOLOCK) WHERE pd.PosBankRdf=b.RDF) AS pos_total, "
                + "(SELECT COUNT(*) FROM dbo.putchk p2 WITH (NOLOCK) WHERE p2.bankrdf=b.RDF) AS paid_checks, "
                + "(SELECT COUNT(*) FROM dbo.getchk g WITH (NOLOCK) WHERE g.our_bankrdf=b.RDF) AS deposit_checks "
                + "FROM dbo.BANK b WITH (NOLOCK) ORDER BY ISNULL(b.Active,1) DESC, b.RDF");
    }

    public static JSONArray bankMovements(Connection c, int bankRdf, String from, String to, int limit) throws Exception {
        return select(c, "SELECT TOP (" + clamp(limit, 1, 300) + ") rdf, bank_rdf, act_id, act_dis, "
                + "ISNULL(act_bes,0) AS act_bes, ISNULL(act_bed,0) AS act_bed, act_date, done_act, Ghno, DocNumber, "
                + "Time, ISNULL(isActive,1) AS isActive FROM dbo.ban_act WITH (NOLOCK) "
                + "WHERE bank_rdf=? AND act_date>=? AND act_date<=? AND ISNULL(isActive,1)=1 "
                + "ORDER BY act_date DESC, rdf DESC", bankRdf, from, to);
    }

    public static JSONArray bankDaily(Connection c, String from, String to) throws Exception {
        return select(c, "SELECT act_date, ISNULL(SUM(act_bed),0) AS in_amount, ISNULL(SUM(act_bes),0) AS out_amount, COUNT(*) AS n "
                + "FROM dbo.ban_act WITH (NOLOCK) WHERE ISNULL(isActive,1)=1 AND act_date>=? AND act_date<=? "
                + "GROUP BY act_date ORDER BY act_date DESC", from, to);
    }

    // ================================================================ customers

    public static JSONArray customers(Connection c, String search, int limit, int offset, String orderBy) throws Exception {
        boolean searching = search != null && !search.trim().isEmpty();
        String like = "%" + (search == null ? "" : search.trim()) + "%";
        int take = clamp(limit, 1, 200);
        String sql = "SELECT c.SHMO, c.MONAME, ISNULL(c.man,0) AS man, ISNULL(c.cred,0) AS cred, c.kind, c.active, "
                + "ISNULL(c.hesab_status,0) AS hesab_status, c.cell, c.tell1, c.RDF_masir, c.vis_rdf, ISNULL(c.black_list,0) AS black_list, "
                + "(SELECT COUNT(*) FROM dbo.sailfact s WITH (NOLOCK) WHERE s.shmo=c.SHMO AND s.active='t') AS invoice_count, "
                + "(SELECT ISNULL(SUM(s.[all]),0) FROM dbo.sailfact s WITH (NOLOCK) WHERE s.shmo=c.SHMO AND s.active='t') AS sales_total, "
                + "(SELECT ISNULL(SUM(d.mab),0) FROM dbo.dar d WITH (NOLOCK) WHERE d.shmo=c.SHMO AND d.p=0 AND ISNULL(d.Active,1)=1) AS received_total, "
                + "(SELECT COUNT(*) FROM dbo.getchk g WITH (NOLOCK) WHERE g.shmo=c.SHMO) AS check_count, "
                + "(SELECT MAX(d.[date]) FROM dbo.dar d WITH (NOLOCK) WHERE d.shmo=c.SHMO AND d.p=0 AND ISNULL(d.Active,1)=1) AS last_receipt_date, "
                + "(SELECT MAX(s.[date]) FROM dbo.sailfact s WITH (NOLOCK) WHERE s.shmo=c.SHMO AND s.active='t') AS last_sale_date, "
                + "(SELECT COUNT(*) FROM dbo.cust_act a WITH (NOLOCK) WHERE a.shmo=c.SHMO) AS ledger_rows "
                + "FROM dbo.CUSTOMERS c WITH (NOLOCK) "
                + (searching ? "WHERE (c.MONAME LIKE ? OR CONVERT(nvarchar(20), c.SHMO) LIKE ? OR ISNULL(c.cell,'') LIKE ? "
                + "OR ISNULL(c.tell1,'') LIKE ? OR ISNULL(c.code,'') LIKE ?) " : "")
                + orderClause(orderBy)
                + " OFFSET " + Math.max(0, offset) + " ROWS FETCH NEXT " + take + " ROWS ONLY";
        if (searching) return select(c, sql, like, like, like, like, like);
        return select(c, sql);
    }

    private static String orderClause(String orderBy) {
        if (orderBy == null) orderBy = "name";
        switch (orderBy) {
            case "balance": return "ORDER BY ISNULL(c.man,0) DESC, c.SHMO";
            case "sales": return "ORDER BY (SELECT ISNULL(SUM(s.[all]),0) FROM dbo.sailfact s WITH (NOLOCK) WHERE s.shmo=c.SHMO AND s.active='t') DESC, c.SHMO";
            default: return "ORDER BY c.MONAME";
        }
    }

    /** Everything the customer file needs in one call (balance, ledger totals, open invoices, cheques). */
    public static JSONObject customerSummary(Connection c, int shmo, String today) throws Exception {
        JSONArray rows = select(c, "SELECT c.SHMO, c.MONAME, ISNULL(c.man,0) AS man, ISNULL(c.cred,0) AS cred, c.kind, c.active, "
                + "c.cell, c.tell1, c.tell2, c.addre, c.code, ISNULL(c.hesab_status,0) AS hesab_status, "
                + "ISNULL(c.check_eteb,0) AS check_eteb, c.maxopen_time, ISNULL(c.black_list,0) AS black_list, "
                + "ISNULL(c.just_naghdi,0) AS just_naghdi, c.vis_rdf, ISNULL(v.vis_name, N'') AS visitor_name, "
                + "(SELECT ISNULL(SUM(a.act_bed),0) FROM dbo.cust_act a WITH (NOLOCK) WHERE a.shmo=c.SHMO) AS debit_total, "
                + "(SELECT ISNULL(SUM(a.act_bes),0) FROM dbo.cust_act a WITH (NOLOCK) WHERE a.shmo=c.SHMO) AS credit_total, "
                + "(SELECT COUNT(*) FROM dbo.cust_act a WITH (NOLOCK) WHERE a.shmo=c.SHMO) AS ledger_rows, "
                + "(SELECT COUNT(*) FROM dbo.sailfact s WITH (NOLOCK) WHERE s.shmo=c.SHMO AND s.active='t') AS invoice_count, "
                + "(SELECT ISNULL(SUM(s.[all]),0) FROM dbo.sailfact s WITH (NOLOCK) WHERE s.shmo=c.SHMO AND s.active='t') AS sales_total, "
                + "(SELECT ISNULL(SUM(s.[all]-ISNULL(s.MabDaryaftFactor,0)),0) FROM dbo.sailfact s WITH (NOLOCK) "
                + "   WHERE s.shmo=c.SHMO AND s.active='t' AND ISNULL(s.bamandeh,0)<>0) AS open_invoice_amount, "
                + "(SELECT COUNT(*) FROM dbo.sailfact s WITH (NOLOCK) WHERE s.shmo=c.SHMO AND s.active='t' AND ISNULL(s.bamandeh,0)<>0) AS open_invoice_count, "
                + "(SELECT ISNULL(SUM(d.mab),0) FROM dbo.dar d WITH (NOLOCK) WHERE d.shmo=c.SHMO AND d.p=0 AND ISNULL(d.Active,1)=1) AS received_total, "
                + "(SELECT ISNULL(SUM(d.naghd),0) FROM dbo.dar d WITH (NOLOCK) WHERE d.shmo=c.SHMO AND d.p=0 AND ISNULL(d.Active,1)=1) AS received_cash, "
                + "(SELECT ISNULL(SUM(d.mabcheck),0) FROM dbo.dar d WITH (NOLOCK) WHERE d.shmo=c.SHMO AND d.p=0 AND ISNULL(d.Active,1)=1) AS received_check, "
                + "(SELECT COUNT(*) FROM dbo.dar d WITH (NOLOCK) WHERE d.shmo=c.SHMO AND d.p=0 AND ISNULL(d.Active,1)=1) AS receipt_count, "
                + "(SELECT COUNT(*) FROM dbo.getchk g WITH (NOLOCK) WHERE g.shmo=c.SHMO) AS check_count, "
                + "(SELECT ISNULL(SUM(g.getchkmab),0) FROM dbo.getchk g WITH (NOLOCK) WHERE g.shmo=c.SHMO AND g.sardate>=?) AS check_open_amount, "
                + "(SELECT COUNT(*) FROM dbo.getchk g WITH (NOLOCK) WHERE g.shmo=c.SHMO AND g.sardate<?) AS check_overdue_count, "
                + "(SELECT MAX(d.[date]) FROM dbo.dar d WITH (NOLOCK) WHERE d.shmo=c.SHMO AND d.p=0 AND ISNULL(d.Active,1)=1) AS last_receipt_date, "
                + "(SELECT ISNULL(m.Mandeh,0) FROM dbo.Sys_Mandeh_Customer m WITH (NOLOCK) WHERE m.Shmo=c.SHMO) AS sys_mandeh, "
                + "(SELECT ISNULL(m.Etebar,0) FROM dbo.Sys_Mandeh_Customer m WITH (NOLOCK) WHERE m.Shmo=c.SHMO) AS sys_etebar "
                + "FROM dbo.CUSTOMERS c WITH (NOLOCK) LEFT JOIN dbo.visitors v WITH (NOLOCK) ON v.vis_rdf=c.vis_rdf "
                + "WHERE c.SHMO=?", today, today, shmo);
        return rows.length() > 0 ? rows.getJSONObject(0) : new JSONObject();
    }

    /**
     * Customer statement from the real ledger (cust_act). ✔ 2724/2724 customers agree with
     * {@code CUSTOMERS.man}, so this ledger is the statement of record; the running balance is built
     * by the screen, never written back.
     */
    public static JSONArray customerStatement(Connection c, int shmo, String from, String to, int limit) throws Exception {
        return select(c, "SELECT TOP (" + clamp(limit, 1, 500) + ") rdf_, shmo, [date], ISNULL(act_bes,0) AS act_bes, "
                + "ISNULL(act_bed,0) AS act_bed, act_dis, act_id, ghno, done_date, DocNumber, AccDocNumber, UserID, "
                + "ISNULL(isActive,1) AS isActive, ISNULL(ShowInReport,1) AS ShowInReport, "
                + "CONVERT(nvarchar(19), t_time, 120) AS stamp "
                + "FROM dbo.cust_act WITH (NOLOCK) WHERE shmo=? AND [date]>=? AND [date]<=? "
                + "ORDER BY [date] DESC, rdf_ DESC", shmo, from, to);
    }

    public static JSONArray openInvoices(Connection c, int shmo) throws Exception {
        return select(c, "SELECT shfacfo, [date], ISNULL([all],0) AS total, ISNULL(MabDaryaftFactor,0) AS received, "
                + "ISNULL([all],0)-ISNULL(MabDaryaftFactor,0) AS remaining, ISNULL(bamandeh,0) AS bamandeh, tasvieh, "
                + "Status, TaeedDate, vis_rdf, ISNULL(driver_name,N'') AS driver_name, ISNULL(description,N'') AS description "
                + "FROM dbo.sailfact WITH (NOLOCK) WHERE shmo=? AND active='t' AND ISNULL(bamandeh,0)<>0 "
                + "ORDER BY [date] DESC", shmo);
    }

    public static JSONArray customerCheques(Connection c, int shmo, String today) throws Exception {
        return select(c, "SELECT g.rdf, g.getdate, g.sardate, g.shgetchk, ISNULL(g.getchkmab,0) AS getchkmab, g.getchbank, "
                + "g.chk_satus, ISNULL(s.StatusName, N'(بدون نام)') AS status_name, g.back, "
                + "CASE WHEN g.sardate < ? THEN 1 ELSE 0 END AS is_overdue "
                + "FROM dbo.getchk g WITH (NOLOCK) LEFT JOIN dbo.getcheckhistorystatus s WITH (NOLOCK) ON s.GetStatusID=g.chk_satus "
                + "WHERE g.shmo=? ORDER BY g.sardate DESC", today, shmo);
    }

    public static JSONArray customerActivity(Connection c, int shmo, int limit) throws Exception {
        return select(c, "SELECT TOP (" + clamp(limit, 1, 200) + ") kind, ref, jalali_date, amount, detail, status FROM ("
                + "SELECT N'فاکتور فروش' AS kind, CONVERT(nvarchar(30), shfacfo) AS ref, [date] AS jalali_date, "
                + "       ISNULL([all],0) AS amount, ISNULL(description, N'') AS detail, "
                + "       CASE WHEN ISNULL(bamandeh,0)<>0 THEN N'تسویهنشده' ELSE N'تسویهشده' END AS status "
                + "  FROM dbo.sailfact WITH (NOLOCK) WHERE shmo=? AND active='t' "
                + "UNION ALL "
                + "SELECT N'قبض دریافت', CONVERT(nvarchar(30), ghno), [date], ISNULL(mab,0), "
                + "       CASE WHEN ISNULL(naghd,0)<>0 THEN N'نقدی' ELSE ISNULL(CONVERT(nvarchar(20), ted_chk), N'') END, "
                + "       CASE WHEN ISNULL(Active,1)=1 THEN N'فعال' ELSE N'ابطال' END "
                + "  FROM dbo.dar WITH (NOLOCK) WHERE shmo=? AND p=0 "
                + ") t ORDER BY jalali_date DESC", shmo, shmo);
    }

    /**
     * Receivables at customer level. The balance is the authoritative {@code CUSTOMERS.man}
     * (✔ equals the cust_act ledger sum for 2724/2724 customers on 1405/07/12), while the aging bands
     * come from the open invoices that actually carry a remaining amount.
     */
    public static JSONArray receivables(Connection c, String today, int limit, int offset) throws Exception {
        int take = clamp(limit, 1, 300);
        return select(c, "SELECT c.SHMO, c.MONAME, ISNULL(c.man,0) AS man, c.cell, c.tell1, c.kind, "
                + "ISNULL(c.check_eteb,0) AS check_eteb, c.maxopen_time, ISNULL(c.black_list,0) AS black_list, "
                + "(SELECT COUNT(*) FROM dbo.sailfact s WITH (NOLOCK) WHERE s.shmo=c.SHMO AND s.active='t' AND ISNULL(s.bamandeh,0)<>0) AS open_count, "
                + "(SELECT ISNULL(SUM(s.[all]-ISNULL(s.MabDaryaftFactor,0)),0) FROM dbo.sailfact s WITH (NOLOCK) "
                + "   WHERE s.shmo=c.SHMO AND s.active='t' AND ISNULL(s.bamandeh,0)<>0) AS open_amount, "
                + "(SELECT MIN(s.[date]) FROM dbo.sailfact s WITH (NOLOCK) WHERE s.shmo=c.SHMO AND s.active='t' AND ISNULL(s.bamandeh,0)<>0) AS oldest_open_date, "
                + "(SELECT ISNULL(SUM(g.getchkmab),0) FROM dbo.getchk g WITH (NOLOCK) WHERE g.shmo=c.SHMO AND g.sardate>?) AS future_check_amount, "
                + "(SELECT ISNULL(SUM(g.getchkmab),0) FROM dbo.getchk g WITH (NOLOCK) WHERE g.shmo=c.SHMO AND g.sardate<=?) AS due_check_amount, "
                + "(SELECT MAX(d.[date]) FROM dbo.dar d WITH (NOLOCK) WHERE d.shmo=c.SHMO AND d.p=0 AND ISNULL(d.Active,1)=1) AS last_receipt_date "
                + "FROM dbo.CUSTOMERS c WITH (NOLOCK) WHERE ISNULL(c.man,0)>0 "
                + "ORDER BY ISNULL(c.man,0) DESC OFFSET " + Math.max(0, offset) + " ROWS FETCH NEXT " + take + " ROWS ONLY",
                today, today);
    }

    public static JSONObject receivableTotals(Connection c, String today) throws Exception {
        JSONObject o = new JSONObject();
        o.put("balanceTotal", sum(c, "SELECT ISNULL(SUM(man),0) AS v FROM dbo.CUSTOMERS WITH (NOLOCK) WHERE ISNULL(man,0)>0"));
        o.put("customers", count(c, "SELECT COUNT(*) AS v FROM dbo.CUSTOMERS WITH (NOLOCK) WHERE ISNULL(man,0)>0"));
        o.put("creditTotal", sum(c, "SELECT ISNULL(SUM(man),0) AS v FROM dbo.CUSTOMERS WITH (NOLOCK) WHERE ISNULL(man,0)<0"));
        o.put("creditCustomers", count(c, "SELECT COUNT(*) AS v FROM dbo.CUSTOMERS WITH (NOLOCK) WHERE ISNULL(man,0)<0"));
        o.put("openAmount", sum(c, "SELECT ISNULL(SUM([all]-ISNULL(MabDaryaftFactor,0)),0) AS v FROM dbo.sailfact WITH (NOLOCK) "
                + "WHERE active='t' AND ISNULL(bamandeh,0)<>0"));
        o.put("openCount", count(c, "SELECT COUNT(*) AS v FROM dbo.sailfact WITH (NOLOCK) WHERE active='t' AND ISNULL(bamandeh,0)<>0"));
        o.put("checksSecuring", sum(c, "SELECT ISNULL(SUM(getchkmab),0) AS v FROM dbo.getchk WITH (NOLOCK) WHERE sardate>?", today));
        o.put("checksDueNow", sum(c, "SELECT ISNULL(SUM(getchkmab),0) AS v FROM dbo.getchk WITH (NOLOCK) WHERE sardate<=?", today));
        o.put("followupsOpen", count(c, "SELECT COUNT(*) AS v FROM dbo.meelano_fin_followup WITH (NOLOCK) WHERE status IN (N'pending',N'promise')"));
        o.put("promisesDue", count(c, "SELECT COUNT(*) AS v FROM dbo.meelano_fin_followup WITH (NOLOCK) WHERE promise_date IS NOT NULL AND promise_date<=?", today));
        return o;
    }

    /**
     * Aging of open invoices. The bands are computed from the invoice date; the app labels them
     * 0-7 / 8-30 / 31-60 / 61-90 / 90+ and shows a zero band honestly as zero — the live data only
     * spans about one month, so the older bands are expected to be empty.
     */
    public static JSONArray agingBands(Connection c, String today) throws Exception {
        return select(c, "SELECT band, COUNT(*) AS n, ISNULL(SUM(remaining),0) AS amount FROM ("
                + "SELECT CASE WHEN ? <= s.[date] THEN N'0-7' "
                + "            WHEN ? <= s.[date] THEN N'8-30' "
                + "            WHEN ? <= s.[date] THEN N'31-60' "
                + "            WHEN ? <= s.[date] THEN N'61-90' ELSE N'90+' END AS band, "
                + "       ISNULL(s.[all],0)-ISNULL(s.MabDaryaftFactor,0) AS remaining "
                + "  FROM dbo.sailfact s WITH (NOLOCK) WHERE s.active='t' AND ISNULL(s.bamandeh,0)<>0) t "
                + "GROUP BY band ORDER BY band",
                FinFmt.addDays(today, -7), FinFmt.addDays(today, -30), FinFmt.addDays(today, -60),
                FinFmt.addDays(today, -90));
    }

    /** Aged open-invoice queue for the aging report and the collection work list. */
    public static JSONArray openInvoiceQueue(Connection c, int limit) throws Exception {
        return select(c, "SELECT TOP (" + clamp(limit, 1, 600) + ") s.shfacfo, s.shmo, ISNULL(c.MONAME,N'') AS name, s.[date], "
                + "ISNULL(s.[all],0) AS total, ISNULL(s.MabDaryaftFactor,0) AS received, "
                + "ISNULL(s.[all],0)-ISNULL(s.MabDaryaftFactor,0) AS remaining, s.tasvieh, s.Status, c.cell "
                + "FROM dbo.sailfact s WITH (NOLOCK) LEFT JOIN dbo.CUSTOMERS c WITH (NOLOCK) ON c.SHMO=s.shmo "
                + "WHERE s.active='t' AND ISNULL(s.bamandeh,0)<>0 ORDER BY s.[date]");
    }

    // ================================================================ cheques

    public static final String MODE_ALL = "all";
    public static final String MODE_DUE_TODAY = "due_today";
    public static final String MODE_OVERDUE = "overdue";
    public static final String MODE_NEXT7 = "next7";
    public static final String MODE_TREASURY = "treasury";
    public static final String MODE_DEPOSITED = "deposited";
    public static final String MODE_RETURNED = "returned";

    /**
     * Cheque rows with the real status text from {@code getcheckhistorystatus}
     * (✔ e.g. 1 «دریافت در خرید و فروش», 5 «خرج چک در پرداخت حسابداری», 8 «اخذ چک از مشتری در خزانه داری»).
     * Period modes filter the entry date (getdate); the due-date modes use the server date.
     */
    public static JSONArray cheques(Connection c, String from, String to, String mode, String search, int limit, int offset)
            throws Exception {
        String today = serverToday(c);
        boolean searching = search != null && !search.trim().isEmpty();
        String like = "%" + (search == null ? "" : search.trim()) + "%";
        String m = mode == null ? MODE_ALL : mode;
        int take = clamp(limit, 1, 300);

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT g.rdf, g.getdate, g.sardate, g.shgetchk, g.getchkshhes, g.getchbank, g.getchkshobe, ")
                .append("ISNULL(g.getchkmab,0) AS getchkmab, g.shmo, ISNULL(c.MONAME, g.VIRTUALNAME) AS customer_name, ")
                .append("g.chk_satus, ISNULL(s.StatusName, N'(بدون نام)') AS status_name, g.back, g.mod, g.soo, g.ghno, ")
                .append("g.kharj_date, ISNULL(g.karj_virtualname,N'') AS kharj_name, g.our_bankrdf, g.CheckTypeID, ")
                .append("ISNULL(ct.Desciption, N'') AS check_type, g.ShenaseSayad, g.RegistrationInquiry, ")
                .append("ISNULL(v.vis_name, N'') AS visitor_name, ISNULL(g.VIRTUALNAME,N'') AS virtual_name, ")
                .append("CASE WHEN g.sardate < ? THEN 1 ELSE 0 END AS is_overdue ")
                .append("FROM dbo.getchk g WITH (NOLOCK) ")
                .append("LEFT JOIN dbo.CUSTOMERS c WITH (NOLOCK) ON c.SHMO=g.shmo ")
                .append("LEFT JOIN dbo.getcheckhistorystatus s WITH (NOLOCK) ON s.GetStatusID=g.chk_satus ")
                .append("LEFT JOIN dbo.CheckTypes ct WITH (NOLOCK) ON ct.ID=g.CheckTypeID ")
                .append("LEFT JOIN dbo.visitors v WITH (NOLOCK) ON v.vis_rdf=g.vis_rdf WHERE 1=1 ");
        List<Object> params = new ArrayList<>();
        params.add(today);
        if (searching) {
            sql.append("AND (g.shgetchk LIKE ? OR c.MONAME LIKE ? OR g.getchbank LIKE ? OR ISNULL(g.VIRTUALNAME,'') LIKE ? ")
                    .append("OR ISNULL(g.ShenaseSayad,'') LIKE ? OR CONVERT(nvarchar(20), g.ghno) LIKE ?) ");
            for (int i = 0; i < 6; i++) params.add(like);
        }
        if (MODE_TREASURY.equals(m)) {
            sql.append("AND g.chk_satus IN (8,9,10) ");
        } else if (MODE_DEPOSITED.equals(m)) {
            sql.append("AND g.our_bankrdf > 0 ");
        } else if (MODE_RETURNED.equals(m)) {
            sql.append("AND g.back IN ('t','T') ");
        } else if (MODE_DUE_TODAY.equals(m)) {
            sql.append("AND g.sardate = ? ");
            params.add(today);
        } else if (MODE_OVERDUE.equals(m)) {
            sql.append("AND g.sardate < ? ");
            params.add(today);
        } else if (MODE_NEXT7.equals(m)) {
            sql.append("AND g.sardate > ? AND g.sardate <= ? ");
            params.add(today);
            params.add(FinFmt.addDays(today, 7));
        } else {
            sql.append("AND g.getdate >= ? AND g.getdate <= ? ");
            params.add(from);
            params.add(to);
        }
        String order = (MODE_DUE_TODAY.equals(m) || MODE_OVERDUE.equals(m) || MODE_NEXT7.equals(m))
                ? " ORDER BY g.sardate, g.rdf DESC" : " ORDER BY g.rdf DESC";
        sql.append(order).append(" OFFSET ").append(Math.max(0, offset)).append(" ROWS FETCH NEXT ").append(take).append(" ROWS ONLY");
        return select(c, sql.toString(), params.toArray());
    }

    /** Cheque census: count and amount per real status, with overdue and returned splits. */
    public static JSONArray chequeStatusCensus(Connection c, boolean received, String today) throws Exception {
        if (received) {
            return select(c, "SELECT g.chk_satus AS status_id, ISNULL(s.StatusName, N'(بدون نام)') AS status_name, "
                    + "COUNT(*) AS n, ISNULL(SUM(g.getchkmab),0) AS total, "
                    + "SUM(CASE WHEN g.sardate < ? THEN 1 ELSE 0 END) AS overdue, "
                    + "SUM(CASE WHEN ISNULL(g.back,'f') IN ('t','T') THEN 1 ELSE 0 END) AS returned "
                    + "FROM dbo.getchk g WITH (NOLOCK) LEFT JOIN dbo.getcheckhistorystatus s WITH (NOLOCK) ON s.GetStatusID=g.chk_satus "
                    + "GROUP BY g.chk_satus, s.StatusName ORDER BY n DESC", today);
        }
        return select(c, "SELECT putchk_status AS status_id, COUNT(*) AS n, ISNULL(SUM(putchkmab),0) AS total, "
                + "SUM(CASE WHEN sardate < ? AND putchk_status IN (1,7) THEN 1 ELSE 0 END) AS overdue "
                + "FROM dbo.putchk WITH (NOLOCK) GROUP BY putchk_status ORDER BY n DESC", today);
    }

    public static JSONArray paidCheques(Connection c, String from, String to, String search, int limit, int offset) throws Exception {
        String today = serverToday(c);
        boolean searching = search != null && !search.trim().isEmpty();
        String like = "%" + (search == null ? "" : search.trim()) + "%";
        int take = clamp(limit, 1, 300);
        String sql = "SELECT p.rdf, p.putdate, p.sardate, p.shputchk, p.bankrdf, "
                + "ISNULL(b.BANKNAME, N'(بینام)') AS bank_name, ISNULL(p.putchkmab,0) AS putchkmab, p.shmo, p.girande, "
                + "p.putchk_status, p.putchkdis, p.amani, p.ghno, p.done_date, p.CheckTypeID, "
                + "ISNULL(ct.Desciption, N'') AS check_type, p.ShenaseSayad, "
                + "CASE WHEN p.sardate < ? AND p.putchk_status IN (1,7) THEN 1 ELSE 0 END AS is_overdue "
                + "FROM dbo.putchk p WITH (NOLOCK) "
                + "LEFT JOIN dbo.BANK b WITH (NOLOCK) ON b.RDF=p.bankrdf "
                + "LEFT JOIN dbo.CheckTypes ct WITH (NOLOCK) ON ct.ID=p.CheckTypeID "
                + "WHERE " + (searching ? "(p.shputchk LIKE ? OR p.girande LIKE ?) AND " : "")
                + "p.putdate>=? AND p.putdate<=? ORDER BY p.sardate, p.rdf DESC "
                + "OFFSET " + Math.max(0, offset) + " ROWS FETCH NEXT " + take + " ROWS ONLY";
        if (searching) return select(c, sql, today, like, like, from, to);
        return select(c, sql, today, from, to);
    }

    public static JSONArray paidChequeCensus(Connection c) throws Exception {
        return select(c, "SELECT putchk_status AS status_id, COUNT(*) AS n, ISNULL(SUM(putchkmab),0) AS total "
                + "FROM dbo.putchk WITH (NOLOCK) GROUP BY putchk_status ORDER BY n DESC");
    }

    /** Cheque status names exactly as Atiran defines them (filters and legend). */
    public static JSONArray checkStatusNames(Connection c) throws Exception {
        return select(c, "SELECT GetStatusID, StatusName FROM dbo.getcheckhistorystatus WITH (NOLOCK) ORDER BY GetStatusID");
    }

    public static JSONArray checkTypes(Connection c) throws Exception {
        return select(c, "SELECT ID, Desciption FROM dbo.CheckTypes WITH (NOLOCK) ORDER BY ID");
    }

    /** Unified calendar of everything with a due date (cheques, invoices, promises, settlements, cases). */
    public static JSONArray calendar(Connection c, String from, String to, String today, int limit) throws Exception {
        return select(c, "SELECT TOP (" + clamp(limit, 1, 300) + ") kind, jalali_date, ref, amount, detail, status FROM ("
                + "SELECT N'چک دریافتی' AS kind, g.sardate AS jalali_date, ISNULL(g.shgetchk,N'') AS ref, ISNULL(g.getchkmab,0) AS amount, "
                + "       ISNULL(c.MONAME, g.VIRTUALNAME) AS detail, "
                + "       CASE WHEN g.sardate < ? THEN N'سررسید گذشته' ELSE N'در انتظار' END AS status "
                + "  FROM dbo.getchk g WITH (NOLOCK) LEFT JOIN dbo.CUSTOMERS c WITH (NOLOCK) ON c.SHMO=g.shmo "
                + " WHERE g.sardate>=? AND g.sardate<=? "
                + "UNION ALL "
                + "SELECT N'چک پرداختی', p.sardate, ISNULL(p.shputchk,N''), ISNULL(p.putchkmab,0), ISNULL(p.girande,N''), "
                + "       CASE WHEN p.sardate < ? AND p.putchk_status IN (1,7) THEN N'سررسید گذشته' ELSE N'ثبتشده' END "
                + "  FROM dbo.putchk p WITH (NOLOCK) WHERE p.sardate>=? AND p.sardate<=? "
                + "UNION ALL "
                + "SELECT N'فاکتور فروش', s.[date], CONVERT(nvarchar(30), s.shfacfo), ISNULL(s.[all],0), ISNULL(c2.MONAME,N''), "
                + "       CASE WHEN ISNULL(s.bamandeh,0)<>0 THEN N'تسویهنشده' ELSE N'تسویهشده' END "
                + "  FROM dbo.sailfact s WITH (NOLOCK) LEFT JOIN dbo.CUSTOMERS c2 WITH (NOLOCK) ON c2.SHMO=s.shmo "
                + " WHERE s.active='t' AND s.[date]>=? AND s.[date]<=? "
                + "UNION ALL "
                + "SELECT N'وعده پرداخت', f.promise_date, CONVERT(nvarchar(30), f.shmo), ISNULL(f.promise_amount,0), "
                + "       ISNULL(f.note,N''), ISNULL(f.status,N'pending') "
                + "  FROM dbo.meelano_fin_followup f WITH (NOLOCK) WHERE f.promise_date IS NOT NULL AND f.promise_date>=? AND f.promise_date<=? "
                + "UNION ALL "
                + "SELECT N'تسویه کاربر', st.jalali_date, ISNULL(st.user_login,N''), ISNULL(st.amount,0), N'تسویه', ISNULL(st.status,N'pending') "
                + "  FROM dbo.meelano_fin_settlement st WITH (NOLOCK) WHERE st.jalali_date>=? AND st.jalali_date<=? "
                + "UNION ALL "
                + "SELECT N'مغایرت', rc.jalali_date, ISNULL(rc.case_key,N''), ISNULL(rc.amount,0), ISNULL(rc.kind,N''), ISNULL(rc.status,N'open') "
                + "  FROM dbo.meelano_fin_recon rc WITH (NOLOCK) WHERE rc.jalali_date IS NOT NULL AND rc.jalali_date>=? AND rc.jalali_date<=? "
                + ") t ORDER BY jalali_date DESC",
                today, from, to, today, from, to, from, to, from, to, from, to, from, to);
    }

    // ================================================================ POS

    public static JSONArray posTransactions(Connection c, String from, String to, String bankFilter, String search,
                                            int limit, int offset) throws Exception {
        boolean searching = search != null && !search.trim().isEmpty();
        String like = "%" + (search == null ? "" : search.trim()) + "%";
        boolean bank = bankFilter != null && !bankFilter.isEmpty() && !"0".equals(bankFilter);
        int take = clamp(limit, 1, 300);
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT pd.ID, pd.ghno, ISNULL(pd.MabPos,0) AS MabPos, pd.PosBankRdf, ")
                .append("ISNULL(b.BANKNAME, N'(بینام)') AS bank_name, ISNULL(pd.Karmozd,0) AS Karmozd, pd.ShPeigiri, pd.IsHavaleh, ")
                .append("pd.UserID, ISNULL(v.vis_name, N'') AS user_name, ISNULL(v.Username, N'') AS username, pd.TerminalID, ")
                .append("d.[date] AS jalali_date, d.shmo, ISNULL(cu.MONAME, N'') AS customer_name, ISNULL(d.mab,0) AS receipt_total, ")
                .append("ISNULL(pd.PosDesc, N'') AS PosDesc, pd.isEdited ")
                .append("FROM dbo.PosDetails pd WITH (NOLOCK) ")
                .append("INNER JOIN dbo.dar d WITH (NOLOCK) ON d.ghno=pd.ghno AND d.p=0 ")
                .append("LEFT JOIN dbo.BANK b WITH (NOLOCK) ON b.RDF=pd.PosBankRdf ")
                .append("LEFT JOIN dbo.visitors v WITH (NOLOCK) ON v.vis_rdf=pd.UserID ")
                .append("LEFT JOIN dbo.CUSTOMERS cu WITH (NOLOCK) ON cu.SHMO=d.shmo ")
                .append("WHERE d.[date]>=? AND d.[date]<=? ");
        List<Object> params = new ArrayList<>();
        params.add(from);
        params.add(to);
        if (bank) {
            sql.append("AND pd.PosBankRdf=? ");
            params.add(Integer.parseInt(bankFilter));
        }
        if (searching) {
            sql.append("AND (pd.ShPeigiri LIKE ? OR v.vis_name LIKE ? OR ISNULL(pd.PosDesc,'') LIKE ? OR ISNULL(b.BANKNAME,'') LIKE ?) ");
            for (int i = 0; i < 4; i++) params.add(like);
        }
        sql.append("ORDER BY pd.ID DESC OFFSET ").append(Math.max(0, offset)).append(" ROWS FETCH NEXT ").append(take).append(" ROWS ONLY");
        return select(c, sql.toString(), params.toArray());
    }

    /**
     * POS settlement view. PosDetails.UserID is the visitor id (✔ verified: UserID 6 = joavad latifi,
     * 3 = fatemeh mahmoudi, 5 = elham), and every transaction is joined to its receipt through
     * {@code dar.ghno}. Transactions without a receipt are counted separately and never added silently.
     */
    public static JSONObject posSummary(Connection c, String from, String to) throws Exception {
        JSONObject o = new JSONObject();
        String join = " FROM dbo.PosDetails pd WITH (NOLOCK) INNER JOIN dbo.dar d WITH (NOLOCK) ON d.ghno=pd.ghno AND d.p=0 "
                + "WHERE d.[date]>=? AND d.[date]<=?";
        o.put("from", from);
        o.put("to", to);
        o.put("total", sum(c, "SELECT ISNULL(SUM(pd.MabPos),0) AS v" + join, from, to));
        o.put("count", count(c, "SELECT COUNT(*) AS v" + join, from, to));
        o.put("fee", sum(c, "SELECT ISNULL(SUM(pd.Karmozd),0) AS v" + join, from, to));
        o.put("havaleh", sum(c, "SELECT ISNULL(SUM(pd.MabPos),0) AS v" + join + " AND pd.IsHavaleh=1", from, to));
        o.put("withoutReceipt", count(c, "SELECT COUNT(*) AS v FROM dbo.PosDetails pd WITH (NOLOCK) "
                + "WHERE NOT EXISTS (SELECT 1 FROM dbo.dar d WITH (NOLOCK) WHERE d.ghno=pd.ghno AND d.p=0)"));
        o.put("terminals", count(c, "SELECT COUNT(*) AS v FROM dbo.TerminalPos WITH (NOLOCK)"));
        o.put("withTerminal", count(c, "SELECT COUNT(*) AS v FROM dbo.PosDetails WITH (NOLOCK) WHERE TerminalID IS NOT NULL AND TerminalID<>0"));
        o.put("byBank", select(c, "SELECT pd.PosBankRdf AS bank, ISNULL(b.BANKNAME, N'(بینام)') AS bank_name, COUNT(*) AS n, "
                + "ISNULL(SUM(pd.MabPos),0) AS total, ISNULL(SUM(pd.Karmozd),0) AS fee"
                + " FROM dbo.PosDetails pd WITH (NOLOCK) INNER JOIN dbo.dar d WITH (NOLOCK) ON d.ghno=pd.ghno AND d.p=0 "
                + "LEFT JOIN dbo.BANK b WITH (NOLOCK) ON b.RDF=pd.PosBankRdf "
                + "WHERE d.[date]>=? AND d.[date]<=? GROUP BY pd.PosBankRdf, b.BANKNAME ORDER BY n DESC", from, to));
        o.put("byUser", select(c, "SELECT pd.UserID, ISNULL(v.vis_name, N'(بینام)') AS user_name, ISNULL(v.Username,'') AS username, "
                + "COUNT(*) AS n, ISNULL(SUM(pd.MabPos),0) AS total"
                + " FROM dbo.PosDetails pd WITH (NOLOCK) INNER JOIN dbo.dar d WITH (NOLOCK) ON d.ghno=pd.ghno AND d.p=0 "
                + "LEFT JOIN dbo.visitors v WITH (NOLOCK) ON v.vis_rdf=pd.UserID "
                + "WHERE d.[date]>=? AND d.[date]<=? GROUP BY pd.UserID, v.vis_name, v.Username ORDER BY n DESC", from, to));
        o.put("byDay", select(c, "SELECT d.[date] AS jalali_date, COUNT(*) AS n, ISNULL(SUM(pd.MabPos),0) AS total"
                + " FROM dbo.PosDetails pd WITH (NOLOCK) INNER JOIN dbo.dar d WITH (NOLOCK) ON d.ghno=pd.ghno AND d.p=0 "
                + "WHERE d.[date]>=? AND d.[date]<=? GROUP BY d.[date] ORDER BY d.[date] DESC", from, to));
        return o;
    }

    // ================================================================ cash & settlement

    public static JSONObject cashSummary(Connection c, String today) throws Exception {
        JSONObject o = new JSONObject();
        String live = " FROM dbo.COW WITH (NOLOCK) WHERE ISNULL(isActive,1)=1";
        o.put("today", today);
        o.put("balance", sum(c, "SELECT ISNULL(SUM(ISNULL(BED,0)-ISNULL(BES,0)),0) AS v" + live));
        o.put("inToday", sum(c, "SELECT ISNULL(SUM(BED),0) AS v" + live + " AND DATE=?", today));
        o.put("outToday", sum(c, "SELECT ISNULL(SUM(BES),0) AS v" + live + " AND DATE=?", today));
        o.put("rowsToday", count(c, "SELECT COUNT(*) AS v" + live + " AND DATE=?", today));
        o.put("inMonth", sum(c, "SELECT ISNULL(SUM(BED),0) AS v" + live + " AND DATE>=? AND DATE<=?", MeelanoJalali.monthStart(today), today));
        o.put("outMonth", sum(c, "SELECT ISNULL(SUM(BES),0) AS v" + live + " AND DATE>=? AND DATE<=?", MeelanoJalali.monthStart(today), today));
        o.put("opening", sum(c, "SELECT ISNULL(SUM(BED),0) AS v FROM dbo.COW WITH (NOLOCK) WHERE act_id=71"));
        o.put("rows", count(c, "SELECT COUNT(*) AS v" + live));
        o.put("lastDate", text(c, "SELECT MAX(DATE) AS v" + live));
        o.put("firstDate", text(c, "SELECT MIN(DATE) AS v" + live));
        o.put("byKind", select(c, "SELECT act_id, COUNT(*) AS n, ISNULL(SUM(BED),0) AS in_amount, ISNULL(SUM(BES),0) AS out_amount, "
                + "MIN(LEFT(ISNULL(DIS,N''),40)) AS sample" + live + " GROUP BY act_id ORDER BY n DESC"));
        return o;
    }

    public static JSONArray cashMovements(Connection c, String from, String to, int limit, int offset) throws Exception {
        int take = clamp(limit, 1, 300);
        return select(c, "SELECT rdf, DATE, act_id, DIS, ISNULL(BED,0) AS BED, "
                + "ISNULL(BES,0) AS BES, bank_rdf, Ghno, DocNumber, AccDocNumber, UserID, Time "
                + "FROM dbo.COW WITH (NOLOCK) WHERE DATE>=? AND DATE<=? AND ISNULL(isActive,1)=1 "
                + "ORDER BY DATE DESC, rdf DESC OFFSET " + Math.max(0, offset) + " ROWS FETCH NEXT " + take + " ROWS ONLY",
                from, to);
    }

    public static JSONArray settlements(Connection c, String from, String to, int limit) throws Exception {
        return select(c, "SELECT TOP (" + clamp(limit, 1, 200) + ") id, op_key, user_login, jalali_date, kind, amount, "
                + "expected, difference, reference, receiver, status, confirmed_by, "
                + "CONVERT(nvarchar(19), created_at, 120) AS created_at, "
                + "CONVERT(nvarchar(19), confirmed_at, 120) AS confirmed_at, created_by, "
                + "CASE WHEN signature IS NULL THEN 0 ELSE 1 END AS has_signature "
                + "FROM dbo.meelano_fin_settlement WITH (NOLOCK) WHERE jalali_date>=? AND jalali_date<=? ORDER BY id DESC",
                from, to);
    }

    /**
     * Operator settlement overview. Real sources:
     *   Sales      sailfact.vis_rdf                        ✔ all 970 active invoices carry a visitor
     *   Receipts   dar.rdf_vis                             the collecting operator of the receipt
     *   POS        PosDetails.UserID = visitors.vis_rdf    ✔ verified operator by operator
     *   Delivered  meelano_fin_settlement                  deliveries recorded in this app
     * Cheque rows carry no operator in this data (getchk.vis_rdf is 0 for all 118 rows), so the cheque
     * column stays empty and the screen says so instead of inventing an attribution.
     */
    public static JSONArray userSettlement(Connection c, String from, String to) throws Exception {
        return select(c, "SELECT v.vis_rdf, v.vis_name, ISNULL(v.Username,'') AS username, v.kind, "
                + "(SELECT COUNT(*) FROM dbo.sailfact s WITH (NOLOCK) WHERE s.vis_rdf=v.vis_rdf AND s.active='t' AND s.[date]>=? AND s.[date]<=?) AS invoices, "
                + "(SELECT ISNULL(SUM(s.[all]),0) FROM dbo.sailfact s WITH (NOLOCK) WHERE s.vis_rdf=v.vis_rdf AND s.active='t' AND s.[date]>=? AND s.[date]<=?) AS sales, "
                + "(SELECT ISNULL(SUM(d.mab),0) FROM dbo.dar d WITH (NOLOCK) WHERE d.rdf_vis=v.vis_rdf AND d.p=0 AND ISNULL(d.Active,1)=1 AND d.[date]>=? AND d.[date]<=?) AS receipts, "
                + "(SELECT ISNULL(SUM(d.naghd),0) FROM dbo.dar d WITH (NOLOCK) WHERE d.rdf_vis=v.vis_rdf AND d.p=0 AND ISNULL(d.Active,1)=1 AND d.[date]>=? AND d.[date]<=?) AS cash, "
                + "(SELECT ISNULL(SUM(d.mabcheck),0) FROM dbo.dar d WITH (NOLOCK) WHERE d.rdf_vis=v.vis_rdf AND d.p=0 AND ISNULL(d.Active,1)=1 AND d.[date]>=? AND d.[date]<=?) AS checks, "
                + "(SELECT ISNULL(SUM(pd.MabPos),0) FROM dbo.PosDetails pd WITH (NOLOCK) "
                + "   INNER JOIN dbo.dar d2 WITH (NOLOCK) ON d2.ghno=pd.ghno AND d2.p=0 "
                + "   WHERE pd.UserID=v.vis_rdf AND d2.[date]>=? AND d2.[date]<=?) AS pos, "
                + "(SELECT ISNULL(SUM(st.amount),0) FROM dbo.meelano_fin_settlement st WITH (NOLOCK) "
                + "   WHERE st.user_login=ISNULL(v.Username,'') AND st.jalali_date>=? AND st.jalali_date<=? AND st.status<>N'rejected') AS delivered, "
                + "(SELECT COUNT(*) FROM dbo.meelano_fin_settlement st WITH (NOLOCK) "
                + "   WHERE st.user_login=ISNULL(v.Username,'') AND st.jalali_date>=? AND st.jalali_date<=? AND st.status<>N'rejected') AS deliveries "
                + "FROM dbo.visitors v WITH (NOLOCK) WHERE v.Username IS NOT NULL AND LTRIM(RTRIM(v.Username))<>'' ORDER BY v.vis_rdf",
                from, to, from, to, from, to, from, to, from, to, from, to, from, to, from, to);
    }

    // ================================================================ follow-up / activity

    public static JSONArray followUps(Connection c, int shmo, int limit) throws Exception {
        if (shmo > 0) {
            return select(c, "SELECT TOP (" + clamp(limit, 1, 200) + ") id, shmo, action, note, promise_amount, promise_date, "
                    + "status, next_action, next_date, assigned_to, created_by, "
                    + "CONVERT(nvarchar(19), created_at, 120) AS created_at "
                    + "FROM dbo.meelano_fin_followup WITH (NOLOCK) WHERE shmo=? ORDER BY id DESC", shmo);
        }
        return select(c, "SELECT TOP (" + clamp(limit, 1, 200) + ") f.id, f.shmo, ISNULL(c.MONAME,N'') AS name, f.action, f.note, "
                + "f.promise_amount, f.promise_date, f.status, f.next_action, f.next_date, f.assigned_to, f.created_by, "
                + "CONVERT(nvarchar(19), f.created_at, 120) AS created_at "
                + "FROM dbo.meelano_fin_followup f WITH (NOLOCK) LEFT JOIN dbo.CUSTOMERS c WITH (NOLOCK) ON c.SHMO=f.shmo "
                + "ORDER BY f.id DESC");
    }

    /** Activity feed: the finance audit trail, or real Atiran receipts before anything was recorded. */
    public static JSONArray activity(Connection c, int limit) throws Exception {
        JSONArray rows = select(c, "SELECT TOP (" + clamp(limit, 1, 200) + ") id, username, role_key, module, action, reference, "
                + "amount, device, app_version, CONVERT(nvarchar(19), created_at, 120) AS at "
                + "FROM dbo.meelano_fin_audit WITH (NOLOCK) ORDER BY id DESC");
        if (rows.length() > 0) return rows;
        return select(c, "SELECT TOP (" + clamp(limit, 1, 200) + ") ghno AS id, N'Atiran' AS username, N'operator' AS role_key, "
                + "N'قبض دریافت' AS module, N'ثبت' AS action, CONVERT(nvarchar(30), ghno) AS reference, ISNULL(mab,0) AS amount, "
                + "N'' AS device, N'' AS app_version, [date] AS at FROM dbo.dar WITH (NOLOCK) WHERE p=0 ORDER BY ghno DESC");
    }

    // ================================================================ reports

    /** Daily operations report: the home figures plus the lists the daily closing needs. */
    public static JSONObject dailyReport(Connection c, String today) throws Exception {
        JSONObject o = home(c, today);
        o.put("checksDueList", select(c, "SELECT TOP (60) g.rdf, g.shgetchk, g.sardate, ISNULL(g.getchkmab,0) AS getchkmab, "
                + "ISNULL(c.MONAME, g.VIRTUALNAME) AS name, ISNULL(s.StatusName, N'') AS status_name "
                + "FROM dbo.getchk g WITH (NOLOCK) LEFT JOIN dbo.CUSTOMERS c WITH (NOLOCK) ON c.SHMO=g.shmo "
                + "LEFT JOIN dbo.getcheckhistorystatus s WITH (NOLOCK) ON s.GetStatusID=g.chk_satus "
                + "WHERE g.sardate<=? ORDER BY g.sardate", today));
        o.put("receiptsToday", select(c, "SELECT TOP (60) d.ghno, d.[date], ISNULL(d.mab,0) AS mab, d.shmo, ISNULL(c.MONAME,N'') AS name, "
                + "ISNULL(d.naghd,0) AS naghd, ISNULL(d.mabcheck,0) AS mabcheck, "
                + "ISNULL((SELECT SUM(pd.MabPos) FROM dbo.PosDetails pd WITH (NOLOCK) WHERE pd.ghno=d.ghno),0) AS pos "
                + "FROM dbo.dar d WITH (NOLOCK) LEFT JOIN dbo.CUSTOMERS c WITH (NOLOCK) ON c.SHMO=d.shmo "
                + "WHERE d.p=0 AND ISNULL(d.Active,1)=1 AND d.[date]=? ORDER BY d.ghno DESC", today));
        o.put("settlementsToday", settlements(c, today, today, 60));
        o.put("openRecon", reconCases(c, 40));
        o.put("mismatchToday", receiptMismatches(c, today, today, 40));
        return o;
    }

    private static int clamp(int v, int min, int max) { return Math.max(min, Math.min(max, v)); }
}

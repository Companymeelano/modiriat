package ir.meelano.android.finance;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Pure-JVM checks for the pieces of «آتیران مالی» that decide what an operator sees: the rounding of
 * a chart axis, the amount formats, the period presets and the status colour language.
 *
 * They run without a device (JUnit on the JVM), which is why they only touch logic that has no
 * Android dependency. The finance screens themselves are exercised end to end by the CI self test
 * against a restored copy of the Atiran database.
 */
public class FinChartsTest {

    @Test public void axisMaximumRoundsUpToReadableThirds() {
        assertEquals(1.2, FinCharts.Base.niceMax(1.0), 1e-9);
        assertEquals(12.0, FinCharts.Base.niceMax(10.0), 1e-9);
        assertEquals(1200.0, FinCharts.Base.niceMax(1000.0), 1e-9);
        assertEquals(1.0, FinCharts.Base.niceMax(0), 1e-9);
        // The rounded maximum is always at least the value itself.
        double[] samples = {3, 7, 19, 55, 340, 4200, 9_700_000, 123_456_789};
        for (double v : samples) assertTrue("niceMax(" + v + ")", FinCharts.Base.niceMax(v) >= v);
    }

    @Test public void monotoneTangentsNeverOvershoot() {
        float[] y = {0f, 10f, 4f, 9f};
        float[] m = FinCharts.Base.monotone(y);
        assertEquals(y.length, m.length);
        // A local minimum (10 → 4) must flatten, or the curve would dip below the data.
        assertEquals(0f, m[2], 1e-6);
        assertEquals(0f, FinCharts.Base.monotone(new float[]{5f})[0], 1e-6);
    }

    @Test public void amountFormatsStayCopyable() {
        assertEquals("1,234,567", FinFmt.amount(1_234_567.4));
        assertEquals("-1,234,567", FinFmt.amount(-1_234_567.4));
        assertEquals("1.2 میلیارد", FinFmt.compact(1_200_000_000.0));
        // Amounts keep Latin digits on purpose (they are copied into Excel/PDF), counts do not.
        assertEquals("500 هزار", FinFmt.compact(500_000.0));
        assertEquals("۸۵۰", FinFmt.compact(850.0));
        assertEquals("۱۲۳", FinFmt.faNumber("123"));
        assertEquals("۱٬۲۴۰", FinCharts.COUNT.format(1240));
    }

    @Test public void periodPresetsAlwaysEndOnServerToday() {
        String today = "1405/07/11";
        assertEquals(today, FinFmt.periodRange("today", today, null, null)[1]);
        assertEquals("1405/07/10", FinFmt.periodRange("yesterday", today, null, null)[0]);
        assertEquals("1405/07/05", FinFmt.periodRange("7d", today, null, null)[0]);
        assertEquals("1405/07/01", FinFmt.periodRange("month", today, null, null)[0]);
        assertEquals("1405/06/01", FinFmt.periodRange("lastmonth", today, null, null)[0]);
        assertEquals("1405/06/31", FinFmt.periodRange("lastmonth", today, null, null)[1]);
        assertEquals("امروز", FinFmt.periodLabel("today"));
        // An unknown key falls back to a single day instead of an open-ended range.
        assertEquals(today, FinFmt.periodRange("nonsense", today, null, null)[0]);
    }

    @Test public void statusColoursKeepTheirMeaning() {
        assertEquals(FinUi.SUCCESS, FinUi.statusColor("تسویه"));
        assertEquals(FinUi.SUCCESS, FinUi.statusColor("balanced"));
        assertEquals(FinUi.WARNING, FinUi.statusColor("در انتظار"));
        assertEquals(FinUi.DANGER, FinUi.statusColor("سررسید گذشته"));
        assertEquals(FinUi.DANGER, FinUi.statusColor("مغایرت"));
        assertEquals(FinUi.MANAGER, FinUi.statusColor("مدیریت"));
        assertEquals(FinUi.MUTED, FinUi.statusColor(null));
    }

    @Test public void chartColoursMixPredictably() {
        int white = 0xFFFFFFFF, black = 0xFF000000;
        assertEquals(0xFFFFFFFF, FinCharts.mix(black, white, 1f));
        assertEquals(0xFF808080, FinCharts.mix(black, white, 0.5f) | 0xFF000000);
        assertEquals(0xFF112233, FinCharts.mix(0xFF112233, 0xFF112233, 0.7f));
        assertEquals(0x80FF0000, FinCharts.alpha(0xFFFF0000, 0x80));
    }

    @Test public void eventCodeIsShortAndStable() {
        String a = FinFmt.eventCode("Some database error 12345");
        assertEquals(6, a.length());
        assertEquals(a, FinFmt.eventCode("Some database error 12345"));
        assertTrue(!a.equals(FinFmt.eventCode("another error")));
    }

    @Test public void csvEscapesQuotes() {
        assertEquals("\"a\"\"b\"", FinFmt.csv("a\"b"));
        assertEquals("\"\"", FinFmt.csv(null));
    }

    @Test public void opKeyIsStableAndUnique() {
        String one = FinDb.opKey("cash", "settle", 12, "1405/07/11", 1500);
        String again = FinDb.opKey("cash", "settle", 12, "1405/07/11", 1500);
        String other = FinDb.opKey("cash", "settle", 12, "1405/07/11", 1501);
        assertEquals(one, again);
        assertTrue(!one.equals(other));
        assertTrue(one.startsWith("cash:settle:"));
    }
}

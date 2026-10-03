package ir.meelano.android.finance;

import java.util.Locale;

/**
 * Formatting rules for the finance app.
 *
 * Dates: Atiran stores Jalali dates as fixed {@code char(10)} text ("1405/07/11"); every comparison
 * in this app happens on the server against {@code dbo.ReturnDateServer()} — the database's own idea
 * of "today" — so the device clock can never move a due date. On the client the same conversion uses
 * the project's {@code MeelanoJalali} algorithm (jalaali-js), which is unit tested.
 *
 * Amounts: kept as {@code double} rial values as they come from the money columns; grouping is done
 * with the Latin-digit separator so the number copies correctly into e-mail, Excel and PDF.
 */
public final class FinFmt {
    private FinFmt() { }

    public static final String CURRENCY = "ریال";

    /** 1,234,567 — no decimals for money (the Atiran money columns carry scale 4 but whole rial). */
    public static String amount(double v) {
        boolean neg = v < 0;
        long abs = Math.round(Math.abs(v));
        String s = String.format(Locale.US, "%,d", abs);
        return (neg ? "-" : "") + s;
    }

    /** 1.2 م / 3.4 میلیارد for KPI tiles where the full number is too wide. */
    public static String compact(double v) {
        double abs = Math.abs(v);
        String sign = v < 0 ? "-" : "";
        if (abs >= 1_000_000_000_000d) return sign + trim(abs / 1_000_000_000_000d) + " همت";
        if (abs >= 1_000_000_000d) return sign + trim(abs / 1_000_000_000d) + " میلیارد";
        if (abs >= 1_000_000d) return sign + trim(abs / 1_000_000d) + " میلیون";
        if (abs >= 1_000d) return sign + trim(abs / 1_000d) + " هزار";
        return sign + String.format(Locale.US, "%,d", Math.round(abs));
    }

    private static String trim(double v) {
        String s = String.format(Locale.US, "%.1f", v);
        if (s.endsWith(".0")) s = s.substring(0, s.length() - 2);
        return s;
    }

    public static String amountWithCurrency(double v) { return amount(v) + " " + CURRENCY; }

    public static String count(long v) { return String.format(Locale.US, "%,d", v); }

    public static String percent(double a, double b) {
        if (b == 0) return "—";
        return String.format(Locale.US, "%.0f%%", (a / b) * 100d);
    }

    // ------------------------------------------------------------------ dates

    /** Jalali today according to the device (only used before the server answers). */
    public static String todayLocal() { return MeelanoJalali.format(MeelanoJalali.today()); }

    public static String addDays(String jalali, int days) { return MeelanoJalali.addDays(jalali, days); }

    public static int daysBetween(String fromJalali, String toJalali) {
        int a = MeelanoJalali.parse(fromJalali);
        int b = MeelanoJalali.parse(toJalali);
        if (a < 0 || b < 0) return Integer.MIN_VALUE;
        return b - a;
    }

    /** Positive when the date is in the past relative to today. */
    public static int daysAgo(String jalali, String today) { return daysBetween(jalali, today); }

    public static String weekday(String jalali) { return MeelanoJalali.weekday(jalali); }

    public static String monthName(String jalali) { return MeelanoJalali.monthName(jalali); }

    /** "1405/07/11" → "۱۱ مهر ۱۴۰۵" for headers. */
    public static String prettyDate(String jalali) {
        int day = MeelanoJalali.parse(jalali);
        if (day < 0) return jalali == null ? "" : jalali;
        int[] j = MeelanoJalali.fromDay(day);
        return faNumber(j[2]) + " " + MeelanoJalali.MONTHS[Math.max(0, Math.min(11, j[1] - 1))] + " " + faNumber(j[0]);
    }

    public static String shortDate(String jalali) {
        int day = MeelanoJalali.parse(jalali);
        if (day < 0) return jalali == null ? "" : jalali;
        int[] j = MeelanoJalali.fromDay(day);
        return String.format(Locale.US, "%02d/%02d", j[1], j[2]);
    }

    /** Persian digits for display only; stored values always stay Latin. */
    public static String faNumber(Object value) {
        String s = String.valueOf(value);
        StringBuilder b = new StringBuilder(s.length());
        for (char ch : s.toCharArray()) {
            if (ch >= '0' && ch <= '9') b.append((char) ('۰' + (ch - '0')));
            else b.append(ch);
        }
        return b.toString();
    }

    /** Period presets used by every report filter. */
    public static final String[] PERIOD_KEYS = {"today", "yesterday", "7d", "30d", "month", "lastmonth", "custom"};
    public static final String[] PERIOD_LABELS = {"امروز", "دیروز", "۷ روز", "۳۰ روز", "این ماه", "ماه گذشته", "بازه دلخواه"};

    public static String[] periodRange(String key, String today, String customFrom, String customTo) {
        if (key == null) key = "today";
        switch (key) {
            case "yesterday": { String d = addDays(today, -1); return new String[]{d, d}; }
            case "7d": return new String[]{addDays(today, -6), today};
            case "30d": return new String[]{addDays(today, -29), today};
            case "month": return new String[]{MeelanoJalali.monthStart(today), today};
            case "lastmonth": {
                String firstThis = MeelanoJalali.monthStart(today);
                String lastPrev = addDays(firstThis, -1);
                return new String[]{MeelanoJalali.monthStart(lastPrev), lastPrev};
            }
            case "custom": return new String[]{customFrom == null || customFrom.isEmpty() ? today : customFrom,
                    customTo == null || customTo.isEmpty() ? today : customTo};
            default: return new String[]{today, today};
        }
    }

    public static String periodLabel(String key) {
        for (int i = 0; i < PERIOD_KEYS.length; i++) if (PERIOD_KEYS[i].equals(key)) return PERIOD_LABELS[i];
        return PERIOD_LABELS[0];
    }

    /**
     * Short event code for an unexpected error: support can correlate a report with the log without
     * the raw SQL text ever being shown on screen (SQL errors must never reach the operator).
     */
    public static String eventCode(String detail) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] d = md.digest((detail == null ? "" : detail).getBytes("UTF-8"));
            StringBuilder b = new StringBuilder();
            for (int i = 0; i < 3; i++) b.append(String.format(Locale.US, "%02X", d[i]));
            return b.toString();
        } catch (Exception e) {
            return "——";
        }
    }

    /** CSV cell: Persian text stays readable in Excel, quotes are escaped. */
    public static String csv(String v) {
        if (v == null) return "";
        String s = v.replace("\"", "\"\"");
        return "\"" + s + "\"";
    }
}

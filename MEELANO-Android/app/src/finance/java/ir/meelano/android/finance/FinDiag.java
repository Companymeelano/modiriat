package ir.meelano.android.finance;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Build;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * «آتیران مالی» — connectivity and environment doctor.
 *
 * One tap answers the only questions that matter when an operator says "it does not connect":
 * is the phone online, does the server name resolve, is the SQL port open, does the database accept
 * the application login, and does it answer with its own Jalali date. Every step is timed and every
 * failure carries its exact exception class and message — but never a credential.
 *
 * {@link #collect} blocks (network + database) and must be called from a background thread; the
 * caller receives ready-to-show lines plus the same report as plain text for copying or sharing.
 */
final class FinDiag {

    /** One checked item of the report. */
    static final class Line {
        final boolean ok;
        final boolean fatal;
        final String title;
        final String detail;
        Line(boolean ok, boolean fatal, String title, String detail) {
            this.ok = ok;
            this.fatal = fatal;
            this.title = title;
            this.detail = detail;
        }
        String glyph() { return ok ? "✓" : (fatal ? "✗" : "▲"); }
    }

    static final class Report {
        final List<Line> lines = new ArrayList<>();
        String text = "";
        boolean allOk = true;
        int failures = 0;
    }

    private FinDiag() { }

    static Report collect(Context ctx, FinDb db) {
        Report r = new Report();
        long started = System.currentTimeMillis();

        r.lines.add(app(ctx));
        r.lines.add(device());
        r.lines.add(memory());
        r.lines.add(network(ctx));
        r.lines.add(resolve());
        r.lines.add(tcp());
        r.lines.add(jdbc(db));
        r.lines.add(schema(db));
        r.lines.add(accounts(db));

        int ok = 0;
        for (Line l : r.lines) {
            if (l == null) continue;
            if (l.ok) ok++;
            else if (l.fatal) r.allOk = false;
            if (!l.ok && l.fatal) r.failures++;
            r.text += l.glyph() + "  " + l.title + " — " + l.detail + "\n";
        }
        r.text += "\nمجموع زمان بررسی: " + (System.currentTimeMillis() - started) + " میلی‌ثانیه · "
                + FinFmt.faNumber(ok) + " از " + FinFmt.faNumber(r.lines.size()) + " مورد موفق\n";
        try {
            FinCrash.log(ctx, "diagnostics", (r.allOk ? "ok" : "issues=" + r.failures));
        } catch (Throwable ignored) { }
        return r;
    }

    // ------------------------------------------------------------------ steps

    private static Line app(Context ctx) {
        try {
            PackageInfo pi = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0);
            return new Line(true, false, "برنامه", ctx.getPackageName() + " · نسخه " + pi.versionName
                    + " (" + pi.versionCode + ")");
        } catch (Exception e) {
            return new Line(false, false, "برنامه", e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    private static Line device() {
        return new Line(true, false, "دستگاه", Build.MANUFACTURER + " " + Build.MODEL + " · Android "
                + Build.VERSION.RELEASE + " (API " + Build.VERSION.SDK_INT + ") · " + Build.SUPPORTED_ABIS[0]);
    }

    private static Line memory() {
        Runtime rt = Runtime.getRuntime();
        long max = rt.maxMemory() / (1024 * 1024), free = rt.freeMemory() / (1024 * 1024);
        return new Line(true, false, "حافظه", free + " از " + max + " مگابایت آزاد");
    }

    private static Line network(Context ctx) {
        try {
            ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm == null) return new Line(false, true, "اینترنت", "سرویس شبکه در دسترس نیست");
            Network n = cm.getActiveNetwork();
            if (n == null) return new Line(false, true, "اینترنت", "هیچ شبکهٔ فعالی وجود ندارد — داده همراه یا وای‌فای را روشن کنید");
            NetworkCapabilities caps = cm.getNetworkCapabilities(n);
            if (caps == null) return new Line(false, true, "اینترنت", "شبکهٔ فعال مشخص نیست");
            boolean internet = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
            boolean validated = Build.VERSION.SDK_INT < 23 || caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
            String kind = caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ? "وای‌فای"
                    : caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ? "داده همراه"
                    : caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) ? "اترنت" : "دیگر";
            return new Line(internet, true, "اینترنت", kind + (validated ? " · متصل و تأییدشده" : " · متصل (تأییدنشده)"));
        } catch (Throwable t) {
            return new Line(false, false, "اینترنت", t.getClass().getSimpleName() + ": " + t.getMessage());
        }
    }

    private static Line resolve() {
        long t0 = System.currentTimeMillis();
        try {
            InetAddress[] all = InetAddress.getAllByName(FinEnv.host());
            return new Line(true, false, "نام سرور (DNS)", FinEnv.host() + " → " + all[0].getHostAddress()
                    + " · " + (System.currentTimeMillis() - t0) + "ms");
        } catch (Exception e) {
            return new Line(false, true, "نام سرور (DNS)", FinEnv.host() + " پیدا نشد · "
                    + e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    private static Line tcp() {
        long t0 = System.currentTimeMillis();
        Socket s = new Socket();
        try {
            s.connect(new InetSocketAddress(FinEnv.host(), FinEnv.PORT), 9000);
            return new Line(true, false, "پورت سرور " + FinEnv.host() + ":" + FinEnv.PORT,
                    "باز است · " + (System.currentTimeMillis() - t0) + "ms");
        } catch (Exception e) {
            return new Line(false, true, "پورت سرور " + FinEnv.host() + ":" + FinEnv.PORT,
                    "در دسترس نیست · " + (System.currentTimeMillis() - t0) + "ms · "
                            + e.getClass().getSimpleName() + ": " + e.getMessage());
        } finally {
            try { s.close(); } catch (Exception ignored) { }
        }
    }

    private static Line jdbc(FinDb db) {
        long t0 = System.currentTimeMillis();
        try (Connection c = db.openDirect()) {
            return new Line(true, false, "ورود به SQL Server (jtds)",
                    "برقرار شد · " + (System.currentTimeMillis() - t0) + "ms · پایگاه " + FinEnv.database());
        } catch (Throwable t) {
            return new Line(false, true, "ورود به SQL Server (jtds)",
                    "ناموفق · " + (System.currentTimeMillis() - t0) + "ms · "
                            + t.getClass().getSimpleName() + ": " + String.valueOf(t.getMessage()));
        }
    }

    private static Line schema(FinDb db) {
        try (Connection c = db.openDirect()) {
            String name = FinDb.scalarText(c, "SELECT CONVERT(nvarchar(60), DB_NAME()) AS v");
            String when = FinDb.scalarText(c, "SELECT CONVERT(nvarchar(30), SYSDATETIME(), 120) AS v");
            String today = FinQueries.serverToday(c);
            return new Line(true, false, "پایگاه داده",
                    name + " · ساعت سرور " + when + " · تاریخ سرور " + today);
        } catch (Throwable t) {
            return new Line(false, true, "پایگاه داده",
                    t.getClass().getSimpleName() + ": " + String.valueOf(t.getMessage()));
        }
    }

    private static Line accounts(FinDb db) {
        try (Connection c = db.openDirect()) {
            String visitors = String.valueOf(FinDb.count(c,
                    "SELECT COUNT(*) AS v FROM dbo.visitors WITH (NOLOCK) WHERE Username IS NOT NULL "
                            + "AND LTRIM(RTRIM(Username)) <> ''"));
            String sysUsers = "—";
            try {
                sysUsers = String.valueOf(FinDb.count(c,
                        "SELECT COUNT(*) AS v FROM dbo.sys_users WITH (NOLOCK)"));
            } catch (Throwable ignored) { }
            return new Line(true, false, "جدول کاربران",
                    "visitors " + FinFmt.faNumber(visitors) + " کاربر · sys_users " + sysUsers);
        } catch (Throwable t) {
            return new Line(false, false, "جدول کاربران",
                    t.getClass().getSimpleName() + ": " + String.valueOf(t.getMessage()));
        }
    }

    /** One-line summary for the header chip. */
    static String summary(Report r) {
        if (r == null) return "";
        if (r.allOk) return "همهٔ بررسی‌ها موفق بود";
        return String.format(Locale.US, "%d مورد ناموفق", r.failures);
    }
}

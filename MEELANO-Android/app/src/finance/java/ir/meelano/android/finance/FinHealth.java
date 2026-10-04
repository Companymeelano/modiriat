package ir.meelano.android.finance;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

/**
 * «آتیران مالی» — the health walk that runs on the first screen of the application.
 *
 * The operator sees it as a checklist that fills in front of their eyes, but underneath it is a real
 * end-to-end verification: the phone's connectivity, the server name, the SQL port, the application
 * login on the database, the database's own Jalali date and a sample read of the finance tables.
 * Every step carries its own duration, and a step that fails carries the exact exception text — which
 * is why a broken installation can be diagnosed from a screenshot instead of a support session.
 *
 * Checks run on one background thread, in order, and stop at the first critical failure (nothing
 * after a closed port can succeed). Callbacks are always delivered on the UI thread.
 */
final class FinHealth {

    static final int PENDING = 0;
    static final int RUNNING = 1;
    static final int OK = 2;
    static final int WARN = 3;   // checked, not perfect, but the app can continue
    static final int FAIL = 4;

    /** One line of the checklist. */
    static final class Check {
        final String title;
        final boolean critical;
        int state = PENDING;
        String detail = "";
        long ms;

        Check(String title, boolean critical) {
            this.title = title;
            this.critical = critical;
        }

        String glyph() {
            switch (state) {
                case RUNNING: return "…";
                case OK: return "✓";
                case WARN: return "▲";
                case FAIL: return "✗";
                default: return "○";
            }
        }
    }

    interface Listener {
        void onCheckStart(int index, Check check);
        void onCheckDone(int index, Check check);
        /** All checks finished; {@code ok} is false when a critical one failed. */
        void onChecksFinished(boolean ok, int doneIndex);
    }

    private FinHealth() { }

    /** The checklist of the startup screen. */
    static List<Check> checklist() {
        List<Check> list = new ArrayList<>();
        list.add(new Check("آماده‌سازی رابط کاربری", false));
        list.add(new Check("اینترنت گوشی", true));
        list.add(new Check("یافتن نام سرور (DNS)", true));
        list.add(new Check("پورت سرور " + FinEnv.PORT, true));
        list.add(new Check("ورود به SQL Server", true));
        list.add(new Check("پایگاه داده و تاریخ سرور", true));
        list.add(new Check("جدول‌های مالی (نمونه‌خوانی)", false));
        return list;
    }

    /**
     * Runs the walk. The caller may cancel it by interrupting the thread; the callbacks simply stop.
     */
    static Thread run(final Context ctx, final FinDb db, final List<Check> checks, final Listener listener) {
        final Handler main = new Handler(Looper.getMainLooper());
        Thread worker = new Thread(() -> {
            boolean allOk = true;
            int doneIndex = -1;
            Connection conn = null;
            for (int i = 0; i < checks.size(); i++) {
                if (Thread.currentThread().isInterrupted()) return;
                final Check c = checks.get(i);
                final int index = i;
                c.state = RUNNING;
                c.detail = "";
                post(main, () -> listener.onCheckStart(index, c));
                long started = System.currentTimeMillis();
                try {
                    String detail;
                    switch (i) {
                        case 0: detail = "نسخهٔ " + FinSession.appVersion() + " · " + Build.MANUFACTURER + " " + Build.MODEL;
                            break;
                        case 1: detail = networkDetail(ctx); break;
                        case 2: detail = dnsDetail(); break;
                        case 3: detail = tcpDetail(); break;
                        case 4: conn = db.openDirect(); detail = "برقرار شد · پایگاه " + FinEnv.database(); break;
                        case 5: detail = databaseDetail(conn); break;
                        default: detail = tablesDetail(conn); break;
                    }
                    c.detail = detail;
                    c.state = OK;
                } catch (Throwable t) {
                    c.detail = shortError(t);
                    c.state = c.critical && i >= 1 ? FAIL : WARN;
                    if (c.state == FAIL) allOk = false;
                    FinCrash.log(ctx, "health-failed", checks.get(i).title + ": " + t.getClass().getName());
                }
                c.ms = System.currentTimeMillis() - started;
                if (i == 0) c.ms = 0;
                doneIndex = i;
                post(main, () -> listener.onCheckDone(index, c));
                if (c.state == FAIL) {
                    // A closed port or a refused login makes the following checks meaningless.
                    if (conn != null) { try { conn.close(); } catch (Throwable ignored) { } }
                    final int last = i;
                    post(main, () -> listener.onChecksFinished(false, last));
                    return;
                }
            }
            if (conn != null) { try { conn.close(); } catch (Throwable ignored) { } }
            final boolean ok = allOk;
            final int last = doneIndex;
            post(main, () -> listener.onChecksFinished(ok, last));
        }, "fin-health");
        worker.setDaemon(true);
        worker.start();
        return worker;
    }

    /** Every callback is wrapped: a painting error must never take the application down. */
    private static void post(Handler main, Runnable work) {
        main.post(() -> {
            try {
                work.run();
            } catch (Throwable t) {
                try { FinCrash.log(FinApp.context(), "health-ui", t.getClass().getName()); } catch (Throwable ignored) { }
                FinDb.UiError reporter = FinDb.reporter();
                if (reporter != null) {
                    try { reporter.onUiError(t); } catch (Throwable ignored) { }
                }
            }
        });
    }

    // ------------------------------------------------------------------ individual checks

    private static String networkDetail(Context ctx) {
        ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) throw new IllegalStateException("سرویس شبکه در دسترس نیست");
        Network n = cm.getActiveNetwork();
        if (n == null) throw new IllegalStateException("هیچ شبکه‌ای فعال نیست — داده همراه یا وای‌فای را روشن کنید");
        NetworkCapabilities caps = cm.getNetworkCapabilities(n);
        if (caps == null) throw new IllegalStateException("شبکهٔ فعال مشخص نیست");
        boolean internet = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
        String kind = caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ? "وای‌فای"
                : caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ? "داده همراه"
                : caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) ? "اترنت" : "شبکه";
        if (!internet) throw new IllegalStateException(kind + " بدون دسترسی به اینترنت");
        return kind + " · متصل";
    }

    private static String dnsDetail() throws Exception {
        long t0 = System.currentTimeMillis();
        InetAddress addr = InetAddress.getByName(FinEnv.host());
        return addr.getHostAddress() + " · " + (System.currentTimeMillis() - t0) + "ms";
    }

    private static String tcpDetail() throws Exception {
        long t0 = System.currentTimeMillis();
        Socket s = new Socket();
        try {
            s.connect(new InetSocketAddress(FinEnv.host(), FinEnv.PORT), 9_000);
            return "باز است · " + (System.currentTimeMillis() - t0) + "ms";
        } finally {
            try { s.close(); } catch (Exception ignored) { }
        }
    }

    private static String databaseDetail(Connection c) throws Exception {
        String name = FinDb.scalarText(c, "SELECT CONVERT(nvarchar(60), DB_NAME()) AS v");
        String today = FinQueries.serverToday(c);
        return name + " · تاریخ سرور " + today;
    }

    private static String tablesDetail(Connection c) throws Exception {
        StringBuilder b = new StringBuilder();
        String[][] tables = {
                {"فاکتور فروش", "SELECT TOP 1 1 AS v FROM dbo.sailfact WITH (NOLOCK)"},
                {"قبض دریافت", "SELECT TOP 1 1 AS v FROM dbo.dar WITH (NOLOCK)"},
                {"چک دریافتی", "SELECT TOP 1 1 AS v FROM dbo.getchk WITH (NOLOCK)"},
                {"گردش مشتری", "SELECT TOP 1 1 AS v FROM dbo.cust_act WITH (NOLOCK)"},
                {"بانک", "SELECT TOP 1 1 AS v FROM dbo.BANK WITH (NOLOCK)"}
        };
        int ok = 0;
        for (String[] t : tables) {
            try {
                FinDb.value(c, t[1]);
                ok++;
            } catch (Throwable ignored) {
                if (b.length() > 0) b.append(" · ");
                b.append("بدون دسترسی: ").append(t[0]);
            }
        }
        return ok + " از " + tables.length + " جدول خوانده شد" + (b.length() == 0 ? "" : " · " + b);
    }

    /** A one-line, operator-safe error: class + message, never a credential and never a stack. */
    static String shortError(Throwable t) {
        if (t == null) return "خطای نامشخص";
        String message = t.getMessage() == null ? "" : t.getMessage().replace('\n', ' ').trim();
        if (message.length() > 110) message = message.substring(0, 110) + "…";
        String name = t.getClass().getSimpleName();
        String text = name + (message.isEmpty() ? "" : ": " + message);
        Throwable cause = t.getCause();
        if (cause != null && cause != t && cause.getMessage() != null) {
            String cm = cause.getMessage().replace('\n', ' ').trim();
            if (cm.length() > 70) cm = cm.substring(0, 70) + "…";
            text = text + " (" + cause.getClass().getSimpleName() + ": " + cm + ")";
        }
        return text;
    }
}

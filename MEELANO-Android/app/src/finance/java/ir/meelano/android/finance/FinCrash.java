package ir.meelano.android.finance;

import android.content.Context;
import android.os.Build;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * «آتیران مالی» — startup log and crash capture.
 *
 * A phone in the field can never be debugged with a cable, so this app records everything that
 * matters on the device itself:
 *
 *  • every startup step (with millisecond timestamps) goes to {@code fin-boot.txt},
 *  • an uncaught exception is written to {@code fin-crash.txt} before the process dies, and is then
 *    shown at the top of the next launch instead of a silent blank screen,
 *  • the operator can read or copy both files from the «تشخیص و پشتیبانی» panel and send them.
 *
 * Nothing else is written: no credential, no user row and no financial value ever reaches these
 * files — only step names, device facts and exception class/message.
 */
public final class FinCrash {

    private static final String CRASH_FILE = "fin-crash.txt";
    private static final String BOOT_FILE = "fin-boot.txt";
    private static final int MAX_CHARS = 24000;
    private static volatile boolean installed = false;
    private static final String STAMP = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date());

    private FinCrash() { }

    /** Installs the process-wide handler once; the previous handler still runs, so Android behaves as usual. */
    public static synchronized void install(final Context ctx) {
        if (installed) return;
        installed = true;
        final Thread.UncaughtExceptionHandler previous = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler((thread, error) -> {
            try {
                write(ctx, CRASH_FILE, describe(error));
            } catch (Throwable ignored) {
                // A crash handler must never crash.
            }
            if (previous != null) previous.uncaughtException(thread, error);
        });
    }

    /** One startup step; {@code detail} may be a duration or a result, never a credential. */
    public static void log(Context ctx, String step, String detail) {
        try {
            String line = String.format(Locale.US, "%s.%03d  %s%s%n",
                    new SimpleDateFormat("HH:mm:ss", Locale.US).format(new Date()),
                    System.currentTimeMillis() % 1000L,
                    step,
                    detail == null || detail.isEmpty() ? "" : "  —  " + detail);
            append(ctx, BOOT_FILE, line);
        } catch (Throwable ignored) { }
    }

    public static boolean hasCrash(Context ctx) {
        String s = read(ctx, CRASH_FILE);
        return s != null && !s.trim().isEmpty();
    }

    public static String lastCrash(Context ctx) { return read(ctx, CRASH_FILE); }

    public static String bootLog(Context ctx) { return read(ctx, BOOT_FILE); }

    public static void clearCrash(Context ctx) {
        try {
            File f = new File(ctx.getFilesDir(), CRASH_FILE);
            if (f.exists()) {
                try (FileOutputStream out = new FileOutputStream(f, false)) {
                    out.write(new byte[0]);
                }
            }
        } catch (Throwable ignored) { }
    }

    public static void clearBoot(Context ctx) { write(ctx, BOOT_FILE, ""); }

    /** Short, quotable code an operator can read to support (never derived from a credential). */
    public static String eventCode(Throwable t) {
        String seed = t == null ? "?" : t.getClass().getName() + ":" + String.valueOf(t.getMessage());
        long h = 17;
        for (int i = 0; i < seed.length(); i++) h = h * 31L + seed.charAt(i);
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            b.append("0123456789ABCDEFGHJKLMNPQRSTUVWXYZ".charAt((int) Math.abs(h >> (i * 5)) % 34));
        }
        return b.toString();
    }

    /** Human-readable description of a throwable: class, message and the first frames of the stack. */
    public static String describe(Throwable t) {
        if (t == null) return "";
        StringBuilder b = new StringBuilder();
        b.append("== ").append(STAMP).append(" · ").append(Build.MANUFACTURER).append(' ').append(Build.MODEL)
                .append(" · Android ").append(Build.VERSION.RELEASE).append(" (API ").append(Build.VERSION.SDK_INT)
                .append(") · کد رویداد ").append(eventCode(t)).append('\n');
        b.append(t.getClass().getName()).append(": ").append(String.valueOf(t.getMessage())).append('\n');
        try {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            t.printStackTrace(pw);
            pw.flush();
            String stack = sw.toString();
            String[] lines = stack.split("\n");
            for (int i = 0; i < lines.length && i < 24; i++) {
                if (lines[i].startsWith("\tat ir.meelano") || i == 0) b.append(lines[i]).append('\n');
            }
        } catch (Throwable ignored) { }
        Throwable cause = t.getCause();
        if (cause != null && cause != t) {
            b.append("سبب: ").append(cause.getClass().getName()).append(": ").append(String.valueOf(cause.getMessage())).append('\n');
        }
        return b.toString();
    }

    // ------------------------------------------------------------------ file helpers

    private static void append(Context ctx, String name, String text) {
        try {
            String existing = read(ctx, name);
            String merged = (existing == null ? "" : existing) + text;
            if (merged.length() > MAX_CHARS) merged = merged.substring(merged.length() - MAX_CHARS);
            write(ctx, name, merged);
        } catch (Throwable ignored) { }
    }

    private static void write(Context ctx, String name, String text) {
        try (FileOutputStream out = new FileOutputStream(new File(ctx.getFilesDir(), name), false)) {
            out.write(text.getBytes(StandardCharsets.UTF_8));
            out.flush();
        } catch (Throwable ignored) { }
    }

    private static String read(Context ctx, String name) {
        try (InputStream in = new FileInputStream(new File(ctx.getFilesDir(), name))) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) > 0) bos.write(buf, 0, n);
            return new String(bos.toByteArray(), StandardCharsets.UTF_8);
        } catch (Throwable e) {
            return null;
        }
    }
}

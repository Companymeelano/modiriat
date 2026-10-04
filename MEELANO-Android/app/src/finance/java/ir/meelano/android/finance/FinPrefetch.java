package ir.meelano.android.finance;

import org.json.JSONArray;

import java.sql.Connection;

/**
 * The post-login preload: right after the sign-in succeeds, every finance screen's data is read
 * <b>from the database</b> in one background pass and kept in the memory cache.
 *
 * <p>Each screen exposes the very same {@code queryPayload(Connection …)} method its own refresh uses, so
 * what the preload stores and what the screen would have read are identical by construction — a
 * screen can never show something that was not fetched from the database. The pass runs one query
 * after another with a short pause, so a low-memory phone never sees the whole load at once, and it
 * never starts before the desk is on screen.</p>
 */
public final class FinPrefetch {

    /** One screen of the pass, reported to the caller for the step trail and the support report. */
    public interface Step { void done(String label, int rows, long ms, String error); }

    private interface Fetch { JSONArray run() throws Exception; }

    private FinPrefetch() { }

    /**
     * Reads every module from the database into the cache. Runs on the caller's background thread and
     * never throws: a module that cannot be read is reported and the rest continue.
     */
    public static void warmAll(FinDb db, String from, String to, String today, Step step) {
        String f = from == null || from.isEmpty() ? today : from;
        String t = to == null || to.isEmpty() ? today : to;
        try (Connection c = db.open()) {
            db.ensureSchema(c);
            // Let the desk and its home screen paint first; if it already read the home payload, the
            // freshness check below simply skips it.
            if (!pause(900L)) return;
            warm(db, "home:" + today, 55_000L, () -> FinScreenHome.queryPayload(c), step, "خانه");
            if (!pause()) return;
            warm(db, "banks", 290_000L, () -> FinScreenBanks.queryPayload(c), step, "بانک‌ها");
            if (!pause()) return;
            warm(db, "checks:" + FinQueries.MODE_ALL + ":" + f + ":in", 110_000L,
                    () -> FinScreenCheques.queryPayload(c, f, t, FinQueries.MODE_ALL, false), step, "چک‌های دریافتی");
            if (!pause()) return;
            warm(db, "recv:" + today, 110_000L, () -> FinScreenReceivables.queryPayload(c), step, "مطالبات");
            if (!pause()) return;
            warm(db, "problems:" + f + ":" + t, 55_000L, () -> FinScreenProblems.queryPayload(c, f, t), step, "کارهای باز");
            if (!pause()) return;
            warm(db, "cash:" + f + ":" + t, 85_000L, () -> FinScreenCash.queryPayload(c, f, t), step, "صندوق");
            if (!pause()) return;
            warm(db, "pos:" + f + ":" + t, 85_000L, () -> FinScreenPos.queryPayload(c, f, t), step, "POS");
            if (!pause()) return;
            warm(db, "sales:" + f + ":" + t, 85_000L, () -> FinScreenSales.queryPayload(c, f, t), step, "فروش و وصول");
            if (!pause()) return;
            warm(db, "daily:" + today, 55_000L, () -> FinScreenDaily.queryPayload(c), step, "گزارش روز");
            if (!pause()) return;
            warm(db, "recon:" + f + ":" + t, 55_000L, () -> FinScreenRecon.queryPayload(c, f, t), step, "مغایرت بانکی");
            if (!pause()) return;
            warm(db, "more:" + today, 110_000L, () -> FinScreenMore.queryPayload(c), step, "بیشتر و رخدادها");
        } catch (Throwable e) {
            // The preload is an optimisation: if the connection itself fails, the screens still read
            // on their own and show their own error card.
            step.done("اتصال پیش‌بارگذاری", 0, 0, FinDb.safeMessage(e));
        }
    }

    private static void warm(FinDb db, String key, long ttl, Fetch fetch, Step step, String label) {
        if (db.fresh(key, ttl)) {
            step.done(label, -1, 0, null);   // already fresh in the cache
            return;
        }
        long t0 = System.currentTimeMillis();
        try {
            JSONArray rows = fetch.run();
            db.warm(key, rows);
            step.done(label, rows.length(), System.currentTimeMillis() - t0, null);
        } catch (Throwable e) {
            step.done(label, 0, System.currentTimeMillis() - t0, FinDb.safeMessage(e));
        }
    }

    /** Small pause between two modules: the load is spread over seconds instead of one spike. */
    private static boolean pause() { return pause(140L); }

    /** False when the worker was interrupted: the pass stops instead of hammering a closing app. */
    private static boolean pause(long ms) {
        try {
            Thread.sleep(ms);
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
}

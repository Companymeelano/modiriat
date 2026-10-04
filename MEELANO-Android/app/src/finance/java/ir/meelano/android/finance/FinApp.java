package ir.meelano.android.finance;

import android.app.Application;
import android.content.Context;

/** Minimal application holder so non-Activity helpers can reach a Context safely. */
public final class FinApp extends Application {
    private static Context appContext;

    @Override public void onCreate() {
        super.onCreate();
        appContext = getApplicationContext();
    }

    public static Context context() { return appContext; }

    /**
     * Makes sure the application context is available. The finance manifest points at this class, so
     * {@code onCreate} normally sets it; the activity calls this as a belt-and-braces guard for the
     * case where a helper runs before the application object is created (never seen in practice, but
     * a null context would silently hide the version number).
     */
    static void attach(Context context) {
        if (appContext == null && context != null) appContext = context.getApplicationContext();
    }
}

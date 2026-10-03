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
}

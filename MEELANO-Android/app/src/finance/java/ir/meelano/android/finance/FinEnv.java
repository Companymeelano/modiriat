package ir.meelano.android.finance;

/**
 * «آتیران مالی» — server environment.
 *
 * The database address and credentials are the ones the Meelano Android app has always used: kept
 * XOR-obfuscated as integer arrays and never written to the source, the APK strings, a log or the
 * repository in clear text. {@code FinEnvTest} proves these arrays decode to exactly the same values
 * the shared {@code ir.meelano.android.MainActivity} uses, so the two cannot drift apart.
 *
 * No credential is ever logged: {@link #describe()} returns a masked description only.
 */
public final class FinEnv {
    private FinEnv() { }

    /** XOR key of the obfuscation (same as the rest of the project). */
    public static final int KEY = 73;
    public static final int PORT = 1433;

    private static final int[] HOST = {122, 126, 103, 120, 125, 122, 103, 120, 125, 126, 103, 120, 112};
    private static final int[] USER = {8, 45, 36, 32, 39, 8, 39};
    private static final int[] PASS = {26, 61, 9, 27, 123, 121, 123, 123, 109};
    private static final int[] DB = {8, 61, 32, 59, 40, 39, 123};

    private static String decode(int[] data) {
        StringBuilder b = new StringBuilder(data.length);
        for (int v : data) b.append((char) (v ^ KEY));
        return b.toString();
    }

    public static String host() { return decode(HOST); }
    public static String database() { return decode(DB); }
    static String user() { return decode(USER); }
    static String password() { return decode(PASS); }

    /** Never exposes the address in full. */
    public static String describe() {
        String h = host();
        int dot = h.indexOf('.');
        String masked = dot > 0 ? h.substring(0, Math.min(3, dot)) + "•••" + h.substring(h.lastIndexOf('.')) : "•••";
        return "SQL Server " + masked + ":" + PORT + "/" + database();
    }
}

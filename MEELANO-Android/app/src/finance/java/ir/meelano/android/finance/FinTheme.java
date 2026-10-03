package ir.meelano.android.finance;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * The four enterprise themes of «آتیران مالی».
 *
 * The palette is data, not code: {@link FinUi} reads one of these objects and every screen inherits
 * it, so switching the theme recolours the whole app without touching a single screen. Status colours
 * (success / info / warning / danger / management) stay semantic and identical in all four themes —
 * the theme only changes surfaces, strokes and the accent, never the meaning of a colour.
 *
 *  • Midnight Finance  — obsidian blue surfaces with gold accents (default)
 *  • Obsidian Gold     — near-black with a stronger gold accent
 *  • Platinum Finance  — light platinum surfaces for bright offices
 *  • Executive Finance — deep navy-teal with a bronze accent
 */
public final class FinTheme {

    public static final String KEY_MIDNIGHT = "midnight";
    public static final String KEY_OBSIDIAN = "obsidian";
    public static final String KEY_PLATINUM = "platinum";
    public static final String KEY_EXECUTIVE = "executive";

    public final String key;
    public final String label;
    public final String labelFa;
    public final boolean light;

    public final int bg;
    public final int surface;
    public final int surface2;
    public final int surface3;
    public final int stroke;
    public final int text;
    public final int textDim;
    public final int textFaint;
    public final int gold;
    public final int goldSoft;
    public final int silver;
    public final int radius;

    private FinTheme(String key, String label, String labelFa, boolean light, int bg, int surface, int surface2,
                     int surface3, int stroke, int text, int textDim, int textFaint, int gold, int goldSoft,
                     int silver, int radius) {
        this.key = key;
        this.label = label;
        this.labelFa = labelFa;
        this.light = light;
        this.bg = bg;
        this.surface = surface;
        this.surface2 = surface2;
        this.surface3 = surface3;
        this.stroke = stroke;
        this.text = text;
        this.textDim = textDim;
        this.textFaint = textFaint;
        this.gold = gold;
        this.goldSoft = goldSoft;
        this.silver = silver;
        this.radius = radius;
    }

    public static final FinTheme MIDNIGHT = new FinTheme(KEY_MIDNIGHT, "Midnight Finance", "نیمهشب مالی", false,
            0xFF080B14, 0xFF101725, 0xFF162032, 0xFF1D2A40, 0xFF25334C, 0xFFF2F5FA, 0xFF9BA8BF, 0xFF6D7A93,
            0xFFD4AF37, 0xFFE8D48B, 0xFFC9CFDA, 16);

    public static final FinTheme OBSIDIAN = new FinTheme(KEY_OBSIDIAN, "Obsidian Gold", "اُبسیدیان طلایی", false,
            0xFF050506, 0xFF0E0E11, 0xFF15151A, 0xFF1E1E25, 0xFF2A2A33, 0xFFF6F3EC, 0xFFA79F92, 0xFF7A7368,
            0xFFD9A93C, 0xFFF0D68C, 0xFFCFC8BC, 16);

    public static final FinTheme PLATINUM = new FinTheme(KEY_PLATINUM, "Platinum Finance", "پلاتینیوم مالی", true,
            0xFFEEF1F6, 0xFFFFFFFF, 0xFFF6F8FC, 0xFFE9EDF5, 0xFFD3DAE6, 0xFF0F1726, 0xFF56637A, 0xFF8A94A8,
            0xFF2B4C8C, 0xFF5B7BBF, 0xFF41506B, 16);

    public static final FinTheme EXECUTIVE = new FinTheme(KEY_EXECUTIVE, "Executive Finance", "مدیریت ارشد", false,
            0xFF07131A, 0xFF0D1E27, 0xFF122833, 0xFF183340, 0xFF21414F, 0xFFEDF4F9, 0xFF93A7B8, 0xFF66798A,
            0xFFC89B52, 0xFFE4C48D, 0xFFBCC9D4, 16);

    public static FinTheme[] all() {
        return new FinTheme[]{MIDNIGHT, OBSIDIAN, PLATINUM, EXECUTIVE};
    }

    public static FinTheme of(String key) {
        if (key != null) {
            for (FinTheme t : all()) if (t.key.equals(key)) return t;
        }
        return MIDNIGHT;
    }

    private static SharedPreferences prefs(Context ctx) {
        return ctx.getSharedPreferences("atiran_finance", Context.MODE_PRIVATE);
    }

    public static FinTheme get(Context ctx) {
        try {
            return of(prefs(ctx).getString("fin_theme", KEY_MIDNIGHT));
        } catch (Exception e) {
            return MIDNIGHT;
        }
    }

    public static void set(Context ctx, String key) {
        prefs(ctx).edit().putString("fin_theme", of(key).key).apply();
    }
}

package com.silo.statusclock;

import android.content.Context;
import android.content.SharedPreferences;

/** Persisted user settings for the overlay clock. */
final class Prefs {
    static final String NAME = "clock_prefs";
    static final String ENABLED = "enabled";
    static final String SHOW_SECONDS = "show_seconds";
    static final String SHOW_DATE = "show_date";
    static final String SIZE_SP = "size_sp";
    static final String X_DP = "x_dp";
    static final String Y_DP = "y_dp";
    static final String DATE_SIZE_SP = "date_size_sp";
    static final String DATE_X_DP = "date_x_dp";
    static final String DATE_Y_DP = "date_y_dp";
    static final String COLOR_MODE = "color_mode";
    static final int COLOR_AUTO = 0, COLOR_WHITE = 1, COLOR_BLACK = 2;

    private Prefs() {}

    static SharedPreferences get(Context c) {
        return c.getSharedPreferences(NAME, Context.MODE_PRIVATE);
    }

    static boolean enabled(Context c) { return get(c).getBoolean(ENABLED, false); }
    static boolean showSeconds(Context c) { return get(c).getBoolean(SHOW_SECONDS, true); }
    static boolean showDate(Context c) { return get(c).getBoolean(SHOW_DATE, true); }
    static int sizeSp(Context c) { return get(c).getInt(SIZE_SP, 13); }
    static int xDp(Context c) { return get(c).getInt(X_DP, 16); }
    static int yDp(Context c) { return get(c).getInt(Y_DP, 6); }
    static int dateSizeSp(Context c) { return get(c).getInt(DATE_SIZE_SP, sizeSp(c)); }
    static int dateXDp(Context c) { return get(c).getInt(DATE_X_DP, xDp(c) + 80); }
    static int dateYDp(Context c) { return get(c).getInt(DATE_Y_DP, yDp(c)); }
    /** Pins the date's starting position once, so moving the time later doesn't drag the date. */
    static void ensureDateDefaults(Context c) {
        SharedPreferences p = get(c);
        if (p.contains(DATE_X_DP)) return;
        p.edit().putInt(DATE_SIZE_SP, dateSizeSp(c)).putInt(DATE_X_DP, dateXDp(c))
                .putInt(DATE_Y_DP, dateYDp(c)).apply();
    }

    static int colorMode(Context c) { return get(c).getInt(COLOR_MODE, COLOR_AUTO); }
}

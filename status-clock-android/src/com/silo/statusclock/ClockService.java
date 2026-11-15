package com.silo.statusclock;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ServiceInfo;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Foreground service that draws two independent labels over the status bar:
 * the time (HH:mm:ss) and the date (dd/MM/yyyy), each with its own position and size.
 */
public class ClockService extends Service implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String CHANNEL_ID = "clock";
    private static final int NOTIFICATION_ID = 1;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private WindowManager windowManager;
    private Label time;
    private Label date;
    private SimpleDateFormat timeFormat;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.ROOT);
    private boolean ticking;

    private static ClockService instance;
    /** Last decision from ColorSenseService: true = light background, use black text. */
    private static boolean autoDarkText;

    /** One floating text label and its window parameters. */
    private final class Label {
        final TextView view;
        final WindowManager.LayoutParams params;
        boolean attached;

        Label() {
            view = new TextView(ClockService.this);
            view.setSingleLine(true);
            view.setIncludeFontPadding(false);
            view.setTextDirection(View.TEXT_DIRECTION_LTR);
            view.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
            view.setFontFeatureSettings("tnum"); // fixed-width digits so the text doesn't jitter
            params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                            | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                            | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                            | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                    PixelFormat.TRANSLUCENT);
            params.gravity = Gravity.TOP | Gravity.LEFT;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                params.layoutInDisplayCutoutMode =
                        WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
            }
        }

        void apply(boolean visible, int sizeSp, int xDp, int yDp) {
            view.setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeSp);
            params.x = dp(xDp);
            params.y = dp(yDp);
            if (visible && !attached) {
                windowManager.addView(view, params);
                attached = true;
            } else if (visible) {
                windowManager.updateViewLayout(view, params);
            } else if (attached) {
                detach();
            }
        }

        void detach() {
            if (!attached) return;
            try { windowManager.removeView(view); } catch (IllegalArgumentException ignored) {}
            attached = false;
        }

        Rect bounds() {
            if (!attached) return null;
            int[] loc = new int[2];
            view.getLocationOnScreen(loc);
            return new Rect(loc[0], loc[1], loc[0] + view.getWidth(), loc[1] + view.getHeight());
        }
    }

    private final Runnable tick = new Runnable() {
        @Override public void run() {
            if (time == null) return;
            long now = System.currentTimeMillis();
            Date d = new Date(now);
            time.view.setText(timeFormat.format(d));
            date.view.setText(dateFormat.format(d));
            // Re-align to the start of the next second so the display never drifts.
            handler.postDelayed(this, 1000 - (now % 1000) + 5);
        }
    };

    private final BroadcastReceiver screenReceiver = new BroadcastReceiver() {
        @Override public void onReceive(Context context, Intent intent) {
            String a = intent.getAction();
            if (Intent.ACTION_SCREEN_OFF.equals(a)) {
                stopTicking();
            } else {
                dateFormat.setTimeZone(java.util.TimeZone.getDefault());
                buildTimeFormat();
                stopTicking();
                startTicking();
            }
        }
    };

    @Override public IBinder onBind(Intent intent) { return null; }

    @Override public void onCreate() {
        super.onCreate();
        Prefs.ensureDateDefaults(this);
        startAsForeground();

        if (!Settings.canDrawOverlays(this)) {
            stopSelf();
            return;
        }

        instance = this;
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        time = new Label();
        date = new Label();
        applyStyle();

        Prefs.get(this).registerOnSharedPreferenceChangeListener(this);

        IntentFilter f = new IntentFilter();
        f.addAction(Intent.ACTION_SCREEN_ON);
        f.addAction(Intent.ACTION_SCREEN_OFF);
        f.addAction(Intent.ACTION_TIME_CHANGED);
        f.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        registerReceiver(screenReceiver, f);

        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        if (pm == null || pm.isInteractive()) startTicking();
    }

    @Override public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override public void onDestroy() {
        if (instance == this) instance = null;
        stopTicking();
        Prefs.get(this).unregisterOnSharedPreferenceChangeListener(this);
        try { unregisterReceiver(screenReceiver); } catch (IllegalArgumentException ignored) {}
        if (time != null) time.detach();
        if (date != null) date.detach();
        time = null;
        date = null;
        super.onDestroy();
    }

    @Override public void onSharedPreferenceChanged(SharedPreferences sp, String key) {
        if (time == null) return;
        applyStyle();
        if (ticking) { stopTicking(); startTicking(); }
    }

    private void buildTimeFormat() {
        timeFormat = new SimpleDateFormat(Prefs.showSeconds(this) ? "HH:mm:ss" : "HH:mm", Locale.ROOT);
    }

    private void applyStyle() {
        buildTimeFormat();
        time.apply(true, Prefs.sizeSp(this), Prefs.xDp(this), Prefs.yDp(this));
        date.apply(Prefs.showDate(this), Prefs.dateSizeSp(this), Prefs.dateXDp(this), Prefs.dateYDp(this));
        applyColor();
    }

    private void applyColor() {
        int mode = Prefs.colorMode(this);
        boolean white = mode == Prefs.COLOR_WHITE
                || (mode == Prefs.COLOR_AUTO && !(ColorSenseService.isRunning() && autoDarkText));
        // A soft halo only for fixed colours; in auto mode the colour already matches the background.
        float r = mode == Prefs.COLOR_AUTO && ColorSenseService.isRunning() ? 0f : 3f;
        for (Label l : new Label[]{time, date}) {
            l.view.setTextColor(white ? Color.WHITE : Color.BLACK);
            l.view.setShadowLayer(r, 0f, 0f, white ? 0xCC000000 : 0xCCFFFFFF);
        }
    }

    /** Re-evaluate the colour, e.g. when the auto-colour service is switched on or off. */
    static void refreshColor() {
        if (isShowing()) instance.applyColor();
    }

    static boolean isShowing() { return instance != null && instance.time != null; }

    /** Screen-space rectangles of the visible labels, so the colour sampler can ignore them. */
    static List<Rect> labelBounds() {
        List<Rect> out = new ArrayList<>();
        if (!isShowing()) return out;
        for (Label l : new Label[]{instance.time, instance.date}) {
            Rect r = l.bounds();
            if (r != null) out.add(r);
        }
        return out;
    }

    /** Called on the main thread with the status-bar background brightness (0 = black, 1 = white). */
    static void onBackgroundLuminance(float lum) {
        boolean dark = autoDarkText;
        if (lum > 0.62f) dark = true;        // hysteresis: avoid flicker on mid-grey backgrounds
        else if (lum < 0.45f) dark = false;
        if (dark == autoDarkText) return;
        autoDarkText = dark;
        refreshColor();
    }

    private int dp(int v) {
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v,
                getResources().getDisplayMetrics()));
    }

    private void startTicking() {
        if (ticking || time == null) return;
        ticking = true;
        handler.removeCallbacks(tick);
        tick.run();
    }

    private void stopTicking() {
        ticking = false;
        handler.removeCallbacks(tick);
    }

    private void startAsForeground() {
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        NotificationChannel ch = new NotificationChannel(CHANNEL_ID, "שעון פעיל",
                NotificationManager.IMPORTANCE_MIN);
        ch.setShowBadge(false);
        nm.createNotificationChannel(ch);

        PendingIntent open = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), PendingIntent.FLAG_IMMUTABLE);
        Notification n = new Notification.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle("השעון המלא פועל")
                .setContentText("לחיצה לפתיחת ההגדרות")
                .setContentIntent(open)
                .setOngoing(true)
                .build();

        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(NOTIFICATION_ID, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);
        } else {
            startForeground(NOTIFICATION_ID, n);
        }
    }
}

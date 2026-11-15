package com.silo.statusclock;

import android.accessibilityservice.AccessibilityService;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.hardware.HardwareBuffer;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.view.Display;
import android.view.accessibility.AccessibilityEvent;

import java.util.Arrays;
import java.util.List;

/**
 * Looks at the top strip of the screen and tells the clock whether the background behind
 * the status bar is light (use black text) or dark (use white text), the same way the
 * battery and Wi-Fi icons switch colour. Screenshots are only read in memory, never saved.
 */
public class ColorSenseService extends AccessibilityService {
    private static final long DEBOUNCE_MS = 350;
    private static final long PERIODIC_MS = 2500;

    private static volatile boolean running;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean busy;
    private long lastShotAt;

    private final Runnable check = this::takeSample;
    private final Runnable periodic = new Runnable() {
        @Override public void run() {
            schedule(0);
            handler.postDelayed(this, PERIODIC_MS);
        }
    };

    static boolean isRunning() { return running; }

    @Override protected void onServiceConnected() {
        running = true;
        ClockService.refreshColor();
        handler.postDelayed(periodic, 500);
    }

    @Override public void onAccessibilityEvent(AccessibilityEvent event) {
        schedule(DEBOUNCE_MS);
    }

    @Override public void onInterrupt() {}

    @Override public boolean onUnbind(android.content.Intent intent) {
        running = false;
        handler.removeCallbacksAndMessages(null);
        ClockService.refreshColor();
        return super.onUnbind(intent);
    }

    @Override public void onDestroy() {
        running = false;
        handler.removeCallbacksAndMessages(null);
        ClockService.refreshColor();
        super.onDestroy();
    }

    private void schedule(long delay) {
        if (Prefs.colorMode(this) != Prefs.COLOR_AUTO || !ClockService.isShowing()) return;
        handler.removeCallbacks(check);
        handler.postDelayed(check, delay);
    }

    private void takeSample() {
        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        if (busy || (pm != null && !pm.isInteractive())) return;
        // The system refuses screenshots taken closer than ~1/3 s apart.
        long wait = 400 - (System.currentTimeMillis() - lastShotAt);
        if (wait > 0) { handler.postDelayed(check, wait); return; }
        busy = true;
        lastShotAt = System.currentTimeMillis();
        takeScreenshot(Display.DEFAULT_DISPLAY, getMainExecutor(), new TakeScreenshotCallback() {
            @Override public void onSuccess(ScreenshotResult result) {
                busy = false;
                HardwareBuffer hb = result.getHardwareBuffer();
                try {
                    Bitmap hw = Bitmap.wrapHardwareBuffer(hb, result.getColorSpace());
                    if (hw == null) return;
                    Bitmap sw = hw.copy(Bitmap.Config.ARGB_8888, false);
                    hw.recycle();
                    if (sw == null) return;
                    Float lum = topLuminance(sw);
                    sw.recycle();
                    if (lum != null) ClockService.onBackgroundLuminance(lum);
                } finally {
                    hb.close();
                }
            }
            @Override public void onFailure(int errorCode) {
                busy = false; // e.g. secure (banking) screens: keep the last colour
            }
        });
    }

    private static boolean insideAny(List<Rect> rects, int x, int y) {
        for (Rect r : rects) {
            if (x >= r.left - 6 && x <= r.right + 6 && y >= r.top - 6 && y <= r.bottom + 6) return true;
        }
        return false;
    }

    /** Median brightness (0..1) of the status-bar background, skipping the clock and camera hole. */
    private Float topLuminance(Bitmap bmp) {
        int w = bmp.getWidth();
        int h = bmp.getHeight();
        int sbId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        int sbH = sbId > 0 ? getResources().getDimensionPixelSize(sbId) : Math.round(28 * getResources().getDisplayMetrics().density);
        List<Rect> labels = ClockService.labelBounds();
        // Rows near the top and bottom edge of the status bar, where icons don't reach.
        int[] rows = {Math.min(2, h - 1), Math.min(sbH - 2, h - 1), Math.min(sbH + 2, h - 1)};
        float[] values = new float[rows.length * 80];
        int n = 0;
        for (int y : rows) {
            for (int i = 0; i < 80; i++) {
                int x = (int) (w * (0.06f + 0.88f * i / 79f));
                if (x > w * 0.40f && x < w * 0.60f) continue; // camera cut-out
                if (insideAny(labels, x, y)) continue;
                int c = bmp.getPixel(x, y);
                values[n++] = (0.2126f * Color.red(c) + 0.7152f * Color.green(c)
                        + 0.0722f * Color.blue(c)) / 255f;
            }
        }
        if (n < 10) return null;
        float[] v = Arrays.copyOf(values, n);
        Arrays.sort(v);
        return v[n / 2];
    }
}

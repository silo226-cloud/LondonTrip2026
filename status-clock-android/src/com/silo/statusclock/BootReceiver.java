package com.silo.statusclock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;

/** Restarts the clock after the phone reboots, if it was on before. */
public class BootReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())
                && Prefs.enabled(context) && Settings.canDrawOverlays(context)) {
            context.startForegroundService(new Intent(context, ClockService.class));
        }
    }
}

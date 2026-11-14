package com.silo.statusclock;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

/** Settings screen: permission, on/off, size, position and colour. */
public class MainActivity extends Activity {
    private TextView status;
    private Button permissionButton;
    private Switch enabledSwitch;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= 33) {
            requestPermissions(new String[]{"android.permission.POST_NOTIFICATIONS"}, 1);
        }

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        int pad = dp(20);
        root.setPadding(pad, dp(40), pad, pad);

        TextView title = new TextView(this);
        title.setText("שעון מלא בשורת המצב");
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
        root.addView(title);

        status = new TextView(this);
        status.setPadding(0, dp(12), 0, dp(12));
        root.addView(status);

        permissionButton = new Button(this);
        permissionButton.setText("1. אישור הצגה מעל אפליקציות אחרות");
        permissionButton.setOnClickListener(v -> startActivity(new Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + getPackageName()))));
        root.addView(permissionButton);

        SharedPreferences sp = Prefs.get(this);

        enabledSwitch = new Switch(this);
        enabledSwitch.setText("2. הפעלת השעון");
        enabledSwitch.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        enabledSwitch.setPadding(0, dp(16), 0, dp(16));
        enabledSwitch.setOnCheckedChangeListener((b, on) -> {
            sp.edit().putBoolean(Prefs.ENABLED, on).apply();
            syncService();
        });
        root.addView(enabledSwitch);

        root.addView(toggle("הצגת שניות", Prefs.SHOW_SECONDS, Prefs.showSeconds(this)));
        root.addView(toggle("הצגת תאריך מלא", Prefs.SHOW_DATE, Prefs.showDate(this)));
        root.addView(toggle("טקסט לבן (כבוי = שחור)", Prefs.WHITE_TEXT, Prefs.whiteText(this)));

        root.addView(slider("גודל טקסט", Prefs.SIZE_SP, 8, 24, Prefs.sizeSp(this)));
        root.addView(slider("מיקום אופקי (משמאל)", Prefs.X_DP, 0, 300, Prefs.xDp(this)));
        root.addView(slider("מיקום אנכי (מלמעלה)", Prefs.Y_DP, 0, 60, Prefs.yDp(this)));

        TextView tip = new TextView(this);
        tip.setPadding(0, dp(20), 0, 0);
        tip.setText("טיפ: כדי שהשעון המקורי לא יופיע מתחת, ב-Good Lock ← QuickStar ← Clock "
                + "יש לבחור HIDE.\n\nאם השעון נעלם אחרי זמן מה: הגדרות ← אפליקציות ← שעון מלא ← "
                + "סוללה ← \"ללא הגבלה\".");
        root.addView(tip);

        ScrollView scroll = new ScrollView(this);
        scroll.addView(root);
        setContentView(scroll);
    }

    @Override protected void onResume() {
        super.onResume();
        boolean canDraw = Settings.canDrawOverlays(this);
        permissionButton.setVisibility(canDraw ? View.GONE : View.VISIBLE);
        enabledSwitch.setEnabled(canDraw);
        enabledSwitch.setChecked(canDraw && Prefs.enabled(this));
        status.setText(canDraw
                ? "ההרשאה ניתנה. אפשר להפעיל את השעון ולכוון את המיקום."
                : "צריך קודם לאשר לאפליקציה להופיע מעל אפליקציות אחרות.");
        syncService();
    }

    private void syncService() {
        Intent i = new Intent(this, ClockService.class);
        if (Settings.canDrawOverlays(this) && Prefs.enabled(this)) {
            startForegroundService(i);
        } else {
            stopService(i);
        }
    }

    private View toggle(String label, String key, boolean value) {
        Switch s = new Switch(this);
        s.setText(label);
        s.setChecked(value);
        s.setPadding(0, dp(10), 0, dp(10));
        s.setOnCheckedChangeListener((b, on) -> Prefs.get(this).edit().putBoolean(key, on).apply());
        return s;
    }

    private View slider(String label, String key, int min, int max, int value) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(0, dp(12), 0, 0);
        TextView t = new TextView(this);
        t.setText(label + ": " + value);
        box.addView(t);
        SeekBar bar = new SeekBar(this);
        bar.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        bar.setMax(max - min);
        bar.setProgress(value - min);
        bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar s, int p, boolean user) {
                t.setText(label + ": " + (p + min));
                if (user) Prefs.get(MainActivity.this).edit().putInt(key, p + min).apply();
            }
            @Override public void onStartTrackingTouch(SeekBar s) {}
            @Override public void onStopTrackingTouch(SeekBar s) {}
        });
        box.addView(bar);
        return box;
    }

    private int dp(int v) {
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v,
                getResources().getDisplayMetrics()));
    }
}

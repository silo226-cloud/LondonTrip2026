import Toybox.Activity;
import Toybox.Application;
import Toybox.Attention;
import Toybox.Graphics;
import Toybox.Lang;
import Toybox.Math;
import Toybox.WatchUi;

// Swim Pace data field.
//
// TEMPO mode (pool): vibrates every N lengths at the target pace, like a
// FINIS Tempo Trainer in Mode 1. Beep times are computed from the start of
// the activity timer, so there is no cumulative drift (22.5s targets beep at
// 23, 45, 68, 90 ... seconds - each beep is at most ~1s late, never more).
//
// GHOST mode (open water): compares GPS distance with where a virtual swimmer
// at the target pace would be. Every checkpoint (default 100m of the ghost):
//   1 short buzz  = on pace (within tolerance)
//   2 short buzzes = behind
//   1 long buzz   = ahead
class SwimPaceField extends WatchUi.DataField {

    const MODE_AUTO = 0;
    const MODE_TEMPO = 1;
    const MODE_GHOST = 2;

    // Settings
    private var _pace100 as Float = 90.0;   // seconds per 100m
    private var _mode as Number = 0;
    private var _poolLength as Number = 25;
    private var _lengthsPerBeep as Number = 1;
    private var _checkpoint as Number = 100;
    private var _threshold as Number = 15;

    // State
    private var _lastTimerMs as Number = 0;
    private var _beepCount as Number = 0;
    private var _cpCount as Number = 0;

    // Display
    private var _label as String = "";
    private var _value as String = "--";
    private var _sub as String = "";

    function initialize() {
        DataField.initialize();
        loadSettings();
    }

    function loadSettings() as Void {
        var mins = toFloat(Application.Properties.getValue("paceMin"), 1.0);
        var secs = toFloat(Application.Properties.getValue("paceSec"), 30.0);
        _pace100 = mins * 60.0 + secs;
        if (_pace100 < 10.0) {
            _pace100 = 10.0;
        }
        _mode = toFloat(Application.Properties.getValue("mode"), 0.0).toNumber();
        _poolLength = clamp(toFloat(Application.Properties.getValue("poolLength"), 25.0).toNumber(), 10, 100);
        _lengthsPerBeep = clamp(toFloat(Application.Properties.getValue("lengthsPerBeep"), 1.0).toNumber(), 1, 20);
        _checkpoint = clamp(toFloat(Application.Properties.getValue("checkpoint"), 100.0).toNumber(), 25, 1000);
        _threshold = clamp(toFloat(Application.Properties.getValue("threshold"), 15.0).toNumber(), 1, 200);
        resetState();
    }

    function onTimerReset() as Void {
        resetState();
    }

    private function resetState() as Void {
        _lastTimerMs = 0;
        _beepCount = 0;
        _cpCount = 0;
    }

    // Auto mode: pool swim -> Tempo, anything else (open water) -> Ghost.
    private function activeMode() as Number {
        if (_mode == MODE_TEMPO || _mode == MODE_GHOST) {
            return _mode;
        }
        if (Activity has :getProfileInfo) {
            var profile = Activity.getProfileInfo();
            if (profile != null && profile.subSport == Activity.SUB_SPORT_LAP_SWIMMING) {
                return MODE_TEMPO;
            }
        }
        return MODE_GHOST;
    }

    function compute(info as Activity.Info) as Void {
        var timerMs = info.timerTime;
        if (timerMs == null) {
            return;
        }
        if (timerMs < _lastTimerMs) {
            resetState();
        }
        // Only vibrate while the timer is actually running.
        var running = timerMs > _lastTimerMs;
        _lastTimerMs = timerMs;

        var dist = 0.0;
        if (info.elapsedDistance != null) {
            dist = info.elapsedDistance.toFloat();
        }
        var elapsedSec = timerMs / 1000.0;
        var targetDist = elapsedSec * 100.0 / _pace100;

        if (activeMode() == MODE_TEMPO) {
            computeTempo(timerMs, dist, targetDist, running);
        } else {
            computeGhost(dist, targetDist, running);
        }
    }

    private function computeTempo(timerMs as Number, dist as Float, targetDist as Float, running as Boolean) as Void {
        // interval (ms) = pace(s/100m) * meters / 100 * 1000
        var intervalMs = _pace100 * _poolLength * _lengthsPerBeep * 10.0;
        var n = Math.floor(timerMs / intervalMs).toNumber();
        if (n > _beepCount) {
            _beepCount = n;
            if (running) {
                vibrate([new Attention.VibeProfile(100, 400)]);
            }
        }
        var remaining = ((n + 1) * intervalMs - timerMs) / 1000.0;
        _label = "TEMPO " + (intervalMs / 1000.0).format("%.1f") + "s";
        _value = remaining.format("%.0f");
        // Pool distance only updates at the wall, so this is a wall-to-wall check.
        _sub = signed(dist - targetDist) + "m";
    }

    private function computeGhost(dist as Float, targetDist as Float, running as Boolean) as Void {
        var diffM = dist - targetDist;                // + ahead, - behind
        var diffS = diffM * _pace100 / 100.0;         // seconds ahead/behind

        var cp = Math.floor(targetDist / _checkpoint).toNumber();
        if (cp > _cpCount) {
            _cpCount = cp;
            if (running) {
                if (diffM < -_threshold) {
                    // behind: two short buzzes
                    vibrate([
                        new Attention.VibeProfile(100, 250),
                        new Attention.VibeProfile(0, 250),
                        new Attention.VibeProfile(100, 250)
                    ]);
                } else if (diffM > _threshold) {
                    // ahead: one long buzz
                    vibrate([new Attention.VibeProfile(100, 1000)]);
                } else {
                    // on pace: one short buzz
                    vibrate([new Attention.VibeProfile(100, 250)]);
                }
            }
        }
        _label = "GHOST " + formatPace(_pace100);
        _value = signed(diffM) + "m";
        _sub = signed(diffS) + "s";
    }

    function onUpdate(dc as Graphics.Dc) as Void {
        var bg = getBackgroundColor();
        var fg = (bg == Graphics.COLOR_BLACK) ? Graphics.COLOR_WHITE : Graphics.COLOR_BLACK;
        dc.setColor(fg, bg);
        dc.clear();

        var w = dc.getWidth();
        var h = dc.getHeight();
        var cx = w / 2;

        var valueFont = Graphics.FONT_NUMBER_MEDIUM;
        if (h < 90) {
            valueFont = Graphics.FONT_MEDIUM;
        }
        var smallFont = Graphics.FONT_XTINY;
        var smallH = dc.getFontHeight(smallFont);

        dc.drawText(cx, 2, smallFont, _label, Graphics.TEXT_JUSTIFY_CENTER);
        dc.drawText(cx, h / 2, valueFont, _value,
            Graphics.TEXT_JUSTIFY_CENTER | Graphics.TEXT_JUSTIFY_VCENTER);
        dc.drawText(cx, h - smallH - 2, smallFont, _sub, Graphics.TEXT_JUSTIFY_CENTER);
    }

    // ---- helpers ----

    private function vibrate(profile as Array<Attention.VibeProfile>) as Void {
        if (Attention has :vibrate) {
            Attention.vibrate(profile);
        }
    }

    private function toFloat(v, dflt as Float) as Float {
        if (v instanceof Number || v instanceof Float || v instanceof Long || v instanceof Double) {
            return v.toFloat();
        }
        return dflt;
    }

    private function clamp(v as Number, lo as Number, hi as Number) as Number {
        if (v < lo) { return lo; }
        if (v > hi) { return hi; }
        return v;
    }

    private function signed(v as Float) as String {
        var r = Math.round(v).toNumber();
        return (r > 0 ? "+" : "") + r.toString();
    }

    private function formatPace(sec as Float) as String {
        var m = Math.floor(sec / 60.0).toNumber();
        var s = sec - m * 60;
        if (s == Math.floor(s)) {
            return m.toString() + ":" + s.toNumber().format("%02d");
        }
        return m.toString() + ":" + s.format("%04.1f");
    }
}

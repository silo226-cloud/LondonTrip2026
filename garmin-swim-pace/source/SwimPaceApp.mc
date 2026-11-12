import Toybox.Application;
import Toybox.Lang;
import Toybox.WatchUi;

class SwimPaceApp extends Application.AppBase {

    private var _field as SwimPaceField?;

    function initialize() {
        AppBase.initialize();
    }

    function getInitialView() {
        _field = new SwimPaceField();
        return [_field];
    }

    // Called when settings are changed from the phone (Connect IQ app).
    function onSettingsChanged() as Void {
        if (_field != null) {
            _field.loadSettings();
        }
        WatchUi.requestUpdate();
    }
}

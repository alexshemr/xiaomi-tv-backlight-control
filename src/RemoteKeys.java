package local.ha.tvbacklight;

import android.accessibilityservice.AccessibilityService;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.graphics.PixelFormat;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.widget.LinearLayout;

public final class RemoteKeys extends AccessibilityService {
    static final String MODIFIER = "shortcut";
    static boolean configuring;
    static java.util.function.Predicate<KeyEvent> captureHandler;
    private final KeyPanel keys = new KeyPanel();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable dismiss = () -> { keys.close(); removePanel(); };
    private SharedPreferences preferences;
    private WindowManager windows;
    private LinearLayout panel;
    private BrightnessGauge gauge;
    private final ContentObserver observer = new ContentObserver(handler) {
        @Override public void onChange(boolean selfChange) {
            if (panel != null) refresh();
        }
    };

    @Override protected void onServiceConnected() {
        Log.i("TVBacklight", "Remote accessibility service connected");
        preferences = Backlight.preferences(this);
        windows = getSystemService(WindowManager.class);
        getContentResolver().registerContentObserver(
            Settings.Global.getUriFor(Bridge.KEY), false, observer);
    }

    @Override protected boolean onKeyEvent(KeyEvent event) {
        int key = event.getKeyCode();
        boolean screenOff = ScreenPower.needsWake(this);
        if (!screenOff && captureHandler != null && captureHandler.test(event)) return true;
        if (event.getAction() == KeyEvent.ACTION_UP) return keys.up(event.getDeviceId(), key);
        if (event.getAction() != KeyEvent.ACTION_DOWN) return false;
        if (configuring && !screenOff) {
            if (event.getRepeatCount() == 0) {
                Log.i("TVBacklight", "Accessibility capture key=" + KeyEvent.keyCodeToString(key));
            }
            return false;
        }
        boolean consumed = keys.down(event.getDeviceId(), key, event.getRepeatCount() != 0,
            event.getEventTime(), preferences.getInt(MODIFIER, 0),
            preferences.getInt("screen_off_shortcut", 0), screenOff);
        if (key == preferences.getInt(MODIFIER, 0) && event.getRepeatCount() == 0) {
            Log.i("TVBacklight", "Shortcut received; panel=" + keys.visible);
        }
        if (!consumed) {
            if (key == KeyEvent.KEYCODE_POWER || key == KeyEvent.KEYCODE_TV_POWER
                    || key == KeyEvent.KEYCODE_SLEEP) ScreenPower.forgetRequest();
            return false;
        }
        try {
            if (keys.screenOffRequested) {
                ScreenPower.setOff(this, true);
                dismiss.run();
                return true;
            }
            if (keys.wakeRequested) {
                int level = Math.min(100, Math.max(5, Backlight.read(this) + keys.delta));
                Backlight.write(this, level);
            }
            if (keys.delta == 0 && key == preferences.getInt(MODIFIER, 0)
                    && keys.visible) ScreenPower.setOff(this, false);
            if (keys.delta != 0 && !keys.wakeRequested) Backlight.write(this,
                Math.max(0, Math.min(100, Backlight.read(this) + keys.delta)));
            if (ScreenPower.isOff(this)) {
                dismiss.run();
            } else if (keys.visible) {
                showPanel();
                refresh();
                handler.removeCallbacks(dismiss);
                handler.postDelayed(dismiss, 5000);
            } else {
                removePanel();
            }
        } catch (RuntimeException error) {
            Log.e("TVBacklight", "Unable to control backlight", error);
            dismiss.run();
        }
        return true;
    }

    private void showPanel() {
        if (panel != null) return;
        panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setGravity(Gravity.CENTER);
        boolean dark = (getResources().getConfiguration().uiMode
            & android.content.res.Configuration.UI_MODE_NIGHT_MASK)
            == android.content.res.Configuration.UI_MODE_NIGHT_YES;
        GradientDrawable background = new GradientDrawable();
        background.setColor(dark ? 0xf022282f : 0xfff5f5f5);
        background.setCornerRadius(dp(20));
        panel.setBackground(background);
        gauge = new BrightnessGauge(this);
        panel.addView(gauge, new LinearLayout.LayoutParams(dp(40), dp(268)));
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
            dp(40), dp(268), WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.RIGHT | Gravity.CENTER_VERTICAL;
        params.x = dp(20);
        windows.addView(panel, params);
    }

    private void refresh() {
        int level = Backlight.read(this);
        gauge.setLevel(level);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private void removePanel() {
        handler.removeCallbacks(dismiss);
        if (panel != null) {
            if (panel.isAttachedToWindow()) windows.removeView(panel);
            panel = null;
        }
    }

    @Override public void onAccessibilityEvent(AccessibilityEvent event) {}
    @Override public void onInterrupt() {
        removePanel();
        keys.reset();
    }
    @Override public void onDestroy() {
        getContentResolver().unregisterContentObserver(observer);
        removePanel();
        keys.reset();
        super.onDestroy();
    }
}

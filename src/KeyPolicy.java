package local.ha.tvbacklight;

import android.view.KeyEvent;

final class KeyPolicy {
    static boolean canAssign(int key) {
        switch (key) {
            case KeyEvent.KEYCODE_UNKNOWN:
            case KeyEvent.KEYCODE_HOME:
            case KeyEvent.KEYCODE_BACK:
            case KeyEvent.KEYCODE_DPAD_UP:
            case KeyEvent.KEYCODE_DPAD_DOWN:
            case KeyEvent.KEYCODE_DPAD_LEFT:
            case KeyEvent.KEYCODE_DPAD_RIGHT:
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER:
            case KeyEvent.KEYCODE_NUMPAD_ENTER:
            case KeyEvent.KEYCODE_MENU:
            case KeyEvent.KEYCODE_SETTINGS:
            case KeyEvent.KEYCODE_VOLUME_UP:
            case KeyEvent.KEYCODE_VOLUME_DOWN:
            case KeyEvent.KEYCODE_VOLUME_MUTE:
            case KeyEvent.KEYCODE_MUTE:
            case KeyEvent.KEYCODE_POWER:
            case KeyEvent.KEYCODE_TV_POWER:
            case KeyEvent.KEYCODE_SLEEP:
            case KeyEvent.KEYCODE_WAKEUP:
            case KeyEvent.KEYCODE_ASSIST:
            case KeyEvent.KEYCODE_VOICE_ASSIST:
                return false;
            default:
                return key > 0;
        }
    }
}

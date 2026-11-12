package local.ha.tvbacklight;

import java.util.HashSet;
import java.util.Set;

/** Tracks complete key presses, including presses spanning overlay closure. */
final class KeyPanel {
    boolean visible;
    int delta;
    boolean screenOffRequested;
    boolean wakeRequested;
    private long lastChange;
    private final Set<Long> consumed = new HashSet<>();
    private final Set<Long> passed = new HashSet<>();

    boolean down(int device, int key, boolean repeat, long time, int shortcut) {
        return down(device, key, repeat, time, shortcut, 0);
    }

    boolean down(int device, int key, boolean repeat, long time, int shortcut, int offShortcut) {
        return down(device, key, repeat, time, shortcut, offShortcut, false);
    }

    boolean down(int device, int key, boolean repeat, long time, int shortcut,
            int offShortcut, boolean screenOff) {
        delta = 0;
        screenOffRequested = false;
        wakeRequested = false;
        long press = press(device, key);
        if (passed.contains(press)) return false;
        if (consumed.contains(press)) {
            if (visible && repeat && time - lastChange >= 150) change(key, time);
            return true;
        }
        if (repeat) return false;
        if (offShortcut != 0 && key == offShortcut) {
            screenOffRequested = true;
            close();
        } else if (screenOff && key != 26 && key != 177 && key != 223 && key != 0) {
            wakeRequested = true;
            visible = key == 19 || key == shortcut;
            delta = key == 19 ? 5 : 0;
            lastChange = time;
        } else if (shortcut != 0 && key == shortcut) {
            visible = !visible;
        } else if (visible && key == 4) {
            close();
        } else if (visible && (key == 19 || key == 20)) {
            change(key, time);
        } else {
            passed.add(press);
            return false;
        }
        consumed.add(press);
        return true;
    }

    boolean up(int device, int key) {
        long press = press(device, key);
        passed.remove(press);
        return consumed.remove(press);
    }

    void close() { visible = false; }
    void reset() {
        close();
        consumed.clear();
        passed.clear();
    }

    private void change(int key, long time) {
        if (key == 19 || key == 20) {
            delta = key == 19 ? 5 : -5;
            lastChange = time;
        }
    }

    private static long press(int device, int key) {
        return ((long) device << 32) | (key & 0xffffffffL);
    }
}

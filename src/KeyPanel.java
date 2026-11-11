package local.ha.tvbacklight;

import java.util.HashSet;
import java.util.Set;

/** Tracks complete key presses, including presses spanning overlay closure. */
final class KeyPanel {
    boolean visible;
    int delta;
    private long lastChange;
    private final Set<Long> consumed = new HashSet<>();
    private final Set<Long> passed = new HashSet<>();

    boolean down(int device, int key, boolean repeat, long time, int shortcut) {
        delta = 0;
        long press = press(device, key);
        if (passed.contains(press)) return false;
        if (consumed.contains(press)) {
            if (visible && repeat && time - lastChange >= 150) change(key, time);
            return true;
        }
        if (repeat) return false;
        if (shortcut != 0 && key == shortcut) {
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

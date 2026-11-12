package local.ha.tvbacklight;

import android.content.Context;
import android.provider.Settings;

final class ScreenPower {
    private static volatile boolean requestedOff;

    static boolean needsWake(Context context) {
        return requestedOff || isOff(context);
    }

    static void forgetRequest() { requestedOff = false; }

    static boolean isOff(Context context) {
        return Settings.Global.getInt(context.getContentResolver(), "power_picture_off", 1) == 0;
    }

    static void setOff(Context context, boolean off) {
        if (!Settings.Global.putInt(context.getContentResolver(), "power_picture_off", off ? 0 : 1)) {
            throw new IllegalStateException("Screen power write failed");
        }
        requestedOff = off;
    }
}

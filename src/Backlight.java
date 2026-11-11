package local.ha.tvbacklight;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;

final class Backlight {
    static SharedPreferences preferences(Context context) {
        SharedPreferences preferences = context.getSharedPreferences("bridge", Context.MODE_PRIVATE);
        if (!preferences.getBoolean("single_key_v1", false)) {
            preferences.edit().remove("modifier").putBoolean("single_key_v1", true).apply();
        }
        return preferences;
    }

    static int read(Context context) {
        int level = Settings.Global.getInt(context.getContentResolver(), Bridge.KEY, -1);
        if (level < 0 || level > 100) throw new IllegalStateException("Unsupported backlight");
        return level;
    }

    static int write(Context context, int level) {
        if (level < 0 || level > 100) throw new IllegalArgumentException("Invalid level");
        if (!Settings.Global.putInt(context.getContentResolver(), Bridge.KEY, level)) {
            throw new IllegalStateException("Backlight write failed");
        }
        return read(context);
    }
}

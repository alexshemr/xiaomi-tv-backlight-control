package local.ha.tvbacklight;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.SystemClock;
import java.security.SecureRandom;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Locale;
import java.util.UUID;

final class Pairing {
    private static String code;
    private static long expires;
    private static int attempts;
    private static boolean accepted;
    static synchronized String open() {
        code = String.format(Locale.ROOT, "%06d", new SecureRandom().nextInt(1000000));
        expires = SystemClock.elapsedRealtime() + 120000;
        attempts = 0;
        accepted = false;
        return code;
    }
    static synchronized void close() { code = null; }
    static synchronized boolean isOpen() {
        return code != null && attempts < 5 && SystemClock.elapsedRealtime() < expires;
    }
    static synchronized String accept(Context context, String supplied) {
        if (!isOpen()) return null;
        attempts++;
        if (!equal(code, supplied)) return null;
        String token = UUID.randomUUID().toString();
        Backlight.preferences(context).edit().putString("paired_token", token).apply();
        close();
        accepted = true;
        return token;
    }
    static synchronized boolean wasAccepted() { return accepted; }
    static synchronized String identity(Context context) {
        SharedPreferences preferences = Backlight.preferences(context);
        String id = preferences.getString("device_id", null);
        if (id == null) {
            id = UUID.randomUUID().toString();
            preferences.edit().putString("device_id", id).commit();
        }
        return id;
    }
    static boolean authorized(Context context, String authorization, String legacy) {
        String paired = Backlight.preferences(context).getString("paired_token", null);
        return equal("Bearer " + legacy, authorization)
            || (paired != null && equal("Bearer " + paired, authorization));
    }
    private static boolean equal(String left, String right) {
        return MessageDigest.isEqual(left.getBytes(StandardCharsets.UTF_8), right.getBytes(StandardCharsets.UTF_8));
    }
}

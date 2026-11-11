package local.ha.tvbacklight;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public final class Provision extends BroadcastReceiver {
    @Override public void onReceive(Context context, Intent intent) {
        String token = intent.getStringExtra("token");
        if (token == null || !token.matches("[a-f0-9-]{36}")) return;
        context.getSharedPreferences("bridge", Context.MODE_PRIVATE)
            .edit().putString("token", token).apply();
        context.stopService(new Intent(context, Bridge.class));
        context.startForegroundService(new Intent(context, Bridge.class));
    }
}

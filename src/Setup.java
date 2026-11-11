package local.ha.tvbacklight;

import android.app.*;
import android.content.*;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.*;
import android.os.*;
import android.provider.Settings;
import android.view.*;
import android.widget.*;
import java.net.URI;
import java.util.UUID;

public final class Setup extends Activity {
    private SharedPreferences preferences;
    private Button capture;
    private TextView status, connection, accessState, apiState;
    private boolean capturing, testing;
    private int capturedKey;
    private int ink, muted, surface, accent, base;
    private BrightnessGauge preview;
    private NetworkDiscovery network;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable update = new Runnable() {
        public void run() {
            try { preview.setLevel(Backlight.read(Setup.this)); }
            catch (RuntimeException error) { status.setText(R.string.unsupported); }
            boolean granted = checkSelfPermission(android.Manifest.permission.WRITE_SECURE_SETTINGS)
                == android.content.pm.PackageManager.PERMISSION_GRANTED;
            String services = Settings.Secure.getString(getContentResolver(), "enabled_accessibility_services");
            boolean enabled = services != null && services.contains(getPackageName() + "/");
            accessState.setText(enabled ? R.string.access_on : R.string.access_off);
            apiState.setText(getString(granted ? R.string.permission_ok : R.string.permission_missing)
                + "\n" + getString(Bridge.running ? R.string.rest_ok : R.string.rest_off));
            handler.postDelayed(this, 1000);
        }
    };

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        preferences = Backlight.preferences(this);
        if (!preferences.contains("token")) {
            preferences.edit().putString("token", UUID.randomUUID().toString()).commit();
        }
        startForegroundService(new Intent(this, Bridge.class));
        network = new NetworkDiscovery(this);
        boolean dark = (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
            == Configuration.UI_MODE_NIGHT_YES;
        base = dark ? 0xff161b22 : 0xfff4f5f6;
        surface = dark ? 0xff242b34 : Color.WHITE;
        ink = dark ? 0xfff3f5f7 : 0xff202832;
        muted = dark ? 0xffa9b4c2 : 0xff596676;
        accent = dark ? 0xffedd18b : 0xff9a6b0d;

        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.HORIZONTAL);
        page.setPadding(dp(40), dp(28), dp(40), dp(28));
        page.setBackgroundColor(base);
        LinearLayout summary = column();
        summary.setPadding(0, dp(10), dp(32), 0);
        summary.addView(text(getString(R.string.app_name), 28, ink, true));
        summary.addView(text(getString(R.string.tagline), 16, muted, false));
        preview = new BrightnessGauge(this);
        LinearLayout.LayoutParams gaugeSize = new LinearLayout.LayoutParams(dp(96), dp(260));
        gaugeSize.topMargin = dp(20);
        summary.addView(preview, gaugeSize);
        page.addView(summary, new LinearLayout.LayoutParams(0, -1, 0.85f));

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        LinearLayout settings = column();
        settings.setPadding(dp(4), dp(4), dp(4), dp(4));
        LinearLayout remote = section(R.string.remote_title);
        remote.addView(text(getString(R.string.remote_hint), 15, muted, false));
        accessState = text("", 14, muted, false);
        remote.addView(accessState);
        capture = button(remote, "", () -> {
            testing = false;
            capturing = true;
            capture.setText(R.string.capture);
            status.setText(R.string.capture_hint);
        });
        status = text("", 14, accent, false);
        remote.addView(status);
        button(remote, getString(R.string.test_key), () -> {
            capturing = false;
            testing = true;
            updateLabel();
            status.setText(R.string.test_hint);
        });
        button(remote, getString(R.string.reset), () -> {
            capturing = testing = false;
            preferences.edit().remove(RemoteKeys.MODIFIER).apply();
            updateLabel();
            status.setText("");
        });
        button(remote, getString(R.string.accessibility), this::openAccessibility);
        remote.addView(text(getString(R.string.key_help), 13, muted, false));
        settings.addView(remote);

        LinearLayout ha = section(R.string.ha_title);
        ha.addView(text(getString(R.string.ha_hint), 15, muted, false));
        connection = text(preferences.getString("ha_server", ""), 14, accent, false);
        ha.addView(connection);
        button(ha, getString(R.string.search), this::search);
        button(ha, getString(R.string.manual), this::manual);
        button(ha, getString(R.string.pair), this::pair);
        settings.addView(ha);

        LinearLayout device = section(R.string.status_title);
        apiState = text("", 14, muted, false);
        device.addView(apiState);
        button(device, getString(R.string.about), () -> {
            String project = "https://github.com/alexshemr/xiaomi-tv-backlight-control";
            LinearLayout contents = column();
            contents.setPadding(dp(24), dp(16), dp(24), dp(16));
            contents.addView(text(project, 15, ink, false));
            ImageView qr = QrCode.view(this, project);
            LinearLayout.LayoutParams size = new LinearLayout.LayoutParams(dp(192), dp(192));
            size.gravity = Gravity.CENTER_HORIZONTAL;
            contents.addView(qr, size);
            new AlertDialog.Builder(this).setTitle(R.string.about)
                .setView(contents).setPositiveButton(android.R.string.ok, null).show();
        });
        settings.addView(device);
        scroll.addView(settings);
        page.addView(scroll, new LinearLayout.LayoutParams(0, -1, 1.35f));
        setContentView(page);
        updateLabel();
    }

    private LinearLayout column() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        return layout;
    }
    private LinearLayout section(int title) {
        LinearLayout layout = column();
        layout.setPadding(dp(22), dp(18), dp(22), dp(18));
        GradientDrawable background = new GradientDrawable();
        background.setColor(surface);
        background.setCornerRadius(dp(18));
        layout.setBackground(background);
        LinearLayout.LayoutParams margins = new LinearLayout.LayoutParams(-1, -2);
        margins.bottomMargin = dp(14);
        layout.setLayoutParams(margins);
        layout.addView(text(getString(title), 21, ink, true));
        return layout;
    }
    private TextView text(String label, int size, int color, boolean strong) {
        TextView view = new TextView(this);
        view.setText(label);
        view.setTextSize(size);
        view.setTextColor(color);
        view.setPadding(0, dp(4), 0, dp(8));
        view.setLineSpacing(dp(3), 1);
        if (strong) view.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        return view;
    }
    private Button button(LinearLayout layout, String label, Runnable action) {
        Button button = new Button(this);
        button.setText(label);
        button.setTextSize(14);
        button.setAllCaps(false);
        button.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
        button.setTextColor(ink);
        button.setPadding(dp(16), 0, dp(16), 0);
        StateListDrawable backgrounds = new StateListDrawable();
        GradientDrawable focus = new GradientDrawable();
        focus.setColor(surface);
        focus.setCornerRadius(dp(10));
        focus.setStroke(dp(2), accent);
        GradientDrawable normal = new GradientDrawable();
        normal.setColor(base);
        normal.setCornerRadius(dp(10));
        backgrounds.addState(new int[]{android.R.attr.state_focused}, focus);
        backgrounds.addState(new int[]{android.R.attr.state_pressed}, focus);
        backgrounds.addState(new int[]{}, normal);
        button.setBackground(backgrounds);
        LinearLayout.LayoutParams dimensions = new LinearLayout.LayoutParams(-1, dp(48));
        dimensions.topMargin = dp(8);
        layout.addView(button, dimensions);
        button.setOnClickListener(view -> action.run());
        return button;
    }
    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }
    private void updateLabel() {
        int key = preferences.getInt(RemoteKeys.MODIFIER, 0);
        capture.setText(key == 0 ? getString(R.string.assign)
            : getString(R.string.change_key, KeyEvent.keyCodeToString(key)));
    }
    private void openAccessibility() {
        Intent direct = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        if (direct.resolveActivity(getPackageManager()) != null) {
            try { startActivity(direct); return; }
            catch (ActivityNotFoundException | SecurityException ignored) {}
        }
        new AlertDialog.Builder(this).setTitle(R.string.accessibility)
            .setMessage(R.string.access_fallback)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                Intent settings = new Intent(Settings.ACTION_SETTINGS);
                if (settings.resolveActivity(getPackageManager()) == null) {
                    settings = new Intent().setComponent(new ComponentName(
                        "com.android.tv.settings", "com.android.tv.settings.MainSettings"));
                }
                try { startActivity(settings); }
                catch (ActivityNotFoundException | SecurityException error) {
                    status.setText(R.string.access_fallback);
                }
            }).show();
    }
    private void search() {
        connection.setText(R.string.searching);
        network.search(address -> runOnUiThread(() -> {
            preferences.edit().putString("ha_server", address).apply();
            connection.setText(address);
        }));
        handler.postDelayed(() -> {
            network.stopSearch();
            if (connection.getText().toString().equals(getString(R.string.searching))) {
                connection.setText(R.string.not_found);
            }
        }, 10000);
    }
    private void manual() {
        EditText address = new EditText(this);
        address.setSingleLine(true);
        address.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_URI);
        address.setText(preferences.getString("ha_server", "http://homeassistant.local:8123"));
        AlertDialog dialog = new AlertDialog.Builder(this).setTitle(R.string.server)
            .setView(address).setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.save, null).create();
        dialog.setOnShowListener(ignored -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> {
            String value = address.getText().toString().trim();
            try {
                URI uri = new URI(value);
                if (!("http".equals(uri.getScheme()) || "https".equals(uri.getScheme()))
                        || uri.getHost() == null || uri.getUserInfo() != null) throw new Exception();
                preferences.edit().putString("ha_server", value).apply();
                connection.setText(value);
                dialog.dismiss();
            } catch (Exception error) { address.setError(getString(R.string.server)); }
        }));
        dialog.show();
    }
    private void pair() {
        String code = Pairing.open();
        TextView instructions = text(getString(R.string.pair_hint) + "\n\n" + code, 20, ink, false);
        instructions.setPadding(dp(24), dp(16), dp(24), dp(16));
        LinearLayout contents = column();
        contents.addView(instructions);
        String server = preferences.getString("ha_server", "");
        if (!server.isEmpty()) {
            ImageView qr = QrCode.view(this, server.replaceAll("/+$", "")
                + "/config/integrations/dashboard/add?domain=xiaomi_tv_backlight");
            LinearLayout.LayoutParams dimensions = new LinearLayout.LayoutParams(dp(144), dp(144));
            dimensions.gravity = Gravity.CENTER_HORIZONTAL;
            contents.addView(qr, dimensions);
        }
        AlertDialog dialog = new AlertDialog.Builder(this).setTitle(R.string.pair)
            .setView(contents).setPositiveButton(android.R.string.ok, null).create();
        Runnable tick = new Runnable() {
            public void run() {
                if (!dialog.isShowing()) return;
                if (!Pairing.isOpen()) {
                    instructions.setText(Pairing.wasAccepted()
                        ? R.string.pair_connected : R.string.pair_expired);
                    return;
                }
                handler.postDelayed(this, 500);
            }
        };
        dialog.setOnDismissListener(ignored -> { Pairing.close(); handler.removeCallbacks(tick); });
        dialog.show();
        handler.postDelayed(tick, 500);
    }
    @Override protected void onResume() {
        super.onResume();
        RemoteKeys.configuring = true;
        RemoteKeys.captureHandler = this::captureFromService;
        handler.post(update);
    }
    @Override protected void onPause() {
        RemoteKeys.configuring = false;
        RemoteKeys.captureHandler = null;
        capturing = testing = false;
        capturedKey = 0;
        updateLabel();
        handler.removeCallbacks(update);
        network.stopSearch();
        super.onPause();
    }
    @Override protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        network.close();
        Pairing.close();
        super.onDestroy();
    }
    private boolean captureFromService(KeyEvent event) {
        if (!capturing && !testing && capturedKey != event.getKeyCode()) return false;
        return dispatchKeyEvent(event);
    }
    @Override public boolean dispatchKeyEvent(KeyEvent event) {
        int key = event.getKeyCode();
        if (capturedKey != 0 && capturedKey == key) {
            if (event.getAction() == KeyEvent.ACTION_UP) capturedKey = 0;
            return true;
        }
        if (testing) {
            if (key == KeyEvent.KEYCODE_BACK) {
                testing = false;
                capturedKey = key;
                status.setText(R.string.cancelled);
                return true;
            }
            if (key != preferences.getInt(RemoteKeys.MODIFIER, 0)) return super.dispatchKeyEvent(event);
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                capturedKey = key;
                testing = false;
                status.setText(getString(R.string.test_ok, KeyEvent.keyCodeToString(key)));
            }
            return true;
        }
        if (!capturing) return super.dispatchKeyEvent(event);
        if (event.getAction() != KeyEvent.ACTION_DOWN || event.getRepeatCount() != 0) return true;
        if (key == KeyEvent.KEYCODE_BACK) {
            capturing = false;
            capturedKey = key;
            updateLabel();
            status.setText(R.string.cancelled);
        } else if (key == KeyEvent.KEYCODE_UNKNOWN || key == KeyEvent.KEYCODE_HOME
                || key == KeyEvent.KEYCODE_POWER || key == KeyEvent.KEYCODE_ENTER
                || (key >= KeyEvent.KEYCODE_DPAD_UP && key <= KeyEvent.KEYCODE_DPAD_CENTER)) {
            status.setText(getString(R.string.rejected, KeyEvent.keyCodeToString(key)));
        } else {
            preferences.edit().putInt(RemoteKeys.MODIFIER, key).apply();
            capturing = false;
            capturedKey = key;
            updateLabel();
            status.setText(getString(R.string.saved, KeyEvent.keyCodeToString(key)));
        }
        return true;
    }
}

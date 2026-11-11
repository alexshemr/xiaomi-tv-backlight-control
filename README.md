# Xiaomi TV Backlight Control

Adjust Xiaomi TV backlight from the remote. Assign PatchWall or another supported
button in the app. Press it to open the brightness slider, then use ↑ / ↓ to
adjust the level. Press the button again or Back to close the slider.

The optional [Home Assistant integration](https://github.com/alexshemr/ha-xiaomi-tv-backlight)
adds a backlight entity for automations, such as dimming the screen at night.

## Installation and permissions

1. Temporarily enable ADB debugging on the TV. Download the [APK](https://github.com/alexshemr/xiaomi-tv-backlight-control/releases/latest)
   and install it with ADB:

   ```sh
   adb connect TV_IP:5555
   adb install -r xiaomi-tv-backlight-control.apk
   adb shell pm grant local.ha.tvbacklight android.permission.WRITE_SECURE_SETTINGS
   ```

   Accept the debugging prompt on the TV when connecting. The last command grants
   permission to change the TV backlight setting.

2. Open TV Settings → Device preferences → Accessibility and enable
   **Xiaomi TV Backlight Control**. On a Russian system, the service is called
   **Подсветка Xiaomi TV**. This lets the app receive the shortcut and arrow keys
   while another app is open. If the in-app shortcut cannot open this section,
   open it through the TV settings.

3. Return to the app, choose **Choose shortcut** and press PatchWall.
   The assigned button opens the backlight slider instead of its original action.
   Use **Test shortcut** to check other buttons. Some firmware handles app buttons
   before the accessibility service can intercept them.

4. Turn off ADB. The shortcut and Home Assistant connection work without it,
   including after a reboot.

## Tested setup

**Xiaomi MiTV-MSSP1 (nino), Android TV 9, PI.5925.**
English and Russian, with the system light or dark theme.

The tested TV uses [CleanYourTV](https://github.com/Gathin23/cleanyourtv).
PatchWall is disabled, and its button opens the backlight slider.
Other Xiaomi firmware may handle backlight settings or remote keys differently.

[Русский](README.ru.md)

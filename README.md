# Xiaomi TV Backlight Control

Adjust Xiaomi TV backlight from the remote. Assign PatchWall or another supported
button in the app. Press it to open the brightness slider, then use ↑ / ↓ to
adjust the level. Press the button again or Back to close the slider.

![Backlight control with the PatchWall button](docs/backlight-demo.gif)

The optional [Home Assistant integration](https://github.com/alexshemr/ha-xiaomi-tv-backlight)
adds a backlight entity for automations, such as dimming the screen at night.

## Installation and permissions

1. Download [Platform-Tools for Windows](https://developer.android.com/tools/releases/platform-tools)
   and extract to `C:\platform-tools`. Download the [APK](https://github.com/alexshemr/xiaomi-tv-backlight-control/releases/latest)
   into the same folder, or install it on the TV from a USB drive.

2. Connect the PC and TV to the same local network and enable ADB debugging on the TV.
   Open PowerShell and run:

   ```powershell
   cd C:\platform-tools

   # Replace 192.168.1.1 with your TV's IP address on the local network
   .\adb.exe connect 192.168.1.1:5555
   ```

   Accept the debugging prompt on the TV, then run:

   ```powershell
   # Skip this line if the APK is already installed from USB
   .\adb.exe install -r xiaomi-tv-backlight-control.apk
   .\adb.exe shell pm grant local.ha.tvbacklight android.permission.WRITE_SECURE_SETTINGS
   ```

   The last command grants backlight access once. It is required even when
   installing from USB and cannot be replaced by a standard settings toggle.

3. Open TV Settings → Device preferences → Accessibility and enable
   **Xiaomi TV Backlight Control**. On a Russian system, the service is called
   **Подсветка Xiaomi TV**. This lets the app receive the shortcut and arrow keys
   while another app is open. If the in-app shortcut cannot open this section,
   open it through the TV settings.

4. Return to the app, choose **Choose shortcut** and press PatchWall.
   The assigned button opens the backlight slider instead of its original action.
   Use **Test shortcut** to check other buttons. Some firmware handles app buttons
   before the accessibility service can intercept them.

5. Turn off ADB. The shortcut and Home Assistant connection work without it,
   including after a reboot.

## Screen off

In the app, optionally enable **Turn the screen off at 0%**, assign a separate
screen-off button, or use both. Both options are disabled by default.
The 0% option also applies to Home Assistant commands.

While the screen is off, Up restores the image from 0% at 5%. Other remote buttons
restore the image without passing that press to the app underneath. Power and the
assigned screen-off button are excluded. Navigation, volume, menu, power and
voice-assistant buttons cannot be assigned as shortcuts.

## Tested setup

**Xiaomi MiTV-MSSP1 (nino), Android TV 9, PI.5925.**
English and Russian, with the system light or dark theme.

The tested TV uses [CleanYourTV](https://github.com/Gathin23/cleanyourtv).
PatchWall is disabled, and its button opens the backlight slider.
Other Xiaomi firmware may handle backlight settings or remote keys differently.

[Русский](README.ru.md)

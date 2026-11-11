# Xiaomi TV Backlight Control

Control Xiaomi TV backlight with the PatchWall button.

Assign PatchWall in the app. Press it to open the brightness slider, then use ↑ / ↓.
Press PatchWall again or Back to close it.

## Install

Download the [APK](https://github.com/alexshemr/xiaomi-tv-backlight-control/releases/latest), install it and grant permission:

```sh
adb install -r xiaomi-tv-backlight-control.apk
adb shell pm grant local.ha.tvbacklight android.permission.WRITE_SECURE_SETTINGS
```

Enable the app in Accessibility settings, assign the button and turn off ADB.

Tested on **MiTV-MSSP1, Android TV 9, PI.5925**.
English and Russian. System light/dark theme.

[Home Assistant integration](https://github.com/alexshemr/ha-xiaomi-tv-backlight) adds a backlight entity for automations.

The tested TV uses [CleanYourTV](https://github.com/Gathin23/cleanyourtv).
PatchWall is disabled. Its button opens the backlight slider.

[Русский](README.ru.md)

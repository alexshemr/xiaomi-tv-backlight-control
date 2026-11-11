# Xiaomi TV Backlight Control

Give the PatchWall button a useful new job. Press it to open a slim backlight
panel, then use Up and Down to make your Xiaomi TV comfortable to watch.

<img src="res/drawable/app_icon.png" alt="Xiaomi TV Backlight Control icon" width="112">

## What it does

- A single remote shortcut opens the panel over the app you are watching.
- Up and Down adjust the physical screen backlight by 5 percentage points.
  Holding an arrow repeats the adjustment.
- Press the shortcut again, press Back, or wait five seconds to dismiss.
- Navigation keys are consumed while the panel is open.
- The app follows the system light or dark theme.
- English is the default language. Russian follows a Russian system locale.
- The remote service and optional local API start after reboot, with ADB disabled.

**Tested device:** Xiaomi MiTV-MSSP1 (nino), Android TV 9, firmware PI.5925.
Remote control, physical backlight changes, Home Assistant pairing and remote
control after reboot were tested on this device. Other Xiaomi TVs may use a
different backlight implementation.

## Reuse the PatchWall button

This app was tested alongside **CleanYourTV**, whose launcher uses the package
`com.gathin.tvhome`. After replacing the stock launcher and disabling PatchWall
on the tested TV, its otherwise unused button became the backlight shortcut.
Choose that button in this app to open brightness control in place of PatchWall.

Follow the launcher replacement and recovery instructions in
[CleanYourTV](https://github.com/Gathin23/cleanyourtv).

The app itself does not disable or uninstall PatchWall. Choose another spare
button if you keep using it. App buttons such as Netflix can also be assigned
when the firmware delivers their key events to accessibility. Use **Test
shortcut** to verify this. A firmware shortcut that launches an app before
accessibility receives the event may still run its original action.

## Install

Download the APK from [Releases](https://github.com/alexshemr/xiaomi-tv-backlight-control/releases).
Enable ADB temporarily, connect to your TV, install and grant backlight access:

```sh
adb connect TV_IP:5555
adb install -r xiaomi-tv-backlight-control.apk
adb shell pm grant local.ha.tvbacklight android.permission.WRITE_SECURE_SETTINGS
```

Open **Xiaomi TV Backlight Control**. Enable its accessibility service in the TV
settings, choose a spare button, then test it on the home screen and in a video
app. Home, Back, Power, OK and arrow buttons remain reserved for navigation.
The chosen shortcut replaces its normal action.

Some Xiaomi firmware hides the accessibility screen. If needed, first read:

```sh
adb shell settings get secure enabled_accessibility_services
```

Append `local.ha.tvbacklight/local.ha.tvbacklight.RemoteKeys` to the existing
colon-separated list, preserving other enabled services, and save it using
`settings put secure enabled_accessibility_services`. Then run
`adb shell settings put secure accessibility_enabled 1`.

Turn off ADB after setup. Only one accessibility service can filter keys at a
time. Check for conflicts with other button remappers.

## Optional Home Assistant automation

The remote shortcut is the main feature. The companion integration adds a local
backlight number so automations can dim the screen for sleep or restore a daytime
level.

Install [Xiaomi TV Backlight for Home Assistant](https://github.com/alexshemr/ha-xiaomi-tv-backlight)
through HACS as a custom integration repository, then restart Home Assistant.

On the TV, choose **Find Home Assistant** or enter the server URL. Open **Pair
with Home Assistant**. In HA, add the discovered integration, or enter the TV
address manually, and type the six-digit code shown on the TV. The QR code opens
the setup page on the selected HA server.

Pairing lasts two minutes and allows five attempts. HA receives a dedicated TV
API token. The TV never needs your HA account password or an HA administrator
token. Re-pairing replaces the integration token.

HA polls every 15 seconds and confirms commands immediately. When the TV is
offline, the backlight number displays 0 and the diagnostic connection sensor
turns off. Zero is a display fallback. The integration does not wake the TV.

## Build and checks

Use JDK 17, Android SDK Platform 34 and Build Tools 34.0.0:

```powershell
./build.ps1 -Sdk <android-sdk> -Jdk <jdk-17>
& <jdk-17>/bin/javac -d build/test src/KeyPanel.java tests/KeyPanelTest.java
& <jdk-17>/bin/java -cp build/test local.ha.tvbacklight.KeyPanelTest
```

The build downloads ZXing Core 3.5.3 from Maven Central with a pinned SHA-256.
The output is `build/tv-backlight.apk`. Preserve the private signing key at
`build/local-signing.p12` for future updates. GitHub Actions can restore it from
the `TV_SIGNING_KEY_BASE64` secret. Without that secret, Actions produces
development APKs signed with a temporary key. Those builds cannot update an APK
signed with the release key.

The app exposes a token-protected HTTP API on port 8765 for trusted local
networks. Keep it on your LAN. The backlight driver uses the Xiaomi setting
`picture_backlight`, with values from 0 to 100.

[Краткая инструкция на русском](README.ru.md)


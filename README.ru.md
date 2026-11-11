# Xiaomi TV Backlight Control

Управляйте подсветкой Xiaomi TV кнопкой Play.

Назначьте Play в приложении. Нажмите её, чтобы открыть шкалу, затем используйте ↑ / ↓.
Повторное нажатие Play или «Назад» закрывает шкалу.

## Установка

Скачайте [APK](https://github.com/alexshemr/xiaomi-tv-backlight-control/releases/latest), установите и выдайте разрешение:

```sh
adb install -r xiaomi-tv-backlight-control.apk
adb shell pm grant local.ha.tvbacklight android.permission.WRITE_SECURE_SETTINGS
```

Включите службу приложения в специальных возможностях, назначьте кнопку и выключите ADB.

Проверено на **MiTV-MSSP1, Android TV 9, PI.5925**.
Английский и русский языки. Системная светлая и тёмная тема.

[Интеграция Home Assistant](https://github.com/alexshemr/ha-xiaomi-tv-backlight) добавляет сущность подсветки для автоматизаций.

На тестовом ТВ установлен [CleanYourTV](https://github.com/Gathin23/cleanyourtv).
После отключения PatchWall его кнопку тоже можно назначить для управления подсветкой.

[English](README.md)

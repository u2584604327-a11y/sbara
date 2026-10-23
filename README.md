# BaraPause 🦫🌱

**A gentle pause for mindless scrolling.**

BaraPause is a capybara-themed screen-time reminder for Android. Choose the apps you want to keep an eye on, set your own time limits, and get a friendly reminder when it may be time for a break.

BaraPause is built around **awareness and personal choice**, not unbreakable phone lockouts.

## Features

- **App-specific reminders:** set a usage limit for each selected app.
- **A friendly capybara:** customize the reminder card, its text, appearance, and supported animations.
- **Focus your way:** temporary focus sessions, scheduled study mode, break reminders, and limited extensions.
- **Up to eight local profiles:** keep separate configurations for studying, everyday use, or downtime. Switch profiles and back up their settings as JSON.
- **Quick access:** home-screen widgets, favorite apps, recent search terms, and launch/search shortcuts.
- **Personalize your experience:** light, dark, and eye-comfort themes, with multiple interface languages.
- **Local by design:** no account required and no app usage history uploaded to a server.

Feature availability and background behavior may vary across Android versions and device manufacturers.

## How it works

BaraPause uses Android's usage-access API to observe selected app activity and a user-enabled foreground service to track time. When appropriate, a small overlay card invites you to pause. It does **not** rely on an Accessibility Service or inspect the contents of your screen.

The core permissions are:

| Permission | Why it is needed |
| --- | --- |
| Usage access (`PACKAGE_USAGE_STATS`) | Detect activity in apps you choose to monitor. |
| Display over other apps (`SYSTEM_ALERT_WINDOW`) | Show the reminder card while another app is open. |
| Foreground-service notifications | Make monitoring visible and support its operation. |

**Important:** Android and device-manufacturer background restrictions may interrupt monitoring. BaraPause is not a system-level app blocker, and it cannot guarantee uninterrupted operation on every phone or restart itself after every user-initiated stop.

## Development status

**Android: Public test builds / ongoing development.**

The latest V1.8.1 development build includes multi-profile backup validation and best-effort monitoring recovery after an app update. The build passed automated Gradle and static checks; behavior on specific phones still needs device testing.

- [Current Android development branch](https://github.com/u2584604327-a11y/sbara/tree/feature/v1_8_1-backup-reliability)
- [Verified V1.8.1 Android build](https://github.com/u2584604327-a11y/sbara/actions/runs/39889953321)
- [Open development changes and review](https://github.com/u2584604327-a11y/sbara/pulls)

> The `main` branch contains an earlier baseline. The newer features described above are in development branches and preview builds, not necessarily in the `main` branch.

### Build from source

The Android code uses **Kotlin and native Android Views**. With the Android SDK and Gradle installed:

```bash
gradle :app:assembleDebug
```

Output: `app/build/outputs/apk/debug/app-debug.apk`

GitHub Actions builds can also provide temporary debug APK artifacts. Preview versions may use separate Android package IDs, so their app data does not automatically transfer between installations. Export local settings before changing preview packages.

## iOS companion

An independent native SwiftUI edition is under development:

**[BaraPause for iOS](https://github.com/u2584604327-a11y/BaraPause-iso)**

The iOS Simulator build has succeeded, but a signed, physical-iPhone-installable IPA has **not** been released. iOS uses Apple's Screen Time frameworks and Shield interface rather than Android overlays.

---

**Your time, your choice — with a little help from a capybara.**

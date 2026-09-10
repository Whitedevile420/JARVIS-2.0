# JARVIS 2.0 — automatic APK build

This project was upgraded from the legacy Gradle/Android plugin setup to:
- Gradle 8.7
- Android Gradle Plugin 8.5.2
- Kotlin 1.9.24
- compileSdk/targetSdk 35

## Build
Push this folder to a GitHub repository. The workflow at `.github/workflows/android.yml` automatically installs JDK 17 and the Android SDK, then runs `assembleDebug`.

The resulting APK is published in the workflow's **Artifacts** section as `JARVIS-2.0-debug`.

The app still follows Android permission and role restrictions. It does not bypass OS security controls.

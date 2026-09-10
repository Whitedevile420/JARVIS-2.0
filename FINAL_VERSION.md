JARVIS 2.0 - LIVE HUD FINAL SOURCE

Version: 2.0-LIVE-HUD

Implemented in this source:
- Futuristic Live HUD main screen
- Voice input and Text-to-Speech output
- Start/Stop JARVIS
- Permission Center with Android runtime permission requests
- Floating HUD overlay
- Installed launcher-app discovery and voice launching
- Voice calling by contact/number
- Dialer launch
- Contact lookup/save/delete flows
- SMS flow
- Camera/selfie flow
- Wi-Fi/Bluetooth settings shortcuts
- Android-safe limitations for force-close and call control
- Cybersecurity learning/authorized-testing architecture boundary

Important Android limitation:
A normal app cannot bypass Android security. Answer/end calls programmatically requires the appropriate Telecom/default-dialer privileges and OS support. The project reports this limitation instead of pretending it has unrestricted access.

Build note:
The supplied project uses Gradle 6.1.1. This environment does not contain the Gradle distribution or Android SDK, and outbound network access is unavailable, so an independently compiled APK could not be verified here.

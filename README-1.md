# 🤖 JARVIS 2.0

### Complete Installation, Setup & Build Guide

JARVIS 2.0 is an Android AI assistant project with a futuristic Live HUD, voice input/output, Android permission management, app launching, calling, contacts, SMS, camera features, settings shortcuts, and an authorized cybersecurity-learning architecture.

This guide covers setup and installation on:

- 📱 AndroidIDE — Android phone
- 📱 Termux — Android phone
- 🪟 Windows — PC/Laptop
- 🐧 Linux — PC/Laptop
- ☁️ GitHub Actions — Build from GitHub without a local Android development environment

---

## 📋 Current Project Build Configuration

The current JARVIS 2.0 project uses:

| Component | Version |
|---|---|
| Gradle | 8.7 |
| Android Gradle Plugin | 8.5.2 |
| Kotlin | 1.9.24 |
| Compile SDK | 35 |
| Target SDK | 35 |
| Recommended JDK | 17 |

The repository includes a Gradle Wrapper, so installing Gradle globally is normally unnecessary. Always prefer the project's `gradlew` / `gradlew.bat` wrapper.

---

# 📱 1. AndroidIDE — Build JARVIS Directly on Android

AndroidIDE is the recommended option when you want to develop and build JARVIS 2.0 directly on an Android phone.

## Requirements

Recommended:

- Android phone with 64-bit ARM (`arm64-v8a`)
- At least 2 GB free RAM
- At least 4 GB free storage
- Stable internet connection
- AndroidIDE
- JDK 17
- Android SDK
- Android SDK Build Tools

> ⚠️ Install AndroidIDE only from a trusted official distribution/source. Avoid modified APKs from unknown websites.

## Step 1 — Install AndroidIDE

Install AndroidIDE and open it.

On first launch, allow the permissions requested by AndroidIDE for its development environment.

## Step 2 — Open the AndroidIDE Terminal

Open the built-in terminal and update its environment:

```bash
pkg upgrade
```

## Step 3 — Install the Android Build Environment

Run the AndroidIDE setup utility:

```bash
idesetup -c
```

Follow the prompts and install the required JDK/Android SDK build environment.

For this project, use **JDK 17** where the setup asks for a Java version.

## Step 4 — Verify Java

```bash
java --version
```

Make sure Java 17 is available.

## Step 5 — Install Git if Required

```bash
pkg install git
```

Verify:

```bash
git --version
```

## Step 6 — Clone JARVIS 2.0

```bash
cd ~
git clone https://github.com/Whitedevile420/JARVIS-2.0.git
cd JARVIS-2.0
```

## Step 7 — Make the Gradle Wrapper Executable

```bash
chmod +x gradlew
```

## Step 8 — Build the Debug APK

```bash
./gradlew assembleDebug
```

The first build can take longer because Gradle downloads required dependencies.

## Step 9 — Locate the APK

After a successful build, the debug APK is normally located at:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Step 10 — Open the Project in AndroidIDE

In AndroidIDE:

1. Choose **Open Existing Project**.
2. Select the `JARVIS-2.0` directory.
3. Wait for Gradle synchronization.
4. Resolve any SDK/JDK prompt using the project requirements above.
5. Build/run the application.

AndroidIDE can be used to open and build existing Gradle-based Android projects.

---

# 📱 2. Termux — Build JARVIS on Android

Termux provides a Linux-like terminal environment directly on Android.

This method is intended for users who prefer command-line development.

> ⚠️ A complete Android native build environment from plain Termux can require additional Android SDK/build-tool configuration. If you want the easiest full Android development experience on a phone, use AndroidIDE. The Gradle Wrapper remains the preferred way to invoke this project's Gradle version.

## Step 1 — Install Termux

Install Termux from a trusted official distribution/source.

Open Termux after installation.

## Step 2 — Update Packages

```bash
pkg update
pkg upgrade
```

## Step 3 — Install Git

```bash
pkg install git
```

Verify:

```bash
git --version
```

## Step 4 — Configure Storage Access

If you want to access your Android Downloads/shared storage:

```bash
termux-setup-storage
```

Approve the Android permission request.

## Step 5 — Check Available OpenJDK Packages

```bash
pkg search openjdk
```

Install a JDK version compatible with the current project. For the current JARVIS build configuration, **JDK 17** is recommended.

On Termux distributions where the package is available under this name:

```bash
pkg install openjdk-17
```

If the package name differs in your Termux repository, install the available JDK 17 package shown by `pkg search`.

Verify:

```bash
java --version
```

## Step 6 — Clone the Repository

```bash
cd ~
git clone https://github.com/Whitedevile420/JARVIS-2.0.git
cd JARVIS-2.0
```

## Step 7 — Make Gradle Wrapper Executable

```bash
chmod +x gradlew
```

## Step 8 — Build

```bash
./gradlew assembleDebug
```

## Step 9 — Find the APK

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Step 10 — Copy APK to Android Downloads

If Termux storage access was enabled:

```bash
cp app/build/outputs/apk/debug/app-debug.apk ~/storage/downloads/
```

Then open the Android Downloads folder and install the APK.

---

# 🪟 3. Windows — Complete Setup

## Requirements

Install:

- Windows 10 or Windows 11
- Git
- Android Studio
- JDK 17
- Android SDK
- Android SDK Platform 35
- Android SDK Build-Tools
- Android SDK Platform-Tools

## Step 1 — Install Git

Install Git for Windows from its official distribution.

Verify in PowerShell:

```powershell
git --version
```

## Step 2 — Install Android Studio

Install Android Studio.

Open:

```text
Android Studio
→ Tools
→ SDK Manager
```

Make sure these components are installed:

```text
Android SDK Platform 35
Android SDK Build-Tools
Android SDK Platform-Tools
Android SDK Command-line Tools
```

## Step 3 — Configure JDK 17

In Android Studio:

```text
File
→ Settings
→ Build, Execution, Deployment
→ Build Tools
→ Gradle
```

Select a JDK 17 installation.

## Step 4 — Clone JARVIS 2.0

Open PowerShell:

```powershell
git clone https://github.com/Whitedevile420/JARVIS-2.0.git
cd JARVIS-2.0
```

## Step 5 — Build the Debug APK

```powershell
.\gradlew.bat assembleDebug
```

## Step 6 — Locate the APK

```text
app\build\outputs\apk\debug\app-debug.apk
```

## Step 7 — Build Through Android Studio

Alternatively:

1. Open Android Studio.
2. Select **Open**.
3. Select `JARVIS-2.0`.
4. Wait for Gradle Sync.
5. Connect an Android phone or create an emulator.
6. Press **Run ▶**.

---

# 📲 Windows — Install APK with ADB

Enable on the Android phone:

```text
Developer Options
→ USB Debugging
```

Connect the phone to the PC.

Verify the device:

```powershell
adb devices
```

Accept the USB debugging prompt on the phone if displayed.

Install:

```powershell
adb install app\build\outputs\apk\debug\app-debug.apk
```

If an older version is already installed and the signing/build permits an update, use:

```powershell
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

---

# 🐧 4. Linux — Complete Setup

These commands are primarily for Debian/Ubuntu-based Linux distributions.

## Requirements

Install:

- Linux
- Git
- JDK 17
- Android Studio or Android SDK command-line tools
- Android SDK Platform 35
- Android SDK Build-Tools
- Android SDK Platform-Tools

## Step 1 — Update the System

```bash
sudo apt update
sudo apt upgrade
```

## Step 2 — Install Git

```bash
sudo apt install git
```

Verify:

```bash
git --version
```

## Step 3 — Install JDK 17

```bash
sudo apt install openjdk-17-jdk
```

Verify:

```bash
java --version
javac --version
```

## Step 4 — Install Android Studio

Install Android Studio from the official Android developer distribution.

Open:

```text
Android Studio
→ Tools
→ SDK Manager
```

Install:

```text
Android SDK Platform 35
Android SDK Build-Tools
Android SDK Platform-Tools
Android SDK Command-line Tools
```

## Step 5 — Configure Android SDK Environment

A common Android SDK location is:

```text
$HOME/Android/Sdk
```

Add the following to `~/.bashrc` if that is your SDK location:

```bash
export ANDROID_HOME="$HOME/Android/Sdk"
export PATH="$ANDROID_HOME/platform-tools:$ANDROID_HOME/cmdline-tools/latest/bin:$PATH"
```

Reload:

```bash
source ~/.bashrc
```

Verify:

```bash
adb --version
```

And:

```bash
sdkmanager --version
```

> If your SDK is installed at another location, replace `$HOME/Android/Sdk` with the actual SDK path.

## Step 6 — Clone JARVIS 2.0

```bash
git clone https://github.com/Whitedevile420/JARVIS-2.0.git
cd JARVIS-2.0
```

## Step 7 — Make Gradle Wrapper Executable

```bash
chmod +x gradlew
```

## Step 8 — Build

```bash
./gradlew assembleDebug
```

## Step 9 — Find APK

```text
app/build/outputs/apk/debug/app-debug.apk
```

---

# 📲 Linux — Install APK with ADB

Enable USB debugging on the Android phone.

Connect the phone:

```bash
adb devices
```

Then install:

```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

For an update installation:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

---

# ☁️ 5. GitHub Actions — Build Without Android Studio

JARVIS 2.0 includes an Android GitHub Actions workflow that can build the debug APK remotely.

This is especially useful if you are developing from an Android phone and do not want to install a complete Android SDK locally.

## Step 1 — Open the Repository

Open:

```text
https://github.com/Whitedevile420/JARVIS-2.0
```

## Step 2 — Open Actions

Go to:

```text
Repository
→ Actions
```

## Step 3 — Select the Android Build Workflow

Open the Android build workflow.

If manual execution is enabled, select:

```text
Run workflow
```

## Step 4 — Wait for the Build

The workflow sets up JDK 17 and Android SDK and runs the debug build.

The resulting artifact is published as:

```text
JARVIS-2.0-debug
```

## Step 5 — Download the APK

After the workflow completes:

```text
Actions
→ Completed workflow run
→ Artifacts
→ JARVIS-2.0-debug
```

Download the artifact ZIP and extract the APK.

---

# 🔄 6. Update an Existing Clone

If the repository is already cloned:

```bash
cd JARVIS-2.0
git pull
```

Then build again:

```bash
./gradlew assembleDebug
```

Windows:

```powershell
.\gradlew.bat assembleDebug
```

---

# 🧹 7. Clean Build

If you encounter Gradle/build-cache problems:

Linux / AndroidIDE / Termux:

```bash
./gradlew clean
./gradlew assembleDebug
```

Windows:

```powershell
.\gradlew.bat clean
.\gradlew.bat assembleDebug
```

---

# 🔍 8. Check Gradle Tasks

Linux / AndroidIDE / Termux:

```bash
./gradlew tasks
```

Windows:

```powershell
.\gradlew.bat tasks
```

This displays the Gradle tasks available in the project.

---

# 📦 9. APK Output Location

The debug APK is normally generated at:

### Linux / AndroidIDE / Termux

```text
app/build/outputs/apk/debug/app-debug.apk
```

### Windows

```text
app\build\outputs\apk\debug\app-debug.apk
```

---

# 🔐 10. Release APK & Signing

The debug APK is intended for development and testing.

For public distribution, create a properly signed release build using your own private signing key.

### Never upload private signing credentials to GitHub.

Do not commit files such as:

```text
*.jks
*.keystore
keystore.properties
local.properties
```

Add sensitive files to `.gitignore` where appropriate.

---

# 🔑 11. Android Permissions

JARVIS may request runtime permissions depending on the enabled functionality.

Possible permissions/features include:

- 🎤 Microphone
- 📞 Phone
- 👤 Contacts
- 💬 SMS
- 📷 Camera
- 📍 Location
- 🔔 Notifications
- 🪟 Overlay/Floating HUD
- 📶 Wi-Fi/Bluetooth-related settings

Grant only the permissions required for the features you intend to use.

If a permission is denied, Android may prevent the corresponding feature from working.

---

# 🧠 12. JARVIS 2.0 Features

The current source includes functionality such as:

- 🖥️ Futuristic Live HUD
- 🎤 Voice input
- 🔊 Text-to-Speech
- ▶️ Start/Stop JARVIS
- 🔐 Permission Center
- 🪟 Floating HUD overlay
- 📱 Installed launcher-app discovery
- 🚀 Voice app launching
- 📞 Voice calling
- ☎️ Dialer launching
- 👤 Contact lookup
- ➕ Contact save flow
- 🗑️ Contact delete flow
- 💬 SMS flow
- 📷 Camera/selfie flow
- 📶 Wi-Fi settings shortcut
- 🔵 Bluetooth settings shortcut
- 🛡️ Android-safe security limitations
- 🧑‍💻 Cybersecurity learning / authorized-testing architecture

---

# ⚠️ 13. Android Security Limitations

JARVIS 2.0 does **not** bypass Android security controls.

Some Android operations require special permissions, system roles, default-app privileges, or OS-level support.

For example, programmatically answering or ending calls can require the appropriate Telecom/default-dialer privileges and Android OS support.

The application should report Android restrictions instead of attempting to bypass them.

---

# 🛡️ 14. Cybersecurity Usage

Cybersecurity-related functionality is intended for:

- Security education
- Authorized penetration testing
- Personal laboratories
- Defensive security research
- Cybersecurity experimentation
- Systems and applications you own or are explicitly authorized to test

Only test systems, networks, devices, or applications where you have permission.

---

# 🆘 15. Troubleshooting

## `Permission denied: ./gradlew`

Run:

```bash
chmod +x gradlew
```

Then:

```bash
./gradlew assembleDebug
```

## `JAVA_HOME` or Java errors

Check:

```bash
java --version
```

The current project recommends JDK 17.

## Android SDK not found

Make sure Android SDK is installed and the SDK path/environment variables are configured correctly.

For Linux, verify:

```bash
echo $ANDROID_HOME
```

Then:

```bash
adb --version
```

## SDK Platform 35 not found

Open Android Studio → SDK Manager and install:

```text
Android SDK Platform 35
```

## Gradle Sync Failed

Try:

```bash
./gradlew clean
./gradlew assembleDebug
```

On Windows:

```powershell
.\gradlew.bat clean
.\gradlew.bat assembleDebug
```

Also check:

- JDK 17
- Android SDK Platform 35
- Internet connection
- Available storage
- Gradle dependency availability

## First build is slow

The first build may take longer because Gradle needs to download the configured Gradle distribution, Android Gradle Plugin, Kotlin plugin, Android dependencies, and other Maven dependencies.

Subsequent builds are normally faster because dependencies are cached.

---

# 📊 16. Which Installation Method Should I Use?

| Method | Device | Difficulty | Recommended For |
|---|---|---:|---|
| 📱 AndroidIDE | Android phone | ⭐⭐ | Full development on phone |
| 📱 Termux | Android phone | ⭐⭐⭐⭐ | Advanced command-line users |
| 🪟 Windows + Android Studio | PC/Laptop | ⭐⭐ | Full Android development |
| 🐧 Linux + Android Studio | PC/Laptop | ⭐⭐⭐ | Advanced development |
| ☁️ GitHub Actions | Any device | ⭐ | Remote APK building |

### Recommended workflow

**Android phone:** AndroidIDE ⭐⭐⭐⭐⭐

**Android phone + terminal:** Termux ⭐⭐⭐

**Windows:** Android Studio + Gradle Wrapper ⭐⭐⭐⭐⭐

**Linux:** Android Studio + Gradle Wrapper ⭐⭐⭐⭐⭐

**No local Android environment:** GitHub Actions ⭐⭐⭐⭐⭐

---

# 🔗 Repository

**JARVIS 2.0**

https://github.com/Whitedevile420/JARVIS-2.0

---

# 📚 Official Documentation

For additional information, consult the official documentation for:

- Android Developers
- Android Studio
- AndroidIDE
- Termux
- Gradle
- GitHub Actions

---

# 📜 Important Notice

JARVIS 2.0 is an educational and development project.

Do not use this software to access, control, monitor, attack, or modify systems without authorization.

Respect Android security controls, user privacy, applicable laws, and the permissions granted by the device owner.

---

## 👨‍💻 Author

**Whitedevile420**

### JARVIS 2.0

> Just A Rather Very Intelligent System

# MiniMessenger ⚡

**MiniMessenger** is a lightweight, privacy-focused native Android wrapper for Facebook Messenger. It provides a robust "app-like" experience for the web interface, enhancing privacy, performance, and usability without the bloat of the official application.

## 🚀 Key Features

### 🎨 Native Experience
- **Native Bottom Navigation:** Seamlessly switch between Chats, Active People, and Settings.
- **Collapsing Toolbar:** Maximizes screen space while scrolling.
- **Pull-to-Refresh:** Standard gesture support to reload conversations.
- **Deep Linking:** Automatically handles `messenger.com` and `m.facebook.com/messages` links.
- **Share Target:** Share text and images from other apps directly to MiniMessenger.

### 🛡️ Privacy & Security
- **Biometric App Lock:** Secure your chats with Fingerprint or Face Unlock (using `androidx.biometric`).
- **Sandboxed Webview:** Runs in a controlled environment.
- **No Tracking:** We do not collect any user data; everything stays on your device.

### ⚙️ Customization & Power User Features
- **Dark Mode:** Enforced dark theme via custom CSS injection.
- **Clutter Removal:** Automatically hides "Story Tray", "Install App" banners, and other distractions.
- **Desktop Mode:** Toggle to spoof a Desktop User-Agent for accessing full-site features.
- **Text Zoom:** granular control (50% - 150%) over text size.
- **Data Saver Mode:** Aggressively blocks images to save bandwidth on metered connections.

### 📂 File Management
- **File Uploads:** Native file picker integration for sending photos/videos.
- **Downloads:** Save received images and files directly to your device.

## 🛠️ Tech Stack
- **Language:** Kotlin
- **Architecture:** MVVM-ish (Logic centralized in MainActivity for wrapper simplicity)
- **Core Components:**
  - `WebView` (Custom `WebChromeClient` & `WebViewClient`)
  - `ConstraintLayout` & `CoordinatorLayout`
  - `androidx.biometric`
  - `androidx.swiperefreshlayout`
- **Dynamic Injection:** Custom Javascript/CSS injection engine (`injector.js`) configured via Kotlin.

## 🏗️ Build Instructions

1. **Prerequisites:**
   - Android Studio Iguana or newer.
   - JDK 17+.
   - Android SDK API 34.

2. **Clone & Build:**
   ```bash
   git clone https://github.com/yourusername/minimessenger.git
   cd minimessenger
   ./gradlew assembleDebug
   ```

3. **Run:**
   - Install the APK on your device or emulator.

## ⚠️ Disclaimer
This project is an independent open-source wrapper and is **not** affiliated with, endorsed by, or connected to Meta Platforms, Inc. or Facebook. "Messenger" is a trademark of Meta Platforms, Inc.

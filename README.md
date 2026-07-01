# 📸 SnapFrame – Kotlin Multiplatform Image App

**SnapFrame** is a Kotlin Multiplatform application built with **Compose Multiplatform**, targeting **Android**, **iOS**, **Desktop**, and **Web**.

The name **SnapFrame** comes from the core idea of the app:

- **Snap**: quickly pick or capture an image
- **Frame**: crop, edit, preview, and prepare the final framed result

SnapFrame focuses on a simple cross-platform image workflow, where the main UI is shared across platforms while platform-specific image APIs are implemented separately for Android and iOS.

---

## ✨ Features

- 📲 **Cross-platform support** using Kotlin Multiplatform
- 🎨 **Shared UI** with Compose Multiplatform
- 🤖 **Android image handling** using Android Bitmap, Photo Picker, and MediaStore
- 🍏 **iOS image handling** using Skia and native Foundation APIs
- 🖼️ **Image selection flow**
- ✂️ **Image crop flow**
- 🛠️ **Image edit flow**
- 👀 **Image preview flow**
- 💾 **Save image support**
---
## 📸 Demo
### 🤖 Android

The Android app can be built and run directly in the **Android Studio emulator**.

[SnapFrame (Android Demo).webm](https://github.com/user-attachments/assets/f5b370c0-394c-4efd-8df6-0cb1c21d9dfb)


### 🍏 iOS

In the **Android Studio terminal**, from the same project folder, open the iOS project with:

```bash
open iosApp/iosApp.xcodeproj
```

https://github.com/user-attachments/assets/0f10fd50-6a47-41d8-8da8-f8d6cccfad9f




## 🛠️ Project Setup

SnapFrame was created with Kotlin Multiplatform Wizard and selected support for:

- Android
- iOS
- Desktop
- Web

<img width="448" height="516" alt="image" src="https://github.com/user-attachments/assets/a2ec5c73-c7a9-404c-a64b-8df912cb5cd1" />

---

## Project Structure

This is a Kotlin Multiplatform project using shared Compose Multiplatform UI.

```text
SnapFrame/
├── composeApp/
│   └── src/
│       ├── commonMain/
│       │   └── shared Compose UI and common business logic
│       │
│       ├── androidMain/
│       │   └── Android-specific image picker, bitmap, and save logic
│       │
│       ├── iosMain/
│       │   └── iOS-specific image picker and image processing logic
│       │
│       ├── desktopMain/
│       │   └── Desktop-specific entry point and logic
│       │
│       └── webMain/
│           └── Web-specific entry point and logic
│
├── iosApp/
│   ├── iosApp.xcodeproj/
│   └── iosApp/
│       ├── ContentView.swift
│       ├── iOSApp.swift
│       └── Info.plist
│
├── gradle/
│   └── wrapper/
│
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
└── README.md


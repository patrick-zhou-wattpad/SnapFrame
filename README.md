# 📸 SnapFrame – Kotlin Multiplatform Art Preview App

**SnapFrame** is a Kotlin Multiplatform application built with **Compose Multiplatform**, targeting **Android** and **iOS**.


This app helps users visualize how artwork will look in a real space before it is printed or installed.


The main workflow is simple:

- Select a **background image**, such as a room or wall
- Crop the background to the desired area
- Add **one or more artwork images**
- Move and resize each artwork to preview how it fits in the room
- Save the final composed image for review


The name **SnapFrame** comes from the core idea of the app:


- **Snap**: quickly select a photo of a room or space
- **Frame**: place and preview artwork in the selected environment


SnapFrame uses a shared Compose Multiplatform UI while platform-specific image APIs are implemented separately for Android and iOS.


---


## ✨ Features


- 📲 **Cross-platform support** using Kotlin Multiplatform
- 🎨 **Shared UI** with Compose Multiplatform
- 🤖 **Android image handling** using Android Bitmap, Photo Picker, and MediaStore
- 🍏 **iOS image handling** using Skia and native Foundation APIs
- 🏠 **Room or wall background image selection**
- ✂️ **Background image crop flow**
- 🖼️ **Multiple artwork overlay support**
- ↔️ **Move and resize artwork while preserving its aspect ratio**
- 👀 **Preview artwork placement in the selected room**
- 💾 **Compose and save the final image with all artwork overlays**

---


## 📸 Demo
| 🤖 Android | 🍏 iOS |
|---|---|
| <video src="https://github.com/user-attachments/assets/9554fdce-c18b-4f8c-80ca-e0a63326d667" controls></video> | <video src="https://github.com/user-attachments/assets/c055a329-8a62-4f2b-9078-c831c5800682" controls></video> |

note:
 - Android app can be built and run directly in the **Android Studio emulator**.
 - iOS app need open the iOS project in the  same project folder:
```bash
open iosApp/iosApp.xcodeproj
```
---

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
│       │   └── Shared Compose UI, editor state, and common business logic
│       │
│       ├── androidMain/
│       │   └── Android-specific image picker, bitmap composition, and save logic
│       │
│       ├── iosMain/
│       │   └── iOS-specific image picker, image composition, and save logic
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
```



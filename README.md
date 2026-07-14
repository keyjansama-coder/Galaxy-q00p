# Galaxy Tunnel

A beautiful, responsive, and secure VPN Configuration Share Hub and monitor built natively for Android using **Kotlin** and **Jetpack Compose**. 

This app is a complete and high-fidelity rewrite of the original React frontend, fully optimized for the Android platform with premium Material 3 design accents, dynamic typography, and local state persistence.

## Features

- 🛰️ **Dynamic Config Fetcher**: Automatically fetches, decodes, and parses real-time vless/trojan VPN server configurations directly from the public Raw GitHub source.
- ⚡ **Parallel TCP Socket Ping Monitor**: Measures real-time latency (ping in ms) to each server endpoint simultaneously using fast multithreaded socket connections, with color-coded speed indicators (Green/Cyan/Amber/Offline).
- 🔘 **Pulsing Power Connection Switch**: A highly interactive, custom-animated Power button with scaling pulse feedback representing the active connection state.
- 🌙 **Centralized App Theme**: Supports full Light and Dark modes.
- 👓 **Eye Comfort Mode**: Applies a soft, eye-protective amber screen overlay tint in Light mode.
- ⚙️ **Custom Visual Densities**: Allows users to dynamically scale App Font Size and Icon Density globally.
- 🛡️ **Advanced VPN Toggle Controls**: Integrated standard Material 3 Side Navigation drawer featuring advanced Kill Switch and UDP Relay switches.
- 🌐 **Full Myanmar (Burmese) & English Localization**: Real-time language toggles accessible right from the main header.
- 📁 **Local Storage Persistence**: Uses Android's `SharedPreferences` to seamlessly save and restore user preferences (theme, language, text size, icon density, advanced switches, and selected server).

## Tech Stack

- **Framework**: Jetpack Compose (Kotlin)
- **Design System**: Material Design 3 (M3)
- **Minimum SDK**: API 26 (Android 8.0+)
- **Target SDK**: API 35 (Android 15)
- **Dependency/Build System**: Gradle Kotlin DSL (.gradle.kts)
- **Core Libraries**: OkHttp, Jetpack Navigation, AndroidX Core & Lifecycle, Kotlin Coroutines

# XCan - Vehicle Diagnostics & Telemetry Dashboard

XCan is an offline-first and reactive Android application designed for real-time vehicle maintenance and OBD2 diagnostic operations.

|  |  |
| ------------- | ------------- |
| <img width="1272" height="2772" alt="Screenshot_2026-09-15-16-06-47-06_25b065d89fc63dcc79b4748f92127b83" src="https://github.com/user-attachments/assets/ce44567a-f890-422b-890f-4bf4a5c2a5db" /> | <img width="1272" height="2772" alt="Screenshot_2026-09-15-16-07-03-82_25b065d89fc63dcc79b4748f92127b83" src="https://github.com/user-attachments/assets/b84d963e-b241-4761-acb0-fe83f57544b1" /> | 
<img width="1272" height="2772" alt="Screenshot_2026-09-15-16-06-54-75_25b065d89fc63dcc79b4748f92127b83" src="https://github.com/user-attachments/assets/a86219ce-5ac2-489d-b16d-8a8ede911505" />  | <img width="1272" height="2772" alt="Screenshot_2026-09-15-16-07-10-73_25b065d89fc63dcc79b4748f92127b83" src="https://github.com/user-attachments/assets/9a4e0d03-884f-4139-a0d0-d060d0a63fb6" />  |





## Architecture

This project follows a clean multi-module architecture adhering to MVI (Model-View-Intent) principles. State flows unidirectionally, with `ViewModel`s exposing a single `StateFlow` and accepting discrete intents.

### Modules

- [`:app`](app/README.md) - Main application container, Hilt DI setup, and type-safe global navigation.
- [`:core:model`](core/model/README.md) - Pure domain models, SAE J1979 PID definitions, and interfaces shared across modules.
- [`:core:database`](core/database/README.md) - Encrypted Room Database (SQLCipher), entities, and DAOs.
- [`:core:data`](core/data/README.md) - Repositories, foreground `LoggingService`, DataStore preferences, and WorkManager sync.
- [`:core:network`](core/network/README.md) - Retrofit API service and remote DTO synchronization (offline-first architecture).
- [`:core:bluetooth`](core/bluetooth/README.md) - Kable BLE integration, ELM327 parsing, SAE J1979 PID discovery, and priority command scheduling.
- [`:core:ui`](core/ui/README.md) - Reusable Compose components, custom Canvas dials, Emil Kowalski motion tokens, and Haze glassmorphism.
- [`:feature:dashboard`](feature/dashboard/README.md) - Real-time vehicle telemetry dials, car profile switcher, and live sensor configuration.
- [`:feature:diagnostics`](feature/diagnostics/README.md) - DTC (Diagnostic Trouble Code) scanning, inspection, and safe clearing routines.
- [`:feature:config`](feature/config/README.md) - Global settings and unit preferences (Metric vs Imperial).
- [`:feature:logging`](feature/logging/README.md) - Telemetry logging session management and interactive Vico timeseries charts.
- [`:feature:maintenance`](feature/maintenance/README.md) - Vehicle service history tracking and maintenance logging timeline.

*(Note: Please refer to the `README.md` located inside each specific module directory for detailed documentation about that module's specific responsibilities and architecture).*

## Technology Stack

- **Language:** Kotlin 2.2.10
- **Android Gradle Plugin (AGP):** 9.4.0 (compileSdk 36, minSdk 33, targetSdk 36)
- **UI Toolkit:** Jetpack Compose (Material 3) with Compose BOM 2026.02.01
- **Visual Effects:** Haze (v1.7.2) for dynamic real-time glassmorphism
- **Telemetry Charting:** Vico Compose (v2.1.2)
- **Navigation:** Jetpack Navigation Compose (v2.8.0) with Kotlinx Serialization type-safe routes
- **Dependency Injection:** Dagger Hilt (v2.60.1)
- **Local Persistence & Security:** Room Database (v2.6.1) encrypted with SQLCipher (v4.6.1) and AndroidX Security Crypto MasterKey (AES256_GCM)
- **User Preferences:** Jetpack DataStore Preferences (v1.0.0)
- **Bluetooth LE:** Kable (v0.30.0)
- **Background Work & Services:** Android Foreground Service (`LoggingService`) & WorkManager (v2.9.0)
- **Networking:** Retrofit (v2.11.0) & OkHttp (v4.12.0)

## Setup & Building

1. Ensure you have Android Studio (Koala Feature Drop or newer) installed with JDK 11+.
2. Clone this repository:
   ```bash
   git clone https://github.com/pavloglez/carSync.git
   ```
3. Open the project in Android Studio.
4. Sync Gradle.
5. Run unit tests to verify system integrity:
   ```bash
   ./gradlew test
   ```
6. Build debug APK:
   ```bash
   ./gradlew assembleDebug
   ```

## Features

- **Real-Time Telemetry Dials:** Custom-drawn Compose Canvas gauges (Speed, RPM, Boost, Coolant, Load) running at 60+ FPS with zero jank.
- **Dynamic Sensor Discovery:** Discovers ECU-supported OBD-II PIDs dynamically via SAE J1979 discovery bitmasks (`0100`, `0120`, `0140`, etc.).
- **Priority Command Scheduling:** `ObdCommandScheduler` intelligently interleaves high-priority telemetry (RPM/Speed) with low-frequency sensor queries (temperatures).
- **Persistent Background Telemetry:** Background `LoggingService` records live sensor frames to the database even when the app is in the background or device is locked.
- **Interactive Session Visualizer:** Multi-metric Vico timeseries charts for analyzing recorded drives, peak RPM, and sensor telemetry.
- **DTC Diagnostic Scanner:** Scans Stored, Pending, and Permanent Fault Codes with safe-clearing confirmation dialogs.
- **Hardware-Level Encryption:** SQLCipher 256-bit AES-GCM database encryption with keys secured in Android Keystore via `EncryptedSharedPreferences`.
- **Multi-Car Management & Preference System:** Seamless car profile switching with dynamic Metric/Imperial unit conversions.

## Design

The UI utilizes a technical, high-performance aesthetic, built on top of a Deep Charcoal Gray background with Electric Blue and Neon Green accents. It incorporates Emil Kowalski motion tokens for spring physics, bounce clicks, and smooth staggered screen entrances, coupled with Haze-powered frosted glassmorphic cards.

## License

See [LICENSE](LICENSE) for full details.


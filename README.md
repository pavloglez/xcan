# XCan - Vehicle Diagnostics & Telemetry Dashboard

XCan is an offline-first, highly scalable, and reactive Android application designed for real-time vehicle maintenance and OBD2 diagnostic operations.

## Architecture

This project follows a clean multi-module architecture adhering to MVI (Model-View-Intent) principles. State flows unidirectionally, with `ViewModel`s exposing a single `StateFlow` and accepting discrete intents.

### Modules

- `:app` - Main application container, Hilt DI setup, and global navigation.
- `:core:model` - Pure data classes, entities, and domain objects shared across modules.
- `:core:database` - Room Database, defining entities and Data Access Objects (DAOs).
- `:core:data` - Data access layer, repositories orchestrating between database/network, and DataStore preferences.
- `:core:network` - API client and Retrofit configuration (mocked for offline-first approach).
- `:core:bluetooth` - Kable integration for BLE operations, OBD2 protocol parsing, and ECU interaction.
- `:core:ui` - Reusable Compose UI components (dials, gauges, theming).
- `:feature:dashboard` - Real-time telemetry dashboard.
- `:feature:diagnostics` - DTC (Diagnostic Trouble Code) scanning and clearing.
- `:feature:config` - Global app settings, unit preferences, and BLE device management.
- `:feature:logging` - Tracking telemetry session logs and data logging.
- `:feature:maintenance` - Service interval tracking.

*(Note: Please refer to the `README.md` located inside each specific module directory for detailed documentation about that module's specific responsibilities and architecture).*

## Technology Stack

- **Language:** Kotlin (2.0.20)
- **UI Toolkit:** Jetpack Compose Material 3
- **Dependency Injection:** Dagger Hilt (v2.60.1)
- **Local Persistence:** Room Database (v2.6.1) & Jetpack DataStore
- **Bluetooth LE:** Kable (v0.30.0)
- **Background Work:** WorkManager (v2.9.0)
- **Build System:** Gradle (AGP 9.2.1)

## Setup & Building

1. Ensure you have Android Studio installed with Kotlin support.
2. Clone this repository.
3. Open the project in Android Studio.
4. Sync Gradle.
5. Run `./gradlew assembleDebug` from the command line, or build via the IDE to test the compilation.

## Features

- **Dynamic Sensor Discovery:** Discovers supported OBD2 sensors dynamically based on bitmask flags (`0100`, `0120`, `0140`).
- **Dynamic Sensor Parsing:** Uses a formula evaluator approach, allowing custom sensors to be defined with their respective parsing equations without recompiling the core parser.
- **DTC Scanning:** Reads Stored, Pending, and Permanent Fault Codes.
- **Customizable Dashboard:** Add, remove, and reorganize high-performance graphical gauges.

## Design

The UI utilizes a technical, high-performance aesthetic, built on top of a Deep Charcoal Gray background with Electric Blue and Neon Green accents. Telemetry dials are rendered via custom Canvas drawing for extreme performance and accuracy.

## License

See [LICENSE](LICENSE) for full details.


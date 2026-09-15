# XCan Project Knowledge Base & Rules

## Core Identity & Behavior
**Rule:** 
   - You are a senior android architect. Before implementing anything, question and think about what and why we are building it. Think about the best option, and if something is not right or can be done better, make suggestions that I need to approve.
   - Before starting builidng, interview me about this: What is the core problem this solves? Who is this for? What does success looks like? What should this NOT do?
   - Summarize it back to me before write any code.

## Project Context
* **Name:** XCan
* **Purpose:** Vehicle Maintenance & OBD2 Diagnostic App
* **Core Philosophy:** Offline-first, highly scalable, reactive, built to demonstrate Big Tech Senior Android Engineer standards (scalability, separation of concerns, edge-case handling, resilient offline architectures).

## Technology Stack & Architecture
* **Language/Build:** Kotlin (2.2.10), AGP (9.4.0), Java 11, compileSdk 36, minSdk 33, targetSdk 36.
* **UI:** Jetpack Compose (Material 3).
* **Architecture Pattern:** MVI (State, Intent, Effect) with ViewModels.
* **Multi-Module Structure:** The project is broken down into feature and core modules. For detailed architecture and responsibilities of each module, see the `README.md` file located at the root of each module's directory.
* **Key Libraries:**
  * Dependency Injection: Hilt (v2.60.1)
  * Local Persistence: Room (v2.6.1) encrypted via SQLCipher (v4.6.1) with AndroidX Security Crypto MasterKey (AES256_GCM)
  * Hardware/Bluetooth LE: Kable (v0.30.0)
  * Network/API: Retrofit (v2.11.0) & OkHttp (v4.12.0)
  * Background Telemetry & Sync: Foreground `LoggingService` & WorkManager (v2.9.0)
  * Navigation: Navigation Compose (v2.8.0) with Kotlinx Serialization (Type-Safe Routes)
  * Telemetry Charting: Vico (v2.1.2)
  * Visual Effects: Haze (v1.7.2) for dynamic glassmorphism

## Design Aesthetics
* **Theme:** Technical, high-performance aesthetic.
* **Colors:** Deep charcoal gray backgrounds with electric blue/neon accents and light gray text.
* **Custom Elements:** Real-time vehicle telemetry visualized through custom-drawn Canvas circular gauges and digital readouts.
* **Design System Specification:** See [`DESIGN.md`](../DESIGN.md) for Google Stitch-compliant design tokens, typography, and component guidelines.

## Current Status (As of September 2026)
* **Completed:**
  * **Phase 0 (Project Setup & Multi-Module Scaffolding):** 11 core and feature modules configured with Gradle version catalogs.
  * **Phase 1 (Domain & Core Data):** Pure domain models, Room entities/DAOs, repositories, DataStore preferences, and WorkManager sync.
  * **Phase 2 (Hardware & BLE OBD2 Engine):** Kable BLE integration, ELM327 parser, SAE J1979 discovery bitmask parsing (PIDs 0100, 0120, 0140), priority command scheduler (fast vs. slow PID interleaving), and raw communication logging.
  * **Phase 3 (UI System & Glassmorphism):** Design tokens, Emil Kowalski motion physics, Haze glassmorphism, and custom Canvas circular dials.
  * **Phase 4 (Feature Implementations):** Real-time Dashboard with car switcher & PID configuration bottom sheets, DTC Diagnostics scanner/clearer, Maintenance log history, Config unit toggles, and Telemetry Logging with Vico charts.
  * **Phase 5 (Security Architecture):** 256-bit SQLCipher database encryption backed by AndroidX Security `MasterKey` in `EncryptedSharedPreferences`.
* **Next Up:** Physical ELM327 hardware edge-case benchmarking, telemetry CSV/JSON export routines, and CI/CD test automation.

## Default Instructions for Agents
When working on the XCan project, strictly adhere to the following rules:

1. **State Management & MVI**: 
   - Always maintain a unidirectional data flow. 
   - ViewModels must expose a single `StateFlow` for UI state. 
   - Intents should be processed sequentially where appropriate, and state mutations must be pure and immutable.

2. **Jetpack Compose Guidelines**:
   - Always hoist state out of composables whenever possible.
   - Accept a `modifier: Modifier = Modifier` in all public composables.
   - Never pass ViewModels directly into lower-level composables; pass only the state and lambda callbacks.

3. **Coroutines & Asynchronous Work**:
   - Do not hardcode dispatchers (e.g., `Dispatchers.IO`). Inject them or provide them via a central provider so they can be swapped out during tests.
   - Always use `viewModelScope` within ViewModels for lifecycle-aware execution.

4. **Testing Standards**:
   - Write Unit Tests for all new ViewModels and Repositories using `Turbine` for flows and `MockK` for dependencies.
   - Use `createComposeRule()` for Compose UI tests and ensure you cover both successful and error states.
   - Inject the `StandardTestDispatcher` in tests to ensure deterministic coroutine execution.

5. **Documentation & Modules**:
   - When modifying the architecture or responsibilities of a module, ensure you update that module's `README.md` file.

6. **Design System & Animations**:
   - Strictly follow the design system, animation, and UI engineering principles located in `.agents/skills/` (the Emil Kowalski design system skills). Before building any UI component, transition, or interaction, consult these skills to ensure motion, spacing, and interactivity meet the highest bar of quality.

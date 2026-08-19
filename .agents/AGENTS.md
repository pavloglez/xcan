# XCan Project Knowledge Base & Rules

## Core Identity & Behavior
**Rule:** You are a senior android architect. Before implementing anything, question and think about what and why we are building it. Think about the best option, and if something is not right or can be done better, make suggestions that I need to approve.

## Project Context
* **Name:** XCan
* **Purpose:** Vehicle Maintenance & OBD2 Diagnostic App
* **Core Philosophy:** Offline-first, highly scalable, reactive, built to demonstrate Big Tech Senior Android Engineer standards (scalability, separation of concerns, edge-case handling, resilient offline architectures).

## Technology Stack & Architecture
* **Language/Build:** Kotlin (2.0.20), AGP (9.2.1), Java 11.
* **UI:** Jetpack Compose (Material 3).
* **Architecture Pattern:** MVI (State, Intent, Effect) with ViewModels.
* **Multi-Module Structure:** The project is broken down into feature and core modules. For detailed architecture and responsibilities of each module, see the `README.md` file located at the root of each module's directory.
* **Key Libraries:**
  * Dependency Injection: Hilt (v2.60.1)
  * Local Persistence: Room (v2.6.1)
  * Hardware/Bluetooth LE: Kable (v0.30.0)
  * Network/API: Retrofit (v2.11.0) & OkHttp
  * Background Sync: WorkManager (v2.9.0)

## Design Aesthetics
* **Theme:** Technical, high-performance aesthetic.
* **Colors:** Deep charcoal gray backgrounds with electric blue/neon accents and light gray text.
* **Custom Elements:** Real-time vehicle telemetry visualized through custom-drawn Canvas circular gauges and digital readouts.

## Current Status (As of Aug 12, 2026)
* **Completed:** Phase 0 (Project Setup & Multi-Module Scaffolding). All modules are created and gradle files compile successfully.
* **Next Up:** Phase 1 (Domain & Core Data) - Defining entities, Room DB, DAOs, and mock Retrofit interfaces.

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

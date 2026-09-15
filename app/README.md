# App Module (`:app`)

The `:app` module serves as the primary entry point and orchestrator for the XCan application. It wires together all `:core` and `:feature` modules into a cohesive runtime.

## Architecture Role
- **Application Lifecycle**: Hosts `XCanApplication` annotated with `@HiltAndroidApp` to initialize Dagger-Hilt dependency injection and `HiltWorkerFactory` for background WorkManager tasks.
- **Type-Safe Navigation**: Implements Jetpack Navigation Compose (v2.8.0) using Kotlinx Serialization `@Serializable` routes:
  - `DashboardRoute`: Real-time telemetry dials and quick vehicle selector.
  - `DiagnosticsRoute`: OBD-II DTC diagnostic fault code scanning and clearing.
  - `MaintenanceRoute`: Vehicle service history and maintenance logs timeline.
  - `LogSessionsRoute`: Telemetry session logs list and management.
  - `ConfigRoute`: Global application settings and Metric/Imperial toggles.
  - `LogSessionDetailRoute(sessionId: String)`: Deep-dive view of a single telemetry recording session featuring interactive Vico charts.
- **Scaffolding & Navigation Bar**: Houses the primary `Scaffold` and a custom floating glassmorphic `NavigationBar` with animated icon transitions (`fadeIn`, `scaleIn`, `fadeOut`).
- **CompositionLocal Providers**: Injects shared UI context down the composable tree:
  - `LocalHazeState`: Powers shared frosted glassmorphism via the Haze library.
  - `LocalActiveCarName`: Supplies the currently selected vehicle profile name observed reactively from `CarRepository`.

## Dependencies
- **Core Modules:**
  - `:core:model`
  - `:core:data`
  - `:core:bluetooth`
  - `:core:ui`
- **Feature Modules:**
  - `:feature:dashboard`
  - `:feature:diagnostics`
  - `:feature:maintenance`
  - `:feature:logging`
  - `:feature:config`


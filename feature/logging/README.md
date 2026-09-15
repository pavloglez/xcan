# Telemetry Logging Feature Module (`:feature:logging`)

The `:feature:logging` module provides the user interface and presentation logic for browsing recorded telemetry sessions, inspecting high-frequency drive logs, and visualizing multi-sensor timeseries data through interactive charts.

## Architecture Role

### MVI Architecture
- **`LogSessionsViewModel`**:
  - **State (`LogSessionsState`)**: Manages the reactive list of `LogSession` objects, loading indicators, and delete confirmation dialog visibility.
  - **Intents (`LogSessionsIntent`)**: Handles `DeleteSession`, `DeleteAllSessions`, `ConfirmDeleteAll`, and `DismissDeleteAll`.
- **`LogSessionDetailViewModel`**:
  - **State (`LogSessionDetailState`)**: Loads session metadata and associated time-series `LogEntry` samples for a given `sessionId`.
  - **Chart Model Producer**: Dynamically builds Vico `CartesianChartModelProducer` datasets, plotting synchronized multi-line series (Engine RPM, Vehicle Speed, Coolant Temp, Engine Load) with dedicated color channels.

### UI Components
- **`LogSessionsScreen`**:
  - Displays a vertical scrollable list of recorded driving sessions with timestamps, durations, total entry counts, and associated car profile tags.
  - Provides swipe/touch actions and confirmation dialogs for individual or bulk session deletion.
- **`LogSessionDetailScreen`**:
  - **Interactive Telemetry Chart**: Renders a Vico `CartesianChartHost` displaying real-time vehicle dynamics with customizable horizontal and vertical axes.
  - **Indexed Sample Stream**: A chronological `LazyColumn` detailing timestamped sensor readings.
  - **Motion & Styling**: Uses `Modifier.staggerEnter` for smooth item cascade animations and `GlassTopAppBar` with frosted blur over charts.

## Dependencies
- `:core:data`
- `:core:model`
- `:core:ui`
- Vico Compose (`com.patrykandpatrick.vico:compose` and `compose-m3`)

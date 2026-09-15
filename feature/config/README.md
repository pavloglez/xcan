# Configuration Feature Module (`:feature:config`)

The `:feature:config` module provides the user interface and logic for managing application settings, measurement units, and user preferences.

## Architecture Role

### MVI Architecture
- **`ConfigViewModel`**:
  - **State (`ConfigState`)**: Exposes reactive preference flags such as `useMetric: Boolean` backed by Jetpack DataStore.
  - **Intents (`ConfigIntent`)**:
    - `ToggleMetric(Boolean)`: Dispatches preference updates to `UserPreferencesRepository`.

### Dynamic Reactive Propagation
- Toggling unit preferences immediately propagates downstream via Kotlin `Flow`s across all feature modules:
  - **`:feature:dashboard`**: Converts telemetry dial ranges and digital readouts between km/h & mph, °C & °F, and kPa & psi in real-time.
  - **`:feature:logging`**: Adapts chart axes and telemetry entry table units.
  - **`:feature:maintenance`**: Converts historical odometer readings and intervals between kilometers and miles.

### UI Styling (`ConfigScreen`)
- Displays preference toggles using customized Material 3 `Switch` components accented with `ElectricBlue`.
- Integrates frosted glassmorphism via `GlassTopAppBar` and `LocalHazeState`.

## Dependencies
- `:core:data`
- `:core:ui`
- `:core:model`


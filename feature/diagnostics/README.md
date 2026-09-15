# Diagnostics Feature Module (`:feature:diagnostics`)

The `:feature:diagnostics` module provides the user interface and business logic for reading, inspecting, and clearing OBD-II Diagnostic Trouble Codes (DTCs).

## Architecture Role

### MVI Architecture
- **`DiagnosticsViewModel`**:
  - **State (`DiagnosticsState`)**: Combines reactive streams from `BleDataSource` to track `connectionStatus`, `scanStatus` (`IDLE`, `SCANNING`, `CLEARING`, `SUCCESS`, `ERROR`), and `faultCodes`. Exposes `isConnected`.
  - **Intents (`DiagnosticsIntent`)**:
    - `ScanFaultCodes`: Triggers Service 03 request via BLE.
    - `ClearFaultCodes`: Dispatches Service 04 clear command to the ECU.
    - `ClearResults`: Resets current scan findings in local state.

### OBD-II Protocol Integration
- **Service 03 (DTC Request)**: Polls the ECU for stored trouble codes, translating raw hex responses into structured `DiagnosticTroubleCode` entities with standard category prefixes (`P`, `C`, `B`, `U`).
- **Service 04 (Clear / Reset)**: Issues the command to wipe ECU fault memory and turn off the Malfunction Indicator Lamp (MIL / Check Engine Light).

### Safety Measures & Presentation
- **Accidental Wipe Prevention**: Requires an explicit confirmation dialog before executing Service 04 commands, warning users about clearing freeze frame data and resetting I/M emissions readiness monitors.
- **UI Presentation (`DiagnosticsScreen`)**:
  - Fault codes rendered as high-contrast cards with system badges and severity coloring.
  - Progress feedback during active scan/clear operations.
  - Frosted glassmorphism top app bar integrated with `LocalHazeState`.

## Dependencies
- `:core:bluetooth`
- `:core:ui`
- `:core:model`


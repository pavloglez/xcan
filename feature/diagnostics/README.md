# Diagnostics Feature Module (`:feature:diagnostics`)

The `:feature:diagnostics` module allows users to read and clear OBD-II Diagnostic Trouble Codes (DTCs).

## Architecture Role
- **MVI Architecture**: Utilizes `DiagnosticsViewModel` to trigger scan routines and manage lists of fault codes.
- **Bluetooth Commands**: Sends explicit Service 03 (Request Emission-Related DTCs) and Service 04 (Clear/Reset Emission-Related Diagnostic Information) instructions via the `BleDataSource` in `:core:bluetooth`.
- **Safety Measures**: Enforces confirmation dialogs before executing destructive commands like clearing fault codes to prevent accidental data erasure.

## Dependencies
- `:core:bluetooth`
- `:core:ui`
- `:core:model`

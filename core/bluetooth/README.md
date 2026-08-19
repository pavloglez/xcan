# Bluetooth Core Module (`:core:bluetooth`)

The `:core:bluetooth` module manages hardware interactions, specifically Bluetooth Low Energy (BLE) connectivity with OBD-II dongles.

## Architecture Role
- **Kable Integration**: Uses Kable for managing BLE scanning, connecting to devices, and holding GATT connections.
- **Data Source**: Exposes `BleDataSource` which provides reactive `StateFlow` streams of connection status, discovered devices, and raw OBD telemetry.
- **ELM327 Parsing**: Translates raw hexadecimal byte streams from the OBD-II device into structured `TelemetryFrame` domain models.

## Dependencies
- `:core:model`

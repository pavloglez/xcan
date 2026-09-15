# Bluetooth Core Module (`:core:bluetooth`)

The `:core:bluetooth` module manages all low-level hardware interactions, Bluetooth Low Energy (BLE) peripheral lifecycle, ELM327 protocol negotiation, and OBD-II SAE J1979 command dispatching.

## Architecture Role

### Reactive Data Source (`BleDataSource`)
- **`connectionState`**: Emits `ConnectionStatus` (`DISCONNECTED`, `CONNECTING`, `CONNECTED`, `ERROR`).
- **`telemetry`**: Emits high-frequency `TelemetryFrame` domain events containing vehicle speed, engine RPM, coolant temperature, engine load, MAF, and throttle position.
- **`sensorScanStatus`**: Emits dynamic PID discovery state (`Idle`, `Scanning(progress)`, `Completed(supportedPids)`, `Error`).
- **`connectionLogs`**: Streams human-readable communication events (raw AT commands, hex payloads, response latency, and parsed values) for in-app diagnostic terminal inspection.
- **Hardware Operations**: Exposes suspend functions for scanning peripherals, establishing GATT connections, reading/clearing Diagnostic Trouble Codes (DTCs), and configuring active polling lists.

### SAE J1979 Dynamic Discovery (`PidBitmapParser`)
- Decodes standard Service 01 bitmask discovery responses (`0100`, `0120`, `0140`, `0160`, `0180`, `01A0`).
- Parses 32-bit (8-hex-character) bitmap representations to dynamically identify which PIDs are implemented by the target vehicle's ECU.
- Evaluates the 32nd bit of each block to determine whether subsequent discovery blocks should be recursively queried.

### Priority Polling Scheduler (`ObdCommandScheduler`)
- Employs an interleaving scheduling algorithm to prevent low-baud ELM327 bus saturation:
  - **Fast PIDs**: High-frequency engine variables (RPM, Speed, Load, Throttle).
  - **Slow PIDs**: Slow-moving thermal metrics (Coolant Temp, Intake Temp).
- Interleaves slow sensor polls at a fixed ratio (1 slow query per 10 fast queries), preserving 60 FPS gauge smoothness while keeping temperatures updated.

### Protocol Decoders (`ObdParser` & `DtcParser`)
- **`ObdParser`**: Strips ELM327 protocol headers/prompts, validates byte lengths, and applies physical conversion equations.
- **`DtcParser`**: Decodes Service 03 fault bytes into standard OBD-II trouble codes with system prefixes:
  - `0x0` -> `P` (Powertrain)
  - `0x1` -> `C` (Chassis)
  - `0x2` -> `B` (Body)
  - `0x3` -> `U` (Network)

## Dependencies
- `:core:model`


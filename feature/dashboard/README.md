# Dashboard Feature Module (`:feature:dashboard`)

The `:feature:dashboard` module is the primary cockpit interface of XCan, presenting real-time vehicle telemetry, dynamic gauge visualizations, vehicle switching, and live logging controls.

## Architecture Role

### Multi-ViewModel Architecture
- **`DashboardViewModel`**:
  - Exposes `DashboardUIState` tracking active telemetry values (Speed, RPM, Coolant Temp, Boost, Engine Load, Throttle).
  - Handles sensor PID toggle intents and synchronizes active polling lists with `:core:bluetooth`.
- **`ConnectionViewModel`**:
  - Manages BLE peripheral scanning, pairing, and connection status transitions.
  - Exposes human-readable communication logs for real-time diagnostic terminal streaming.
- **`LoggingViewModel`**:
  - Interfaces with the foreground `LoggingService` via `LoggingRepository`.
  - Dispatches Start, Pause, Resume, and Stop commands for drive recording sessions.
- **`CarProfileViewModel`**:
  - Coordinates vehicle profile creation, selection, and active car persistence via `CarRepository`.

### UI Components & Visualizations
- **Custom Canvas `TelemetryDial`**:
  - High-performance circular dials drawn directly onto Compose `Canvas` using sweep gradients, calibrated tick marks, numerical digital readouts, and animated needle sweeps.
  - Zero-allocation drawing pipeline designed to sustain smooth 60+ FPS rendering under rapid telemetry streams.
- **`DashboardConfigBottomSheet`**:
  - Modal sheet enabling users to select individual OBD-II sensors and trigger dynamic SAE J1979 ECU PID discovery rescans.
- **`CarPickerBottomSheet`**:
  - Vehicle switcher allowing instantaneous transitions between registered car profiles.
- **`LogFloatingControl`**:
  - Floating pill overlay displaying drive recording elapsed time with tactile pause, resume, and stop buttons.
- **`ConnectionLogsDialog`**:
  - Real-time diagnostic console displaying raw ELM327 hexadecimal transactions and latency metrics.

## Dependencies
- `:core:bluetooth`
- `:core:data`
- `:core:ui`
- `:core:model`


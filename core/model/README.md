# Model Core Module (`:core:model`)

The `:core:model` module represents the shared domain language of the XCan application. It contains pure Kotlin models, enumerations, mathematical constants, and domain contracts with zero framework dependencies.

## Architecture Role

### Domain Models
- **`CarProfile`**: Vehicle specifications (`id`, `name`, `make`, `model`, `year`, `vin`, `isActive`).
- **`TelemetryFrame`**: Real-time snapshot of engine telemetry (`timestamp`, `speedKmh`, `engineRpm`, `coolantTempC`, `engineLoadPercent`, `intakePressureKpa`, `throttlePercent`, `fuelLevelPercent`).
- **`DiagnosticTroubleCode`**: OBD-II fault code details (`code`, `description`, `system`, `isPermanent`).
- **`MaintenanceLog`**: Service records (`id`, `carId`, `serviceType`, `date`, `mileage`, `cost`, `notes`, `associatedDtc`).
- **`LogSession`**: Telemetry recording session metadata (`id`, `carId`, `carLabel`, `startTime`, `endTime`, `entryCount`).
- **`LogEntry`**: Individual time-series metric entry recorded during a drive (`id`, `sessionId`, `timestamp`, `sensorId`, `value`, `unit`).
- **`ObdSensor`**: Configuration model for dynamic ECU sensors (`id`, `pid`, `displayName`, `unit`, `expectedBytes`, `formula`).
- **`SensorScanStatus`**: Sealed interface representing ECU discovery states (`Idle`, `Scanning`, `Completed`, `Error`).

### SAE J1979 Standard Specifications (`StandardPid`)
- Defines an enumeration of SAE J1979 Service 01 PIDs including:
  - `ENGINE_LOAD` (0104), `COOLANT_TEMP` (0105), `INTAKE_PRESSURE` (010B), `ENGINE_RPM` (010C)
  - `VEHICLE_SPEED` (010D), `TIMING_ADVANCE` (010E), `INTAKE_TEMP` (010F), `MAF_AIR_FLOW` (0110)
  - `THROTTLE_POSITION` (0111), `RUN_TIME` (011F), `FUEL_LEVEL` (012F), `CONTROL_MODULE_VOLTAGE` (0142), etc.
- Each entry encapsulates its expected response byte count and an inline pure Kotlin lambda `(ByteArray) -> Float` for instantaneous byte decoding.

### Domain Interfaces & Constants
- **`DispatcherProvider`**: Interface abstracting Kotlin Coroutine dispatchers (`main`, `io`, `default`, `unconfined`) to allow seamless swapping with test dispatchers.
- **`SensorRepository`**: Interface defining sensor discovery queries.
- **`ObdConstants`**: Centralized mathematical conversion factors, PID masks, and timeouts.

## Dependencies
- Pure Kotlin standard library (zero Android dependencies)


# Model Core Module (`:core:model`)

The `:core:model` module is a lightweight module holding pure Kotlin data classes. It represents the shared domain language of the application.

## Architecture Role
- **Domain Models**: Contains classes like `CarProfile`, `MaintenanceLog`, `TelemetryFrame`, and `ScannedDevice`.
- **Zero Dependencies**: By containing no Android or framework-specific dependencies (other than standard Kotlin libraries), it ensures clean separation and avoids circular dependencies between other modules.

## Dependencies
- None

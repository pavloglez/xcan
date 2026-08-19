# Dashboard Feature Module (`:feature:dashboard`)

The `:feature:dashboard` module presents real-time vehicle telemetry to the user.

## Architecture Role
- **MVI Architecture**: Utilizes `DashboardViewModel` to manage `DashboardState`, `DashboardIntent`, and `DashboardEffect`.
- **Live Telemetry**: Subscribes to the `BleDataSource` from `:core:bluetooth` to update canvas dials (RPM, Speed, Engine Load) in real-time.
- **Vehicle Selection**: Provides UI for the user to switch active car profiles, updating the global state in `:core:data`.

## Dependencies
- `:core:bluetooth`
- `:core:data`
- `:core:ui`
- `:core:model`

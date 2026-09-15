# Data Core Module (`:core:data`)

The `:core:data` module serves as the single source of truth for all business and vehicle telemetry data. It abstracts and orchestrates data flow between encrypted local persistence (`:core:database`), remote network services (`:core:network`), hardware streams (`:core:bluetooth`), and preference storage.

## Architecture Role

### Repositories
- **`CarRepository`**: Manages vehicle profiles (`CarProfile`), active vehicle switching, and default car fallback initialization.
- **`MaintenanceRepository`**: Handles vehicle service histories (`MaintenanceLog`), offline caching, and remote cloud synchronization.
- **`TelemetryRepository`**: Ingests live telemetry frames from BLE, caches current metrics, and persists historical telemetry.
- **`LoggingRepository`**: Manages telemetry recording sessions (`LogSession`) and high-throughput time-series sensor points (`LogEntry`).
- **`UserPreferencesRepository`**: Persists user settings (e.g., Metric vs. Imperial unit preferences) via AndroidX DataStore Preferences.
- **`SensorRepositoryImpl`**: Implements the `:core:model` `SensorRepository` contract, exposing standard SAE J1979 PIDs, formulas, and sensor metadata.

### Background Telemetry Service (`LoggingService`)
- Runs as an Android **Foreground Service** with a persistent notification to ensure telemetry continues logging seamlessly when the app is in the background or the screen is locked.
- Exposes a reactive `StateFlow<LoggingState>` (`Idle`, `Running`, `Paused`) to all UI consumers.
- Subscribes to `BleDataSource.telemetry`, buffers high-frequency sensor readings, and flushes them to Room in batches every 500ms to eliminate disk I/O bottlenecks.

### Offline-First Background Sync (`SyncWorker` & `SyncHelper`)
- Employs AndroidX `WorkManager` with network constraint policies (`NetworkType.CONNECTED`) to automatically sync offline maintenance logs to `XCanApiService`.
- Supports one-time manual sync triggers and periodic background sync schedules.

### Dependency Injection & Dispatchers
- **`DispatchersModule`**: Injects a centralized `DispatcherProvider` interface (Default, IO, Main, Unconfined) across all repositories and services to ensure deterministic testability.
- **`RepositoryModule` & `DataModule`**: Binds clean architecture interfaces to their concrete implementations.

## Dependencies
- `:core:database`
- `:core:network`
- `:core:bluetooth`
- `:core:model`


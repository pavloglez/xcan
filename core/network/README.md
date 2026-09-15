# Network Core Module (`:core:network`)

The `:core:network` module provides HTTP client abstractions, remote REST API service contracts, and Data Transfer Objects (DTOs) for cloud synchronization.

## Architecture Role

### API Service Contracts (`XCanApiService`)
- **`GET v1/maintenance`**: Retrieves remote vehicle maintenance records from the cloud.
- **`POST v1/maintenance/sync`**: Uploads batches of local maintenance records (`List<MaintenanceLogDto>`) to synchronize across devices.

### Data Transfer Objects (DTOs)
- **`MaintenanceLogDto`**: JSON serialization model mapping between remote payloads and local domain models (`MaintenanceLog`).

### Dependency Injection (`NetworkModule`)
- Exposes singleton instances of Retrofit, OkHttpClient, and `XCanApiService` configured with `GsonConverterFactory` and base URL (`https://mock.xcantelemetry.com/api/v1/`).

### Offline-First Philosophy
Network calls are decoupled from the UI and invoked exclusively via background synchronization workers (`SyncWorker` in `:core:data`). If the device is offline or without cellular connectivity, local Room operations proceed without interruption.

## Dependencies
- `:core:model`
- Retrofit (v2.11.0) & OkHttp (v4.12.0)
- Gson


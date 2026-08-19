# Network Core Module (`:core:network`)

The `:core:network` module handles all external API communications, ensuring that data can be synced to and from remote servers.

## Architecture Role
- **Retrofit/OkHttp**: Provides the networking stack, API service interfaces, and mock interceptors for offline testing.
- **Offline-First Sync**: Used by components like `WorkManager` (defined in other modules) to push/pull telemetry or logs when network connectivity is available.
- **DI**: Provides singleton instances of `Retrofit`, `OkHttpClient`, and specific API interfaces via Hilt modules.

## Dependencies
- `:core:model`

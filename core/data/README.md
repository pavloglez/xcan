# Data Core Module (`:core:data`)

The `:core:data` module serves as the single source of truth for the app's business data, orchestrating data flow between local persistence (`:core:database`), network (`:core:network`), and user preferences.

## Architecture Role
- **Repository Pattern**: Implements repositories (e.g., `CarRepository`, `MaintenanceRepository`, `UserPreferencesRepository`) that abstract the origins of the data.
- **DataStore Integration**: Manages user configuration preferences via AndroidX DataStore (e.g., Metric/Imperial units).
- **Mappers**: Maps Database Entities and Network DTOs into pure Domain Models (`:core:model`) for the upper layers.

## Dependencies
- `:core:database`
- `:core:network`
- `:core:model`

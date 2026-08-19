# Database Core Module (`:core:database`)

The `:core:database` module is responsible for the app's local persistence using Room.

## Architecture Role
- **Room Database**: Defines the `XCanDatabase` class, entities, and DAOs.
- **Entities**: Maps domain models to relational tables (e.g., `CarProfileEntity`, `MaintenanceLogEntity`).
- **Data Access Objects (DAOs)**: Exposes `Flow` and suspend functions for CRUD operations.
- **DI**: Provides the database instance and DAOs to the `:core:data` repository layer.

## Dependencies
- `:core:model`

# App Module (`:app`)

The `:app` module serves as the primary entry point for the XCan application. It wires together all core and feature modules.

## Architecture Role
- **Application Class**: Hosts the `XCanApplication` class annotated with `@HiltAndroidApp` for dependency injection.
- **Navigation**: Defines the top-level Compose Navigation graph (`NavHost`), integrating routes like Dashboard, Maintenance, Config, and Diagnostics.
- **Bottom Navigation**: Hosts the main scaffolding and bottom navigation bar components.

## Dependencies
- All `:core` modules.
- All `:feature` modules.

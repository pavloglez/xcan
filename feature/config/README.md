# Configuration Feature Module (`:feature:config`)

The `:feature:config` module provides the settings and preferences interface for the application.

## Architecture Role
- **MVI Architecture**: Managed by `ConfigViewModel`, exposing states such as unit preferences (Metric/Imperial).
- **Settings UI**: Exposes a Compose screen for users to toggle settings. It directly manipulates the `UserPreferencesRepository` in `:core:data`.
- **Dynamic Application State**: Changes made here immediately propagate to features like `:feature:dashboard` and `:feature:maintenance` to dynamically adjust UI labels and unit conversions.

## Dependencies
- `:core:data`
- `:core:ui`
- `:core:model`

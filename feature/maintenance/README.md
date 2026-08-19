# Maintenance Feature Module (`:feature:maintenance`)

The `:feature:maintenance` module provides the user interface and logic for managing vehicle maintenance histories.

## Architecture Role
- **MVI Architecture**: Utilizes `MaintenanceViewModel` to handle user intents (like adding a log) and manage the UI state.
- **Timeline UI**: Displays a vertical scrolling timeline of past services, differentiating standard maintenance (oil changes, tire rotations) from resolved Diagnostic Trouble Codes (DTCs).
- **Data Entry**: Provides an `AddMaintenanceLogDialog` for users to manually input services and associate them with specific mileages or prior DTCs.

## Dependencies
- `:core:data`
- `:core:ui`
- `:core:model`

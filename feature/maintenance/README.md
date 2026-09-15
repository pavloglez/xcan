# Maintenance Feature Module (`:feature:maintenance`)

The `:feature:maintenance` module provides the user interface and logic for managing vehicle maintenance histories, odometer tracking, and service logging.

## Architecture Role

### MVI Architecture
- **`MaintenanceViewModel`**:
  - **State (`MaintenanceState`)**: Reactively combines streams from `MaintenanceRepository`, `CarRepository`, and `UserPreferencesRepository` to supply `logs`, `activeCar`, `useMetric`, and `isLoading`.
  - **Intents (`MaintenanceIntent`)**:
    - `AddLog(serviceType, notes, mileage, relatedDtc)`: Validates and creates a `MaintenanceLog` tied to the active vehicle profile.

### Timeline UI & Visual Identity (`MaintenanceScreen`)
- **Service Timeline**: Displays a chronologically sorted vertical timeline separating routine maintenance (oil changes, tire rotations) from corrective repairs linked to resolved Diagnostic Trouble Codes (DTCs).
- **`AddMaintenanceLogDialog`**: Modal dialog for manual service data entry, associating mileage readings and fault codes.
- **Adaptive Units**: Displays mileage in kilometers or miles based on real-time preference state from `:feature:config`.
- **Motion**: Applies `pressBounce` physics to the Floating Action Button and `staggerEnter` animations to timeline cards.

### Data Flow & Persistence
- Logs are stored locally in the SQLCipher-encrypted Room database via `MaintenanceRepository` (`:core:data`) and synchronized in the background with `XCanApiService` via `SyncWorker`.

## Dependencies
- `:core:data`
- `:core:ui`
- `:core:model`


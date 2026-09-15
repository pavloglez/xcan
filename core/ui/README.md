# UI Core Module (`:core:ui`)

The `:core:ui` module encapsulates the visual language, design tokens, custom Compose components, motion physics, and glassmorphic styling of the XCan application.

## Architecture Role

### Design System & Theme
- **Color Palette**: Dark cyberpunk / high-performance aesthetic anchored by `DeepCharcoal` (`0xFF121418`) and `CharcoalSurface` (`0xFF1A1D24`), highlighted with `ElectricBlue` (`0xFF00C8FF`) and `NeonAccent` (`0xFF39FF14`).
- **`XCanTheme`**: Material 3 theme wrapper configuring typography, shapes, and tonal palettes.
- **Typography & Tokens**: Technical mono and sans-serif typography tailored for high-contrast in-cabin readability.

### Emil Kowalski Motion Engineering
- **Motion Curves (`XCanEasing`)**:
  - `EaseOut` (`CubicBezierEasing(0.23f, 1f, 0.32f, 1f)`): Highly responsive start for snappy UI feedback.
  - `EaseInOut` (`CubicBezierEasing(0.77f, 0f, 0.175f, 1f)`): Smooth continuous transitions.
  - `EaseDrawer` (`CubicBezierEasing(0.32f, 0.72f, 0f, 1f)`): Physical spring-like bottom sheet gestures.
- **Duration Tokens (`XCanDuration`)**: Standardized 160ms press feedback and 300ms layout transitions.
- **Interactive Modifiers**:
  - `Modifier.bounceClick()` & `Modifier.pressBounce()`: Spring physics providing tactile scale-down feedback when buttons and cards are pressed.
  - `Modifier.staggerEnter(index)`: Staggered cascaded entrance animations for list and grid items.

### Dynamic Glassmorphism (Haze)
- **`LocalHazeState`**: CompositionLocal propagating a shared `HazeState` from root scaffolding down to feature components.
- **`Modifier.glassmorphism()`**: Applies true frosted glass blur (`HazeStyle` with tint and border) over underlying scrollable content.

### Shared UI Components
- **`GlassTopAppBar`**: Frosted glass header bar featuring the active vehicle indicator and action buttons.
- **`XCanBackground`**: Technical grid backdrop with subtle ambient light gradients.
- **`XCanComponents`**: Reusable technical buttons, status badges, metric indicators, and alert dialogs.

## Dependencies
- Jetpack Compose Material 3
- Haze (`dev.chrisbanes.haze:haze`)


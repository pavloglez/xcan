# UI & Theme Refactoring Plan for XCan

This plan details the steps required to consolidate the project's theme, extract reusable UI components, eliminate hardcoded colors, and ensure a unified "high-performance, technical" aesthetic across all screens (matching the Dashboard).

## 1. Theme & Color Consolidation
Currently, colors are redefined in multiple feature modules (e.g., `LogSessionDetailScreen`, `LogSessionsScreen`, `DashboardConfigBottomSheet`) and the `app` module contains default Android Studio theme files.

**Action Plan:**
- **Centralize Colors:** Move all definitive color definitions (`DeepCharcoal`, `CharcoalSurface`, `ElectricBlue`, `NeonAccent`, `ErrorRed`, etc.) to `core/ui/.../theme/Color.kt`.
- **Refine `XCanTheme`:** Map these colors properly to the Material 3 `darkColorScheme` in `core/ui/.../theme/Theme.kt`.
- **Remove Duplicates:** Delete the redundant `app/src/.../ui/theme` package and remove local color properties from feature screens.

## 2. Define Common Reusable UI Components
To ensure consistency and avoid boilerplate, we will create standard Compose components in `core/ui/.../components`:

- **`XCanBackground`**: A base container for all screens that provides the consistent `DeepCharcoal` background (or gradient if applicable).
- **`XCanCard` & `XCanGlassCard`**: Reusable surfaces for items, dialogs, and panels. These will replace hardcoded `Modifier.background(Color.White.copy(alpha = 0.05f))` implementations.
- **`XCanText`**: Standardized text components (Titles, Body, Label) that default to `MaterialTheme.colorScheme.onBackground` (Light Gray) and `onSurface` (White).
- **`XCanButton`**: A reusable button component styled with the `ElectricBlue` primary color.
- **`XCanDivider`**: A standardized divider replacing `HorizontalDivider(color = Color.White.copy(alpha = 0.08f))`.

## 3. Replace Hardcoded Colors Across Features
A project-wide search revealed extensive use of hardcoded colors (e.g., `Color.White`, `Color.Gray`, `Color(0xFF00C8FF)`) in almost every UI file. 

**Action Plan:**
- Update all `.kt` files in `feature/dashboard`, `feature/logging`, `feature/diagnostics`, `feature/maintenance`, and `app/MainActivity.kt`.
- Replace instances of `Color.X` with `MaterialTheme.colorScheme.[color]`.
- Replace `Color.White.copy(alpha = ...)` with `MaterialTheme.colorScheme.onSurface.copy(...)` or use the new `XCanGlassCard` component.

## 4. Unify Look and Feel
The Dashboard sets the standard: dark backgrounds, glassmorphism overlays, and bright neon/blue accents.

**Action Plan:**
- Ensure all screens (Maintenance, Logging, Diagnostics) use the new `XCanBackground`.
- Ensure all top app bars use the existing `GlassTopAppBar` from `core/ui`.
- Review the bottom navigation bar in `MainActivity.kt` to ensure it uses theme colors instead of hardcoded alphas.
- Migrate any standard Material `AlertDialog` or `BottomSheet` to use the `CharcoalSurface` background.

## Next Steps
Once you approve this plan, I will execute these changes systematically, starting with the core `ui` module, followed by the feature modules, and finally `MainActivity`.

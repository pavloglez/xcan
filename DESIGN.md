---
version: alpha
name: XCan Technical Telemetry
description: High-performance, dark automotive cockpit design system for real-time OBD-II diagnostics, telemetry gauges, and vehicle data visualization.
colors:
  background: "#121212"
  surface: "#1E1E1E"
  surface-container: "#1A1D24"
  primary: "#00E5FF"
  on-primary: "#121212"
  secondary: "#00FF88"
  on-secondary: "#121212"
  on-background: "#B0B0B0"
  on-surface: "#FFFFFF"
  error: "#FF4C4C"
  outline: "#2A2E39"
  outline-variant: "rgba(255, 255, 255, 0.15)"
  glass-tint: "rgba(255, 255, 255, 0.05)"
typography:
  display-mono:
    fontFamily: Monospace
    fontSize: 32px
    fontWeight: 700
    lineHeight: 40px
    letterSpacing: 0px
  headline-md:
    fontFamily: Sans-Serif
    fontSize: 24px
    fontWeight: 600
    lineHeight: 32px
    letterSpacing: 0px
  body-lg:
    fontFamily: Sans-Serif
    fontSize: 16px
    fontWeight: 400
    lineHeight: 24px
    letterSpacing: 0.5px
  body-md:
    fontFamily: Sans-Serif
    fontSize: 14px
    fontWeight: 400
    lineHeight: 20px
    letterSpacing: 0.25px
  label-sm:
    fontFamily: Sans-Serif
    fontSize: 12px
    fontWeight: 500
    lineHeight: 16px
    letterSpacing: 0.1px
  label-mono:
    fontFamily: Monospace
    fontSize: 10px
    fontWeight: 600
    lineHeight: 14px
    letterSpacing: 0.5px
rounded:
  sm: 8px
  md: 16px
  lg: 24px
  pill: 32px
  full: 9999px
spacing:
  unit: 8px
  xs: 4px
  sm: 8px
  md: 16px
  lg: 24px
  xl: 32px
components:
  glass-card:
    backgroundColor: "{colors.surface-container}"
    textColor: "{colors.on-surface}"
    rounded: "{rounded.lg}"
    padding: 16px
  glass-top-app-bar:
    backgroundColor: "{colors.surface}"
    textColor: "{colors.on-surface}"
    height: 64px
    padding: 16px
  button-primary:
    backgroundColor: "{colors.primary}"
    textColor: "{colors.on-primary}"
    typography: "{typography.label-sm}"
    rounded: "{rounded.pill}"
    height: 48px
    padding: 16px
  button-primary-pressed:
    backgroundColor: "{colors.secondary}"
    textColor: "{colors.on-secondary}"
  glass-overlay:
    backgroundColor: "{colors.glass-tint}"
    rounded: "{rounded.lg}"
  floating-log-pill:
    backgroundColor: "{colors.surface-container}"
    textColor: "{colors.primary}"
    typography: "{typography.label-mono}"
    rounded: "{rounded.pill}"
    height: 40px
    padding: 12px
---

# XCan Design System

## Overview

High-Performance Automotive Cockpit meets Precision Instrumentation.

XCan is engineered for drivers, technicians, and track enthusiasts who require instant, legible telemetry feedback under varied lighting conditions. The visual atmosphere is intentionally dark, tactical, and distraction-free: deep charcoal surfaces eliminate cabin glare at night, while electric blue and neon green indicators create immediate visual priority for mission-critical engine parameters.

Interactions follow Emil Kowalski motion physics: tactile spring feedback on press, rapid ease-out transitions for high responsiveness, and frosted glassmorphic layering to maintain spatial depth without heavy drop shadows.

## Colors

The palette is rooted in deep obsidian tones accented by high-saturation phosphor indicators, strictly adhering to dark-mode-first automotive standards.

- **Background (`#121212` - Deep Charcoal):** Absolute foundation for the application. Prevents OLED power drain and driver eye fatigue during night driving.
- **Surface (`#1E1E1E` - Charcoal Surface):** Primary elevation layer for sheets, menus, and container backgrounds.
- **Surface Container (`#1A1D24`):** Elevated contrast surface for floating controls, dialogs, and pill widgets.
- **Primary (`#00E5FF` - Electric Blue):** Core dynamic telemetry color representing active sensor streams, speed gauges, active tabs, and primary controls.
- **Secondary (`#00FF88` - Neon Accent):** Vital status and success indicator. Used for normal engine operating temperatures, active Bluetooth connections, and healthy sensor bitmasks.
- **Error (`#FF4C4C` - Error Red):** High-priority warning signal reserved exclusively for Diagnostic Trouble Codes (DTCs), ECU communication failures, and engine over-limit boundaries.
- **Text Hierarchy:**
  - `on-surface` (`#FFFFFF`): High-contrast pure white for critical metrics, active values, and primary headings.
  - `on-background` (`#B0B0B0` - Light Gray): Subtitles, sensor units (`km/h`, `RPM`, `°C`), and secondary metadata.

## Typography

Typography establishes a strict hierarchy separating dynamic telemetry metrics from narrative UI text.

- **Telemetry Readouts (`display-mono` & `label-mono`):** Rendered in strict monospaced type (`FontFamily.Monospace`). Fixed character widths prevent digital jitter and layout shift as live values fluctuate at 60 Hz.
- **Headlines & Interface (`headline-md` & `body-lg`):** Clean, modern grotesque sans-serif for clear legibility at glancing angles inside a moving vehicle.
- **Units & Subtitles (`label-sm`):** Secondary sizing with deliberate letter-spacing to ensure units remain clearly distinguishable from numerical values.

## Layout

The layout adheres to a strict **8dp base grid system** with 4dp micro-steps (`xs: 4px`) for compact instrumentation density.

- **Screen Padding:** Standard horizontal margins use `16px` (`spacing.md`) on compact mobile screens, expanding to `24px` (`spacing.lg`) on tablets and wide in-dash displays.
- **Dashboard Grid:** Two-column telemetry dial grid with square aspect ratio cells (`aspectRatio(1f)`), ensuring zero gauge distortion across varying screen dimensions.
- **Floating Controls:** Persistent controls (such as `LogFloatingControl` and the bottom `NavigationBar`) float with a `16px` inset from screen edges over scrollable content.

## Elevation & Depth

Visual hierarchy is communicated through **Translucent Glassmorphism and Layered Tints** rather than diffuse drop shadows:

- **Haze Frosted Glass (`Modifier.glassmorphism`):** Containers apply a 10dp blur over underlying canvas and graph elements, tinted with 5% translucent white (`rgba(255, 255, 255, 0.05)`).
- **Subtle Stroke Boundaries:** Glass cards are demarcated by a delicate 1dp boundary at 15% opacity (`rgba(255, 255, 255, 0.15)`), eliminating the muddy appearance of traditional shadows on dark OLED panels.
- **Depth Layers:**
  1. Base: Deep Charcoal (`#121212`) grid canvas.
  2. Midground: Live SVG/Canvas dials and Vico timeseries charts.
  3. Foreground: Glassmorphic top bar, bottom bar, and floating control pills.

## Shapes

Shapes communicate technical precision through controlled curvature:

- **Controls & Chips (`8px` - `rounded.sm`):** Snug corner radius for status badges, sensor PID chips, and table cells.
- **Interactive Cards (`16px` - `rounded.md`):** Medium radius for DTC diagnostic cards and maintenance timeline entries.
- **Dialogs & Bottom Sheets (`24px` - `rounded.lg`):** Soft modern curvature for modal sheets, ECU config selectors, and device pairing dialogs.
- **Floating Controls (`32px` - `rounded.pill`):** Full capsule pill shape for floating session recorders and primary action buttons.

## Components

- **Custom Canvas Telemetry Dial (`TelemetryDial`):**
  - Sweeps a 240° arc starting at 150° rotation.
  - Features gradient sweeps (`ElectricBlue` to `NeonAccent`), calibrated circular tick marks, warning zones, and centered digital readouts.
- **Glass Top App Bar (`GlassTopAppBar`):**
  - Semi-transparent frosted glass header fixed to the top window insets.
  - Hosts the active vehicle name pill (`LocalActiveCarName`) and screen title.
- **Floating Log Pill (`LogFloatingControl`):**
  - Persistent capsule indicator displaying live elapsed time (`%02d:%02d`) with interactive Pause, Resume, and Stop controls.
- **DTC Diagnostic Card:**
  - High-contrast card with system prefix badges (`P`, `C`, `B`, `U`), severity coloring, and description.
- **Emil Kowalski Touch Physics:**
  - All interactive elements implement spring scale reduction (`scale(0.97f)`) and alpha reduction (`0.8f`) on press via `Modifier.bounceClick()` / `Modifier.pressBounce()`.

## Do's and Don'ts

- **Do** use `display-mono` or `label-mono` for all numeric sensor values, speeds, temperatures, and DTC codes to prevent layout jitter.
- **Do** wrap elevated cards in `Modifier.glassmorphism()` or `CharcoalSurface` borders rather than adding black drop shadows.
- **Do** apply `Modifier.pressBounce()` or `Modifier.bounceClick()` to interactive buttons and cards for tactile spring feedback.
- **Do** preserve the dark theme universally—automotive telemetry must never flash bright white backgrounds.
- **Don't** use generic Material 3 saturated primary purple or pastel tones; stick strictly to `ElectricBlue` (`#00E5FF`) and `NeonAccent` (`#00FF88`).
- **Don't** use sharp 0px corners or arbitrarily large radii (>32px) on standard cards.
- **Don't** clutter the real-time telemetry screen with low-contrast decorative text; keep vital metrics legible at arm's length.

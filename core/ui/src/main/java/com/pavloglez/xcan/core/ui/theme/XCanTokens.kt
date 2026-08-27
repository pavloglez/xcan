package com.pavloglez.xcan.core.ui.theme

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Centralized design tokens for the XCan design system.
 * All UI dimensions, alphas, and animation values live here.
 */
object XCanTokens {
    // --- Corner radii ---
    val CornerSmall = 8.dp
    val CornerMedium = 16.dp
    val CornerLarge = 24.dp
    val CornerPill = 32.dp

    // --- Glassmorphism ---
    const val GlassBgAlpha = 0.05f
    const val GlassBorderAlpha = 0.15f
    val GlassBorderWidth = 1.dp
    val GlassBlurRadius = 10.dp

    // --- Animation durations (ms) ---
    const val DurationFast = 200
    const val DurationStandard = 300
    const val DurationSlow = 800
    const val StaggerDelay = 40
    const val StaggerOffsetY = 20f
    const val RecordingPulseDuration = 700
    const val AccentColorAnimDuration = 300

    // --- Bounce / press feedback ---
    const val BouncePressedScale = 0.97f
    const val BouncePressedAlpha = 0.8f

    // --- Navigation bar ---
    val NavBarHeight = 68.dp
    val NavBarPadding = 16.dp
    val NavBarCornerRadius = 32.dp
    const val NavBarBgAlpha = 0.5f
    const val NavItemGlowAlpha = 0.5f
    const val NavItemGlowRadiusFactor = 2f
    val NavLabelFontSize = 10.sp

    // --- Gauge dial ---
    const val DialSweepAngle = 240f
    const val DialStartRotation = 150f
    const val DialAnimDuration = 300

    // --- Navigation transitions ---
    const val NavEnterScale = 0.95f

    // --- Button ---
    val ButtonHorizontalPadding = 24.dp
    val ButtonVerticalPadding = 12.dp
    const val ButtonDisabledAlpha = 0.5f

    // --- Divider ---
    const val DividerAlpha = 0.1f
    val DividerThickness = 1.dp

    // --- Typography ---
    val SubtitleFontSize = 12.sp

    // --- Background ---
    const val GradientEndAlpha = 0.8f

    // --- Track mode ---
    const val TrackPulseMaxScale = 1.3f
    const val TrackPulseInitialAlpha = 0.5f
    const val ConnectingMinAlpha = 0.3f
    const val RecordingPulseMinAlpha = 0.2f

    // --- Stagger animation (LogSessions) ---
    const val ListStaggerDelay = 30

    // --- Timeline ---
    val TimelineConnectorHeight = 150.dp
}

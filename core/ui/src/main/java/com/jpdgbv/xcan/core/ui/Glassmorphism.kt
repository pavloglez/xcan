package com.jpdgbv.xcan.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.jpdgbv.xcan.core.ui.theme.DeepCharcoal
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect

/**
 * Reusable LocalHazeState so multiple screens can share it or we can pass it down.
 */
val LocalHazeState = androidx.compose.runtime.compositionLocalOf { HazeState() }

/**
 * Applies a true frosted glass effect using the Haze library.
 * The underlying content must be wrapped in `Modifier.haze(hazeState)`.
 */
fun Modifier.glassmorphism(
    hazeState: HazeState,
    shape: Shape = RoundedCornerShape(24.dp),
    backgroundColor: Color = Color(0xFFFFFFFF).copy(alpha = 0.05f),
    borderColor: Color = Color(0xFFFFFFFF).copy(alpha = 0.15f),
    borderWidth: androidx.compose.ui.unit.Dp = 1.dp
): Modifier = this
    .clip(shape)
    .hazeEffect(
        state = hazeState,
        style = dev.chrisbanes.haze.HazeStyle(
            backgroundColor = DeepCharcoal, // Transparent to allow blur to show
            tint = dev.chrisbanes.haze.HazeTint(backgroundColor),
            blurRadius = 10.dp,
            //noiseFactor = 1f
        )
    )
    .border(borderWidth, borderColor, shape)

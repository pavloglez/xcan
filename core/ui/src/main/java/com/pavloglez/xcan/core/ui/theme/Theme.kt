package com.pavloglez.xcan.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val XCanColorScheme = darkColorScheme(
    primary = ElectricBlue,
    secondary = NeonAccent,
    background = DeepCharcoal,
    surface = CharcoalSurface,
    onPrimary = DeepCharcoal,
    onSecondary = DeepCharcoal,
    onBackground = LightGrayText,
    onSurface = WhiteText,
    error = ErrorRed
)

@Composable
fun XCanTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = XCanColorScheme, // We enforce the dark automotive theme universally
        typography = Typography,
        content = content
    )
}

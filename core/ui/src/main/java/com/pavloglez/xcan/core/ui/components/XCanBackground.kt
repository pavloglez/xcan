package com.pavloglez.xcan.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import com.pavloglez.xcan.core.ui.theme.XCanTokens

@Composable
fun XCanBackground(
    modifier: Modifier = Modifier,
    useGradient: Boolean = false,
    content: @Composable BoxScope.() -> Unit
) {
    val backgroundModifier = if (useGradient) {
        modifier.background(
            brush = Brush.verticalGradient(
                colors = listOf(
                    MaterialTheme.colorScheme.background,
                    MaterialTheme.colorScheme.background.copy(alpha = XCanTokens.GradientEndAlpha)
                )
            )
        )
    } else {
        modifier.background(MaterialTheme.colorScheme.background)
    }

    Box(
        modifier = backgroundModifier,
        content = content
    )
}

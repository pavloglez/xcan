package com.pavloglez.xcan.core.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.pavloglez.xcan.core.ui.theme.XCanTokens

@Composable
fun XCanCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(XCanTokens.CornerMedium),
        color = MaterialTheme.colorScheme.surface,
        content = content
    )
}

@Composable
fun XCanButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: String,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = RoundedCornerShape(XCanTokens.CornerSmall),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = XCanTokens.ButtonDisabledAlpha),
            disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = XCanTokens.ButtonDisabledAlpha)
        ),
        contentPadding = PaddingValues(horizontal = XCanTokens.ButtonHorizontalPadding, vertical = XCanTokens.ButtonVerticalPadding)
    ) {
        Text(text = text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun XCanDivider(
    modifier: Modifier = Modifier,
    color: Color = Color.White.copy(alpha = XCanTokens.DividerAlpha)
) {
    HorizontalDivider(
        modifier = modifier,
        color = color,
        thickness = XCanTokens.DividerThickness
    )
}

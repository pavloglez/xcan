package com.pavloglez.xcan.core.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.pavloglez.xcan.core.ui.LocalHazeState
import com.pavloglez.xcan.core.ui.glassmorphism
import com.pavloglez.xcan.core.ui.theme.ElectricBlue
import com.pavloglez.xcan.core.ui.theme.XCanTokens

val LocalActiveCarName = compositionLocalOf<String?> { null }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlassTopAppBar(
    modifier: Modifier = Modifier,
    title: String? = null,
    titleContent: @Composable () -> Unit = { title?.let { Text(it, color = Color.White) } },
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {}
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .glassmorphism(
                hazeState = LocalHazeState.current,
                shape = RoundedCornerShape(bottomStart = XCanTokens.CornerLarge, bottomEnd = XCanTokens.CornerLarge)
            )
    ) {
        TopAppBar(
            title = {
                val activeCar = LocalActiveCarName.current
                Column {
                    titleContent()
                    if (activeCar != null) {
                        Text(
                            text = activeCar,
                            color = ElectricBlue,
                            fontSize = XCanTokens.SubtitleFontSize
                        )
                    }
                }
            },
            navigationIcon = navigationIcon,
            actions = actions,
            windowInsets = androidx.compose.foundation.layout.WindowInsets(0.dp),
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                titleContentColor = Color.White,
                navigationIconContentColor = ElectricBlue,
                actionIconContentColor = ElectricBlue
            )
        )
    }
}

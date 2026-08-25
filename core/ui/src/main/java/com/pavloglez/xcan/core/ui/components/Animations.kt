package com.pavloglez.xcan.core.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.pavloglez.xcan.core.ui.theme.XCanDuration
import com.pavloglez.xcan.core.ui.theme.XCanEasing

fun Modifier.staggerEnter(
    index: Int,
    baseDelay: Int = 40,
    duration: Int = 300,
    initialOffsetY: Float = 20f
): Modifier = composed {
    val alpha = remember { Animatable(0f) }
    val offsetY = remember { Animatable(initialOffsetY) }

    LaunchedEffect(Unit) {
        val delay = index * baseDelay
        
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = duration,
                delayMillis = delay,
                easing = XCanEasing.EaseOut
            )
        )
    }
    
    LaunchedEffect(Unit) {
        val delay = index * baseDelay
        
        offsetY.animateTo(
            targetValue = 0f,
            animationSpec = tween(
                durationMillis = duration,
                delayMillis = delay,
                easing = XCanEasing.EaseOut
            )
        )
    }

    this.graphicsLayer {
        this.alpha = alpha.value
    }.offset(y = offsetY.value.dp)
}

package com.jpdgbv.xcan.feature.dashboard.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jpdgbv.xcan.core.data.LoggingState
import com.jpdgbv.xcan.core.ui.LocalHazeState
import com.jpdgbv.xcan.core.ui.components.bounceClick
import com.jpdgbv.xcan.core.ui.glassmorphism
import kotlinx.coroutines.delay

private val GlassWhite = Color(0xFFFFFFFF).copy(alpha = 0.12f)
private val GlassBorder = Color(0xFFFFFFFF).copy(alpha = 0.18f)

@Composable
fun LogFloatingControl(
    loggingState: LoggingState,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onStop: () -> Unit,
    onStartLog: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    val accentColor by animateColorAsState(
        targetValue = when (loggingState) {
            is LoggingState.Recording -> MaterialTheme.colorScheme.error
            is LoggingState.Paused -> MaterialTheme.colorScheme.secondary
            LoggingState.Idle -> MaterialTheme.colorScheme.primary
        },
        animationSpec = tween(300),
        label = "accentColor"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(tween(700), RepeatMode.Reverse),
        label = "pulseAlpha"
    )

    var elapsedMs by remember { mutableLongStateOf(0L) }
    LaunchedEffect(loggingState) {
        if (loggingState is LoggingState.Recording) {
            while (true) {
                elapsedMs = System.currentTimeMillis() - loggingState.startMs
                delay(1000)
            }
        } else if (loggingState is LoggingState.Paused) {
            elapsedMs = loggingState.pausedAtMs - loggingState.startMs
        }
    }

    Row(
        modifier = modifier
            .glassmorphism(
                hazeState = LocalHazeState.current,
                shape = RoundedCornerShape(50)
            )
            .then(
                if (loggingState is LoggingState.Idle) {
                    Modifier.bounceClick(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onStartLog()
                    })
                } else Modifier
            )
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        when (loggingState) {
            LoggingState.Idle -> {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Log",
                    color = accentColor,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            }
            is LoggingState.Recording, is LoggingState.Paused -> {
                val isPaused = loggingState is LoggingState.Paused
                val mins = (elapsedMs / 1000) / 60
                val secs = (elapsedMs / 1000) % 60

                Icon(
                    imageVector = Icons.Filled.Stop,
                    contentDescription = null,
                    tint = accentColor.copy(alpha = if (!isPaused) pulseAlpha else 1f),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(Modifier.width(6.dp))
                AnimatedContent(
                    targetState = isPaused,
                    transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(200)) },
                    label = "timerLabel"
                ) { paused ->
                    Text(
                        text = if (paused) "Paused" else "%02d:%02d".format(mins, secs),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp
                    )
                }
                Spacer(Modifier.width(10.dp))
                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        if (isPaused) onResume() else onPause()
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (isPaused) Icons.Filled.PlayArrow else Icons.Filled.Pause,
                        contentDescription = if (isPaused) "Resume" else "Pause",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onStop()
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Stop,
                        contentDescription = "Stop",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

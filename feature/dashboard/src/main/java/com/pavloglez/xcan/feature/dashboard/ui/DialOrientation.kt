package com.pavloglez.xcan.feature.dashboard.ui

import android.view.OrientationEventListener
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.pavloglez.xcan.core.ui.theme.XCanEasing

/**
 * Calculates the shortest-path target angle so rotations between adjacent
 * quadrants always animate through a 90-degree delta without spinning 270 degrees.
 */
internal fun calculateShortestTargetAngle(currentAngle: Float, targetQuadrantDegrees: Float): Float {
    var diff = (targetQuadrantDegrees - currentAngle) % 360f
    if (diff > 180f) diff -= 360f
    if (diff < -180f) diff += 360f
    return currentAngle + diff
}

/**
 * Maps raw accelerometer orientation (0..359 degrees) to the dial counter-rotation
 * angle needed to keep dials upright relative to gravity.
 *
 * Orientation sensor convention:
 * - 0°: Normal portrait -> dial rotation 0°
 * - 90°: Left side up (landscape right tilt) -> dial counter-rotation -90°
 * - 180°: Upside down -> dial counter-rotation 180°
 * - 270°: Right side up (landscape left tilt) -> dial counter-rotation 90°
 */
internal fun mapOrientationToDialAngle(orientationDegrees: Int): Float = when (orientationDegrees) {
    in 45..134 -> -90f
    in 135..224 -> 180f
    in 225..314 -> 90f
    else -> 0f
}

/**
 * Observes device orientation via accelerometer and returns an animated rotation
 * angle (in degrees) to keep dashboard dials upright in the vehicle.
 */
@Composable
fun rememberDialRotation(): State<Float> {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val targetDegrees = remember { mutableFloatStateOf(0f) }
    val animatableRotation = remember { Animatable(0f) }

    DisposableEffect(context, lifecycleOwner) {
        val orientationEventListener = object : OrientationEventListener(context) {
            override fun onOrientationChanged(orientation: Int) {
                if (orientation == ORIENTATION_UNKNOWN) return
                val newTarget = mapOrientationToDialAngle(orientation)
                if (newTarget != targetDegrees.floatValue) {
                    targetDegrees.floatValue = newTarget
                }
            }
        }

        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                if (orientationEventListener.canDetectOrientation()) {
                    orientationEventListener.enable()
                }
            } else if (event == Lifecycle.Event.ON_PAUSE) {
                orientationEventListener.disable()
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)
        if (orientationEventListener.canDetectOrientation()) {
            orientationEventListener.enable()
        }

        onDispose {
            orientationEventListener.disable()
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(targetDegrees.floatValue) {
        val target = calculateShortestTargetAngle(
            currentAngle = animatableRotation.value,
            targetQuadrantDegrees = targetDegrees.floatValue
        )
        animatableRotation.animateTo(
            targetValue = target,
            animationSpec = tween(durationMillis = 350, easing = XCanEasing.EaseOut)
        )
    }

    return animatableRotation.asState()
}

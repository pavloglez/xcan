package com.jpdgbv.xcan.core.ui.theme

import androidx.compose.animation.core.CubicBezierEasing

object XCanEasing {
    /** Strong ease-out for UI (starts fast, feels responsive) */
    val EaseOut = CubicBezierEasing(0.23f, 1f, 0.32f, 1f)
    
    /** Strong ease-in-out for on-screen movement */
    val EaseInOut = CubicBezierEasing(0.77f, 0f, 0.175f, 1f)
    
    /** iOS-like drawer curve */
    val EaseDrawer = CubicBezierEasing(0.32f, 0.72f, 0f, 1f)
}

object XCanDuration {
    const val PressFeedback = 160
    const val Standard = 300
}

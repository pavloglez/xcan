package com.pavloglez.xcan.core.model

data class LogSession(
    val id: String,
    val carId: String,
    val carLabel: String,
    val startTimeMs: Long,
    val endTimeMs: Long? = null,
    val isPaused: Boolean = false
) {
    val durationMs: Long?
        get() = endTimeMs?.minus(startTimeMs)

    val isActive: Boolean
        get() = endTimeMs == null
}

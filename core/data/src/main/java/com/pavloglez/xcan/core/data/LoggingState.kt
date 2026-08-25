package com.pavloglez.xcan.core.data

sealed interface LoggingState {
    object Idle : LoggingState
    data class Recording(
        val sessionId: String,
        val carLabel: String,
        val startMs: Long
    ) : LoggingState
    data class Paused(
        val sessionId: String,
        val carLabel: String,
        val startMs: Long,
        val pausedAtMs: Long
    ) : LoggingState
}

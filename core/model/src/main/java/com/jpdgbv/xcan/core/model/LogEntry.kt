package com.jpdgbv.xcan.core.model

data class LogEntry(
    val id: Long = 0,
    val sessionId: String,
    val timestampMs: Long,
    val pid: String,
    val value: Float
)

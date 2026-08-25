package com.pavloglez.xcan.core.model

data class TelemetryFrame(
    val id: String,
    val timestampMs: Long,
    val sensors: Map<String, Float>
)

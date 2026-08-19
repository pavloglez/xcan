package com.jpdgbv.xcan.core.model

data class ObdSensor(
    val id: String = java.util.UUID.randomUUID().toString(),
    val pid: String,
    val displayName: String,
    val unit: String,
    val expectedBytes: Int,
    val formula: String
)

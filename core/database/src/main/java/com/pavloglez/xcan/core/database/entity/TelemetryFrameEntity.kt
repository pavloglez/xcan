package com.pavloglez.xcan.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.pavloglez.xcan.core.model.TelemetryFrame

@Entity(tableName = "telemetry_frames")
data class TelemetryFrameEntity(
    @PrimaryKey
    val id: String,
    val timestampMs: Long,
    val sensors: Map<String, Float>
)

fun TelemetryFrameEntity.toDomainModel() = TelemetryFrame(
    id = id,
    timestampMs = timestampMs,
    sensors = sensors
)

fun TelemetryFrame.toEntity() = TelemetryFrameEntity(
    id = id,
    timestampMs = timestampMs,
    sensors = sensors
)

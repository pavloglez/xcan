package com.pavloglez.xcan.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.pavloglez.xcan.core.model.LogSession

@Entity(tableName = "log_sessions")
data class LogSessionEntity(
    @PrimaryKey val id: String,
    val carId: String,
    val carLabel: String,
    val startTimeMs: Long,
    val endTimeMs: Long? = null,
    val isPaused: Boolean = false
)

fun LogSessionEntity.toDomainModel() = LogSession(
    id = id,
    carId = carId,
    carLabel = carLabel,
    startTimeMs = startTimeMs,
    endTimeMs = endTimeMs,
    isPaused = isPaused
)

fun LogSession.toEntity() = LogSessionEntity(
    id = id,
    carId = carId,
    carLabel = carLabel,
    startTimeMs = startTimeMs,
    endTimeMs = endTimeMs,
    isPaused = isPaused
)

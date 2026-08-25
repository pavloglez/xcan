package com.pavloglez.xcan.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.pavloglez.xcan.core.model.LogEntry

@Entity(
    tableName = "log_entries",
    foreignKeys = [
        ForeignKey(
            entity = LogSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("sessionId")]
)
data class LogEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: String,
    val timestampMs: Long,
    val pid: String,
    val value: Float
)

fun LogEntryEntity.toDomainModel() = LogEntry(
    id = id,
    sessionId = sessionId,
    timestampMs = timestampMs,
    pid = pid,
    value = value
)

fun LogEntry.toEntity() = LogEntryEntity(
    id = id,
    sessionId = sessionId,
    timestampMs = timestampMs,
    pid = pid,
    value = value
)

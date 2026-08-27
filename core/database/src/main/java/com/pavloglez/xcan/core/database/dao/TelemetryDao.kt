package com.pavloglez.xcan.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pavloglez.xcan.core.database.entity.TelemetryFrameEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TelemetryDao {
    @Query("SELECT * FROM telemetry_frames ORDER BY timestampMs DESC LIMIT :limit")
    fun getRecentTelemetry(limit: Int): Flow<List<TelemetryFrameEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTelemetry(frame: TelemetryFrameEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTelemetries(frames: List<TelemetryFrameEntity>)

    @Query("DELETE FROM telemetry_frames WHERE timestampMs < :olderThanMs")
    fun deleteOldTelemetry(olderThanMs: Long): Int
}

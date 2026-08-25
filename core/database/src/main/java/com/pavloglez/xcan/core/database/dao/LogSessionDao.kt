package com.pavloglez.xcan.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.pavloglez.xcan.core.database.entity.LogSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LogSessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSession(session: LogSessionEntity): Long

    @Update
    fun updateSession(session: LogSessionEntity): Int

    @Delete
    fun deleteSession(session: LogSessionEntity): Int

    @Query("DELETE FROM log_sessions")
    fun deleteAllSessions(): Int

    @Query("SELECT * FROM log_sessions ORDER BY startTimeMs DESC")
    fun getAllSessions(): Flow<List<LogSessionEntity>>

    @Query("SELECT * FROM log_sessions WHERE id = :id")
    fun getSessionById(id: String): Flow<LogSessionEntity?>

    @Query("SELECT * FROM log_sessions WHERE endTimeMs IS NULL LIMIT 1")
    fun getActiveSession(): LogSessionEntity?
}


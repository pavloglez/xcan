package com.jpdgbv.xcan.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jpdgbv.xcan.core.database.entity.LogEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LogEntryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertEntry(entry: LogEntryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertEntries(entries: List<LogEntryEntity>): List<Long>

    @Query("SELECT * FROM log_entries WHERE sessionId = :sessionId ORDER BY timestampMs ASC")
    fun getEntriesForSession(sessionId: String): Flow<List<LogEntryEntity>>

    @Query("SELECT COUNT(*) FROM log_entries WHERE sessionId = :sessionId")
    fun getEntryCountForSession(sessionId: String): Int
}


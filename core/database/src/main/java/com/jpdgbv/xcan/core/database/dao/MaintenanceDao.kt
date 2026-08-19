package com.jpdgbv.xcan.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jpdgbv.xcan.core.database.entity.MaintenanceLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MaintenanceDao {
    @Query("SELECT * FROM maintenance_logs WHERE carId = :carId ORDER BY dateMs DESC")
    fun getAllLogs(carId: String): Flow<List<MaintenanceLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertLog(log: MaintenanceLogEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertLogs(logs: List<MaintenanceLogEntity>): List<Long>

    @Query("DELETE FROM maintenance_logs WHERE id = :id")
    fun deleteLog(id: String): Int
}

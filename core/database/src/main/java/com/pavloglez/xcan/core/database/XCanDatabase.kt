package com.pavloglez.xcan.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.pavloglez.xcan.core.database.dao.CarProfileDao
import com.pavloglez.xcan.core.database.dao.LogEntryDao
import com.pavloglez.xcan.core.database.dao.LogSessionDao
import com.pavloglez.xcan.core.database.dao.MaintenanceDao
import com.pavloglez.xcan.core.database.dao.TelemetryDao
import com.pavloglez.xcan.core.database.entity.CarProfileEntity
import com.pavloglez.xcan.core.database.entity.LogEntryEntity
import com.pavloglez.xcan.core.database.entity.LogSessionEntity
import com.pavloglez.xcan.core.database.entity.MaintenanceLogEntity
import com.pavloglez.xcan.core.database.entity.TelemetryFrameEntity

@Database(
    entities = [
        MaintenanceLogEntity::class,
        TelemetryFrameEntity::class,
        CarProfileEntity::class,
        LogSessionEntity::class,
        LogEntryEntity::class
    ],
    version = 4,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class XCanDatabase : RoomDatabase() {
    abstract fun carProfileDao(): CarProfileDao
    abstract fun maintenanceDao(): MaintenanceDao
    abstract fun telemetryDao(): TelemetryDao
    abstract fun logSessionDao(): LogSessionDao
    abstract fun logEntryDao(): LogEntryDao
}


package com.jpdgbv.xcan.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.jpdgbv.xcan.core.database.dao.CarProfileDao
import com.jpdgbv.xcan.core.database.dao.MaintenanceDao
import com.jpdgbv.xcan.core.database.dao.TelemetryDao
import com.jpdgbv.xcan.core.database.entity.CarProfileEntity
import com.jpdgbv.xcan.core.database.entity.MaintenanceLogEntity
import com.jpdgbv.xcan.core.database.entity.TelemetryFrameEntity
import androidx.room.TypeConverters

@Database(
    entities = [MaintenanceLogEntity::class, TelemetryFrameEntity::class, CarProfileEntity::class],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class XCanDatabase : RoomDatabase() {
    abstract fun carProfileDao(): CarProfileDao
    abstract fun maintenanceDao(): MaintenanceDao
    abstract fun telemetryDao(): TelemetryDao
}

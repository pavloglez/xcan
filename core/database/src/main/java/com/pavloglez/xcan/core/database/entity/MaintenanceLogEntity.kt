package com.pavloglez.xcan.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.pavloglez.xcan.core.model.MaintenanceLog

@Entity(
    tableName = "maintenance_logs",
    foreignKeys = [
        ForeignKey(
            entity = CarProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["carId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["carId"])]
)
data class MaintenanceLogEntity(
    @PrimaryKey
    val id: String,
    val carId: String,
    val serviceType: String,
    val dateMs: Long,
    val mileage: Int,
    val cost: Double,
    val notes: String,
    val relatedDtc: String? = null
)

fun MaintenanceLogEntity.toDomainModel() = MaintenanceLog(
    id = id,
    carId = carId,
    serviceType = serviceType,
    dateMs = dateMs,
    mileage = mileage,
    cost = cost,
    notes = notes,
    relatedDtc = relatedDtc
)

fun MaintenanceLog.toEntity() = MaintenanceLogEntity(
    id = id,
    carId = carId,
    serviceType = serviceType,
    dateMs = dateMs,
    mileage = mileage,
    cost = cost,
    notes = notes,
    relatedDtc = relatedDtc
)

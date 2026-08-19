package com.jpdgbv.xcan.core.network.model

import com.google.gson.annotations.SerializedName
import com.jpdgbv.xcan.core.model.MaintenanceLog

data class MaintenanceLogDto(
    @SerializedName("id") val id: String,
    @SerializedName("service_type") val serviceType: String,
    @SerializedName("date_ms") val dateMs: Long,
    @SerializedName("mileage") val mileage: Int,
    @SerializedName("cost") val cost: Double,
    @SerializedName("notes") val notes: String
)

fun MaintenanceLogDto.toDomainModel() = MaintenanceLog(
    id = id,
    carId = "default_car", // Fallback for old API data
    serviceType = serviceType,
    dateMs = dateMs,
    mileage = mileage,
    cost = cost,
    notes = notes
)

fun MaintenanceLog.toDto() = MaintenanceLogDto(
    id = id,
    serviceType = serviceType,
    dateMs = dateMs,
    mileage = mileage,
    cost = cost,
    notes = notes
)

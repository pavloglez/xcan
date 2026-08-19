package com.jpdgbv.xcan.core.model

data class MaintenanceLog(
    val id: String,
    val carId: String,
    val serviceType: String,
    val dateMs: Long,
    val mileage: Int,
    val cost: Double,
    val notes: String,
    val relatedDtc: String? = null
)

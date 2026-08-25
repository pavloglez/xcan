package com.pavloglez.xcan.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.pavloglez.xcan.core.model.CarProfile

@Entity(tableName = "car_profiles")
data class CarProfileEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val make: String,
    val model: String,
    val year: Int,
    val isActive: Boolean
)

fun CarProfileEntity.toDomainModel() = CarProfile(
    id = id,
    name = name,
    make = make,
    model = model,
    year = year,
    isActive = isActive
)

fun CarProfile.toEntity() = CarProfileEntity(
    id = id,
    name = name,
    make = make,
    model = model,
    year = year,
    isActive = isActive
)

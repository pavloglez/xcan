package com.jpdgbv.xcan.core.data

import com.jpdgbv.xcan.core.database.dao.CarProfileDao
import com.jpdgbv.xcan.core.database.entity.toDomainModel
import com.jpdgbv.xcan.core.database.entity.toEntity
import com.jpdgbv.xcan.core.model.CarProfile

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import com.jpdgbv.xcan.core.model.DispatcherProvider
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CarRepository @Inject constructor(
    private val dispatchers: DispatcherProvider,
    private val carProfileDao: CarProfileDao
) {
    fun getAllCars(): Flow<List<CarProfile>> {
        return carProfileDao.getAllProfiles().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    fun getActiveCar(): Flow<CarProfile?> {
        return carProfileDao.getActiveProfile().map { it?.toDomainModel() }
    }

    suspend fun addCar(car: CarProfile) {
        withContext(dispatchers.io) {
            carProfileDao.insertProfile(car.toEntity())
        }
    }

    suspend fun setActiveCar(carId: String) {
        withContext(dispatchers.io) {
            carProfileDao.setActiveProfile(carId)
        }
    }

    suspend fun deleteCar(carId: String) {
        withContext(dispatchers.io) {
            carProfileDao.deleteProfile(carId)
        }
    }
}

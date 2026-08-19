package com.jpdgbv.xcan.core.data

import com.jpdgbv.xcan.core.database.dao.MaintenanceDao
import com.jpdgbv.xcan.core.database.entity.toDomainModel
import com.jpdgbv.xcan.core.database.entity.toEntity
import com.jpdgbv.xcan.core.model.MaintenanceLog
import com.jpdgbv.xcan.core.network.XCanApiService
import com.jpdgbv.xcan.core.network.model.toDomainModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton

class MaintenanceRepository @Inject constructor(
    private val maintenanceDao: MaintenanceDao,
    private val apiService: XCanApiService,
    private val carRepository: CarRepository
) {

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    fun getAllLogs(): Flow<List<MaintenanceLog>> {
        return carRepository.getActiveCar().flatMapLatest { car ->
            if (car == null) {
                emptyFlow()
            } else {
                maintenanceDao.getAllLogs(car.id).map { entities ->
                    entities.map { it.toDomainModel() }
                }
            }
        }
    }

    suspend fun addLog(log: MaintenanceLog) {
        withContext(Dispatchers.IO) {
            maintenanceDao.insertLog(log.toEntity())
        }
    }

    suspend fun syncLogs() {
        withContext(Dispatchers.IO) {
            try {
                val remoteLogs = apiService.fetchMaintenanceLogs()
                maintenanceDao.insertLogs(remoteLogs.map { it.toDomainModel().toEntity() })
            } catch (e: Exception) {
                // Ignore network errors, rely on local cache
            }
        }
    }
}

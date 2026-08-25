package com.pavloglez.xcan.core.data

import com.pavloglez.xcan.core.database.dao.MaintenanceDao
import com.pavloglez.xcan.core.database.entity.toDomainModel
import com.pavloglez.xcan.core.database.entity.toEntity
import com.pavloglez.xcan.core.model.MaintenanceLog
import com.pavloglez.xcan.core.network.XCanApiService
import com.pavloglez.xcan.core.network.model.toDomainModel

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import com.pavloglez.xcan.core.model.DispatcherProvider
import javax.inject.Inject
import javax.inject.Singleton

@Singleton

class MaintenanceRepository @Inject constructor(
    private val dispatchers: DispatcherProvider,
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
        withContext(dispatchers.io) {
            maintenanceDao.insertLog(log.toEntity())
        }
    }

    suspend fun syncLogs() {
        withContext(dispatchers.io) {
            try {
                val remoteLogs = apiService.fetchMaintenanceLogs()
                maintenanceDao.insertLogs(remoteLogs.map { it.toDomainModel().toEntity() })
            } catch (e: Exception) {
                // Ignore network errors, rely on local cache
            }
        }
    }
}

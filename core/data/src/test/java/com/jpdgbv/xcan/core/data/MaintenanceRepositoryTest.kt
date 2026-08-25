package com.jpdgbv.xcan.core.data

import com.jpdgbv.xcan.core.model.DispatcherProvider
import com.jpdgbv.xcan.core.database.dao.MaintenanceDao
import com.jpdgbv.xcan.core.database.entity.MaintenanceLogEntity
import com.jpdgbv.xcan.core.model.CarProfile
import com.jpdgbv.xcan.core.model.MaintenanceLog
import com.jpdgbv.xcan.core.network.XCanApiService
import com.jpdgbv.xcan.core.network.model.MaintenanceLogDto
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.util.UUID

class MaintenanceRepositoryTest {

    private lateinit var maintenanceDao: MaintenanceDao
    private lateinit var apiService: XCanApiService
    private lateinit var carRepository: CarRepository
    private lateinit var dispatcherProvider: DispatcherProvider
    private lateinit var maintenanceRepository: MaintenanceRepository

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        maintenanceDao = mockk(relaxed = true)
        apiService = mockk(relaxed = true)
        carRepository = mockk(relaxed = true)
        
        dispatcherProvider = object : DispatcherProvider {
            override val main = testDispatcher
            override val io = testDispatcher
            override val default = testDispatcher
        }
        
        val activeCar = CarProfile("car1", "My Car", "Make", "Model", 2020, true)
        every { carRepository.getActiveCar() } returns flowOf(activeCar)
        
        maintenanceRepository = MaintenanceRepository(dispatcherProvider, maintenanceDao, apiService, carRepository)
    }

    @Test
    fun `addLog inserts log into database with mapped entity`() = runTest(testDispatcher) {
        // Given
        val log = MaintenanceLog(
            id = UUID.randomUUID().toString(),
            carId = "car1",
            serviceType = "Oil Change",
            dateMs = 1000L,
            mileage = 50000,
            cost = 150.0,
            notes = "Routine maintenance"
        )

        // When
        maintenanceRepository.addLog(log)

        // Then
        coVerify {
            maintenanceDao.insertLog(match {
                it.id == log.id &&
                it.serviceType == log.serviceType &&
                it.dateMs == log.dateMs &&
                it.mileage == log.mileage &&
                it.cost == log.cost &&
                it.notes == log.notes
            })
        }
    }

    @Test
    fun `getAllLogs returns mapped models from dao flow`() = runTest(testDispatcher) {
        // Given
        val entity = MaintenanceLogEntity(
            id = "1",
            carId = "car1",
            serviceType = "Brakes",
            dateMs = 2000L,
            mileage = 60000,
            cost = 200.0,
            notes = "Front pads"
        )
        every { maintenanceDao.getAllLogs("car1") } returns flowOf(listOf(entity))

        // When
        val logs = maintenanceRepository.getAllLogs().first()

        // Then
        assertEquals(1, logs.size)
        val log = logs.first()
        assertEquals(entity.id, log.id)
        assertEquals(entity.serviceType, log.serviceType)
    }

    @Test
    fun `syncLogs fetches from api and inserts to dao`() = runTest(testDispatcher) {
        // Given
        val dto = MaintenanceLogDto(
            id = "1",
            serviceType = "Tires",
            dateMs = 3000L,
            mileage = 70000,
            cost = 400.0,
            notes = "New tires"
        )
        coEvery { apiService.fetchMaintenanceLogs() } returns listOf(dto)

        // When
        maintenanceRepository.syncLogs()

        // Then
        coVerify { apiService.fetchMaintenanceLogs() }
        coVerify {
            maintenanceDao.insertLogs(match { list ->
                list.size == 1 && list[0].id == dto.id
            })
        }
    }
}

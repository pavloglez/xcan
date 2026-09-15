package com.pavloglez.xcan.feature.dashboard

import app.cash.turbine.test
import com.pavloglez.xcan.core.bluetooth.BleDataSource
import com.pavloglez.xcan.core.bluetooth.ConnectionStatus
import com.pavloglez.xcan.core.data.UserPreferencesRepository
import com.pavloglez.xcan.core.data.CarRepository
import com.pavloglez.xcan.core.model.SensorRepository
import com.pavloglez.xcan.core.model.SensorScanStatus
import com.pavloglez.xcan.core.model.TelemetryFrame
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var bleDataSource: BleDataSource
    private lateinit var userPreferencesRepository: UserPreferencesRepository
    private lateinit var sensorRepository: SensorRepository
    private lateinit var carRepository: CarRepository
    private lateinit var viewModel: DashboardViewModel

    private val telemetryFlow = MutableStateFlow<TelemetryFrame>(TelemetryFrame("id", 0L, emptyMap()))
    private val connectionStateFlow = MutableStateFlow(ConnectionStatus.DISCONNECTED)
    private val sensorScanStatusFlow = MutableStateFlow(SensorScanStatus.IDLE)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        bleDataSource = mockk(relaxed = true)
        userPreferencesRepository = mockk(relaxed = true)
        sensorRepository = mockk(relaxed = true)
        carRepository = mockk(relaxed = true)

        every { bleDataSource.telemetry } returns telemetryFlow
        every { bleDataSource.connectionState } returns connectionStateFlow
        every { bleDataSource.sensorScanStatus } returns sensorScanStatusFlow
        every { userPreferencesRepository.useMetric } returns flowOf(false)
        every { userPreferencesRepository.getSelectedSensors(any()) } returns flowOf(emptySet())
        every { userPreferencesRepository.getSupportedPids(any()) } returns flowOf(null)
        every { sensorRepository.getSensors() } returns flowOf(emptyList())
        every { carRepository.getActiveCar() } returns flowOf(null)

        viewModel = DashboardViewModel(bleDataSource, userPreferencesRepository, sensorRepository, carRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state emits default telemetry`() = runTest(testDispatcher) {
        viewModel.state.test {
            val initialState = awaitItem()
            assertNull(initialState.telemetry)
            val combinedState = awaitItem()
            assertEquals(emptyMap<String, Float>(), combinedState.telemetry?.sensors)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `emits telemetry frame`() = runTest(testDispatcher) {
        viewModel.state.test {
            awaitItem() // initial

            val telemetry = TelemetryFrame(
                id = "id",
                timestampMs = 123L,
                sensors = mapOf(
                    "010D" to 100f
                )
            )
            telemetryFlow.value = telemetry

            val telemetryState = awaitItem()
            assertEquals(telemetry, telemetryState.telemetry)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `scanSensors updates supportedSensors from BleDataSource when cache is empty`() = runTest(testDispatcher) {
        val sensorRPM = com.pavloglez.xcan.core.model.ObdSensor(
            pid = "010C",
            displayName = "Engine RPM",
            unit = "RPM",
            expectedBytes = 2,
            formula = ""
        )
        io.mockk.coEvery { bleDataSource.getSupportedSensors() } returns listOf(sensorRPM)

        viewModel.state.test {
            awaitItem() // initial
            val state1 = awaitItem()
            assertEquals(emptyList<com.pavloglez.xcan.core.model.ObdSensor>(), state1.supportedSensors)

            viewModel.scanSensors(forceRescan = true)
            testScheduler.advanceUntilIdle()

            val scannedState = awaitItem()
            assertEquals(listOf(sensorRPM), scannedState.supportedSensors)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `scanSensors loads from cache when cachedPids is present and not forceRescan`() = runTest(testDispatcher) {
        val carId = "car-123"
        val activeCar = com.pavloglez.xcan.core.model.CarProfile(
            id = carId,
            name = "Test Car",
            make = "Mazda",
            model = "Miata",
            year = 2020,
            isActive = true
        )
        val sensorSpeed = com.pavloglez.xcan.core.model.ObdSensor(
            pid = "010D",
            displayName = "Vehicle Speed",
            unit = "KM/H",
            expectedBytes = 1,
            formula = ""
        )

        every { carRepository.getActiveCar() } returns flowOf(activeCar)
        every { userPreferencesRepository.getSupportedPids(carId) } returns flowOf(setOf("010D"))
        every { sensorRepository.getSensors() } returns flowOf(listOf(sensorSpeed))

        viewModel.state.test {
            awaitItem() // initial default state
            viewModel.scanSensors(forceRescan = false)
            testScheduler.advanceUntilIdle()

            val current = awaitItem()
            assertEquals(listOf(sensorSpeed), current.supportedSensors)
            cancelAndIgnoreRemainingEvents()
        }
    }
}

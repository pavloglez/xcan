package com.jpdgbv.xcan.feature.dashboard

import app.cash.turbine.test
import com.jpdgbv.xcan.core.bluetooth.BleDataSource
import com.jpdgbv.xcan.core.bluetooth.ConnectionStatus
import com.jpdgbv.xcan.core.data.CarRepository
import com.jpdgbv.xcan.core.data.UserPreferencesRepository
import com.jpdgbv.xcan.core.model.CarProfile
import com.jpdgbv.xcan.core.model.TelemetryFrame
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
    private lateinit var carRepository: CarRepository
    private lateinit var userPreferencesRepository: UserPreferencesRepository
    private lateinit var viewModel: DashboardViewModel

    private val connectionStateFlow = MutableStateFlow(ConnectionStatus.CONNECTING)
    private val telemetryFlow = MutableStateFlow<TelemetryFrame>(
        TelemetryFrame("0", 0L, emptyMap())
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        bleDataSource = mockk(relaxed = true)
        carRepository = mockk(relaxed = true)
        userPreferencesRepository = mockk(relaxed = true)

        every { bleDataSource.connectionState } returns connectionStateFlow
        every { bleDataSource.telemetry } returns telemetryFlow
        every { bleDataSource.connectionLogs } returns flowOf()
        every { carRepository.getAllCars() } returns flowOf(emptyList())
        every { carRepository.getActiveCar() } returns flowOf(null)
        every { userPreferencesRepository.useMetric } returns flowOf(false)
        every { userPreferencesRepository.selectedSensors } returns flowOf(emptySet())

        viewModel = DashboardViewModel(bleDataSource, carRepository, userPreferencesRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is CONNECTING with default telemetry`() = runTest(testDispatcher) {
        viewModel.state.test {
            val initialState = awaitItem()
            assertEquals(ConnectionStatus.DISCONNECTED, initialState.connectionStatus)
            
            val connectingState = awaitItem()
            assertEquals(ConnectionStatus.CONNECTING, connectingState.connectionStatus)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `emits telemetry when CONNECTED`() = runTest(testDispatcher) {
        viewModel.state.test {
            awaitItem() // initial DISCONNECTED
            awaitItem() // CONNECTING from mock

            connectionStateFlow.value = ConnectionStatus.CONNECTED
            
            // Should emit CONNECTED with default telemetry
            val connectedState = awaitItem()
            assertEquals(ConnectionStatus.CONNECTED, connectedState.connectionStatus)
            assertEquals(telemetryFlow.value, connectedState.telemetry)

            val telemetry = TelemetryFrame(
                id = "id",
                timestampMs = 123L,
                sensors = mapOf(
                    "010D" to 100f,
                    "010C" to 3000f,
                    "0105" to 90f,
                    "0104" to 50f
                )
            )
            telemetryFlow.value = telemetry

            val telemetryState = awaitItem()
            assertEquals(telemetry, telemetryState.telemetry)
            assertEquals(ConnectionStatus.CONNECTED, telemetryState.connectionStatus)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `handles DISCONNECTED state`() = runTest(testDispatcher) {
        viewModel.state.test {
            awaitItem() // initial DISCONNECTED
            awaitItem() // CONNECTING from mock

            connectionStateFlow.value = ConnectionStatus.CONNECTED
            awaitItem() // CONNECTED

            connectionStateFlow.value = ConnectionStatus.DISCONNECTED
            
            val disconnectedState = awaitItem()
            assertEquals(ConnectionStatus.DISCONNECTED, disconnectedState.connectionStatus)
            assertNull(disconnectedState.telemetry)
            cancelAndIgnoreRemainingEvents()
        }
    }
}

package com.pavloglez.xcan.feature.dashboard

import app.cash.turbine.test
import com.pavloglez.xcan.core.bluetooth.BleDataSource
import com.pavloglez.xcan.core.bluetooth.ConnectionStatus
import com.pavloglez.xcan.core.data.UserPreferencesRepository
import com.pavloglez.xcan.core.model.SensorRepository
import com.pavloglez.xcan.core.model.TelemetryFrame
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
    private lateinit var viewModel: DashboardViewModel

    private val telemetryFlow = MutableStateFlow<TelemetryFrame>(TelemetryFrame("id", 0L, emptyMap()))
    private val connectionStateFlow = MutableStateFlow(ConnectionStatus.DISCONNECTED)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        bleDataSource = mockk(relaxed = true)
        userPreferencesRepository = mockk(relaxed = true)
        sensorRepository = mockk(relaxed = true)

        every { bleDataSource.telemetry } returns telemetryFlow
        every { bleDataSource.connectionState } returns connectionStateFlow
        every { userPreferencesRepository.useMetric } returns flowOf(false)
        every { userPreferencesRepository.selectedSensors } returns flowOf(emptySet())
        every { sensorRepository.getSensors() } returns flowOf(emptyList())

        viewModel = DashboardViewModel(bleDataSource, userPreferencesRepository, sensorRepository)
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
}

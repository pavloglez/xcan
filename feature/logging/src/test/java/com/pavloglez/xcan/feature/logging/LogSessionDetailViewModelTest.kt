package com.pavloglez.xcan.feature.logging

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.pavloglez.xcan.core.data.LoggingRepository
import com.pavloglez.xcan.core.model.LogEntry
import com.pavloglez.xcan.core.model.LogSession
import com.pavloglez.xcan.core.model.ObdSensor
import com.pavloglez.xcan.core.model.SensorRepository
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
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LogSessionDetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val loggingRepository: LoggingRepository = mockk(relaxed = true)
    private val sensorRepository: SensorRepository = mockk(relaxed = true)

    private val testSessionId = "session-123"
    private val sampleSession = LogSession(
        id = testSessionId,
        carId = "car-1",
        carLabel = "2020 Honda Civic",
        startTimeMs = 1000L,
        endTimeMs = 5000L
    )

    private val standardSensors = listOf(
        ObdSensor(pid = "010C", displayName = "Engine RPM", unit = "RPM", expectedBytes = 2, formula = ""),
        ObdSensor(pid = "010D", displayName = "Vehicle Speed", unit = "KM/H", expectedBytes = 1, formula = "")
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `resolves known sensor names and units from repository`() = runTest {
        val entries = listOf(
            LogEntry(id = 1, sessionId = testSessionId, timestampMs = 1000L, pid = "010C", value = 800f),
            LogEntry(id = 2, sessionId = testSessionId, timestampMs = 2000L, pid = "010C", value = 1200f),
            LogEntry(id = 3, sessionId = testSessionId, timestampMs = 1000L, pid = "010D", value = 50f)
        )

        every { loggingRepository.getSessionById(testSessionId) } returns flowOf(sampleSession)
        every { loggingRepository.getEntriesForSession(testSessionId) } returns flowOf(entries)
        every { sensorRepository.getSensors() } returns flowOf(standardSensors)

        val savedStateHandle = SavedStateHandle(mapOf("sessionId" to testSessionId))
        val viewModel = LogSessionDetailViewModel(savedStateHandle, loggingRepository, sensorRepository)

        viewModel.state.test {
            testDispatcher.scheduler.advanceUntilIdle()
            val state = expectMostRecentItem()

            assertFalse(state.isLoading)
            assertEquals(sampleSession, state.session)
            assertEquals(2, state.series.size)

            val rpmSeries = state.series.first { it.pid == "010C" }
            assertEquals("Engine RPM", rpmSeries.displayName)
            assertEquals("RPM", rpmSeries.unit)
            assertEquals(2, rpmSeries.entries.size)

            val speedSeries = state.series.first { it.pid == "010D" }
            assertEquals("Vehicle Speed", speedSeries.displayName)
            assertEquals("KM/H", speedSeries.unit)
            assertEquals(1, speedSeries.entries.size)
        }
    }

    @Test
    fun `falls back gracefully for unknown PIDs`() = runTest {
        val unknownPid = "0199"
        val entries = listOf(
            LogEntry(id = 1, sessionId = testSessionId, timestampMs = 1000L, pid = unknownPid, value = 42f)
        )

        every { loggingRepository.getSessionById(testSessionId) } returns flowOf(sampleSession)
        every { loggingRepository.getEntriesForSession(testSessionId) } returns flowOf(entries)
        every { sensorRepository.getSensors() } returns flowOf(standardSensors)
        every { sensorRepository.getSensorByPidSync(unknownPid) } returns ObdSensor(
            pid = unknownPid,
            displayName = "Unknown PID $unknownPid",
            unit = "Raw",
            expectedBytes = -1,
            formula = ""
        )

        val savedStateHandle = SavedStateHandle(mapOf("sessionId" to testSessionId))
        val viewModel = LogSessionDetailViewModel(savedStateHandle, loggingRepository, sensorRepository)

        viewModel.state.test {
            testDispatcher.scheduler.advanceUntilIdle()
            val state = expectMostRecentItem()

            assertFalse(state.isLoading)
            assertEquals(1, state.series.size)

            val unknownSeries = state.series.first()
            assertEquals("Unknown PID 0199", unknownSeries.displayName)
            assertEquals("Raw", unknownSeries.unit)
            assertEquals(unknownPid, unknownSeries.pid)
            assertEquals(42f, unknownSeries.entries.first().value)
        }
    }

    @Test
    fun `emits empty series when no log entries exist`() = runTest {
        every { loggingRepository.getSessionById(testSessionId) } returns flowOf(sampleSession)
        every { loggingRepository.getEntriesForSession(testSessionId) } returns flowOf(emptyList())
        every { sensorRepository.getSensors() } returns flowOf(standardSensors)

        val savedStateHandle = SavedStateHandle(mapOf("sessionId" to testSessionId))
        val viewModel = LogSessionDetailViewModel(savedStateHandle, loggingRepository, sensorRepository)

        viewModel.state.test {
            testDispatcher.scheduler.advanceUntilIdle()
            val state = expectMostRecentItem()

            assertFalse(state.isLoading)
            assertEquals(sampleSession, state.session)
            assertEquals(0, state.series.size)
            assertEquals(0, state.entriesByPid.size)
        }
    }
}

package com.pavloglez.xcan.feature.logging

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import com.pavloglez.xcan.core.model.LogEntry
import com.pavloglez.xcan.core.model.LogSession
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w1000dp-h1000dp")
class LogSessionDetailScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val sampleSession = LogSession(
        id = "session-1",
        carId = "car-1",
        carLabel = "2020 Honda Civic",
        startTimeMs = 1700000000000L,
        endTimeMs = 1700000120000L
    )

    @Test
    fun `shows empty message when no telemetry data recorded`() {
        composeTestRule.setContent {
            LogSessionDetailScreen(
                state = LogSessionDetailState(
                    session = sampleSession,
                    series = emptyList(),
                    isLoading = false
                )
            )
        }

        composeTestRule.onNodeWithText("No data recorded in this session").assertIsDisplayed()
    }

    @Test
    fun `displays human readable sensor name, pid, and units in stat chips`() {
        val series = listOf(
            SensorMetricSeries(
                pid = "010C",
                displayName = "Engine RPM",
                unit = "RPM",
                entries = listOf(
                    LogEntry(id = 1, sessionId = "session-1", timestampMs = 1000L, pid = "010C", value = 800f),
                    LogEntry(id = 2, sessionId = "session-1", timestampMs = 2000L, pid = "010C", value = 1200f)
                )
            ),
            SensorMetricSeries(
                pid = "010D",
                displayName = "Vehicle Speed",
                unit = "KM/H",
                entries = listOf(
                    LogEntry(id = 3, sessionId = "session-1", timestampMs = 1000L, pid = "010D", value = 65f)
                )
            )
        )

        composeTestRule.setContent {
            LogSessionDetailScreen(
                state = LogSessionDetailState(
                    session = sampleSession,
                    series = series,
                    isLoading = false
                )
            )
        }

        // Verify human-readable names are displayed
        composeTestRule.onNodeWithText("Engine RPM").assertIsDisplayed()
        composeTestRule.onNodeWithText("Vehicle Speed").assertIsDisplayed()

        // Verify PID badges are displayed
        composeTestRule.onNodeWithText("010C").assertIsDisplayed()
        composeTestRule.onNodeWithText("010D").assertIsDisplayed()

        // Verify point counts
        composeTestRule.onNodeWithText("2 pts").assertIsDisplayed()
        composeTestRule.onNodeWithText("1 pts").assertIsDisplayed()

        // Verify values with units (Min, Avg, Max for RPM)
        composeTestRule.onNodeWithText("800.0 RPM").assertIsDisplayed()
        composeTestRule.onNodeWithText("1000.0 RPM").assertIsDisplayed()
        composeTestRule.onNodeWithText("1200.0 RPM").assertIsDisplayed()

        // Verify values with units for Speed (all 3 chips have 65.0 KM/H)
        composeTestRule.onAllNodesWithText("65.0 KM/H").onFirst().assertIsDisplayed()
    }
}

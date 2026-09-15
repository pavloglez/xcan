package com.pavloglez.xcan.feature.dashboard.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.pavloglez.xcan.core.model.ObdSensor
import com.pavloglez.xcan.core.model.SensorScanStatus
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalMaterial3Api::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w1000dp-h1000dp")
class DashboardConfigBottomSheetTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val sampleSensors = listOf(
        ObdSensor(pid = "010C", displayName = "Engine RPM", unit = "RPM", expectedBytes = 2, formula = ""),
        ObdSensor(pid = "010D", displayName = "Vehicle Speed", unit = "KM/H", expectedBytes = 1, formula = "")
    )

    @Test
    fun `shows idle prompt when disconnected`() {
        composeTestRule.setContent {
            DashboardConfigSheetContent(
                allSensors = sampleSensors,
                selectedPids = emptySet(),
                sensorScanStatus = SensorScanStatus.IDLE,
                isConnected = false,
                onToggleSensor = { _, _ -> },
                onRescan = { }
            )
        }

        composeTestRule.onNodeWithText("Connect to a vehicle to scan available ECU sensors").assertIsDisplayed()
    }

    @Test
    fun `shows scanning message when scanning`() {
        composeTestRule.setContent {
            DashboardConfigSheetContent(
                allSensors = sampleSensors,
                selectedPids = emptySet(),
                sensorScanStatus = SensorScanStatus.SCANNING,
                isConnected = true,
                onToggleSensor = { _, _ -> },
                onRescan = { }
            )
        }

        composeTestRule.onNodeWithText("Scanning ECU for supported sensors…").assertIsDisplayed()
    }

    @Test
    fun `shows completed count and triggers rescan`() {
        var rescanClicked = false
        composeTestRule.setContent {
            DashboardConfigSheetContent(
                allSensors = sampleSensors,
                selectedPids = setOf("010C"),
                sensorScanStatus = SensorScanStatus.COMPLETED,
                isConnected = true,
                onToggleSensor = { _, _ -> },
                onRescan = { rescanClicked = true }
            )
        }

        composeTestRule.onNodeWithText("Found 2 sensors supported by this vehicle").assertIsDisplayed()
        composeTestRule.onNodeWithText("Rescan").assertIsDisplayed()
        composeTestRule.onNodeWithText("Rescan").performClick()
        assertTrue(rescanClicked)
    }

    @Test
    fun `shows failed message when scan failed`() {
        composeTestRule.setContent {
            DashboardConfigSheetContent(
                allSensors = sampleSensors,
                selectedPids = emptySet(),
                sensorScanStatus = SensorScanStatus.FAILED,
                isConnected = true,
                onToggleSensor = { _, _ -> },
                onRescan = { }
            )
        }

        composeTestRule.onNodeWithText("Could not read ECU capabilities · Showing known sensors").assertIsDisplayed()
    }
}

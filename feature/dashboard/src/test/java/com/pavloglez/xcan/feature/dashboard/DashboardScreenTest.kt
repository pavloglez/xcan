package com.pavloglez.xcan.feature.dashboard

import com.pavloglez.xcan.feature.dashboard.presentation.DashboardUIState

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.pavloglez.xcan.core.bluetooth.ConnectionStatus
import com.pavloglez.xcan.core.model.ObdSensor
import com.pavloglez.xcan.core.model.TelemetryFrame
import com.pavloglez.xcan.core.ui.theme.ElectricBlue
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w1000dp-h1000dp")
class DashboardScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun showsConnectOBDIIButtonWhenDisconnected() {
        var connectClicked = false
        composeTestRule.setContent {
            DashboardScreen(
                state = DashboardUIState(connectionStatus = ConnectionStatus.DISCONNECTED),
                onConnect = { connectClicked = true },
                onDisconnect = { },
                onShowCarSelect = { },
                onShowLogs = { },
                onShowConfig = { },
                onStartLog = { },
                onPauseLog = { },
                onResumeLog = { },
                onStopLog = { },
                onToggleTrackMode = { }
            )
        }

        composeTestRule.onNodeWithText("Connect OBD-II").assertIsDisplayed()
        composeTestRule.onNodeWithText("Connect OBD-II").performClick()
        assertTrue(connectClicked)
    }

    @Test
    fun showsConnectingButtonWhenConnecting() {
        composeTestRule.setContent {
            DashboardScreen(
                state = DashboardUIState(connectionStatus = ConnectionStatus.CONNECTING),
                onConnect = { },
                onDisconnect = { },
                onShowCarSelect = { },
                onShowLogs = { },
                onShowConfig = { },
                onStartLog = { },
                onPauseLog = { },
                onResumeLog = { },
                onStopLog = { },
                onToggleTrackMode = { }
            )
        }

        composeTestRule.onNodeWithText("Connecting…").assertIsDisplayed()
        composeTestRule.onNodeWithText("Connecting…").assertIsNotEnabled()
    }

    @Test
    fun showsDisconnectAndDialsWhenConnected() {
        var disconnectClicked = false
        val telemetryFrame = TelemetryFrame("id", 0L, mapOf("010C" to 1000f, "010D" to 50f))

        composeTestRule.setContent {
            DashboardScreen(
                state = DashboardUIState(
                    connectionStatus = ConnectionStatus.CONNECTED,
                    telemetry = telemetryFrame,
                    useMetric = true,
                    selectedSensors = setOf("010C", "010D"),
                    allKnownSensors = listOf(
                        ObdSensor(pid = "010C", displayName = "RPM", unit = "rpm", expectedBytes = 2, formula = ""),
                        ObdSensor(pid = "010D", displayName = "Speed", unit = "km/h", expectedBytes = 1, formula = "")
                    )
                ),
                onConnect = { },
                onDisconnect = { disconnectClicked = true },
                onShowCarSelect = { },
                onShowLogs = { },
                onShowConfig = { },
                onStartLog = { },
                onPauseLog = { },
                onResumeLog = { },
                onStopLog = { },
                onToggleTrackMode = { }
            )
        }

        composeTestRule.onNodeWithText("Disconnect").assertIsDisplayed()
        composeTestRule.onNodeWithText("Disconnect").performClick()
        assertTrue(disconnectClicked)

        composeTestRule.onNodeWithText("RPM").assertIsDisplayed()
        composeTestRule.onNodeWithText("1000").assertIsDisplayed()
        
        composeTestRule.onNodeWithText("Speed").assertIsDisplayed()
        composeTestRule.onNodeWithText("50").assertIsDisplayed()
    }

    @Test
    fun telemetryDial_displaysTitleValueAndLabelAcrossRotations() {
        var currentAngle by mutableFloatStateOf(0f)
        composeTestRule.setContent {
            TelemetryDial(
                title = "Engine RPM",
                value = 4200f,
                maxValue = 8000f,
                label = "rpm",
                color = ElectricBlue,
                rotationAngle = currentAngle
            )
        }

        val testAngles = listOf(0f, -90f, 90f, 180f)
        for (angle in testAngles) {
            currentAngle = angle
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithText("Engine RPM").assertIsDisplayed()
            composeTestRule.onNodeWithText("4200").assertIsDisplayed()
            composeTestRule.onNodeWithText("rpm").assertIsDisplayed()
        }
    }
}

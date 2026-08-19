package com.jpdgbv.xcan.feature.dashboard

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.jpdgbv.xcan.core.bluetooth.ConnectionStatus
import com.jpdgbv.xcan.core.model.TelemetryFrame
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class DashboardScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `shows Connect OBD-II button when disconnected`() {
        var connectClicked = false
        composeTestRule.setContent {
            DashboardScreen(
                state = DashboardState(connectionStatus = ConnectionStatus.DISCONNECTED),
                onConnect = { connectClicked = true },
                onDisconnect = { },
                onShowCarSelect = { },
                onShowLogs = { },
                onShowConfig = { }
            )
        }

        composeTestRule.onNodeWithText("Connect OBD-II").assertIsDisplayed()
        composeTestRule.onNodeWithText("Connect OBD-II").performClick()
        assertTrue(connectClicked)
    }

    @Test
    fun `shows Connecting button when connecting`() {
        composeTestRule.setContent {
            DashboardScreen(
                state = DashboardState(
                    connectionStatus = ConnectionStatus.CONNECTING,
                    useMetric = true
                ),
                onConnect = {},
                onDisconnect = {},
                onShowCarSelect = {},
                onShowLogs = {},
                onShowConfig = { }
            )
        }

        composeTestRule.onNodeWithText("Connecting...").assertIsDisplayed()
        composeTestRule.onNodeWithText("Connecting...").assertIsNotEnabled()
    }

    @Test
    fun `shows Disconnect and Dials when connected`() {
        var disconnectClicked = false

        composeTestRule.setContent {
            DashboardScreen(
                state = DashboardState(
                    connectionStatus = ConnectionStatus.CONNECTED,
                    telemetry = TelemetryFrame(
                        id = "id", 
                        timestampMs = 0L, 
                        sensors = mapOf(
                            "010C" to 1000f,
                            "010D" to 50f,
                            "0104" to 40f,
                            "0105" to 90f
                        )
                    ),
                    useMetric = true,
                    selectedSensors = setOf("010C", "010D")
                ),
                onConnect = {},
                onDisconnect = { disconnectClicked = true },
                onShowCarSelect = { },
                onShowLogs = { },
                onShowConfig = { }
            )
        }

        composeTestRule.onNodeWithText("Disconnect").assertIsDisplayed()
        composeTestRule.onNodeWithText("Disconnect").performClick()
        assertTrue(disconnectClicked)

        composeTestRule.onNodeWithText("RPM").assertIsDisplayed()
        composeTestRule.onNodeWithText("1000").assertIsDisplayed()
        
        composeTestRule.onNodeWithText("km/h").assertIsDisplayed()
        composeTestRule.onNodeWithText("50").assertIsDisplayed()
    }
}

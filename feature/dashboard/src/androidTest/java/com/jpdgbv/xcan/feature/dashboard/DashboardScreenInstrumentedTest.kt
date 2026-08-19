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
import androidx.test.ext.junit.runners.AndroidJUnit4

@RunWith(AndroidJUnit4::class)
class DashboardScreenInstrumentedTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `shows Connect OBD-II button when disconnected`() {
        var connectClicked = false
        composeTestRule.setContent {
            DashboardScreen(
                state = DashboardState(connectionStatus = ConnectionStatus.DISCONNECTED),
                onConnect = { connectClicked = true },
                onDisconnect = { }
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
                state = DashboardState(connectionStatus = ConnectionStatus.CONNECTING),
                onConnect = { },
                onDisconnect = { }
            )
        }

        composeTestRule.onNodeWithText("Connecting...").assertIsDisplayed()
        composeTestRule.onNodeWithText("Connecting...").assertIsNotEnabled()
    }

    @Test
    fun `shows Disconnect and Dials when connected`() {
        var disconnectClicked = false
        val telemetryFrame = TelemetryFrame("id", 0L, 3500, 120, 50f, 90)

        composeTestRule.setContent {
            DashboardScreen(
                state = DashboardState(
                    connectionStatus = ConnectionStatus.CONNECTED,
                    telemetry = telemetryFrame
                ),
                onConnect = { },
                onDisconnect = { disconnectClicked = true }
            )
        }

        composeTestRule.onNodeWithText("Disconnect").assertIsDisplayed()
        composeTestRule.onNodeWithText("Disconnect").performClick()
        assertTrue(disconnectClicked)

        composeTestRule.onNodeWithText("RPM").assertIsDisplayed()
        composeTestRule.onNodeWithText("3500").assertIsDisplayed()
        
        composeTestRule.onNodeWithText("KM/H").assertIsDisplayed()
        composeTestRule.onNodeWithText("120").assertIsDisplayed()
    }
}

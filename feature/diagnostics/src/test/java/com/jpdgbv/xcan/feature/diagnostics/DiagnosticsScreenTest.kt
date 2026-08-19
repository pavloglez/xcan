package com.jpdgbv.xcan.feature.diagnostics

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.jpdgbv.xcan.core.bluetooth.ConnectionStatus
import com.jpdgbv.xcan.core.model.DiagnosticTroubleCode
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class DiagnosticsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `shows connect message when disconnected`() {
        composeTestRule.setContent {
            DiagnosticsScreen(
                state = DiagnosticsState(connectionStatus = ConnectionStatus.DISCONNECTED),
                onScanClicked = {},
                onClearClicked = {}
            )
        }

        composeTestRule.onNodeWithText("Please connect to the OBD2 adapter to run a scan.").assertIsDisplayed()
    }

    @Test
    fun `shows scan button when connected`() {
        var clicked = false
        composeTestRule.setContent {
            DiagnosticsScreen(
                state = DiagnosticsState(connectionStatus = ConnectionStatus.CONNECTED),
                onScanClicked = { clicked = true },
                onClearClicked = {}
            )
        }

        composeTestRule.onNodeWithText("Scan for Faults").assertIsDisplayed()
        composeTestRule.onNodeWithText("Scan for Faults").performClick()
        assertTrue(clicked)
    }

    @Test
    fun `shows scanning loading indicator`() {
        composeTestRule.setContent {
            DiagnosticsScreen(
                state = DiagnosticsState(
                    connectionStatus = ConnectionStatus.CONNECTED,
                    scanStatus = ScanStatus.SCANNING
                ),
                onScanClicked = {},
                onClearClicked = {}
            )
        }

        composeTestRule.onNodeWithText("Scanning...").assertIsDisplayed()
        composeTestRule.onNodeWithText("Scanning...").assertIsNotEnabled() // Button should be disabled
    }

    @Test
    fun `shows healthy message when no faults`() {
        composeTestRule.setContent {
            DiagnosticsScreen(
                state = DiagnosticsState(
                    connectionStatus = ConnectionStatus.CONNECTED,
                    scanStatus = ScanStatus.SUCCESS,
                    faultCodes = emptyList()
                ),
                onScanClicked = {},
                onClearClicked = {}
            )
        }

        composeTestRule.onNodeWithText("No fault codes detected. Your vehicle is healthy!").assertIsDisplayed()
    }

    @Test
    fun `shows fault codes when success`() {
        val code = DiagnosticTroubleCode("P0300", "Random/Multiple Cylinder Misfire Detected")
        composeTestRule.setContent {
            DiagnosticsScreen(
                state = DiagnosticsState(
                    connectionStatus = ConnectionStatus.CONNECTED,
                    scanStatus = ScanStatus.SUCCESS,
                    faultCodes = listOf(code)
                ),
                onScanClicked = {},
                onClearClicked = {}
            )
        }

        composeTestRule.onNodeWithText("P0300").assertIsDisplayed()
        composeTestRule.onNodeWithText("Random/Multiple Cylinder Misfire Detected").assertIsDisplayed()
        composeTestRule.onNodeWithText("STORED").assertIsDisplayed()
    }
}

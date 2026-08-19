package com.jpdgbv.xcan.feature.config

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4

@RunWith(AndroidJUnit4::class)
class ConfigScreenInstrumentedTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `shows config screen correctly with switch off`() {
        composeTestRule.setContent {
            ConfigScreen(
                state = ConfigState(useMetric = false),
                onToggleMetric = {}
            )
        }

        composeTestRule.onNodeWithText("ECU Configuration").assertIsDisplayed()
        composeTestRule.onNodeWithText("Use Metric Units").assertIsDisplayed()
        
        // Find the switch by clicking on it
        // Actually, we can check the Switch state by finding it as a Toggleable
        composeTestRule.onNode(androidx.compose.ui.test.isToggleable()).assertIsOff()
    }

    @Test
    fun `shows config screen correctly with switch on`() {
        composeTestRule.setContent {
            ConfigScreen(
                state = ConfigState(useMetric = true),
                onToggleMetric = {}
            )
        }

        composeTestRule.onNode(androidx.compose.ui.test.isToggleable()).assertIsOn()
    }

    @Test
    fun `clicking switch invokes callback`() {
        var callbackValue = false
        composeTestRule.setContent {
            ConfigScreen(
                state = ConfigState(useMetric = false),
                onToggleMetric = { callbackValue = it }
            )
        }

        composeTestRule.onNode(androidx.compose.ui.test.isToggleable()).performClick()
        assertTrue(callbackValue)
    }
}

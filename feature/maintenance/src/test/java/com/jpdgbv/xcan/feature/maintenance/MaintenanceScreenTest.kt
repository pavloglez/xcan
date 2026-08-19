package com.jpdgbv.xcan.feature.maintenance

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.jpdgbv.xcan.core.model.CarProfile
import com.jpdgbv.xcan.core.model.MaintenanceLog
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class MaintenanceScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `shows empty state when no logs`() {
        composeTestRule.setContent {
            MaintenanceScreen(
                state = MaintenanceState(logs = emptyList(), isLoading = false, activeCar = CarProfile("1", "Car 1", "Make", "Model", 2020, true)),
                onAddLog = { _, _, _, _ -> }
            )
        }

        composeTestRule.onNodeWithText("Maintenance History").assertIsDisplayed()
        composeTestRule.onNodeWithText("No logs found. Tap + to add.").assertIsDisplayed()
    }

    @Test
    fun `shows logs when available`() {
        val log = MaintenanceLog(
            id = "1",
            carId = "car1",
            dateMs = 1700000000000L,
            serviceType = "Oil Change",
            notes = "Replaced synthetic oil",
            mileage = 45000,
            cost = 50.0
        )
        composeTestRule.setContent {
            MaintenanceScreen(
                state = MaintenanceState(logs = listOf(log), isLoading = false, activeCar = CarProfile("1", "Car 1", "Make", "Model", 2020, true)),
                onAddLog = { _, _, _, _ -> }
            )
        }

        composeTestRule.onNodeWithText("Oil Change").assertIsDisplayed()
        composeTestRule.onNodeWithText("Replaced synthetic oil").assertIsDisplayed()
        composeTestRule.onNodeWithText("Mileage: 45000 mi").assertIsDisplayed()
    }

    @Test
    fun `fab click opens dialog and saves`() {
        var savedType = ""
        var savedNotes = ""
        var savedMileage = 0

        var savedDtc: String? = null

        composeTestRule.setContent {
            MaintenanceScreen(
                state = MaintenanceState(logs = emptyList(), isLoading = false, activeCar = CarProfile("1", "Car 1", "Make", "Model", 2020, true)),
                onAddLog = { type, notes, mileage, dtc ->
                    savedType = type
                    savedNotes = notes
                    savedMileage = mileage
                    savedDtc = dtc
                }
            )
        }

        // Open Dialog
        composeTestRule.onNodeWithContentDescription("Add Log").performClick()
        composeTestRule.onNodeWithText("Add Maintenance Log").assertIsDisplayed()

        // Input Data
        composeTestRule.onNodeWithText("Service Type").performTextInput("Brake Pad Replacement")
        composeTestRule.onNodeWithText("Notes").performTextInput("Front and rear pads")
        composeTestRule.onNodeWithText("Mileage (mi)").performTextInput("50000")

        // Save
        composeTestRule.onNodeWithText("Save").performClick()

        // Verify Callback
        assertTrue(savedType == "Brake Pad Replacement")
        assertTrue(savedNotes == "Front and rear pads")
        assertTrue(savedMileage == 50000)
        assertTrue(savedDtc == null)
    }
}

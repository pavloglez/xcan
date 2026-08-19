package com.jpdgbv.xcan.feature.maintenance

import androidx.compose.foundation.background
import com.jpdgbv.xcan.core.ui.components.pressBounce
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jpdgbv.xcan.core.model.MaintenanceLog
import com.jpdgbv.xcan.core.ui.theme.CharcoalSurface
import com.jpdgbv.xcan.core.ui.theme.DeepCharcoal
import com.jpdgbv.xcan.core.ui.theme.ElectricBlue
import com.jpdgbv.xcan.core.ui.theme.NeonAccent
import androidx.compose.ui.tooling.preview.Preview
import com.jpdgbv.xcan.core.ui.theme.XCanTheme
import com.jpdgbv.xcan.core.model.CarProfile
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MaintenanceRoute(
    viewModel: MaintenanceViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    MaintenanceScreen(
        state = state,
        onAddLog = { title, desc, miles, dtc ->
            viewModel.onIntent(MaintenanceIntent.AddLog(title, desc, miles, dtc))
        }
    )
}

@Composable
fun MaintenanceScreen(
    state: MaintenanceState,
    onAddLog: (String, String, Int, String?) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepCharcoal)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Maintenance History",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 24.dp)
            )

            if (state.activeCar != null) {
                Text(
                    text = state.activeCar.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = ElectricBlue,
                    modifier = Modifier.padding(bottom = 16.dp, top = 4.dp)
                )
            } else {
                Text(
                    text = "No Vehicle Selected",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 16.dp, top = 4.dp)
                )
            }

            if (state.activeCar == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Please select a vehicle on the Dashboard.", color = Color.Gray)
                }
            } else if (state.logs.isEmpty() && !state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No logs found. Tap + to add.", color = Color.Gray)
                }
            } else {
                LazyColumn {
                    items(state.logs.sortedByDescending { it.dateMs }) { log ->
                        TimelineMaintenanceLogItem(log = log, useMetric = state.useMetric)
                    }
                }
            }
        }

        if (state.activeCar != null) {
            val interactionSource = remember { MutableInteractionSource() }
            FloatingActionButton(
                onClick = { showDialog = true },
                interactionSource = interactionSource,
                containerColor = NeonAccent,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp)
                    .pressBounce(interactionSource)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Log", tint = DeepCharcoal)
            }
        }
        
        if (showDialog) {
            AddMaintenanceLogDialog(
                useMetric = state.useMetric,
                onDismiss = { showDialog = false },
                onConfirm = { type, notes, mileage, dtc ->
                    showDialog = false
                    onAddLog(type, notes, mileage, dtc)
                }
            )
        }
    }
}

@Composable
fun AddMaintenanceLogDialog(
    useMetric: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String, String, Int, String?) -> Unit
) {
    var serviceType by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var mileageStr by remember { mutableStateOf("") }
    var dtc by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Maintenance Log") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = serviceType,
                    onValueChange = { serviceType = it },
                    label = { Text("Service Type") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = dtc,
                    onValueChange = { dtc = it },
                    label = { Text("Related Fault Code (e.g. P0133)") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )
                val unitLabel = if (useMetric) "km" else "mi"
                OutlinedTextField(
                    value = mileageStr,
                    onValueChange = { mileageStr = it },
                    label = { Text("Mileage ($unitLabel)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val mileage = mileageStr.toIntOrNull() ?: 0
                    val finalDtc = dtc.trim().takeIf { it.isNotEmpty() }
                    onConfirm(serviceType, notes, mileage, finalDtc)
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun TimelineMaintenanceLogItem(log: MaintenanceLog, useMetric: Boolean) {
    val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val dateString = formatter.format(Date(log.dateMs))
    val isError = !log.relatedDtc.isNullOrEmpty()
    
    val iconTint = if (isError) MaterialTheme.colorScheme.error else ElectricBlue
    val iconVector = if (isError) Icons.Default.Warning else Icons.Default.Build

    Row(modifier = Modifier.fillMaxWidth()) {
        // Timeline Column
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(end = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(iconTint)
                    .padding(top = 24.dp)
            )
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .weight(1f, fill = false)
                    .height(150.dp) // Ensures a line connects down to the next item
                    .background(Color.Gray.copy(alpha = 0.3f))
            )
        }

        // Content Card
        Card(
            colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = iconVector,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = log.serviceType,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = dateString,
                    color = if (isError) MaterialTheme.colorScheme.error else NeonAccent,
                    style = MaterialTheme.typography.bodyMedium,
                )
                
                if (isError) {
                    Text(
                        text = "Fault Code: ${log.relatedDtc}",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Text(
                    text = log.notes,
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
                
                val unitLabel = if (useMetric) "km" else "mi"
                Text(
                    text = "Mileage: ${log.mileage} $unitLabel",
                    color = Color.LightGray,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MaintenanceScreenPreview() {
    XCanTheme {
        MaintenanceScreen(
            state = MaintenanceState(
                activeCar = CarProfile(id = "1", name = "My Civic", make = "Honda", model = "Civic", year = 2022, isActive = true),
                logs = listOf(
                    MaintenanceLog(id = "1", carId = "1", serviceType = "Oil Change & Filter", dateMs = 1715000000000, mileage = 55000, cost = 85.0, notes = "Full synthetic 0W-20"),
                    MaintenanceLog(id = "2", carId = "1", serviceType = "Engine Misfire Detected", dateMs = 1712000000000, mileage = 54000, cost = 120.0, notes = "Replaced ignition coil on cylinder 3", relatedDtc = "P0303"),
                    MaintenanceLog(id = "3", carId = "1", serviceType = "Tire Rotation", dateMs = 1705000000000, mileage = 50000, cost = 20.0, notes = "Rotated front to back and checked pressure"),
                    MaintenanceLog(id = "4", carId = "1", serviceType = "O2 Sensor Replacement", dateMs = 1698000000000, mileage = 48000, cost = 250.0, notes = "O2 Sensor Circuit Slow Response Bank 1 Sensor 1", relatedDtc = "P0133"),
                    MaintenanceLog(id = "5", carId = "1", serviceType = "Brake Pads", dateMs = 1690000000000, mileage = 45000, cost = 300.0, notes = "Ceramic brake pads installed on all four wheels")
                ),
                useMetric = false,
                isLoading = false
            ),
            onAddLog = { _, _, _, _ -> }
        )
    }
}

package com.jpdgbv.xcan.feature.dashboard.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jpdgbv.xcan.core.ui.components.bounceClick
import com.jpdgbv.xcan.core.model.ObdSensor
import com.jpdgbv.xcan.core.ui.components.pressBounce


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardConfigBottomSheet(
    allSensors: List<ObdSensor>,
    selectedPids: Set<String>,
    sheetState: SheetState,
    onToggleSensor: (String, Boolean) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier
    ) {
        Column(Modifier.padding(horizontal = 16.dp)) {
            Text(
                text = "Dashboard Widgets",
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Checked items appear on the dashboard and are logged automatically",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 13.sp
            )
            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

            LazyColumn {
                items(allSensors) { sensor ->
                    val isChecked = sensor.pid in selectedPids
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .bounceClick(onClick = { onToggleSensor(sensor.pid, !isChecked) })
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isChecked,
                            onCheckedChange = { onToggleSensor(sensor.pid, it) },
                            colors = CheckboxDefaults.colors(
                                checkedColor = MaterialTheme.colorScheme.primary,
                                uncheckedColor = MaterialTheme.colorScheme.onBackground
                            )
                        )
                        Column(Modifier.padding(start = 8.dp)) {
                            Text(
                                text = sensor.pid,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "${sensor.pid} · ${sensor.unit}",
                                color = MaterialTheme.colorScheme.onBackground,
                                fontSize = 11.sp
                            )
                        }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f))
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

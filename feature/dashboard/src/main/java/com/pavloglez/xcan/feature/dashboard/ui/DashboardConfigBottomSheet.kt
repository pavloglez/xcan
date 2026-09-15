package com.pavloglez.xcan.feature.dashboard.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pavloglez.xcan.core.ui.components.bounceClick
import com.pavloglez.xcan.core.model.ObdSensor
import com.pavloglez.xcan.core.model.SensorScanStatus
import androidx.compose.ui.res.stringResource
import com.pavloglez.xcan.core.ui.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardConfigBottomSheet(
    allSensors: List<ObdSensor>,
    selectedPids: Set<String>,
    sensorScanStatus: SensorScanStatus,
    isConnected: Boolean,
    sheetState: SheetState,
    onToggleSensor: (String, Boolean) -> Unit,
    onRescan: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier
    ) {
        DashboardConfigSheetContent(
            allSensors = allSensors,
            selectedPids = selectedPids,
            sensorScanStatus = sensorScanStatus,
            isConnected = isConnected,
            onToggleSensor = onToggleSensor,
            onRescan = onRescan
        )
    }
}

@Composable
fun DashboardConfigSheetContent(
    allSensors: List<ObdSensor>,
    selectedPids: Set<String>,
    sensorScanStatus: SensorScanStatus,
    isConnected: Boolean,
    onToggleSensor: (String, Boolean) -> Unit,
    onRescan: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier.padding(horizontal = 16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.sheet_title_dashboard_widgets),
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            if (isConnected && sensorScanStatus != SensorScanStatus.SCANNING) {
                TextButton(onClick = onRescan) {
                    Text(
                        text = stringResource(R.string.btn_rescan_sensors),
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
        Spacer(Modifier.height(4.dp))

        val subtitleText = when {
            !isConnected -> stringResource(R.string.sheet_subtitle_scan_idle)
            sensorScanStatus == SensorScanStatus.SCANNING -> stringResource(R.string.sheet_subtitle_scanning)
            sensorScanStatus == SensorScanStatus.COMPLETED -> stringResource(
                R.string.sheet_subtitle_scan_complete,
                allSensors.size
            )
            sensorScanStatus == SensorScanStatus.FAILED -> stringResource(R.string.sheet_subtitle_scan_failed)
            else -> stringResource(R.string.sheet_subtitle_dashboard_widgets)
        }

        Text(
            text = subtitleText,
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 13.sp
        )

        AnimatedVisibility(
            visible = sensorScanStatus == SensorScanStatus.SCANNING,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            )
        }

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

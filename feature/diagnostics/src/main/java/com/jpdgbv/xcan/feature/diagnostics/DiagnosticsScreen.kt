package com.jpdgbv.xcan.feature.diagnostics

import androidx.compose.foundation.background
import com.jpdgbv.xcan.core.ui.components.pressBounce
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jpdgbv.xcan.core.model.DiagnosticTroubleCode
import com.jpdgbv.xcan.core.model.DtcType
import com.jpdgbv.xcan.core.ui.theme.DeepCharcoal
import com.jpdgbv.xcan.core.ui.theme.ElectricBlue
import androidx.compose.ui.tooling.preview.Preview
import com.jpdgbv.xcan.core.ui.theme.XCanTheme

@Composable
fun DiagnosticsRoute(
    viewModel: DiagnosticsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    DiagnosticsScreen(
        state = state,
        onScanClicked = { viewModel.onIntent(DiagnosticsIntent.ScanFaultCodes) },
        onClearClicked = { viewModel.onIntent(DiagnosticsIntent.ClearFaultCodes) }
    )
}

@Composable
fun DiagnosticsScreen(
    state: DiagnosticsState,
    onScanClicked: () -> Unit,
    onClearClicked: () -> Unit
) {
    var showClearDialog by remember { mutableStateOf(false) }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Clear Fault Codes?") },
            text = { Text("This will erase all stored and pending fault codes and turn off the Check Engine Light. Are you sure you want to proceed?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearDialog = false
                        onClearClicked()
                    }
                ) {
                    Text("Clear Codes", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepCharcoal)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Vehicle Diagnostics",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 24.dp, top = 24.dp)
            )

            if (!state.isConnected) {
                Text(
                    text = "Please connect to the OBD2 adapter to run a scan.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                val scanInteraction = remember { MutableInteractionSource() }
                Button(
                    onClick = onScanClicked,
                    enabled = state.scanStatus != ScanStatus.SCANNING && state.scanStatus != ScanStatus.CLEARING,
                    interactionSource = scanInteraction,
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp).pressBounce(scanInteraction)
                ) {
                    if (state.scanStatus == ScanStatus.SCANNING) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Scanning...")
                    } else {
                        Text("Scan for Faults")
                    }
                }
                
                if (state.faultCodes.isNotEmpty()) {
                    val clearInteraction = remember { MutableInteractionSource() }
                    Button(
                        onClick = { showClearDialog = true },
                        enabled = state.scanStatus != ScanStatus.SCANNING && state.scanStatus != ScanStatus.CLEARING,
                        interactionSource = clearInteraction,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp).pressBounce(clearInteraction)
                    ) {
                        if (state.scanStatus == ScanStatus.CLEARING) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Clearing...")
                        } else {
                            Text("Clear Fault Codes")
                        }
                    }
                }
            }

            when (state.scanStatus) {
                ScanStatus.IDLE -> {
                    // Show nothing or a placeholder
                }
                ScanStatus.SCANNING -> {
                    // Handled in the button
                }
                ScanStatus.SUCCESS -> {
                    if (state.faultCodes.isEmpty()) {
                        Text(
                            text = "No fault codes detected. Your vehicle is healthy!",
                            color = Color.Green,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    } else {
                        FaultCodeList(faultCodes = state.faultCodes)
                    }
                }
                ScanStatus.ERROR -> {
                    Text(
                        text = "An error occurred. Please try again.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                ScanStatus.CLEARING -> {
                    // Handled in the clear button
                }
            }
        }
    }
}

@Composable
fun FaultCodeList(faultCodes: List<DiagnosticTroubleCode>) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(faultCodes) { code ->
            FaultCodeItem(code = code)
        }
    }
}

@Composable
fun FaultCodeItem(code: DiagnosticTroubleCode) {
    val tintColor = when (code.type) {
        DtcType.STORED -> MaterialTheme.colorScheme.error
        DtcType.PENDING -> Color(0xFFFFA500) // Orange
        DtcType.PERMANENT -> MaterialTheme.colorScheme.error
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = tintColor,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = code.code,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = tintColor
                )
                Text(
                    text = code.description ?: "Unknown Code",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = code.type.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DiagnosticsScreenPreview() {
    XCanTheme {
        DiagnosticsScreen(
            state = DiagnosticsState(
                connectionStatus = com.jpdgbv.xcan.core.bluetooth.ConnectionStatus.CONNECTED,
                scanStatus = ScanStatus.SUCCESS,
                faultCodes = listOf(
                    DiagnosticTroubleCode("P0133", "O2 Sensor Circuit Slow Response", DtcType.STORED)
                )
            ),
            onScanClicked = {},
            onClearClicked = {}
        )
    }
}

package com.pavloglez.xcan.feature.diagnostics

import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import com.pavloglez.xcan.core.ui.components.pressBounce
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
import com.pavloglez.xcan.core.ui.LocalHazeState
import dev.chrisbanes.haze.hazeSource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pavloglez.xcan.core.model.DiagnosticTroubleCode
import com.pavloglez.xcan.core.model.DtcType
import com.pavloglez.xcan.core.ui.components.GlassTopAppBar
import com.pavloglez.xcan.core.ui.theme.DeepCharcoal
import com.pavloglez.xcan.core.ui.theme.ElectricBlue
import androidx.compose.ui.tooling.preview.Preview
import com.pavloglez.xcan.core.ui.theme.XCanTheme
import androidx.compose.ui.res.stringResource
import com.pavloglez.xcan.core.ui.R

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
            title = { Text(stringResource(R.string.dialog_title_clear_fault_codes)) },
            text = { Text(stringResource(R.string.dialog_message_clear_fault_codes)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearDialog = false
                        onClearClicked()
                    }
                ) {
                    Text(stringResource(R.string.btn_clear_codes), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = DeepCharcoal,
        topBar = {
            GlassTopAppBar(title = stringResource(R.string.title_vehicle_diagnostics))
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DeepCharcoal)
                .hazeSource(LocalHazeState.current)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

            if (!state.isConnected) {
                Text(
                    text = stringResource(R.string.prompt_connect_adapter_to_scan),
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
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.btn_scanning))
                    } else {
                        Text(stringResource(R.string.btn_scan_faults))
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
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.btn_clearing))
                        } else {
                            Text(stringResource(R.string.btn_clear_fault_codes))
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
                            text = stringResource(R.string.msg_no_fault_codes_detected),
                            color = Color.Green,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    } else {
                        FaultCodeList(faultCodes = state.faultCodes)
                    }
                }
                ScanStatus.ERROR -> {
                    Text(
                        text = stringResource(R.string.error_diagnostics_scan_failed),
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
                    text = code.description ?: stringResource(R.string.dtc_unknown_description),
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
                connectionStatus = com.pavloglez.xcan.core.bluetooth.ConnectionStatus.CONNECTED,
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

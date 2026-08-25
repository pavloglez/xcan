package com.pavloglez.xcan.feature.diagnostics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pavloglez.xcan.core.bluetooth.BleDataSource
import com.pavloglez.xcan.core.bluetooth.ConnectionStatus
import com.pavloglez.xcan.core.model.DiagnosticTroubleCode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ScanStatus {
    IDLE,
    SCANNING,
    CLEARING,
    SUCCESS,
    ERROR
}

data class DiagnosticsState(
    val connectionStatus: ConnectionStatus = ConnectionStatus.DISCONNECTED,
    val scanStatus: ScanStatus = ScanStatus.IDLE,
    val faultCodes: List<DiagnosticTroubleCode> = emptyList()
) {
    val isConnected: Boolean get() = connectionStatus == ConnectionStatus.CONNECTED
}

sealed interface DiagnosticsIntent {
    object ScanFaultCodes : DiagnosticsIntent
    object ClearFaultCodes : DiagnosticsIntent
    object ClearResults : DiagnosticsIntent
}

@HiltViewModel
class DiagnosticsViewModel @Inject constructor(
    private val bleDataSource: BleDataSource
) : ViewModel() {

    private val _scanStatus = MutableStateFlow(ScanStatus.IDLE)
    private val _faultCodes = MutableStateFlow<List<DiagnosticTroubleCode>>(emptyList())

    val state: StateFlow<DiagnosticsState> = combine(
        bleDataSource.connectionState,
        _scanStatus,
        _faultCodes
    ) { status, scanStatus, codes ->
        DiagnosticsState(
            connectionStatus = status,
            scanStatus = scanStatus,
            faultCodes = codes
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DiagnosticsState()
    )

    fun onIntent(intent: DiagnosticsIntent) {
        when (intent) {
            DiagnosticsIntent.ScanFaultCodes -> {
                if (state.value.connectionStatus != ConnectionStatus.CONNECTED) return
                
                _scanStatus.value = ScanStatus.SCANNING
                viewModelScope.launch {
                    try {
                        val codes = bleDataSource.requestFaultCodes()
                        _faultCodes.value = codes
                        _scanStatus.value = ScanStatus.SUCCESS
                    } catch (e: Exception) {
                        e.printStackTrace()
                        _scanStatus.value = ScanStatus.ERROR
                    }
                }
            }
            DiagnosticsIntent.ClearFaultCodes -> {
                if (state.value.connectionStatus != ConnectionStatus.CONNECTED) return
                
                _scanStatus.value = ScanStatus.CLEARING
                viewModelScope.launch {
                    try {
                        val success = bleDataSource.clearFaultCodes()
                        if (success) {
                            _faultCodes.value = emptyList()
                            _scanStatus.value = ScanStatus.IDLE
                        } else {
                            _scanStatus.value = ScanStatus.ERROR
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        _scanStatus.value = ScanStatus.ERROR
                    }
                }
            }
            DiagnosticsIntent.ClearResults -> {
                _faultCodes.value = emptyList()
                _scanStatus.value = ScanStatus.IDLE
            }
        }
    }
}

package com.pavloglez.xcan.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pavloglez.xcan.core.bluetooth.BleDataSource
import com.pavloglez.xcan.core.bluetooth.ConnectionStatus
import com.pavloglez.xcan.core.bluetooth.ScannedDevice
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.pavloglez.xcan.core.model.ObdConstants

data class ConnectionState(
    val connectionStatus: ConnectionStatus = ConnectionStatus.DISCONNECTED,
    val connectionLogs: List<String> = emptyList(),
    val discoveredDevices: List<ScannedDevice> = emptyList(),
    val isScanning: Boolean = false
) {
    val isConnected: Boolean get() = connectionStatus == ConnectionStatus.CONNECTED
    val isConnecting: Boolean get() = connectionStatus == ConnectionStatus.CONNECTING
}

@HiltViewModel
class ConnectionViewModel @Inject constructor(
    private val bleDataSource: BleDataSource
) : ViewModel() {
    private val _isScanning = MutableStateFlow(false)
    private val _discoveredDevices = MutableStateFlow<List<ScannedDevice>>(emptyList())
    private var scanningJob: Job? = null
    private val _connectionLogs = MutableStateFlow<List<String>>(emptyList())

    val state = combine(
        bleDataSource.connectionState,
        _connectionLogs,
        _discoveredDevices,
        _isScanning
    ) { status, logs, devices, scanning ->
        ConnectionState(status, logs, devices, scanning)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(ObdConstants.STOP_TIMEOUT_MS), ConnectionState())

    init {
        viewModelScope.launch {
            bleDataSource.connectionLogs.collect { log ->
                val currentLogs = _connectionLogs.value.toMutableList()
                currentLogs.add(log)
                if (currentLogs.size > 100) currentLogs.removeAt(0)
                _connectionLogs.value = currentLogs
            }
        }
    }

    fun startScanning() {
        _isScanning.value = true
        scanningJob?.cancel()
        scanningJob = viewModelScope.launch {
            bleDataSource.scanForDevices().collect { devices ->
                _discoveredDevices.value = devices
            }
        }
    }

    fun stopScanning() {
        _isScanning.value = false
        scanningJob?.cancel()
    }

    fun connect(macAddress: String) {
        _isScanning.value = false
        scanningJob?.cancel()
        viewModelScope.launch { bleDataSource.connect(macAddress) }
    }

    fun disconnect() {
        _isScanning.value = false
        scanningJob?.cancel()
        viewModelScope.launch { bleDataSource.disconnect() }
    }
}

package com.pavloglez.xcan.core.bluetooth

import com.pavloglez.xcan.core.model.TelemetryFrame
import kotlinx.coroutines.flow.Flow

enum class ConnectionStatus {
    DISCONNECTED, CONNECTING, CONNECTED
}

data class ScannedDevice(
    val name: String,
    val macAddress: String
)

interface BleDataSource {
    val connectionState: Flow<ConnectionStatus>
    val telemetry: Flow<TelemetryFrame>
    val connectionLogs: Flow<String>
    fun scanForDevices(): Flow<List<ScannedDevice>>
    suspend fun connect(macAddress: String)
    suspend fun disconnect()
    suspend fun requestFaultCodes(): List<com.pavloglez.xcan.core.model.DiagnosticTroubleCode>
    suspend fun clearFaultCodes(): Boolean
    suspend fun getSupportedSensors(): List<com.pavloglez.xcan.core.model.ObdSensor>
    fun setPollingPids(pids: List<String>)
}

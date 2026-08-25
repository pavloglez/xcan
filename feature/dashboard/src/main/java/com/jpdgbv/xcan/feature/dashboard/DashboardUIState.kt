package com.jpdgbv.xcan.feature.dashboard

import com.jpdgbv.xcan.core.bluetooth.ConnectionStatus
import com.jpdgbv.xcan.core.bluetooth.ScannedDevice
import com.jpdgbv.xcan.core.data.LoggingState
import com.jpdgbv.xcan.core.model.CarProfile
import com.jpdgbv.xcan.core.model.ObdSensor
import com.jpdgbv.xcan.core.model.TelemetryFrame

data class DashboardUIState(
    val connectionStatus: ConnectionStatus = ConnectionStatus.DISCONNECTED,
    val telemetry: TelemetryFrame? = null,
    val connectionLogs: List<String> = emptyList(),
    val discoveredDevices: List<ScannedDevice> = emptyList(),
    val isScanning: Boolean = false,
    val cars: List<CarProfile> = emptyList(),
    val activeCar: CarProfile? = null,
    val useMetric: Boolean = false,
    val supportedSensors: List<ObdSensor> = emptyList(),
    val selectedSensors: Set<String> = emptySet(),
    val allKnownSensors: List<ObdSensor> = emptyList(),
    val loggingState: LoggingState = LoggingState.Idle,
    val isTrackMode: Boolean = false
) {
    val isConnected: Boolean get() = connectionStatus == ConnectionStatus.CONNECTED
    val isConnecting: Boolean get() = connectionStatus == ConnectionStatus.CONNECTING
}

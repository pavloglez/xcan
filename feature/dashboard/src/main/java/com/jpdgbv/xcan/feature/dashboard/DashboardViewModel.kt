package com.jpdgbv.xcan.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jpdgbv.xcan.core.bluetooth.BleDataSource
import com.jpdgbv.xcan.core.bluetooth.ConnectionStatus
import com.jpdgbv.xcan.core.data.UserPreferencesRepository
import com.jpdgbv.xcan.core.model.ObdSensor
import com.jpdgbv.xcan.core.model.SensorRepository
import com.jpdgbv.xcan.core.model.TelemetryFrame
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardState(
    val telemetry: TelemetryFrame? = null,
    val useMetric: Boolean = false,
    val supportedSensors: List<ObdSensor> = emptyList(),
    val selectedSensors: Set<String> = emptySet(),
    val allKnownSensors: List<ObdSensor> = emptyList(),
    val isTrackMode: Boolean = false
)

sealed interface DashboardEffect {
    data class ShowToast(val message: String) : DashboardEffect
}

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val bleDataSource: BleDataSource,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val sensorRepository: SensorRepository
) : ViewModel() {

    private val _effect = Channel<DashboardEffect>()
    val effect: Flow<DashboardEffect> = _effect.receiveAsFlow()

    private val _supportedSensors = MutableStateFlow<List<ObdSensor>>(emptyList())
    private val _isTrackMode = MutableStateFlow(false)

    init {
        viewModelScope.launch {
            userPreferencesRepository.selectedSensors.collect { pids ->
                bleDataSource.setPollingPids(pids.toList())
            }
        }
        viewModelScope.launch {
            bleDataSource.connectionState.collect { status ->
                if (status == ConnectionStatus.CONNECTED) {
                    scanSensors()
                }
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    val state = combine(
        bleDataSource.telemetry,
        userPreferencesRepository.useMetric,
        _supportedSensors,
        userPreferencesRepository.selectedSensors,
        sensorRepository.getSensors(),
        _isTrackMode
    ) { args ->
        DashboardState(
            telemetry = args[0] as? TelemetryFrame,
            useMetric = args[1] as Boolean,
            supportedSensors = args[2] as List<ObdSensor>,
            selectedSensors = args[3] as Set<String>,
            allKnownSensors = args[4] as List<ObdSensor>,
            isTrackMode = args[5] as Boolean
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardState())

    fun setSelectedSensors(sensors: Set<String>) {
        viewModelScope.launch {
            userPreferencesRepository.setSelectedSensors(sensors)
        }
    }

    private fun scanSensors() {
        viewModelScope.launch {
            _supportedSensors.value = bleDataSource.getSupportedSensors()
        }
    }

    fun toggleTrackMode() {
        _isTrackMode.value = !_isTrackMode.value
    }
}

package com.pavloglez.xcan.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pavloglez.xcan.core.bluetooth.BleDataSource
import com.pavloglez.xcan.core.bluetooth.ConnectionStatus
import com.pavloglez.xcan.core.data.CarRepository
import com.pavloglez.xcan.core.data.UserPreferencesRepository
import com.pavloglez.xcan.core.model.ObdSensor
import com.pavloglez.xcan.core.model.SensorRepository
import com.pavloglez.xcan.core.model.TelemetryFrame
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.pavloglez.xcan.core.model.ObdConstants

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

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val bleDataSource: BleDataSource,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val sensorRepository: SensorRepository,
    private val carRepository: CarRepository
) : ViewModel() {

    private val _effect = Channel<DashboardEffect>()
    val effect: Flow<DashboardEffect> = _effect.receiveAsFlow()

    private val _supportedSensors = MutableStateFlow<List<ObdSensor>>(emptyList())
    private val _isTrackMode = MutableStateFlow(false)

    private val activeCarSelectedSensors = carRepository.getActiveCar()
        .flatMapLatest { car ->
            userPreferencesRepository.getSelectedSensors(car?.id)
        }

    init {
        viewModelScope.launch {
            activeCarSelectedSensors.collect { pids ->
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
        activeCarSelectedSensors,
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
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(ObdConstants.STOP_TIMEOUT_MS), DashboardState())

    fun setSelectedSensors(sensors: Set<String>) {
        viewModelScope.launch {
            val carId = carRepository.getActiveCar().firstOrNull()?.id
            userPreferencesRepository.setSelectedSensors(carId, sensors)
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


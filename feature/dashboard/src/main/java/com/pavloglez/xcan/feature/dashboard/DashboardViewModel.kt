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
    val sensorScanStatus: com.pavloglez.xcan.core.model.SensorScanStatus = com.pavloglez.xcan.core.model.SensorScanStatus.IDLE,
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
                    scanSensors(forceRescan = false)
                }
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    val state = combine(
        bleDataSource.telemetry,
        userPreferencesRepository.useMetric,
        _supportedSensors,
        bleDataSource.sensorScanStatus,
        activeCarSelectedSensors,
        sensorRepository.getSensors(),
        _isTrackMode
    ) { args ->
        DashboardState(
            telemetry = args[0] as? TelemetryFrame,
            useMetric = args[1] as Boolean,
            supportedSensors = args[2] as List<ObdSensor>,
            sensorScanStatus = args[3] as com.pavloglez.xcan.core.model.SensorScanStatus,
            selectedSensors = args[4] as Set<String>,
            allKnownSensors = args[5] as List<ObdSensor>,
            isTrackMode = args[6] as Boolean
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(ObdConstants.STOP_TIMEOUT_MS), DashboardState())

    fun setSelectedSensors(sensors: Set<String>) {
        viewModelScope.launch {
            val carId = carRepository.getActiveCar().firstOrNull()?.id
            userPreferencesRepository.setSelectedSensors(carId, sensors)
        }
    }

    fun scanSensors(forceRescan: Boolean = false) {
        viewModelScope.launch {
            val activeCar = carRepository.getActiveCar().firstOrNull()
            if (!forceRescan) {
                val cachedPids = userPreferencesRepository.getSupportedPids(activeCar?.id).firstOrNull()
                if (!cachedPids.isNullOrEmpty()) {
                    val allKnown = sensorRepository.getSensors().firstOrNull() ?: emptyList()
                    _supportedSensors.value = allKnown.filter { cachedPids.contains(it.pid) }
                    return@launch
                }
            }

            val discovered = bleDataSource.getSupportedSensors()
            _supportedSensors.value = discovered
            if (discovered.isNotEmpty()) {
                val pidSet = discovered.map { it.pid }.toSet()
                userPreferencesRepository.setSupportedPids(activeCar?.id, pidSet)
            }
        }
    }

    fun toggleTrackMode() {
        _isTrackMode.value = !_isTrackMode.value
    }
}


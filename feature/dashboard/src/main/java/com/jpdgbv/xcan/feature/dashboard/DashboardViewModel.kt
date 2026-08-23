package com.jpdgbv.xcan.feature.dashboard

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jpdgbv.xcan.core.bluetooth.BleDataSource
import com.jpdgbv.xcan.core.bluetooth.ConnectionStatus
import com.jpdgbv.xcan.core.data.CarRepository
import com.jpdgbv.xcan.core.data.LoggingRepository
import com.jpdgbv.xcan.core.data.LoggingService
import com.jpdgbv.xcan.core.data.LoggingState
import com.jpdgbv.xcan.core.data.UserPreferencesRepository
import com.jpdgbv.xcan.core.model.CarProfile
import com.jpdgbv.xcan.core.model.ObdSensor
import com.jpdgbv.xcan.core.model.TelemetryFrame
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

import com.jpdgbv.xcan.core.bluetooth.ScannedDevice
import com.jpdgbv.xcan.core.model.SensorRepository

data class DashboardState(
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

sealed interface DashboardIntent {
    object StartScanning : DashboardIntent
    object StopScanning : DashboardIntent
    data class Connect(val macAddress: String) : DashboardIntent
    object Disconnect : DashboardIntent
    data class AddCar(val make: String, val model: String, val year: Int) : DashboardIntent
    data class SelectCar(val id: String) : DashboardIntent
    data class SetSelectedSensors(val sensors: Set<String>) : DashboardIntent
    object ScanSensors : DashboardIntent
    data class StartLogging(val carId: String, val carLabel: String) : DashboardIntent
    object PauseLogging : DashboardIntent
    object ResumeLogging : DashboardIntent
    object StopLogging : DashboardIntent
    object ToggleTrackMode : DashboardIntent
}

@HiltViewModel
class DashboardViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val bleDataSource: BleDataSource,
    private val carRepository: CarRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val sensorRepository: SensorRepository,
    private val loggingRepository: LoggingRepository
) : ViewModel() {

    private val _isScanning = MutableStateFlow(false)
    private val _discoveredDevices = MutableStateFlow<List<ScannedDevice>>(emptyList())
    private var scanningJob: kotlinx.coroutines.Job? = null

    private val _connectionLogs = MutableStateFlow<List<String>>(emptyList())
    val _supportedSensors = MutableStateFlow<List<ObdSensor>>(emptyList())
    private val _loggingState = LoggingService.loggingState
    private val _isTrackMode = MutableStateFlow(false)

    init {
        viewModelScope.launch {
            bleDataSource.connectionLogs.collect { log ->
                val currentLogs = _connectionLogs.value.toMutableList()
                currentLogs.add(log)
                if (currentLogs.size > 100) {
                    currentLogs.removeAt(0)
                }
                _connectionLogs.value = currentLogs
            }
        }
        viewModelScope.launch {
            userPreferencesRepository.selectedSensors.collect { pids ->
                bleDataSource.setPollingPids(pids.toList())
            }
        }
        viewModelScope.launch {
            bleDataSource.connectionState.collect { status ->
                if (status == ConnectionStatus.CONNECTED) {
                    onIntent(DashboardIntent.ScanSensors)
                }
            }
        }
    }

    val state: StateFlow<DashboardState> = kotlinx.coroutines.flow.combine(
        bleDataSource.connectionState,
        bleDataSource.telemetry,
        _connectionLogs,
        _discoveredDevices,
        _isScanning,
        carRepository.getAllCars(),
        carRepository.getActiveCar(),
        userPreferencesRepository.useMetric,
        _supportedSensors,
        userPreferencesRepository.selectedSensors,
        sensorRepository.getSensors(),
        _loggingState,
        _isTrackMode
    ) { args ->
        val status = args[0] as ConnectionStatus
        val telemetry = args[1] as? TelemetryFrame
        val logs = args[2] as List<String>
        val devices = args[3] as List<ScannedDevice>
        val isScanning = args[4] as Boolean
        val cars = args[5] as List<CarProfile>
        val activeCar = args[6] as? CarProfile
        val useMetric = args[7] as Boolean
        val supported = args[8] as List<ObdSensor>
        val selected = args[9] as Set<String>
        val allKnown = args[10] as List<ObdSensor>
        val loggingState = args[11] as LoggingState
        val trackMode = args[12] as Boolean

        DashboardState(
            connectionStatus = status,
            telemetry = if (status == ConnectionStatus.CONNECTED) telemetry else null,
            connectionLogs = logs,
            discoveredDevices = devices,
            isScanning = isScanning,
            cars = cars,
            activeCar = activeCar,
            useMetric = useMetric,
            supportedSensors = supported,
            selectedSensors = selected,
            allKnownSensors = allKnown,
            loggingState = loggingState,
            isTrackMode = trackMode
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardState()
    )

    fun onIntent(intent: DashboardIntent) {
        when (intent) {
            DashboardIntent.StartScanning -> {
                _isScanning.value = true
                scanningJob?.cancel()
                scanningJob = viewModelScope.launch {
                    bleDataSource.scanForDevices().collect { devices ->
                        _discoveredDevices.value = devices
                    }
                }
            }
            DashboardIntent.StopScanning -> {
                _isScanning.value = false
                scanningJob?.cancel()
            }
            is DashboardIntent.Connect -> {
                _isScanning.value = false
                scanningJob?.cancel()
                viewModelScope.launch {
                    bleDataSource.connect(intent.macAddress)
                }
            }
            DashboardIntent.Disconnect -> {
                _isScanning.value = false
                scanningJob?.cancel()
                viewModelScope.launch {
                    bleDataSource.disconnect()
                }
            }
            is DashboardIntent.AddCar -> {
                viewModelScope.launch {
                    val newCar = CarProfile(
                        id = UUID.randomUUID().toString(),
                        name = "${intent.year} ${intent.make} ${intent.model}",
                        make = intent.make,
                        model = intent.model,
                        year = intent.year,
                        isActive = false
                    )
                    carRepository.addCar(newCar)
                    if (state.value.activeCar == null) {
                        carRepository.setActiveCar(newCar.id)
                    }
                }
            }
            is DashboardIntent.SelectCar -> {
                viewModelScope.launch {
                    carRepository.setActiveCar(intent.id)
                }
            }
            is DashboardIntent.SetSelectedSensors -> {
                viewModelScope.launch {
                    userPreferencesRepository.setSelectedSensors(intent.sensors)
                }
            }
            DashboardIntent.ScanSensors -> {
                viewModelScope.launch {
                    _supportedSensors.value = bleDataSource.getSupportedSensors()
                }
            }
            is DashboardIntent.StartLogging -> {
                viewModelScope.launch {
                    val sessionId = loggingRepository.startSession(intent.carId, intent.carLabel)
                    val startMs = System.currentTimeMillis()
                    context.startService(
                        LoggingService.startIntent(context, sessionId, intent.carLabel, startMs)
                    )
                }
            }
            DashboardIntent.PauseLogging -> {
                val current = _loggingState.value as? LoggingState.Recording ?: return
                context.startService(LoggingService.pauseIntent(context, current.sessionId))
            }
            DashboardIntent.ResumeLogging -> {
                val current = _loggingState.value as? LoggingState.Paused ?: return
                context.startService(LoggingService.resumeIntent(context, current.sessionId))
            }
            DashboardIntent.StopLogging -> {
                val current = _loggingState.value
                val sessionId = when (current) {
                    is LoggingState.Recording -> current.sessionId
                    is LoggingState.Paused -> current.sessionId
                    else -> return
                }
                context.startService(LoggingService.stopIntent(context, sessionId))
            }
            DashboardIntent.ToggleTrackMode -> {
                _isTrackMode.value = !_isTrackMode.value
            }
        }
    }
}

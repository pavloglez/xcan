package com.pavloglez.xcan.feature.logging

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pavloglez.xcan.core.data.LoggingRepository
import com.pavloglez.xcan.core.model.LogEntry
import com.pavloglez.xcan.core.model.LogSession
import com.pavloglez.xcan.core.model.SensorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import com.pavloglez.xcan.core.model.ObdConstants

data class SensorMetricSeries(
    val pid: String,
    val displayName: String,
    val unit: String,
    val entries: List<LogEntry>
)

data class LogSessionDetailState(
    val session: LogSession? = null,
    val series: List<SensorMetricSeries> = emptyList(),
    val isLoading: Boolean = true
) {
    val entriesByPid: Map<String, List<LogEntry>>
        get() = series.associate { it.pid to it.entries }
}

@HiltViewModel
class LogSessionDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val loggingRepository: LoggingRepository,
    private val sensorRepository: SensorRepository
) : ViewModel() {

    private val sessionId: String = checkNotNull(savedStateHandle["sessionId"])

    val state: StateFlow<LogSessionDetailState> = combine(
        loggingRepository.getSessionById(sessionId),
        loggingRepository.getEntriesForSession(sessionId),
        sensorRepository.getSensors()
    ) { session, entries, sensors ->
        val sensorMap = sensors.associateBy { it.pid }
        val metricSeries = entries.groupBy { it.pid }.map { (pid, logEntries) ->
            val sensor = sensorMap[pid] ?: sensorRepository.getSensorByPidSync(pid)
            SensorMetricSeries(
                pid = pid,
                displayName = sensor.displayName,
                unit = sensor.unit,
                entries = logEntries
            )
        }
        LogSessionDetailState(
            session = session,
            series = metricSeries,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(ObdConstants.STOP_TIMEOUT_MS),
        initialValue = LogSessionDetailState()
    )
}

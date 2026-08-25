package com.pavloglez.xcan.feature.logging

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pavloglez.xcan.core.data.LoggingRepository
import com.pavloglez.xcan.core.model.LogEntry
import com.pavloglez.xcan.core.model.LogSession
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class LogSessionDetailState(
    val session: LogSession? = null,
    val entriesByPid: Map<String, List<LogEntry>> = emptyMap(),
    val isLoading: Boolean = true
)

@HiltViewModel
class LogSessionDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val loggingRepository: LoggingRepository
) : ViewModel() {

    private val sessionId: String = checkNotNull(savedStateHandle["sessionId"])

    val state: StateFlow<LogSessionDetailState> = combine(
        loggingRepository.getSessionById(sessionId),
        loggingRepository.getEntriesForSession(sessionId)
    ) { session, entries ->
        LogSessionDetailState(
            session = session,
            entriesByPid = entries.groupBy { it.pid },
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = LogSessionDetailState()
    )
}

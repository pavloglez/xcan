package com.pavloglez.xcan.feature.logging

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pavloglez.xcan.core.data.LoggingRepository
import com.pavloglez.xcan.core.model.LogSession
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LogSessionsState(
    val sessions: List<LogSession> = emptyList(),
    val isLoading: Boolean = true,
    val showDeleteAllConfirm: Boolean = false
)

sealed interface LogSessionsIntent {
    data class DeleteSession(val session: LogSession) : LogSessionsIntent
    object DeleteAllSessions : LogSessionsIntent
    object ConfirmDeleteAll : LogSessionsIntent
    object DismissDeleteAll : LogSessionsIntent
}

@HiltViewModel
class LogSessionsViewModel @Inject constructor(
    private val loggingRepository: LoggingRepository
) : ViewModel() {

    val state: StateFlow<LogSessionsState> = loggingRepository.getAllSessions()
        .map { sessions ->
            LogSessionsState(sessions = sessions, isLoading = false)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = LogSessionsState()
        )

    fun onIntent(intent: LogSessionsIntent) {
        when (intent) {
            is LogSessionsIntent.DeleteSession -> {
                viewModelScope.launch {
                    loggingRepository.deleteSession(intent.session)
                }
            }
            LogSessionsIntent.DeleteAllSessions -> { /* handled by state directly */ }
            LogSessionsIntent.ConfirmDeleteAll -> {
                viewModelScope.launch {
                    loggingRepository.deleteAllSessions()
                }
            }
            LogSessionsIntent.DismissDeleteAll -> { /* handled by state directly */ }
        }
    }
}

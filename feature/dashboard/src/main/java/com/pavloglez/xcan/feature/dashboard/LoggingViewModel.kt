package com.pavloglez.xcan.feature.dashboard

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pavloglez.xcan.core.data.LoggingRepository
import com.pavloglez.xcan.core.data.LoggingService
import com.pavloglez.xcan.core.data.LoggingState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoggingViewState(
    val loggingState: LoggingState = LoggingState.Idle
)

@HiltViewModel
class LoggingViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val loggingRepository: LoggingRepository
) : ViewModel() {

    val state = LoggingService.loggingState.map {
        LoggingViewState(it)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LoggingViewState())

    fun startLogging(carId: String, carLabel: String) {
        viewModelScope.launch {
            val sessionId = loggingRepository.startSession(carId, carLabel)
            val startMs = System.currentTimeMillis()
            context.startService(
                LoggingService.startIntent(context, sessionId, carLabel, startMs)
            )
        }
    }

    fun pauseLogging() {
        val current = state.value.loggingState as? LoggingState.Recording ?: return
        context.startService(LoggingService.pauseIntent(context, current.sessionId))
    }

    fun resumeLogging() {
        val current = state.value.loggingState as? LoggingState.Paused ?: return
        context.startService(LoggingService.resumeIntent(context, current.sessionId))
    }

    fun stopLogging() {
        val current = state.value.loggingState
        val sessionId = when (current) {
            is LoggingState.Recording -> current.sessionId
            is LoggingState.Paused -> current.sessionId
            else -> return
        }
        context.startService(LoggingService.stopIntent(context, sessionId))
    }
}

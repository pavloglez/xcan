package com.jpdgbv.xcan.feature.config

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch
import com.jpdgbv.xcan.core.data.UserPreferencesRepository
import javax.inject.Inject

data class ConfigState(
    val useMetric: Boolean = false,
    val overrideProtocol: String = "AUTO"
)

sealed interface ConfigIntent {
    data class ToggleMetric(val metric: Boolean) : ConfigIntent
    data class SetProtocol(val protocol: String) : ConfigIntent
}

@HiltViewModel
class ConfigViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    val state: StateFlow<ConfigState> = userPreferencesRepository.useMetric.map { useMetric ->
        ConfigState(useMetric = useMetric)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ConfigState()
    )

    fun onIntent(intent: ConfigIntent) {
        when (intent) {
            is ConfigIntent.ToggleMetric -> {
                viewModelScope.launch {
                    userPreferencesRepository.setUseMetric(intent.metric)
                }
            }
            is ConfigIntent.SetProtocol -> {
                // Not yet implemented for persistence
            }
        }
    }
}

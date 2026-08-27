package com.pavloglez.xcan.feature.maintenance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pavloglez.xcan.core.data.CarRepository
import com.pavloglez.xcan.core.data.MaintenanceRepository
import com.pavloglez.xcan.core.data.UserPreferencesRepository
import com.pavloglez.xcan.core.model.CarProfile
import com.pavloglez.xcan.core.model.MaintenanceLog
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import com.pavloglez.xcan.core.model.ObdConstants

data class MaintenanceState(
    val logs: List<MaintenanceLog> = emptyList(),
    val isLoading: Boolean = true,
    val activeCar: CarProfile? = null,
    val useMetric: Boolean = false
)

sealed interface MaintenanceIntent {
    data class AddLog(val serviceType: String, val notes: String, val mileage: Int, val relatedDtc: String?) : MaintenanceIntent
}

@HiltViewModel
class MaintenanceViewModel @Inject constructor(
    private val maintenanceRepository: MaintenanceRepository,
    private val carRepository: CarRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    val state: StateFlow<MaintenanceState> = combine(
        maintenanceRepository.getAllLogs(),
        carRepository.getActiveCar(),
        userPreferencesRepository.useMetric
    ) { logs, activeCar, useMetric ->
        MaintenanceState(
            logs = logs,
            isLoading = false,
            activeCar = activeCar,
            useMetric = useMetric
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(ObdConstants.STOP_TIMEOUT_MS),
        initialValue = MaintenanceState(isLoading = true)
    )

    fun onIntent(intent: MaintenanceIntent) {
        when (intent) {
            is MaintenanceIntent.AddLog -> {
                viewModelScope.launch {
                    val currentCar = state.value.activeCar ?: return@launch
                    val newLog = MaintenanceLog(
                        id = UUID.randomUUID().toString(),
                        carId = currentCar.id,
                        serviceType = intent.serviceType,
                        dateMs = System.currentTimeMillis(),
                        mileage = intent.mileage,
                        cost = 0.0,
                        notes = intent.notes,
                        relatedDtc = intent.relatedDtc
                    )
                    maintenanceRepository.addLog(newLog)
                }
            }

        }
    }
}

package com.pavloglez.xcan.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pavloglez.xcan.core.data.CarRepository
import com.pavloglez.xcan.core.model.CarProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class CarProfileState(
    val cars: List<CarProfile> = emptyList(),
    val activeCar: CarProfile? = null
)

sealed interface CarProfileEffect {
    data class ShowError(val message: String) : CarProfileEffect
}

@HiltViewModel
class CarProfileViewModel @Inject constructor(
    private val carRepository: CarRepository
) : ViewModel() {

    private val _effect = Channel<CarProfileEffect>()
    val effect: Flow<CarProfileEffect> = _effect.receiveAsFlow()

    val state = combine(
        carRepository.getAllCars(),
        carRepository.getActiveCar()
    ) { cars, activeCar ->
        CarProfileState(cars, activeCar)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CarProfileState())

    fun addCar(make: String, model: String, year: Int) {
        val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
        if (make.isBlank() || model.isBlank() || year !in 1886..currentYear + 1) {
            viewModelScope.launch {
                _effect.send(CarProfileEffect.ShowError("Invalid car details"))
            }
            return
        }
        viewModelScope.launch {
            val newCar = CarProfile(
                id = UUID.randomUUID().toString(),
                name = "$year $make $model",
                make = make,
                model = model,
                year = year,
                isActive = false
            )
            carRepository.addCar(newCar)
            if (state.value.activeCar == null) {
                carRepository.setActiveCar(newCar.id)
            }
        }
    }

    fun selectCar(id: String) {
        viewModelScope.launch {
            carRepository.setActiveCar(id)
        }
    }
}

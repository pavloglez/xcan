package com.jpdgbv.xcan.core.data.repository

import com.jpdgbv.xcan.core.model.ObdSensor
import com.jpdgbv.xcan.core.model.SensorRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SensorRepositoryImpl @Inject constructor() : SensorRepository {

    // Default predefined standard sensors
    private val standardSensors = listOf(
        ObdSensor(pid = "010C", displayName = "Engine RPM", unit = "RPM", expectedBytes = 2, formula = "(A*256+B)/4"),
        ObdSensor(pid = "010D", displayName = "Vehicle Speed", unit = "KM/H", expectedBytes = 1, formula = "A"),
        ObdSensor(pid = "0104", displayName = "Engine Load", unit = "%", expectedBytes = 1, formula = "(A*100)/255"),
        ObdSensor(pid = "0105", displayName = "Coolant Temp", unit = "°C", expectedBytes = 1, formula = "A-40"),
        ObdSensor(pid = "010F", displayName = "Intake Air Temp", unit = "°C", expectedBytes = 1, formula = "A-40"),
        ObdSensor(pid = "0110", displayName = "MAF Air Flow Rate", unit = "g/s", expectedBytes = 2, formula = "(A*256+B)/100"),
        ObdSensor(pid = "0111", displayName = "Throttle Position", unit = "%", expectedBytes = 1, formula = "(A*100)/255"),
        ObdSensor(pid = "011F", displayName = "Run Time", unit = "sec", expectedBytes = 2, formula = "A*256+B"),
        ObdSensor(pid = "012F", displayName = "Fuel Level", unit = "%", expectedBytes = 1, formula = "(A*100)/255"),
        ObdSensor(pid = "0146", displayName = "Ambient Air Temp", unit = "°C", expectedBytes = 1, formula = "A-40")
    )

    // In a real app, this would be backed by Room. For now, it's just in memory.
    private val customSensors = java.util.concurrent.CopyOnWriteArrayList<ObdSensor>()
    private val _sensors = MutableStateFlow(standardSensors + customSensors)

    override fun getSensors(): Flow<List<ObdSensor>> = _sensors.asStateFlow()

    override suspend fun getSensorByPid(pid: String): ObdSensor {
        return getSensorByPidSync(pid)
    }

    override fun getSensorByPidSync(pid: String): ObdSensor {
        val allSensors = standardSensors + customSensors
        return allSensors.find { it.pid == pid } ?: ObdSensor(
            pid = pid,
            displayName = "Unknown PID $pid",
            unit = "Raw",
            expectedBytes = -1, // -1 signals we don't know the exact length, parse all available
            formula = "RAW"
        )
    }

    override suspend fun saveSensor(sensor: ObdSensor) {
        val existing = customSensors.indexOfFirst { it.pid == sensor.pid }
        if (existing != -1) {
            customSensors[existing] = sensor
        } else {
            customSensors.add(sensor)
        }
        _sensors.value = standardSensors + customSensors
    }
}

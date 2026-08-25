package com.pavloglez.xcan.core.model

import kotlinx.coroutines.flow.Flow

interface SensorRepository {
    /**
     * Get a flow of all sensors (predefined + custom user-defined).
     */
    fun getSensors(): Flow<List<ObdSensor>>

    /**
     * Get a specific sensor by its PID.
     * If the sensor is not found, should return an "Unknown" sensor configuration.
     */
    suspend fun getSensorByPid(pid: String): ObdSensor

    /**
     * Get a specific sensor by its PID synchronously.
     * This is useful for parsers that are synchronous.
     */
    fun getSensorByPidSync(pid: String): ObdSensor

    /**
     * Save a custom sensor configuration (e.g. user renamed an unknown PID).
     */
    suspend fun saveSensor(sensor: ObdSensor)
}

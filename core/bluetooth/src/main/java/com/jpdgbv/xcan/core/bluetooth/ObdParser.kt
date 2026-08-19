package com.jpdgbv.xcan.core.bluetooth

import com.jpdgbv.xcan.core.model.SensorRepository
import com.jpdgbv.xcan.core.model.TelemetryFrame
import java.util.UUID

object ObdParser {
    val OBD_SERVICE_UUID: UUID = UUID.fromString("0000FFF0-0000-1000-8000-00805F9B34FB")
    val OBD_RX_CHARACTERISTIC_UUID: UUID = UUID.fromString("0000FFF1-0000-1000-8000-00805F9B34FB")
    val OBD_TX_CHARACTERISTIC_UUID: UUID = UUID.fromString("0000FFF2-0000-1000-8000-00805F9B34FB")

    // We maintain internal state so we can return a unified TelemetryFrame on each update
    private val currentSensors = mutableMapOf<String, Float>()

    fun parse(data: String, sensorRepo: SensorRepository): TelemetryFrame? {
        val cleanData = data.replace(" ", "").replace("\r", "").replace(">", "")
        if (cleanData.length < 4 || !cleanData.startsWith("41")) return null

        val pid = cleanData.substring(2, 4)
        val fullPid = "01$pid"
        
        try {
            val sensor = sensorRepo.getSensorByPidSync(fullPid)
            
            val expectedBytes = if (sensor.expectedBytes == -1) {
                (cleanData.length - 4) / 2 // Parse all remaining bytes if length is unknown
            } else {
                sensor.expectedBytes
            }
            
            val requiredLength = 4 + (expectedBytes * 2)
            if (cleanData.length >= requiredLength && expectedBytes > 0) {
                val bytes = IntArray(expectedBytes)
                for (i in 0 until expectedBytes) {
                    val startIndex = 4 + (i * 2)
                    bytes[i] = cleanData.substring(startIndex, startIndex + 2).toInt(16)
                }
                
                currentSensors[fullPid] = evaluateFormula(sensor.formula, bytes)
            }
        } catch (e: Exception) {
            return null // Parsing error, ignore
        }

        return TelemetryFrame(
            id = UUID.randomUUID().toString(),
            timestampMs = System.currentTimeMillis(),
            sensors = currentSensors.toMap()
        )
    }
    
    private fun evaluateFormula(formula: String, bytes: IntArray): Float {
        if (formula == "RAW") {
            // For raw data, just convert the first few bytes to a float representation 
            // so it can fit in our TelemetryFrame.
            var value = 0f
            if (bytes.isNotEmpty()) value = bytes[0].toFloat()
            if (bytes.size > 1) value += (bytes[1] / 1000f)
            return value
        }
        
        // Lightweight predefined evaluator
        // In the future, replace with a proper math expression evaluator library (e.g., exp4j)
        return try {
            when (formula) {
                "(A*256+B)/4" -> if (bytes.size >= 2) ((bytes[0] * 256) + bytes[1]) / 4f else 0f
                "A" -> if (bytes.size >= 1) bytes[0].toFloat() else 0f
                "(A*100)/255" -> if (bytes.size >= 1) (bytes[0] * 100f) / 255f else 0f
                "A-40" -> if (bytes.size >= 1) (bytes[0] - 40).toFloat() else 0f
                "(A*256+B)/100" -> if (bytes.size >= 2) ((bytes[0] * 256) + bytes[1]) / 100f else 0f
                "A*256+B" -> if (bytes.size >= 2) ((bytes[0] * 256) + bytes[1]).toFloat() else 0f
                else -> 0f 
            }
        } catch (e: Exception) {
            0f
        }
    }
}


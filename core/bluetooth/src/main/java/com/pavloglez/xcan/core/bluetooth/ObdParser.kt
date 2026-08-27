package com.pavloglez.xcan.core.bluetooth

import com.pavloglez.xcan.core.model.SensorRepository
import java.util.UUID
import javax.inject.Inject
import com.pavloglez.xcan.core.model.ObdConstants

class ObdParser @Inject constructor(
    private val sensorRepo: SensorRepository
) {
    companion object {
        val OBD_SERVICE_UUID: UUID = UUID.fromString("0000FFF0-0000-1000-8000-00805F9B34FB")
        val OBD_RX_CHARACTERISTIC_UUID: UUID = UUID.fromString("0000FFF1-0000-1000-8000-00805F9B34FB")
        val OBD_TX_CHARACTERISTIC_UUID: UUID = UUID.fromString("0000FFF2-0000-1000-8000-00805F9B34FB")
    }

    fun parse(data: String): Pair<String, Float>? {
        val cleanData = data.replace(" ", "").replace("\r", "").replace(">", "")
        if (cleanData.length < ObdConstants.MIN_OBD_RESPONSE_LENGTH || !cleanData.startsWith(ObdConstants.SERVICE_01_RESPONSE_PREFIX)) return null

        val pid = cleanData.substring(2, 4)
        val fullPid = "${ObdConstants.SERVICE_01_MODE}$pid"
        
        try {
            // High-performance path for standard PIDs
            val standardPid = com.pavloglez.xcan.core.model.StandardPid.fromHex(pid)
            if (standardPid != null) {
                val expectedBytes = standardPid.bytesReturned
                val requiredLength = 4 + (expectedBytes * 2)
                if (cleanData.length >= requiredLength && expectedBytes > 0) {
                    val bytes = ByteArray(expectedBytes)
                    for (i in 0 until expectedBytes) {
                        val startIndex = 4 + (i * 2)
                        bytes[i] = cleanData.substring(startIndex, startIndex + 2).toInt(16).toByte()
                    }
                    return Pair(fullPid, standardPid.decode(bytes))
                }
            }

            // Fallback for custom or string-formula sensors
            val sensor = sensorRepo.getSensorByPidSync(fullPid)
            
            val expectedBytes = if (sensor.expectedBytes == -1) {
                (cleanData.length - 4) / 2 
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
                return Pair(fullPid, evaluateFormula(sensor.formula, bytes))
            }
        } catch (e: Exception) {
            return null 
        }
        return null
    }
    
    private fun evaluateFormula(formula: String, bytes: IntArray): Float {
        if (formula == ObdConstants.FORMULA_RAW) {
            // For raw data, just convert the first few bytes to a float representation 
            // so it can fit in our TelemetryFrame.
            var value = 0f
            if (bytes.isNotEmpty()) value = bytes[0].toFloat()
            if (bytes.size > 1) value += (bytes[1] / ObdConstants.RAW_BYTE2_DIVISOR)
            return value
        }
        
        // Lightweight predefined evaluator
        // In the future, replace with a proper math expression evaluator library (e.g., exp4j)
        return try {
            when (formula) {
                ObdConstants.FORMULA_RPM -> if (bytes.size >= 2) ((bytes[0] * ObdConstants.HIGH_BYTE_MULTIPLIER) + bytes[1]) / ObdConstants.RPM_DIVISOR else 0f
                ObdConstants.FORMULA_SINGLE_BYTE -> if (bytes.size >= 1) bytes[0].toFloat() else 0f
                ObdConstants.FORMULA_PERCENT -> if (bytes.size >= 1) (bytes[0] * ObdConstants.PERCENT_SCALE) / ObdConstants.BYTE_MAX_FLOAT else 0f
                ObdConstants.FORMULA_TEMP -> if (bytes.size >= 1) (bytes[0] - ObdConstants.TEMP_OFFSET_CELSIUS.toInt()).toFloat() else 0f
                ObdConstants.FORMULA_MAF -> if (bytes.size >= 2) ((bytes[0] * ObdConstants.HIGH_BYTE_MULTIPLIER) + bytes[1]) / ObdConstants.MAF_DIVISOR else 0f
                ObdConstants.FORMULA_TWO_BYTE -> if (bytes.size >= 2) ((bytes[0] * ObdConstants.HIGH_BYTE_MULTIPLIER) + bytes[1]).toFloat() else 0f
                else -> 0f 
            }
        } catch (e: Exception) {
            0f
        }
    }
}


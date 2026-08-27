package com.pavloglez.xcan.core.model

/**
 * Standard SAE J1979 OBD-II PIDs for Service 01 (Current Data).
 *
 * Each PID defines its expected response size and a lambda to decode the
 * raw bytes into a physical floating-point value.
 */
enum class StandardPid(
    val mode: String,
    val pidHex: String,
    val bytesReturned: Int,
    val description: String,
    val unit: String,
    val decode: (ByteArray) -> Float
) {
    // 01 04 - Calculated Engine Load
    ENGINE_LOAD("01", "04", 1, "Calculated engine load", "%", { 
        (it[0].toUByte().toFloat() * ObdConstants.PERCENT_SCALE) / ObdConstants.BYTE_MAX_FLOAT 
    }),

    // 01 05 - Engine Coolant Temperature
    COOLANT_TEMP("01", "05", 1, "Engine coolant temperature", "°C", { 
        it[0].toUByte().toFloat() - ObdConstants.TEMP_OFFSET_CELSIUS 
    }),

    // 01 0B - Intake Manifold Absolute Pressure
    INTAKE_PRESSURE("01", "0B", 1, "Intake manifold absolute pressure", "kPa", { 
        it[0].toUByte().toFloat() 
    }),

    // 01 0C - Engine RPM
    ENGINE_RPM("01", "0C", 2, "Engine RPM", "rpm", { 
        ((it[0].toUByte().toInt() * ObdConstants.HIGH_BYTE_MULTIPLIER) + it[1].toUByte().toInt()) / ObdConstants.RPM_DIVISOR 
    }),

    // 01 0D - Vehicle Speed
    VEHICLE_SPEED("01", "0D", 1, "Vehicle speed", "km/h", { 
        it[0].toUByte().toFloat() 
    }),

    // 01 0E - Timing Advance
    TIMING_ADVANCE("01", "0E", 1, "Timing advance", "°", { 
        (it[0].toUByte().toFloat() / ObdConstants.TIMING_ADVANCE_DIVISOR) - ObdConstants.TIMING_ADVANCE_OFFSET 
    }),

    // 01 0F - Intake Air Temperature
    INTAKE_TEMP("01", "0F", 1, "Intake air temperature", "°C", { 
        it[0].toUByte().toFloat() - ObdConstants.TEMP_OFFSET_CELSIUS 
    }),

    // 01 10 - MAF Air Flow Rate
    MAF_AIR_FLOW("01", "10", 2, "MAF air flow rate", "grams/sec", { 
        ((it[0].toUByte().toInt() * ObdConstants.HIGH_BYTE_MULTIPLIER) + it[1].toUByte().toInt()) / ObdConstants.MAF_DIVISOR 
    }),

    // 01 11 - Throttle Position
    THROTTLE_POSITION("01", "11", 1, "Throttle position", "%", { 
        (it[0].toUByte().toFloat() * ObdConstants.PERCENT_SCALE) / ObdConstants.BYTE_MAX_FLOAT 
    }),
    
    // 01 33 - Barometric Pressure
    BAROMETRIC_PRESSURE("01", "33", 1, "Barometric pressure", "kPa", {
        it[0].toUByte().toFloat()
    }),

    // 01 46 - Ambient Air Temperature
    AMBIENT_AIR_TEMP("01", "46", 1, "Ambient air temperature", "°C", {
        it[0].toUByte().toFloat() - ObdConstants.TEMP_OFFSET_CELSIUS
    }),
    
    // 01 5C - Engine Oil Temperature
    ENGINE_OIL_TEMP("01", "5C", 1, "Engine oil temperature", "°C", {
        it[0].toUByte().toFloat() - ObdConstants.TEMP_OFFSET_CELSIUS
    });

    companion object {
        private val map = entries.associateBy { it.pidHex }
        
        /**
         * Finds a StandardPid by its hexadecimal string (e.g. "0C").
         */
        fun fromHex(pidHex: String): StandardPid? = map[pidHex]
    }
}

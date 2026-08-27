package com.pavloglez.xcan.core.model

/**
 * Central repository of OBD-II protocol constants defined by SAE J1979.
 * Shared across :core:bluetooth, :core:data, and feature modules.
 */
object ObdConstants {
    // --- Byte math ---
    const val BYTE_MAX_FLOAT = 255f
    const val HIGH_BYTE_MULTIPLIER = 256
    const val PERCENT_SCALE = 100f
    const val NIBBLE_MASK = 0xF
    const val NIBBLE_SHIFT = 4

    // --- SAE J1979 formula constants ---
    const val TEMP_OFFSET_CELSIUS = 40f
    const val RPM_DIVISOR = 4f
    const val TIMING_ADVANCE_DIVISOR = 2f
    const val TIMING_ADVANCE_OFFSET = 64f
    const val MAF_DIVISOR = 100f

    // --- Unit conversion ---
    const val KMH_TO_MPH = 0.621371f
    /** Correct floating-point scale (fixes integer division bug from 9/5 = 1). */
    const val CELSIUS_TO_FAHRENHEIT_SCALE = 1.8f
    const val CELSIUS_TO_FAHRENHEIT_OFFSET = 32f

    // --- Service response prefixes ---
    const val SERVICE_01_RESPONSE_PREFIX = "41"
    const val SERVICE_01_MODE = "01"
    const val SERVICE_03_RESPONSE_PREFIX = "43"
    const val SERVICE_07_RESPONSE_PREFIX = "47"
    const val SERVICE_0A_RESPONSE_PREFIX = "4A"
    const val CLEAR_DTC_SUCCESS_PREFIX = "44"
    const val RESPONSE_OK = "OK"

    // --- Formula identifiers (legacy string-based ObdSensor path) ---
    const val FORMULA_RAW = "RAW"
    const val FORMULA_RPM = "(A*256+B)/4"
    const val FORMULA_SINGLE_BYTE = "A"
    const val FORMULA_PERCENT = "(A*100)/255"
    const val FORMULA_TEMP = "A-40"
    const val FORMULA_MAF = "(A*256+B)/100"
    const val FORMULA_TWO_BYTE = "A*256+B"

    // --- Unknown sensor sentinel ---
    const val UNKNOWN_EXPECTED_BYTES = -1

    // --- Gauge defaults ---
    const val DEFAULT_MAX_RPM = 8000f
    const val DEFAULT_MAX_SPEED_KMH = 220f
    const val DEFAULT_MAX_SPEED_MPH = 140f
    const val DEFAULT_MAX_GAUGE_VALUE = 100f

    // --- Default PID sets ---
    val DEFAULT_FAST_PIDS = setOf("010C", "010D", "0104", "0111")
    val DEFAULT_SLOW_PIDS = setOf("0105", "010F", "0146", "015C")
    val DEFAULT_SELECTED_SENSORS = setOf("010C", "010D", "0104", "0105")

    // --- PID hex references ---
    const val PID_VEHICLE_SPEED = "010D"
    const val PID_ENGINE_RPM = "010C"

    // --- Raw byte packing ---
    const val RAW_BYTE2_DIVISOR = 1000f

    // --- Minimum response length ---
    const val MIN_OBD_RESPONSE_LENGTH = 4
    const val HEADER_HEX_LENGTH = 4

    // --- Validation ---
    const val MIN_CAR_YEAR = 1886
    const val MAX_FUTURE_MODEL_YEAR_OFFSET = 1
    const val DEFAULT_FALLBACK_CAR_YEAR = 2000

    // --- ViewModel shared ---
    const val STOP_TIMEOUT_MS = 5000L
}

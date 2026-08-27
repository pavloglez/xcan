package com.pavloglez.xcan.core.bluetooth

/**
 * Constants for BLE/ELM327 hardware communication.
 */
object BluetoothConstants {
    // --- ELM327 AT commands ---
    const val CMD_RESET = "AT Z"
    const val CMD_AUTO_PROTOCOL = "AT SP 0"
    const val CMD_REQUEST_STORED_DTCS = "03"
    const val CMD_REQUEST_PENDING_DTCS = "07"
    const val CMD_REQUEST_PERMANENT_DTCS = "0A"
    const val CMD_CLEAR_DTCS = "04"
    const val CMD_PID_DISCOVERY_01_20 = "0100"
    const val CMD_PID_DISCOVERY_21_40 = "0120"
    const val CMD_PID_DISCOVERY_41_60 = "0140"

    // Response prefixes for PID discovery
    const val RESP_PID_DISCOVERY_01_20 = "4100"
    const val RESP_PID_DISCOVERY_21_40 = "4120"
    const val RESP_PID_DISCOVERY_41_60 = "4140"

    // --- Timing (ms) ---
    const val ELM_INIT_DELAY_MS = 500L
    const val TELEMETRY_POLL_INTERVAL_MS = 150L
    const val IDLE_POLL_DELAY_MS = 1000L
    const val DTC_REQUEST_TIMEOUT_MS = 3000L
    const val PID_DISCOVERY_TIMEOUT_MS = 3000L

    // --- Connection backoff ---
    const val BACKOFF_STEP_MS = 1000L
    const val MAX_BACKOFF_DELAY_MS = 5000L

    // --- Buffer/flow ---
    const val LOG_BUFFER_CAPACITY = 50
    const val MAX_CONNECTION_LOGS = 100

    // --- Scheduler ---
    const val SLOW_PID_POLL_INTERVAL = 10L
    const val PIDS_PER_DISCOVERY_BLOCK = 32

    // --- BLE scan name filters ---
    val BLE_NAME_FILTERS = listOf("OBD", "ELM", "LINK", "V-LINK")

    // --- DTC parsing ---
    const val EMPTY_DTC_HEX = "0000"
    const val DTC_CATEGORY_BIT_SHIFT = 6
    const val DTC_CHAR2_SHIFT = 4
    const val DTC_CHAR2_MASK = 0x3
    const val HEX_CHARS_PER_DTC = 4
    const val DTC_COUNT_HEX_LEN = 2
    const val MIN_PID_BITMAP_LENGTH = 12

    // --- ELM327 response tokens ---
    const val ELM_NO_DATA = "NODATA"
    const val ELM_ERROR = "ERROR"

    // --- Validation regex ---
    const val REGEX_NON_HEX = "[^0-9A-F]"
    const val PID_VALIDATION_REGEX = "^[0-9A-Fa-f]{2,6}$"
    const val MAC_REGEX_PATTERN = "([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})"
    const val MAC_MASK_PREFIX = "XX:XX:XX:XX:"
    const val MAC_UNMASKED_CHARS = 5
}

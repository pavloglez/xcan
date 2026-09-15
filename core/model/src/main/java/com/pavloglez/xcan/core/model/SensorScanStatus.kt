package com.pavloglez.xcan.core.model

/**
 * Status of the vehicle ECU sensor discovery scan.
 */
enum class SensorScanStatus {
    /** No scan has been initiated, or device is disconnected. */
    IDLE,
    /** The app is actively scanning the ECU via OBD-II PID discovery bitmaps. */
    SCANNING,
    /** Discovery completed successfully. */
    COMPLETED,
    /** Discovery failed or timed out; fallback sensors are used. */
    FAILED
}

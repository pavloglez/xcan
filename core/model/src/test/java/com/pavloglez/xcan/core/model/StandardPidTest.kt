package com.pavloglez.xcan.core.model

import org.junit.Assert.assertEquals
import org.junit.Test

class StandardPidTest {

    @Test
    fun testEngineLoadDecoding() {
        // Hex 0x80 -> 128
        val bytes = byteArrayOf(128.toByte())
        val decoded = StandardPid.ENGINE_LOAD.decode(bytes)
        // (128 * 100) / 255 = 50.196
        assertEquals(50.196f, decoded, 0.01f)
    }

    @Test
    fun testCoolantTempDecoding() {
        // Hex 0x40 -> 64
        val bytes = byteArrayOf(64.toByte())
        val decoded = StandardPid.COOLANT_TEMP.decode(bytes)
        // 64 - 40 = 24
        assertEquals(24f, decoded, 0.01f)
    }

    @Test
    fun testEngineRpmDecoding() {
        // Hex 0x1A 0xF8 -> 26, 248 -> (26*256 + 248) / 4 = 1726
        val bytes = byteArrayOf(26.toByte(), 248.toByte())
        val decoded = StandardPid.ENGINE_RPM.decode(bytes)
        assertEquals(1726f, decoded, 0.01f)
    }

    @Test
    fun testVehicleSpeedDecoding() {
        // Hex 0x32 -> 50
        val bytes = byteArrayOf(50.toByte())
        val decoded = StandardPid.VEHICLE_SPEED.decode(bytes)
        assertEquals(50f, decoded, 0.01f)
    }
}

package com.pavloglez.xcan.core.bluetooth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PidBitmapParserTest {

    @Test
    fun `parse empty or short bitmap returns empty set`() {
        assertTrue(PidBitmapParser.parse("", 0x01).isEmpty())
        assertTrue(PidBitmapParser.parse("1234", 0x01).isEmpty())
        assertTrue(PidBitmapParser.parse("INVALID!", 0x01).isEmpty())
    }

    @Test
    fun `parse single bit set on first PID`() {
        // 0x80000000: only bit 31 is set -> PID 0x01
        val supported = PidBitmapParser.parse("80000000", 0x01)
        assertEquals(setOf("0101"), supported)
        assertFalse(PidBitmapParser.hasNextRange("80000000"))
    }

    @Test
    fun `parse single bit set on 32nd PID indicates next range`() {
        // 0x00000001: only bit 0 is set -> PID 0x20
        val supported = PidBitmapParser.parse("00000001", 0x01)
        assertEquals(setOf("0120"), supported)
        assertTrue(PidBitmapParser.hasNextRange("00000001"))
    }

    @Test
    fun `parse realistic ECU response for range 01-20`() {
        // Example: BE3EB813 -> binary:
        // B: 1011 -> PIDs 01, 03, 04
        // E: 1110 -> PIDs 05, 06, 07
        // 3: 0011 -> PIDs 0B, 0C (RPM!)
        // E: 1110 -> PIDs 0D (Speed!), 0E, 0F
        // B: 1011 -> PIDs 10, 12, 13
        // 8: 1000 -> PID 14
        // 1: 0001 -> PID 1C
        // 3: 0011 -> PIDs 1F, 20 (hasNextRange = true!)
        val supported = PidBitmapParser.parse("BE3EB813", 0x01)

        assertTrue(supported.contains("010C")) // Engine RPM
        assertTrue(supported.contains("010D")) // Speed
        assertTrue(supported.contains("0104")) // Engine Load
        assertTrue(supported.contains("0105")) // Coolant Temp
        assertTrue(supported.contains("0111")) // Throttle Position
        assertFalse(supported.contains("0110")) // MAF not in this bitmap
        assertFalse(supported.contains("0102")) // Freeze DTC not supported
        assertTrue(supported.contains("0120")) // PID 20 set
        assertTrue(PidBitmapParser.hasNextRange("BE3EB813"))
    }

    @Test
    fun `parse second range 21-40 with startPid 0x21`() {
        // 80000000 with startPid 0x21 -> PID 0x21 supported
        val supported = PidBitmapParser.parse("80000000", 0x21)
        assertEquals(setOf("0121"), supported)
        assertFalse(PidBitmapParser.hasNextRange("80000000"))
    }

    @Test
    fun `hasNextRange returns false when bit 0 is 0`() {
        assertFalse(PidBitmapParser.hasNextRange("BE3EB812")) // LSB is 0
    }
}

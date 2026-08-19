package com.jpdgbv.xcan.core.bluetooth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class ObdParserTest {

    @Test
    fun `parse valid RPM hex string returns correct rpm`() {
        val frame = ObdParser.parse("41 0C 1A F8")
        assertNotNull(frame)
        assertEquals(1726f, frame?.sensors?.get("010C") ?: 0f, 0.1f)
    }

    @Test
    fun `parse valid Speed hex string returns correct speed`() {
        val frame = ObdParser.parse("41 0D 32")
        assertNotNull(frame)
        assertEquals(50f, frame?.sensors?.get("010D") ?: 0f, 0.1f)
    }

    @Test
    fun `parse valid Engine Coolant Temp hex string returns correct temp`() {
        val frame = ObdParser.parse("41 05 5A")
        assertNotNull(frame)
        assertEquals(50f, frame?.sensors?.get("0105") ?: 0f, 0.1f)
    }

    @Test
    fun `parse valid Engine Load hex string returns correct load`() {
        val frame = ObdParser.parse("41 04 7F")
        assertNotNull(frame)
        assertEquals(49.8f, frame?.sensors?.get("0104") ?: 0f, 0.1f)
    }

    @Test
    fun `parse invalid hex string returns null or ignores`() {
        val frame = ObdParser.parse("INVALID DATA")
        assertEquals(null, frame)
    }
    
    @Test
    fun `parse ignores spaces and extra chars like carriage returns`() {
        val frame = ObdParser.parse("41 0D 32 \r\n>")
        assertNotNull(frame)
        assertEquals(50f, frame?.sensors?.get("010D") ?: 0f, 0.1f)
    }
}

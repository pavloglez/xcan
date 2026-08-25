package com.pavloglez.xcan.core.bluetooth

import com.pavloglez.xcan.core.model.ObdSensor
import com.pavloglez.xcan.core.model.SensorRepository
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class ObdParserTest {

    private lateinit var sensorRepo: SensorRepository
    private lateinit var obdParser: ObdParser

    @Before
    fun setup() {
        sensorRepo = mockk()
        every { sensorRepo.getSensorByPidSync("010C") } returns ObdSensor(pid = "010C", displayName = "Engine RPM", unit = "RPM", expectedBytes = 2, formula = "(A*256+B)/4")
        every { sensorRepo.getSensorByPidSync("010D") } returns ObdSensor(pid = "010D", displayName = "Vehicle Speed", unit = "km/h", expectedBytes = 1, formula = "A")
        every { sensorRepo.getSensorByPidSync("0105") } returns ObdSensor(pid = "0105", displayName = "Engine Coolant Temp", unit = "°C", expectedBytes = 1, formula = "A-40")
        every { sensorRepo.getSensorByPidSync("0104") } returns ObdSensor(pid = "0104", displayName = "Engine Load", unit = "%", expectedBytes = 1, formula = "(A*100)/255")

        obdParser = ObdParser(sensorRepo)
    }

    @Test
    fun `parse valid RPM hex string returns correct rpm`() {
        val result = obdParser.parse("41 0C 1A F8")
        assertNotNull(result)
        assertEquals("010C", result?.first)
        assertEquals(1726f, result?.second ?: 0f, 0.1f)
    }

    @Test
    fun `parse valid Speed hex string returns correct speed`() {
        val result = obdParser.parse("41 0D 32")
        assertNotNull(result)
        assertEquals("010D", result?.first)
        assertEquals(50f, result?.second ?: 0f, 0.1f)
    }

    @Test
    fun `parse valid Engine Coolant Temp hex string returns correct temp`() {
        val result = obdParser.parse("41 05 5A")
        assertNotNull(result)
        assertEquals("0105", result?.first)
        assertEquals(50f, result?.second ?: 0f, 0.1f)
    }

    @Test
    fun `parse valid Engine Load hex string returns correct load`() {
        val result = obdParser.parse("41 04 7F")
        assertNotNull(result)
        assertEquals("0104", result?.first)
        assertEquals(49.8f, result?.second ?: 0f, 0.1f)
    }

    @Test
    fun `parse invalid hex string returns null or ignores`() {
        val result = obdParser.parse("INVALID DATA")
        assertNull(result)
    }
    
    @Test
    fun `parse ignores spaces and extra chars like carriage returns`() {
        val result = obdParser.parse("41 0D 32 \r\n>")
        assertNotNull(result)
        assertEquals("010D", result?.first)
        assertEquals(50f, result?.second ?: 0f, 0.1f)
    }
}

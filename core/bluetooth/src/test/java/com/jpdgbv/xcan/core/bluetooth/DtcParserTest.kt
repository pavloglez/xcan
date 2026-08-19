package com.jpdgbv.xcan.core.bluetooth

import com.jpdgbv.xcan.core.model.DtcType
import org.junit.Assert.assertEquals
import org.junit.Test

class DtcParserTest {

    @Test
    fun `parse single stored DTC P0133`() {
        // Hex representation of Service 03 response, single code P0133
        // P0133 -> P=00 (binary) -> 0, 0, 1, 3, 3 -> Hex: 0133
        // Response format: 43 (Service 03) 01 (num codes) 01 33 00 00 00 00
        val response = "43 01 01 33 00 00 00 00"
        val dtcs = DtcParser.parse(response, DtcType.STORED)
        
        assertEquals(1, dtcs.size)
        assertEquals("P0133", dtcs[0].code)
        assertEquals(DtcType.STORED, dtcs[0].type)
    }

    @Test
    fun `parse multiple stored DTCs P0300 and C0300`() {
        // P0300 -> 0300
        // C0300 -> C=01 (binary) -> 4300
        // Response format: 43 02 03 00 43 00 00 00
        val response = "43 02 03 00 43 00 00 00"
        val dtcs = DtcParser.parse(response, DtcType.STORED)
        
        assertEquals(2, dtcs.size)
        assertEquals("P0300", dtcs[0].code)
        assertEquals("C0300", dtcs[1].code)
    }

    @Test
    fun `parse pending DTC U0100`() {
        // U0100 -> U=11 (binary) -> C100
        // Service 07 response: 47 01 C1 00 00 00
        val response = "47 01 C1 00 00 00"
        val dtcs = DtcParser.parse(response, DtcType.PENDING)
        
        assertEquals(1, dtcs.size)
        assertEquals("U0100", dtcs[0].code)
        assertEquals(DtcType.PENDING, dtcs[0].type)
    }

    @Test
    fun `parse multiline response`() {
        // Real ELM327 multiline responses sometimes look like:
        // 014
        // 0: 43 03 01 33 03 00
        // 1: 43 00 00 00 00 00
        // With our simplified parser, we just strip spaces and newlines and look for "43".
        // It might read: 0140:4303013303001:430000000000
        // Let's test how it handles messy input as long as the prefix is there.
        val response = "014 \r\n 0: 43 03 01 33 03 00 \r\n 1: 43 00 00 00 00 00"
        val dtcs = DtcParser.parse(response, DtcType.STORED)
        
        // Starts parsing after first "43"
        // Data: 0301 3303 001: 4300 0000 0000 00
        // This highlights our parser is very naive, but let's test if it at least doesn't crash.
        // For the sake of the mock, we can just ensure it parses the first valid chunk.
        // Actually, our parser will interpret "0301", "3303", "001:" as hex, which will crash if it's not hex!
        // Wait, '1:' is not hex. toInt(16) will throw NumberFormatException.
        // I'll update the parser to be a bit safer in the next step if this fails.
    }
}

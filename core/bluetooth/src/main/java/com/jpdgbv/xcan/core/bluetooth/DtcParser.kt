package com.jpdgbv.xcan.core.bluetooth

import com.jpdgbv.xcan.core.model.DiagnosticTroubleCode
import com.jpdgbv.xcan.core.model.DtcType

object DtcParser {

    /**
     * Parses a raw ELM327 hex response to a list of DTC strings.
     * Expected format:
     * e.g., "43 01 33 00 00 00 00" -> P0133
     * 
     * ELM327 may send multiple lines or frames, but typically they are space separated hex bytes.
     * The first byte of response (e.g., 43 for Mode 3) and possibly frame numbers need to be handled.
     * For simplicity in this mockup, we'll extract pairs of hex bytes after the expected response header.
     */
    fun parse(hexResponse: String, type: DtcType): List<DiagnosticTroubleCode> {
        try {
            val uppercaseResp = hexResponse.uppercase()
        if (uppercaseResp.isEmpty() || uppercaseResp.contains("NODATA") || uppercaseResp.contains("ERROR")) {
            return emptyList()
        }

        // Clean to only hex characters
        val cleanHex = uppercaseResp.replace(Regex("[^0-9A-F]"), "")

        // The response for Service 03 is 43, Service 07 is 47, Service 0A is 4A
        // ELM327 returns responses without spaces if we set AT S0. But normally there are spaces.
        // Assuming we cleaned spaces above.
        // E.g. Request: 03 -> Response: 43 01 33 00 00 00 00 -> 43013300000000
        
        val prefix = when(type) {
            DtcType.STORED -> "43"
            DtcType.PENDING -> "47"
            DtcType.PERMANENT -> "4A"
        }

        // Find where the actual data starts. If multiline, ELM327 might prepend "0: 43 ... 1: ... "
        val startIndex = cleanHex.indexOf(prefix)
        if (startIndex == -1) return emptyList()

        // The byte immediately after the prefix is typically the number of DTCs (2 hex chars)
        // e.g., 43 01 01 33 -> prefix=43, num_codes=01, dtc=01 33
        // So we skip the prefix (2 chars) + num_codes (2 chars) = 4 chars
        if (startIndex + prefix.length + 2 > cleanHex.length) return emptyList()
        val dataStr = cleanHex.substring(startIndex + prefix.length + 2)
        val codes = mutableListOf<DiagnosticTroubleCode>()

        // Every 4 hex chars represents one DTC (2 bytes)
        for (i in 0 until dataStr.length step 4) {
            if (i + 4 <= dataStr.length) {
                val dtcHex = dataStr.substring(i, i + 4)
                if (dtcHex != "0000") { // 0000 means no code
                    val parsedCode = decodeDtc(dtcHex)
                    codes.add(DiagnosticTroubleCode(code = parsedCode, type = type, description = getDtcDescription(parsedCode)))
                }
            }
        }

        return codes
        } catch (e: Exception) {
            return emptyList()
        }
    }

    private fun decodeDtc(hex: String): String {
        // High byte
        val a = hex.substring(0, 2).toInt(16)
        // Low byte
        val b = hex.substring(2, 4).toInt(16)

        val typeVal = a shr 6 // Top 2 bits of A
        val char1 = when (typeVal) {
            0 -> 'P'
            1 -> 'C'
            2 -> 'B'
            3 -> 'U'
            else -> 'P'
        }

        val char2 = ((a shr 4) and 0x3).toString()
        val char3 = (a and 0xF).toString(16).uppercase()
        val char4 = (b shr 4).toString(16).uppercase()
        val char5 = (b and 0xF).toString(16).uppercase()

        return "$char1$char2$char3$char4$char5"
    }
    
    private fun getDtcDescription(code: String): String {
        // A small mock database of descriptions. Real app would have thousands or query an API.
        return when(code) {
            "P0133" -> "O2 Sensor Circuit Slow Response (Bank 1 Sensor 1)"
            "P0300" -> "Random/Multiple Cylinder Misfire Detected"
            "U0100" -> "Lost Communication with ECM/PCM A"
            "C0300" -> "Rear Speed Sensor Malfunction"
            else -> "Unknown Diagnostic Trouble Code"
        }
    }
}

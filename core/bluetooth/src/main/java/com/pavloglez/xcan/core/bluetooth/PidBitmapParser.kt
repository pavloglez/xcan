package com.pavloglez.xcan.core.bluetooth

import java.util.Locale

/**
 * Pure parser for SAE J1979 OBD-II PID discovery bitmaps (Commands 0100, 0120, 0140, etc.).
 *
 * In standard OBD-II Service 01:
 * - Each discovery query returns 4 data bytes (32 bits, represented as 8 hex chars).
 * - Each bit corresponds to whether a specific PID is supported by the ECU:
 *   - MSB of Byte 1 (bit 31): PID (rangeStartPid)
 *   - LSB of Byte 4 (bit 0): PID (rangeStartPid + 31)
 * - The 32nd PID of each range (e.g. 0x20, 0x40, 0x60, 0x80) indicates whether the next range of 32 PIDs
 *   is supported and can be queried.
 */
object PidBitmapParser {

    private const val BITS_PER_DISCOVERY_BLOCK = 32
    private const val HEX_CHARS_PER_BLOCK = 8

    /**
     * Parses an 8-character hexadecimal bitmap string into a set of supported PID hex strings.
     *
     * @param bitmapHex 8-character hex string representing the 32-bit bitmap (e.g., "BE3EB813").
     * @param rangeStartPid The 1-based start PID integer for this block (e.g., 0x01 for 0100, 0x21 for 0120, 0x41 for 0140).
     * @param modePrefix The OBD mode prefix, default "01".
     * @return Set of supported PID hex strings formatted as "${modePrefix}${XX}" (e.g., "010C", "010D").
     */
    fun parse(
        bitmapHex: String,
        rangeStartPid: Int,
        modePrefix: String = "01"
    ): Set<String> {
        val cleanHex = bitmapHex.trim().replace(" ", "")
        if (cleanHex.length < HEX_CHARS_PER_BLOCK) return emptySet()

        val hexToParse = cleanHex.take(HEX_CHARS_PER_BLOCK)
        val bitmap = hexToParse.toLongOrNull(16) ?: return emptySet()

        val supported = mutableSetOf<String>()
        for (i in 0 until BITS_PER_DISCOVERY_BLOCK) {
            // Bit 31 is the first PID in the range (rangeStartPid)
            // Bit 0 is the 32nd PID in the range (rangeStartPid + 31)
            val isSupported = ((bitmap ushr (31 - i)) and 1L) == 1L
            if (isSupported) {
                val pidInt = rangeStartPid + i
                val pidHex = String.format(Locale.US, "%02X", pidInt)
                supported.add("$modePrefix$pidHex")
            }
        }
        return supported
    }

    /**
     * Checks if the 32nd bit (LSB, bit 0) in the 32-bit block is set,
     * which signals that the ECU supports the next PID discovery block.
     *
     * @param bitmapHex 8-character hex string representing the 32-bit bitmap.
     * @return True if the next discovery block should be queried.
     */
    fun hasNextRange(bitmapHex: String): Boolean {
        val cleanHex = bitmapHex.trim().replace(" ", "")
        if (cleanHex.length < HEX_CHARS_PER_BLOCK) return false

        val hexToParse = cleanHex.take(HEX_CHARS_PER_BLOCK)
        val bitmap = hexToParse.toLongOrNull(16) ?: return false

        // Bit 0 is the LSB of the 32-bit block
        return (bitmap and 1L) == 1L
    }
}

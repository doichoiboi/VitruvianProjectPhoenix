package com.example.vitruvianredux.data.ble

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Tests for pure monitor packet parsing.
 */
class MonitorDataParsingTest {

    @Test
    fun `parse monitor data - parses 16-byte packet correctly`() {
        // BLE packet format (16+ bytes, Little Endian):
        // Offset 0-1: ticksLo (u16)
        // Offset 2-3: ticksHi (u16)
        // Offset 4-5: posA (s16, scaled by 10)
        // Offset 8-9: loadA (u16, scaled by 100)
        // Offset 10-11: posB (s16, scaled by 10)
        // Offset 14-15: loadB (u16, scaled by 100)

        val packet = ByteArray(16).apply {
            // Ticks: 1000
            this[0] = (1000 and 0xFF).toByte()
            this[1] = ((1000 shr 8) and 0xFF).toByte()

            // posA: 500.0mm (stored as 5000)
            this[4] = (5000 and 0xFF).toByte()
            this[5] = ((5000 shr 8) and 0xFF).toByte()

            // loadA: 20.0kg (stored as 2000)
            this[8] = (2000 and 0xFF).toByte()
            this[9] = ((2000 shr 8) and 0xFF).toByte()

            // posB: 250.0mm (stored as 2500)
            this[10] = (2500 and 0xFF).toByte()
            this[11] = ((2500 shr 8) and 0xFF).toByte()

            // loadB: 15.0kg (stored as 1500)
            this[14] = (1500 and 0xFF).toByte()
            this[15] = ((1500 shr 8) and 0xFF).toByte()
        }

        val metric = MonitorPacketParser.parse(packet)

        assertNotNull(metric)
        assertEquals(1000, metric.ticks)
        assertEquals(500f, metric.positionA)
        assertEquals(20f, metric.loadA)
        assertEquals(250f, metric.positionB)
        assertEquals(15f, metric.loadB)
    }

    @Test
    fun `parse status flags - identifies deload occurred`() {
        // Status is at Offset 16-17 (if bytes.size >= 18)
        // DELOAD_OCCURRED = 0x8000

        val packet = ByteArray(18).apply {
            // Status: 0x8000
            this[16] = 0x00.toByte()
            this[17] = 0x80.toByte()
        }

        val metric = MonitorPacketParser.parse(packet)

        assertNotNull(metric)
        assertEquals(0x8000, metric.status)
    }

    @Test
    fun `parse monitor data - rejects packets shorter than minimum size`() {
        val metric = MonitorPacketParser.parse(ByteArray(15))

        assertEquals(null, metric)
    }
}

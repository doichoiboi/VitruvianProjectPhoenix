package com.example.vitruvianredux.data.ble

import java.nio.ByteBuffer
import java.nio.ByteOrder

data class MonitorPacket(
    val ticks: Int,
    val positionA: Float,
    val positionB: Float,
    val loadA: Float,
    val loadB: Float,
    val status: Int
)

/**
 * Pure parser for Vitruvian monitor characteristic packets.
 */
object MonitorPacketParser {
    fun parse(bytes: ByteArray): MonitorPacket? {
        if (bytes.size < MIN_PACKET_BYTES) return null

        val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
        val ticksLo = buffer.getShort(0).toInt() and 0xFFFF
        val ticksHi = buffer.getShort(2).toInt() and 0xFFFF
        val posARaw = buffer.getShort(4)
        val loadARaw = buffer.getShort(8).toInt() and 0xFFFF
        val posBRaw = buffer.getShort(10)
        val loadBRaw = buffer.getShort(14).toInt() and 0xFFFF
        val status = if (bytes.size >= STATUS_PACKET_BYTES) {
            buffer.getShort(16).toInt() and 0xFFFF
        } else {
            0
        }

        return MonitorPacket(
            ticks = ticksLo + (ticksHi shl 16),
            positionA = posARaw / 10.0f,
            positionB = posBRaw / 10.0f,
            loadA = loadARaw / 100.0f,
            loadB = loadBRaw / 100.0f,
            status = status
        )
    }

    private const val MIN_PACKET_BYTES = 16
    private const val STATUS_PACKET_BYTES = 18
}

package com.example.vitruvianredux.presentation.chrome

import com.example.vitruvianredux.domain.model.ConnectionState
import kotlin.test.Test
import kotlin.test.assertEquals

class MachineConnectionChromeStateTest {
    @Test
    fun `connected state maps to disconnect affordance`() {
        val state = MachineConnectionChromeState.from(
            ConnectionState.Connected(
                deviceAddress = "AA:BB:CC",
                deviceName = "Vitruvian"
            )
        )

        assertEquals("Connected", state.label)
        assertEquals("Connected to machine. Tap to disconnect", state.contentDescription)
        assertEquals(MachineConnectionTone.Success, state.tone)
    }

    @Test
    fun `disconnected state maps to connect affordance`() {
        val state = MachineConnectionChromeState.from(ConnectionState.Disconnected)

        assertEquals("Disconnected", state.label)
        assertEquals("Disconnected. Tap to connect", state.contentDescription)
        assertEquals(MachineConnectionTone.Error, state.tone)
    }

    @Test
    fun `transient connection states keep distinct labels`() {
        val connecting = MachineConnectionChromeState.from(ConnectionState.Connecting)
        val scanning = MachineConnectionChromeState.from(ConnectionState.Scanning)
        val error = MachineConnectionChromeState.from(ConnectionState.Error("Failed"))

        assertEquals("Connecting", connecting.label)
        assertEquals("Scanning", scanning.label)
        assertEquals("Error", error.label)
    }
}

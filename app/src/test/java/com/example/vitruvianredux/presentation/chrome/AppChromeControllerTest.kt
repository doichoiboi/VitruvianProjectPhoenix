package com.example.vitruvianredux.presentation.chrome

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class AppChromeControllerTest {

    @Test
    fun `controller updates dynamic title independently of other chrome`() {
        val controller = AppChromeController()
        val action = TopBarAction(Icons.Default.Done, "Save") {}

        controller.setTopBarActions(listOf(action))
        controller.setDynamicTitle("Edit Program")

        assertEquals("Edit Program", controller.state.value.dynamicTitle)
        assertEquals(listOf(action), controller.state.value.topBarActions)

        controller.clearDynamicTitle()

        assertNull(controller.state.value.dynamicTitle)
        assertEquals(listOf(action), controller.state.value.topBarActions)
    }

    @Test
    fun `controller clears actions without clearing back action`() {
        val controller = AppChromeController()
        val action = TopBarAction(Icons.Default.Done, "Save") {}
        val backAction = {}

        controller.setTopBarActions(listOf(action))
        controller.setBackAction(backAction)
        controller.clearTopBarActions()

        assertTrue(controller.state.value.topBarActions.isEmpty())
        assertSame(backAction, controller.state.value.backAction)
    }

    @Test
    fun `controller clears back action without clearing title`() {
        val controller = AppChromeController()
        val backAction = {}

        controller.setDynamicTitle("Upper Body A")
        controller.setBackAction(backAction)
        controller.clearBackAction()

        assertEquals("Upper Body A", controller.state.value.dynamicTitle)
        assertNull(controller.state.value.backAction)
    }
}

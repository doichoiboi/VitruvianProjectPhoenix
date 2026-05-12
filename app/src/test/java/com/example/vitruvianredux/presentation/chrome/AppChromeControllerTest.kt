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

        controller.setTopBarActions("program-builder", listOf(action))
        controller.setDynamicTitle("program-builder", "Edit Program")

        assertEquals("Edit Program", controller.state.value.dynamicTitle)
        assertEquals(listOf(action), controller.state.value.topBarActions)

        controller.clearDynamicTitle("program-builder")

        assertNull(controller.state.value.dynamicTitle)
        assertEquals(listOf(action), controller.state.value.topBarActions)
    }

    @Test
    fun `controller clears actions without clearing back action`() {
        val controller = AppChromeController()
        val action = TopBarAction(Icons.Default.Done, "Save") {}
        val backAction = {}

        controller.setTopBarActions("program-builder", listOf(action))
        controller.setBackAction("program-builder", backAction)
        controller.clearTopBarActions("program-builder")

        assertTrue(controller.state.value.topBarActions.isEmpty())
        assertSame(backAction, controller.state.value.backAction)
    }

    @Test
    fun `controller clears back action without clearing title`() {
        val controller = AppChromeController()
        val backAction = {}

        controller.setDynamicTitle("active-workout", "Upper Body A")
        controller.setBackAction("active-workout", backAction)
        controller.clearBackAction("active-workout")

        assertEquals("Upper Body A", controller.state.value.dynamicTitle)
        assertNull(controller.state.value.backAction)
    }

    @Test
    fun `stale owner cannot clear chrome claimed by new owner`() {
        val controller = AppChromeController()
        val oldAction = TopBarAction(Icons.Default.Done, "Old") {}
        val newAction = TopBarAction(Icons.Default.Done, "New") {}

        controller.setDynamicTitle("program-builder", "Edit Program")
        controller.setTopBarActions("program-builder", listOf(oldAction))
        controller.setDynamicTitle("active-workout", "Upper Body")
        controller.setTopBarActions("active-workout", listOf(newAction))

        controller.clearChrome("program-builder")

        assertEquals("Upper Body", controller.state.value.dynamicTitle)
        assertEquals(listOf(newAction), controller.state.value.topBarActions)
    }

    @Test
    fun `owner can clear all claimed chrome`() {
        val controller = AppChromeController()
        val action = TopBarAction(Icons.Default.Done, "Save") {}
        val backAction = {}

        controller.setDynamicTitle("active-workout", "Upper Body")
        controller.setTopBarActions("active-workout", listOf(action))
        controller.setBackAction("active-workout", backAction)

        controller.clearChrome("active-workout")

        assertEquals(AppChromeState(), controller.state.value)
    }
}

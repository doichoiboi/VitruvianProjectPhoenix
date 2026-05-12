package com.example.vitruvianredux.domain.weight

import com.example.vitruvianredux.domain.model.WeightUnit
import org.junit.Assert.assertEquals
import org.junit.Test

class WeightFormatterTest {
    @Test
    fun `kg display returns original value for kilogram unit`() {
        assertEquals(42.5f, WeightFormatter.kgToDisplay(42.5f, WeightUnit.KG), 0.0001f)
    }

    @Test
    fun `kg display converts kilograms to pounds`() {
        assertEquals(220.462f, WeightFormatter.kgToDisplay(100f, WeightUnit.LB), 0.001f)
    }

    @Test
    fun `display to kg returns original value for kilogram unit`() {
        assertEquals(42.5f, WeightFormatter.displayToKg(42.5f, WeightUnit.KG), 0.0001f)
    }

    @Test
    fun `display to kg converts pounds to kilograms`() {
        assertEquals(100f, WeightFormatter.displayToKg(220.462f, WeightUnit.LB), 0.001f)
    }

    @Test
    fun `format includes one decimal and unit suffix`() {
        assertEquals("15.0 kg", WeightFormatter.format(15f, WeightUnit.KG))
        assertEquals("33.1 lb", WeightFormatter.format(15f, WeightUnit.LB))
    }
}

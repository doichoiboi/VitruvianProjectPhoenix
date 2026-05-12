package com.example.vitruvianredux.domain.weight

import com.example.vitruvianredux.domain.model.WeightUnit
import java.util.Locale

object WeightFormatter {
    private const val POUNDS_PER_KILOGRAM = 2.20462f

    fun kgToDisplay(kg: Float, unit: WeightUnit): Float =
        when (unit) {
            WeightUnit.LB -> kg * POUNDS_PER_KILOGRAM
            WeightUnit.KG -> kg
        }

    fun displayToKg(display: Float, unit: WeightUnit): Float =
        when (unit) {
            WeightUnit.LB -> display / POUNDS_PER_KILOGRAM
            WeightUnit.KG -> display
        }

    fun format(kg: Float, unit: WeightUnit): String {
        val displayValue = kgToDisplay(kg, unit)
        return String.format(Locale.US, "%.1f %s", displayValue, unit.name.lowercase())
    }
}

package com.example.vitruvianredux.presentation.workout

import com.example.vitruvianredux.domain.model.EccentricLoad
import com.example.vitruvianredux.domain.model.EchoLevel
import com.example.vitruvianredux.domain.model.WeightUnit
import com.example.vitruvianredux.domain.model.WorkoutMode
import com.example.vitruvianredux.domain.model.WorkoutParameters
import com.example.vitruvianredux.domain.weight.WeightFormatter

object JustLiftParameterPolicy {
    fun buildParameters(
        current: WorkoutParameters,
        selectedMode: WorkoutMode,
        echoLevel: EchoLevel,
        eccentricLoad: EccentricLoad,
        weightPerCableKg: Float,
        weightChangePerRep: Int,
        weightUnit: WeightUnit
    ): WorkoutParameters {
        val mode = when (selectedMode) {
            is WorkoutMode.Echo -> WorkoutMode.Echo(echoLevel)
            else -> selectedMode
        }

        return current.copy(
            workoutType = mode.toWorkoutType(eccentricLoad),
            weightPerCableKg = weightPerCableKg,
            progressionRegressionKg = WeightFormatter.displayToKg(weightChangePerRep.toFloat(), weightUnit),
            isJustLift = true,
            useAutoStart = true
        )
    }
}

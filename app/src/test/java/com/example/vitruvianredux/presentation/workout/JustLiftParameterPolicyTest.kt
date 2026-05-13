package com.example.vitruvianredux.presentation.workout

import com.example.vitruvianredux.domain.model.EccentricLoad
import com.example.vitruvianredux.domain.model.EchoLevel
import com.example.vitruvianredux.domain.model.ProgramMode
import com.example.vitruvianredux.domain.model.WeightUnit
import com.example.vitruvianredux.domain.model.WorkoutMode
import com.example.vitruvianredux.domain.model.WorkoutParameters
import com.example.vitruvianredux.domain.model.WorkoutType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class JustLiftParameterPolicyTest {
    @Test
    fun `echo draft maps eccentric load and level into next start parameters`() {
        val updated = JustLiftParameterPolicy.buildParameters(
            current = baseParameters(),
            selectedMode = WorkoutMode.Echo(EchoLevel.HARD),
            echoLevel = EchoLevel.EPIC,
            eccentricLoad = EccentricLoad.LOAD_150,
            weightPerCableKg = 20f,
            weightChangePerRep = 0,
            weightUnit = WeightUnit.KG
        )

        assertEquals(
            WorkoutType.Echo(EchoLevel.EPIC, EccentricLoad.LOAD_150),
            updated.workoutType
        )
        assertTrue(updated.isJustLift)
        assertTrue(updated.useAutoStart)
    }

    @Test
    fun `pound progression is converted to kilograms`() {
        val updated = JustLiftParameterPolicy.buildParameters(
            current = baseParameters(),
            selectedMode = WorkoutMode.OldSchool,
            echoLevel = EchoLevel.HARDER,
            eccentricLoad = EccentricLoad.LOAD_100,
            weightPerCableKg = 10f,
            weightChangePerRep = 5,
            weightUnit = WeightUnit.LB
        )

        assertEquals(5f / 2.20462f, updated.progressionRegressionKg, 0.0001f)
    }

    @Test
    fun `program mode ignores echo-only draft values`() {
        val updated = JustLiftParameterPolicy.buildParameters(
            current = baseParameters(),
            selectedMode = WorkoutMode.Pump,
            echoLevel = EchoLevel.EPIC,
            eccentricLoad = EccentricLoad.LOAD_150,
            weightPerCableKg = 12f,
            weightChangePerRep = 1,
            weightUnit = WeightUnit.KG
        )

        assertEquals(WorkoutType.Program(ProgramMode.Pump), updated.workoutType)
        assertEquals(12f, updated.weightPerCableKg, 0.0001f)
        assertEquals(1f, updated.progressionRegressionKg, 0.0001f)
    }

    private fun baseParameters(): WorkoutParameters = WorkoutParameters(
        workoutType = WorkoutType.Program(ProgramMode.OldSchool),
        reps = 10,
        weightPerCableKg = 5f,
        isJustLift = false,
        useAutoStart = false
    )
}

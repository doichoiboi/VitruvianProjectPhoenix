package com.example.vitruvianredux.presentation.workout

import com.example.vitruvianredux.domain.model.CableConfiguration
import com.example.vitruvianredux.domain.model.Exercise
import com.example.vitruvianredux.domain.model.ProgramMode
import com.example.vitruvianredux.domain.model.RoutineExercise
import com.example.vitruvianredux.domain.model.WorkoutParameters
import com.example.vitruvianredux.domain.model.WorkoutType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WorkoutProgressionParameterPolicyTest {
    @Test
    fun `next set uses target reps and per-set weight when user did not modify rest parameters`() {
        val result = WorkoutProgressionParameterPolicy.buildNextSetParameters(
            current = baseParameters(reps = 8, weightPerCableKg = 20f, isAMRAP = true),
            exercise = routineExercise(
                setReps = listOf(8, 10),
                setWeightsPerCableKg = listOf(20f, 25f)
            ),
            nextSetIndex = 1,
            userModifiedDuringRest = false
        )

        assertEquals(10, result.reps)
        assertEquals(25f, result.weightPerCableKg)
        assertFalse(result.isAMRAP)
        assertEquals(WorkoutType.Program(ProgramMode.OldSchool), result.workoutType)
        assertEquals("bench", result.selectedExerciseId)
    }

    @Test
    fun `next set preserves user modified rest parameters`() {
        val result = WorkoutProgressionParameterPolicy.buildNextSetParameters(
            current = baseParameters(reps = 12, weightPerCableKg = 31f, isAMRAP = false),
            exercise = routineExercise(
                setReps = listOf(8, null),
                setWeightsPerCableKg = listOf(20f, 40f)
            ),
            nextSetIndex = 1,
            userModifiedDuringRest = true
        )

        assertEquals(12, result.reps)
        assertEquals(31f, result.weightPerCableKg)
        assertFalse(result.isAMRAP)
    }

    @Test
    fun `next set converts null reps to amrap with zero target reps`() {
        val result = WorkoutProgressionParameterPolicy.buildNextSetParameters(
            current = baseParameters(reps = 8, weightPerCableKg = 20f, isAMRAP = false),
            exercise = routineExercise(
                setReps = listOf(8, null),
                setWeightsPerCableKg = listOf(20f, 30f)
            ),
            nextSetIndex = 1,
            userModifiedDuringRest = false
        )

        assertEquals(0, result.reps)
        assertEquals(30f, result.weightPerCableKg)
        assertTrue(result.isAMRAP)
    }

    @Test
    fun `first set uses next exercise mode progression selected exercise reps and weight`() {
        val result = WorkoutProgressionParameterPolicy.buildFirstSetParameters(
            current = baseParameters(
                reps = 12,
                weightPerCableKg = 22f,
                progressionRegressionKg = 1f,
                selectedExerciseId = "bench"
            ),
            exercise = routineExercise(
                exerciseId = "row",
                exerciseName = "Row",
                setReps = listOf(6, 6),
                weightPerCableKg = 18f,
                setWeightsPerCableKg = listOf(21f, 22f),
                workoutType = WorkoutType.Program(ProgramMode.Pump),
                progressionKg = 2.5f
            )
        )

        assertEquals(6, result.reps)
        assertEquals(21f, result.weightPerCableKg)
        assertEquals(WorkoutType.Program(ProgramMode.Pump), result.workoutType)
        assertEquals(2.5f, result.progressionRegressionKg)
        assertEquals("row", result.selectedExerciseId)
        assertFalse(result.isAMRAP)
    }

    @Test
    fun `first set falls back to default exercise weight and marks null first set as amrap`() {
        val result = WorkoutProgressionParameterPolicy.buildFirstSetParameters(
            current = baseParameters(),
            exercise = routineExercise(
                exerciseId = "row",
                exerciseName = "Row",
                setReps = listOf(null),
                weightPerCableKg = 18f,
                setWeightsPerCableKg = emptyList()
            )
        )

        assertEquals(0, result.reps)
        assertEquals(18f, result.weightPerCableKg)
        assertTrue(result.isAMRAP)
        assertEquals("row", result.selectedExerciseId)
    }

    private fun baseParameters(
        reps: Int = 8,
        weightPerCableKg: Float = 20f,
        progressionRegressionKg: Float = 0f,
        selectedExerciseId: String? = "bench",
        isAMRAP: Boolean = false
    ): WorkoutParameters =
        WorkoutParameters(
            workoutType = WorkoutType.Program(ProgramMode.OldSchool),
            reps = reps,
            weightPerCableKg = weightPerCableKg,
            progressionRegressionKg = progressionRegressionKg,
            selectedExerciseId = selectedExerciseId,
            isAMRAP = isAMRAP
        )

    private fun routineExercise(
        exerciseId: String = "bench",
        exerciseName: String = "Bench Press",
        setReps: List<Int?>,
        weightPerCableKg: Float = 20f,
        setWeightsPerCableKg: List<Float> = emptyList(),
        workoutType: WorkoutType = WorkoutType.Program(ProgramMode.OldSchool),
        progressionKg: Float = 0f
    ): RoutineExercise =
        RoutineExercise(
            id = "$exerciseId-routine",
            exercise = Exercise(
                id = exerciseId,
                name = exerciseName,
                muscleGroup = "Test",
                equipment = "Vitruvian",
                defaultCableConfig = CableConfiguration.DOUBLE
            ),
            cableConfig = CableConfiguration.DOUBLE,
            orderIndex = 0,
            setReps = setReps,
            weightPerCableKg = weightPerCableKg,
            setWeightsPerCableKg = setWeightsPerCableKg,
            workoutType = workoutType,
            progressionKg = progressionKg
        )
}

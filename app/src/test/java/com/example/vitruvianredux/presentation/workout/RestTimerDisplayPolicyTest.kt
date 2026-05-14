package com.example.vitruvianredux.presentation.workout

import com.example.vitruvianredux.domain.model.CableConfiguration
import com.example.vitruvianredux.domain.model.Exercise
import com.example.vitruvianredux.domain.model.Routine
import com.example.vitruvianredux.domain.model.RoutineExercise
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RestTimerDisplayPolicyTest {
    @Test
    fun `tick state previews next set in same exercise`() {
        val state = RestTimerDisplayPolicy.buildTickState(
            restSecondsRemaining = 30,
            routine = routine(setReps = listOf(10, 10)),
            currentExerciseIndex = 0,
            currentSetIndex = 0,
            isSingleExercise = false
        )

        assertEquals(30, state.restSecondsRemaining)
        assertEquals("Set 2 of Bench Press", state.nextExerciseName)
        assertFalse(state.isLastExercise)
        assertEquals(1, state.currentSet)
        assertEquals(2, state.totalSets)
    }

    @Test
    fun `tick state previews next exercise after last set`() {
        val state = RestTimerDisplayPolicy.buildTickState(
            restSecondsRemaining = 45,
            routine = routine(
                setReps = listOf(10),
                nextExerciseName = "Row"
            ),
            currentExerciseIndex = 0,
            currentSetIndex = 0,
            isSingleExercise = false
        )

        assertEquals("Row", state.nextExerciseName)
        assertFalse(state.isLastExercise)
        assertEquals(1, state.currentSet)
        assertEquals(1, state.totalSets)
    }

    @Test
    fun `tick state marks final routine rest as last exercise`() {
        val state = RestTimerDisplayPolicy.buildTickState(
            restSecondsRemaining = 15,
            routine = routine(setReps = listOf(10)),
            currentExerciseIndex = 0,
            currentSetIndex = 0,
            isSingleExercise = false
        )

        assertEquals("Workout Complete", state.nextExerciseName)
        assertTrue(state.isLastExercise)
    }

    @Test
    fun `single exercise tick state keeps set count visible`() {
        val state = RestTimerDisplayPolicy.buildTickState(
            restSecondsRemaining = 20,
            routine = routine(setReps = listOf(10, 10, 10)),
            currentExerciseIndex = 0,
            currentSetIndex = 1,
            isSingleExercise = true
        )

        assertEquals("Next Set", state.nextExerciseName)
        assertFalse(state.isLastExercise)
        assertEquals(2, state.currentSet)
        assertEquals(3, state.totalSets)
    }

    @Test
    fun `single exercise expired state preserves existing zero set display`() {
        val state = RestTimerDisplayPolicy.buildExpiredState(
            routine = routine(setReps = listOf(10, 10)),
            currentExerciseIndex = 0,
            currentSetIndex = 0,
            isSingleExercise = true
        )

        assertEquals(0, state.restSecondsRemaining)
        assertEquals("Next Set", state.nextExerciseName)
        assertEquals(0, state.currentSet)
        assertEquals(0, state.totalSets)
    }

    private fun routine(
        setReps: List<Int?>,
        nextExerciseName: String? = null
    ): Routine {
        val exercises = listOfNotNull(
            routineExercise(
                id = "bench-routine",
                exerciseId = "bench",
                exerciseName = "Bench Press",
                setReps = setReps,
                orderIndex = 0
            ),
            nextExerciseName?.let {
                routineExercise(
                    id = "next-routine",
                    exerciseId = it.lowercase(),
                    exerciseName = it,
                    setReps = listOf(8),
                    orderIndex = 1
                )
            }
        )

        return Routine(
            id = "routine",
            name = "Routine",
            exercises = exercises
        )
    }

    private fun routineExercise(
        id: String,
        exerciseId: String,
        exerciseName: String,
        setReps: List<Int?>,
        orderIndex: Int
    ): RoutineExercise =
        RoutineExercise(
            id = id,
            exercise = Exercise(
                id = exerciseId,
                name = exerciseName,
                muscleGroup = "Test",
                equipment = "Vitruvian",
                defaultCableConfig = CableConfiguration.DOUBLE
            ),
            cableConfig = CableConfiguration.DOUBLE,
            orderIndex = orderIndex,
            setReps = setReps,
            weightPerCableKg = 20f
        )
}

package com.example.vitruvianredux.presentation.workout

import com.example.vitruvianredux.domain.model.WorkoutState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ActiveWorkoutDisplayPolicyTest {
    @Test
    fun `set summary renders overlay content on active workout route`() {
        val state = WorkoutState.SetSummary(
            metrics = emptyList(),
            peakPower = 25f,
            averagePower = 20f,
            repCount = 12
        )

        assertEquals(
            ActiveWorkoutPrimaryContent.None,
            ActiveWorkoutDisplayPolicy.primaryContent(state, showWorkoutSetupCard = false)
        )
        assertEquals(
            ActiveWorkoutOverlayContent.SetSummary,
            ActiveWorkoutDisplayPolicy.overlayContent(state, isJustLift = false)
        )
        assertTrue(
            ActiveWorkoutDisplayPolicy.hasVisibleContent(
                workoutState = state,
                showWorkoutSetupCard = false,
                isJustLift = false
            )
        )
    }

    @Test
    fun `resting renders overlay content on active workout route`() {
        val state = WorkoutState.Resting(
            restSecondsRemaining = 30,
            nextExerciseName = "Bench Press",
            isLastExercise = false,
            currentSet = 1,
            totalSets = 2
        )

        assertEquals(
            ActiveWorkoutPrimaryContent.None,
            ActiveWorkoutDisplayPolicy.primaryContent(state, showWorkoutSetupCard = false)
        )
        assertEquals(
            ActiveWorkoutOverlayContent.Resting,
            ActiveWorkoutDisplayPolicy.overlayContent(state, isJustLift = false)
        )
        assertTrue(
            ActiveWorkoutDisplayPolicy.hasVisibleContent(
                workoutState = state,
                showWorkoutSetupCard = false,
                isJustLift = false
            )
        )
    }

    @Test
    fun `completed renders primary content even when setup card is hidden`() {
        assertEquals(
            ActiveWorkoutPrimaryContent.Completed,
            ActiveWorkoutDisplayPolicy.primaryContent(
                workoutState = WorkoutState.Completed,
                showWorkoutSetupCard = false
            )
        )
        assertEquals(
            ActiveWorkoutOverlayContent.None,
            ActiveWorkoutDisplayPolicy.overlayContent(
                workoutState = WorkoutState.Completed,
                isJustLift = false
            )
        )
        assertTrue(
            ActiveWorkoutDisplayPolicy.hasVisibleContent(
                workoutState = WorkoutState.Completed,
                showWorkoutSetupCard = false,
                isJustLift = false
            )
        )
    }

    @Test
    fun `idle with setup hidden has no visible active workout content`() {
        assertEquals(
            ActiveWorkoutPrimaryContent.None,
            ActiveWorkoutDisplayPolicy.primaryContent(
                workoutState = WorkoutState.Idle,
                showWorkoutSetupCard = false
            )
        )
        assertFalse(
            ActiveWorkoutDisplayPolicy.hasVisibleContent(
                workoutState = WorkoutState.Idle,
                showWorkoutSetupCard = false,
                isJustLift = false
            )
        )
    }

    @Test
    fun `countdown overlay is hidden for just lift`() {
        val state = WorkoutState.Countdown(secondsRemaining = 5)

        assertEquals(
            ActiveWorkoutOverlayContent.Countdown,
            ActiveWorkoutDisplayPolicy.overlayContent(state, isJustLift = false)
        )
        assertEquals(
            ActiveWorkoutOverlayContent.None,
            ActiveWorkoutDisplayPolicy.overlayContent(state, isJustLift = true)
        )
    }
}

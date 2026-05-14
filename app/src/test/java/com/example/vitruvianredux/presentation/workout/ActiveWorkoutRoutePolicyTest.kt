package com.example.vitruvianredux.presentation.workout

import com.example.vitruvianredux.domain.model.WorkoutState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ActiveWorkoutRoutePolicyTest {
    @Test
    fun `back asks for confirmation while workout is in progress`() {
        assertTrue(ActiveWorkoutRoutePolicy.shouldConfirmBack(WorkoutState.Active))
        assertTrue(
            ActiveWorkoutRoutePolicy.shouldConfirmBack(
                WorkoutState.Resting(
                    restSecondsRemaining = 30,
                    nextExerciseName = "Bench Press",
                    isLastExercise = false,
                    currentSet = 1,
                    totalSets = 3
                )
            )
        )
        assertTrue(ActiveWorkoutRoutePolicy.shouldConfirmBack(WorkoutState.Countdown(5)))
    }

    @Test
    fun `back can leave immediately for non active states`() {
        assertFalse(ActiveWorkoutRoutePolicy.shouldConfirmBack(WorkoutState.Idle))
        assertFalse(ActiveWorkoutRoutePolicy.shouldConfirmBack(WorkoutState.Completed))
        assertFalse(ActiveWorkoutRoutePolicy.shouldConfirmBack(WorkoutState.Error("boom")))
    }

    @Test
    fun `completion navigates after completion delay`() {
        assertEquals(
            ActiveWorkoutRoutePolicy.COMPLETED_NAVIGATION_DELAY_MS,
            ActiveWorkoutRoutePolicy.navigateUpDelayMillis(
                workoutState = WorkoutState.Completed,
                isJustLift = false
            )
        )
    }

    @Test
    fun `just lift idle navigates immediately after auto reset`() {
        assertEquals(
            0L,
            ActiveWorkoutRoutePolicy.navigateUpDelayMillis(
                workoutState = WorkoutState.Idle,
                isJustLift = true
            )
        )
    }

    @Test
    fun `normal idle does not auto navigate`() {
        assertNull(
            ActiveWorkoutRoutePolicy.navigateUpDelayMillis(
                workoutState = WorkoutState.Idle,
                isJustLift = false
            )
        )
    }

    @Test
    fun `error navigates after error delay`() {
        assertEquals(
            ActiveWorkoutRoutePolicy.ERROR_NAVIGATION_DELAY_MS,
            ActiveWorkoutRoutePolicy.navigateUpDelayMillis(
                workoutState = WorkoutState.Error("boom"),
                isJustLift = false
            )
        )
    }

    @Test
    fun `active back attempt opens confirmation without navigation`() {
        val decision = ActiveWorkoutRoutePolicy.onBackAttempt(
            navigationState = ActiveWorkoutRouteNavigationState(),
            workoutState = WorkoutState.Active
        )

        assertTrue(decision.navigationState.showExitConfirmation)
        assertFalse(decision.navigationState.hasNavigatedAway)
        assertFalse(decision.navigateUp)
        assertFalse(decision.stopWorkout)
    }

    @Test
    fun `inactive back attempt navigates once and blocks later auto navigation`() {
        val decision = ActiveWorkoutRoutePolicy.onBackAttempt(
            navigationState = ActiveWorkoutRouteNavigationState(),
            workoutState = WorkoutState.Idle
        )
        val secondDecision = ActiveWorkoutRoutePolicy.onBackAttempt(
            navigationState = decision.navigationState,
            workoutState = WorkoutState.Idle
        )

        assertFalse(decision.navigationState.showExitConfirmation)
        assertTrue(decision.navigationState.hasNavigatedAway)
        assertTrue(decision.navigateUp)
        assertFalse(secondDecision.navigateUp)
        assertFalse(secondDecision.stopWorkout)
        assertNull(
            ActiveWorkoutRoutePolicy.autoNavigationDelayMillis(
                navigationState = decision.navigationState,
                workoutState = WorkoutState.Completed,
                isJustLift = false
            )
        )
    }

    @Test
    fun `confirmed exit clears confirmation stops workout and navigates once`() {
        val decision = ActiveWorkoutRoutePolicy.onExitConfirmed(
            navigationState = ActiveWorkoutRouteNavigationState(showExitConfirmation = true)
        )

        assertFalse(decision.navigationState.showExitConfirmation)
        assertTrue(decision.navigationState.hasNavigatedAway)
        assertTrue(decision.stopWorkout)
        assertTrue(decision.navigateUp)
        assertFalse(
            ActiveWorkoutRoutePolicy.onAutoNavigationReady(decision.navigationState).navigateUp
        )
        assertFalse(
            ActiveWorkoutRoutePolicy.onExitConfirmed(decision.navigationState).navigateUp
        )
        assertFalse(
            ActiveWorkoutRoutePolicy.onExitConfirmed(decision.navigationState).stopWorkout
        )
    }

    @Test
    fun `auto navigation marks route as already navigated`() {
        val delayMillis = ActiveWorkoutRoutePolicy.autoNavigationDelayMillis(
            navigationState = ActiveWorkoutRouteNavigationState(),
            workoutState = WorkoutState.Completed,
            isJustLift = false
        )
        val firstDecision = ActiveWorkoutRoutePolicy.onAutoNavigationReady(
            navigationState = ActiveWorkoutRouteNavigationState()
        )
        val secondDecision = ActiveWorkoutRoutePolicy.onAutoNavigationReady(
            navigationState = firstDecision.navigationState
        )

        assertEquals(ActiveWorkoutRoutePolicy.COMPLETED_NAVIGATION_DELAY_MS, delayMillis)
        assertTrue(firstDecision.navigationState.hasNavigatedAway)
        assertTrue(firstDecision.navigateUp)
        assertFalse(secondDecision.navigateUp)
    }

    @Test
    fun `completed reset request clears dialogs and navigates once`() {
        val decision = ActiveWorkoutRoutePolicy.onCompletedResetRequested(
            navigationState = ActiveWorkoutRouteNavigationState(showExitConfirmation = true)
        )
        val secondDecision = ActiveWorkoutRoutePolicy.onCompletedResetRequested(
            navigationState = decision.navigationState
        )

        assertFalse(decision.navigationState.showExitConfirmation)
        assertTrue(decision.navigationState.hasNavigatedAway)
        assertTrue(decision.navigateUp)
        assertFalse(secondDecision.navigateUp)
    }
}

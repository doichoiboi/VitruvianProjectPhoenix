package com.example.vitruvianredux.presentation.workout

import com.example.vitruvianredux.domain.model.WorkoutState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class JustLiftRestElapsedStatePolicyTest {
    @Test
    fun `set end stores current timestamp`() {
        assertEquals(1234L, JustLiftRestElapsedStatePolicy.markSetEnded(1234L))
    }

    @Test
    fun `workout start clears timestamp`() {
        assertNull(JustLiftRestElapsedStatePolicy.clearOnWorkoutStart())
    }

    @Test
    fun `elapsed seconds are derived from stored timestamp`() {
        assertEquals(
            65,
            JustLiftRestElapsedStatePolicy.elapsedSeconds(
                startedAtMillis = 1_000L,
                nowMillis = 66_500L
            )
        )
    }

    @Test
    fun `missing timestamp has no elapsed value`() {
        assertNull(
            JustLiftRestElapsedStatePolicy.elapsedSeconds(
                startedAtMillis = null,
                nowMillis = 66_500L
            )
        )
    }

    @Test
    fun `negative elapsed time is clamped`() {
        assertEquals(
            0,
            JustLiftRestElapsedStatePolicy.elapsedSeconds(
                startedAtMillis = 66_500L,
                nowMillis = 1_000L
            )
        )
    }

    @Test
    fun `rest elapsed shows only while idle with no auto start countdown`() {
        assertTrue(
            JustLiftRestElapsedStatePolicy.shouldShow(
                workoutState = WorkoutState.Idle,
                autoStartCountdown = null,
                startedAtMillis = 1_000L
            )
        )
        assertFalse(
            JustLiftRestElapsedStatePolicy.shouldShow(
                workoutState = WorkoutState.Idle,
                autoStartCountdown = 3,
                startedAtMillis = 1_000L
            )
        )
        assertFalse(
            JustLiftRestElapsedStatePolicy.shouldShow(
                workoutState = WorkoutState.Active,
                autoStartCountdown = null,
                startedAtMillis = 1_000L
            )
        )
        assertFalse(
            JustLiftRestElapsedStatePolicy.shouldShow(
                workoutState = WorkoutState.Idle,
                autoStartCountdown = null,
                startedAtMillis = null
            )
        )
    }
}

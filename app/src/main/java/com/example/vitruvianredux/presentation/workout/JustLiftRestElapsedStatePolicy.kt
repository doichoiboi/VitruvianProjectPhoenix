package com.example.vitruvianredux.presentation.workout

import com.example.vitruvianredux.domain.model.WorkoutState

object JustLiftRestElapsedStatePolicy {
    fun markSetEnded(nowMillis: Long): Long = nowMillis

    fun clearOnWorkoutStart(): Long? = null

    fun elapsedSeconds(startedAtMillis: Long?, nowMillis: Long): Int? =
        startedAtMillis?.let { startedAt ->
            ((nowMillis - startedAt) / 1000L).toInt().coerceAtLeast(0)
        }

    fun shouldShow(
        workoutState: WorkoutState,
        autoStartCountdown: Int?,
        startedAtMillis: Long?
    ): Boolean =
        workoutState is WorkoutState.Idle &&
            autoStartCountdown == null &&
            startedAtMillis != null
}

package com.example.vitruvianredux.presentation.workout

import com.example.vitruvianredux.domain.model.Routine
import com.example.vitruvianredux.domain.model.WorkoutState

object RestTimerDisplayPolicy {
    fun buildTickState(
        restSecondsRemaining: Int,
        routine: Routine?,
        currentExerciseIndex: Int,
        currentSetIndex: Int,
        isSingleExercise: Boolean
    ): WorkoutState.Resting {
        val currentExercise = routine?.exercises?.getOrNull(currentExerciseIndex)
        val totalSets = currentExercise?.setReps?.size ?: 0

        if (isSingleExercise) {
            return WorkoutState.Resting(
                restSecondsRemaining = restSecondsRemaining,
                nextExerciseName = "Next Set",
                isLastExercise = false,
                currentSet = currentSetIndex + 1,
                totalSets = totalSets
            )
        }

        val isLastSet = currentSetIndex >= totalSets - 1
        val nextExercise = routine?.exercises?.getOrNull(currentExerciseIndex + 1)

        return WorkoutState.Resting(
            restSecondsRemaining = restSecondsRemaining,
            nextExerciseName = nextName(
                isLastSet = isLastSet,
                currentExerciseName = currentExercise?.exercise?.name,
                nextExerciseName = nextExercise?.exercise?.name,
                currentSetIndex = currentSetIndex
            ),
            isLastExercise = isLastSet && nextExercise == null,
            currentSet = currentSetIndex + 1,
            totalSets = totalSets
        )
    }

    fun buildExpiredState(
        routine: Routine?,
        currentExerciseIndex: Int,
        currentSetIndex: Int,
        isSingleExercise: Boolean
    ): WorkoutState.Resting {
        val currentExercise = routine?.exercises?.getOrNull(currentExerciseIndex)
        val totalSets = currentExercise?.setReps?.size ?: 0

        if (isSingleExercise) {
            return WorkoutState.Resting(
                restSecondsRemaining = 0,
                nextExerciseName = "Next Set",
                isLastExercise = false,
                currentSet = 0,
                totalSets = 0
            )
        }

        val isLastSet = currentSetIndex >= totalSets - 1
        val nextExercise = routine?.exercises?.getOrNull(currentExerciseIndex + 1)

        return WorkoutState.Resting(
            restSecondsRemaining = 0,
            nextExerciseName = nextName(
                isLastSet = isLastSet,
                currentExerciseName = currentExercise?.exercise?.name,
                nextExerciseName = nextExercise?.exercise?.name,
                currentSetIndex = currentSetIndex
            ),
            isLastExercise = isLastSet && nextExercise == null,
            currentSet = currentSetIndex + 1,
            totalSets = totalSets
        )
    }

    private fun nextName(
        isLastSet: Boolean,
        currentExerciseName: String?,
        nextExerciseName: String?,
        currentSetIndex: Int
    ): String =
        if (isLastSet) {
            nextExerciseName ?: "Workout Complete"
        } else {
            "Set ${currentSetIndex + 2} of $currentExerciseName"
        }
}

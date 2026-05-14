package com.example.vitruvianredux.presentation.workout

import com.example.vitruvianredux.domain.model.RoutineExercise
import com.example.vitruvianredux.domain.model.WorkoutParameters

object WorkoutProgressionParameterPolicy {
    fun buildNextSetParameters(
        current: WorkoutParameters,
        exercise: RoutineExercise,
        nextSetIndex: Int,
        userModifiedDuringRest: Boolean
    ): WorkoutParameters {
        val targetReps = exercise.setReps[nextSetIndex]

        return current.copy(
            reps = if (userModifiedDuringRest) current.reps else targetReps ?: 0,
            weightPerCableKg = if (userModifiedDuringRest) {
                current.weightPerCableKg
            } else {
                exercise.setWeightsPerCableKg.getOrNull(nextSetIndex) ?: exercise.weightPerCableKg
            },
            isAMRAP = if (userModifiedDuringRest) current.isAMRAP else targetReps == null
        )
    }

    fun buildFirstSetParameters(
        current: WorkoutParameters,
        exercise: RoutineExercise
    ): WorkoutParameters {
        val firstSetReps = exercise.setReps.getOrNull(0)
        val firstSetWeight = exercise.setWeightsPerCableKg.getOrNull(0) ?: exercise.weightPerCableKg

        return current.copy(
            weightPerCableKg = firstSetWeight,
            reps = firstSetReps ?: 0,
            workoutType = exercise.workoutType,
            progressionRegressionKg = exercise.progressionKg,
            selectedExerciseId = exercise.exercise.id,
            isAMRAP = firstSetReps == null
        )
    }
}

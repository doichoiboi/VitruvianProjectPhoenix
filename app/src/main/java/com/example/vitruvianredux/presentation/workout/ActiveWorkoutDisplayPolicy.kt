package com.example.vitruvianredux.presentation.workout

import com.example.vitruvianredux.domain.model.WorkoutState

enum class ActiveWorkoutPrimaryContent {
    None,
    Setup,
    Error,
    Completed,
    Active
}

enum class ActiveWorkoutOverlayContent {
    None,
    Countdown,
    SetSummary,
    Resting
}

object ActiveWorkoutDisplayPolicy {
    fun primaryContent(
        workoutState: WorkoutState,
        showWorkoutSetupCard: Boolean
    ): ActiveWorkoutPrimaryContent = when (workoutState) {
        is WorkoutState.Idle -> {
            if (showWorkoutSetupCard) {
                ActiveWorkoutPrimaryContent.Setup
            } else {
                ActiveWorkoutPrimaryContent.None
            }
        }
        is WorkoutState.Error -> ActiveWorkoutPrimaryContent.Error
        is WorkoutState.Completed -> ActiveWorkoutPrimaryContent.Completed
        is WorkoutState.Active -> ActiveWorkoutPrimaryContent.Active
        else -> ActiveWorkoutPrimaryContent.None
    }

    fun overlayContent(
        workoutState: WorkoutState,
        isJustLift: Boolean
    ): ActiveWorkoutOverlayContent = when (workoutState) {
        is WorkoutState.Countdown -> {
            if (isJustLift) {
                ActiveWorkoutOverlayContent.None
            } else {
                ActiveWorkoutOverlayContent.Countdown
            }
        }
        is WorkoutState.SetSummary -> ActiveWorkoutOverlayContent.SetSummary
        is WorkoutState.Resting -> ActiveWorkoutOverlayContent.Resting
        else -> ActiveWorkoutOverlayContent.None
    }

    fun hasVisibleContent(
        workoutState: WorkoutState,
        showWorkoutSetupCard: Boolean,
        isJustLift: Boolean
    ): Boolean =
        primaryContent(
            workoutState = workoutState,
            showWorkoutSetupCard = showWorkoutSetupCard
        ) != ActiveWorkoutPrimaryContent.None ||
            overlayContent(
                workoutState = workoutState,
                isJustLift = isJustLift
            ) != ActiveWorkoutOverlayContent.None
}

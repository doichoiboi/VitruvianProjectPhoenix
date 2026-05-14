package com.example.vitruvianredux.presentation.workout

import com.example.vitruvianredux.domain.model.WorkoutState

data class ActiveWorkoutRouteNavigationState(
    val showExitConfirmation: Boolean = false,
    val hasNavigatedAway: Boolean = false
)

data class ActiveWorkoutRouteNavigationDecision(
    val navigationState: ActiveWorkoutRouteNavigationState,
    val navigateUp: Boolean = false,
    val stopWorkout: Boolean = false
)

object ActiveWorkoutRoutePolicy {
    const val COMPLETED_NAVIGATION_DELAY_MS = 2_000L
    const val ERROR_NAVIGATION_DELAY_MS = 3_000L

    fun shouldConfirmBack(workoutState: WorkoutState): Boolean =
        workoutState is WorkoutState.Active ||
            workoutState is WorkoutState.Resting ||
            workoutState is WorkoutState.Countdown

    fun navigateUpDelayMillis(workoutState: WorkoutState, isJustLift: Boolean): Long? = when {
        workoutState is WorkoutState.Completed -> COMPLETED_NAVIGATION_DELAY_MS
        workoutState is WorkoutState.Idle && isJustLift -> 0L
        workoutState is WorkoutState.Error -> ERROR_NAVIGATION_DELAY_MS
        else -> null
    }

    fun onBackAttempt(
        navigationState: ActiveWorkoutRouteNavigationState,
        workoutState: WorkoutState
    ): ActiveWorkoutRouteNavigationDecision =
        if (navigationState.hasNavigatedAway) {
            ActiveWorkoutRouteNavigationDecision(navigationState = navigationState)
        } else if (shouldConfirmBack(workoutState)) {
            ActiveWorkoutRouteNavigationDecision(
                navigationState = navigationState.copy(showExitConfirmation = true)
            )
        } else {
            ActiveWorkoutRouteNavigationDecision(
                navigationState = navigationState.copy(hasNavigatedAway = true),
                navigateUp = true
            )
        }

    fun onDismissExitConfirmation(
        navigationState: ActiveWorkoutRouteNavigationState
    ): ActiveWorkoutRouteNavigationState =
        navigationState.copy(showExitConfirmation = false)

    fun onExitConfirmed(
        navigationState: ActiveWorkoutRouteNavigationState
    ): ActiveWorkoutRouteNavigationDecision =
        if (navigationState.hasNavigatedAway) {
            ActiveWorkoutRouteNavigationDecision(navigationState = navigationState)
        } else {
            ActiveWorkoutRouteNavigationDecision(
                navigationState = navigationState.copy(
                    showExitConfirmation = false,
                    hasNavigatedAway = true
                ),
                navigateUp = true,
                stopWorkout = true
            )
        }

    fun autoNavigationDelayMillis(
        navigationState: ActiveWorkoutRouteNavigationState,
        workoutState: WorkoutState,
        isJustLift: Boolean
    ): Long? =
        if (navigationState.hasNavigatedAway) {
            null
        } else {
            navigateUpDelayMillis(workoutState = workoutState, isJustLift = isJustLift)
        }

    fun onAutoNavigationReady(
        navigationState: ActiveWorkoutRouteNavigationState
    ): ActiveWorkoutRouteNavigationDecision =
        if (navigationState.hasNavigatedAway) {
            ActiveWorkoutRouteNavigationDecision(navigationState = navigationState)
        } else {
            ActiveWorkoutRouteNavigationDecision(
                navigationState = navigationState.copy(hasNavigatedAway = true),
                navigateUp = true
            )
        }

    fun onCompletedResetRequested(
        navigationState: ActiveWorkoutRouteNavigationState
    ): ActiveWorkoutRouteNavigationDecision =
        if (navigationState.hasNavigatedAway) {
            ActiveWorkoutRouteNavigationDecision(navigationState = navigationState)
        } else {
            ActiveWorkoutRouteNavigationDecision(
                navigationState = navigationState.copy(
                    showExitConfirmation = false,
                    hasNavigatedAway = true
                ),
                navigateUp = true
            )
        }
}

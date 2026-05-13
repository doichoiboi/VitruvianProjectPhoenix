package com.example.vitruvianredux.presentation.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.vitruvianredux.data.repository.ExerciseRepository
import com.example.vitruvianredux.domain.model.ConnectionState
import com.example.vitruvianredux.domain.model.HapticEvent
import com.example.vitruvianredux.domain.model.PRCelebrationEvent
import com.example.vitruvianredux.domain.model.RepCount
import com.example.vitruvianredux.domain.model.Routine
import com.example.vitruvianredux.domain.model.UserPreferences
import com.example.vitruvianredux.domain.model.WeightUnit
import com.example.vitruvianredux.domain.model.WorkoutMetric
import com.example.vitruvianredux.domain.model.WorkoutParameters
import com.example.vitruvianredux.domain.model.WorkoutState
import com.example.vitruvianredux.domain.usecase.RepRanges
import com.example.vitruvianredux.presentation.viewmodel.AutoStopUiState
import kotlinx.coroutines.flow.SharedFlow

/**
 * Active Workout screen - displays workout controls and metrics during a workout.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveWorkoutScreen(
    connectionState: ConnectionState,
    workoutState: WorkoutState,
    currentMetric: WorkoutMetric?,
    currentHeuristicKgMax: Float,
    workoutParameters: WorkoutParameters,
    repCount: RepCount,
    repRanges: RepRanges?,
    autoStopState: AutoStopUiState,
    weightUnit: WeightUnit,
    enableVideoPlayback: Boolean,
    loadedRoutine: Routine?,
    currentExerciseIndex: Int,
    isBodyweightExercise: Boolean,
    bodyweightTimerState: Pair<Int, Int>?,
    hapticEvents: SharedFlow<HapticEvent>,
    userPreferences: UserPreferences,
    prCelebrationEvent: PRCelebrationEvent?,
    exerciseRepository: ExerciseRepository,
    kgToDisplay: (Float, WeightUnit) -> Float,
    displayToKg: (Float, WeightUnit) -> Float,
    formatWeight: (Float, WeightUnit) -> String,
    showExitConfirmation: Boolean,
    onBackAttempt: () -> Unit,
    onDismissExitConfirmation: () -> Unit,
    onExitConfirmed: () -> Unit,
    onDismissPrCelebration: () -> Unit,
    onScan: () -> Unit,
    onDisconnect: () -> Unit,
    onStartWorkout: () -> Unit,
    onSkipRest: () -> Unit,
    onProceedFromSummary: () -> Unit,
    onResetForNewWorkout: () -> Unit,
    onStartNextExercise: () -> Unit,
    onUpdateParameters: (WorkoutParameters) -> Unit
) {
    BackHandler(enabled = true) {
        onBackAttempt()
    }

    HapticFeedbackEffect(hapticEvents = hapticEvents, beepsEnabled = userPreferences.beepsEnabled)

    WorkoutTab(
        connectionState = connectionState,
        workoutState = workoutState,
        currentMetric = currentMetric,
        currentHeuristicKgMax = currentHeuristicKgMax,
        workoutParameters = workoutParameters,
        repCount = repCount,
        repRanges = repRanges,
        autoStopState = autoStopState,
        weightUnit = weightUnit,
        enableVideoPlayback = enableVideoPlayback,
        beepsEnabled = userPreferences.beepsEnabled,
        exerciseRepository = exerciseRepository,
        isWorkoutSetupDialogVisible = false,
        hapticEvents = hapticEvents,
        loadedRoutine = loadedRoutine,
        currentExerciseIndex = currentExerciseIndex,
        autoplayEnabled = userPreferences.autoplayEnabled,
        isBodyweightExercise = isBodyweightExercise,
        bodyweightTimerState = bodyweightTimerState,
        kgToDisplay = kgToDisplay,
        displayToKg = displayToKg,
        formatWeight = formatWeight,
        onScan = onScan,
        onDisconnect = onDisconnect,
        onStartWorkout = onStartWorkout,
        onStopWorkout = onBackAttempt,
        onSkipRest = onSkipRest,
        onProceedFromSummary = onProceedFromSummary,
        onResetForNewWorkout = onResetForNewWorkout,
        onStartNextExercise = onStartNextExercise,
        onUpdateParameters = onUpdateParameters,
        onShowWorkoutSetupDialog = { },
        onHideWorkoutSetupDialog = { },
        showConnectionCard = false,
        showWorkoutSetupCard = false
    )

    if (showExitConfirmation) {
        AlertDialog(
            onDismissRequest = onDismissExitConfirmation,
            title = { Text("Exit Workout?") },
            text = { Text("The workout is currently active. Are you sure you want to exit?") },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = MaterialTheme.shapes.medium,
            confirmButton = {
                Button(onClick = onExitConfirmed) {
                    Text("Exit")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissExitConfirmation) {
                    Text("Cancel")
                }
            }
        )
    }

    prCelebrationEvent?.let { event ->
        val prDisplayType = when {
            event.isBothPRs -> com.example.vitruvianredux.presentation.components.PRDisplayType.BOTH
            event.isVolumePR -> com.example.vitruvianredux.presentation.components.PRDisplayType.VOLUME
            else -> com.example.vitruvianredux.presentation.components.PRDisplayType.WEIGHT
        }

        val totalVolume = event.weightPerCableKg * event.reps * 2
        val volumeFormatted = "${formatWeight(totalVolume, weightUnit)} total"

        com.example.vitruvianredux.presentation.components.PRCelebrationDialog(
            show = true,
            exerciseName = event.exerciseName,
            weight = "${formatWeight(event.weightPerCableKg, weightUnit)}/cable x ${event.reps} reps",
            prType = prDisplayType,
            volume = volumeFormatted,
            onDismiss = onDismissPrCelebration
        )
    }
}

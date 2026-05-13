package com.example.vitruvianredux.presentation.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.navigation.NavController
import com.example.vitruvianredux.data.repository.ExerciseRepository
import com.example.vitruvianredux.domain.model.PRCelebrationEvent
import com.example.vitruvianredux.presentation.chrome.LocalAppChrome
import com.example.vitruvianredux.presentation.viewmodel.MainViewModel
import com.example.vitruvianredux.presentation.workout.ActiveWorkoutRouteNavigationState
import com.example.vitruvianredux.presentation.workout.ActiveWorkoutRoutePolicy
import kotlinx.coroutines.delay

@Composable
fun ActiveWorkoutRoute(
    navController: NavController,
    viewModel: MainViewModel,
    exerciseRepository: ExerciseRepository
) {
    val workoutState by viewModel.workoutState.collectAsState()
    val currentMetric by viewModel.currentMetric.collectAsState()
    val currentHeuristicKgMax by viewModel.currentHeuristicKgMax.collectAsState()
    val workoutParameters by viewModel.workoutParameters.collectAsState()
    val repCount by viewModel.repCount.collectAsState()
    val repRanges by viewModel.repRanges.collectAsState()
    val autoStopState by viewModel.autoStopState.collectAsState()
    val weightUnit by viewModel.weightUnit.collectAsState()
    val enableVideoPlayback by viewModel.enableVideoPlayback.collectAsState()
    val loadedRoutine by viewModel.loadedRoutine.collectAsState()
    val currentExerciseIndex by viewModel.currentExerciseIndex.collectAsState()
    val isBodyweightExercise by viewModel.isCurrentExerciseBodyweight.collectAsState()
    val bodyweightTimerState by viewModel.bodyweightTimerState.collectAsState()
    val connectionState by viewModel.connectionState.collectAsState()
    val userPreferences by viewModel.userPreferences.collectAsState()
    val hapticEvents = viewModel.hapticEvents
    val appChrome = LocalAppChrome.current
    val chromeOwner = remember { "ActiveWorkoutScreen" }

    var navigationState by remember { mutableStateOf(ActiveWorkoutRouteNavigationState()) }
    var prCelebrationEvent by remember { mutableStateOf<PRCelebrationEvent?>(null) }

    LaunchedEffect(Unit) {
        viewModel.prCelebrationEvent.collect { event ->
            prCelebrationEvent = event
        }
    }

    val screenTitle = remember(loadedRoutine, workoutParameters.isJustLift) {
        when {
            loadedRoutine != null -> loadedRoutine?.name ?: "Routine"
            workoutParameters.isJustLift -> "Just Lift"
            else -> "Single Exercise"
        }
    }

    val onBackAttempt: () -> Unit = {
        val decision = ActiveWorkoutRoutePolicy.onBackAttempt(
            navigationState = navigationState,
            workoutState = workoutState
        )
        navigationState = decision.navigationState
        if (decision.navigateUp) {
            navController.navigateUp()
        }
    }
    val latestOnBackAttempt by rememberUpdatedState(onBackAttempt)
    val latestNavigationState by rememberUpdatedState(navigationState)

    LaunchedEffect(appChrome, chromeOwner, screenTitle) {
        appChrome.setDynamicTitle(chromeOwner, screenTitle)
    }

    LaunchedEffect(appChrome, chromeOwner) {
        appChrome.setBackAction(chromeOwner) {
            latestOnBackAttempt()
        }
    }

    DisposableEffect(appChrome, chromeOwner) {
        onDispose {
            appChrome.clearChrome(chromeOwner)
        }
    }

    LaunchedEffect(workoutState, workoutParameters.isJustLift) {
        val delayMillis = ActiveWorkoutRoutePolicy.autoNavigationDelayMillis(
            navigationState = latestNavigationState,
            workoutState = workoutState,
            isJustLift = workoutParameters.isJustLift
        )
        if (delayMillis != null) {
            if (delayMillis > 0L) {
                delay(delayMillis)
            }
            val decision = ActiveWorkoutRoutePolicy.onAutoNavigationReady(latestNavigationState)
            navigationState = decision.navigationState
            if (decision.navigateUp) {
                navController.navigateUp()
            }
        }
    }

    ActiveWorkoutScreen(
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
        loadedRoutine = loadedRoutine,
        currentExerciseIndex = currentExerciseIndex,
        isBodyweightExercise = isBodyweightExercise,
        bodyweightTimerState = bodyweightTimerState,
        hapticEvents = hapticEvents,
        userPreferences = userPreferences,
        prCelebrationEvent = prCelebrationEvent,
        exerciseRepository = exerciseRepository,
        kgToDisplay = viewModel::kgToDisplay,
        displayToKg = viewModel::displayToKg,
        formatWeight = viewModel::formatWeight,
        showExitConfirmation = navigationState.showExitConfirmation,
        onBackAttempt = onBackAttempt,
        onDismissExitConfirmation = {
            navigationState = ActiveWorkoutRoutePolicy.onDismissExitConfirmation(navigationState)
        },
        onExitConfirmed = {
            val decision = ActiveWorkoutRoutePolicy.onExitConfirmed(navigationState)
            navigationState = decision.navigationState
            if (decision.stopWorkout) {
                viewModel.stopWorkout()
            }
            if (decision.navigateUp) {
                navController.navigateUp()
            }
        },
        onDismissPrCelebration = { prCelebrationEvent = null },
        onScan = viewModel::startScanning,
        onDisconnect = viewModel::disconnect,
        onStartWorkout = {
            viewModel.ensureConnection(
                onConnected = { viewModel.startWorkout() },
                onFailed = { }
            )
        },
        onSkipRest = viewModel::skipRest,
        onProceedFromSummary = viewModel::proceedFromSummary,
        onResetForNewWorkout = viewModel::resetForNewWorkout,
        onStartNextExercise = viewModel::advanceToNextExercise,
        onUpdateParameters = viewModel::updateWorkoutParameters
    )
}

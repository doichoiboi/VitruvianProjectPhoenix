package com.example.vitruvianredux.presentation.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import com.example.vitruvianredux.domain.model.ConnectionState
import com.example.vitruvianredux.domain.model.WorkoutState
import com.example.vitruvianredux.presentation.navigation.NavigationRoutes
import com.example.vitruvianredux.presentation.viewmodel.MainViewModel
import com.example.vitruvianredux.ui.theme.ThemeMode

@Suppress("UNUSED_PARAMETER")
@Composable
fun JustLiftRoute(
    navController: NavController,
    viewModel: MainViewModel,
    themeMode: ThemeMode
) {
    val workoutState by viewModel.workoutState.collectAsState()
    val workoutParameters by viewModel.workoutParameters.collectAsState()
    val currentMetric by viewModel.currentMetric.collectAsState()
    val currentHeuristicKgMax by viewModel.currentHeuristicKgMax.collectAsState()
    val repCount by viewModel.repCount.collectAsState()
    val autoStopState by viewModel.autoStopState.collectAsState()
    val autoStartCountdown by viewModel.autoStartCountdown.collectAsState()
    val justLiftRestStartedAtMillis by viewModel.justLiftRestStartedAtMillis.collectAsState()
    val weightUnit by viewModel.weightUnit.collectAsState()
    val connectionState by viewModel.connectionState.collectAsState()

    LaunchedEffect(workoutState) {
        when (workoutState) {
            is WorkoutState.Active -> navController.navigate(NavigationRoutes.ActiveWorkout.route)
            is WorkoutState.Idle -> Unit
            else -> viewModel.prepareForJustLift()
        }
    }

    LaunchedEffect(connectionState) {
        if (connectionState is ConnectionState.Connected) {
            viewModel.enableHandleDetection()
        }
    }

    JustLiftScreen(
        workoutState = workoutState,
        workoutParameters = workoutParameters,
        currentMetric = currentMetric,
        currentHeuristicKgMax = currentHeuristicKgMax,
        repCount = repCount,
        autoStopState = autoStopState,
        autoStartCountdown = autoStartCountdown,
        justLiftRestStartedAtMillis = justLiftRestStartedAtMillis,
        weightUnit = weightUnit,
        getJustLiftDefaults = viewModel::getJustLiftDefaults,
        kgToDisplay = viewModel::kgToDisplay,
        displayToKg = viewModel::displayToKg,
        formatWeight = viewModel::formatWeight,
        onWorkoutParametersChanged = viewModel::updateWorkoutParameters,
        onStopWorkout = viewModel::stopWorkout
    )
}

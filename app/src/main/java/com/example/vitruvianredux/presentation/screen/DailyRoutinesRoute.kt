package com.example.vitruvianredux.presentation.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.vitruvianredux.data.repository.ExerciseRepository
import com.example.vitruvianredux.presentation.navigation.NavigationRoutes
import com.example.vitruvianredux.presentation.viewmodel.MainViewModel
import com.example.vitruvianredux.ui.theme.ThemeMode

@Composable
fun DailyRoutinesRoute(
    navController: NavController,
    viewModel: MainViewModel,
    exerciseRepository: ExerciseRepository,
    themeMode: ThemeMode,
    modifier: Modifier = Modifier
) {
    val routines by viewModel.routines.collectAsState()
    val weightUnit by viewModel.weightUnit.collectAsState()
    val enableVideoPlayback by viewModel.enableVideoPlayback.collectAsState()

    DailyRoutinesScreen(
        themeMode = themeMode,
        routines = routines,
        exerciseRepository = exerciseRepository,
        personalRecordRepository = viewModel.personalRecordRepository,
        weightUnit = weightUnit,
        enableVideoPlayback = enableVideoPlayback,
        formatWeight = viewModel::formatWeight,
        kgToDisplay = viewModel::kgToDisplay,
        displayToKg = viewModel::displayToKg,
        onStartWorkout = { routine ->
            viewModel.ensureConnection(
                onConnected = {
                    viewModel.loadRoutine(routine)
                    viewModel.startWorkout()
                    navController.navigate(NavigationRoutes.ActiveWorkout.route)
                },
                onFailed = { /* Error shown via StateFlow */ }
            )
        },
        onDeleteRoutine = { viewModel.deleteRoutine(it) },
        onSaveRoutine = { viewModel.saveRoutine(it) },
        onUpdateRoutine = { viewModel.updateRoutine(it) },
        modifier = modifier
    )
}

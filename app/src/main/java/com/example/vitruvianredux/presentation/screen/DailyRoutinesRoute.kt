package com.example.vitruvianredux.presentation.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.vitruvianredux.data.repository.ExerciseRepository
import com.example.vitruvianredux.presentation.navigation.NavigationRoutes
import com.example.vitruvianredux.presentation.viewmodel.MainViewModel
import com.example.vitruvianredux.presentation.workout.MainViewModelWorkoutLaunchController
import com.example.vitruvianredux.presentation.workout.WorkoutLaunchCoordinator
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
    val workoutLauncher = remember(navController, viewModel) {
        WorkoutLaunchCoordinator(
            controller = MainViewModelWorkoutLaunchController(viewModel),
            navigateToActiveWorkout = {
                navController.navigate(NavigationRoutes.ActiveWorkout.route)
            }
        )
    }

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
            workoutLauncher.startRoutine(routine)
        },
        onDeleteRoutine = { viewModel.deleteRoutine(it) },
        onSaveRoutine = { viewModel.saveRoutine(it) },
        onUpdateRoutine = { viewModel.updateRoutine(it) },
        modifier = modifier
    )
}

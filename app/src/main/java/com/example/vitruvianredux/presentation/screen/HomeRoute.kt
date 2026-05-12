package com.example.vitruvianredux.presentation.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.vitruvianredux.presentation.navigation.NavigationRoutes
import com.example.vitruvianredux.presentation.viewmodel.MainViewModel
import com.example.vitruvianredux.presentation.workout.MainViewModelWorkoutLaunchController
import com.example.vitruvianredux.presentation.workout.WorkoutLaunchCoordinator
import com.example.vitruvianredux.presentation.workout.WorkoutLaunchDestination
import com.example.vitruvianredux.ui.theme.ThemeMode

@Composable
fun HomeRoute(
    navController: NavController,
    viewModel: MainViewModel,
    themeMode: ThemeMode,
    modifier: Modifier = Modifier
) {
    val activeProgram by viewModel.activeProgram.collectAsState()
    val routines by viewModel.routines.collectAsState()
    val weightUnit by viewModel.weightUnit.collectAsState()
    val workoutLauncher = remember(navController, viewModel) {
        WorkoutLaunchCoordinator(
            controller = MainViewModelWorkoutLaunchController(viewModel),
            navigateToDailyRoutines = {
                navController.navigate(NavigationRoutes.DailyRoutines.route)
            }
        )
    }

    HomeScreen(
        themeMode = themeMode,
        activeProgram = activeProgram,
        routines = routines,
        weightUnit = weightUnit,
        formatWeight = viewModel::formatWeight,
        kgToDisplay = viewModel::kgToDisplay,
        onStartRoutine = { routineId ->
            workoutLauncher.startRoutineById(
                routineId = routineId,
                destination = WorkoutLaunchDestination.DailyRoutines
            )
        },
        onNavigateToJustLift = { navController.navigate(NavigationRoutes.JustLift.route) },
        onNavigateToSingleExercise = { navController.navigate(NavigationRoutes.SingleExercise.route) },
        onNavigateToDailyRoutines = { navController.navigate(NavigationRoutes.DailyRoutines.route) },
        onNavigateToWeeklyPrograms = { navController.navigate(NavigationRoutes.WeeklyPrograms.route) },
        modifier = modifier
    )
}

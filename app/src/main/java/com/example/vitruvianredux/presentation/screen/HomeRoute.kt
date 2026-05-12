package com.example.vitruvianredux.presentation.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.vitruvianredux.presentation.navigation.NavigationRoutes
import com.example.vitruvianredux.presentation.viewmodel.MainViewModel
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

    HomeScreen(
        themeMode = themeMode,
        activeProgram = activeProgram,
        routines = routines,
        weightUnit = weightUnit,
        formatWeight = viewModel::formatWeight,
        kgToDisplay = viewModel::kgToDisplay,
        onStartRoutine = { routineId ->
            viewModel.ensureConnection(
                onConnected = {
                    viewModel.loadRoutineById(routineId)
                    viewModel.startWorkout()
                    navController.navigate(NavigationRoutes.DailyRoutines.route)
                },
                onFailed = { /* Error shown via StateFlow */ }
            )
        },
        onNavigateToJustLift = { navController.navigate(NavigationRoutes.JustLift.route) },
        onNavigateToSingleExercise = { navController.navigate(NavigationRoutes.SingleExercise.route) },
        onNavigateToDailyRoutines = { navController.navigate(NavigationRoutes.DailyRoutines.route) },
        onNavigateToWeeklyPrograms = { navController.navigate(NavigationRoutes.WeeklyPrograms.route) },
        modifier = modifier
    )
}

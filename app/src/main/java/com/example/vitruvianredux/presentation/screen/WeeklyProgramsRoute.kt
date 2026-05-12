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
fun WeeklyProgramsRoute(
    navController: NavController,
    viewModel: MainViewModel,
    themeMode: ThemeMode,
    modifier: Modifier = Modifier
) {
    val programs by viewModel.weeklyPrograms.collectAsState()
    val activeProgram by viewModel.activeProgram.collectAsState()
    val routines by viewModel.routines.collectAsState()

    WeeklyProgramsScreen(
        themeMode = themeMode,
        programs = programs,
        activeProgram = activeProgram,
        routines = routines,
        onStartTodayWorkout = { routineId ->
            viewModel.ensureConnection(
                onConnected = {
                    viewModel.loadRoutineById(routineId)
                    viewModel.startWorkout()
                },
                onFailed = { /* Error shown via StateFlow */ }
            )
        },
        onCreateProgram = {
            navController.navigate(NavigationRoutes.ProgramBuilder.createRoute())
        },
        onEditProgram = { programId ->
            navController.navigate(NavigationRoutes.ProgramBuilder.createRoute(programId))
        },
        onActivateProgram = { programId ->
            viewModel.activateProgram(programId)
        },
        onDeleteProgram = { programId ->
            viewModel.deleteProgram(programId)
        },
        modifier = modifier
    )
}

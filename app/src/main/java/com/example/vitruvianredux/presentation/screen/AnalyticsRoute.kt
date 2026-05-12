package com.example.vitruvianredux.presentation.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.vitruvianredux.presentation.viewmodel.MainViewModel
import com.example.vitruvianredux.ui.theme.ThemeMode

@Composable
fun AnalyticsRoute(
    viewModel: MainViewModel,
    themeMode: ThemeMode,
    modifier: Modifier = Modifier
) {
    val workoutHistory by viewModel.workoutHistory.collectAsState()
    val groupedWorkoutHistory by viewModel.groupedWorkoutHistory.collectAsState()
    val allWorkoutSessions by viewModel.allWorkoutSessions.collectAsState()
    val personalRecords by viewModel.allPersonalRecords.collectAsState()
    val weightUnit by viewModel.weightUnit.collectAsState()

    AnalyticsScreen(
        themeMode = themeMode,
        workoutHistory = workoutHistory,
        groupedWorkoutHistory = groupedWorkoutHistory,
        allWorkoutSessions = allWorkoutSessions,
        personalRecords = personalRecords,
        weightUnit = weightUnit,
        exerciseRepository = viewModel.exerciseRepository,
        formatWeight = viewModel::formatWeight,
        onDeleteWorkout = { viewModel.deleteWorkout(it) },
        modifier = modifier
    )
}

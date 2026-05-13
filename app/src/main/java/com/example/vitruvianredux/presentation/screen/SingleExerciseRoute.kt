package com.example.vitruvianredux.presentation.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.vitruvianredux.data.repository.ExerciseRepository
import com.example.vitruvianredux.domain.model.Routine
import com.example.vitruvianredux.presentation.navigation.NavigationRoutes
import com.example.vitruvianredux.presentation.viewmodel.MainViewModel
import com.example.vitruvianredux.presentation.viewmodel.MainViewModel.Companion.TEMP_SINGLE_EXERCISE_PREFIX
import java.util.UUID

@Composable
fun SingleExerciseRoute(
    navController: NavController,
    viewModel: MainViewModel,
    exerciseRepository: ExerciseRepository,
    modifier: Modifier = Modifier
) {
    val weightUnit by viewModel.weightUnit.collectAsState()
    val enableVideoPlayback by viewModel.enableVideoPlayback.collectAsState()
    val sessionEccentricLoad by viewModel.sessionEccentricLoad.collectAsState()

    SingleExerciseScreen(
        exerciseRepository = exerciseRepository,
        personalRecordRepository = viewModel.personalRecordRepository,
        weightUnit = weightUnit,
        enableVideoPlayback = enableVideoPlayback,
        sessionEccentricLoad = sessionEccentricLoad,
        kgToDisplay = viewModel::kgToDisplay,
        displayToKg = viewModel::displayToKg,
        formatWeight = viewModel::formatWeight,
        getSingleExerciseDefaults = viewModel::getSingleExerciseDefaults,
        onStartWorkout = { configuredExercise ->
            val tempRoutine = Routine(
                id = "${TEMP_SINGLE_EXERCISE_PREFIX}${UUID.randomUUID()}",
                name = "Single Exercise: ${configuredExercise.exercise.name}",
                description = "Temporary routine for single exercise mode",
                exercises = listOf(configuredExercise)
            )

            viewModel.loadRoutine(tempRoutine)
            viewModel.ensureConnection(
                onConnected = {
                    viewModel.startWorkout()
                    navController.navigate(NavigationRoutes.ActiveWorkout.route) {
                        popUpTo(NavigationRoutes.Home.route)
                    }
                },
                onFailed = {}
            )
        },
        modifier = modifier
    )
}

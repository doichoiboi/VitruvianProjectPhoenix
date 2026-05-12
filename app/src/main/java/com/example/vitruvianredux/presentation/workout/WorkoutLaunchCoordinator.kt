package com.example.vitruvianredux.presentation.workout

import com.example.vitruvianredux.domain.model.Routine
import com.example.vitruvianredux.presentation.viewmodel.MainViewModel

enum class WorkoutLaunchDestination {
    None,
    ActiveWorkout,
    DailyRoutines
}

interface WorkoutLaunchController {
    fun ensureConnection(onConnected: () -> Unit, onFailed: () -> Unit = {})
    fun loadRoutine(routine: Routine)
    fun loadRoutineById(routineId: String)
    fun startWorkout()
}

class MainViewModelWorkoutLaunchController(
    private val viewModel: MainViewModel
) : WorkoutLaunchController {
    override fun ensureConnection(onConnected: () -> Unit, onFailed: () -> Unit) {
        viewModel.ensureConnection(onConnected = onConnected, onFailed = onFailed)
    }

    override fun loadRoutine(routine: Routine) {
        viewModel.loadRoutine(routine)
    }

    override fun loadRoutineById(routineId: String) {
        viewModel.loadRoutineById(routineId)
    }

    override fun startWorkout() {
        viewModel.startWorkout()
    }
}

class WorkoutLaunchCoordinator(
    private val controller: WorkoutLaunchController,
    private val navigateToActiveWorkout: () -> Unit = {},
    private val navigateToDailyRoutines: () -> Unit = {}
) {
    fun startRoutine(
        routine: Routine,
        destination: WorkoutLaunchDestination = WorkoutLaunchDestination.ActiveWorkout
    ) {
        controller.ensureConnection(
            onConnected = {
                controller.loadRoutine(routine)
                controller.startWorkout()
                navigate(destination)
            },
            onFailed = {}
        )
    }

    fun startRoutineById(
        routineId: String,
        destination: WorkoutLaunchDestination = WorkoutLaunchDestination.None
    ) {
        controller.ensureConnection(
            onConnected = {
                controller.loadRoutineById(routineId)
                controller.startWorkout()
                navigate(destination)
            },
            onFailed = {}
        )
    }

    private fun navigate(destination: WorkoutLaunchDestination) {
        when (destination) {
            WorkoutLaunchDestination.None -> Unit
            WorkoutLaunchDestination.ActiveWorkout -> navigateToActiveWorkout()
            WorkoutLaunchDestination.DailyRoutines -> navigateToDailyRoutines()
        }
    }
}

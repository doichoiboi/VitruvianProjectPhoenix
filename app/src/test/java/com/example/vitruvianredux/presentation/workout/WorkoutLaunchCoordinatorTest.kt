package com.example.vitruvianredux.presentation.workout

import com.example.vitruvianredux.domain.model.Routine
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class WorkoutLaunchCoordinatorTest {

    @Test
    fun `startRoutine loads routine starts workout and navigates active workout after connection`() {
        val routine = Routine(id = "routine-1", name = "Push")
        val events = mutableListOf<String>()
        val controller = RecordingWorkoutLaunchController(events)
        val coordinator = WorkoutLaunchCoordinator(
            controller = controller,
            navigateToActiveWorkout = { events += "navigate:active_workout" }
        )

        coordinator.startRoutine(routine)

        assertThat(events).containsExactly(
            "ensureConnection",
            "loadRoutine:routine-1",
            "startWorkout",
            "navigate:active_workout"
        ).inOrder()
    }

    @Test
    fun `startRoutineById loads routine id starts workout and can navigate daily routines`() {
        val events = mutableListOf<String>()
        val controller = RecordingWorkoutLaunchController(events)
        val coordinator = WorkoutLaunchCoordinator(
            controller = controller,
            navigateToDailyRoutines = { events += "navigate:daily_routines" }
        )

        coordinator.startRoutineById(
            routineId = "routine-2",
            destination = WorkoutLaunchDestination.DailyRoutines
        )

        assertThat(events).containsExactly(
            "ensureConnection",
            "loadRoutineById:routine-2",
            "startWorkout",
            "navigate:daily_routines"
        ).inOrder()
    }

    @Test
    fun `startRoutineById defaults to no navigation for weekly program start`() {
        val events = mutableListOf<String>()
        val controller = RecordingWorkoutLaunchController(events)
        val coordinator = WorkoutLaunchCoordinator(controller)

        coordinator.startRoutineById("routine-3")

        assertThat(events).containsExactly(
            "ensureConnection",
            "loadRoutineById:routine-3",
            "startWorkout"
        ).inOrder()
    }

    @Test
    fun `connection failure does not load start or navigate`() {
        val events = mutableListOf<String>()
        val controller = RecordingWorkoutLaunchController(
            events = events,
            connectSuccessfully = false
        )
        val coordinator = WorkoutLaunchCoordinator(
            controller = controller,
            navigateToActiveWorkout = { events += "navigate:active_workout" }
        )

        coordinator.startRoutine(Routine(id = "routine-4", name = "Legs"))

        assertThat(events).containsExactly(
            "ensureConnection",
            "connectionFailed"
        ).inOrder()
    }

    private class RecordingWorkoutLaunchController(
        private val events: MutableList<String>,
        private val connectSuccessfully: Boolean = true
    ) : WorkoutLaunchController {
        override fun ensureConnection(onConnected: () -> Unit, onFailed: () -> Unit) {
            events += "ensureConnection"
            if (connectSuccessfully) {
                onConnected()
            } else {
                events += "connectionFailed"
                onFailed()
            }
        }

        override fun loadRoutine(routine: Routine) {
            events += "loadRoutine:${routine.id}"
        }

        override fun loadRoutineById(routineId: String) {
            events += "loadRoutineById:$routineId"
        }

        override fun startWorkout() {
            events += "startWorkout"
        }
    }
}

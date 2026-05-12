package com.example.vitruvianredux.domain.workout

import com.example.vitruvianredux.domain.model.WorkoutMetric
import com.example.vitruvianredux.domain.model.WorkoutParameters
import com.example.vitruvianredux.domain.model.WorkoutState

/**
 * Pure domain owner for workout-flow state transitions.
 *
 * This class intentionally does not know about Android, Hilt, BLE libraries,
 * foreground services, Room, or Compose. It returns effects for the outer app
 * layer to perform, which keeps orchestration debuggable and testable.
 */
class WorkoutEngine(
    initialParameters: WorkoutParameters,
    private val clockMillis: () -> Long = { System.currentTimeMillis() }
) {
    private var parameters: WorkoutParameters = initialParameters
    private var state: WorkoutState = WorkoutState.Idle
    private var startedAtMillis: Long? = null
    private val collectedMetrics = mutableListOf<WorkoutMetric>()

    fun snapshot(): WorkoutEngineSnapshot = WorkoutEngineSnapshot(
        state = state,
        parameters = parameters,
        startedAtMillis = startedAtMillis,
        metrics = collectedMetrics.toList()
    )

    fun dispatch(action: WorkoutEngineAction): WorkoutEngineResult {
        val effects = mutableListOf<WorkoutEngineEffect>()

        when (action) {
            is WorkoutEngineAction.UpdateParameters -> {
                if (state == WorkoutState.Idle) {
                    parameters = action.parameters
                } else {
                    effects += WorkoutEngineEffect.ReportError("Workout parameters can only change while idle")
                }
            }

            is WorkoutEngineAction.StartRequested -> {
                parameters = action.parameters ?: parameters
                collectedMetrics.clear()
                startedAtMillis = null

                if (action.skipCountdown) {
                    transitionToActive(effects)
                } else {
                    state = WorkoutState.Countdown(DEFAULT_COUNTDOWN_SECONDS)
                }
            }

            WorkoutEngineAction.CountdownTick -> {
                val current = state as? WorkoutState.Countdown
                if (current != null) {
                    val nextSeconds = current.secondsRemaining - 1
                    if (nextSeconds <= 0) {
                        transitionToActive(effects)
                    } else {
                        state = WorkoutState.Countdown(nextSeconds)
                    }
                }
            }

            is WorkoutEngineAction.MetricReceived -> {
                if (state == WorkoutState.Active) {
                    collectedMetrics += action.metric
                }
            }

            WorkoutEngineAction.StopRequested -> {
                if (state == WorkoutState.Active || state is WorkoutState.Countdown) {
                    state = WorkoutState.Completed
                    effects += WorkoutEngineEffect.StopMachine
                }
            }

            is WorkoutEngineAction.SetCompleted -> {
                if (state == WorkoutState.Active) {
                    state = buildSetSummary(action.repCount)
                    effects += WorkoutEngineEffect.StopMachine
                }
            }

            WorkoutEngineAction.ResetRequested -> {
                state = WorkoutState.Idle
                startedAtMillis = null
                collectedMetrics.clear()
            }
        }

        return WorkoutEngineResult(snapshot(), effects)
    }

    private fun transitionToActive(effects: MutableList<WorkoutEngineEffect>) {
        state = WorkoutState.Active
        startedAtMillis = clockMillis()
        effects += WorkoutEngineEffect.StartMachine(parameters)
    }

    private fun buildSetSummary(repCount: Int): WorkoutState.SetSummary {
        val peakPerCableKg = if (collectedMetrics.isEmpty()) {
            parameters.weightPerCableKg
        } else {
            collectedMetrics.maxOf { it.totalLoad } / 2f
        }
        val averagePerCableKg = if (collectedMetrics.isEmpty()) {
            parameters.weightPerCableKg
        } else {
            collectedMetrics.map { it.totalLoad / 2f }.average().toFloat()
        }

        return WorkoutState.SetSummary(
            metrics = collectedMetrics.toList(),
            peakPower = peakPerCableKg,
            averagePower = averagePerCableKg,
            repCount = repCount
        )
    }

    private companion object {
        const val DEFAULT_COUNTDOWN_SECONDS = 5
    }
}

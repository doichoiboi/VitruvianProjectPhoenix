package com.example.vitruvianredux.domain.workout

import com.example.vitruvianredux.domain.model.WorkoutMetric
import com.example.vitruvianredux.domain.model.WorkoutParameters
import com.example.vitruvianredux.domain.model.WorkoutState

/**
 * Stable debug snapshot for workout orchestration.
 */
data class WorkoutEngineSnapshot(
    val state: WorkoutState,
    val parameters: WorkoutParameters,
    val startedAtMillis: Long?,
    val metrics: List<WorkoutMetric>
)

/**
 * Inputs accepted by the pure workout engine.
 */
sealed interface WorkoutEngineAction {
    data class UpdateParameters(val parameters: WorkoutParameters) : WorkoutEngineAction
    data class StartRequested(
        val parameters: WorkoutParameters? = null,
        val skipCountdown: Boolean = false
    ) : WorkoutEngineAction

    data object CountdownTick : WorkoutEngineAction
    data class MetricReceived(val metric: WorkoutMetric) : WorkoutEngineAction
    data object StopRequested : WorkoutEngineAction
    data class SetCompleted(val repCount: Int = 0) : WorkoutEngineAction
    data object ResetRequested : WorkoutEngineAction
}

/**
 * Side effects the Android/BLE edge must perform for the engine.
 */
sealed interface WorkoutEngineEffect {
    data class StartMachine(val parameters: WorkoutParameters) : WorkoutEngineEffect
    data object StopMachine : WorkoutEngineEffect
    data class ReportError(val message: String) : WorkoutEngineEffect
}

data class WorkoutEngineResult(
    val snapshot: WorkoutEngineSnapshot,
    val effects: List<WorkoutEngineEffect> = emptyList()
)

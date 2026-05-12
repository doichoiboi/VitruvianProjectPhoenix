package com.example.vitruvianredux.domain.workout

import com.example.vitruvianredux.domain.model.ProgramMode
import com.example.vitruvianredux.domain.model.WorkoutMetric
import com.example.vitruvianredux.domain.model.WorkoutParameters
import com.example.vitruvianredux.domain.model.WorkoutState
import com.example.vitruvianredux.domain.model.WorkoutType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class WorkoutEngineTest {

    private val baseParameters = WorkoutParameters(
        workoutType = WorkoutType.Program(ProgramMode.OldSchool),
        reps = 10,
        weightPerCableKg = 12.5f
    )

    @Test
    fun `start without countdown enters active and asks edge to start machine`() {
        val engine = WorkoutEngine(baseParameters, clockMillis = { 1_000L })

        val result = engine.dispatch(
            WorkoutEngineAction.StartRequested(skipCountdown = true)
        )

        assertEquals(WorkoutState.Active, result.snapshot.state)
        assertEquals(1_000L, result.snapshot.startedAtMillis)
        assertEquals(listOf(WorkoutEngineEffect.StartMachine(baseParameters)), result.effects)
    }

    @Test
    fun `start with countdown waits for five ticks before machine start`() {
        val engine = WorkoutEngine(baseParameters, clockMillis = { 2_000L })

        val start = engine.dispatch(WorkoutEngineAction.StartRequested())
        assertEquals(WorkoutState.Countdown(5), start.snapshot.state)
        assertTrue(start.effects.isEmpty())

        repeat(4) {
            engine.dispatch(WorkoutEngineAction.CountdownTick)
        }

        val beforeStart = engine.snapshot().state
        assertEquals(WorkoutState.Countdown(1), beforeStart)

        val active = engine.dispatch(WorkoutEngineAction.CountdownTick)
        assertEquals(WorkoutState.Active, active.snapshot.state)
        assertEquals(listOf(WorkoutEngineEffect.StartMachine(baseParameters)), active.effects)
    }

    @Test
    fun `set completion creates summary from collected metrics and stops machine`() {
        val engine = WorkoutEngine(baseParameters)
        engine.dispatch(WorkoutEngineAction.StartRequested(skipCountdown = true))

        engine.dispatch(WorkoutEngineAction.MetricReceived(metric(loadA = 10f, loadB = 12f)))
        engine.dispatch(WorkoutEngineAction.MetricReceived(metric(loadA = 18f, loadB = 20f)))

        val result = engine.dispatch(WorkoutEngineAction.SetCompleted(repCount = 7))

        val summary = assertIs<WorkoutState.SetSummary>(result.snapshot.state)
        assertEquals(19f, summary.peakPower)
        assertEquals(15f, summary.averagePower)
        assertEquals(7, summary.repCount)
        assertEquals(2, summary.metrics.size)
        assertEquals(listOf(WorkoutEngineEffect.StopMachine), result.effects)
    }

    @Test
    fun `parameters cannot change during active workout`() {
        val engine = WorkoutEngine(baseParameters)
        engine.dispatch(WorkoutEngineAction.StartRequested(skipCountdown = true))

        val newParameters = baseParameters.copy(reps = 20)
        val result = engine.dispatch(WorkoutEngineAction.UpdateParameters(newParameters))

        assertEquals(baseParameters, result.snapshot.parameters)
        assertEquals(
            listOf(WorkoutEngineEffect.ReportError("Workout parameters can only change while idle")),
            result.effects
        )
    }

    private fun metric(loadA: Float, loadB: Float): WorkoutMetric = WorkoutMetric(
        loadA = loadA,
        loadB = loadB,
        positionA = 100f,
        positionB = 100f
    )
}

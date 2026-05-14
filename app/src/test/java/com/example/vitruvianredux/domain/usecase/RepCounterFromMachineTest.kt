package com.example.vitruvianredux.domain.usecase

import com.example.vitruvianredux.domain.model.RepEvent
import com.example.vitruvianredux.domain.model.RepType
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RepCounterFromMachineTest {

    private lateinit var repCounter: RepCounterFromMachine
    private val capturedEvents = mutableListOf<RepEvent>()

    @Before
    fun setup() {
        repCounter = RepCounterFromMachine()
        capturedEvents.clear()
        repCounter.onRepEvent = { capturedEvents.add(it) }
    }

    @Test
    fun `modern mode - warmup reps increment correctly`() {
        repCounter.configure(warmupTarget = 3, workingTarget = 5, isJustLift = false, stopAtTop = false)

        // Simulate warmup reps from machine
        repCounter.process(repsRomCount = 1, repsSetCount = 0, up = 1, down = 1)
        assertEquals(1, repCounter.getRepCount().warmupReps)
        assertEquals(RepType.WARMUP_COMPLETED, capturedEvents.last().type)

        repCounter.process(repsRomCount = 2, repsSetCount = 0, up = 2, down = 2)
        assertEquals(2, repCounter.getRepCount().warmupReps)

        repCounter.process(repsRomCount = 3, repsSetCount = 0, up = 3, down = 3)
        assertEquals(3, repCounter.getRepCount().warmupReps)
        assertTrue(repCounter.getRepCount().isWarmupComplete)

        // Should have received WARMUP_COMPLETE event
        assertTrue(capturedEvents.any { it.type == RepType.WARMUP_COMPLETE })
    }

    @Test
    fun `modern mode - working reps and pending state`() {
        repCounter.configure(warmupTarget = 0, workingTarget = 5, isJustLift = false, stopAtTop = false)

        // Baseline directional counters before detecting movement.
        repCounter.process(repsRomCount = 0, repsSetCount = 0, up = 0, down = 0)

        // 1. Move to TOP - should trigger PENDING
        repCounter.process(repsRomCount = 0, repsSetCount = 0, up = 1, down = 0)
        assertTrue(repCounter.getRepCount().hasPendingRep)
        assertEquals(RepType.WORKING_PENDING, capturedEvents.last().type)

        // 2. Move to BOTTOM - pending remains until machine confirms set count
        repCounter.process(repsRomCount = 0, repsSetCount = 0, up = 1, down = 1)
        assertFalse(repCounter.getRepCount().hasPendingRep)

        // 3. Machine confirms rep
        repCounter.process(repsRomCount = 0, repsSetCount = 1, up = 1, down = 1)
        assertEquals(1, repCounter.getRepCount().workingReps)
        assertEquals(RepType.WORKING_COMPLETED, capturedEvents.last().type)
    }

    @Test
    fun `legacy mode - counts reps based on top counter`() {
        repCounter.configure(warmupTarget = 0, workingTarget = 5, isJustLift = false, stopAtTop = false)

        // First call sets lastTopCounter.
        repCounter.process(repsRomCount = 0, repsSetCount = 0, up = 0, down = 0, isLegacyFormat = true)
        assertEquals(0, repCounter.getRepCount().workingReps)

        // Second call with increment
        repCounter.process(repsRomCount = 0, repsSetCount = 0, up = 1, down = 1, isLegacyFormat = true)
        assertEquals(1, repCounter.getRepCount().workingReps)
        assertEquals(RepType.WORKING_COMPLETED, capturedEvents.last().type)
    }

    @Test
    fun `workout complete - triggers when target reached`() {
        repCounter.configure(warmupTarget = 0, workingTarget = 2, isJustLift = false, stopAtTop = false)

        // Rep 1
        repCounter.process(repsRomCount = 0, repsSetCount = 1, up = 1, down = 1)
        assertFalse(repCounter.shouldStopWorkout())

        // Rep 2
        repCounter.process(repsRomCount = 0, repsSetCount = 2, up = 2, down = 2)
        assertTrue(repCounter.shouldStopWorkout())
        assertTrue(capturedEvents.any { it.type == RepType.WORKOUT_COMPLETE })
    }

    @Test
    fun `modern mode - stop at top completes final rep before machine bottom confirmation`() {
        repCounter.configure(warmupTarget = 0, workingTarget = 5, isJustLift = false, stopAtTop = true)
        repCounter.process(repsRomCount = 0, repsSetCount = 0, up = 0, down = 0)

        for (rep in 1..4) {
            repCounter.process(repsRomCount = 0, repsSetCount = rep - 1, up = rep, down = rep - 1)
            repCounter.process(repsRomCount = 0, repsSetCount = rep, up = rep, down = rep)
        }

        assertEquals(4, repCounter.getRepCount().workingReps)
        assertFalse(repCounter.shouldStopWorkout())

        repCounter.process(repsRomCount = 0, repsSetCount = 4, up = 5, down = 4)

        assertEquals(5, repCounter.getRepCount().workingReps)
        assertFalse(repCounter.getRepCount().hasPendingRep)
        assertTrue(repCounter.shouldStopWorkout())
        assertEquals(
            1,
            capturedEvents.count { it.type == RepType.WORKOUT_COMPLETE }
        )

        repCounter.process(repsRomCount = 0, repsSetCount = 5, up = 5, down = 5)

        assertEquals(5, repCounter.getRepCount().workingReps)
        assertEquals(
            1,
            capturedEvents.count { it.type == RepType.WORKOUT_COMPLETE },
            "Machine bottom confirmation should not duplicate completion after stop-at-top"
        )
    }

    @Test
    fun `danger zone - detects when handles are near bottom`() {
        repCounter.configure(warmupTarget = 0, workingTarget = 5, isJustLift = true, stopAtTop = false)

        // Establish range: 0 to 500mm
        repCounter.process(repsRomCount = 0, repsSetCount = 0, up = 0, down = 0, posA = 10f, posB = 10f)

        // recordTopPosition needs up counter increment.
        repCounter.process(repsRomCount = 0, repsSetCount = 0, up = 1, down = 0, posA = 500f, posB = 500f)

        // recordBottomPosition needs down counter increment.
        repCounter.process(repsRomCount = 0, repsSetCount = 0, up = 1, down = 1, posA = 10f, posB = 10f)

        // Check if 15mm is in danger zone (Range is 10 to 500, diff=490. 5% of 490 is 24.5. 10 + 24.5 = 34.5)
        assertTrue(repCounter.isInDangerZone(posA = 15f, posB = 15f))

        // 100mm should NOT be in danger zone
        assertFalse(repCounter.isInDangerZone(posA = 100f, posB = 100f))
    }

    @Test
    fun `hasMeaningfulRange - returns true after sufficient movement`() {
        repCounter.configure(warmupTarget = 0, workingTarget = 5, isJustLift = true, stopAtTop = false)

        // Small movement (20mm)
        repCounter.updatePositionRangesContinuously(10f, 10f)
        repCounter.updatePositionRangesContinuously(30f, 30f)
        assertFalse(repCounter.hasMeaningfulRange())

        // Larger movement (100mm)
        repCounter.updatePositionRangesContinuously(110f, 110f)
        assertTrue(repCounter.hasMeaningfulRange())
    }
}

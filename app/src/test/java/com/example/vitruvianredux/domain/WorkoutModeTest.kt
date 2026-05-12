package com.example.vitruvianredux.domain

import com.example.vitruvianredux.domain.model.RepEvent
import com.example.vitruvianredux.domain.model.RepType
import com.example.vitruvianredux.domain.usecase.RepCounterFromMachine
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Comprehensive test suite for workout modes and rep counting
 * Based on reference web app (app.js:949-1100)
 * 
 * The Vitruvian machine sends rep notifications via characteristic:
 * UUID: 8308f2a6-0875-4a94-a86f-5c5c5e1b068a
 * 
 * Format: Array of u16 values (little-endian)
 * - u16[0] = top counter (increments when reaching top of movement)
 * - u16[1] = unknown  
 * - u16[2] = complete counter (increments when rep is complete at bottom)
 *
 * KEY INSIGHT: The machine itself counts the reps! We just track the counters.
 */
class WorkoutModeTest {

    private lateinit var handler: RepCounterFromMachine
    
    @Before
    fun setup() {
        handler = RepCounterFromMachine()
    }
    
    /**
     * Helper to initialize handler and set baseline counter
     */
    private fun initHandler(warmupTarget: Int = 3, workingTarget: Int = 10, isJustLift: Boolean = false, stopAtTop: Boolean = false) {
        handler.configure(warmupTarget, workingTarget, isJustLift, stopAtTop)
        // Send initial notification to establish directional-counter baseline.
        handler.process(repsRomCount = 0, repsSetCount = 0, up = 0, down = 0)
    }

    private fun completeWarmup(count: Int = 3) {
        for (i in 1..count) {
            handler.process(repsRomCount = i, repsSetCount = 0, up = i, down = i)
        }
    }

    private fun completeWorkingReps(count: Int, warmupTarget: Int = 3, firstDirectionCounter: Int = warmupTarget + 1) {
        for (rep in 1..count) {
            val directionCounter = firstDirectionCounter + rep - 1
            handler.process(
                repsRomCount = warmupTarget,
                repsSetCount = rep,
                up = directionCounter,
                down = directionCounter
            )
        }
    }

    @Test
    fun `Old School mode - 3 warmup + 10 working reps`() {
        initHandler(warmupTarget = 3, workingTarget = 10, isJustLift = false)
        
        // Simulate 3 warmup reps from machine
        handler.process(repsRomCount = 1, repsSetCount = 0, up = 1, down = 1)
        assertEquals(1, handler.getRepCount().warmupReps)
        assertEquals(0, handler.getRepCount().workingReps)
        
        handler.process(repsRomCount = 2, repsSetCount = 0, up = 2, down = 2)
        assertEquals(2, handler.getRepCount().warmupReps)
        
        handler.process(repsRomCount = 3, repsSetCount = 0, up = 3, down = 3)
        assertEquals(3, handler.getRepCount().warmupReps)
        assertTrue(handler.getRepCount().isWarmupComplete)
        
        // Now 10 working reps
        completeWorkingReps(10)
        
        assertEquals(3, handler.getRepCount().warmupReps)
        assertEquals(10, handler.getRepCount().workingReps)
        assertTrue("Should auto-stop at target", handler.shouldStopWorkout())
    }

    @Test
    fun `Pump mode - 3 warmup + 20 working reps`() {
        initHandler(warmupTarget = 3, workingTarget = 20, isJustLift = false)
        
        completeWarmup()
        
        // Complete 20 pump reps
        completeWorkingReps(20)
        
        assertEquals(20, handler.getRepCount().workingReps)
        assertTrue(handler.shouldStopWorkout())
    }

    @Test
    fun `TUT mode - 3 warmup + 6 working reps`() {
        initHandler(warmupTarget = 3, workingTarget = 6, isJustLift = false)
        
        completeWarmup()
        
        // Working reps (TUT typically has fewer reps but longer time under tension)
        completeWorkingReps(6)
        
        assertEquals(6, handler.getRepCount().workingReps)
        assertTrue(handler.shouldStopWorkout())
    }

    @Test
    fun `TUT Beast mode - 3 warmup + 3 working reps`() {
        initHandler(warmupTarget = 3, workingTarget = 3, isJustLift = false)
        
        completeWarmup()
        
        // Working reps (Beast mode is very low reps with maximum intensity)
        completeWorkingReps(3)
        
        assertEquals(3, handler.getRepCount().workingReps)
        assertTrue(handler.shouldStopWorkout())
    }

    @Test
    fun `Eccentric Only mode - 3 warmup + 8 working reps`() {
        initHandler(warmupTarget = 3, workingTarget = 8, isJustLift = false)
        
        completeWarmup()
        
        // Eccentric reps
        completeWorkingReps(8)
        
        assertEquals(8, handler.getRepCount().workingReps)
        assertTrue(handler.shouldStopWorkout())
    }

    @Test
    fun `Echo mode - 3 warmup + 12 working reps`() {
        initHandler(warmupTarget = 3, workingTarget = 12, isJustLift = false)
        
        completeWarmup()
        
        // Echo reps
        completeWorkingReps(12)
        
        assertEquals(12, handler.getRepCount().workingReps)
        assertTrue(handler.shouldStopWorkout())
    }

    @Test
    fun `Just Lift mode - never auto-stops`() {
        initHandler(warmupTarget = 3, workingTarget = 0, isJustLift = true)
        
        completeWarmup()
        
        // Keep going indefinitely
        for (rep in 1..97) {
            handler.process(repsRomCount = 3, repsSetCount = rep, up = 3 + rep, down = 3 + rep)
            assertFalse("Just Lift should never auto-stop", handler.shouldStopWorkout())
        }
        
        assertEquals(97, handler.getRepCount().workingReps) // 100 - 3 warmup
    }

    @Test
    fun `Modern mode - pending top rep waits for machine confirmation`() {
        initHandler(warmupTarget = 3, workingTarget = 5, isJustLift = false, stopAtTop = true)
        
        completeWarmup()

        // Do 4 complete working reps
        completeWorkingReps(4)

        assertEquals(4, handler.getRepCount().workingReps)

        // Top movement previews the final rep, but modern mode waits for the
        // machine set counter before confirming the rep or stopping.
        handler.process(repsRomCount = 3, repsSetCount = 4, up = 8, down = 7, posA = 800f, posB = 800f)
        
        assertFalse("Pending top rep should not stop before machine confirmation", handler.shouldStopWorkout())
        assertEquals(4, handler.getRepCount().workingReps)
        assertTrue(handler.getRepCount().hasPendingRep)

        handler.process(repsRomCount = 3, repsSetCount = 5, up = 8, down = 8, posA = 500f, posB = 500f)

        assertTrue("Should stop when machine confirms final rep", handler.shouldStopWorkout())
        assertEquals(5, handler.getRepCount().workingReps)
    }

    @Test
    fun `Counter wrap-around at 65535`() {
        handler.configure(warmupTarget = 0, workingTarget = 100, isJustLift = false, stopAtTop = false)
        handler.process(repsRomCount = 0, repsSetCount = 0, up = 65532, down = 65532, isLegacyFormat = true)
        
        // Start near max u16 value
        handler.process(repsRomCount = 0, repsSetCount = 0, up = 65533, down = 65533, isLegacyFormat = true)
        assertEquals(1, handler.getRepCount().workingReps)
        
        handler.process(repsRomCount = 0, repsSetCount = 0, up = 65534, down = 65534, isLegacyFormat = true)
        assertEquals(2, handler.getRepCount().workingReps)
        
        handler.process(repsRomCount = 0, repsSetCount = 0, up = 65535, down = 65535, isLegacyFormat = true)
        assertEquals(3, handler.getRepCount().workingReps)
        
        // Wrap to 0
        handler.process(repsRomCount = 0, repsSetCount = 0, up = 0, down = 0, isLegacyFormat = true)
        assertEquals(4, handler.getRepCount().workingReps)
        
        // Continue after wrap
        handler.process(repsRomCount = 0, repsSetCount = 0, up = 1, down = 1, isLegacyFormat = true)
        assertEquals(5, handler.getRepCount().workingReps)
        
        handler.process(repsRomCount = 0, repsSetCount = 0, up = 2, down = 2, isLegacyFormat = true)
        assertEquals(6, handler.getRepCount().workingReps)
    }

    @Test
    fun `No spurious reps from duplicate notifications`() {
        initHandler(warmupTarget = 3, workingTarget = 10, isJustLift = false)
        
        // First rep
        handler.process(repsRomCount = 1, repsSetCount = 0, up = 1, down = 1)
        assertEquals(1, handler.getRepCount().warmupReps)
        
        // Same notification again (BLE retransmission)
        handler.process(repsRomCount = 1, repsSetCount = 0, up = 1, down = 1)
        assertEquals(1, handler.getRepCount().warmupReps) // Should still be 1
        
        // Next rep
        handler.process(repsRomCount = 2, repsSetCount = 0, up = 2, down = 2)
        assertEquals(2, handler.getRepCount().warmupReps)
    }

    @Test
    fun `Invalid data - too short`() {
        // This test is no longer applicable since we're not parsing ByteArray
        // The parsing happens in BleManager, not in RepCounterFromMachine
        // Test removed - counters are always valid Ints
    }

    @Test
    fun `Machine counts during warmup - app calibrates range`() {
        initHandler(warmupTarget = 3, workingTarget = 10, isJustLift = false)
        
        // During warmup, machine sends notifications AND app tracks positions
        // Warmup rep 1
        handler.process(repsRomCount = 1, repsSetCount = 0, up = 1, down = 1, posA = 850f, posB = 850f)

        // Warmup rep 2
        handler.process(repsRomCount = 2, repsSetCount = 0, up = 2, down = 2, posA = 830f, posB = 830f)

        // Warmup rep 3
        handler.process(repsRomCount = 3, repsSetCount = 0, up = 3, down = 3, posA = 840f, posB = 840f)

        // Range should be calibrated from warmup (average ~840)
        val range = handler.getCalibratedTopPosition()
        assertNotNull(range)
        assertTrue("Top should be around 840", range!! in 830f..850f)
    }

    @Test
    fun `Countdown before workout start - no rep counting`() {
        // During countdown, position data flows but no rep notifications
        // This is a UI concern - handler only processes notifications from machine
        
        initHandler(warmupTarget = 3, workingTarget = 10, isJustLift = false)
        
        // No notifications (beyond init) = no reps counted  
        assertEquals(0, handler.getRepCount().warmupReps)
        assertEquals(0, handler.getRepCount().workingReps)
    }

    @Test
    fun `Complete workflow - Old School mode end-to-end`() {
        initHandler(warmupTarget = 3, workingTarget = 10, isJustLift = false)
        
        var lastEvent: RepEvent? = null
        handler.onRepEvent = { event -> lastEvent = event }
        
        // Phase 1: Warmup
        completeWarmup()
        assertTrue(handler.getRepCount().isWarmupComplete)
        assertNotNull(lastEvent)
        assertEquals(RepType.WARMUP_COMPLETE, lastEvent?.type)
        
        // Phase 2: Working reps
        completeWorkingReps(10)
        
        // Phase 3: Auto-complete at target
        assertTrue(handler.shouldStopWorkout())
        assertEquals(RepType.WORKOUT_COMPLETE, lastEvent?.type)
        
        // Final counts
        val final = handler.getRepCount()
        assertEquals(3, final.warmupReps)
        assertEquals(10, final.workingReps)
        assertEquals(10, final.totalReps) // Updated after Session 17 rep counting changes
    }
}

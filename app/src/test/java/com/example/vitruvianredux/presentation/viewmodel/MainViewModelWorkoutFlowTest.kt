package com.example.vitruvianredux.presentation.viewmodel

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.example.vitruvianredux.data.preferences.PreferencesManager
import com.example.vitruvianredux.data.repository.BleRepository
import com.example.vitruvianredux.data.repository.ExerciseRepository
import com.example.vitruvianredux.data.repository.PersonalRecordRepository
import com.example.vitruvianredux.data.repository.WorkoutRepository
import com.example.vitruvianredux.domain.model.*
import com.example.vitruvianredux.domain.usecase.RepCounterFromMachine
import com.example.vitruvianredux.util.DataBackupManager
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class MainViewModelWorkoutFlowTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var application: Application
    private lateinit var bleRepository: BleRepository
    private lateinit var workoutRepository: WorkoutRepository
    private lateinit var exerciseRepository: ExerciseRepository
    private lateinit var personalRecordRepository: PersonalRecordRepository
    private lateinit var repCounter: RepCounterFromMachine
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var dataBackupManager: DataBackupManager
    private lateinit var viewModel: MainViewModel

    private val handleStateFlow = MutableStateFlow(com.example.vitruvianredux.data.ble.HandleState.Released)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        application = ApplicationProvider.getApplicationContext()
        bleRepository = mockk(relaxed = true)
        workoutRepository = mockk(relaxed = true)
        exerciseRepository = mockk(relaxed = true)
        personalRecordRepository = mockk(relaxed = true)
        repCounter = mockk(relaxed = true)
        preferencesManager = mockk(relaxed = true)
        dataBackupManager = mockk(relaxed = true)

        // Setup common mock returns
        every { bleRepository.connectionState } returns MutableStateFlow(ConnectionState.Connected("Test Device", "00:11:22:33:44:55"))
        every { bleRepository.monitorData } returns flowOf() // Replaced below if needed
        every { bleRepository.repEvents } returns emptyFlow()
        every { bleRepository.scannedDevices } returns emptyFlow()
        every { bleRepository.handleState } returns handleStateFlow
        every { bleRepository.heuristicData } returns MutableStateFlow(null)
        every { bleRepository.deloadOccurredEvents } returns emptyFlow()

        // Use an actual flow that we can emit to for monitor data
        val monitorSharedFlow = kotlinx.coroutines.flow.MutableSharedFlow<WorkoutMetric>()
        every { bleRepository.monitorData } returns monitorSharedFlow

        every { workoutRepository.getRecentSessions(any()) } returns flowOf(emptyList())
        every { workoutRepository.getAllRoutines() } returns flowOf(emptyList())
        every { workoutRepository.getAllSessions() } returns flowOf(emptyList())
        every { workoutRepository.getAllPrograms() } returns flowOf(emptyList())
        every { workoutRepository.getActiveProgram() } returns flowOf(null)
        every { personalRecordRepository.getAllPRsGrouped() } returns flowOf(emptyList())

        every { preferencesManager.preferencesFlow } returns flowOf(UserPreferences())

        coEvery { bleRepository.startWorkout(any()) } returns Result.success(Unit)
        coEvery { bleRepository.stopWorkout() } returns Result.success(Unit)

        viewModel = MainViewModel(
            application,
            bleRepository,
            workoutRepository,
            exerciseRepository,
            personalRecordRepository,
            repCounter,
            preferencesManager,
            dataBackupManager
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun setViewModelField(name: String, value: Any?) {
        MainViewModel::class.java.getDeclaredField(name).apply {
            isAccessible = true
            set(viewModel, value)
        }
    }

    @Test
    fun `workout flow - starts countdown then becomes active`() = runTest {
        // Start workout
        viewModel.startWorkout(skipCountdown = false)

        // Initial state should be Countdown(5)
        advanceTimeBy(100)
        assertIs<WorkoutState.Countdown>(viewModel.workoutState.value)
        assertEquals(5, (viewModel.workoutState.value as WorkoutState.Countdown).secondsRemaining)

        // Advance 5 seconds
        advanceTimeBy(5500)

        // Should be Active
        assertIs<WorkoutState.Active>(viewModel.workoutState.value)

        // Verify BLE startWorkout was called
        coVerify { bleRepository.startWorkout(any()) }
    }

    @Test
    fun `workout flow - stopWorkout transitions to Completed and saves session`() = runTest {
        // Start workout and skip countdown
        viewModel.startWorkout(skipCountdown = true)
        advanceUntilIdle()

        assertIs<WorkoutState.Active>(viewModel.workoutState.value)

        // Stop workout
        viewModel.stopWorkout()
        advanceUntilIdle()

        // State should be Completed
        assertIs<WorkoutState.Completed>(viewModel.workoutState.value)

        // Verify session saved
        coVerify { workoutRepository.saveSession(any()) }
        coVerify { bleRepository.stopWorkout() }
    }

    @Test
    fun `just lift mode - auto-start triggers after 5 seconds of handle grab`() = runTest {
        // Setup Just Lift mode
        viewModel.prepareForJustLift()
        advanceUntilIdle()

        assertTrue(viewModel.workoutParameters.value.isJustLift)
        assertEquals(WorkoutState.Idle, viewModel.workoutState.value)

        // Mock handle grab
        handleStateFlow.value = com.example.vitruvianredux.data.ble.HandleState.Grabbed

        // Countdown should start
        advanceTimeBy(100)
        assertEquals(5, viewModel.autoStartCountdown.value)

        // Wait for countdown to finish (5 seconds)
        advanceTimeBy(5500)

        // Should be Active
        assertIs<WorkoutState.Active>(viewModel.workoutState.value)
        assertTrue(viewModel.workoutParameters.value.isJustLift)
    }

    @Test
    fun `auto-stop - triggers after stall detection duration`() = runTest {
        // Start Just Lift workout
        viewModel.startWorkout(skipCountdown = true, isJustLiftMode = true)
        advanceUntilIdle()

        assertIs<WorkoutState.Active>(viewModel.workoutState.value)

        // Configure repCounter mock to return hasMeaningfulRange = true
        every { repCounter.hasMeaningfulRange() } returns true

        // Simulate a metric with very low velocity (stalled)
        // Note: MainViewModel uses checkAutoStop which is called from bleRepository.monitorData collector
        val monitorSharedFlow = bleRepository.monitorData as kotlinx.coroutines.flow.MutableSharedFlow<WorkoutMetric>

        val stalledMetric = WorkoutMetric(
            loadA = 10f, loadB = 10f, positionA = 100f, positionB = 100f, velocityA = 0.0, velocityB = 0.0
        )

        // We need handles to be extended for stall detection to trigger
        // STALL_MIN_POSITION = 10.0

        monitorSharedFlow.emit(stalledMetric)
        advanceUntilIdle()

        // Production code uses System.currentTimeMillis(), not the coroutine test clock.
        setViewModelField("stallDetectionStartTime", System.currentTimeMillis() - 5_500L)

        monitorSharedFlow.emit(stalledMetric)
        runCurrent()

        // Should have triggered auto-stop and be in SetSummary
        assertIs<WorkoutState.SetSummary>(viewModel.workoutState.value)
    }
}

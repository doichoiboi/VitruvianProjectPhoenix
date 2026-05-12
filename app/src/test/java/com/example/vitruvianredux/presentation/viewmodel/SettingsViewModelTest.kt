package com.example.vitruvianredux.presentation.viewmodel

import android.net.Uri
import com.example.vitruvianredux.data.local.BackupContent
import com.example.vitruvianredux.data.local.BackupData
import com.example.vitruvianredux.data.preferences.PreferencesManager
import com.example.vitruvianredux.data.repository.BleRepository
import com.example.vitruvianredux.data.repository.WorkoutRepository
import com.example.vitruvianredux.domain.model.UserPreferences
import com.example.vitruvianredux.domain.model.WeightUnit
import com.example.vitruvianredux.util.DataBackupManager
import com.example.vitruvianredux.util.ImportResult
import com.google.common.truth.Truth.assertThat
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class SettingsViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var preferencesFlow: MutableStateFlow<UserPreferences>
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var bleRepository: BleRepository
    private lateinit var workoutRepository: WorkoutRepository
    private lateinit var dataBackupManager: DataBackupManager
    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        preferencesFlow = MutableStateFlow(UserPreferences())
        preferencesManager = mockk(relaxed = true)
        bleRepository = mockk(relaxed = true)
        workoutRepository = mockk(relaxed = true)
        dataBackupManager = mockk(relaxed = true)

        every { preferencesManager.preferencesFlow } returns preferencesFlow

        viewModel = SettingsViewModel(
            preferencesManager = preferencesManager,
            bleRepository = bleRepository,
            workoutRepository = workoutRepository,
            dataBackupManager = dataBackupManager
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        clearAllMocks()
    }

    @Test
    fun `ui state mirrors user preferences`() = runTest {
        preferencesFlow.value = UserPreferences(
            weightUnit = WeightUnit.LB,
            autoplayEnabled = false,
            stopAtTop = true,
            enableVideoPlayback = false,
            beepsEnabled = false
        )
        advanceUntilIdle()

        assertThat(viewModel.uiState.value).isEqualTo(
            SettingsUiState(
                weightUnit = WeightUnit.LB,
                autoplayEnabled = false,
                stopAtTop = true,
                enableVideoPlayback = false,
                beepsEnabled = false
            )
        )
    }

    @Test
    fun `settings changes delegate to preferences manager`() = runTest {
        viewModel.setWeightUnit(WeightUnit.LB)
        viewModel.setAutoplayEnabled(false)
        viewModel.setStopAtTop(true)
        viewModel.setEnableVideoPlayback(false)
        viewModel.setBeepsEnabled(false)
        advanceUntilIdle()

        coVerify { preferencesManager.setWeightUnit(WeightUnit.LB) }
        coVerify { preferencesManager.setAutoplayEnabled(false) }
        coVerify { preferencesManager.setStopAtTop(true) }
        coVerify { preferencesManager.setEnableVideoPlayback(false) }
        coVerify { preferencesManager.setBeepsEnabled(false) }
    }

    @Test
    fun `machine and data actions delegate to route dependencies`() = runTest {
        coEvery { bleRepository.setColorScheme(2) } returns Result.success(Unit)
        coEvery { workoutRepository.deleteAllWorkouts() } returns Result.success(Unit)

        viewModel.setColorScheme(2)
        viewModel.deleteAllWorkouts()
        advanceUntilIdle()

        coVerify { bleRepository.setColorScheme(2) }
        coVerify { workoutRepository.deleteAllWorkouts() }
    }

    @Test
    fun `export emits share effect and clears exporting state`() = runTest {
        val backup = BackupData(
            exportedAt = "2026-05-12T00:00:00Z",
            appVersion = "test",
            data = BackupContent()
        )
        val uri = Uri.parse("content://vitruvian/export.json")
        val effects = mutableListOf<SettingsEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effects.toList(effects)
        }
        coEvery { dataBackupManager.exportAllData() } returns backup
        coEvery { dataBackupManager.saveToCache(backup) } returns Result.success(uri)

        viewModel.exportAllData()
        advanceUntilIdle()

        assertThat(effects).containsExactly(SettingsEffect.ShareExport(uri))
        assertThat(viewModel.uiState.value.isExporting).isFalse()
    }

    @Test
    fun `import success shows result dialog until dismissed`() = runTest {
        val uri = Uri.parse("content://vitruvian/import.json")
        val result = ImportResult(
            sessionsImported = 1,
            sessionsSkipped = 2,
            metricsImported = 3,
            routinesImported = 4,
            routinesSkipped = 5,
            routineExercisesImported = 6,
            programsImported = 7,
            programsSkipped = 8,
            programDaysImported = 9,
            personalRecordsImported = 10,
            personalRecordsSkipped = 11
        )
        coEvery { dataBackupManager.importFromUri(uri) } returns Result.success(result)

        viewModel.importFromUri(uri)
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.importResult).isEqualTo(result)
        assertThat(viewModel.uiState.value.showImportResultDialog).isTrue()
        assertThat(viewModel.uiState.value.isImporting).isFalse()

        viewModel.dismissImportResult()
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.importResult).isNull()
        assertThat(viewModel.uiState.value.showImportResultDialog).isFalse()
    }

    @Test
    fun `import failure shows empty result dialog`() = runTest {
        val uri = Uri.parse("content://vitruvian/import.json")
        coEvery { dataBackupManager.importFromUri(uri) } returns Result.failure(IllegalStateException("bad file"))

        viewModel.importFromUri(uri)
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.importResult?.totalImported).isEqualTo(0)
        assertThat(viewModel.uiState.value.importResult?.totalSkipped).isEqualTo(0)
        assertThat(viewModel.uiState.value.showImportResultDialog).isTrue()
        assertThat(viewModel.uiState.value.isImporting).isFalse()
    }
}

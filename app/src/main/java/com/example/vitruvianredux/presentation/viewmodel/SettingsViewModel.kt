package com.example.vitruvianredux.presentation.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vitruvianredux.data.preferences.PreferencesManager
import com.example.vitruvianredux.data.repository.BleRepository
import com.example.vitruvianredux.data.repository.WorkoutRepository
import com.example.vitruvianredux.domain.model.UserPreferences
import com.example.vitruvianredux.domain.model.WeightUnit
import com.example.vitruvianredux.util.DataBackupManager
import com.example.vitruvianredux.util.ImportResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

data class SettingsUiState(
    val weightUnit: WeightUnit = WeightUnit.KG,
    val autoplayEnabled: Boolean = true,
    val stopAtTop: Boolean = false,
    val enableVideoPlayback: Boolean = true,
    val beepsEnabled: Boolean = true,
    val isExporting: Boolean = false,
    val isImporting: Boolean = false,
    val importResult: ImportResult? = null,
    val showImportResultDialog: Boolean = false
)

sealed interface SettingsEffect {
    data class ShareExport(val uri: Uri) : SettingsEffect
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val bleRepository: BleRepository,
    private val workoutRepository: WorkoutRepository,
    private val dataBackupManager: DataBackupManager
) : ViewModel() {

    private val isExporting = MutableStateFlow(false)
    private val isImporting = MutableStateFlow(false)
    private val importResult = MutableStateFlow<ImportResult?>(null)
    private val showImportResultDialog = MutableStateFlow(false)

    private val _effects = MutableSharedFlow<SettingsEffect>(extraBufferCapacity = 1)
    val effects = _effects.asSharedFlow()

    val uiState: StateFlow<SettingsUiState> = combine(
        preferencesManager.preferencesFlow,
        isExporting,
        isImporting,
        importResult,
        showImportResultDialog
    ) { preferences, exporting, importing, result, showDialog ->
        SettingsUiState(
            weightUnit = preferences.weightUnit,
            autoplayEnabled = preferences.autoplayEnabled,
            stopAtTop = preferences.stopAtTop,
            enableVideoPlayback = preferences.enableVideoPlayback,
            beepsEnabled = preferences.beepsEnabled,
            isExporting = exporting,
            isImporting = importing,
            importResult = result,
            showImportResultDialog = showDialog
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = SettingsUiState()
    )

    fun setWeightUnit(unit: WeightUnit) {
        viewModelScope.launch {
            preferencesManager.setWeightUnit(unit)
        }
    }

    fun setAutoplayEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setAutoplayEnabled(enabled)
        }
    }

    fun setStopAtTop(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setStopAtTop(enabled)
        }
    }

    fun setEnableVideoPlayback(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setEnableVideoPlayback(enabled)
        }
    }

    fun setBeepsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setBeepsEnabled(enabled)
        }
    }

    fun setColorScheme(schemeIndex: Int) {
        viewModelScope.launch {
            bleRepository.setColorScheme(schemeIndex)
        }
    }

    fun deleteAllWorkouts() {
        viewModelScope.launch {
            workoutRepository.deleteAllWorkouts()
        }
    }

    fun exportAllData() {
        viewModelScope.launch {
            isExporting.value = true
            try {
                val backup = dataBackupManager.exportAllData()
                val uriResult = dataBackupManager.saveToCache(backup)

                uriResult.onSuccess { uri ->
                    _effects.emit(SettingsEffect.ShareExport(uri))
                    Timber.d("Export successful: ${backup.data.workoutSessions.size} sessions, ${backup.data.routines.size} routines")
                }.onFailure { e ->
                    Timber.e(e, "Failed to save backup file")
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to export data")
            } finally {
                isExporting.value = false
            }
        }
    }

    fun importFromUri(uri: Uri) {
        viewModelScope.launch {
            isImporting.value = true
            try {
                val result = dataBackupManager.importFromUri(uri)

                result.onSuccess { result ->
                    importResult.value = result
                    showImportResultDialog.value = true
                    Timber.d("Import successful: ${result.sessionsImported} sessions, ${result.routinesImported} routines")
                }.onFailure { e ->
                    Timber.e(e, "Failed to import data")
                    importResult.value = emptyImportResult()
                    showImportResultDialog.value = true
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to import data")
                importResult.value = emptyImportResult()
                showImportResultDialog.value = true
            } finally {
                isImporting.value = false
            }
        }
    }

    fun dismissImportResult() {
        showImportResultDialog.value = false
        importResult.value = null
    }

    private fun emptyImportResult(): ImportResult = ImportResult(
        sessionsImported = 0,
        sessionsSkipped = 0,
        metricsImported = 0,
        routinesImported = 0,
        routinesSkipped = 0,
        routineExercisesImported = 0,
        programsImported = 0,
        programsSkipped = 0,
        programDaysImported = 0,
        personalRecordsImported = 0,
        personalRecordsSkipped = 0
    )
}

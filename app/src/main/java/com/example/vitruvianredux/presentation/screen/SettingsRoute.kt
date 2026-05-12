package com.example.vitruvianredux.presentation.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.vitruvianredux.presentation.navigation.NavigationRoutes
import com.example.vitruvianredux.presentation.viewmodel.MainViewModel

@Composable
fun SettingsRoute(
    navController: NavController,
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val weightUnit by viewModel.weightUnit.collectAsState()
    val userPreferences by viewModel.userPreferences.collectAsState()
    val isExporting by viewModel.isExporting.collectAsState()
    val isImporting by viewModel.isImporting.collectAsState()
    val importResult by viewModel.importResult.collectAsState()
    val showImportResultDialog by viewModel.showImportResultDialog.collectAsState()

    SettingsTab(
        weightUnit = weightUnit,
        autoplayEnabled = userPreferences.autoplayEnabled,
        stopAtTop = userPreferences.stopAtTop,
        enableVideoPlayback = userPreferences.enableVideoPlayback,
        beepsEnabled = userPreferences.beepsEnabled,
        onWeightUnitChange = { viewModel.setWeightUnit(it) },
        onAutoplayChange = { viewModel.setAutoplayEnabled(it) },
        onStopAtTopChange = { viewModel.setStopAtTop(it) },
        onEnableVideoPlaybackChange = { viewModel.setEnableVideoPlayback(it) },
        onBeepsEnabledChange = { viewModel.setBeepsEnabled(it) },
        onColorSchemeChange = { viewModel.setColorScheme(it) },
        onDeleteAllWorkouts = { viewModel.deleteAllWorkouts() },
        onNavigateToConnectionLogs = { navController.navigate(NavigationRoutes.ConnectionLogs.route) },
        onNavigateToProtocolTester = { navController.navigate(NavigationRoutes.ProtocolTester.route) },
        isExporting = isExporting,
        isImporting = isImporting,
        importResult = importResult,
        showImportResultDialog = showImportResultDialog,
        onExportData = { viewModel.exportAllData() },
        onImportData = { uri -> viewModel.importFromUri(uri) },
        onDismissImportResult = { viewModel.dismissImportResult() },
        modifier = modifier
    )
}

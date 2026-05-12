package com.example.vitruvianredux.presentation.screen

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.vitruvianredux.presentation.navigation.NavigationRoutes
import com.example.vitruvianredux.presentation.viewmodel.SettingsViewModel

@Composable
fun SettingsRoute(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(
        viewModelStoreOwner = LocalContext.current.findActivity()
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    uiState.pendingExportUri?.let { uri ->
        LaunchedEffect(uri, context) {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/json"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val chooserIntent = Intent.createChooser(shareIntent, "Export Workout Data")
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooserIntent)
            viewModel.onExportShareHandled(uri)
        }
    }

    SettingsTab(
        weightUnit = uiState.weightUnit,
        autoplayEnabled = uiState.autoplayEnabled,
        stopAtTop = uiState.stopAtTop,
        enableVideoPlayback = uiState.enableVideoPlayback,
        beepsEnabled = uiState.beepsEnabled,
        onWeightUnitChange = { viewModel.setWeightUnit(it) },
        onAutoplayChange = { viewModel.setAutoplayEnabled(it) },
        onStopAtTopChange = { viewModel.setStopAtTop(it) },
        onEnableVideoPlaybackChange = { viewModel.setEnableVideoPlayback(it) },
        onBeepsEnabledChange = { viewModel.setBeepsEnabled(it) },
        onColorSchemeChange = { viewModel.setColorScheme(it) },
        onDeleteAllWorkouts = { viewModel.deleteAllWorkouts() },
        onNavigateToConnectionLogs = { navController.navigate(NavigationRoutes.ConnectionLogs.route) },
        onNavigateToProtocolTester = { navController.navigate(NavigationRoutes.ProtocolTester.route) },
        isExporting = uiState.isExporting,
        isImporting = uiState.isImporting,
        importResult = uiState.importResult,
        showImportResultDialog = uiState.showImportResultDialog,
        onExportData = { viewModel.exportAllData() },
        onImportData = { uri -> viewModel.importFromUri(uri) },
        onDismissImportResult = { viewModel.dismissImportResult() },
        modifier = modifier
    )
}

private tailrec fun Context.findActivity(): ComponentActivity = when (this) {
    is ComponentActivity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> error("SettingsRoute must be hosted by a ComponentActivity")
}

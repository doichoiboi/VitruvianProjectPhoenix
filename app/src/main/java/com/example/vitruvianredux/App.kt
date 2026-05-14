package com.example.vitruvianredux

import android.view.Window
import android.view.WindowManager
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.vitruvianredux.domain.model.WorkoutState
import com.example.vitruvianredux.presentation.screen.AppScaffold
import com.example.vitruvianredux.presentation.screen.LargeSplashScreen
import com.example.vitruvianredux.presentation.viewmodel.MainViewModel
import com.example.vitruvianredux.ui.theme.VitruvianProjectPhoenixTheme
import com.example.vitruvianredux.ui.theme.resolveDarkTheme
import kotlinx.coroutines.delay
import timber.log.Timber
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun App(
    window: Window,
    mainViewModel: MainViewModel = hiltViewModel()
) {
    val themeMode by mainViewModel.themeMode.collectAsState()
    val workoutState by mainViewModel.workoutState.collectAsState()
    val useDarkColors = themeMode.resolveDarkTheme(isSystemInDarkTheme())
    var showLargeSplash by remember { mutableStateOf(true) }

    DisposableEffect(window, useDarkColors) {
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        controller.isAppearanceLightStatusBars = !useDarkColors
        controller.isAppearanceLightNavigationBars = !useDarkColors

        onDispose {}
    }

    // Keep screen on during active workouts so screen lock does not interrupt BLE.
    DisposableEffect(window, workoutState) {
        val shouldKeepScreenOn = when (workoutState) {
            is WorkoutState.Active,
            is WorkoutState.Countdown,
            is WorkoutState.Resting,
            is WorkoutState.Initializing -> true
            else -> false
        }

        if (shouldKeepScreenOn) {
            Timber.d("Keeping screen on during workout state: $workoutState")
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            Timber.d("Releasing screen keep-on for state: $workoutState")
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }

        onDispose {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    LaunchedEffect(Unit) {
        delay(900.milliseconds)
        showLargeSplash = false
    }

    VitruvianProjectPhoenixTheme(themeMode = themeMode) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            if (showLargeSplash) {
                LargeSplashScreen(visible = true)
            } else {
                AppScaffold(
                    viewModel = mainViewModel,
                    themeMode = themeMode,
                    onThemeModeChange = mainViewModel::setThemeMode
                )
            }
        }
    }
}

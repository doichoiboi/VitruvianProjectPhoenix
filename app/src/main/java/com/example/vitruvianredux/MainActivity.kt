package com.example.vitruvianredux

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Install splash screen BEFORE super.onCreate() to prevent black screen
        // This keeps the splash visible until the first frame is drawn
        installSplashScreen()

        super.onCreate(savedInstanceState)

        // Enable edge-to-edge display for proper Material 3 insets handling
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            App(window = window)
        }
    }
}

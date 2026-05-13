package com.example.vitruvianredux.presentation.workout

import java.util.Locale

object JustLiftRestElapsedFormatter {
    fun label(seconds: Int): String = "Resting ${formatDuration(seconds)}"

    private fun formatDuration(seconds: Int): String {
        val safeSeconds = seconds.coerceAtLeast(0)
        val minutes = safeSeconds / 60
        val remainingSeconds = safeSeconds % 60
        return String.format(Locale.US, "%d:%02d", minutes, remainingSeconds)
    }
}

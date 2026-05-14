package com.example.vitruvianredux.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.vitruvianredux.data.repository.ExerciseRepository
import com.example.vitruvianredux.data.repository.PersonalRecordRepository
import com.example.vitruvianredux.domain.model.Routine
import com.example.vitruvianredux.domain.model.WeightUnit
import com.example.vitruvianredux.ui.theme.ThemeMode
import com.example.vitruvianredux.ui.theme.appBrushes

/**
 * Daily Routines screen - view and manage pre-built routines.
 * This screen wraps the existing RoutinesTab functionality.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyRoutinesScreen(
    themeMode: ThemeMode,
    routines: List<Routine>,
    exerciseRepository: ExerciseRepository,
    personalRecordRepository: PersonalRecordRepository,
    weightUnit: WeightUnit,
    enableVideoPlayback: Boolean,
    formatWeight: (Float, WeightUnit) -> String,
    kgToDisplay: (Float, WeightUnit) -> Float,
    displayToKg: (Float, WeightUnit) -> Float,
    onStartWorkout: (Routine) -> Unit,
    onDeleteRoutine: (String) -> Unit,
    onSaveRoutine: (Routine) -> Unit,
    onUpdateRoutine: (Routine) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.appBrushes.screenBackground)
    ) {
        // Reuse RoutinesTab content
        RoutinesTab(
            routines = routines,
            exerciseRepository = exerciseRepository,
            personalRecordRepository = personalRecordRepository,
            formatWeight = formatWeight,
            weightUnit = weightUnit,
            enableVideoPlayback = enableVideoPlayback,
            kgToDisplay = kgToDisplay,
            displayToKg = displayToKg,
            onStartWorkout = onStartWorkout,
            onDeleteRoutine = onDeleteRoutine,
            onSaveRoutine = onSaveRoutine,
            onUpdateRoutine = onUpdateRoutine,
            themeMode = themeMode,
            modifier = Modifier.fillMaxSize()
        )

    }
}

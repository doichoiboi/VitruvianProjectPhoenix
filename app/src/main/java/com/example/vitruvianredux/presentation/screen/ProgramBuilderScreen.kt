package com.example.vitruvianredux.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.vitruvianredux.domain.model.Routine
import com.example.vitruvianredux.ui.theme.Spacing
import com.example.vitruvianredux.ui.theme.ThemeMode
import com.example.vitruvianredux.ui.theme.appBrushes
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.*

/**
 * Program Builder screen - create or edit a weekly program.
 * Allows user to assign routines to each day of the week.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgramBuilderScreen(
    programName: String,
    onProgramNameChange: (String) -> Unit,
    routines: List<Routine>,
    dailyRoutines: Map<DayOfWeek, Routine?>,
    onDailyRoutinesChange: (Map<DayOfWeek, Routine?>) -> Unit,
    themeMode: ThemeMode
) {
    var showRoutinePicker by remember { mutableStateOf(false) }
    var selectedDay by remember { mutableStateOf<DayOfWeek?>(null) }

    // Track scroll state to show scroll indicator
    val listState = rememberLazyListState()

    // Determine if we can scroll down (more content below)
    val canScrollDown by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()

            // Can scroll down if the last visible item is not the last item in the list
            lastVisibleItem?.let {
                it.index < layoutInfo.totalItemsCount - 1
            } ?: false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.appBrushes.screenBackground)
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(Spacing.medium), // Removed padding(padding) as no local scaffold
            verticalArrangement = Arrangement.spacedBy(Spacing.medium)
        ) {
            item {
                // Program Name Input
                OutlinedTextField(
                    value = programName,
                    onValueChange = onProgramNameChange,
                    label = { Text("Program Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
            }

            item {
                Text(
                    "Schedule workouts for each day",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            // 7 day cards
            itemsIndexed(DayOfWeek.entries.toList()) { index, day ->
                DayRoutineCard(
                    day = day,
                    routine = dailyRoutines[day],
                    onSelectRoutine = {
                        selectedDay = day
                        showRoutinePicker = true
                    },
                    onClearRoutine = {
                        onDailyRoutinesChange(dailyRoutines.toMutableMap().apply {
                            put(day, null)
                        })
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(Spacing.medium))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Spacing.medium)
                    ) {
                        Text(
                            "Program Summary",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(Spacing.small))

                        val workoutDays = dailyRoutines.values.filterNotNull().size
                        val restDays = 7 - workoutDays

                        Text(
                            "$workoutDays workout days, $restDays rest days",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // Scroll indicator - gradient fade at bottom when more content is available
        if (canScrollDown) {
            val bottomColor = MaterialTheme.colorScheme.background
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                bottomColor.copy(alpha = 0.85f),
                                bottomColor
                            )
                        )
                    )
                    .zIndex(1f)
            ) {
                // Down arrow icon to indicate more content
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 12.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Scroll down for more",
                        modifier = Modifier
                            .padding(8.dp)
                            .size(28.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
        if (showRoutinePicker && selectedDay != null) {
            AlertDialog(
                onDismissRequest = { showRoutinePicker = false },
                title = { 
                    Text(
                        "Select Routine for ${selectedDay!!.getDisplayName(TextStyle.FULL, Locale.getDefault())}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    ) 
                },
                text = {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(Spacing.small)
                    ) {
                        if (routines.isEmpty()) {
                            item {
                                Text(
                                    "No routines available. Create a routine first.",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            itemsIndexed(routines) { _, routine ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            onDailyRoutinesChange(dailyRoutines.toMutableMap().apply {
                                                put(selectedDay!!, routine)
                                            })
                                            showRoutinePicker = false
                                        },
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(Spacing.medium)
                                    ) {
                                        Text(
                                            routine.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            "${routine.exercises.size} exercises",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(
                        onClick = { showRoutinePicker = false },
                        modifier = Modifier.height(56.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            "Cancel",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                shape = RoundedCornerShape(16.dp)
            )
        }

    }
}

/**
 * Card for selecting a routine for a specific day.
 */
@Composable
fun DayRoutineCard(
    day: DayOfWeek,
    routine: Routine?,
    onSelectRoutine: () -> Unit,
    onClearRoutine: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelectRoutine),
        colors = CardDefaults.cardColors(
            containerColor = if (routine != null) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceContainerHighest
            }
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(
            2.dp,
            if (routine != null) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
            } else {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    day.getDisplayName(TextStyle.FULL, Locale.getDefault()),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                if (routine != null) {
                    Spacer(modifier = Modifier.height(Spacing.extraSmall))
                    Text(
                        routine.name,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        "${routine.exercises.size} exercises",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Spacer(modifier = Modifier.height(Spacing.extraSmall))
                    Text(
                        "Rest day",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (routine != null) {
                IconButton(onClick = onClearRoutine) {
                    Icon(
                        Icons.Default.Clear,
                        contentDescription = "Clear routine",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            } else {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add routine",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

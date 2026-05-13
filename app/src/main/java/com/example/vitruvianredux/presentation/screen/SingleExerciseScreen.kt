package com.example.vitruvianredux.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.vitruvianredux.data.preferences.SingleExerciseDefaults
import com.example.vitruvianredux.data.repository.ExerciseRepository
import com.example.vitruvianredux.data.repository.PersonalRecordRepository
import com.example.vitruvianredux.domain.model.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SingleExerciseScreen(
    exerciseRepository: ExerciseRepository,
    personalRecordRepository: PersonalRecordRepository,
    weightUnit: WeightUnit,
    enableVideoPlayback: Boolean,
    sessionEccentricLoad: EccentricLoad,
    kgToDisplay: (Float, WeightUnit) -> Float,
    displayToKg: (Float, WeightUnit) -> Float,
    formatWeight: (Float, WeightUnit) -> String,
    getSingleExerciseDefaults: suspend (exerciseId: String, cableConfig: String) -> SingleExerciseDefaults?,
    onStartWorkout: (RoutineExercise) -> Unit,
    modifier: Modifier = Modifier
) {
    var exerciseToConfig by remember { mutableStateOf<RoutineExercise?>(null) }
    var isLoadingDefaults by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    // Track current loading job to cancel on rapid selection changes
    var loadingJob by remember { mutableStateOf<Job?>(null) }

    // Local state for picker
    var searchQuery by remember { mutableStateOf("") }
    var selectedMuscleFilter by remember { mutableStateOf("All") }
    var selectedEquipmentFilter by remember { mutableStateOf("All") }
    var showFavoritesOnly by remember { mutableStateOf(false) }

    // Get exercises from repository
    val allExercises by remember(searchQuery, selectedMuscleFilter, showFavoritesOnly) {
        when {
            showFavoritesOnly -> exerciseRepository.getFavorites()
            searchQuery.isNotBlank() -> exerciseRepository.searchExercises(searchQuery)
            selectedMuscleFilter != "All" -> exerciseRepository.filterByMuscleGroup(selectedMuscleFilter)
            else -> exerciseRepository.getAllExercises()
        }
    }.collectAsState(initial = emptyList())

    // Apply equipment filter
    val exercises = remember(allExercises, selectedEquipmentFilter) {
        if (selectedEquipmentFilter != "All") {
            allExercises.filter { exercise ->
                val databaseValues = when (selectedEquipmentFilter) {
                    "Long Bar" -> listOf("BAR", "LONG_BAR", "BARBELL")
                    "Short Bar" -> listOf("SHORT_BAR")
                    "Ankle Strap" -> listOf("ANKLE_STRAP", "STRAPS")
                    "Handles" -> listOf("HANDLES", "SINGLE_HANDLE", "BOTH_HANDLES")
                    "Bench" -> listOf("BENCH")
                    "Rope" -> listOf("ROPE")
                    "Belt" -> listOf("BELT")
                    "Bodyweight" -> listOf("BODYWEIGHT")
                    else -> emptyList()
                }
                val equipmentList = exercise.equipment.uppercase().split(",").map { it.trim() }
                databaseValues.any { dbValue -> equipmentList.contains(dbValue.uppercase()) }
            }
        } else {
            allExercises
        }
    }

    // Trigger import
    LaunchedEffect(Unit) {
        exerciseRepository.importExercises()
    }

    Scaffold(
        // No local topBar needed
    ) { padding ->
        Box(modifier = modifier.padding(padding)) {
            // Always show the picker content as the background
            com.example.vitruvianredux.presentation.components.ExercisePickerContent(
                exercises = exercises,
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                showFavoritesOnly = showFavoritesOnly,
                onShowFavoritesOnlyChange = { 
                    showFavoritesOnly = it
                    if (it) {
                        searchQuery = ""
                        selectedMuscleFilter = "All"
                        selectedEquipmentFilter = "All"
                    }
                },
                selectedMuscleFilter = selectedMuscleFilter,
                onMuscleFilterChange = { selectedMuscleFilter = it },
                selectedEquipmentFilter = selectedEquipmentFilter,
                onEquipmentFilterChange = { selectedEquipmentFilter = it },
                onExerciseSelected = { selectedExercise ->
                    val exercise = Exercise(
                        name = selectedExercise.name,
                        muscleGroup = selectedExercise.muscleGroups.split(",").firstOrNull()?.trim() ?: "Full Body",
                        equipment = selectedExercise.equipment.split(",").firstOrNull()?.trim() ?: "",
                        defaultCableConfig = CableConfiguration.DOUBLE,
                        id = selectedExercise.id
                    )

                    val defaultCableConfig = exercise.resolveDefaultCableConfig()

                    // Cancel any in-progress loading to prevent race conditions
                    loadingJob?.cancel()

                    // Set loading state to prevent showing dialog before defaults are loaded
                    isLoadingDefaults = true

                    // Load saved defaults for this exercise+cable config asynchronously
                    loadingJob = coroutineScope.launch {
                        try {
                            val savedDefaults = selectedExercise.id?.let { exerciseId ->
                                getSingleExerciseDefaults(exerciseId, defaultCableConfig.name)
                            }

                            val newRoutineExercise = if (savedDefaults != null) {
                                // Apply saved defaults using helper methods
                                Timber.d("Loaded saved defaults for ${selectedExercise.name} (${savedDefaults.cableConfig})")

                                RoutineExercise(
                                    id = UUID.randomUUID().toString(),
                                    exercise = exercise,
                                    cableConfig = savedDefaults.getCableConfiguration(),
                                    orderIndex = 0,
                                    setReps = savedDefaults.setReps,
                                    weightPerCableKg = savedDefaults.weightPerCableKg,
                                    setWeightsPerCableKg = savedDefaults.setWeightsPerCableKg,
                                    progressionKg = savedDefaults.progressionKg,
                                    setRestSeconds = savedDefaults.setRestSeconds,
                                    workoutType = savedDefaults.toWorkoutType(),
                                    eccentricLoad = savedDefaults.getEccentricLoad(),
                                    echoLevel = savedDefaults.getEchoLevel(),
                                    duration = savedDefaults.duration,
                                    isAMRAP = savedDefaults.isAMRAP,
                                    perSetRestTime = savedDefaults.perSetRestTime
                                )
                            } else {
                                // No saved defaults - use system defaults with session eccentric load
                                RoutineExercise(
                                    id = UUID.randomUUID().toString(),
                                    exercise = exercise,
                                    cableConfig = defaultCableConfig,
                                    orderIndex = 0,
                                    setReps = listOf(10, 10, 10),
                                    weightPerCableKg = 20f,
                                    progressionKg = 0f,
                                    setRestSeconds = listOf(60, 60, 60),
                                    workoutType = WorkoutType.Program(ProgramMode.OldSchool),
                                    eccentricLoad = sessionEccentricLoad,
                                    echoLevel = EchoLevel.HARDER
                                )
                            }
                            exerciseToConfig = newRoutineExercise
                        } finally {
                            isLoadingDefaults = false
                        }
                    }
                },
                exerciseRepository = exerciseRepository,
                enableVideoPlayback = enableVideoPlayback,
                fullScreen = true // Use full screen layout (no local header)
            )

            // Show loading indicator while defaults are being loaded
            if (isLoadingDefaults) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            // Show bottom sheet as overlay when an exercise is selected and defaults are loaded
            if (!isLoadingDefaults) {
                exerciseToConfig?.let {
                    ExerciseEditBottomSheet(
                        exercise = it,
                        weightUnit = weightUnit,
                        enableVideoPlayback = enableVideoPlayback,
                        kgToDisplay = kgToDisplay,
                        displayToKg = displayToKg,
                        exerciseRepository = exerciseRepository,
                        personalRecordRepository = personalRecordRepository,
                        formatWeight = formatWeight,
                        buttonText = "Start Workout",
                        onSave = { configuredExercise ->
                            onStartWorkout(configuredExercise)
                            exerciseToConfig = null
                        },
                        onDismiss = {
                            exerciseToConfig = null
                        }
                    )
                }
            }
        }

    }
}

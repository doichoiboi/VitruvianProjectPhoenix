@file:Suppress("unused")  // Insight components - exported for reuse

package com.example.vitruvianredux.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.vitruvianredux.domain.model.PersonalRecord
import com.example.vitruvianredux.domain.model.WeightUnit
import com.example.vitruvianredux.domain.model.WorkoutSession
import com.example.vitruvianredux.presentation.components.charts.*
import timber.log.Timber

/**
 * Improved insights components utilizing the chart library
 */

private data class VolumeIntensityChartData(
    val volumeTrend: List<Pair<String, Float>>,
    val intensityTrend: List<Pair<String, Float>>,
    val latestVolume: Float,
    val latestIntensity: Float,
    val unitLabel: String
)

@Composable
fun MuscleBalanceRadarCard(
    personalRecords: List<PersonalRecord>,
    exerciseRepository: com.example.vitruvianredux.data.repository.ExerciseRepository,
    modifier: Modifier = Modifier
) {
    // Calculate muscle group frequency
    // Map of Muscle Group -> Frequency (0.0 - 1.0)
    var radarData by remember { mutableStateOf<List<Pair<String, Float>>>(emptyList()) }
    
    LaunchedEffect(personalRecords) {
        val counts = mutableMapOf<String, Int>()
        var total = 0
        
        personalRecords.forEach { pr ->
            try {
                val exercise = exerciseRepository.getExerciseById(pr.exerciseId)
                val groups = exercise?.muscleGroups?.split(",")?.map { it.trim() } ?: listOf("Other")
                
                groups.forEach { group ->
                    // Normalize group names
                    val normalizedGroup = when {
                        group.contains("Chest", ignoreCase = true) -> "Chest"
                        group.contains("Back", ignoreCase = true) -> "Back"
                        group.contains("Leg", ignoreCase = true) || group.contains("Quadriceps", ignoreCase = true) || group.contains("Hamstrings", ignoreCase = true) -> "Legs"
                        group.contains("Shoulder", ignoreCase = true) -> "Shoulders"
                        group.contains("Arm", ignoreCase = true) || group.contains("Bicep", ignoreCase = true) || group.contains("Tricep", ignoreCase = true) -> "Arms"
                        group.contains("Core", ignoreCase = true) || group.contains("Abs", ignoreCase = true) -> "Core"
                        else -> "Other"
                    }
                    
                    if (normalizedGroup != "Other") {
                        counts[normalizedGroup] = counts.getOrDefault(normalizedGroup, 0) + 1
                        total++
                    }
                }
            } catch (e: Exception) {
                Timber.w("Failed to get exercise for PR: ${e.message}")
            }
        }
        
        // Convert to relative frequency (0.0 - 1.0) relative to the max category
        // This makes the chart look full even if absolute counts are low
        val maxCount = counts.values.maxOrNull()?.toFloat() ?: 1f
        
        // Ensure all standard groups are represented
        val standardGroups = listOf("Chest", "Back", "Legs", "Shoulders", "Arms", "Core")
        
        radarData = standardGroups.map { group ->
            val count = counts[group] ?: 0
            group to (if (maxCount > 0) count / maxCount else 0f)
        }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Muscle Balance",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Relative training focus by body part",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            if (radarData.isNotEmpty() && radarData.any { it.second > 0 }) {
                RadarChart(
                    data = radarData,
                    maxValue = 1.0f,
                    modifier = Modifier.height(300.dp)
                )
            } else {
                Text(
                    "Complete workouts to see your muscle balance analysis.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 32.dp)
                )
            }
        }
    }
}

@Composable
fun ConsistencyGaugeCard(
    workoutSessions: List<WorkoutSession>,
    modifier: Modifier = Modifier
) {
    val stats = remember(workoutSessions) {
        val thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
        val count = workoutSessions.count { it.timestamp >= thirtyDaysAgo }
        count
    }
    
    // Dynamic target based on history, defaulting to 12 (3/week)
    val target = 12f 

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Monthly Consistency",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Workouts in the last 30 days",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            GaugeChart(
                currentValue = stats.toFloat(),
                targetValue = target,
                label = "Workouts",
                modifier = Modifier.height(250.dp)
            )
        }
    }
}

@Composable
fun VolumeVsIntensityCard(
    workoutSessions: List<WorkoutSession>,
    weightUnit: WeightUnit,
    modifier: Modifier = Modifier
) {
    // Prepare data for the last 7 sessions
    val chartData = remember(workoutSessions, weightUnit) {
        val sortedSessions = workoutSessions
            .filter { it.totalReps > 0 && it.weightPerCableKg > 0f }
            .sortedBy { it.timestamp }
            .takeLast(7)
        
        if (sortedSessions.isEmpty()) {
            VolumeIntensityChartData(
                volumeTrend = emptyList(),
                intensityTrend = emptyList(),
                latestVolume = 0f,
                latestIntensity = 0f,
                unitLabel = if (weightUnit == WeightUnit.KG) "kg" else "lb"
            )
        } else {
            val unitMultiplier = if (weightUnit == WeightUnit.LB) 2.20462f else 1f
            val unitLabel = if (weightUnit == WeightUnit.KG) "kg" else "lb"

            val rawVolume = sortedSessions.mapIndexed { index, session ->
                val label = "S${index + 1}"
                val volume = session.weightPerCableKg * 2 * session.totalReps * unitMultiplier
                label to volume
            }
            
            val rawIntensity = sortedSessions.mapIndexed { index, session ->
                val label = "S${index + 1}"
                val maxWeight = session.weightPerCableKg * 2 * unitMultiplier
                label to maxWeight
            }

            val maxVolume = rawVolume.maxOfOrNull { it.second }?.coerceAtLeast(1f) ?: 1f
            val maxIntensity = rawIntensity.maxOfOrNull { it.second }?.coerceAtLeast(1f) ?: 1f

            VolumeIntensityChartData(
                volumeTrend = rawVolume.map { (label, value) -> label to (value / maxVolume * 100f) },
                intensityTrend = rawIntensity.map { (label, value) -> label to (value / maxIntensity * 100f) },
                latestVolume = rawVolume.last().second,
                latestIntensity = rawIntensity.last().second,
                unitLabel = unitLabel
            )
        }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Volume vs Intensity",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Last 7 completed sessions",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "Relative trend: each metric is scaled to its own recent peak.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (chartData.volumeTrend.isNotEmpty()) {
                ComboChart(
                    columnData = chartData.volumeTrend,
                    lineData = chartData.intensityTrend,
                    columnLabel = "Volume trend",
                    lineLabel = "Intensity trend",
                    modifier = Modifier.height(300.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            "Latest volume",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "${"%.0f".format(chartData.latestVolume)} ${chartData.unitLabel}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Column {
                        Text(
                            "Latest intensity",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "${"%.1f".format(chartData.latestIntensity)} ${chartData.unitLabel}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                Text(
                    "No workout data available yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 32.dp)
                )
            }
        }
    }
}

@Composable
fun WorkoutModeDistributionCard(
    personalRecords: List<PersonalRecord>,
    modifier: Modifier = Modifier
) {
    val modeData = remember(personalRecords) {
        personalRecords
            .groupingBy { it.workoutMode }
            .eachCount()
            .map { it.key to it.value.toFloat() }
            .sortedByDescending { it.second }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Mode Distribution",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Based on Personal Records",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (modeData.isNotEmpty()) {
                // Using MuscleGroupCircleChart as a donut chart
                MuscleGroupCircleChart(
                    data = modeData,
                    modifier = Modifier.height(300.dp)
                )
            } else {
                Text(
                    "No mode data available.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 32.dp)
                )
            }
        }
    }
}

@Composable
fun TotalVolumeCard(
    workoutSessions: List<WorkoutSession>,
    weightUnit: WeightUnit,
    formatWeight: (Float, WeightUnit) -> String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Total Volume History",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Volume lifted per workout session",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (workoutSessions.isNotEmpty()) {
                VolumeTrendChart(
                    workoutSessions = workoutSessions,
                    weightUnit = weightUnit,
                    formatWeight = formatWeight,
                    modifier = Modifier.height(280.dp)
                )
            } else {
                Text(
                    "No workout data available yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 32.dp)
                )
            }
        }
    }
}

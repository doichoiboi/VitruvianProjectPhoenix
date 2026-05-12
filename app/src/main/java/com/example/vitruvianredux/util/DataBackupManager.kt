package com.example.vitruvianredux.util

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import androidx.room.withTransaction
import com.example.vitruvianredux.BuildConfig
import com.example.vitruvianredux.data.local.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import timber.log.Timber
import java.io.File
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Result of an import operation
 */
data class ImportResult(
    val sessionsImported: Int,
    val sessionsSkipped: Int,
    val metricsImported: Int,
    val routinesImported: Int,
    val routinesSkipped: Int,
    val routineExercisesImported: Int,
    val programsImported: Int,
    val programsSkipped: Int,
    val programDaysImported: Int,
    val personalRecordsImported: Int,
    val personalRecordsSkipped: Int
) {
    val totalImported: Int
        get() = sessionsImported + metricsImported + routinesImported +
                routineExercisesImported + programsImported + programDaysImported +
                personalRecordsImported

    val totalSkipped: Int
        get() = sessionsSkipped + routinesSkipped + programsSkipped + personalRecordsSkipped
}

/**
 * Manages export and import of all workout data for backup/restore and migration
 */
@Singleton
class DataBackupManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: WorkoutDatabase,
    private val workoutDao: WorkoutDao,
    private val personalRecordDao: PersonalRecordDao
) {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true  // Forward compatibility
        encodeDefaults = true
    }

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    private val fileDateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())

    /**
     * Export all data to a JSON backup
     */
    suspend fun exportAllData(): BackupData = withContext(Dispatchers.IO) {
        Timber.d("Starting full data export")

        val sessions = workoutDao.getAllSessionsSync()
        val metrics = workoutDao.getAllMetricsSync()
        val routines = workoutDao.getAllRoutinesSync()
        val routineExercises = workoutDao.getAllRoutineExercisesSync()
        val programs = workoutDao.getAllProgramsSync()
        val programDays = workoutDao.getAllProgramDaysSync()
        val personalRecords = personalRecordDao.getAllPRsSync()

        Timber.d("Export counts: sessions=${sessions.size}, metrics=${metrics.size}, " +
                "routines=${routines.size}, routineExercises=${routineExercises.size}, " +
                "programs=${programs.size}, programDays=${programDays.size}, " +
                "personalRecords=${personalRecords.size}")

        BackupData(
            version = 1,
            exportedAt = dateFormat.format(Date()),
            appVersion = BuildConfig.VERSION_NAME,
            data = BackupContent(
                workoutSessions = sessions.map { it.toBackup() },
                workoutMetrics = metrics.map { it.toBackup() },
                routines = routines.map { it.toBackup() },
                routineExercises = routineExercises.map { it.toBackup() },
                weeklyPrograms = programs.map { it.toBackup() },
                programDays = programDays.map { it.toBackup() },
                personalRecords = personalRecords.map { it.toBackup() }
            )
        )
    }

    /**
     * Save backup data to Downloads folder and return the URI
     */
    suspend fun saveToDownloads(backup: BackupData): Result<Uri> = withContext(Dispatchers.IO) {
        try {
            val jsonString = json.encodeToString(backup)
            val timestamp = fileDateFormat.format(Date())
            val fileName = "vitruvian_backup_$timestamp.json"

            val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10+ use MediaStore
                val contentValues = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                    put(MediaStore.Downloads.MIME_TYPE, "application/json")
                    put(MediaStore.Downloads.RELATIVE_PATH, "Download/VitruvianRedux")
                }

                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                    ?: throw Exception("Failed to create file in Downloads")

                resolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(jsonString.toByteArray())
                }

                uri
            } else {
                // Android 9 and below - direct file access
                @Suppress("DEPRECATION")
                val downloadsDir = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    "VitruvianRedux"
                )
                downloadsDir.mkdirs()

                val file = File(downloadsDir, fileName)
                file.writeText(jsonString)

                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
            }

            Timber.d("Backup saved to: $uri")
            Result.success(uri)
        } catch (e: Exception) {
            Timber.e(e, "Failed to save backup")
            Result.failure(e)
        }
    }

    /**
     * Save backup to cache for sharing
     */
    suspend fun saveToCache(backup: BackupData): Result<Uri> = withContext(Dispatchers.IO) {
        try {
            val jsonString = json.encodeToString(backup)
            val timestamp = fileDateFormat.format(Date())
            val fileName = "vitruvian_backup_$timestamp.json"

            val file = File(context.cacheDir, fileName)
            file.writeText(jsonString)

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            Timber.d("Backup saved to cache: $uri")
            Result.success(uri)
        } catch (e: Exception) {
            Timber.e(e, "Failed to save backup to cache")
            Result.failure(e)
        }
    }

    /**
     * Import data from a backup file URI
     * Uses "skip duplicates" strategy - existing records are not overwritten
     */
    suspend fun importFromUri(uri: Uri): Result<ImportResult> = withContext(Dispatchers.IO) {
        try {
            val inputStream: InputStream = context.contentResolver.openInputStream(uri)
                ?: throw Exception("Cannot open file")

            val jsonString = inputStream.bufferedReader().use { it.readText() }
            val backup = json.decodeFromString<BackupData>(jsonString)

            Timber.d("Importing backup version ${backup.version} from ${backup.appVersion}")

            if (backup.version > 1) {
                Timber.w("Backup version ${backup.version} is newer than supported (1)")
            }

            val result = importBackupData(backup)

            Timber.d("Import complete: $result")
            Result.success(result)
        } catch (e: Exception) {
            Timber.e(e, "Failed to import backup")
            Result.failure(e)
        }
    }

    internal suspend fun importBackupData(backup: BackupData): ImportResult = database.withTransaction {
        val existingSessionIds = workoutDao.getAllSessionIds().toSet()
        val existingRoutineIds = workoutDao.getAllRoutineIds().toSet()
        val existingProgramIds = workoutDao.getAllProgramIds().toSet()

        var sessionsImported = 0
        var sessionsSkipped = 0
        backup.data.workoutSessions.forEach { session ->
            val insertedId = workoutDao.insertSessionIgnore(session.toEntity())
            if (insertedId == -1L) {
                sessionsSkipped++
            } else {
                sessionsImported++
            }
        }

        var metricsImported = 0
        val restorableSessionIds = existingSessionIds + backup.data.workoutSessions.map { it.id }
        backup.data.workoutMetrics.forEach { metric ->
            if (metric.sessionId in restorableSessionIds) {
                val insertedId = workoutDao.insertMetricIgnore(metric.toEntity())
                if (insertedId != -1L) {
                    metricsImported++
                }
            }
        }

        var routinesImported = 0
        var routinesSkipped = 0
        backup.data.routines.forEach { routine ->
            val insertedId = workoutDao.insertRoutineIgnore(routine.toEntity())
            if (insertedId == -1L) {
                routinesSkipped++
            } else {
                routinesImported++
            }
        }

        var routineExercisesImported = 0
        val restorableRoutineIds = existingRoutineIds + backup.data.routines.map { it.id }
        backup.data.routineExercises.forEach { exercise ->
            if (exercise.routineId in restorableRoutineIds) {
                val insertedId = workoutDao.insertRoutineExerciseIgnore(exercise.toEntity())
                if (insertedId != -1L) {
                    routineExercisesImported++
                }
            }
        }

        var programsImported = 0
        var programsSkipped = 0
        backup.data.weeklyPrograms.forEach { program ->
            val insertedId = workoutDao.insertProgramIgnore(program.toEntity())
            if (insertedId == -1L) {
                programsSkipped++
            } else {
                programsImported++
            }
        }

        var programDaysImported = 0
        val restorableProgramIds = existingProgramIds + backup.data.weeklyPrograms.map { it.id }
        backup.data.programDays.forEach { day ->
            if (day.programId in restorableProgramIds) {
                val insertedId = workoutDao.insertProgramDayIgnore(day.toEntity())
                if (insertedId != -1L) {
                    programDaysImported++
                }
            }
        }

        var personalRecordsImported = 0
        var personalRecordsSkipped = 0
        backup.data.personalRecords.forEach { pr ->
            val insertedId = personalRecordDao.insertPRIgnore(pr.toEntity())
            if (insertedId == -1L) {
                personalRecordsSkipped++
            } else {
                personalRecordsImported++
            }
        }

        ImportResult(
            sessionsImported = sessionsImported,
            sessionsSkipped = sessionsSkipped,
            metricsImported = metricsImported,
            routinesImported = routinesImported,
            routinesSkipped = routinesSkipped,
            routineExercisesImported = routineExercisesImported,
            programsImported = programsImported,
            programsSkipped = programsSkipped,
            programDaysImported = programDaysImported,
            personalRecordsImported = personalRecordsImported,
            personalRecordsSkipped = personalRecordsSkipped
        )
    }
}

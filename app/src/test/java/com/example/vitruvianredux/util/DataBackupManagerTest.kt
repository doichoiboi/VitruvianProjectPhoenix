package com.example.vitruvianredux.util

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.vitruvianredux.data.local.BackupContent
import com.example.vitruvianredux.data.local.BackupData
import com.example.vitruvianredux.data.local.ProgramDayBackup
import com.example.vitruvianredux.data.local.WeeklyProgramBackup
import com.example.vitruvianredux.data.local.WorkoutDatabase
import com.example.vitruvianredux.data.local.WorkoutMetricBackup
import com.example.vitruvianredux.data.local.WorkoutSessionBackup
import com.example.vitruvianredux.data.local.toEntity
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class DataBackupManagerTest {

    private lateinit var database: WorkoutDatabase
    private lateinit var manager: DataBackupManager

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, WorkoutDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        manager = DataBackupManager(
            context = context,
            database = database,
            workoutDao = database.workoutDao(),
            personalRecordDao = database.personalRecordDao()
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `import rolls back all writes when a child insert fails`() = runTest {
        val backup = BackupData(
            exportedAt = "2026-05-12T00:00:00Z",
            appVersion = "test",
            data = BackupContent(
                workoutSessions = listOf(sessionBackup(id = "session-1")),
                weeklyPrograms = listOf(
                    WeeklyProgramBackup(
                        id = "program-1",
                        title = "Program",
                        createdAt = 1L
                    )
                ),
                programDays = listOf(
                    ProgramDayBackup(
                        programId = "program-1",
                        routineId = "missing-routine",
                        dayOfWeek = 1
                    )
                )
            )
        )

        val result = runCatching { manager.importBackupData(backup) }

        assertThat(result.isFailure).isTrue()
        assertThat(database.workoutDao().getAllSessionsSync()).isEmpty()
        assertThat(database.workoutDao().getAllProgramsSync()).isEmpty()
    }

    @Test
    fun `import restores missing metrics for an existing session`() = runTest {
        val session = sessionBackup(id = "session-1")
        database.workoutDao().insertSessionIgnore(session.toEntity())

        val backup = BackupData(
            exportedAt = "2026-05-12T00:00:00Z",
            appVersion = "test",
            data = BackupContent(
                workoutSessions = listOf(session),
                workoutMetrics = listOf(
                    WorkoutMetricBackup(
                        id = 1L,
                        sessionId = "session-1",
                        timestamp = 2L,
                        loadA = 10f,
                        loadB = 11f,
                        positionA = 12f,
                        positionB = 13f,
                        ticks = 14
                    )
                )
            )
        )

        val result = manager.importBackupData(backup)

        assertThat(result.sessionsImported).isEqualTo(0)
        assertThat(result.sessionsSkipped).isEqualTo(1)
        assertThat(result.metricsImported).isEqualTo(1)
        assertThat(database.workoutDao().getMetricsForSessionSync("session-1")).hasSize(1)
    }

    private fun sessionBackup(id: String): WorkoutSessionBackup = WorkoutSessionBackup(
        id = id,
        timestamp = 1L,
        mode = "Old School",
        reps = 10,
        weightPerCableKg = 20f,
        progressionKg = 0f,
        duration = 30L,
        totalReps = 10,
        warmupReps = 3,
        workingReps = 7,
        isJustLift = false,
        stopAtTop = false
    )
}

package com.example.vitruvianredux.di

import android.content.Context
import androidx.room.Room
import com.example.vitruvianredux.data.local.ConnectionLogDao
import com.example.vitruvianredux.data.local.ExerciseDao
import com.example.vitruvianredux.data.local.PersonalRecordDao
import com.example.vitruvianredux.data.local.WorkoutDao
import com.example.vitruvianredux.data.local.WorkoutDatabase
import com.example.vitruvianredux.data.local.dao.DiagnosticsDao
import com.example.vitruvianredux.data.local.dao.PhaseStatisticsDao
import com.example.vitruvianredux.data.local.migration.DatabaseMigrations
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideWorkoutDatabase(
        @ApplicationContext context: Context
    ): WorkoutDatabase {
        return Room.databaseBuilder(
            context,
            WorkoutDatabase::class.java,
            "vitruvian_workout_db"
        )
            .addMigrations(*DatabaseMigrations.ALL)
            .build()
    }

    @Provides
    @Singleton
    fun provideWorkoutDao(database: WorkoutDatabase): WorkoutDao {
        return database.workoutDao()
    }

    @Provides
    @Singleton
    fun provideExerciseDao(database: WorkoutDatabase): ExerciseDao {
        return database.exerciseDao()
    }

    @Provides
    @Singleton
    fun providePersonalRecordDao(database: WorkoutDatabase): PersonalRecordDao {
        return database.personalRecordDao()
    }

    @Provides
    @Singleton
    fun providePhaseStatisticsDao(database: WorkoutDatabase): PhaseStatisticsDao {
        return database.phaseStatisticsDao()
    }

    @Provides
    @Singleton
    fun provideDiagnosticsDao(database: WorkoutDatabase): DiagnosticsDao {
        return database.diagnosticsDao()
    }

    @Provides
    @Singleton
    fun provideConnectionLogDao(database: WorkoutDatabase): ConnectionLogDao {
        return database.connectionLogDao()
    }

}

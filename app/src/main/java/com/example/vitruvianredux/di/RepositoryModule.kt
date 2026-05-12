package com.example.vitruvianredux.di

import com.example.vitruvianredux.data.local.ExerciseDao
import com.example.vitruvianredux.data.local.ExerciseImporter
import com.example.vitruvianredux.data.local.PersonalRecordDao
import com.example.vitruvianredux.data.local.WorkoutDao
import com.example.vitruvianredux.data.local.dao.DiagnosticsDao
import com.example.vitruvianredux.data.local.dao.PhaseStatisticsDao
import com.example.vitruvianredux.data.repository.ExerciseRepository
import com.example.vitruvianredux.data.repository.ExerciseRepositoryImpl
import com.example.vitruvianredux.data.repository.PersonalRecordRepository
import com.example.vitruvianredux.data.repository.WorkoutRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Provides
    @Singleton
    fun provideWorkoutRepository(
        workoutDao: WorkoutDao,
        personalRecordDao: PersonalRecordDao,
        phaseStatisticsDao: PhaseStatisticsDao,
        diagnosticsDao: DiagnosticsDao
    ): WorkoutRepository {
        return WorkoutRepository(workoutDao, personalRecordDao, phaseStatisticsDao, diagnosticsDao)
    }

    @Provides
    @Singleton
    fun provideExerciseRepository(
        exerciseDao: ExerciseDao,
        exerciseImporter: ExerciseImporter
    ): ExerciseRepository {
        return ExerciseRepositoryImpl(exerciseDao, exerciseImporter)
    }

    @Provides
    @Singleton
    fun providePersonalRecordRepository(personalRecordDao: PersonalRecordDao): PersonalRecordRepository {
        return PersonalRecordRepository(personalRecordDao)
    }
}

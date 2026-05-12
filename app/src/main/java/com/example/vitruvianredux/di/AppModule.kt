package com.example.vitruvianredux.di

import android.content.Context
import com.example.vitruvianredux.data.local.WorkoutDao
import com.example.vitruvianredux.data.local.ExerciseDao
import com.example.vitruvianredux.data.local.ExerciseImporter
import com.example.vitruvianredux.data.local.PersonalRecordDao
import com.example.vitruvianredux.data.local.ConnectionLogDao
import com.example.vitruvianredux.data.local.dao.PhaseStatisticsDao
import com.example.vitruvianredux.data.local.dao.DiagnosticsDao
import com.example.vitruvianredux.data.ble.VitruvianBleManager
import com.example.vitruvianredux.data.logger.ConnectionLogger
import com.example.vitruvianredux.data.preferences.PreferencesManager
import com.example.vitruvianredux.data.repository.BleRepository
import com.example.vitruvianredux.data.repository.BleRepositoryImpl
import com.example.vitruvianredux.data.repository.WorkoutRepository
import com.example.vitruvianredux.data.repository.ExerciseRepository
import com.example.vitruvianredux.data.repository.ExerciseRepositoryImpl
import com.example.vitruvianredux.data.repository.PersonalRecordRepository
import com.example.vitruvianredux.domain.usecase.RepCounterFromMachine
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideConnectionLogger(connectionLogDao: ConnectionLogDao): ConnectionLogger {
        return ConnectionLogger(connectionLogDao)
    }

    @Provides
    @Singleton
    fun provideBleRepository(
        impl: BleRepositoryImpl // Hilt will provide BleRepositoryImpl
    ): BleRepository = impl
    
    @Provides
    @Singleton
    fun provideVitruvianBleManager(
        @ApplicationContext context: Context,
        connectionLogger: ConnectionLogger
    ): VitruvianBleManager {
        return VitruvianBleManager(context, connectionLogger)
    }

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
    fun provideRepCounterFromMachine(): RepCounterFromMachine {
        return RepCounterFromMachine()
    }

    @Provides
    @Singleton
    fun providePreferencesManager(
        @ApplicationContext context: Context
    ): PreferencesManager {
        return PreferencesManager(context)
    }
    
    @Provides
    @Singleton
    fun provideExerciseImporter(
        @ApplicationContext context: Context,
        exerciseDao: ExerciseDao
    ): ExerciseImporter {
        return ExerciseImporter(context, exerciseDao)
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

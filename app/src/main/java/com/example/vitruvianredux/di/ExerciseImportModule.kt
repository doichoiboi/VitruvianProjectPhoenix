package com.example.vitruvianredux.di

import android.content.Context
import com.example.vitruvianredux.data.local.ExerciseDao
import com.example.vitruvianredux.data.local.ExerciseImporter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ExerciseImportModule {
    @Provides
    @Singleton
    fun provideExerciseImporter(
        @ApplicationContext context: Context,
        exerciseDao: ExerciseDao
    ): ExerciseImporter {
        return ExerciseImporter(context, exerciseDao)
    }
}

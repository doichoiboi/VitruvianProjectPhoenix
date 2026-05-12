package com.example.vitruvianredux.di

import android.content.Context
import com.example.vitruvianredux.data.ble.VitruvianBleManager
import com.example.vitruvianredux.data.local.ConnectionLogDao
import com.example.vitruvianredux.data.logger.ConnectionLogger
import com.example.vitruvianredux.data.repository.BleRepository
import com.example.vitruvianredux.data.repository.BleRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BleModule {
    @Provides
    @Singleton
    fun provideConnectionLogger(connectionLogDao: ConnectionLogDao): ConnectionLogger {
        return ConnectionLogger(connectionLogDao)
    }

    @Provides
    @Singleton
    fun provideBleRepository(
        impl: BleRepositoryImpl
    ): BleRepository = impl

    @Provides
    @Singleton
    fun provideVitruvianBleManager(
        @ApplicationContext context: Context,
        connectionLogger: ConnectionLogger
    ): VitruvianBleManager {
        return VitruvianBleManager(context, connectionLogger)
    }
}

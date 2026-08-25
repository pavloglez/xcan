package com.pavloglez.xcan.core.data.di

import com.pavloglez.xcan.core.data.repository.SensorRepositoryImpl
import com.pavloglez.xcan.core.model.SensorRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindSensorRepository(
        impl: SensorRepositoryImpl
    ): SensorRepository
}

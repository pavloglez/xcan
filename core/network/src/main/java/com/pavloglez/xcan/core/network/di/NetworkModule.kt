package com.pavloglez.xcan.core.network.di

import com.pavloglez.xcan.core.network.XCanApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideXCanApiService(): XCanApiService {
        return Retrofit.Builder()
            .baseUrl("https://mock.xcantelemetry.com/api/v1/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(XCanApiService::class.java)
    }
}

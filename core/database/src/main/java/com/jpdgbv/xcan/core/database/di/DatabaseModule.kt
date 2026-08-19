package com.jpdgbv.xcan.core.database.di

import android.content.Context
import androidx.room.Room
import com.jpdgbv.xcan.core.database.XCanDatabase
import com.jpdgbv.xcan.core.database.dao.CarProfileDao
import com.jpdgbv.xcan.core.database.dao.MaintenanceDao
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
    fun provideXCanDatabase(
        @ApplicationContext context: Context
    ): XCanDatabase {
        return Room.databaseBuilder(
            context,
            XCanDatabase::class.java,
            "xcan-database"
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    fun provideMaintenanceDao(
        database: XCanDatabase
    ): MaintenanceDao {
        return database.maintenanceDao()
    }

    @Provides
    fun provideCarProfileDao(
        database: XCanDatabase
    ): CarProfileDao {
        return database.carProfileDao()
    }
}

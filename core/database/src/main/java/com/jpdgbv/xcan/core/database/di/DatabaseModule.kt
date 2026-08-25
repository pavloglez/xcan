package com.jpdgbv.xcan.core.database.di

import android.content.Context
import android.util.Base64
import androidx.room.Room
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.jpdgbv.xcan.core.database.XCanDatabase
import com.jpdgbv.xcan.core.database.dao.CarProfileDao
import com.jpdgbv.xcan.core.database.dao.LogEntryDao
import com.jpdgbv.xcan.core.database.dao.LogSessionDao
import com.jpdgbv.xcan.core.database.dao.MaintenanceDao
import com.jpdgbv.xcan.core.database.dao.TelemetryDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.sqlcipher.database.SupportFactory
import java.security.SecureRandom
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    private fun getDbPassword(context: Context): ByteArray {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
            
        val sharedPrefs = EncryptedSharedPreferences.create(
            context,
            "xcan_db_secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        var keyString = sharedPrefs.getString("db_key", null)
        if (keyString == null) {
            val randomBytes = ByteArray(32)
            SecureRandom().nextBytes(randomBytes)
            keyString = Base64.encodeToString(randomBytes, Base64.NO_WRAP)
            sharedPrefs.edit().putString("db_key", keyString).apply()
        }

        return Base64.decode(keyString, Base64.NO_WRAP)
    }

    @Provides
    @Singleton
    fun provideXCanDatabase(
        @ApplicationContext context: Context
    ): XCanDatabase {
        val factory = SupportFactory(getDbPassword(context))
        return Room.databaseBuilder(
            context,
            XCanDatabase::class.java,
            "xcan-database"
        )
        .openHelperFactory(factory)
        .fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    fun provideMaintenanceDao(database: XCanDatabase): MaintenanceDao = database.maintenanceDao()

    @Provides
    fun provideCarProfileDao(database: XCanDatabase): CarProfileDao = database.carProfileDao()

    @Provides
    fun provideLogSessionDao(database: XCanDatabase): LogSessionDao = database.logSessionDao()

    @Provides
    fun provideLogEntryDao(database: XCanDatabase): LogEntryDao = database.logEntryDao()
    
    @Provides
    fun provideTelemetryDao(database: XCanDatabase): TelemetryDao = database.telemetryDao()
}

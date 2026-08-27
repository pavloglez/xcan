package com.pavloglez.xcan.core.database.di

import android.content.Context
import android.util.Base64
import androidx.room.Room
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.pavloglez.xcan.core.database.XCanDatabase
import com.pavloglez.xcan.core.database.dao.CarProfileDao
import com.pavloglez.xcan.core.database.dao.LogEntryDao
import com.pavloglez.xcan.core.database.dao.LogSessionDao
import com.pavloglez.xcan.core.database.dao.MaintenanceDao
import com.pavloglez.xcan.core.database.dao.TelemetryDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

import java.security.SecureRandom
import javax.inject.Singleton

private const val PREFS_DB_SECURITY = "xcan_db_secure_prefs"
private const val PREF_KEY_DB_PASSPHRASE = "db_key"
private const val DB_PASSPHRASE_BYTE_COUNT = 32
private const val DATABASE_NAME = "xcan-database"

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    private fun getDbPassword(context: Context): ByteArray {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
            
        val sharedPrefs = EncryptedSharedPreferences.create(
            context,
            PREFS_DB_SECURITY,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        var keyString = sharedPrefs.getString(PREF_KEY_DB_PASSPHRASE, null)
        if (keyString == null) {
            val randomBytes = ByteArray(DB_PASSPHRASE_BYTE_COUNT)
            SecureRandom().nextBytes(randomBytes)
            keyString = Base64.encodeToString(randomBytes, Base64.NO_WRAP)
            sharedPrefs.edit().putString(PREF_KEY_DB_PASSPHRASE, keyString).apply()
        }

        return Base64.decode(keyString, Base64.NO_WRAP)
    }

    @Provides
    @Singleton
    fun provideXCanDatabase(
        @ApplicationContext context: Context
    ): XCanDatabase {
        val factory = net.zetetic.database.sqlcipher.SupportOpenHelperFactory(getDbPassword(context))
        return Room.databaseBuilder(
            context,
            XCanDatabase::class.java,
            DATABASE_NAME
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

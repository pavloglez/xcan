package com.pavloglez.xcan.core.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import com.pavloglez.xcan.core.model.ObdConstants

@Singleton
class UserPreferencesRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {

    private object PreferencesKeys {
        val USE_METRIC = booleanPreferencesKey("use_metric")
        val SELECTED_SENSORS = stringSetPreferencesKey("selected_sensors")
    }

    val useMetric: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            // Default to metric (true) or false? In ConfigViewModel the default was false. Let's use false.
            preferences[PreferencesKeys.USE_METRIC] ?: false
        }

    suspend fun setUseMetric(useMetric: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.USE_METRIC] = useMetric
        }
    }

    fun getSelectedSensors(carId: String?): Flow<Set<String>> {
        val key = if (carId == null) PreferencesKeys.SELECTED_SENSORS else stringSetPreferencesKey("selected_sensors_$carId")
        return dataStore.data
            .catch { exception ->
                if (exception is IOException) emit(emptyPreferences()) else throw exception
            }
            .map { preferences ->
                preferences[key] ?: ObdConstants.DEFAULT_SELECTED_SENSORS
            }
    }

    suspend fun setSelectedSensors(carId: String?, sensors: Set<String>) {
        val key = if (carId == null) PreferencesKeys.SELECTED_SENSORS else stringSetPreferencesKey("selected_sensors_$carId")
        dataStore.edit { preferences ->
            preferences[key] = sensors
        }
    }
}

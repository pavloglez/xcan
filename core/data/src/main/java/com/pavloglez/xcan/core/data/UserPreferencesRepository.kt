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

    val selectedSensors: Flow<Set<String>> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            preferences[PreferencesKeys.SELECTED_SENSORS] ?: setOf("010C", "010D", "0104", "0105")
        }

    suspend fun setSelectedSensors(sensors: Set<String>) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.SELECTED_SENSORS] = sensors
        }
    }
}

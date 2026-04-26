package com.alvarocervantes.fittrackplus.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.alvarocervantes.fittrackplus.core.design.AppThemeMode
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.userPreferencesDataStore by preferencesDataStore(
    name = "fittrackplus_user_preferences"
)

@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    val activeRoutineId: Flow<Long?> = context.userPreferencesDataStore.data.map { preferences ->
        preferences[Keys.ACTIVE_ROUTINE_ID]
    }

    val weightUnit: Flow<String> = context.userPreferencesDataStore.data.map { preferences ->
        preferences[Keys.WEIGHT_UNIT] ?: "kg"
    }

    val themeMode: Flow<AppThemeMode> = context.userPreferencesDataStore.data.map { preferences ->
        AppThemeMode.fromStorageValue(preferences[Keys.THEME_MODE])
    }

    val hasSeenSnapshotInfo: Flow<Boolean> = context.userPreferencesDataStore.data.map { preferences ->
        preferences[Keys.HAS_SEEN_SNAPSHOT_INFO] ?: false
    }

    suspend fun setActiveRoutineId(routineId: Long?) {
        context.userPreferencesDataStore.edit { preferences ->
            if (routineId == null) {
                preferences.remove(Keys.ACTIVE_ROUTINE_ID)
            } else {
                preferences[Keys.ACTIVE_ROUTINE_ID] = routineId
            }
        }
    }

    suspend fun setWeightUnit(unit: String) {
        context.userPreferencesDataStore.edit { preferences ->
            preferences[Keys.WEIGHT_UNIT] = unit
        }
    }

    suspend fun setThemeMode(mode: AppThemeMode) {
        context.userPreferencesDataStore.edit { preferences ->
            preferences[Keys.THEME_MODE] = mode.storageValue
        }
    }

    suspend fun dismissSnapshotInfo() {
        context.userPreferencesDataStore.edit { preferences ->
            preferences[Keys.HAS_SEEN_SNAPSHOT_INFO] = true
        }
    }

    private object Keys {
        val ACTIVE_ROUTINE_ID: Preferences.Key<Long> = longPreferencesKey("active_routine_id")
        val WEIGHT_UNIT: Preferences.Key<String> = stringPreferencesKey("weight_unit")
        val THEME_MODE: Preferences.Key<String> = stringPreferencesKey("theme_mode")
        val HAS_SEEN_SNAPSHOT_INFO: Preferences.Key<Boolean> = booleanPreferencesKey("has_seen_snapshot_info")
    }
}

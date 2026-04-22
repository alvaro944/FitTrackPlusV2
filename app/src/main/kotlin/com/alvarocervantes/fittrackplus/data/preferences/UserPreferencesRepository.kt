package com.alvarocervantes.fittrackplus.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
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

    private object Keys {
        val ACTIVE_ROUTINE_ID: Preferences.Key<Long> = longPreferencesKey("active_routine_id")
        val WEIGHT_UNIT: Preferences.Key<String> = stringPreferencesKey("weight_unit")
    }
}

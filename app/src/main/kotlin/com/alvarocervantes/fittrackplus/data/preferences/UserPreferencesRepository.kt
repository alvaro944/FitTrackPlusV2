package com.alvarocervantes.fittrackplus.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
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

data class RestTimerPreferences(
    val durationSeconds: Int = 0,
    val remainingSeconds: Int = 0,
    val status: String = "Stopped",
    val endsAtMillis: Long? = null,
    val autoStartEnabled: Boolean = false
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

    val hasSeenOnboarding: Flow<Boolean> = context.userPreferencesDataStore.data.map { preferences ->
        preferences[Keys.HAS_SEEN_ONBOARDING] ?: false
    }

    val dailyStepGoal: Flow<Int> = context.userPreferencesDataStore.data.map { preferences ->
        preferences[Keys.DAILY_STEP_GOAL] ?: 10_000
    }

    val healthConnectConnected: Flow<Boolean> = context.userPreferencesDataStore.data.map { preferences ->
        preferences[Keys.HEALTH_CONNECT_CONNECTED] ?: false
    }

    val restTimerPreferences: Flow<RestTimerPreferences> = context.userPreferencesDataStore.data.map { preferences ->
        RestTimerPreferences(
            durationSeconds = preferences[Keys.REST_TIMER_DURATION_SECONDS] ?: 0,
            remainingSeconds = preferences[Keys.REST_TIMER_REMAINING_SECONDS] ?: 0,
            status = preferences[Keys.REST_TIMER_STATUS] ?: "Stopped",
            endsAtMillis = preferences[Keys.REST_TIMER_ENDS_AT_MILLIS],
            autoStartEnabled = preferences[Keys.REST_TIMER_AUTO_START_ENABLED] ?: false
        )
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

    suspend fun setHasSeenOnboarding(seen: Boolean) {
        context.userPreferencesDataStore.edit { preferences ->
            preferences[Keys.HAS_SEEN_ONBOARDING] = seen
        }
    }

    suspend fun setDailyStepGoal(goal: Int) {
        context.userPreferencesDataStore.edit { preferences ->
            preferences[Keys.DAILY_STEP_GOAL] = goal
        }
    }

    suspend fun setHealthConnectConnected(connected: Boolean) {
        context.userPreferencesDataStore.edit { preferences ->
            preferences[Keys.HEALTH_CONNECT_CONNECTED] = connected
        }
    }

    suspend fun setRestTimerPreferences(timer: RestTimerPreferences) {
        context.userPreferencesDataStore.edit { preferences ->
            preferences[Keys.REST_TIMER_DURATION_SECONDS] = timer.durationSeconds
            preferences[Keys.REST_TIMER_REMAINING_SECONDS] = timer.remainingSeconds
            preferences[Keys.REST_TIMER_STATUS] = timer.status
            preferences[Keys.REST_TIMER_AUTO_START_ENABLED] = timer.autoStartEnabled
            if (timer.endsAtMillis == null) {
                preferences.remove(Keys.REST_TIMER_ENDS_AT_MILLIS)
            } else {
                preferences[Keys.REST_TIMER_ENDS_AT_MILLIS] = timer.endsAtMillis
            }
        }
    }

    private object Keys {
        val ACTIVE_ROUTINE_ID: Preferences.Key<Long> = longPreferencesKey("active_routine_id")
        val WEIGHT_UNIT: Preferences.Key<String> = stringPreferencesKey("weight_unit")
        val THEME_MODE: Preferences.Key<String> = stringPreferencesKey("theme_mode")
        val HAS_SEEN_SNAPSHOT_INFO: Preferences.Key<Boolean> = booleanPreferencesKey("has_seen_snapshot_info")
        val HAS_SEEN_ONBOARDING: Preferences.Key<Boolean> = booleanPreferencesKey("has_seen_onboarding")
        val DAILY_STEP_GOAL: Preferences.Key<Int> = intPreferencesKey("daily_step_goal")
        val HEALTH_CONNECT_CONNECTED: Preferences.Key<Boolean> = booleanPreferencesKey("health_connect_connected")
        val REST_TIMER_DURATION_SECONDS: Preferences.Key<Int> = intPreferencesKey("rest_timer_duration_seconds")
        val REST_TIMER_REMAINING_SECONDS: Preferences.Key<Int> = intPreferencesKey("rest_timer_remaining_seconds")
        val REST_TIMER_STATUS: Preferences.Key<String> = stringPreferencesKey("rest_timer_status")
        val REST_TIMER_ENDS_AT_MILLIS: Preferences.Key<Long> = longPreferencesKey("rest_timer_ends_at_millis")
        val REST_TIMER_AUTO_START_ENABLED: Preferences.Key<Boolean> = booleanPreferencesKey("rest_timer_auto_start_enabled")
    }
}

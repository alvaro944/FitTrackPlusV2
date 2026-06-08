package com.alvarocervantes.fittrackplus.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alvarocervantes.fittrackplus.BuildConfig
import com.alvarocervantes.fittrackplus.core.design.AppThemeMode
import com.alvarocervantes.fittrackplus.data.local.seed.DebugDemoDataSeeder
import com.alvarocervantes.fittrackplus.data.preferences.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val debugDemoDataSeeder: DebugDemoDataSeeder
) : ViewModel() {

    val isDebugBuild: Boolean = BuildConfig.DEBUG

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    val weightUnit: StateFlow<String> = userPreferencesRepository.weightUnit
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "kg")

    val themeMode: StateFlow<AppThemeMode> = userPreferencesRepository.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppThemeMode.System)

    fun setWeightUnit(unit: String) {
        if (unit == weightUnit.value) return
        viewModelScope.launch {
            userPreferencesRepository.setWeightUnit(unit)
            _message.value = "Unidad cambiada a $unit."
        }
    }

    fun setThemeMode(mode: AppThemeMode) {
        val message = themeModeChangeMessage(
            current = themeMode.value,
            requested = mode
        ) ?: return
        viewModelScope.launch {
            userPreferencesRepository.setThemeMode(mode)
            _message.value = message
        }
    }

    fun clearMessage() {
        _message.value = null
    }

    fun reloadDemoData() {
        viewModelScope.launch {
            _message.value = null
            runCatching { debugDemoDataSeeder.reseed() }
                .onSuccess { _message.value = "Datos demo recargados." }
                .onFailure { _message.value = "Error al recargar datos demo." }
        }
    }
}

fun themeModeChangeMessage(current: AppThemeMode, requested: AppThemeMode): String? {
    return if (current == requested) {
        null
    } else {
        "Tema cambiado a ${requested.label}."
    }
}

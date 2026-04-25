package com.alvarocervantes.fittrackplus.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    val weightUnit: StateFlow<String> = userPreferencesRepository.weightUnit
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "kg")

    fun setWeightUnit(unit: String) {
        if (unit == weightUnit.value) return
        viewModelScope.launch {
            userPreferencesRepository.setWeightUnit(unit)
            _message.value = "Unidad cambiada a $unit."
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}

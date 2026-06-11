package com.alvarocervantes.fittrackplus.core.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alvarocervantes.fittrackplus.core.design.AppThemeMode
import com.alvarocervantes.fittrackplus.data.preferences.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class AppShellViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _blockedRoute = MutableStateFlow<AppRoute?>(null)
    private val _pendingNavigation = MutableStateFlow<NavigationRequest?>(null)
    private val _message = MutableStateFlow<String?>(null)
    private val _approvedNavigation = MutableSharedFlow<NavigationRequest>(extraBufferCapacity = 1)

    val pendingNavigation: StateFlow<NavigationRequest?> = _pendingNavigation.asStateFlow()
    val message: StateFlow<String?> = _message.asStateFlow()
    val approvedNavigation: SharedFlow<NavigationRequest> = _approvedNavigation.asSharedFlow()

    val weightUnit: StateFlow<String> = userPreferencesRepository.weightUnit
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "kg")

    val themeMode: StateFlow<AppThemeMode> = userPreferencesRepository.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppThemeMode.System)

    fun setWeightUnit(unit: String) {
        if (unit == weightUnit.value) return
        viewModelScope.launch {
            userPreferencesRepository.setWeightUnit(unit)
            _message.value = "Unidad cambiada a $unit."
        }
    }

    fun setThemeMode(mode: AppThemeMode) {
        if (mode == themeMode.value) return
        viewModelScope.launch {
            userPreferencesRepository.setThemeMode(mode)
            _message.value = "Tema cambiado a ${mode.label}."
        }
    }

    fun showFutureActionMessage(title: String) {
        _message.value = "$title disponible en una fase futura."
    }

    fun setNavigationBlocker(route: AppRoute, isBlocked: Boolean) {
        _blockedRoute.value = when {
            isBlocked -> route
            _blockedRoute.value == route -> null
            else -> _blockedRoute.value
        }
        if (!isBlocked && _blockedRoute.value != route) {
            dismissPendingNavigation()
        }
    }

    fun requestNavigation(
        currentRoute: AppRoute?,
        targetRoute: AppRoute,
        kind: NavigationRequestKind
    ): Boolean {
        if (currentRoute == null || currentRoute == targetRoute) return false
        if (_blockedRoute.value != currentRoute) return false
        _pendingNavigation.value = NavigationRequest(route = targetRoute, kind = kind)
        return true
    }

    fun confirmPendingNavigation() {
        val navigation = _pendingNavigation.value ?: return
        _pendingNavigation.value = null
        _approvedNavigation.tryEmit(navigation)
    }

    fun dismissPendingNavigation() {
        _pendingNavigation.value = null
    }

    fun clearMessage() {
        _message.value = null
    }
}

enum class NavigationRequestKind {
    TopLevel,
    Secondary
}

data class NavigationRequest(
    val route: AppRoute,
    val kind: NavigationRequestKind
)

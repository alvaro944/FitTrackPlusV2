package com.alvarocervantes.fittrackplus.core.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.content.Context
import android.net.Uri
import com.alvarocervantes.fittrackplus.core.design.AppThemeMode
import com.alvarocervantes.fittrackplus.data.preferences.UserPreferencesRepository
import com.alvarocervantes.fittrackplus.domain.usecase.ExportUserDataUseCase
import com.alvarocervantes.fittrackplus.domain.usecase.UserDataExport
import dagger.hilt.android.qualifiers.ApplicationContext
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
    private val userPreferencesRepository: UserPreferencesRepository,
    private val exportUserData: ExportUserDataUseCase,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _blockedRoute = MutableStateFlow<AppRoute?>(null)
    private val _menuHiddenForRoute = MutableStateFlow<AppRoute?>(null)
    private val _pendingNavigation = MutableStateFlow<NavigationRequest?>(null)
    private val _message = MutableStateFlow<String?>(null)
    private val _approvedNavigation = MutableSharedFlow<NavigationRequest>(extraBufferCapacity = 1)
    // Fires when the user taps the bottom-nav item for the tab they're already on, so that
    // tab's screen can pop its own internal state back to the top (e.g. History closing a
    // detail view back to the list) instead of the tap doing nothing.
    private val _activeTabReselected = MutableSharedFlow<AppRoute>(extraBufferCapacity = 1)
    private val _exportRequests = MutableSharedFlow<UserDataExport>(extraBufferCapacity = 1)

    val pendingNavigation: StateFlow<NavigationRequest?> = _pendingNavigation.asStateFlow()
    val message: StateFlow<String?> = _message.asStateFlow()
    val menuHiddenForRoute: StateFlow<AppRoute?> = _menuHiddenForRoute.asStateFlow()
    val approvedNavigation: SharedFlow<NavigationRequest> = _approvedNavigation.asSharedFlow()
    val activeTabReselected: SharedFlow<AppRoute> = _activeTabReselected.asSharedFlow()
    val exportRequests: SharedFlow<UserDataExport> = _exportRequests.asSharedFlow()

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

    fun showMessage(text: String) {
        _message.value = text
    }

    /**
     * Screens whose header shows trailing actions in the top-end corner hide the floating shell
     * menu button while they are up, so the two never overlap. Callers must clear this on dispose,
     * the same way they clear a navigation blocker.
     */
    fun setMenuButtonHidden(route: AppRoute, hidden: Boolean) {
        _menuHiddenForRoute.value = when {
            hidden -> route
            _menuHiddenForRoute.value == route -> null
            else -> _menuHiddenForRoute.value
        }
    }

    fun requestDataExport() {
        viewModelScope.launch {
            runCatching { exportUserData() }
                .onSuccess { _exportRequests.emit(it) }
                .onFailure { throwable ->
                    _message.value = throwable.message ?: "No se pudieron preparar los datos para exportar."
                }
        }
    }

    fun saveDataExport(uri: Uri, export: UserDataExport) {
        viewModelScope.launch {
            runCatching {
                requireNotNull(context.contentResolver.openOutputStream(uri))
                    .bufferedWriter().use { it.write(export.content) }
            }.onSuccess {
                _message.value = "Datos exportados correctamente."
            }.onFailure { throwable ->
                _message.value = throwable.message ?: "No se pudieron guardar los datos exportados."
            }
        }
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

    fun notifyActiveTabReselected(route: AppRoute) {
        _activeTabReselected.tryEmit(route)
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

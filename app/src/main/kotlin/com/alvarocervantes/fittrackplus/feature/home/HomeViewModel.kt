package com.alvarocervantes.fittrackplus.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alvarocervantes.fittrackplus.data.preferences.UserPreferencesRepository
import com.alvarocervantes.fittrackplus.data.repository.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Calendar
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class HomeUiState(
    val activeRoutineId: Long? = null,
    val sessionsThisWeek: Int = 0,
    val totalSessions: Int = 0,
    val isLoading: Boolean = true,
    val message: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val workoutRepository: WorkoutRepository
) : ViewModel() {

    private val message = MutableStateFlow<String?>(null)

    val uiState: StateFlow<HomeUiState> = combine(
        userPreferencesRepository.activeRoutineId,
        workoutRepository.observeFinishedSessions()
    ) { activeId, sessions ->
        val weekStart = currentWeekStartMillis()
        HomeUiState(
            activeRoutineId = activeId,
            sessionsThisWeek = sessions.count { it.startedAt >= weekStart },
            totalSessions = sessions.size,
            isLoading = false
        )
    }.catch { throwable ->
        message.value = throwable.message ?: "No se pudo cargar el inicio."
        emit(HomeUiState(isLoading = false))
    }.combine(message) { state, currentMessage ->
        state.copy(message = currentMessage)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )

    fun clearMessage() {
        message.value = null
    }

    private fun currentWeekStartMillis(): Long {
        val cal = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }
}

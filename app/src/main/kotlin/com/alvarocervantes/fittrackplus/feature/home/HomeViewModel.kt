package com.alvarocervantes.fittrackplus.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alvarocervantes.fittrackplus.data.local.entity.WorkoutSessionEntity
import com.alvarocervantes.fittrackplus.data.preferences.UserPreferencesRepository
import com.alvarocervantes.fittrackplus.data.repository.WorkoutRepository
import com.alvarocervantes.fittrackplus.domain.model.StepsData
import com.alvarocervantes.fittrackplus.domain.usecase.ReadDailyStepsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Calendar
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn

data class HomeUiState(
    val activeRoutineId: Long? = null,
    val trainedDaysThisWeek: Set<Int> = emptySet(),
    val sessionsThisWeek: Int = 0,
    val totalSessions: Int = 0,
    val todaySteps: Long? = null,
    val dailyStepGoal: Int = 10_000,
    val stepsDaysCompleted: Set<Int> = emptySet(),
    val isLoading: Boolean = true,
    val message: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val workoutRepository: WorkoutRepository,
    private val readDailyStepsUseCase: ReadDailyStepsUseCase
) : ViewModel() {

    private val message = MutableStateFlow<String?>(null)
    private val stepsData = MutableStateFlow<StepsData?>(null)

    init {
        userPreferencesRepository.healthConnectConnected
            .onEach { connected ->
                stepsData.value = if (connected) readDailyStepsUseCase() else null
            }
            .catch { stepsData.value = null }
            .launchIn(viewModelScope)
    }

    val uiState: StateFlow<HomeUiState> = combine(
        userPreferencesRepository.activeRoutineId,
        workoutRepository.observeFinishedSessions(),
        stepsData,
        userPreferencesRepository.dailyStepGoal
    ) { activeId, sessions, steps, stepGoal ->
        val nowMillis = System.currentTimeMillis()
        val trainedDays = trainedDaysThisWeek(sessions, nowMillis = nowMillis)
        HomeUiState(
            activeRoutineId = activeId,
            trainedDaysThisWeek = trainedDays,
            sessionsThisWeek = sessions.count { isInCurrentWeek(it.startedAt, nowMillis = nowMillis) },
            totalSessions = sessions.size,
            todaySteps = steps?.todaySteps,
            dailyStepGoal = stepGoal,
            stepsDaysCompleted = computeStepsDaysCompleted(steps, stepGoal),
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
}

private fun computeStepsDaysCompleted(steps: StepsData?, goal: Int): Set<Int> {
    if (steps == null || goal <= 0) return emptySet()
    return steps.weekDaySteps
        .filterValues { daySteps -> daySteps >= goal }
        .keys
}

internal fun trainedDaysThisWeek(
    sessions: List<WorkoutSessionEntity>,
    nowMillis: Long = System.currentTimeMillis(),
    calendarFactory: () -> Calendar = { Calendar.getInstance() }
): Set<Int> {
    return sessions
        .asSequence()
        .filter { isInCurrentWeek(it.startedAt, nowMillis = nowMillis, calendarFactory = calendarFactory) }
        .map { weekdayIndexMondayFirst(it.startedAt, calendarFactory) }
        .toSet()
}

internal fun isInCurrentWeek(
    timeMillis: Long,
    nowMillis: Long = System.currentTimeMillis(),
    calendarFactory: () -> Calendar = { Calendar.getInstance() }
): Boolean {
    return timeMillis >= currentWeekStartMillis(nowMillis, calendarFactory)
}

internal fun currentWeekStartMillis(
    nowMillis: Long = System.currentTimeMillis(),
    calendarFactory: () -> Calendar = { Calendar.getInstance() }
): Long {
    return calendarFactory().apply {
        timeInMillis = nowMillis
        firstDayOfWeek = Calendar.MONDAY
        set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}

internal fun weekdayIndexMondayFirst(
    timeMillis: Long,
    calendarFactory: () -> Calendar = { Calendar.getInstance() }
): Int {
    val dayOfWeek = calendarFactory().apply {
        timeInMillis = timeMillis
    }.get(Calendar.DAY_OF_WEEK)
    return (dayOfWeek + 5) % 7
}

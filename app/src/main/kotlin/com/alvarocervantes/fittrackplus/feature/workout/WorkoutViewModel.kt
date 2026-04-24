package com.alvarocervantes.fittrackplus.feature.workout

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alvarocervantes.fittrackplus.data.local.relation.WorkoutSessionWithExercises
import com.alvarocervantes.fittrackplus.data.preferences.UserPreferencesRepository
import com.alvarocervantes.fittrackplus.data.repository.WorkoutRepository
import com.alvarocervantes.fittrackplus.domain.model.WorkoutPreview
import com.alvarocervantes.fittrackplus.domain.usecase.FinishWorkoutSessionUseCase
import com.alvarocervantes.fittrackplus.domain.usecase.GetNextWorkoutPreviewUseCase
import com.alvarocervantes.fittrackplus.domain.usecase.StartWorkoutSessionUseCase
import com.alvarocervantes.fittrackplus.domain.usecase.UpdateWorkoutSetUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class WorkoutViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val workoutRepository: WorkoutRepository,
    private val getNextWorkoutPreview: GetNextWorkoutPreviewUseCase,
    private val startWorkoutSession: StartWorkoutSessionUseCase,
    private val finishWorkoutSession: FinishWorkoutSessionUseCase,
    private val updateWorkoutSet: UpdateWorkoutSetUseCase
) : ViewModel() {

    companion object {
        private const val SESSION_KEY = "active_session_id"
    }

    private val _uiState = MutableStateFlow(WorkoutUiState())
    val uiState: StateFlow<WorkoutUiState> = _uiState.asStateFlow()

    init {
        userPreferencesRepository.activeRoutineId
            .distinctUntilChanged()
            .onEach { activeRoutineId ->
                loadWorkoutState(activeRoutineId)
            }
            .catch { throwable ->
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        message = throwable.message ?: "No se pudo cargar el entrenamiento."
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun refresh() {
        viewModelScope.launch {
            loadWorkoutState(_uiState.value.activeRoutineId)
        }
    }

    fun startWorkout() {
        val routineId = _uiState.value.activeRoutineId
        if (routineId == null) {
            _uiState.update { state ->
                state.copy(message = "Selecciona una rutina activa antes de entrenar.")
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { state -> state.copy(isStarting = true) }

            val result = runCatching { startWorkoutSession(routineId) }

            result.onFailure { throwable ->
                _uiState.update { state ->
                    state.copy(
                        isStarting = false,
                        message = throwable.message ?: "No se pudo iniciar el entrenamiento."
                    )
                }
            }
            if (result.isFailure) return@launch

            val startedSession = result.getOrNull()
            if (startedSession == null) {
                _uiState.update { state ->
                    state.copy(isStarting = false, message = "No se pudo iniciar el entrenamiento.")
                }
                return@launch
            }

            savedStateHandle[SESSION_KEY] = startedSession.sessionId

            val activeSession = workoutRepository
                .getSessionWithExercises(startedSession.sessionId)
                ?.toUiState()
                ?.let { enrichWithPreviousWeights(it) }

            _uiState.update { state ->
                state.copy(
                    isStarting = false,
                    preview = null,
                    activeSession = activeSession,
                    message = if (activeSession == null) "No se pudo cargar la sesion iniciada." else null
                )
            }
        }
    }

    fun updateSetWeight(setId: Long, weightText: String) {
        val set = _uiState.value.activeSession?.findSet(setId) ?: return
        updateSetState(setId) { it.copy(weightText = weightText) }
        persistSet(
            setId = setId,
            weightText = weightText,
            repsText = set.repsText
        )
    }

    fun updateSetReps(setId: Long, repsText: String) {
        val set = _uiState.value.activeSession?.findSet(setId) ?: return
        updateSetState(setId) { it.copy(repsText = repsText) }
        persistSet(
            setId = setId,
            weightText = set.weightText,
            repsText = repsText
        )
    }

    fun finishWorkout() {
        val sessionId = _uiState.value.activeSession?.sessionId ?: return

        viewModelScope.launch {
            _uiState.update { state -> state.copy(isFinishing = true) }

            runCatching {
                finishWorkoutSession(sessionId)
            }.onSuccess {
                savedStateHandle.remove<Long>(SESSION_KEY)
                val activeRoutineId = _uiState.value.activeRoutineId
                val nextPreview = activeRoutineId?.let { getNextWorkoutPreview(it) }
                _uiState.update { state ->
                    state.copy(
                        isFinishing = false,
                        activeSession = null,
                        preview = nextPreview?.toUiState(),
                        message = "Entrenamiento finalizado."
                    )
                }
            }.onFailure { throwable ->
                _uiState.update { state ->
                    state.copy(
                        isFinishing = false,
                        message = throwable.message ?: "No se pudo finalizar el entrenamiento."
                    )
                }
            }
        }
    }

    fun clearMessage() {
        _uiState.update { state -> state.copy(message = null) }
    }

    private fun persistSet(setId: Long, weightText: String, repsText: String) {
        viewModelScope.launch {
            runCatching {
                updateWorkoutSet(
                    setId = setId,
                    weightText = weightText,
                    repsText = repsText
                )
            }.onFailure { throwable ->
                _uiState.update { state ->
                    state.copy(message = throwable.message ?: "No se pudo guardar la serie.")
                }
            }
        }
    }

    private suspend fun loadWorkoutState(activeRoutineId: Long?) {
        _uiState.update { state ->
            state.copy(
                isLoading = true,
                activeRoutineId = activeRoutineId,
                message = null
            )
        }

        val savedSessionId = savedStateHandle.get<Long>(SESSION_KEY)
        val activeSession = if (savedSessionId != null) {
            val session = workoutRepository.getSessionWithExercises(savedSessionId)
                ?.takeIf { it.session.finishedAt == null }
                ?.toUiState()
                ?.let { enrichWithPreviousWeights(it) }
            if (session == null) savedStateHandle.remove<Long>(SESSION_KEY)
            session
        } else {
            workoutRepository.getActiveSessionWithExercises()
                ?.toUiState()
                ?.let { enrichWithPreviousWeights(it) }
        }
        val preview = if (activeSession == null && activeRoutineId != null) {
            getNextWorkoutPreview(activeRoutineId)?.toUiState()
        } else {
            null
        }

        _uiState.update { state ->
            state.copy(
                isLoading = false,
                activeSession = activeSession,
                preview = preview
            )
        }
    }

    private suspend fun enrichWithPreviousWeights(
        session: ActiveWorkoutSessionUiState
    ): ActiveWorkoutSessionUiState {
        return session.copy(
            exercises = session.exercises.map { exercise ->
                exercise.copy(
                    sets = exercise.sets.map { set ->
                        val prevKg = workoutRepository.getLastWeightKgForExerciseSet(
                            exerciseName = exercise.name,
                            setNumber = set.setNumber
                        )
                        set.copy(previousWeight = prevKg?.toInputText())
                    }
                )
            }
        )
    }

    private fun updateSetState(
        setId: Long,
        transform: (WorkoutSetUiState) -> WorkoutSetUiState
    ) {
        _uiState.update { state ->
            val activeSession = state.activeSession ?: return@update state
            state.copy(
                activeSession = activeSession.copy(
                    exercises = activeSession.exercises.map { exercise ->
                        exercise.copy(
                            sets = exercise.sets.map { set ->
                                if (set.id == setId) transform(set) else set
                            }
                        )
                    }
                )
            )
        }
    }
}

data class WorkoutUiState(
    val isLoading: Boolean = true,
    val isStarting: Boolean = false,
    val isFinishing: Boolean = false,
    val activeRoutineId: Long? = null,
    val preview: WorkoutPreviewUiState? = null,
    val activeSession: ActiveWorkoutSessionUiState? = null,
    val message: String? = null
)

data class WorkoutPreviewUiState(
    val routineName: String,
    val dayName: String,
    val weekNumber: Int,
    val exerciseCount: Int
)

data class ActiveWorkoutSessionUiState(
    val sessionId: Long,
    val routineName: String,
    val dayName: String,
    val weekNumber: Int,
    val startedAt: Long,
    val exercises: List<WorkoutExerciseUiState>
) {
    val totalSetCount: Int = exercises.sumOf { it.sets.size }
    val completedSetCount: Int = exercises.sumOf { exercise ->
        exercise.sets.count { it.repsText.toIntOrNull()?.let { reps -> reps > 0 } == true }
    }
}

data class WorkoutExerciseUiState(
    val id: Long,
    val name: String,
    val targetRepsText: String,
    val sets: List<WorkoutSetUiState>
)

data class WorkoutSetUiState(
    val id: Long,
    val setNumber: Int,
    val weightText: String,
    val repsText: String,
    val previousWeight: String? = null
)

private fun WorkoutPreview.toUiState(): WorkoutPreviewUiState {
    return WorkoutPreviewUiState(
        routineName = routineName,
        dayName = dayName,
        weekNumber = weekNumber,
        exerciseCount = exerciseCount
    )
}

private fun WorkoutSessionWithExercises.toUiState(): ActiveWorkoutSessionUiState {
    return ActiveWorkoutSessionUiState(
        sessionId = session.id,
        routineName = session.routineNameSnapshot,
        dayName = session.dayNameSnapshot,
        weekNumber = session.weekNumber,
        startedAt = session.startedAt,
        exercises = exercises
            .sortedBy { it.exercise.position }
            .map { exerciseWithSets ->
                WorkoutExerciseUiState(
                    id = exerciseWithSets.exercise.id,
                    name = exerciseWithSets.exercise.exerciseNameSnapshot,
                    targetRepsText = exerciseWithSets.exercise.targetRepsSnapshot,
                    sets = exerciseWithSets.sets
                        .sortedBy { it.setNumber }
                        .map { set ->
                            WorkoutSetUiState(
                                id = set.id,
                                setNumber = set.setNumber,
                                weightText = set.weightKg.toInputText(),
                                repsText = set.reps.toString()
                            )
                        }
                )
            }
    )
}

private fun ActiveWorkoutSessionUiState.findSet(setId: Long): WorkoutSetUiState? {
    return exercises.firstNotNullOfOrNull { exercise ->
        exercise.sets.firstOrNull { it.id == setId }
    }
}

private fun Double.toInputText(): String {
    return if (this % 1.0 == 0.0) {
        toInt().toString()
    } else {
        toString()
    }
}

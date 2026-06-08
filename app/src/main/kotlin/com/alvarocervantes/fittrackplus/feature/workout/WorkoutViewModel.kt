package com.alvarocervantes.fittrackplus.feature.workout

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alvarocervantes.fittrackplus.data.local.relation.WorkoutSessionWithExercises
import com.alvarocervantes.fittrackplus.data.preferences.UserPreferencesRepository
import com.alvarocervantes.fittrackplus.data.repository.RoutineRepository
import com.alvarocervantes.fittrackplus.data.repository.WorkoutRepository
import com.alvarocervantes.fittrackplus.domain.model.PrType
import com.alvarocervantes.fittrackplus.domain.model.WorkoutPreview
import com.alvarocervantes.fittrackplus.domain.usecase.DetectPersonalRecordUseCase
import com.alvarocervantes.fittrackplus.domain.usecase.FinishWorkoutSessionUseCase
import com.alvarocervantes.fittrackplus.domain.usecase.GetNextWorkoutPreviewUseCase
import com.alvarocervantes.fittrackplus.domain.usecase.StartWorkoutSessionUseCase
import com.alvarocervantes.fittrackplus.domain.usecase.UpdateWorkoutSetUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class WorkoutViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val routineRepository: RoutineRepository,
    private val workoutRepository: WorkoutRepository,
    private val getNextWorkoutPreview: GetNextWorkoutPreviewUseCase,
    private val startWorkoutSession: StartWorkoutSessionUseCase,
    private val finishWorkoutSession: FinishWorkoutSessionUseCase,
    private val updateWorkoutSet: UpdateWorkoutSetUseCase,
    private val detectPersonalRecord: DetectPersonalRecordUseCase
) : ViewModel() {

    companion object {
        private const val SESSION_KEY = "active_session_id"
    }

    private val _uiState = MutableStateFlow(WorkoutUiState())
    val uiState: StateFlow<WorkoutUiState> = _uiState.asStateFlow()

    private val _prHapticEvent = Channel<Unit>(Channel.BUFFERED)
    val prHapticEvent = _prHapticEvent.receiveAsFlow()

    private var restTimerJob: Job? = null

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

        // Refresca el preview cuando cambia el contenido de la rutina activa (ej: nombre editado).
        // drop(1) evita un reload doble en el arranque (ya cubierto por activeRoutineId arriba).
        routineRepository.observeRoutines()
            .drop(1)
            .onEach {
                val state = _uiState.value
                if (state.activeSession == null && state.activeRoutineId != null) {
                    loadWorkoutState(state.activeRoutineId)
                }
            }
            .catch { }
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
        val previousRepsWasZero = (set.repsText.toIntOrNull() ?: 0) == 0
        val exerciseName = _uiState.value.activeSession?.exercises
            ?.firstOrNull { ex -> ex.sets.any { it.id == setId } }?.name
        val shouldAutoStartTimer = shouldAutoStartRestTimer(
            previousRepsText = set.repsText,
            nextRepsText = repsText,
            timer = _uiState.value.restTimer
        )
        updateSetState(setId) { it.copy(repsText = repsText) }
        if (shouldAutoStartTimer) {
            startRestTimer(_uiState.value.restTimer.durationSeconds.takeIf { it > 0 } ?: DEFAULT_REST_TIMER_SECONDS)
        }
        persistSet(
            setId = setId,
            weightText = set.weightText,
            repsText = repsText,
            exerciseName = exerciseName,
            previousRepsWasZero = previousRepsWasZero
        )
    }

    fun startRestTimer(seconds: Int) {
        _uiState.update { state ->
            state.copy(restTimer = state.restTimer.startRestTimer(seconds))
        }
        launchRestTimerJob()
    }

    fun pauseRestTimer() {
        stopRestTimerJob()
        _uiState.update { state -> state.copy(restTimer = state.restTimer.pauseRestTimer()) }
    }

    fun resumeRestTimer() {
        _uiState.update { state -> state.copy(restTimer = state.restTimer.resumeRestTimer()) }
        if (_uiState.value.restTimer.status == RestTimerStatus.Running) {
            launchRestTimerJob()
        }
    }

    fun resetRestTimer() {
        stopRestTimerJob()
        _uiState.update { state -> state.copy(restTimer = state.restTimer.resetRestTimer()) }
    }

    fun cancelRestTimer() {
        stopRestTimerJob()
        _uiState.update { state -> state.copy(restTimer = state.restTimer.cancelRestTimer()) }
    }

    fun setAutoStartRestTimerEnabled(enabled: Boolean) {
        _uiState.update { state -> state.copy(restTimer = state.restTimer.withAutoStart(enabled)) }
    }

    fun finishWorkout() {
        val sessionId = _uiState.value.activeSession?.sessionId ?: return

        viewModelScope.launch {
            _uiState.update { state -> state.copy(isFinishing = true) }

            runCatching {
                finishWorkoutSession(sessionId)
            }.onSuccess {
                savedStateHandle.remove<Long>(SESSION_KEY)
                stopRestTimerJob()
                val prCount = _uiState.value.activeSession?.prCount ?: 0
                val activeRoutineId = _uiState.value.activeRoutineId
                val nextPreview = activeRoutineId?.let { getNextWorkoutPreview(it) }
                _uiState.update { state ->
                    state.copy(
                        isFinishing = false,
                        activeSession = null,
                        preview = nextPreview?.toUiState(),
                        restTimer = state.restTimer.cancelRestTimer(),
                        celebration = if (prCount > 0) CelebrationData(prCount) else null,
                        message = if (prCount == 0) "Entrenamiento finalizado." else null
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

    fun dismissCelebration() {
        _uiState.update { state -> state.copy(celebration = null, message = "Entrenamiento finalizado.") }
    }

    private fun persistSet(
        setId: Long,
        weightText: String,
        repsText: String,
        exerciseName: String? = null,
        previousRepsWasZero: Boolean = false
    ) {
        viewModelScope.launch {
            runCatching {
                updateWorkoutSet(
                    setId = setId,
                    weightText = weightText,
                    repsText = repsText
                )
            }.onSuccess {
                if (previousRepsWasZero && exerciseName != null) {
                    val reps = repsText.toIntOrNull() ?: 0
                    val weightKg = weightText.toDoubleOrNull() ?: 0.0
                    detectPrIfEligible(setId, exerciseName, weightKg, reps)
                }
            }.onFailure { throwable ->
                _uiState.update { state ->
                    state.copy(message = throwable.message ?: "No se pudo guardar la serie.")
                }
            }
        }
    }

    private suspend fun detectPrIfEligible(
        setId: Long,
        exerciseName: String,
        weightKg: Double,
        reps: Int
    ) {
        if (reps <= 0 || weightKg <= 0.0) return
        val prType = detectPersonalRecord(exerciseName, weightKg, reps)
        if (prType != null) {
            updateSetState(setId) { it.copy(prType = prType) }
            _uiState.update { state ->
                val session = state.activeSession ?: return@update state
                state.copy(activeSession = session.copy(prCount = session.prCount + 1))
            }
            _prHapticEvent.trySend(Unit)
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
                preview = preview,
                restTimer = if (activeSession == null) state.restTimer.cancelRestTimer() else state.restTimer
            )
        }
        if (activeSession == null) {
            stopRestTimerJob()
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

    private fun launchRestTimerJob() {
        stopRestTimerJob()
        restTimerJob = viewModelScope.launch {
            while (_uiState.value.restTimer.status == RestTimerStatus.Running) {
                delay(1_000)
                _uiState.update { state -> state.copy(restTimer = state.restTimer.tickRestTimer()) }
            }
        }
    }

    private fun stopRestTimerJob() {
        restTimerJob?.cancel()
        restTimerJob = null
    }
}

data class WorkoutUiState(
    val isLoading: Boolean = true,
    val isStarting: Boolean = false,
    val isFinishing: Boolean = false,
    val activeRoutineId: Long? = null,
    val preview: WorkoutPreviewUiState? = null,
    val activeSession: ActiveWorkoutSessionUiState? = null,
    val restTimer: RestTimerUiState = RestTimerUiState(),
    val celebration: CelebrationData? = null,
    val message: String? = null
)

data class CelebrationData(val prCount: Int)

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
    val exercises: List<WorkoutExerciseUiState>,
    val prCount: Int = 0
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
    val previousWeight: String? = null,
    val prType: PrType? = null
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

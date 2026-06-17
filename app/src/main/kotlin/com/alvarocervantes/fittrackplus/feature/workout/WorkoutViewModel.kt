package com.alvarocervantes.fittrackplus.feature.workout

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alvarocervantes.fittrackplus.data.local.relation.WorkoutSessionWithExercises
import com.alvarocervantes.fittrackplus.data.preferences.UserPreferencesRepository
import com.alvarocervantes.fittrackplus.data.repository.RoutineRepository
import com.alvarocervantes.fittrackplus.data.repository.WorkoutRepository
import com.alvarocervantes.fittrackplus.domain.model.PrType
import com.alvarocervantes.fittrackplus.domain.model.ProgressionHint
import com.alvarocervantes.fittrackplus.domain.model.RoutineExerciseAlternativeDraft
import com.alvarocervantes.fittrackplus.domain.model.RoutineExerciseSnapshot
import com.alvarocervantes.fittrackplus.domain.model.RoutineSnapshot
import com.alvarocervantes.fittrackplus.domain.model.WorkoutPreview
import com.alvarocervantes.fittrackplus.domain.usecase.DetectPersonalRecordUseCase
import com.alvarocervantes.fittrackplus.domain.usecase.FinishWorkoutSessionUseCase
import com.alvarocervantes.fittrackplus.domain.usecase.GetNextWorkoutPreviewUseCase
import com.alvarocervantes.fittrackplus.domain.usecase.GetProgressionHintUseCase
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

@Suppress("TooManyFunctions")
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
    private val detectPersonalRecord: DetectPersonalRecordUseCase,
    private val getProgressionHint: GetProgressionHintUseCase
) : ViewModel() {

    companion object {
        private const val SESSION_KEY = "active_session_id"
    }

    private val _uiState = MutableStateFlow(WorkoutUiState())
    val uiState: StateFlow<WorkoutUiState> = _uiState.asStateFlow()

    private val _prHapticEvent = Channel<Unit>(Channel.BUFFERED)
    val prHapticEvent = _prHapticEvent.receiveAsFlow()

    private val _setCompletionHapticEvent = Channel<Unit>(Channel.BUFFERED)
    val setCompletionHapticEvent = _setCompletionHapticEvent.receiveAsFlow()

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

    fun openExerciseAlternatives(workoutExerciseId: Long) {
        viewModelScope.launch {
            val picker = buildAlternativePicker(workoutExerciseId)
            if (picker == null) {
                _uiState.update { state ->
                    state.copy(message = "No se pudieron cargar las alternativas de este ejercicio.")
                }
            } else {
                _uiState.update { state -> state.copy(alternativePicker = picker) }
            }
        }
    }

    fun dismissExerciseAlternatives() {
        _uiState.update { state -> state.copy(alternativePicker = null) }
    }

    fun startCreatingExerciseAlternative() {
        _uiState.update { state ->
            val picker = state.alternativePicker ?: return@update state
            val seed = picker.currentOption
            state.copy(
                alternativePicker = picker.copy(
                    draft = ExerciseAlternativeDraftUiState(
                        name = seed.name,
                        targetSets = seed.targetSets.toString(),
                        targetRepsText = seed.targetRepsText,
                        notes = seed.notes.orEmpty()
                    )
                )
            )
        }
    }

    fun cancelCreatingExerciseAlternative() {
        _uiState.update { state ->
            val picker = state.alternativePicker ?: return@update state
            state.copy(alternativePicker = picker.copy(draft = null, isSaving = false))
        }
    }

    fun updateAlternativeDraftName(name: String) {
        updateAlternativeDraft { draft -> draft.copy(name = normalizeWorkoutAlternativeNameInput(name)) }
    }

    fun updateAlternativeDraftSets(targetSets: String) {
        updateAlternativeDraft { draft -> draft.copy(targetSets = targetSets) }
    }

    fun updateAlternativeDraftReps(targetRepsText: String) {
        updateAlternativeDraft { draft -> draft.copy(targetRepsText = targetRepsText) }
    }

    fun updateAlternativeDraftNotes(notes: String) {
        updateAlternativeDraft { draft -> draft.copy(notes = notes) }
    }

    fun saveExerciseAlternative() {
        val picker = _uiState.value.alternativePicker ?: return
        val draft = picker.draft ?: return
        if (!draft.canSave) return

        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(alternativePicker = state.alternativePicker?.copy(isSaving = true))
            }
            runCatching {
                val alternative = routineRepository.createExerciseAlternative(
                    routineExerciseId = picker.routineExerciseId,
                    draft = RoutineExerciseAlternativeDraft(
                        name = draft.name.trim(),
                        targetSets = draft.targetSets.toInt(),
                        targetRepsText = draft.targetRepsText.trim(),
                        notes = draft.notes.trim().ifBlank { null }
                    )
                )
                val applied = workoutRepository.replaceWorkoutExerciseVariant(
                    workoutExerciseId = picker.workoutExerciseId,
                    variantKey = alternative.variantKey,
                    exerciseName = alternative.name,
                    targetRepsText = alternative.targetRepsText,
                    targetSets = alternative.targetSets
                )
                applied
            }.onSuccess { applied ->
                if (applied) {
                    refreshActiveSessionFromRepository()
                    _uiState.update { state ->
                        state.copy(
                            alternativePicker = null,
                            message = "Alternativa creada y aplicada para este entrenamiento."
                        )
                    }
                } else {
                    _uiState.update { state ->
                        state.copy(
                            alternativePicker = state.alternativePicker?.copy(isSaving = false),
                            message = "Cambia la variante antes de registrar series en este ejercicio."
                        )
                    }
                }
            }.onFailure { throwable ->
                _uiState.update { state ->
                    state.copy(
                        alternativePicker = state.alternativePicker?.copy(isSaving = false),
                        message = throwable.message ?: "No se pudo crear la alternativa."
                    )
                }
            }
        }
    }

    fun applyExerciseVariant(variantKey: String) {
        val picker = _uiState.value.alternativePicker ?: return
        val option = picker.options.firstOrNull { it.variantKey == variantKey } ?: return

        viewModelScope.launch {
            val applied = workoutRepository.replaceWorkoutExerciseVariant(
                workoutExerciseId = picker.workoutExerciseId,
                variantKey = option.variantKey,
                exerciseName = option.name,
                targetRepsText = option.targetRepsText,
                targetSets = option.targetSets
            )
            if (applied) {
                refreshActiveSessionFromRepository()
                _uiState.update { state -> state.copy(alternativePicker = null) }
            } else {
                _uiState.update { state ->
                    state.copy(message = "Cambia la variante antes de registrar series en este ejercicio.")
                }
            }
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
                ?.let { enrichWorkoutSession(it) }
            val hints = activeSession?.let { loadProgressionHints(it) }.orEmpty()

            _uiState.update { state ->
                state.copy(
                    isStarting = false,
                    preview = null,
                    activeSession = activeSession,
                    expandedExerciseId = resolveExpandedExerciseId(activeSession),
                    hints = hints,
                    message = if (activeSession == null) "No se pudo cargar la sesion iniciada." else null
                )
            }
        }
    }

    fun toggleExerciseExpanded(exerciseId: Long) {
        _uiState.update { state ->
            val session = state.activeSession ?: return@update state
            if (session.exercises.none { it.id == exerciseId }) return@update state
            state.copy(
                expandedExerciseId = if (state.expandedExerciseId == exerciseId) {
                    null
                } else {
                    exerciseId
                }
            )
        }
    }

    fun updateSetWeight(setId: Long, weightText: String) {
        val set = _uiState.value.activeSession?.findSet(setId) ?: return
        val previousSetWasIncomplete = !isWorkoutSetCompleted(
            weightText = set.weightText,
            repsText = set.repsText
        )
        val sanitizedWeightText = sanitizeWorkoutWeightInput(weightText)
        val exercise = _uiState.value.activeSession?.exercises
            ?.firstOrNull { ex -> ex.sets.any { it.id == setId } }
        val shouldAutoStartTimer = shouldAutoStartRestTimerOnSetCompletion(
            previousWeightText = set.weightText,
            previousRepsText = set.repsText,
            nextWeightText = sanitizedWeightText,
            nextRepsText = set.repsText,
            timer = _uiState.value.restTimer
        )
        updateSetState(setId) {
            it.copy(
                weightText = sanitizedWeightText,
                isCompleted = isWorkoutSetCompleted(sanitizedWeightText, it.repsText)
            )
        }
        if (shouldAutoStartTimer) {
            startRestTimer(_uiState.value.restTimer.durationSeconds.takeIf { it > 0 } ?: DEFAULT_REST_TIMER_SECONDS)
        }
        persistSet(
            setId = setId,
            weightText = sanitizedWeightText,
            repsText = set.repsText,
            exerciseName = exercise?.name,
            variantKey = exercise?.variantKey,
            previousSetWasIncomplete = previousSetWasIncomplete
        )
    }

    fun updateSetReps(setId: Long, repsText: String) {
        val set = _uiState.value.activeSession?.findSet(setId) ?: return
        val previousSetWasIncomplete = !isWorkoutSetCompleted(
            weightText = set.weightText,
            repsText = set.repsText
        )
        val exercise = _uiState.value.activeSession?.exercises
            ?.firstOrNull { ex -> ex.sets.any { it.id == setId } }
        val shouldAutoStartTimer = shouldAutoStartRestTimerOnSetCompletion(
            previousWeightText = set.weightText,
            previousRepsText = set.repsText,
            nextWeightText = set.weightText,
            nextRepsText = repsText,
            timer = _uiState.value.restTimer
        )
        updateSetState(setId) {
            it.copy(
                repsText = repsText,
                isCompleted = isWorkoutSetCompleted(
                    weightText = it.weightText,
                    repsText = repsText
                )
            )
        }
        if (shouldAutoStartTimer) {
            startRestTimer(_uiState.value.restTimer.durationSeconds.takeIf { it > 0 } ?: DEFAULT_REST_TIMER_SECONDS)
        }
        persistSet(
            setId = setId,
            weightText = set.weightText,
            repsText = repsText,
            exerciseName = exercise?.name,
            variantKey = exercise?.variantKey,
            previousSetWasIncomplete = previousSetWasIncomplete
        )
    }

    fun stepSetReps(setId: Long, delta: Int) {
        val set = _uiState.value.activeSession?.findSet(setId) ?: return
        updateSetReps(setId, adjustWorkoutRepsInput(set.repsText, delta))
    }

    fun stepSetWeight(setId: Long, deltaKg: Double) {
        val set = _uiState.value.activeSession?.findSet(setId) ?: return
        updateSetWeight(setId, adjustWorkoutWeightInput(set.weightText, deltaKg))
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
        val session = _uiState.value.activeSession ?: return
        val sessionId = session.sessionId
        val shouldDiscardSession = session.completedSetCount == 0

        viewModelScope.launch {
            _uiState.update { state -> state.copy(isFinishing = true) }

            runCatching {
                if (shouldDiscardSession) {
                    workoutRepository.discardSession(sessionId)
                } else {
                    finishWorkoutSession(sessionId)
                }
            }.onSuccess {
                savedStateHandle.remove<Long>(SESSION_KEY)
                stopRestTimerJob()
                val prCount = if (shouldDiscardSession) 0 else (_uiState.value.activeSession?.prCount ?: 0)
                val activeRoutineId = _uiState.value.activeRoutineId
                val nextPreview = activeRoutineId?.let { getNextWorkoutPreview(it) }
                _uiState.update { state ->
                    state.copy(
                        isFinishing = false,
                        activeSession = null,
                        expandedExerciseId = null,
                        hints = emptyMap(),
                        preview = nextPreview?.toUiState(),
                        restTimer = state.restTimer.cancelRestTimer(),
                        celebration = if (!shouldDiscardSession && prCount > 0) CelebrationData(prCount) else null,
                        message = when {
                            shouldDiscardSession -> "Sesion descartada."
                            prCount == 0 -> "Entrenamiento finalizado."
                            else -> null
                        }
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
        variantKey: String? = null,
        previousSetWasIncomplete: Boolean = false
    ) {
        viewModelScope.launch {
            runCatching {
                updateWorkoutSet(
                    setId = setId,
                    weightText = weightText,
                    repsText = repsText
                )
            }.onSuccess {
                val reps = repsText.toIntOrNull() ?: 0
                val weightKg = parseWorkoutWeightInput(weightText) ?: 0.0
                if (previousSetWasIncomplete && isWorkoutSetCompleted(weightText, repsText)) {
                    _setCompletionHapticEvent.trySend(Unit)
                }
                if (previousSetWasIncomplete && exerciseName != null && variantKey != null) {
                    detectPrIfEligible(setId, variantKey, weightKg, reps)
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
        variantKey: String,
        weightKg: Double,
        reps: Int
    ) {
        if (reps <= 0 || weightKg <= 0.0) return
        val prType = detectPersonalRecord(variantKey, weightKg, reps)
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
                ?.let { enrichWorkoutSession(it) }
            if (session == null) savedStateHandle.remove<Long>(SESSION_KEY)
            session
        } else {
            workoutRepository.getActiveSessionWithExercises()
                ?.toUiState()
                ?.let { enrichWorkoutSession(it) }
        }
        val preview = if (activeSession == null && activeRoutineId != null) {
            getNextWorkoutPreview(activeRoutineId)?.toUiState()
        } else {
            null
        }
        val hints = activeSession?.let { loadProgressionHints(it) }.orEmpty()

        _uiState.update { state ->
            state.copy(
                isLoading = false,
                activeSession = activeSession,
                expandedExerciseId = resolveExpandedExerciseId(
                    session = activeSession,
                    preferredExerciseId = state.expandedExerciseId
                ),
                hints = hints,
                preview = preview,
                restTimer = if (activeSession == null) state.restTimer.cancelRestTimer() else state.restTimer
            )
        }
        if (activeSession == null) {
            stopRestTimerJob()
        }
    }

    private suspend fun enrichWorkoutSession(
        session: ActiveWorkoutSessionUiState
    ): ActiveWorkoutSessionUiState {
        return session.copy(
            exercises = session.exercises.map { exercise ->
                val suggestedExercise = exercise.withSuggestedInputs()
                suggestedExercise.copy(
                    sets = suggestedExercise.sets.map { set ->
                        val prevKg = workoutRepository.getLastWeightKgForExerciseSet(
                            variantKey = exercise.variantKey,
                            setNumber = set.setNumber
                        )
                        val previousReps = workoutRepository.getLastRepsForExerciseSet(
                            variantKey = exercise.variantKey,
                            setNumber = set.setNumber
                        )?.takeIf { it > 0 }
                        set.copy(
                            previousWeight = prevKg?.toInputText(),
                            previousReps = previousReps
                        )
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
                    exercises = updateWorkoutExercisesForSet(
                        exercises = activeSession.exercises,
                        setId = setId,
                        transform = transform
                    )
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

    private fun updateAlternativeDraft(
        transform: (ExerciseAlternativeDraftUiState) -> ExerciseAlternativeDraftUiState
    ) {
        _uiState.update { state ->
            val picker = state.alternativePicker ?: return@update state
            val draft = picker.draft ?: return@update state
            state.copy(alternativePicker = picker.copy(draft = transform(draft)))
        }
    }

    private suspend fun refreshActiveSessionFromRepository() {
        val sessionId = _uiState.value.activeSession?.sessionId ?: return
        val refreshed = workoutRepository.getSessionWithExercises(sessionId)
            ?.toUiState()
            ?.let { enrichWorkoutSession(it) }
        val hints = refreshed?.let { loadProgressionHints(it) }.orEmpty()
        _uiState.update { state ->
            state.copy(
                activeSession = refreshed,
                expandedExerciseId = resolveExpandedExerciseId(
                    session = refreshed,
                    preferredExerciseId = state.expandedExerciseId
                ),
                hints = hints
            )
        }
    }

    private suspend fun loadProgressionHints(
        session: ActiveWorkoutSessionUiState
    ): Map<Long, ProgressionHint> {
        return session.exercises.associate { exercise ->
            exercise.id to getProgressionHint(
                variantKey = exercise.variantKey,
                targetRepsText = exercise.targetRepsText
            )
        }
    }

    @Suppress("ReturnCount")
    private suspend fun buildAlternativePicker(workoutExerciseId: Long): ExerciseAlternativesUiState? {
        val currentState = _uiState.value
        val session = currentState.activeSession ?: return null
        val routineId = currentState.activeRoutineId ?: return null
        val workoutExercise = session.exercises.firstOrNull { it.id == workoutExerciseId } ?: return null
        val routineExerciseId = workoutExercise.exerciseTemplateId ?: return null
        val routine = routineRepository.getRoutineSnapshot(routineId) ?: return null
        val routineExercise = routine.findExercise(routineExerciseId) ?: return null
        return ExerciseAlternativesUiState(
            workoutExerciseId = workoutExerciseId,
            routineExerciseId = routineExerciseId,
            title = workoutExercise.name,
            currentVariantKey = workoutExercise.variantKey,
            defaultVariantKey = routineExercise.defaultVariantKey,
            options = routineExercise.toVariantOptions(currentVariantKey = workoutExercise.variantKey)
        )
    }
}

data class WorkoutUiState(
    val isLoading: Boolean = true,
    val isStarting: Boolean = false,
    val isFinishing: Boolean = false,
    val activeRoutineId: Long? = null,
    val preview: WorkoutPreviewUiState? = null,
    val activeSession: ActiveWorkoutSessionUiState? = null,
    val expandedExerciseId: Long? = null,
    val hints: Map<Long, ProgressionHint> = emptyMap(),
    val alternativePicker: ExerciseAlternativesUiState? = null,
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
        exercise.sets.count { it.isCompleted }
    }
}

data class WorkoutExerciseUiState(
    val id: Long,
    val exerciseTemplateId: Long?,
    val variantKey: String,
    val name: String,
    val targetRepsText: String,
    val sets: List<WorkoutSetUiState>
)

private val WorkoutExerciseUiState.completedSetCount: Int
    get() = sets.count { it.isCompleted }

private val WorkoutExerciseUiState.isCompleted: Boolean
    get() = sets.isNotEmpty() && completedSetCount == sets.size

data class WorkoutSetUiState(
    val id: Long,
    val setNumber: Int,
    val weightText: String,
    val repsText: String,
    val isCompleted: Boolean = false,
    val previousWeight: String? = null,
    val previousReps: Int? = null,
    val prType: PrType? = null
)

data class ExerciseAlternativesUiState(
    val workoutExerciseId: Long,
    val routineExerciseId: Long,
    val title: String,
    val currentVariantKey: String,
    val defaultVariantKey: String,
    val options: List<ExerciseVariantOptionUiState>,
    val draft: ExerciseAlternativeDraftUiState? = null,
    val isSaving: Boolean = false
) {
    val currentOption: ExerciseVariantOptionUiState
        get() = options.firstOrNull { it.variantKey == currentVariantKey } ?: options.first()
}

data class ExerciseVariantOptionUiState(
    val variantKey: String,
    val name: String,
    val targetSets: Int,
    val targetRepsText: String,
    val notes: String?,
    val isDefault: Boolean,
    val isCurrent: Boolean
)

data class ExerciseAlternativeDraftUiState(
    val name: String = "",
    val targetSets: String = "3",
    val targetRepsText: String = "8-12",
    val notes: String = ""
) {
    val canSave: Boolean
        get() = name.isNotBlank() &&
            targetSets.toIntOrNull()?.let { it in 1..99 } == true &&
            com.alvarocervantes.fittrackplus.feature.routines.isValidTargetReps(targetRepsText)
}

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
                    exerciseTemplateId = exerciseWithSets.exercise.exerciseTemplateId,
                    variantKey = exerciseWithSets.exercise.performedVariantKey,
                    name = exerciseWithSets.exercise.exerciseNameSnapshot,
                    targetRepsText = exerciseWithSets.exercise.targetRepsSnapshot,
                    sets = exerciseWithSets.sets
                        .sortedBy { it.setNumber }
                        .map { set ->
                            WorkoutSetUiState(
                                id = set.id,
                                setNumber = set.setNumber,
                                weightText = if (set.weightKg > 0.0) set.weightKg.toInputText() else "",
                                repsText = if (set.reps > 0) set.reps.toString() else "",
                                isCompleted = set.weightKg > 0.0 && set.reps > 0
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

private fun ActiveWorkoutSessionUiState.firstPendingExerciseId(): Long? {
    return exercises.firstOrNull { exercise -> !exercise.isCompleted }?.id
}

private fun resolveExpandedExerciseId(
    session: ActiveWorkoutSessionUiState?,
    preferredExerciseId: Long? = null
): Long? {
    session ?: return null
    return when {
        preferredExerciseId != null && session.exercises.any { it.id == preferredExerciseId } -> preferredExerciseId
        else -> session.firstPendingExerciseId() ?: session.exercises.firstOrNull()?.id
    }
}

private fun WorkoutExerciseUiState.withSuggestedInputs(): WorkoutExerciseUiState {
    return copy(sets = applyWorkoutSetInputSuggestions(sets = sets, targetRepsText = targetRepsText))
}

private fun Double.toInputText(): String {
    return if (this % 1.0 == 0.0) {
        toInt().toString()
    } else {
        toString().replace('.', ',')
    }
}

private fun RoutineSnapshot.findExercise(routineExerciseId: Long): RoutineExerciseSnapshot? {
    return days.flatMap { it.exercises }.firstOrNull { it.id == routineExerciseId }
}

private fun RoutineExerciseSnapshot.toVariantOptions(currentVariantKey: String): List<ExerciseVariantOptionUiState> {
    val baseOption = ExerciseVariantOptionUiState(
        variantKey = variantKey,
        name = name,
        targetSets = targetSets,
        targetRepsText = targetRepsText,
        notes = notes,
        isDefault = defaultVariantKey == variantKey,
        isCurrent = currentVariantKey == variantKey
    )
    return buildList {
        add(baseOption)
        alternatives.sortedBy { it.position }.forEach { alternative ->
            add(
                ExerciseVariantOptionUiState(
                    variantKey = alternative.variantKey,
                    name = alternative.name,
                    targetSets = alternative.targetSets,
                    targetRepsText = alternative.targetRepsText,
                    notes = alternative.notes,
                    isDefault = defaultVariantKey == alternative.variantKey,
                    isCurrent = currentVariantKey == alternative.variantKey
                )
            )
        }
    }
}

private fun normalizeWorkoutAlternativeNameInput(value: String): String {
    if (value.isEmpty()) return value
    return value.replaceFirstChar { char ->
        if (char.isLowerCase()) char.titlecase() else char.toString()
    }
}

internal fun suggestWorkoutSetRepsInput(
    previousCompletedReps: Int?,
    targetRepsText: String
): String {
    previousCompletedReps?.takeIf { it > 0 }?.let { reps ->
        return reps.toString()
    }

    val targetRange = com.alvarocervantes.fittrackplus.domain.usecase.parseProgressionTargetRange(targetRepsText)
    return targetRange?.first?.toString().orEmpty()
}

internal fun applyWorkoutSetInputSuggestions(
    sets: List<WorkoutSetUiState>,
    targetRepsText: String
): List<WorkoutSetUiState> {
    var previousCompletedReps: Int? = null
    return sets.map { set ->
        when {
            set.isCompleted -> {
                previousCompletedReps = set.repsText.toIntOrNull()
                set
            }
            set.repsText.isNotBlank() -> set
            else -> set.copy(
                repsText = suggestWorkoutSetRepsInput(
                    previousCompletedReps = previousCompletedReps,
                    targetRepsText = targetRepsText
                )
            )
        }
    }
}

internal fun updateWorkoutExercisesForSet(
    exercises: List<WorkoutExerciseUiState>,
    setId: Long,
    transform: (WorkoutSetUiState) -> WorkoutSetUiState
): List<WorkoutExerciseUiState> {
    return exercises.map { exercise ->
        if (exercise.sets.none { set -> set.id == setId }) {
            exercise
        } else {
            exercise.copy(
                sets = exercise.sets.map { set ->
                    if (set.id == setId) transform(set) else set
                }
            ).withSuggestedInputs()
        }
    }
}

internal fun adjustWorkoutRepsInput(currentValue: String, delta: Int): String {
    val baseValue = currentValue.toIntOrNull() ?: 0
    return (baseValue + delta).coerceAtLeast(0).toString()
}

internal fun sanitizeWorkoutWeightInput(value: String): String {
    val sanitized = buildString {
        var hasDecimalSeparator = false
        value.forEach { char ->
            when {
                char.isDigit() -> append(char)
                (char == '.' || char == ',') && !hasDecimalSeparator -> {
                    append(',')
                    hasDecimalSeparator = true
                }
            }
        }
    }
    return sanitized
}

internal fun parseWorkoutWeightInput(value: String): Double? {
    return sanitizeWorkoutWeightInput(value)
        .replace(',', '.')
        .toDoubleOrNull()
}

internal fun adjustWorkoutWeightInput(currentValue: String, deltaKg: Double): String {
    val baseValue = parseWorkoutWeightInput(currentValue) ?: 0.0
    val adjusted = (baseValue + deltaKg).coerceAtLeast(0.0)
    return adjusted.toInputText()
}

internal fun isWorkoutSetCompleted(weightText: String, repsText: String): Boolean {
    val weightKg = parseWorkoutWeightInput(weightText) ?: 0.0
    val reps = repsText.toIntOrNull() ?: 0
    return weightKg > 0.0 && reps > 0
}

internal fun shouldAutoStartRestTimerOnSetCompletion(
    previousWeightText: String,
    previousRepsText: String,
    nextWeightText: String,
    nextRepsText: String,
    timer: RestTimerUiState
): Boolean {
    if (!timer.autoStartEnabled || timer.status == RestTimerStatus.Running || timer.status == RestTimerStatus.Paused) {
        return false
    }
    return !isWorkoutSetCompleted(previousWeightText, previousRepsText) &&
        isWorkoutSetCompleted(nextWeightText, nextRepsText)
}

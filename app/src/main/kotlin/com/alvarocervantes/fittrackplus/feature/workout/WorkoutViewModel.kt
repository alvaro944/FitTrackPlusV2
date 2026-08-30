@file:Suppress("TooManyFunctions")

package com.alvarocervantes.fittrackplus.feature.workout

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alvarocervantes.fittrackplus.domain.model.isWorkoutSetCompleted
import com.alvarocervantes.fittrackplus.data.local.relation.WorkoutSessionWithExercises
import com.alvarocervantes.fittrackplus.data.preferences.RestTimerPreferences
import com.alvarocervantes.fittrackplus.data.preferences.UserPreferencesRepository
import com.alvarocervantes.fittrackplus.data.repository.RoutineRepository
import com.alvarocervantes.fittrackplus.data.repository.WorkoutRepository
import com.alvarocervantes.fittrackplus.domain.model.PrType
import com.alvarocervantes.fittrackplus.domain.model.ProgressionHint
import com.alvarocervantes.fittrackplus.domain.model.RoutineExerciseAlternativeDraft
import com.alvarocervantes.fittrackplus.domain.model.RoutineExerciseSnapshot
import com.alvarocervantes.fittrackplus.domain.model.RoutineSnapshot
import com.alvarocervantes.fittrackplus.domain.model.WorkoutPreview
import com.alvarocervantes.fittrackplus.domain.model.WeightUnit
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

@Suppress("TooManyFunctions", "LargeClass")
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
        private const val CELEBRATION_DURATION_MS = 1_500L
    }

    private val _uiState = MutableStateFlow(WorkoutUiState())
    val uiState: StateFlow<WorkoutUiState> = _uiState.asStateFlow()

    private val _prHapticEvent = Channel<Unit>(Channel.BUFFERED)
    val prHapticEvent = _prHapticEvent.receiveAsFlow()

    private val _setCompletionHapticEvent = Channel<Unit>(Channel.BUFFERED)
    val setCompletionHapticEvent = _setCompletionHapticEvent.receiveAsFlow()

    private val _restTimerFinishedHapticEvent = Channel<Unit>(Channel.BUFFERED)
    val restTimerFinishedHapticEvent = _restTimerFinishedHapticEvent.receiveAsFlow()

    private var restTimerJob: Job? = null
    private var celebrationDismissJob: Job? = null
    private var hasLoadedWorkoutState = false

    init {
        userPreferencesRepository.weightUnit
            .distinctUntilChanged()
            .onEach { preference ->
                val weightUnit = WeightUnit.fromPreference(preference)
                val changed = _uiState.value.weightUnit != weightUnit
                _uiState.update { state -> state.copy(weightUnit = weightUnit) }
                if (changed && _uiState.value.activeSession != null) {
                    refreshActiveSessionFromRepository()
                }
            }
            .launchIn(viewModelScope)

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

        userPreferencesRepository.restTimerPreferences
            .distinctUntilChanged()
            .onEach { preferences ->
                val restoredTimer = preferences.toRestTimerUiState()
                val timer = if (hasLoadedWorkoutState && _uiState.value.activeSession == null) {
                    RestTimerUiState(autoStartEnabled = restoredTimer.autoStartEnabled)
                } else {
                    restoredTimer
                }
                _uiState.update { state -> state.copy(restTimer = timer) }
                if (timer.status == RestTimerStatus.Running) {
                    launchRestTimerJob()
                } else {
                    stopRestTimerJob()
                }
                if (preferences != timer.toPreferences()) {
                    persistRestTimer(timer)
                }
                if (restoredTimer.status == RestTimerStatus.Finished &&
                    preferences.status == RestTimerStatus.Running.name
                ) {
                    _restTimerFinishedHapticEvent.trySend(Unit)
                }
            }
            .catch { }
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

    /**
     * Reloads only when no session is currently in progress, so a session reopened from History is
     * picked up on resume without disturbing an active workout.
     */
    fun refreshIfIdle() {
        if (_uiState.value.activeSession != null) return
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
        updateAlternativeDraft { draft -> draft.copy(name = name) }
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
        if (!draft.canSave || picker.isSaving) return

        // The swap rebuilds this exercise's sets, so it is rejected once any set holds data.
        // Check that before writing anything: otherwise the alternative is persisted in the
        // routine, the swap fails, and every retry leaves another orphan copy behind.
        if (!picker.canSwapVariant) {
            _uiState.update { state ->
                state.copy(
                    alternativePicker = state.alternativePicker?.copy(draft = null),
                    message = "Ya has registrado series en este ejercicio. " +
                        "Crea la alternativa desde Rutinas."
                )
            }
            return
        }

        if (picker.hasVariantNamed(draft.name)) {
            _uiState.update { state ->
                state.copy(message = "Ya existe una variante con ese nombre.")
            }
            return
        }

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
                    targetSets = alternative.targetSets,
                    notes = alternative.notes
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
                    // The pre-check passed but the swap still lost a race against a set write.
                    // The alternative is already saved in the routine, so close the dialog rather
                    // than inviting a retry that would create a duplicate.
                    _uiState.update { state ->
                        state.copy(
                            alternativePicker = null,
                            message = "Alternativa guardada en la rutina, pero no se aplico: " +
                                "ya has registrado series en este ejercicio."
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
        if (picker.isSaving || !picker.canSwapVariant) return
        val option = picker.options.firstOrNull { it.variantKey == variantKey } ?: return

        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(alternativePicker = state.alternativePicker?.copy(isSaving = true))
            }
            val applied = workoutRepository.replaceWorkoutExerciseVariant(
                workoutExerciseId = picker.workoutExerciseId,
                variantKey = option.variantKey,
                exerciseName = option.name,
                targetRepsText = option.targetRepsText,
                targetSets = option.targetSets,
                notes = option.notes
            )
            if (applied) {
                refreshActiveSessionFromRepository()
                _uiState.update { state ->
                    state.copy(
                        alternativePicker = null,
                        message = "Variante cambiada a ${option.name}."
                    )
                }
            } else {
                _uiState.update { state ->
                    state.copy(
                        alternativePicker = null,
                        message = "Ya has registrado series en este ejercicio."
                    )
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
                ?.toUiState(_uiState.value.weightUnit)
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
        val sanitizedWeightText = sanitizeWorkoutWeightInput(weightText)
        updateSetState(setId) {
            updateWorkoutSetWeightInput(it, sanitizedWeightText)
        }
        persistSet(
            setId = setId,
            weightText = sanitizedWeightText,
            repsText = set.repsText,
            isCompleted = false
        )
    }

    fun updateSetReps(setId: Long, repsText: String) {
        val set = _uiState.value.activeSession?.findSet(setId) ?: return
        val sanitizedRepsText = sanitizeWorkoutRepsInput(repsText)
        updateSetState(setId) {
            updateWorkoutSetRepsInput(it, sanitizedRepsText)
        }
        persistSet(
            setId = setId,
            weightText = set.weightText,
            repsText = sanitizedRepsText,
            isCompleted = false
        )
    }

    fun updateSetNotes(setId: Long, notes: String) {
        updateSetState(setId) { set -> set.copy(notes = notes) }
        viewModelScope.launch {
            runCatching {
                workoutRepository.updateSetNotes(setId, notes)
            }.onFailure { throwable ->
                _uiState.update { state ->
                    state.copy(message = throwable.message ?: "No se pudieron guardar las notas de la serie.")
                }
            }
        }
    }

    fun completeSet(setId: Long) {
        val session = _uiState.value.activeSession ?: return
        val set = session.findSet(setId) ?: return
        if (set.isCompleted) {
            updateSetState(setId) { it.copy(isCompleted = false, prType = null) }
            if (set.prType != null) {
                _uiState.update { state ->
                    val activeSession = state.activeSession ?: return@update state
                    state.copy(activeSession = activeSession.copy(prCount = (activeSession.prCount - 1).coerceAtLeast(0)))
                }
            }
            persistSet(
                setId = setId,
                weightText = set.weightText,
                repsText = set.repsText,
                isCompleted = false
            )
            return
        }
        if (!isWorkoutSetReadyToComplete(set.repsText, set.isCompleted)) return

        val exercise = session.exercises.firstOrNull { ex -> ex.sets.any { it.id == setId } }
        updateSetState(setId) { it.copy(isCompleted = true) }
        if (shouldAutoStartRestTimerOnManualSetCompletion(_uiState.value.restTimer)) {
            startRestTimer(_uiState.value.restTimer.durationSeconds.takeIf { it > 0 } ?: DEFAULT_REST_TIMER_SECONDS)
        }
        persistSet(
            setId = setId,
            weightText = set.weightText,
            repsText = set.repsText,
            exerciseName = exercise?.name,
            variantKey = exercise?.variantKey,
            previousSetWasIncomplete = true,
            isCompleted = true
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
        val timer = _uiState.value.restTimer.startRestTimer(seconds)
        updateRestTimer(timer)
        launchRestTimerJob()
    }

    fun pauseRestTimer() {
        stopRestTimerJob()
        updateRestTimer(_uiState.value.restTimer.pauseRestTimer())
    }

    fun resumeRestTimer() {
        updateRestTimer(_uiState.value.restTimer.resumeRestTimer())
        if (_uiState.value.restTimer.status == RestTimerStatus.Running) {
            launchRestTimerJob()
        }
    }

    fun resetRestTimer() {
        stopRestTimerJob()
        updateRestTimer(_uiState.value.restTimer.resetRestTimer())
    }

    fun cancelRestTimer() {
        stopRestTimerJob()
        updateRestTimer(_uiState.value.restTimer.cancelRestTimer())
    }

    fun setAutoStartRestTimerEnabled(enabled: Boolean) {
        updateRestTimer(_uiState.value.restTimer.withAutoStart(enabled))
    }

    fun finishWorkout(notes: String? = null) {
        val session = _uiState.value.activeSession ?: return
        val sessionId = session.sessionId
        val shouldDiscardSession = session.completedSetCount == 0

        viewModelScope.launch {
            _uiState.update { state -> state.copy(isFinishing = true) }

            runCatching {
                if (shouldDiscardSession) {
                    workoutRepository.discardSession(sessionId)
                } else {
                    finishWorkoutSession(sessionId, notes)
                }
            }.onSuccess {
                savedStateHandle.remove<Long>(SESSION_KEY)
                stopRestTimerJob()
                val cancelledTimer = _uiState.value.restTimer.cancelRestTimer()
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
                        restTimer = cancelledTimer,
                        celebration = if (!shouldDiscardSession && prCount > 0) CelebrationData(prCount) else null,
                        message = when {
                            shouldDiscardSession -> "Sesion descartada."
                            prCount == 0 -> "Entrenamiento finalizado."
                            else -> null
                        }
                    )
                }
                persistRestTimer(cancelledTimer)
                if (!shouldDiscardSession && prCount > 0) {
                    scheduleCelebrationDismissal()
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
        celebrationDismissJob?.cancel()
        celebrationDismissJob = null
        _uiState.update { state -> state.copy(celebration = null, message = "Entrenamiento finalizado.") }
    }

    private fun scheduleCelebrationDismissal() {
        celebrationDismissJob?.cancel()
        celebrationDismissJob = viewModelScope.launch {
            delay(CELEBRATION_DURATION_MS)
            _uiState.update { state ->
                if (state.celebration == null) state else {
                    state.copy(celebration = null, message = "Entrenamiento finalizado.")
                }
            }
        }
    }

    private fun persistSet(
        setId: Long,
        weightText: String,
        repsText: String,
        exerciseName: String? = null,
        variantKey: String? = null,
        previousSetWasIncomplete: Boolean = false,
        isCompleted: Boolean? = null
    ) {
        val weightUnit = _uiState.value.weightUnit
        viewModelScope.launch {
            runCatching {
                updateWorkoutSet(
                    setId = setId,
                    weightText = weightText,
                    repsText = repsText,
                    weightUnit = weightUnit
                )
                if (isCompleted != null) {
                    workoutRepository.updateSetCompletion(setId, isCompleted)
                }
            }.onSuccess {
                val reps = repsText.toIntOrNull() ?: 0
                val weightKg = parseWorkoutWeightInput(weightText)
                    ?.let(weightUnit::toKilograms)
                    ?: 0.0
                if (previousSetWasIncomplete && isCompleted == true) {
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
                ?.toUiState(_uiState.value.weightUnit)
                ?.let { enrichWorkoutSession(it) }
            if (session == null) savedStateHandle.remove<Long>(SESSION_KEY)
            session
        } else {
            workoutRepository.getActiveSessionWithExercises()
                ?.toUiState(_uiState.value.weightUnit)
                ?.let { enrichWorkoutSession(it) }
        }
        val preview = if (activeSession == null && activeRoutineId != null) {
            getNextWorkoutPreview(activeRoutineId)?.toUiState()
        } else {
            null
        }
        val hints = activeSession?.let { loadProgressionHints(it) }.orEmpty()
        hasLoadedWorkoutState = true
        val timerAfterLoading = if (activeSession == null) {
            _uiState.value.restTimer.cancelRestTimer()
        } else {
            _uiState.value.restTimer
        }

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
                restTimer = timerAfterLoading
            )
        }
        if (activeSession == null) {
            stopRestTimerJob()
            persistRestTimer(timerAfterLoading)
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
                            previousWeight = prevKg?.let(_uiState.value.weightUnit::fromKilograms)?.toInputText(),
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
                val nextTimer = _uiState.value.restTimer.tickRestTimer()
                _uiState.update { state -> state.copy(restTimer = nextTimer) }
                if (nextTimer.status == RestTimerStatus.Finished) {
                    persistRestTimer(nextTimer)
                    _restTimerFinishedHapticEvent.trySend(Unit)
                }
            }
        }
    }

    private fun stopRestTimerJob() {
        restTimerJob?.cancel()
        restTimerJob = null
    }

    private fun updateRestTimer(timer: RestTimerUiState) {
        _uiState.update { state -> state.copy(restTimer = timer) }
        persistRestTimer(timer)
    }

    private fun persistRestTimer(timer: RestTimerUiState) {
        viewModelScope.launch {
            userPreferencesRepository.setRestTimerPreferences(timer.toPreferences())
        }
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
            ?.toUiState(_uiState.value.weightUnit)
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
            options = routineExercise.toVariantOptions(currentVariantKey = workoutExercise.variantKey),
            canSwapVariant = workoutRepository.canReplaceWorkoutExerciseVariant(workoutExerciseId)
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
    val weightUnit: WeightUnit = WeightUnit.Kilograms,
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
    val notes: String? = null,
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
    val notes: String? = null,
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
    val canSwapVariant: Boolean = true,
    val draft: ExerciseAlternativeDraftUiState? = null,
    val isSaving: Boolean = false
) {
    val currentOption: ExerciseVariantOptionUiState
        get() = options.firstOrNull { it.variantKey == currentVariantKey } ?: options.first()

    /** True when [name] already belongs to the base exercise or one of its alternatives. */
    fun hasVariantNamed(name: String): Boolean {
        val candidate = name.trim()
        return options.any { it.name.trim().equals(candidate, ignoreCase = true) }
    }
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

private fun WorkoutSessionWithExercises.toUiState(weightUnit: WeightUnit): ActiveWorkoutSessionUiState {
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
                    notes = exerciseWithSets.exercise.notes,
                    sets = exerciseWithSets.sets
                        .sortedBy { it.setNumber }
                        .map { set ->
                            WorkoutSetUiState(
                                id = set.id,
                                setNumber = set.setNumber,
                                weightText = if (set.weightKg > 0.0) {
                                    weightUnit.fromKilograms(set.weightKg).toInputText()
                                } else {
                                    ""
                                },
                                repsText = if (set.reps > 0) set.reps.toString() else "",
                                notes = set.notes,
                                isCompleted = set.isCompleted
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

internal fun Double.toInputText(): String {
    return if (this % 1.0 == 0.0) {
        toInt().toString()
    } else {
        // Double.toString() can emit scientific notation for extreme magnitudes (e.g. "1.0E7").
        // "%.2f" never does, so format explicitly instead of relying on toString().
        String.format(java.util.Locale.US, "%.2f", this)
            .trimEnd('0')
            .trimEnd('.')
            .replace('.', ',')
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
    // 'e'/'E' signals scientific notation ("1.0E7"): everything from there on is an exponent, not
    // more decimal digits. Dropping it avoids keeping the trailing digits and producing a
    // wrong-but-plausible value like "1,07".
    val withoutExponent = value.takeWhile { it != 'e' && it != 'E' }

    return buildString {
        var hasDecimalSeparator = false
        var integerDigits = 0
        var decimalDigits = 0
        for (char in withoutExponent) {
            // Cap the digits on each side instead of the parsed value, so typing stays fluid and
            // an accidental extra keystroke cannot log 999999999 kg.
            val atDigitCap = if (hasDecimalSeparator) {
                decimalDigits == MAX_WEIGHT_DECIMAL_DIGITS
            } else {
                integerDigits == MAX_WEIGHT_INTEGER_DIGITS
            }
            if (char.isDigit() && atDigitCap) break

            when {
                char.isDigit() && hasDecimalSeparator -> {
                    decimalDigits++
                    append(char)
                }
                char.isDigit() -> {
                    integerDigits++
                    append(char)
                }
                // Any other stray character (e.g. a mistyped letter) is skipped so typing flows.
                (char == '.' || char == ',') && !hasDecimalSeparator -> {
                    append(',')
                    hasDecimalSeparator = true
                }
            }
        }
    }
}

/** Longest reps entry accepted: nobody logs four digits of repetitions. */
internal const val MAX_REPS_DIGITS: Int = 3

/** Digits accepted on each side of the decimal separator for a weight. */
internal const val MAX_WEIGHT_INTEGER_DIGITS: Int = 4
internal const val MAX_WEIGHT_DECIMAL_DIGITS: Int = 2

internal fun sanitizeWorkoutRepsInput(value: String): String {
    return value.takeWhile { it.isDigit() }.take(MAX_REPS_DIGITS)
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

internal fun isWorkoutSetCompleted(repsText: String): Boolean {
    val reps = repsText.toIntOrNull() ?: 0
    return isWorkoutSetCompleted(reps)
}

internal fun isWorkoutSetReadyToComplete(
    repsText: String,
    isCompleted: Boolean
): Boolean {
    return !isCompleted && isWorkoutSetCompleted(repsText)
}

internal fun updateWorkoutSetWeightInput(set: WorkoutSetUiState, weightText: String): WorkoutSetUiState {
    return set.copy(weightText = weightText, isCompleted = false, prType = null)
}

internal fun updateWorkoutSetRepsInput(set: WorkoutSetUiState, repsText: String): WorkoutSetUiState {
    return set.copy(repsText = repsText, isCompleted = false, prType = null)
}

internal fun shouldAutoStartRestTimerOnSetCompletion(
    previousRepsText: String,
    nextRepsText: String,
    timer: RestTimerUiState
): Boolean {
    if (!timer.autoStartEnabled || timer.status == RestTimerStatus.Running || timer.status == RestTimerStatus.Paused) {
        return false
    }
    return !isWorkoutSetCompleted(previousRepsText) &&
        isWorkoutSetCompleted(nextRepsText)
}

internal fun shouldAutoStartRestTimerOnManualSetCompletion(timer: RestTimerUiState): Boolean {
    return timer.autoStartEnabled &&
        timer.status != RestTimerStatus.Running &&
        timer.status != RestTimerStatus.Paused
}

private fun RestTimerPreferences.toRestTimerUiState(
    nowMillis: Long = System.currentTimeMillis()
): RestTimerUiState {
    val status = RestTimerStatus.entries.firstOrNull { it.name == this.status }
        ?: RestTimerStatus.Stopped
    return RestTimerUiState(
        durationSeconds = durationSeconds.coerceAtLeast(0),
        remainingSeconds = remainingSeconds.coerceAtLeast(0),
        status = status,
        endsAtMillis = endsAtMillis,
        autoStartEnabled = autoStartEnabled
    ).tickRestTimer(nowMillis)
}

private fun RestTimerUiState.toPreferences(): RestTimerPreferences {
    return RestTimerPreferences(
        durationSeconds = durationSeconds,
        remainingSeconds = remainingSeconds,
        status = status.name,
        endsAtMillis = endsAtMillis,
        autoStartEnabled = autoStartEnabled
    )
}

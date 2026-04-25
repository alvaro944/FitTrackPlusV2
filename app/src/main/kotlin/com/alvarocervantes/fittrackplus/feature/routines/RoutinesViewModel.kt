package com.alvarocervantes.fittrackplus.feature.routines

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alvarocervantes.fittrackplus.data.preferences.UserPreferencesRepository
import com.alvarocervantes.fittrackplus.data.repository.RoutineRepository
import com.alvarocervantes.fittrackplus.domain.model.RoutineDayDraft
import com.alvarocervantes.fittrackplus.domain.model.RoutineDraft
import com.alvarocervantes.fittrackplus.domain.model.RoutineExerciseDraft
import com.alvarocervantes.fittrackplus.domain.model.RoutineSnapshot
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class RoutinesViewModel @Inject constructor(
    private val routineRepository: RoutineRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RoutinesUiState())
    val uiState: StateFlow<RoutinesUiState> = _uiState.asStateFlow()

    init {
        combine(
            routineRepository.observeRoutines(),
            routineRepository.observeArchivedRoutines(),
            userPreferencesRepository.activeRoutineId
        ) { routines, archivedRoutines, activeRoutineId ->
            val activeId = activeRoutineId?.takeIf { id -> routines.any { it.id == id } }
            val activeItems = routines.map { routine ->
                RoutineListItemUiState(
                    id = routine.id,
                    name = routine.name,
                    dayCount = routine.dayCount,
                    isActive = routine.id == activeId
                )
            }
            val archivedItems = archivedRoutines.map { routine ->
                RoutineListItemUiState(
                    id = routine.id,
                    name = routine.name,
                    dayCount = routine.dayCount,
                    isActive = false
                )
            }
            Triple(activeItems, archivedItems, activeId)
        }
            .onEach { (routines, archivedRoutines, activeRoutineId) ->
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        routines = routines,
                        archivedRoutines = archivedRoutines,
                        activeRoutineId = activeRoutineId
                    )
                }
            }
            .catch { throwable ->
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        message = throwable.message ?: "No se pudieron cargar las rutinas."
                    )
                }
            }
            .launchIn(viewModelScope)

        userPreferencesRepository.hasSeenSnapshotInfo
            .onEach { seen ->
                _uiState.update { state -> state.copy(hasSeenSnapshotInfo = seen) }
            }
            .launchIn(viewModelScope)
    }

    fun startCreateRoutine() {
        _uiState.update { state ->
            state.copy(editor = RoutineEditorUiState())
        }
    }

    fun startCreateRoutineFromTemplate(templateId: String) {
        val template = routineTemplates.firstOrNull { template -> template.id == templateId } ?: return
        _uiState.update { state ->
            state.copy(editor = template.toEditorState().copy(isDirty = true))
        }
    }

    fun startEditRoutine(routineId: Long) {
        viewModelScope.launch {
            val snapshot = routineRepository.getRoutineSnapshot(routineId) ?: return@launch
            _uiState.update { state ->
                state.copy(editor = snapshot.toEditorState())
            }
        }
    }

    fun requestCloseEditor() {
        val editor = _uiState.value.editor ?: return
        if (editor.isDirty) {
            _uiState.update { state -> state.copy(editor = editor.copy(showCloseConfirmation = true)) }
        } else {
            _uiState.update { state -> state.copy(editor = null) }
        }
    }

    fun resolveCloseConfirmation(discard: Boolean) {
        if (discard) {
            _uiState.update { state -> state.copy(editor = null) }
        } else {
            updateEditor { editor -> editor.copy(showCloseConfirmation = false) }
        }
    }

    fun updateRoutineName(name: String) {
        updateEditor { editor -> editor.copy(name = name) }
    }

    fun addDay() {
        updateEditor { editor ->
            editor.copy(days = editor.days + RoutineDayEditorUiState(name = "Dia ${editor.days.size + 1}"))
        }
    }

    fun updateDayName(dayIndex: Int, name: String) {
        updateEditor { editor ->
            editor.copy(days = editor.days.replaceAt(dayIndex) { it.copy(name = name) })
        }
    }

    fun removeDay(dayIndex: Int) {
        updateEditor { editor ->
            if (editor.days.size <= 1) editor else editor.copy(days = editor.days.removeAt(dayIndex))
        }
    }

    fun addExercise(dayIndex: Int) {
        updateEditor { editor ->
            editor.copy(
                days = editor.days.replaceAt(dayIndex) { day ->
                    day.copy(exercises = day.exercises + RoutineExerciseEditorUiState())
                }
            )
        }
    }

    fun updateExerciseName(dayIndex: Int, exerciseIndex: Int, name: String) {
        updateEditor { editor ->
            editor.updateExercise(dayIndex, exerciseIndex) { exercise -> exercise.copy(name = name) }
        }
    }

    fun updateExerciseSets(dayIndex: Int, exerciseIndex: Int, targetSets: String) {
        updateEditor { editor ->
            editor.updateExercise(dayIndex, exerciseIndex) { exercise -> exercise.copy(targetSets = targetSets) }
        }
    }

    fun updateExerciseReps(dayIndex: Int, exerciseIndex: Int, targetRepsText: String) {
        updateEditor { editor ->
            editor.updateExercise(dayIndex, exerciseIndex) { exercise -> exercise.copy(targetRepsText = targetRepsText) }
        }
    }

    fun updateExerciseNotes(dayIndex: Int, exerciseIndex: Int, notes: String) {
        updateEditor { editor ->
            editor.updateExercise(dayIndex, exerciseIndex) { exercise -> exercise.copy(notes = notes) }
        }
    }

    fun removeExercise(dayIndex: Int, exerciseIndex: Int) {
        updateEditor { editor ->
            editor.copy(
                days = editor.days.replaceAt(dayIndex) { day ->
                    if (day.exercises.size <= 1) day else day.copy(exercises = day.exercises.removeAt(exerciseIndex))
                }
            )
        }
    }

    fun applyEditorOperation(operation: RoutineEditorOperation) {
        updateEditor { editor ->
            when (operation) {
                is RoutineEditorOperation.DuplicateDay -> editor.duplicateDay(operation.dayIndex)
                is RoutineEditorOperation.MoveDay -> editor.moveDay(operation.dayIndex, operation.direction)
                is RoutineEditorOperation.DuplicateExercise -> {
                    editor.duplicateExercise(operation.dayIndex, operation.exerciseIndex)
                }
                is RoutineEditorOperation.MoveExercise -> {
                    editor.moveExercise(operation.dayIndex, operation.exerciseIndex, operation.direction)
                }
            }
        }
    }

    fun saveEditor() {
        val editor = _uiState.value.editor ?: return
        if (!editor.canSave) return

        viewModelScope.launch {
            _uiState.update { state -> state.copy(isSaving = true) }

            runCatching {
                val draft = editor.toDraft()
                if (editor.routineId == null) {
                    routineRepository.createRoutine(draft)
                } else {
                    routineRepository.replaceRoutine(editor.routineId, draft)
                    editor.routineId
                }
            }.onSuccess { routineId ->
                _uiState.update { state -> state.copy(editor = null, isSaving = false) }
                if (_uiState.value.activeRoutineId == null) {
                    userPreferencesRepository.setActiveRoutineId(routineId)
                }
            }.onFailure { throwable ->
                _uiState.update { state ->
                    state.copy(
                        isSaving = false,
                        message = throwable.message ?: "No se pudo guardar la rutina."
                    )
                }
            }
        }
    }

    fun setActiveRoutine(routineId: Long) {
        viewModelScope.launch {
            userPreferencesRepository.setActiveRoutineId(routineId)
        }
    }

    fun archiveRoutine(routineId: Long) {
        viewModelScope.launch {
            routineRepository.archiveRoutine(routineId)
            if (_uiState.value.activeRoutineId == routineId) {
                userPreferencesRepository.setActiveRoutineId(null)
            }
        }
    }

    fun setShowArchived(show: Boolean) {
        _uiState.update { state -> state.copy(showArchived = show) }
    }

    fun dismissSnapshotInfo() {
        viewModelScope.launch {
            userPreferencesRepository.dismissSnapshotInfo()
        }
    }

    fun restoreRoutine(routineId: Long) {
        viewModelScope.launch {
            routineRepository.restoreRoutine(routineId)
        }
    }

    fun clearMessage() {
        _uiState.update { state -> state.copy(message = null) }
    }

    private fun updateEditor(transform: (RoutineEditorUiState) -> RoutineEditorUiState) {
        _uiState.update { state ->
            val editor = state.editor ?: return@update state
            state.copy(editor = transform(editor).copy(isDirty = true))
        }
    }
}

private fun RoutineEditorUiState.updateExercise(
    dayIndex: Int,
    exerciseIndex: Int,
    transform: (RoutineExerciseEditorUiState) -> RoutineExerciseEditorUiState
): RoutineEditorUiState {
    return copy(
        days = days.replaceAt(dayIndex) { day ->
            day.copy(exercises = day.exercises.replaceAt(exerciseIndex, transform))
        }
    )
}

data class RoutinesUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val routines: List<RoutineListItemUiState> = emptyList(),
    val archivedRoutines: List<RoutineListItemUiState> = emptyList(),
    val activeRoutineId: Long? = null,
    val editor: RoutineEditorUiState? = null,
    val message: String? = null,
    val showArchived: Boolean = false,
    val hasSeenSnapshotInfo: Boolean = false
)

data class RoutineListItemUiState(
    val id: Long,
    val name: String,
    val dayCount: Int,
    val isActive: Boolean
)

data class RoutineEditorUiState(
    val routineId: Long? = null,
    val name: String = "",
    val days: List<RoutineDayEditorUiState> = listOf(RoutineDayEditorUiState()),
    val isDirty: Boolean = false,
    val showCloseConfirmation: Boolean = false
) {
    val title: String = if (routineId == null) "Nueva rutina" else "Editar rutina"
    val routineNameError: String?
        get() = if (name.isBlank()) "Pon un nombre para la rutina." else null
    val validationMessage: String?
        get() = when {
            routineNameError != null -> routineNameError
            days.any { it.nameError != null } -> "Revisa los nombres de los dias."
            days.any { day -> day.exercises.isEmpty() } -> "Cada dia necesita al menos un ejercicio."
            days.any { day -> day.exercises.any { it.nameError != null } } -> "Revisa los nombres de los ejercicios."
            days.any { day -> day.exercises.any { it.targetSetsError != null } } -> "Las series deben estar entre 1 y 99."
            days.any { day -> day.exercises.any { it.targetRepsError != null } } -> "Revisa las reps objetivo."
            else -> null
        }
    val canSave: Boolean
        get() = routineNameError == null &&
            days.isNotEmpty() &&
            days.all { day ->
                day.nameError == null &&
                day.exercises.isNotEmpty() &&
                    day.exercises.all { exercise ->
                        exercise.nameError == null &&
                            exercise.targetSetsError == null &&
                            exercise.targetRepsError == null
                    }
            }
}

data class RoutineDayEditorUiState(
    val name: String = "Dia 1",
    val exercises: List<RoutineExerciseEditorUiState> = listOf(RoutineExerciseEditorUiState())
) {
    val nameError: String?
        get() = if (name.isBlank()) "Pon un nombre para el dia." else null
}

data class RoutineExerciseEditorUiState(
    val name: String = "",
    val targetSets: String = "3",
    val targetRepsText: String = "8-12",
    val notes: String = ""
) {
    val nameError: String?
        get() = if (name.isBlank()) "Pon un nombre para el ejercicio." else null
    val targetSetsError: String?
        get() = if (targetSets.toIntOrNull()?.let { it in 1..99 } == true) {
            null
        } else {
            "Usa entre 1 y 99 series."
        }
    val targetRepsError: String?
        get() = if (isValidTargetReps(targetRepsText)) {
            null
        } else {
            "Usa 8, 8-12, AMRAP o RPE 8."
        }
}

internal fun isValidTargetReps(value: String): Boolean {
    val normalized = value.trim()
    if (normalized.isEmpty()) return false
    return when {
        normalized.equals("AMRAP", ignoreCase = true) -> true
        normalized.toIntOrNull()?.let { it in 1..99 } == true -> true
        else -> {
            val rangeMatch = Regex("""^(\d{1,2})\s*-\s*(\d{1,2})$""").matchEntire(normalized)
            val rpeMatch = Regex("""^RPE\s*(\d{1,2})$""", RegexOption.IGNORE_CASE).matchEntire(normalized)
            when {
                rangeMatch != null -> {
                    val start = rangeMatch.groupValues[1].toInt()
                    val end = rangeMatch.groupValues[2].toInt()
                    start in 1..99 && end in 1..99 && start <= end
                }
                rpeMatch != null -> rpeMatch.groupValues[1].toInt() in 1..10
                else -> false
            }
        }
    }
}

private fun RoutineSnapshot.toEditorState(): RoutineEditorUiState {
    return RoutineEditorUiState(
        routineId = id,
        name = name,
        days = days.map { day ->
            RoutineDayEditorUiState(
                name = day.name,
                exercises = day.exercises.map { exercise ->
                    RoutineExerciseEditorUiState(
                        name = exercise.name,
                        targetSets = exercise.targetSets.toString(),
                        targetRepsText = exercise.targetRepsText,
                        notes = exercise.notes.orEmpty()
                    )
                }.ifEmpty { listOf(RoutineExerciseEditorUiState()) }
            )
        }.ifEmpty { listOf(RoutineDayEditorUiState()) }
    )
}

private fun RoutineEditorUiState.toDraft(): RoutineDraft {
    return RoutineDraft(
        name = name.trim(),
        days = days.map { day ->
            RoutineDayDraft(
                name = day.name.trim(),
                exercises = day.exercises.map { exercise ->
                    RoutineExerciseDraft(
                        name = exercise.name.trim(),
                        targetSets = exercise.targetSets.toInt(),
                        targetRepsText = exercise.targetRepsText.trim(),
                        notes = exercise.notes.trim().ifBlank { null }
                    )
                }
            )
        }
    )
}

private fun <T> List<T>.replaceAt(index: Int, transform: (T) -> T): List<T> {
    if (index !in indices) return this
    return mapIndexed { currentIndex, item ->
        if (currentIndex == index) transform(item) else item
    }
}

private fun <T> List<T>.removeAt(index: Int): List<T> {
    if (index !in indices) return this
    return filterIndexed { currentIndex, _ -> currentIndex != index }
}

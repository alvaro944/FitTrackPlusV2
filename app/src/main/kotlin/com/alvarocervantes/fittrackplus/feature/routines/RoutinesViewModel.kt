package com.alvarocervantes.fittrackplus.feature.routines

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alvarocervantes.fittrackplus.data.preferences.UserPreferencesRepository
import com.alvarocervantes.fittrackplus.data.repository.RoutineRepository
import com.alvarocervantes.fittrackplus.domain.model.RoutineDayDraft
import com.alvarocervantes.fittrackplus.domain.model.RoutineDraft
import com.alvarocervantes.fittrackplus.domain.model.RoutineExerciseAlternativeDraft
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

@Suppress("TooManyFunctions")
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
        if (editor.hasUnsavedChanges) {
            _uiState.update { state -> state.copy(editor = editor.copy(showCloseConfirmation = true)) }
        } else {
            _uiState.update { state -> state.copy(editor = null) }
        }
    }

    fun discardEditorChanges() {
        _uiState.update { state -> state.copy(editor = null) }
    }

    fun resolveCloseConfirmation(discard: Boolean) {
        if (discard) {
            discardEditorChanges()
        } else {
            updateEditor { editor -> editor.copy(showCloseConfirmation = false) }
        }
    }

    fun toggleDayExpansion(dayIndex: Int) {
        _uiState.update { state ->
            val editor = state.editor ?: return@update state
            state.copy(editor = editor.toggleDayExpansion(dayIndex))
        }
    }

    fun updateRoutineName(name: String) {
        updateEditor { editor -> editor.copy(name = normalizeEditorNameInput(name)) }
    }

    fun addDay() {
        updateEditor { editor ->
            editor.copy(
                days = editor.days + RoutineDayEditorUiState(name = "Dia ${editor.days.size + 1}"),
                expandedDayIndex = editor.days.size
            )
        }
    }

    fun updateDayName(dayIndex: Int, name: String) {
        updateEditor { editor ->
            editor.copy(
                days = editor.days.replaceAt(dayIndex) {
                    it.copy(name = normalizeEditorNameInput(name))
                }
            )
        }
    }

    fun removeDay(dayIndex: Int) {
        updateEditor { editor ->
            if (editor.days.size <= 1) {
                editor
            } else {
                editor.copy(
                    days = editor.days.removeAt(dayIndex),
                    expandedDayIndex = editor.expandedDayIndex.afterRemovingDay(dayIndex)
                )
            }
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
            editor.updateExercise(dayIndex, exerciseIndex) { exercise ->
                exercise.copy(name = normalizeEditorNameInput(name))
            }
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

    fun addExerciseAlternative(dayIndex: Int, exerciseIndex: Int) {
        updateEditor { editor ->
            editor.updateExercise(dayIndex, exerciseIndex) { exercise ->
                exercise.copy(
                    alternatives = exercise.alternatives + RoutineExerciseAlternativeEditorUiState(
                        name = exercise.name,
                        targetSets = exercise.targetSets,
                        targetRepsText = exercise.targetRepsText,
                        notes = exercise.notes
                    )
                )
            }
        }
    }

    fun updateExerciseAlternativeName(dayIndex: Int, exerciseIndex: Int, alternativeIndex: Int, name: String) {
        updateEditor { editor ->
            editor.updateExercise(dayIndex, exerciseIndex) { exercise ->
                exercise.copy(
                    alternatives = exercise.alternatives.replaceAt(alternativeIndex) { alternative ->
                        alternative.copy(name = normalizeEditorNameInput(name))
                    }
                )
            }
        }
    }

    fun updateExerciseAlternativeSets(dayIndex: Int, exerciseIndex: Int, alternativeIndex: Int, targetSets: String) {
        updateEditor { editor ->
            editor.updateExercise(dayIndex, exerciseIndex) { exercise ->
                exercise.copy(
                    alternatives = exercise.alternatives.replaceAt(alternativeIndex) { alternative ->
                        alternative.copy(targetSets = targetSets)
                    }
                )
            }
        }
    }

    fun updateExerciseAlternativeReps(dayIndex: Int, exerciseIndex: Int, alternativeIndex: Int, targetRepsText: String) {
        updateEditor { editor ->
            editor.updateExercise(dayIndex, exerciseIndex) { exercise ->
                exercise.copy(
                    alternatives = exercise.alternatives.replaceAt(alternativeIndex) { alternative ->
                        alternative.copy(targetRepsText = targetRepsText)
                    }
                )
            }
        }
    }

    fun updateExerciseAlternativeNotes(dayIndex: Int, exerciseIndex: Int, alternativeIndex: Int, notes: String) {
        updateEditor { editor ->
            editor.updateExercise(dayIndex, exerciseIndex) { exercise ->
                exercise.copy(
                    alternatives = exercise.alternatives.replaceAt(alternativeIndex) { alternative ->
                        alternative.copy(notes = notes)
                    }
                )
            }
        }
    }

    fun removeExerciseAlternative(dayIndex: Int, exerciseIndex: Int, alternativeIndex: Int) {
        updateEditor { editor ->
            editor.updateExercise(dayIndex, exerciseIndex) { exercise ->
                val alternative = exercise.alternatives.getOrNull(alternativeIndex)
                val updatedDefault = if (alternative?.variantKey != null && exercise.defaultVariantKey == alternative.variantKey) {
                    exercise.variantKey
                } else {
                    exercise.defaultVariantKey
                }
                exercise.copy(
                    defaultVariantKey = updatedDefault,
                    alternatives = exercise.alternatives.removeAt(alternativeIndex)
                )
            }
        }
    }

    fun setExerciseDefaultVariant(dayIndex: Int, exerciseIndex: Int, variantKey: String?) {
        updateEditor { editor ->
            editor.updateExercise(dayIndex, exerciseIndex) { exercise ->
                exercise.copy(defaultVariantKey = variantKey ?: exercise.variantKey)
            }
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
    val showCloseConfirmation: Boolean = false,
    val expandedDayIndex: Int? = null
) {
    val title: String = if (routineId == null) "Nueva rutina" else "Editar rutina"
    val hasUnsavedChanges: Boolean
        get() = isDirty
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
            days.any { day -> day.exercises.any { exercise -> exercise.alternatives.any { it.nameError != null } } } -> {
                "Revisa los nombres de las alternativas."
            }
            days.any { day -> day.exercises.any { exercise -> exercise.alternatives.any { it.targetSetsError != null } } } -> {
                "Las series de las alternativas deben estar entre 1 y 99."
            }
            days.any { day -> day.exercises.any { exercise -> exercise.alternatives.any { it.targetRepsError != null } } } -> {
                "Revisa las reps objetivo de las alternativas."
            }
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
                            exercise.targetRepsError == null &&
                            exercise.alternatives.all { alternative ->
                                alternative.nameError == null &&
                                    alternative.targetSetsError == null &&
                                    alternative.targetRepsError == null
                            }
                    }
            }
}

internal fun RoutineEditorUiState.toggleDayExpansion(dayIndex: Int): RoutineEditorUiState {
    if (dayIndex !in days.indices) return this
    return copy(
        expandedDayIndex = if (expandedDayIndex == dayIndex) null else dayIndex
    )
}

data class RoutineDayEditorUiState(
    val name: String = "Dia 1",
    val exercises: List<RoutineExerciseEditorUiState> = listOf(RoutineExerciseEditorUiState())
) {
    val nameError: String?
        get() = if (name.isBlank()) "Pon un nombre para el dia." else null
}

data class RoutineExerciseEditorUiState(
    val routineExerciseId: Long? = null,
    val variantKey: String? = null,
    val defaultVariantKey: String? = null,
    val name: String = "",
    val targetSets: String = "3",
    val targetRepsText: String = "8-12",
    val notes: String = "",
    val alternatives: List<RoutineExerciseAlternativeEditorUiState> = emptyList()
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

data class RoutineExerciseAlternativeEditorUiState(
    val alternativeId: Long? = null,
    val variantKey: String? = null,
    val name: String = "",
    val targetSets: String = "3",
    val targetRepsText: String = "8-12",
    val notes: String = ""
) {
    val nameError: String?
        get() = if (name.isBlank()) "Pon un nombre para la alternativa." else null
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

internal fun normalizeEditorNameInput(value: String): String {
    if (value.isEmpty()) return value
    return value.replaceFirstChar { char ->
        if (char.isLowerCase()) {
            char.titlecase()
        } else {
            char.toString()
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
                        routineExerciseId = exercise.id,
                        variantKey = exercise.variantKey,
                        defaultVariantKey = exercise.defaultVariantKey,
                        name = exercise.name,
                        targetSets = exercise.targetSets.toString(),
                        targetRepsText = exercise.targetRepsText,
                        notes = exercise.notes.orEmpty(),
                        alternatives = exercise.alternatives.map { alternative ->
                            RoutineExerciseAlternativeEditorUiState(
                                alternativeId = alternative.id,
                                variantKey = alternative.variantKey,
                                name = alternative.name,
                                targetSets = alternative.targetSets.toString(),
                                targetRepsText = alternative.targetRepsText,
                                notes = alternative.notes.orEmpty()
                            )
                        }
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
                        variantKey = exercise.variantKey,
                        name = exercise.name.trim(),
                        targetSets = exercise.targetSets.toInt(),
                        targetRepsText = exercise.targetRepsText.trim(),
                        notes = exercise.notes.trim().ifBlank { null },
                        defaultVariantKey = exercise.defaultVariantKey ?: exercise.variantKey,
                        alternatives = exercise.alternatives.map { alternative ->
                            RoutineExerciseAlternativeDraft(
                                variantKey = alternative.variantKey,
                                name = alternative.name.trim(),
                                targetSets = alternative.targetSets.toInt(),
                                targetRepsText = alternative.targetRepsText.trim(),
                                notes = alternative.notes.trim().ifBlank { null }
                            )
                        }
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

private fun Int?.afterRemovingDay(removedIndex: Int): Int? {
    return when {
        this == null -> null
        this == removedIndex -> null
        this > removedIndex -> this - 1
        else -> this
    }
}

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
            userPreferencesRepository.activeRoutineId
        ) { routines, activeRoutineId ->
            val activeId = activeRoutineId?.takeIf { id -> routines.any { it.id == id } }
            routines.map { routine ->
                RoutineListItemUiState(
                    id = routine.id,
                    name = routine.name,
                    dayCount = routine.dayCount,
                    isActive = routine.id == activeId
                )
            } to activeId
        }
            .onEach { (routines, activeRoutineId) ->
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        routines = routines,
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
    }

    fun startCreateRoutine() {
        _uiState.update { state ->
            state.copy(editor = RoutineEditorUiState())
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

    fun closeEditor() {
        _uiState.update { state -> state.copy(editor = null) }
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
        updateExercise(dayIndex, exerciseIndex) { exercise -> exercise.copy(name = name) }
    }

    fun updateExerciseSets(dayIndex: Int, exerciseIndex: Int, targetSets: String) {
        updateExercise(dayIndex, exerciseIndex) { exercise -> exercise.copy(targetSets = targetSets) }
    }

    fun updateExerciseReps(dayIndex: Int, exerciseIndex: Int, targetRepsText: String) {
        updateExercise(dayIndex, exerciseIndex) { exercise -> exercise.copy(targetRepsText = targetRepsText) }
    }

    fun updateExerciseNotes(dayIndex: Int, exerciseIndex: Int, notes: String) {
        updateExercise(dayIndex, exerciseIndex) { exercise -> exercise.copy(notes = notes) }
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

    fun clearMessage() {
        _uiState.update { state -> state.copy(message = null) }
    }

    private fun updateExercise(
        dayIndex: Int,
        exerciseIndex: Int,
        transform: (RoutineExerciseEditorUiState) -> RoutineExerciseEditorUiState
    ) {
        updateEditor { editor ->
            editor.copy(
                days = editor.days.replaceAt(dayIndex) { day ->
                    day.copy(exercises = day.exercises.replaceAt(exerciseIndex, transform))
                }
            )
        }
    }

    private fun updateEditor(transform: (RoutineEditorUiState) -> RoutineEditorUiState) {
        _uiState.update { state ->
            val editor = state.editor ?: return@update state
            state.copy(editor = transform(editor))
        }
    }
}

data class RoutinesUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val routines: List<RoutineListItemUiState> = emptyList(),
    val activeRoutineId: Long? = null,
    val editor: RoutineEditorUiState? = null,
    val message: String? = null
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
    val days: List<RoutineDayEditorUiState> = listOf(RoutineDayEditorUiState())
) {
    val title: String = if (routineId == null) "Nueva rutina" else "Editar rutina"
    val canSave: Boolean
        get() = name.isNotBlank() &&
            days.isNotEmpty() &&
            days.all { day ->
                day.exercises.isNotEmpty() &&
                    day.exercises.all { exercise ->
                        exercise.name.isNotBlank() &&
                            exercise.targetSets.toIntOrNull()?.let { it in 1..99 } == true &&
                            exercise.targetRepsText.isNotBlank()
                    }
            }
}

data class RoutineDayEditorUiState(
    val name: String = "Dia 1",
    val exercises: List<RoutineExerciseEditorUiState> = listOf(RoutineExerciseEditorUiState())
)

data class RoutineExerciseEditorUiState(
    val name: String = "",
    val targetSets: String = "3",
    val targetRepsText: String = "8-12",
    val notes: String = ""
)

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

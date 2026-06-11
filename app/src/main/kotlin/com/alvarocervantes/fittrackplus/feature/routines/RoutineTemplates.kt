package com.alvarocervantes.fittrackplus.feature.routines

data class RoutineTemplateUiState(
    val id: String,
    val name: String,
    val description: String,
    val days: List<RoutineTemplateDayUiState>
)

data class RoutineTemplateDayUiState(
    val name: String,
    val exercises: List<RoutineTemplateExerciseUiState>
)

data class RoutineTemplateExerciseUiState(
    val name: String,
    val targetSets: String,
    val targetRepsText: String,
    val notes: String = ""
)

enum class MoveDirection {
    Up,
    Down
}

sealed interface RoutineEditorOperation {
    data class DuplicateDay(val dayIndex: Int) : RoutineEditorOperation
    data class MoveDay(val dayIndex: Int, val direction: MoveDirection) : RoutineEditorOperation
    data class DuplicateExercise(val dayIndex: Int, val exerciseIndex: Int) : RoutineEditorOperation
    data class MoveExercise(
        val dayIndex: Int,
        val exerciseIndex: Int,
        val direction: MoveDirection
    ) : RoutineEditorOperation
}

val routineTemplates: List<RoutineTemplateUiState> = listOf(
    RoutineTemplateUiState(
        id = "ppl",
        name = "Push Pull Legs",
        description = "Tres dias clasicos para fuerza e hipertrofia.",
        days = listOf(
            RoutineTemplateDayUiState(
                name = "Push",
                exercises = listOf(
                    RoutineTemplateExerciseUiState("Press banca", "3", "6-8"),
                    RoutineTemplateExerciseUiState("Press militar", "3", "8-12"),
                    RoutineTemplateExerciseUiState("Press inclinado mancuernas", "3", "8-12"),
                    RoutineTemplateExerciseUiState("Elevaciones laterales", "3", "10-15")
                )
            ),
            RoutineTemplateDayUiState(
                name = "Pull",
                exercises = listOf(
                    RoutineTemplateExerciseUiState("Dominadas", "3", "AMRAP"),
                    RoutineTemplateExerciseUiState("Remo con barra", "3", "6-8"),
                    RoutineTemplateExerciseUiState("Jalon al pecho", "3", "8-12"),
                    RoutineTemplateExerciseUiState("Curl biceps", "3", "10-15")
                )
            ),
            RoutineTemplateDayUiState(
                name = "Legs",
                exercises = listOf(
                    RoutineTemplateExerciseUiState("Sentadilla", "3", "6-8"),
                    RoutineTemplateExerciseUiState("Peso muerto rumano", "3", "8-12"),
                    RoutineTemplateExerciseUiState("Prensa", "3", "8-12"),
                    RoutineTemplateExerciseUiState("Gemelos", "3", "10-15")
                )
            )
        )
    ),
    RoutineTemplateUiState(
        id = "upper-lower",
        name = "Upper Lower",
        description = "Cuatro dias equilibrados para tren superior e inferior.",
        days = listOf(
            RoutineTemplateDayUiState(
                name = "Upper A",
                exercises = listOf(
                    RoutineTemplateExerciseUiState("Press banca", "3", "6-8"),
                    RoutineTemplateExerciseUiState("Remo con barra", "3", "8-12"),
                    RoutineTemplateExerciseUiState("Press militar", "3", "8-12")
                )
            ),
            RoutineTemplateDayUiState(
                name = "Lower A",
                exercises = listOf(
                    RoutineTemplateExerciseUiState("Sentadilla", "3", "6-8"),
                    RoutineTemplateExerciseUiState("Peso muerto rumano", "3", "8-12"),
                    RoutineTemplateExerciseUiState("Zancadas", "3", "10-15")
                )
            ),
            RoutineTemplateDayUiState(
                name = "Upper B",
                exercises = listOf(
                    RoutineTemplateExerciseUiState("Press inclinado", "3", "8-12"),
                    RoutineTemplateExerciseUiState("Dominadas", "3", "AMRAP"),
                    RoutineTemplateExerciseUiState("Face pull", "3", "10-15")
                )
            ),
            RoutineTemplateDayUiState(
                name = "Lower B",
                exercises = listOf(
                    RoutineTemplateExerciseUiState("Peso muerto", "3", "5"),
                    RoutineTemplateExerciseUiState("Prensa", "3", "8-12"),
                    RoutineTemplateExerciseUiState("Curl femoral", "3", "10-15")
                )
            )
        )
    ),
    RoutineTemplateUiState(
        id = "full-body",
        name = "Full Body",
        description = "Tres sesiones completas para empezar con poco roce.",
        days = listOf(
            RoutineTemplateDayUiState(
                name = "Full Body A",
                exercises = listOf(
                    RoutineTemplateExerciseUiState("Sentadilla", "3", "6-8"),
                    RoutineTemplateExerciseUiState("Press banca", "3", "8-12"),
                    RoutineTemplateExerciseUiState("Remo con barra", "3", "8-12")
                )
            ),
            RoutineTemplateDayUiState(
                name = "Full Body B",
                exercises = listOf(
                    RoutineTemplateExerciseUiState("Peso muerto rumano", "3", "8-12"),
                    RoutineTemplateExerciseUiState("Press militar", "3", "8-12"),
                    RoutineTemplateExerciseUiState("Jalon al pecho", "3", "8-12")
                )
            ),
            RoutineTemplateDayUiState(
                name = "Full Body C",
                exercises = listOf(
                    RoutineTemplateExerciseUiState("Prensa", "3", "10-15"),
                    RoutineTemplateExerciseUiState("Press inclinado", "3", "8-12"),
                    RoutineTemplateExerciseUiState("Dominadas", "3", "AMRAP")
                )
            )
        )
    )
)

fun RoutineTemplateUiState.toEditorState(): RoutineEditorUiState {
    return RoutineEditorUiState(
        name = name,
        days = days.map { day ->
            RoutineDayEditorUiState(
                name = day.name,
                exercises = day.exercises.map { exercise ->
                    RoutineExerciseEditorUiState(
                        name = exercise.name,
                        targetSets = exercise.targetSets,
                        targetRepsText = exercise.targetRepsText,
                        notes = exercise.notes
                    )
                }
            )
        }
    )
}

fun RoutineEditorUiState.duplicateDay(dayIndex: Int): RoutineEditorUiState {
    if (dayIndex !in days.indices) return this
    val source = days[dayIndex]
    val duplicate = source.copy(name = "${source.name} copia")
    return copy(
        days = days.take(dayIndex + 1) + duplicate + days.drop(dayIndex + 1),
        expandedDayIndex = dayIndex + 1,
        isDirty = true
    )
}

fun RoutineEditorUiState.moveDay(dayIndex: Int, direction: MoveDirection): RoutineEditorUiState {
    if (dayIndex !in days.indices) return this
    val targetIndex = dayIndex.targetIndex(direction, days.lastIndex)
    if (targetIndex == dayIndex) return this
    return copy(
        days = days.swap(dayIndex, targetIndex),
        expandedDayIndex = expandedDayIndex.afterMovingDay(dayIndex, targetIndex),
        isDirty = true
    )
}

fun RoutineEditorUiState.duplicateExercise(dayIndex: Int, exerciseIndex: Int): RoutineEditorUiState {
    if (dayIndex !in days.indices) return this
    val day = days[dayIndex]
    if (exerciseIndex !in day.exercises.indices) return this
    val source = day.exercises[exerciseIndex]
    val duplicate = source.copy(name = "${source.name} copia")
    return copy(
        days = days.replaceAt(dayIndex) {
            it.copy(
                exercises = day.exercises.take(exerciseIndex + 1) +
                    duplicate +
                    day.exercises.drop(exerciseIndex + 1)
            )
        },
        isDirty = true
    )
}

fun RoutineEditorUiState.moveExercise(
    dayIndex: Int,
    exerciseIndex: Int,
    direction: MoveDirection
): RoutineEditorUiState {
    if (dayIndex !in days.indices) return this
    val day = days[dayIndex]
    if (exerciseIndex !in day.exercises.indices) return this
    val targetIndex = exerciseIndex.targetIndex(direction, day.exercises.lastIndex)
    if (targetIndex == exerciseIndex) return this
    return copy(
        days = days.replaceAt(dayIndex) {
            it.copy(exercises = day.exercises.swap(exerciseIndex, targetIndex))
        },
        isDirty = true
    )
}

private fun Int.targetIndex(direction: MoveDirection, lastIndex: Int): Int {
    return when (direction) {
        MoveDirection.Up -> (this - 1).coerceAtLeast(0)
        MoveDirection.Down -> (this + 1).coerceAtMost(lastIndex)
    }
}

private fun <T> List<T>.replaceAt(index: Int, transform: (T) -> T): List<T> {
    if (index !in indices) return this
    return mapIndexed { currentIndex, item ->
        if (currentIndex == index) transform(item) else item
    }
}

private fun <T> List<T>.swap(fromIndex: Int, toIndex: Int): List<T> {
    if (fromIndex !in indices || toIndex !in indices) return this
    return toMutableList().also { items ->
        val moved = items[fromIndex]
        items[fromIndex] = items[toIndex]
        items[toIndex] = moved
    }
}

private fun Int?.afterMovingDay(fromIndex: Int, toIndex: Int): Int? {
    return when (this) {
        null -> null
        fromIndex -> toIndex
        toIndex -> fromIndex
        else -> this
    }
}

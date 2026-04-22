package com.alvarocervantes.fittrackplus.domain.model

data class RoutineSummary(
    val id: Long,
    val name: String,
    val dayCount: Int,
    val updatedAt: Long
)

data class RoutineDraft(
    val name: String,
    val days: List<RoutineDayDraft>
)

data class RoutineDayDraft(
    val name: String,
    val exercises: List<RoutineExerciseDraft>
)

data class RoutineExerciseDraft(
    val name: String,
    val targetSets: Int,
    val targetRepsText: String,
    val notes: String? = null
)

data class RoutineSnapshot(
    val id: Long,
    val name: String,
    val days: List<RoutineDaySnapshot>
)

data class RoutineDaySnapshot(
    val id: Long,
    val name: String,
    val position: Int,
    val exercises: List<RoutineExerciseSnapshot>
)

data class RoutineExerciseSnapshot(
    val id: Long,
    val name: String,
    val targetSets: Int,
    val targetRepsText: String,
    val position: Int,
    val notes: String?
)

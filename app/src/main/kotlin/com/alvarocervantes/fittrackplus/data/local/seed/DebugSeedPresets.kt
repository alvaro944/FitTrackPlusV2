package com.alvarocervantes.fittrackplus.data.local.seed

internal const val DEFAULT_BASE_TARGET_REPS = "8-12"

internal data class DebugSeedCatalog(
    val activeRoutineName: String,
    val routines: List<DebugSeedRoutineDefinition>
)

internal data class DebugSeedRoutineDefinition(
    val name: String,
    val createdDaysAgo: Long,
    val updatedDaysAgo: Long,
    val days: List<DebugSeedDayDefinition>,
    val finishedSessions: List<DebugSeedSessionDefinition> = emptyList()
)

internal data class DebugSeedDayDefinition(
    val name: String,
    val exercises: List<DebugSeedExerciseDefinition>
)

internal data class DebugSeedExerciseDefinition(
    val name: String,
    val targetSets: Int,
    val targetReps: String = DEFAULT_BASE_TARGET_REPS
)

internal data class DebugSeedSessionDefinition(
    val dayIndex: Int,
    val weekNumber: Int,
    val startedDaysAgo: Long,
    val minutes: Int,
    val setValues: List<List<Pair<Double, Int>>>,
    val snapshotDayName: String? = null
)

internal fun buildDebugSeedCatalog(): DebugSeedCatalog {
    return DebugSeedCatalog(
        activeRoutineName = "Rutina Álvaro",
        routines = listOf(
            buildAlvaroBaseRoutineSeed(),
            buildPplDemoRoutineSeed()
        )
    )
}

internal fun buildAlvaroBaseRoutineSeed(): DebugSeedRoutineDefinition {
    return DebugSeedRoutineDefinition(
        name = "Rutina Álvaro",
        createdDaysAgo = 0L,
        updatedDaysAgo = 0L,
        days = listOf(
            DebugSeedDayDefinition(
                name = "Entreno 1 — Empujes",
                exercises = listOf(
                    DebugSeedExerciseDefinition("Press plano", 2),
                    DebugSeedExerciseDefinition("Press militar", 2),
                    DebugSeedExerciseDefinition("Extensión unilateral tríceps", 3),
                    DebugSeedExerciseDefinition("Press inclinado", 2),
                    DebugSeedExerciseDefinition("Elevación lateral", 3),
                    DebugSeedExerciseDefinition("Extensión de codo a 90° de flex. de hombro", 2),
                    DebugSeedExerciseDefinition("Aperturas", 1)
                )
            ),
            DebugSeedDayDefinition(
                name = "Entreno 2 — Pierna Cuádriceps",
                exercises = listOf(
                    DebugSeedExerciseDefinition("Prensa profunda", 2),
                    DebugSeedExerciseDefinition("Abductor (glúteo)", 2),
                    DebugSeedExerciseDefinition("Remo agarre ancho sentado con barra en polea", 3),
                    DebugSeedExerciseDefinition("Crunch abdominal máquina", 2),
                    DebugSeedExerciseDefinition("Extensión de cuádriceps", 2),
                    DebugSeedExerciseDefinition("Curl femoral sentado", 2),
                    DebugSeedExerciseDefinition("Hombro posterior cable", 2),
                    DebugSeedExerciseDefinition("Bíceps en cable", 2),
                    DebugSeedExerciseDefinition("Gemelos", 3)
                )
            ),
            DebugSeedDayDefinition(
                name = "Entreno 3 — Espalda",
                exercises = listOf(
                    DebugSeedExerciseDefinition("Remo alto unilateral plano sagital", 2),
                    DebugSeedExerciseDefinition("Remo en punta máquina agarre amplio", 2),
                    DebugSeedExerciseDefinition("Curl de bíceps con barra", 2),
                    DebugSeedExerciseDefinition("Remo Gironda manos plano sagital", 3),
                    DebugSeedExerciseDefinition("Pull-over en máquina", 2),
                    DebugSeedExerciseDefinition("Curl de bíceps unilateral", 2),
                    DebugSeedExerciseDefinition("Posterior en polea", 1),
                    DebugSeedExerciseDefinition("Martillo", 1)
                )
            ),
            DebugSeedDayDefinition(
                name = "Entreno 4 — Pierna Femoral",
                exercises = listOf(
                    DebugSeedExerciseDefinition("Peso muerto rumano", 2),
                    DebugSeedExerciseDefinition("Extensión de cuádriceps", 2),
                    DebugSeedExerciseDefinition("Press con mancuernas", 2),
                    DebugSeedExerciseDefinition("Hip thrust", 2),
                    DebugSeedExerciseDefinition("Aductor", 2),
                    DebugSeedExerciseDefinition("Curl femoral sentado", 2),
                    DebugSeedExerciseDefinition("Crunch abdominal", 2),
                    DebugSeedExerciseDefinition("Elevaciones laterales en cable", 2),
                    DebugSeedExerciseDefinition("Tríceps en polea", 2)
                )
            )
        )
    )
}

private fun buildPplDemoRoutineSeed(): DebugSeedRoutineDefinition {
    return DebugSeedRoutineDefinition(
        name = "PPL Demo",
        createdDaysAgo = 12L,
        updatedDaysAgo = 2L,
        days = buildPplDemoDays(),
        finishedSessions = buildPplDemoSessions()
    )
}

private fun buildPplDemoDays(): List<DebugSeedDayDefinition> {
    return listOf(
        DebugSeedDayDefinition(
            name = "Push",
            exercises = listOf(
                DebugSeedExerciseDefinition("Press banca", 3, "8-10"),
                DebugSeedExerciseDefinition("Press militar", 3, "8-10"),
                DebugSeedExerciseDefinition("Fondos asistidos", 2, "10-12")
            )
        ),
        DebugSeedDayDefinition(
            name = "Pull",
            exercises = listOf(
                DebugSeedExerciseDefinition("Dominadas asistidas", 3, "6-8"),
                DebugSeedExerciseDefinition("Remo con barra", 3, "8-10"),
                DebugSeedExerciseDefinition("Curl biceps", 2, "10-12")
            )
        ),
        DebugSeedDayDefinition(
            name = "Legs",
            exercises = listOf(
                DebugSeedExerciseDefinition("Sentadilla", 3, "6-8"),
                DebugSeedExerciseDefinition("Peso muerto rumano", 3, "8-10"),
                DebugSeedExerciseDefinition("Prensa", 2, "10-12")
            )
        )
    )
}

private fun buildPplDemoSessions(): List<DebugSeedSessionDefinition> {
    return listOf(
        DebugSeedSessionDefinition(
            dayIndex = 0,
            weekNumber = 1,
            startedDaysAgo = 9L,
            minutes = 58,
            setValues = listOf(
                listOf(60.0 to 10, 62.5 to 9, 62.5 to 8),
                listOf(35.0 to 10, 37.5 to 9, 37.5 to 8),
                listOf(0.0 to 12, 0.0 to 11)
            )
        ),
        DebugSeedSessionDefinition(
            dayIndex = 1,
            weekNumber = 1,
            startedDaysAgo = 7L,
            minutes = 52,
            setValues = listOf(
                listOf(0.0 to 8, 0.0 to 7, 0.0 to 7),
                listOf(50.0 to 10, 52.5 to 9, 52.5 to 8),
                listOf(12.5 to 12, 12.5 to 11)
            )
        ),
        DebugSeedSessionDefinition(
            dayIndex = 2,
            weekNumber = 1,
            startedDaysAgo = 5L,
            minutes = 64,
            setValues = listOf(
                listOf(80.0 to 8, 82.5 to 7, 82.5 to 6),
                listOf(65.0 to 10, 67.5 to 9, 67.5 to 8),
                listOf(140.0 to 12, 145.0 to 10)
            )
        ),
        DebugSeedSessionDefinition(
            dayIndex = 0,
            weekNumber = 2,
            startedDaysAgo = 2L,
            minutes = 56,
            snapshotDayName = "Push snapshot antiguo",
            setValues = listOf(
                listOf(62.5 to 10, 65.0 to 9, 65.0 to 8),
                listOf(37.5 to 10, 40.0 to 8, 40.0 to 8),
                listOf(0.0 to 12, 0.0 to 12)
            )
        )
    )
}

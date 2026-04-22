package com.alvarocervantes.fittrackplus.feature.workout

import androidx.compose.runtime.Composable
import com.alvarocervantes.fittrackplus.core.design.PlaceholderScaffold

@Composable
fun WorkoutScreen() {
    PlaceholderScaffold(
        title = "Entrenar",
        summary = "Fase 2.",
        items = listOf(
            "Siguiente dia de rutina",
            "Registro por serie",
            "Snapshot historico al finalizar"
        )
    )
}

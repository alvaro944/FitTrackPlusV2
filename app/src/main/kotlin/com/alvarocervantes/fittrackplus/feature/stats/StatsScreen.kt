package com.alvarocervantes.fittrackplus.feature.stats

import androidx.compose.runtime.Composable
import com.alvarocervantes.fittrackplus.core.design.PlaceholderScaffold

@Composable
fun StatsScreen() {
    PlaceholderScaffold(
        title = "Datos",
        summary = "Fase 4.",
        items = listOf(
            "Progreso por ejercicio",
            "Volumen por sesion",
            "Mejores marcas simples"
        )
    )
}

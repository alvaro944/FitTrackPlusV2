package com.alvarocervantes.fittrackplus.feature.history

import androidx.compose.runtime.Composable
import com.alvarocervantes.fittrackplus.core.design.PlaceholderScaffold

@Composable
fun HistoryScreen() {
    PlaceholderScaffold(
        title = "Historial",
        summary = "Fase 3.",
        items = listOf(
            "Sesiones por fecha",
            "Semanas y dias",
            "Detalle de ejercicios y series"
        )
    )
}

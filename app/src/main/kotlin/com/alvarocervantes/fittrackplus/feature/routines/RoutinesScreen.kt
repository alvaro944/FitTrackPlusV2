package com.alvarocervantes.fittrackplus.feature.routines

import androidx.compose.runtime.Composable
import com.alvarocervantes.fittrackplus.core.design.PlaceholderScaffold

@Composable
fun RoutinesScreen() {
    PlaceholderScaffold(
        title = "Rutinas",
        summary = "Fase 1.",
        items = listOf(
            "Crear rutina",
            "Editar dias y ejercicios",
            "Seleccionar rutina activa"
        )
    )
}

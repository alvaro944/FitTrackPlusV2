package com.alvarocervantes.fittrackplus.feature.home

import androidx.compose.runtime.Composable
import com.alvarocervantes.fittrackplus.core.design.PlaceholderScaffold

@Composable
fun HomeScreen() {
    PlaceholderScaffold(
        title = "FitTrackPlus",
        summary = "Gestiona rutinas, registra entrenamientos y consulta tu historial local.",
        items = listOf(
            "1. Crea una rutina en Rutinas y marcala como activa.",
            "2. Entra en Entrenar para iniciar el siguiente dia preparado.",
            "3. Finaliza sesiones para verlas en Historial y Datos.",
            "Los datos del MVP siguen siendo locales. Sync queda para una fase futura."
        )
    )
}

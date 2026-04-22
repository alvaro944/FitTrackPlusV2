package com.alvarocervantes.fittrackplus.feature.home

import androidx.compose.runtime.Composable
import com.alvarocervantes.fittrackplus.core.design.PlaceholderScaffold

@Composable
fun HomeScreen() {
    PlaceholderScaffold(
        title = "FitTrackPlus",
        summary = "Base v2 lista para crecer por fases.",
        items = listOf(
            "Arquitectura Compose local-first",
            "Room preparado para rutinas e historico",
            "DataStore preparado para preferencias",
            "Firebase reservado para una fase futura"
        )
    )
}

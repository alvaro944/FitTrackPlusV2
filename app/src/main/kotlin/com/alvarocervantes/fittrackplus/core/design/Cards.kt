package com.alvarocervantes.fittrackplus.core.design

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun FitTrackCard(
    modifier: Modifier = Modifier,
    highlighted: Boolean = false,
    containerColor: Color = if (highlighted) MaterialTheme.colorScheme.surfaceCard else MaterialTheme.colorScheme.surface,
    borderColor: Color = MaterialTheme.colorScheme.borderLight,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier.padding(FitSpacing.card),
            verticalArrangement = Arrangement.spacedBy(FitSpacing.md),
            content = content
        )
    }
}

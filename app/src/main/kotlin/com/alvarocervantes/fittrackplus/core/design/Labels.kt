package com.alvarocervantes.fittrackplus.core.design

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun FitTrackSectionLabel(
    label: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.textTertiary
        )
        if (actionLabel != null && onAction != null) {
            TextButton(onClick = onAction) {
                Text(
                    text = actionLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun FitTrackBadge(
    label: String,
    tone: FitTrackBadgeTone,
    modifier: Modifier = Modifier
) {
    val background = when (tone) {
        FitTrackBadgeTone.Primary -> MaterialTheme.colorScheme.primarySoft
        FitTrackBadgeTone.Warm -> MaterialTheme.colorScheme.accentSoft
        FitTrackBadgeTone.Neutral -> MaterialTheme.colorScheme.surfaceAlt
        FitTrackBadgeTone.Error -> MaterialTheme.colorScheme.errorSoft
        FitTrackBadgeTone.Active -> MaterialTheme.colorScheme.primary
    }
    val foreground = when (tone) {
        FitTrackBadgeTone.Primary -> MaterialTheme.colorScheme.primary
        FitTrackBadgeTone.Warm -> MaterialTheme.colorScheme.accentWarm
        FitTrackBadgeTone.Neutral -> MaterialTheme.colorScheme.onSurfaceVariant
        FitTrackBadgeTone.Error -> MaterialTheme.colorScheme.error
        FitTrackBadgeTone.Active -> MaterialTheme.colorScheme.onPrimary
    }

    Box(
        modifier = modifier
            .background(background, CircleShape)
            .padding(horizontal = FitSpacing.smMd, vertical = FitSpacing.xs + 1.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = foreground
        )
    }
}

@Composable
fun FitTrackScreenHeader(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    trailing: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(FitSpacing.xs)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (trailing != null) {
            Box(
                modifier = Modifier.padding(start = FitSpacing.md),
                contentAlignment = Alignment.TopEnd
            ) {
                trailing()
            }
        }
    }
}

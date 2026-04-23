package com.alvarocervantes.fittrackplus.core.design

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class FitTrackMetricAccent {
    Neutral,
    Primary,
    Warm,
    Error
}

enum class FitTrackBadgeTone {
    Primary,
    Warm,
    Neutral,
    Error,
    Active
}

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
fun FitTrackMetric(
    value: String,
    label: String,
    unit: String? = null,
    accent: FitTrackMetricAccent = FitTrackMetricAccent.Neutral,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    val accentColor = when (accent) {
        FitTrackMetricAccent.Primary -> MaterialTheme.colorScheme.primary
        FitTrackMetricAccent.Warm -> MaterialTheme.colorScheme.accentWarm
        FitTrackMetricAccent.Error -> MaterialTheme.colorScheme.error
        FitTrackMetricAccent.Neutral -> MaterialTheme.colorScheme.onSurface
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = value,
                color = accentColor,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = if (compact) 24.sp else 30.sp,
                lineHeight = if (compact) 26.sp else 32.sp
            )
            if (unit != null) {
                Text(
                    text = unit,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 3.dp)
                )
            }
        }
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.textTertiary
        )
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
            .padding(horizontal = 10.dp, vertical = FitSpacing.xs + 1.dp)
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
            verticalArrangement = Arrangement.spacedBy(4.dp)
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
                modifier = Modifier.padding(start = 12.dp),
                contentAlignment = Alignment.TopEnd
            ) {
                trailing()
            }
        }
    }
}

@Composable
fun FitTrackEmptyState(
    icon: ImageVector,
    title: String,
    message: String,
    supporting: String? = null,
    modifier: Modifier = Modifier,
    action: @Composable (() -> Unit)? = null
) {
    FitTrackCard(modifier = modifier) {
        Column(
            verticalArrangement = Arrangement.spacedBy(FitSpacing.md),
            horizontalAlignment = Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(MaterialTheme.colorScheme.primarySoft, MaterialTheme.shapes.large)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        shape = MaterialTheme.shapes.large
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (supporting != null) {
                Text(
                    text = supporting,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.textTertiary
                )
            }
            if (action != null) {
                action()
            }
        }
    }
}

@Composable
fun FitTrackLoadingCard(text: String, modifier: Modifier = Modifier) {
    FitTrackCard(modifier = modifier) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(FitSpacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun FitTrackProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(6.dp)
            .background(MaterialTheme.colorScheme.surfaceAlt, CircleShape)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .height(6.dp)
                .background(color, CircleShape)
        )
    }
}

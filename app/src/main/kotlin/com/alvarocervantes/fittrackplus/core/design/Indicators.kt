package com.alvarocervantes.fittrackplus.core.design

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
        verticalArrangement = Arrangement.spacedBy(FitSpacing.xs)
    ) {
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(FitSpacing.xs)
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
fun FitTrackProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    contentDescription: String = ""
) {
    val normalizedProgress = progress.coerceIn(0f, 1f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(6.dp)
            .semantics {
                if (contentDescription.isNotEmpty()) {
                    this.contentDescription = contentDescription
                }
                progressBarRangeInfo = ProgressBarRangeInfo(
                    current = normalizedProgress,
                    range = 0f..1f
                )
            }
            .background(MaterialTheme.colorScheme.surfaceAlt, CircleShape)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(normalizedProgress)
                .height(6.dp)
                .background(color, CircleShape)
        )
    }
}

package com.alvarocervantes.fittrackplus.core.design

import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
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

@Composable
fun FitTrackRadialTimer(
    remainingSeconds: Int,
    durationSeconds: Int,
    label: String,
    modifier: Modifier = Modifier,
    isUrgent: Boolean = false,
    contentDescription: String = ""
) {
    val normalizedProgress = if (durationSeconds <= 0) {
        0f
    } else {
        1f - (remainingSeconds.toFloat() / durationSeconds.toFloat()).coerceIn(0f, 1f)
    }
    val trackColor = MaterialTheme.colorScheme.surfaceAlt
    val progressColor = if (isUrgent) {
        MaterialTheme.colorScheme.accentWarm
    } else {
        MaterialTheme.colorScheme.primary
    }

    Box(
        modifier = modifier
            .size(132.dp)
            .semantics {
                if (contentDescription.isNotEmpty()) {
                    this.contentDescription = contentDescription
                }
                progressBarRangeInfo = ProgressBarRangeInfo(
                    current = normalizedProgress,
                    range = 0f..1f
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 11.dp.toPx()
            drawCircle(
                color = trackColor,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            drawArc(
                color = progressColor,
                startAngle = -90f,
                sweepAngle = normalizedProgress * 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(FitSpacing.xs)
        ) {
            Text(
                text = if (durationSeconds > 0) formatTimerLabel(remainingSeconds) else "--",
                color = progressColor,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                lineHeight = 30.sp
            )
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun formatTimerLabel(totalSeconds: Int): String {
    val safeSeconds = totalSeconds.coerceAtLeast(0)
    val minutes = safeSeconds / 60
    val seconds = safeSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}

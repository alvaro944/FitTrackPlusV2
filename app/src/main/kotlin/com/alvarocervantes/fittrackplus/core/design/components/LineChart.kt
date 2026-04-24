package com.alvarocervantes.fittrackplus.core.design.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp

@Composable
fun LineChart(
    points: List<Pair<Long, Float>>,
    modifier: Modifier = Modifier
) {
    if (points.size < 2) {
        Box(
            modifier = modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Se necesitan al menos 2 sesiones",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    val lineColor = MaterialTheme.colorScheme.primary
    val dotOuterColor = MaterialTheme.colorScheme.primary
    val dotInnerColor = MaterialTheme.colorScheme.surface

    val sortedPoints = points.sortedBy { it.first }
    val minY = sortedPoints.minOf { it.second }
    val maxY = sortedPoints.maxOf { it.second }
    val yRange = if (maxY == minY) 1f else maxY - minY

    Canvas(modifier = modifier) {
        val padH = 8.dp.toPx()
        val padV = 12.dp.toPx()
        val chartW = size.width - padH * 2
        val chartH = size.height - padV * 2

        val offsets = sortedPoints.mapIndexed { index, (_, value) ->
            Offset(
                x = padH + (index.toFloat() / (sortedPoints.size - 1)) * chartW,
                y = padV + chartH - ((value - minY) / yRange) * chartH
            )
        }

        // Connecting line
        for (i in 0 until offsets.size - 1) {
            drawLine(
                color = lineColor,
                start = offsets[i],
                end = offsets[i + 1],
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )
        }

        // Dots: outer filled circle + inner surface-color circle
        offsets.forEach { offset ->
            drawCircle(color = dotOuterColor, radius = 4.dp.toPx(), center = offset)
            drawCircle(color = dotInnerColor, radius = 2.dp.toPx(), center = offset)
        }
    }
}

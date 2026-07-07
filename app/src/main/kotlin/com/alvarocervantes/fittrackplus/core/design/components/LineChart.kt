package com.alvarocervantes.fittrackplus.core.design.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import android.graphics.Paint
import kotlin.math.hypot

@Composable
fun LineChart(
    points: List<Pair<Long, Float>>,
    modifier: Modifier = Modifier,
    selectedPointIndex: Int? = null,
    onPointSelected: ((Int) -> Unit)? = null,
    pointLabels: List<String> = emptyList(),
    xAxisLabels: List<String> = emptyList()
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
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant

    val sortedPoints = points.sortedBy { it.first }

    Canvas(
        modifier = modifier.pointerInput(sortedPoints, onPointSelected) {
            if (onPointSelected == null) return@pointerInput
            detectTapGestures { tapOffset ->
                val offsets = sortedPoints.chartOffsets(
                    width = size.width.toFloat(),
                    height = size.height.toFloat(),
                    padH = 18.dp.toPx(),
                    padTop = 22.dp.toPx(),
                    padBottom = 26.dp.toPx()
                )
                val hitIndex = offsets
                    .mapIndexed { index, offset -> index to offset.distanceTo(tapOffset) }
                    .minByOrNull { (_, distance) -> distance }
                    ?.takeIf { (_, distance) -> distance <= 24.dp.toPx() }
                    ?.first
                if (hitIndex != null) {
                    onPointSelected(hitIndex)
                }
            }
        }
    ) {
        val padH = 18.dp.toPx()
        val padTop = 22.dp.toPx()
        val padBottom = 26.dp.toPx()
        val offsets = sortedPoints.chartOffsets(
            width = size.width,
            height = size.height,
            padH = padH,
            padTop = padTop,
            padBottom = padBottom
        )
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = labelColor.toArgb()
            textSize = 10.dp.toPx()
            textAlign = Paint.Align.CENTER
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
        offsets.forEachIndexed { index, offset ->
            val isSelected = selectedPointIndex == index
            drawCircle(
                color = dotOuterColor,
                radius = if (isSelected) 6.dp.toPx() else 4.dp.toPx(),
                center = offset
            )
            drawCircle(color = dotInnerColor, radius = 2.dp.toPx(), center = offset)
            pointLabels.getOrNull(index)?.let { label ->
                drawContext.canvas.nativeCanvas.drawText(
                    label,
                    offset.x,
                    (offset.y - 8.dp.toPx()).coerceAtLeast(10.dp.toPx()),
                    textPaint
                )
            }
            xAxisLabels.getOrNull(index)?.let { label ->
                drawContext.canvas.nativeCanvas.drawText(
                    label,
                    offset.x,
                    size.height - 4.dp.toPx(),
                    textPaint
                )
            }
        }
    }
}

private fun List<Pair<Long, Float>>.chartOffsets(
    width: Float,
    height: Float,
    padH: Float,
    padTop: Float,
    padBottom: Float
): List<Offset> {
    val minY = minOf { it.second }
    val maxY = maxOf { it.second }
    val yRange = if (maxY == minY) 1f else maxY - minY
    val chartW = width - padH * 2
    val chartH = height - padTop - padBottom
    return mapIndexed { index, (_, value) ->
        Offset(
            x = padH + (index.toFloat() / (size - 1)) * chartW,
            y = padTop + chartH - ((value - minY) / yRange) * chartH
        )
    }
}

private fun Offset.distanceTo(other: Offset): Float {
    return hypot(x - other.x, y - other.y)
}

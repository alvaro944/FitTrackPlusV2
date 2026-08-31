package com.alvarocervantes.fittrackplus.core.design.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import android.graphics.Paint
import java.math.BigDecimal
import kotlin.math.hypot
import kotlin.math.max

enum class LineChartBaselineMode {
    AutoRange,
    ZeroBaseline
}

internal data class LineChartAxisRange(
    val minimum: Float,
    val maximum: Float
) {
    val span: Float = (maximum - minimum).takeIf { it != 0f } ?: 1f
}

internal fun calculateLineChartAxisRange(
    values: List<Float>,
    baselineMode: LineChartBaselineMode
): LineChartAxisRange {
    require(values.isNotEmpty()) { "A chart axis requires at least one value." }

    val maximum = values.maxOrNull() ?: 0f
    val minimum = when (baselineMode) {
        LineChartBaselineMode.AutoRange -> values.minOrNull() ?: 0f
        LineChartBaselineMode.ZeroBaseline -> 0f
    }
    return LineChartAxisRange(minimum = minimum, maximum = maximum)
}

private data class LineChartHorizontalPadding(
    val left: Float,
    val right: Float
)

private fun formatLineChartAxisLabel(value: Float): String =
    BigDecimal.valueOf(value.toDouble()).stripTrailingZeros().toPlainString()

internal fun lineChartLabelIndices(
    values: List<Float>,
    selectedPointIndex: Int?
): Set<Int> {
    if (values.isEmpty()) return emptySet()

    return buildSet {
        add(0)
        add(values.lastIndex)
        add(values.indices.minBy { values[it] })
        add(values.indices.maxBy { values[it] })
        selectedPointIndex?.takeIf { it in values.indices }?.let(::add)
    }
}

@Composable
fun LineChart(
    points: List<Pair<Long, Float>>,
    modifier: Modifier = Modifier,
    selectedPointIndex: Int? = null,
    onPointSelected: ((Int) -> Unit)? = null,
    pointLabels: List<String> = emptyList(),
    xAxisLabels: List<String> = emptyList(),
    chartDescription: String? = null,
    baselineMode: LineChartBaselineMode = LineChartBaselineMode.AutoRange
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
    // Read from the type scale, which is in sp, so the labels follow the user's font size setting.
    // Drawing them at a dp size ignored that setting entirely.
    val labelFontSize = MaterialTheme.typography.labelSmall.fontSize

    val sortedPoints = points.sortedBy { it.first }
    val axisRange = calculateLineChartAxisRange(
        values = sortedPoints.map { it.second },
        baselineMode = baselineMode
    )
    val labelledPointIndices = lineChartLabelIndices(
        values = sortedPoints.map { it.second },
        selectedPointIndex = selectedPointIndex
    )
    val axisLabels = listOf(axisRange.maximum, axisRange.minimum).map(::formatLineChartAxisLabel)
    val density = LocalDensity.current
    val horizontalPadding = remember(axisLabels, labelFontSize, density) {
        val axisPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = with(density) { labelFontSize.toPx() }
        }
        val minimumPadding = with(density) { 18.dp.toPx() }
        val axisLabelGap = with(density) { 4.dp.toPx() }
        LineChartHorizontalPadding(
            left = max(
                minimumPadding,
                (axisLabels.maxOfOrNull(axisPaint::measureText) ?: 0f) + axisLabelGap
            ),
            right = minimumPadding
        )
    }
    val effectiveDescription = resolveChartDescription(chartDescription, sortedPoints, pointLabels)

    Canvas(
        modifier = modifier
            .semantics { contentDescription = effectiveDescription }
            .chartPointSelection(
                points = sortedPoints,
                axisRange = axisRange,
                horizontalPadding = horizontalPadding,
                onPointSelected = onPointSelected
            )
    ) {
        val padLeft = horizontalPadding.left
        val padRight = horizontalPadding.right
        val padTop = 22.dp.toPx()
        val padBottom = 26.dp.toPx()
        val offsets = sortedPoints.chartOffsets(
            width = size.width,
            height = size.height,
            padLeft = padLeft,
            padRight = padRight,
            padTop = padTop,
            padBottom = padBottom,
            axisRange = axisRange
        )
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = labelColor.toArgb()
            textSize = labelFontSize.toPx()
        }

        textPaint.textAlign = Paint.Align.LEFT
        drawContext.canvas.nativeCanvas.drawText(
            axisLabels.first(),
            0f,
            padTop - textPaint.fontMetrics.ascent,
            textPaint
        )
        drawContext.canvas.nativeCanvas.drawText(
            axisLabels.last(),
            0f,
            size.height - padBottom,
            textPaint
        )
        textPaint.textAlign = Paint.Align.CENTER

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
            pointLabels.getOrNull(index)?.takeIf { index in labelledPointIndices }?.let { label ->
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

private fun resolveChartDescription(
    chartDescription: String?,
    points: List<Pair<Long, Float>>,
    pointLabels: List<String>
): String = chartDescription ?: buildString {
    append("Grafica con ${points.size} puntos")
    pointLabels.firstOrNull()?.let { append(", desde $it") }
    pointLabels.lastOrNull()?.let { append(" hasta $it") }
}

private fun Modifier.chartPointSelection(
    points: List<Pair<Long, Float>>,
    axisRange: LineChartAxisRange,
    horizontalPadding: LineChartHorizontalPadding,
    onPointSelected: ((Int) -> Unit)?
): Modifier = pointerInput(points, axisRange, horizontalPadding, onPointSelected) {
    if (onPointSelected == null) return@pointerInput
    detectTapGestures { tapOffset ->
        val offsets = points.chartOffsets(
            width = size.width.toFloat(),
            height = size.height.toFloat(),
            padLeft = horizontalPadding.left,
            padRight = horizontalPadding.right,
            padTop = 22.dp.toPx(),
            padBottom = 26.dp.toPx(),
            axisRange = axisRange
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

internal fun List<Pair<Long, Float>>.chartOffsets(
    width: Float,
    height: Float,
    padLeft: Float,
    padRight: Float,
    padTop: Float,
    padBottom: Float,
    axisRange: LineChartAxisRange
): List<Offset> {
    val chartW = width - padLeft - padRight
    val chartH = height - padTop - padBottom
    return mapIndexed { index, (_, value) ->
        Offset(
            x = padLeft + (index.toFloat() / (size - 1)) * chartW,
            y = padTop + chartH - ((value - axisRange.minimum) / axisRange.span) * chartH
        )
    }
}

private fun Offset.distanceTo(other: Offset): Float {
    return hypot(x - other.x, y - other.y)
}

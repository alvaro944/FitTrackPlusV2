package com.alvarocervantes.fittrackplus.core.design.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.alvarocervantes.fittrackplus.core.design.FitTrackPlusStyle
import com.alvarocervantes.fittrackplus.domain.model.HeatmapDay

private const val WEEKS = 53
private const val DAYS_IN_WEEK = 7

@Composable
fun HeatmapCalendar(
    days: List<HeatmapDay>,
    modifier: Modifier = Modifier,
    onDayClick: ((HeatmapDay) -> Unit)? = null
) {
    val extraColors = FitTrackPlusStyle.extraColors
    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant
    val primary = MaterialTheme.colorScheme.primary
    val colorScale = listOf(
        surfaceVariantColor,          // nivel 0 — sin actividad
        extraColors.primarySoft,      // nivel 1
        extraColors.primaryMid,       // nivel 2
        primary,                      // nivel 3
        extraColors.primaryDark       // nivel 4
    )

    val dayMap = remember(days) { days.associateBy { it.epochDay } }

    // epochDay del primer slot del grid (lunes de la semana más antigua)
    val todayEpochDay = remember(days) {
        if (days.isNotEmpty()) days.last().epochDay else System.currentTimeMillis() / 86_400_000L
    }
    // Retroceder hasta el lunes de hace WEEKS semanas
    val todayDow = (todayEpochDay % 7 + 3).toInt() % 7  // 0=Lun, 6=Dom (epoch 0 es jueves)
    val gridStartEpochDay = todayEpochDay - todayDow - (WEEKS - 1) * 7L

    val cellSizeDp = 11.dp
    val gapDp = 2.dp
    val stepDp = cellSizeDp + gapDp

    val dayLabels = listOf("L", "", "X", "", "V", "", "D")

    Row(modifier = modifier) {
        // Etiquetas de día (columna fija a la izquierda)
        Column(modifier = Modifier.padding(top = 18.dp, end = 4.dp)) {
            dayLabels.forEach { label ->
                Box(modifier = Modifier.size(cellSizeDp + gapDp)) {
                    if (label.isNotEmpty()) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.align(androidx.compose.ui.Alignment.Center)
                        )
                    }
                }
            }
        }

        // Grid scrolleable horizontalmente
        Column(modifier = Modifier.horizontalScroll(rememberScrollState())) {
            // Etiquetas de mes
            Row {
                repeat(WEEKS) { weekIdx ->
                    val firstDayOfWeek = gridStartEpochDay + weekIdx * 7L
                    val label = monthLabelIfFirst(firstDayOfWeek, weekIdx)
                    Box(modifier = Modifier.width(stepDp)) {
                        if (label != null) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(2.dp))

            // Celdas: canvas dibuja cada semana como columna de 7 celdas
            val totalWidthDp = stepDp * WEEKS
            val totalHeightDp = stepDp * DAYS_IN_WEEK

            Canvas(
                modifier = Modifier
                    .size(totalWidthDp, totalHeightDp)
                    .pointerInput(dayMap, onDayClick) {
                        if (onDayClick == null) return@pointerInput
                        detectTapGestures { tap ->
                            val cellPx = (cellSizeDp + gapDp).toPx()
                            val col = (tap.x / cellPx).toInt().coerceIn(0, WEEKS - 1)
                            val row = (tap.y / cellPx).toInt().coerceIn(0, DAYS_IN_WEEK - 1)
                            val epochDay = gridStartEpochDay + col * 7L + row
                            dayMap[epochDay]?.let { onDayClick(it) }
                        }
                    }
            ) {
                val cellPx = cellSizeDp.toPx()
                val gapPx = gapDp.toPx()
                val stepPx = cellPx + gapPx
                val cornerPx = 2.dp.toPx()

                for (col in 0 until WEEKS) {
                    for (row in 0 until DAYS_IN_WEEK) {
                        val epochDay = gridStartEpochDay + col * 7L + row
                        val day = dayMap[epochDay]
                        val level = day?.intensityLevel ?: 0
                        val color = colorScale.getOrElse(level) { colorScale.last() }

                        drawRoundRect(
                            color = color,
                            topLeft = Offset(col * stepPx, row * stepPx),
                            size = Size(cellPx, cellPx),
                            cornerRadius = CornerRadius(cornerPx)
                        )
                    }
                }
            }
        }
    }
}

/** Devuelve la abreviatura del mes si es la primera semana visible de ese mes. */
private fun monthLabelIfFirst(firstDayOfWeek: Long, weekIdx: Int): String? {
    val epochMs = firstDayOfWeek * 86_400_000L
    val cal = java.util.Calendar.getInstance().apply { timeInMillis = epochMs }
    val dayOfMonth = cal.get(java.util.Calendar.DAY_OF_MONTH)
    if (weekIdx == 0 || dayOfMonth <= 7) {
        val months = listOf("Ene", "Feb", "Mar", "Abr", "May", "Jun",
                            "Jul", "Ago", "Sep", "Oct", "Nov", "Dic")
        return months[cal.get(java.util.Calendar.MONTH)]
    }
    return null
}

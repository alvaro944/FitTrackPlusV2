package com.alvarocervantes.fittrackplus.grit.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alvarocervantes.fittrackplus.domain.model.WorkoutStatsPeriod
import com.alvarocervantes.fittrackplus.feature.stats.ProgressMetric
import com.alvarocervantes.fittrackplus.feature.stats.StatsViewModel
import com.alvarocervantes.fittrackplus.grit.components.GritCard
import com.alvarocervantes.fittrackplus.grit.components.GritEmptyState
import com.alvarocervantes.fittrackplus.grit.components.GritScreenHeader
import com.alvarocervantes.fittrackplus.grit.components.GritSectionLabel
import com.alvarocervantes.fittrackplus.grit.components.GritStatTile
import com.alvarocervantes.fittrackplus.grit.components.GritToast
import com.alvarocervantes.fittrackplus.grit.theme.GritColors
import com.alvarocervantes.fittrackplus.grit.theme.GritShapes
import com.alvarocervantes.fittrackplus.grit.theme.GritType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val statsPeriodLabels = mapOf(
    WorkoutStatsPeriod.All to "Todo",
    WorkoutStatsPeriod.LastFourWeeks to "4 semanas",
    WorkoutStatsPeriod.LastTwelveWeeks to "12 semanas"
)

@Composable
fun GritStatsScreen(
    viewModel: StatsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            GritScreenHeader(title = "Tus Datos", icon = Icons.Filled.BarChart)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                WorkoutStatsPeriod.entries.forEach { period ->
                    val selected = uiState.selectedPeriod == period
                    Text(
                        text = (statsPeriodLabels[period] ?: period.name).uppercase(),
                        style = GritType.monoLabelSmall,
                        color = if (selected) GritColors.Black else GritColors.TextSecondary,
                        modifier = Modifier
                            .clip(GritShapes.small)
                            .background(if (selected) GritColors.Lime else GritColors.Surface)
                            .border(
                                1.dp,
                                if (selected) GritColors.Lime else GritColors.Border,
                                GritShapes.small
                            )
                            .clickable { viewModel.setPeriodFilter(period) }
                            .padding(horizontal = 12.dp, vertical = 7.dp)
                    )
                }
            }

            if (uiState.isEmpty && !uiState.isLoading) {
                GritEmptyState(
                    icon = Icons.Filled.BarChart,
                    title = "Sin datos todavía",
                    body = "Completa sesiones de entrenamiento para ver tu progreso aquí."
                )
            } else {
                SummaryTiles(
                    sessionCount = uiState.sessionCount,
                    totalVolumeKg = uiState.summarySessionVolumes.sumOf { it.totalVolumeKg },
                    exerciseCount = uiState.focusedExerciseRecords.size
                )

                ProgressionCard(viewModel = viewModel, uiState = uiState)

                ConsistencyCard(
                    heatmapDays = uiState.heatmapDays.map { it.intensityLevel }
                )

                RecordsCard(uiState = uiState)
            }
        }

        uiState.message?.let { message ->
            GritToast(
                title = "Datos",
                message = message,
                onDismiss = viewModel::clearMessage,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            )
        }
    }
}

@Composable
private fun SummaryTiles(
    sessionCount: Int,
    totalVolumeKg: Double,
    exerciseCount: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        GritStatTile(
            label = "Sesiones",
            value = "$sessionCount",
            modifier = Modifier.weight(1f)
        )
        GritStatTile(
            label = "Volumen",
            value = "%.1f".format(totalVolumeKg / 1000.0),
            unit = "K KG",
            modifier = Modifier.weight(1f)
        )
        GritStatTile(
            label = "Ejercicios",
            value = "$exerciseCount",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ProgressionCard(
    viewModel: StatsViewModel,
    uiState: com.alvarocervantes.fittrackplus.feature.stats.StatsUiState
) {
    GritCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = (uiState.selectedExerciseName ?: "PROGRESIÓN").uppercase(),
                    style = GritType.itemTitle
                )
                GritSectionLabel(
                    text = "Evolución de ${uiState.selectedProgressMetric.label}"
                )
            }

            // Exercise scope selector
            if (uiState.focusedExerciseProgress.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    uiState.focusedExerciseProgress.forEach { progress ->
                        val selected = uiState.selectedExerciseScopeKey == progress.scopeKey
                        Text(
                            text = progress.exerciseName.uppercase(),
                            style = GritType.monoLabelSmall,
                            color = if (selected) GritColors.Lime else GritColors.TextSecondary,
                            modifier = Modifier
                                .clip(GritShapes.small)
                                .border(
                                    1.dp,
                                    if (selected) GritColors.Lime else GritColors.Border,
                                    GritShapes.small
                                )
                                .clickable { viewModel.selectExerciseScope(progress.scopeKey) }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // Metric selector
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ProgressMetric.entries.forEach { metric ->
                    val selected = uiState.selectedProgressMetric == metric
                    Text(
                        text = metric.label.uppercase(),
                        style = GritType.monoLabelSmall,
                        color = if (selected) GritColors.Black else GritColors.TextSecondary,
                        modifier = Modifier
                            .clip(GritShapes.small)
                            .background(if (selected) GritColors.Lime else GritColors.Background)
                            .clickable { viewModel.selectProgressMetric(metric) }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            if (uiState.progressPoints.isEmpty()) {
                Text(
                    text = "Sin registros para este ejercicio en el periodo.",
                    style = GritType.monoBody,
                    modifier = Modifier.padding(vertical = 20.dp)
                )
            } else {
                ProgressBars(viewModel = viewModel, uiState = uiState)
            }
        }
    }
}

@Composable
private fun ProgressBars(
    viewModel: StatsViewModel,
    uiState: com.alvarocervantes.fittrackplus.feature.stats.StatsUiState
) {
    val values = uiState.progressChartValues
    val maxValue = values.maxOfOrNull { it.second }?.takeIf { it > 0f } ?: 1f
    val dateFormat = remember { SimpleDateFormat("d MMM", Locale("es", "ES")) }
    val selectedPoint = uiState.selectedProgressPoint

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        selectedPoint?.let { point ->
            val value = when (uiState.selectedProgressMetric) {
                ProgressMetric.MaxWeight -> point.maxWeightKg
                ProgressMetric.Volume -> point.volumeKg
                ProgressMetric.Reps -> point.totalReps.toDouble()
                ProgressMetric.EstimatedOneRepMax -> point.estimatedOneRepMaxKg
            }
            Row(
                modifier = Modifier
                    .clip(GritShapes.small)
                    .background(GritColors.Black)
                    .border(1.dp, GritColors.Lime, GritShapes.small)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "%.1f %s".format(value, uiState.selectedProgressMetric.unit).trim(),
                    style = GritType.monoStrong,
                    color = GritColors.Lime
                )
                Text(
                    text = dateFormat.format(Date(point.finishedAt)).uppercase(),
                    style = GritType.monoLabelSmall
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            uiState.progressPoints.forEach { point ->
                val metricValue = when (uiState.selectedProgressMetric) {
                    ProgressMetric.MaxWeight -> point.maxWeightKg.toFloat()
                    ProgressMetric.Volume -> point.volumeKg.toFloat()
                    ProgressMetric.Reps -> point.totalReps.toFloat()
                    ProgressMetric.EstimatedOneRepMax -> point.estimatedOneRepMaxKg.toFloat()
                }
                val fraction = (metricValue / maxValue).coerceIn(0.04f, 1f)
                val isSelected = selectedPoint?.sessionId == point.sessionId
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .height(150.dp * fraction)
                        .clip(GritShapes.small)
                        .background(
                            if (isSelected) GritColors.LimeLight
                            else GritColors.Lime.copy(alpha = 0.4f)
                        )
                        .clickable {
                            if (isSelected) {
                                viewModel.clearSelectedProgressPoint()
                            } else {
                                viewModel.selectProgressPoint(point.sessionId)
                            }
                        }
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(GritColors.Border)
        )
    }
}

@Composable
private fun ConsistencyCard(heatmapDays: List<Int>) {
    GritCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(text = "CONSISTENCIA", style = GritType.itemTitle)
                    GritSectionLabel(text = "Frecuencia de sesiones")
                }
                Icon(
                    imageVector = Icons.Filled.EmojiEvents,
                    contentDescription = null,
                    tint = GritColors.Lime,
                    modifier = Modifier.size(18.dp)
                )
            }

            val recentDays = heatmapDays.takeLast(35)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                recentDays.chunked(7).forEach { week ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        week.forEach { intensity ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .clip(GritShapes.small)
                                    .background(
                                        if (intensity > 0) {
                                            GritColors.Lime.copy(alpha = 0.25f * intensity)
                                        } else {
                                            GritColors.Neutral900
                                        }
                                    )
                            )
                        }
                        repeat(7 - week.size) {
                            Box(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RecordsCard(
    uiState: com.alvarocervantes.fittrackplus.feature.stats.StatsUiState
) {
    val dateFormat = remember { SimpleDateFormat("dd.MM.yyyy", Locale("es", "ES")) }
    GritCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.AutoAwesome,
                    contentDescription = null,
                    tint = GritColors.Lime,
                    modifier = Modifier.size(18.dp)
                )
                Text(text = "MEJORES MARCAS", style = GritType.itemTitle)
            }

            if (uiState.focusedExerciseRecords.isEmpty()) {
                Text(
                    text = "Todavía no hay marcas registradas.",
                    style = GritType.monoBody
                )
            } else {
                uiState.focusedExerciseRecords.forEach { records ->
                    val best = records.maxWeight ?: return@forEach
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(GritShapes.small)
                            .background(GritColors.Background)
                            .border(1.dp, GritColors.Border, GritShapes.small)
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            GritSectionLabel(text = records.exerciseName)
                            Text(
                                text = "%.1f KG × %d".format(best.weightKg, best.reps),
                                style = GritType.cardTitle
                            )
                        }
                        Text(
                            text = dateFormat.format(Date(best.finishedAt)),
                            style = GritType.monoLabelSmall
                        )
                    }
                }
            }
        }
    }
}

package com.alvarocervantes.fittrackplus.grit.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alvarocervantes.fittrackplus.feature.history.HistoryDetailUiState
import com.alvarocervantes.fittrackplus.feature.history.HistoryPeriodFilter
import com.alvarocervantes.fittrackplus.feature.history.HistorySessionUiState
import com.alvarocervantes.fittrackplus.feature.history.HistoryViewModel
import com.alvarocervantes.fittrackplus.grit.components.GritBadge
import com.alvarocervantes.fittrackplus.grit.components.GritCard
import com.alvarocervantes.fittrackplus.grit.components.GritEmptyState
import com.alvarocervantes.fittrackplus.grit.components.GritScreenHeader
import com.alvarocervantes.fittrackplus.grit.components.GritSectionLabel
import com.alvarocervantes.fittrackplus.grit.components.GritToast
import com.alvarocervantes.fittrackplus.grit.theme.GritColors
import com.alvarocervantes.fittrackplus.grit.theme.GritShapes
import com.alvarocervantes.fittrackplus.grit.theme.GritType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun GritHistoryScreen(
    onGoToWorkout: () -> Unit,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                horizontal = 16.dp,
                vertical = 20.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                GritScreenHeader(title = "Historial", icon = Icons.Filled.History)
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    HistoryPeriodFilter.entries.forEach { period ->
                        val selected = uiState.selectedPeriod == period
                        Text(
                            text = period.label.uppercase(),
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
            }

            if (uiState.sessions.isEmpty() && !uiState.isLoading) {
                item {
                    GritEmptyState(
                        icon = Icons.Filled.History,
                        title = "Historial vacío",
                        body = "Aún no has registrado ningún entrenamiento. ¡Ve a entrenar para empezar tu racha!",
                        actionText = "Comenzar Entrenamiento",
                        onAction = onGoToWorkout
                    )
                }
            } else {
                items(uiState.sessions, key = { it.sessionId }) { session ->
                    HistorySessionCard(
                        session = session,
                        isExpanded = uiState.selectedSessionId == session.sessionId,
                        isDetailLoading = uiState.isDetailLoading &&
                            uiState.selectedSessionId == session.sessionId,
                        detail = uiState.selectedDetail?.takeIf {
                            it.sessionId == session.sessionId
                        },
                        onToggle = {
                            if (uiState.selectedSessionId == session.sessionId) {
                                viewModel.clearSelection()
                            } else {
                                viewModel.selectSession(session.sessionId)
                            }
                        }
                    )
                }
            }
        }

        uiState.message?.let { message ->
            GritToast(
                title = "Historial",
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
private fun HistorySessionCard(
    session: HistorySessionUiState,
    isExpanded: Boolean,
    isDetailLoading: Boolean,
    detail: HistoryDetailUiState?,
    onToggle: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("d 'DE' MMMM", Locale("es", "ES")) }
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale("es", "ES")) }

    GritCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            Column(
                modifier = Modifier
                    .clickable(onClick = onToggle)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        GritBadge(
                            text = dateFormat.format(Date(session.startedAt))
                                .uppercase(Locale("es", "ES"))
                        )
                        Text(
                            text = timeFormat.format(Date(session.startedAt)),
                            style = GritType.monoLabelSmall
                        )
                    }
                    Icon(
                        imageVector = Icons.Filled.ExpandMore,
                        contentDescription = if (isExpanded) "Contraer" else "Expandir",
                        tint = GritColors.TextSecondary,
                        modifier = Modifier
                            .size(20.dp)
                            .rotate(if (isExpanded) 180f else 0f)
                    )
                }

                Text(
                    text = "${session.routineName.uppercase()} • ${session.dayName.uppercase()}",
                    style = GritType.itemTitle
                )

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    MetricChip(
                        icon = Icons.Filled.Timer,
                        text = formatDuration(session.durationMillis)
                    )
                    MetricChip(
                        icon = Icons.Filled.FitnessCenter,
                        text = "VOL: %.2fK KG".format(session.totalVolumeKg / 1000.0)
                    )
                    MetricChip(
                        icon = Icons.Filled.Layers,
                        text = "${session.setCount} SERIES"
                    )
                }
            }

            if (isExpanded) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(GritColors.Border)
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(GritColors.Background)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    GritSectionLabel(
                        text = "Desglose del entrenamiento",
                        color = GritColors.Lime
                    )
                    when {
                        isDetailLoading -> Text(
                            text = "Cargando desglose…",
                            style = GritType.monoBody
                        )
                        detail != null -> detail.exercises.forEach { exercise ->
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = exercise.name.uppercase(),
                                        style = GritType.monoStrong
                                    )
                                    GritSectionLabel(text = "${exercise.sets.size} SERIES")
                                }
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    exercise.sets.take(5).forEach { set ->
                                        Text(
                                            text = "S${set.setNumber}: ${set.weightText}kg×${set.reps}",
                                            style = GritType.monoLabelSmall,
                                            modifier = Modifier
                                                .clip(GritShapes.small)
                                                .background(GritColors.Neutral900)
                                                .border(1.dp, GritColors.Border, GritShapes.small)
                                                .padding(horizontal = 6.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = GritColors.Lime,
            modifier = Modifier.size(12.dp)
        )
        Text(text = text, style = GritType.monoLabelSmall)
    }
}

private fun formatDuration(durationMillis: Long): String {
    val totalMinutes = durationMillis / 60_000
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return if (hours > 0) "${hours}H ${minutes}M" else "$totalMinutes MIN"
}

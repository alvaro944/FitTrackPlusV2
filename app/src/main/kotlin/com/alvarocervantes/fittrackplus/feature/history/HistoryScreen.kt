package com.alvarocervantes.fittrackplus.feature.history

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alvarocervantes.fittrackplus.core.design.FitSpacing
import com.alvarocervantes.fittrackplus.core.design.FitTrackBadge
import com.alvarocervantes.fittrackplus.core.design.FitTrackBadgeTone
import com.alvarocervantes.fittrackplus.core.design.FitTrackCard
import com.alvarocervantes.fittrackplus.core.design.FitTrackEmptyState
import com.alvarocervantes.fittrackplus.core.design.FitTrackLoadingCard
import com.alvarocervantes.fittrackplus.core.design.FitTrackMetric
import com.alvarocervantes.fittrackplus.core.design.FitTrackMetricAccent
import com.alvarocervantes.fittrackplus.core.design.FitTrackScreenHeader
import com.alvarocervantes.fittrackplus.core.design.FitTrackSectionLabel
import com.alvarocervantes.fittrackplus.core.design.surfaceAlt
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    state.message?.let { message ->
        LaunchedEffect(message) {
            snackbarHostState.showSnackbar(message)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        HistoryContent(
            state = state,
            contentPadding = padding,
            onSessionClick = viewModel::selectSession,
            onBackToList = viewModel::clearSelection
        )
    }
}

@Composable
private fun HistoryContent(
    state: HistoryUiState,
    contentPadding: PaddingValues,
    onSessionClick: (Long) -> Unit,
    onBackToList: () -> Unit
) {
    val showingDetail = state.selectedSessionId != null || state.isDetailLoading

    AnimatedContent(
        targetState = showingDetail,
        transitionSpec = {
            fadeIn(animationSpec = tween(200)) togetherWith fadeOut(animationSpec = tween(200))
        },
        label = "history_content"
    ) { isDetailVisible ->
        if (isDetailVisible) {
            HistoryDetailContent(
                state = state,
                contentPadding = contentPadding,
                onBackToList = onBackToList
            )
        } else {
            HistoryListContent(
                state = state,
                contentPadding = contentPadding,
                onSessionClick = onSessionClick
            )
        }
    }
}

@Composable
private fun HistoryListContent(
    state: HistoryUiState,
    contentPadding: PaddingValues,
    onSessionClick: (Long) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding),
        contentPadding = PaddingValues(
            start = FitSpacing.screenHorizontal,
            top = FitSpacing.screenTop,
            end = FitSpacing.screenHorizontal,
            bottom = FitSpacing.screenBottom
        ),
        verticalArrangement = Arrangement.spacedBy(FitSpacing.lg)
    ) {
        item {
            FitTrackScreenHeader(
                title = "Historial",
                subtitle = "Sesiones finalizadas"
            )
        }

        when {
            state.isLoading -> {
                item { FitTrackLoadingCard(text = "Cargando sesiones finalizadas...") }
            }

            state.sessions.isEmpty() -> {
                item {
                    FitTrackEmptyState(
                        icon = Icons.Filled.History,
                        title = "Sin sesiones finalizadas",
                        message = "Finaliza un entrenamiento para verlo aqui.",
                        supporting = "El historial usa snapshots, asi que los cambios futuros en rutinas no modificaran estas sesiones."
                    )
                }
            }

            else -> {
                item {
                    FitTrackSectionLabel(label = "Sesiones")
                }
                items(
                    items = state.sessions,
                    key = { session -> session.sessionId }
                ) { session ->
                    HistorySessionCard(
                        session = session,
                        onClick = { onSessionClick(session.sessionId) }
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoryDetailContent(
    state: HistoryUiState,
    contentPadding: PaddingValues,
    onBackToList: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding),
        contentPadding = PaddingValues(
            start = FitSpacing.screenHorizontal,
            top = FitSpacing.screenTop,
            end = FitSpacing.screenHorizontal,
            bottom = FitSpacing.screenBottom
        ),
        verticalArrangement = Arrangement.spacedBy(FitSpacing.lg)
    ) {
        item {
            FitTrackScreenHeader(
                title = "Historial",
                subtitle = "Detalle historico",
                trailing = {
                    IconButton(onClick = onBackToList) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Volver al listado de historial"
                        )
                    }
                }
            )
        }

        when {
            state.isDetailLoading -> {
                item { FitTrackLoadingCard(text = "Cargando detalle historico...") }
            }

            state.selectedDetail != null -> {
                item {
                    HistoryDetailSummary(detail = state.selectedDetail)
                }
                item {
                    FitTrackSectionLabel(label = "Ejercicios")
                }
                items(
                    items = state.selectedDetail.exercises,
                    key = { exercise -> exercise.exerciseId }
                ) { exercise ->
                    HistoryExerciseCard(exercise = exercise)
                }
            }

            else -> {
                item { FitTrackLoadingCard(text = "Cargando detalle historico...") }
            }
        }
    }
}

@Composable
private fun HistorySessionCard(
    session: HistorySessionUiState,
    onClick: () -> Unit
) {
    FitTrackCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                role = Role.Button,
                onClickLabel = "Ver detalle de la sesion",
                onClick = onClick
            )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(FitSpacing.tiny)
            ) {
                Text(
                    text = session.routineName,
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = session.dayName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = formatDate(session.finishedAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            FitTrackBadge(
                label = "Semana ${session.weekNumber}",
                tone = FitTrackBadgeTone.Neutral
            )
        }
    }
}

@Composable
private fun HistoryDetailSummary(detail: HistoryDetailUiState) {
    FitTrackCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = detail.routineName,
            style = MaterialTheme.typography.titleLarge,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = detail.dayName,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(FitSpacing.xl)
        ) {
            FitTrackMetric(
                value = detail.exercises.size.toString(),
                label = "ejercicios",
                accent = FitTrackMetricAccent.Primary,
                compact = true
            )
            FitTrackMetric(
                value = detail.totalSetCount.toString(),
                label = "series",
                compact = true
            )
        }
        Text(
            text = "Finalizada ${formatDate(detail.finishedAt)}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun HistoryExerciseCard(exercise: HistoryExerciseUiState) {
    FitTrackCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            verticalArrangement = Arrangement.spacedBy(FitSpacing.md)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(FitSpacing.xs)
            ) {
                Text(
                    text = exercise.name,
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Objetivo: ${exercise.targetRepsText} reps",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            exercise.sets.forEach { set ->
                HistorySetRow(set = set)
            }
        }
    }
}

@Composable
private fun HistorySetRow(set: HistorySetUiState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceAlt, MaterialTheme.shapes.large)
            .padding(FitSpacing.smMd),
        horizontalArrangement = Arrangement.spacedBy(FitSpacing.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .background(MaterialTheme.colorScheme.surface, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = set.setNumber.toString(),
                style = MaterialTheme.typography.labelMedium
            )
        }
        Text(
            text = "${set.weightKg.toDisplayText()} kg",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "${set.reps} reps",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
    }
}

private fun formatDate(timestamp: Long): String {
    return SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(timestamp))
}

private fun Double.toDisplayText(): String {
    return if (this % 1.0 == 0.0) {
        toInt().toString()
    } else {
        String.format(Locale.getDefault(), "%.1f", this)
    }
}

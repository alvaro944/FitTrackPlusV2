package com.alvarocervantes.fittrackplus.feature.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun StatsScreen(
    viewModel: StatsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold { padding ->
        StatsContent(
            state = state,
            contentPadding = padding
        )
    }
}

@Composable
private fun StatsContent(
    state: StatsUiState,
    contentPadding: PaddingValues
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { StatsHeader(message = state.message) }

        when {
            state.isLoading -> {
                item { CircularProgressIndicator() }
            }

            state.isEmpty -> {
                item { EmptyStatsState() }
            }

            else -> {
                item { SectionTitle("Volumen por sesion") }
                items(
                    items = state.sessionVolumes,
                    key = { session -> session.sessionId }
                ) { session ->
                    SessionVolumeCard(session = session)
                }

                item { SectionTitle("Progreso por ejercicio") }
                items(
                    items = state.exerciseProgress,
                    key = { progress -> progress.exerciseKey }
                ) { progress ->
                    ExerciseProgressCard(progress = progress)
                }

                item { SectionTitle("Mejores marcas") }
                items(
                    items = state.exerciseRecords,
                    key = { records -> records.exerciseKey }
                ) { records ->
                    ExerciseRecordsCard(records = records)
                }
            }
        }
    }
}

@Composable
private fun StatsHeader(message: String?) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = "Datos",
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = "Estadisticas desde sesiones finalizadas",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (message != null) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun EmptyStatsState() {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Sin estadisticas",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Finaliza entrenamientos para calcular volumen, progreso y marcas.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.padding(top = 8.dp)
    )
}

@Composable
private fun SessionVolumeCard(session: SessionVolumeUiState) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = session.routineName,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = session.dayName,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Text(
                    text = "${session.totalVolumeKg.toDisplayText()} kg",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Text(
                text = formatDate(session.finishedAt),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ExerciseProgressCard(progress: ExerciseProgressUiState) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = progress.exerciseName,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            progress.entries.forEach { entry ->
                ProgressEntryRow(entry = entry)
            }
        }
    }
}

@Composable
private fun ProgressEntryRow(entry: ExerciseProgressEntryUiState) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = formatDate(entry.finishedAt),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "Volumen ${entry.volumeKg.toDisplayText()} kg - max ${entry.maxWeightKg.toDisplayText()} kg - reps ${entry.totalReps} - 1RM ${entry.estimatedOneRepMaxKg.toDisplayText()} kg",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun ExerciseRecordsCard(records: ExerciseRecordsUiState) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = records.exerciseName,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            RecordRow("Peso max", records.maxWeight?.let { "${it.weightKg.toDisplayText()} kg x ${it.reps}" })
            RecordRow("Reps max", records.maxReps?.let { "${it.reps} reps con ${it.weightKg.toDisplayText()} kg" })
            RecordRow("Volumen set", records.bestSetVolume?.let { "${it.setVolumeKg.toDisplayText()} kg" })
            RecordRow("1RM estimado", records.bestEstimatedOneRepMax?.let { "${it.estimatedOneRepMaxKg.toDisplayText()} kg" })
        }
    }
}

@Composable
private fun RecordRow(
    label: String,
    value: String?
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value ?: "-",
            style = MaterialTheme.typography.bodyMedium
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

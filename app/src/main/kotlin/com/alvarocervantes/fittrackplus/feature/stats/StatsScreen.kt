package com.alvarocervantes.fittrackplus.feature.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alvarocervantes.fittrackplus.core.design.components.LineChart
import com.alvarocervantes.fittrackplus.core.design.FitTrackBadge
import com.alvarocervantes.fittrackplus.core.design.FitTrackBadgeTone
import com.alvarocervantes.fittrackplus.core.design.FitTrackCard
import com.alvarocervantes.fittrackplus.core.design.FitTrackEmptyState
import com.alvarocervantes.fittrackplus.core.design.FitTrackLoadingCard
import com.alvarocervantes.fittrackplus.core.design.FitTrackMetric
import com.alvarocervantes.fittrackplus.core.design.FitTrackMetricAccent
import com.alvarocervantes.fittrackplus.core.design.FitTrackProgressBar
import com.alvarocervantes.fittrackplus.core.design.FitTrackScreenHeader
import com.alvarocervantes.fittrackplus.core.design.FitTrackSectionLabel
import com.alvarocervantes.fittrackplus.core.design.accentSoft
import com.alvarocervantes.fittrackplus.core.design.accentWarm
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
            contentPadding = padding,
            onSelectExercise = viewModel::selectExercise
        )
    }
}

@Composable
private fun StatsContent(
    state: StatsUiState,
    contentPadding: PaddingValues,
    onSelectExercise: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding),
        contentPadding = PaddingValues(start = 20.dp, top = 12.dp, end = 20.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            FitTrackScreenHeader(
                title = "Datos",
                subtitle = "Estadisticas desde sesiones finalizadas"
            )
        }

        when {
            state.isLoading -> {
                item { FitTrackLoadingCard(text = "Calculando datos desde sesiones finalizadas...") }
            }

            state.message != null -> {
                item {
                    FitTrackCard {
                        Text(
                            text = "No se pudieron cargar los datos",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            state.isEmpty -> {
                item {
                    FitTrackEmptyState(
                        icon = Icons.Filled.BarChart,
                        title = "Sin estadisticas",
                        message = "Finaliza entrenamientos para calcular volumen, progreso y marcas.",
                        supporting = "Solo cuentan sesiones finalizadas; una sesion abierta no aparece aqui."
                    )
                }
            }

            else -> {
                item {
                    SummaryGrid(state = state)
                }

                if (state.exerciseProgress.isNotEmpty()) {
                    item {
                        ProgressChartCard(
                            exerciseNames = state.exerciseProgress.map { it.exerciseName },
                            selectedExerciseName = state.selectedExerciseName,
                            progressPoints = state.progressPoints,
                            onSelectExercise = onSelectExercise
                        )
                    }
                }

                item { FitTrackSectionLabel(label = "Volumen por sesion") }
                items(
                    items = state.sessionVolumes,
                    key = { session -> session.sessionId }
                ) { session ->
                    SessionVolumeCard(session = session)
                }

                item { FitTrackSectionLabel(label = "Progreso por ejercicio") }
                items(
                    items = state.exerciseProgress,
                    key = { progress -> progress.exerciseKey }
                ) { progress ->
                    ExerciseProgressCard(progress = progress)
                }

                item { FitTrackSectionLabel(label = "Mejores marcas") }
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
private fun SummaryGrid(state: StatsUiState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        FitTrackCard(modifier = Modifier.weight(1f)) {
            FitTrackMetric(
                value = state.sessionVolumes.size.toString(),
                label = "sesiones",
                accent = FitTrackMetricAccent.Primary,
                compact = true
            )
        }
        FitTrackCard(modifier = Modifier.weight(1f)) {
            FitTrackMetric(
                value = state.exerciseProgress.size.toString(),
                label = "ejercicios",
                compact = true
            )
        }
        FitTrackCard(modifier = Modifier.weight(1f)) {
            FitTrackMetric(
                value = state.exerciseRecords.size.toString(),
                label = "records",
                accent = FitTrackMetricAccent.Warm,
                compact = true
            )
        }
    }
}

@Composable
private fun SessionVolumeCard(session: SessionVolumeUiState) {
    FitTrackCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
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
            FitTrackMetric(
                value = session.totalVolumeKg.toDisplayText(),
                unit = "kg",
                label = "volumen",
                accent = FitTrackMetricAccent.Primary,
                compact = true
            )
        }
    }
}

@Composable
private fun ExerciseProgressCard(progress: ExerciseProgressUiState) {
    val latest = progress.entries.lastOrNull()
    val maxWeight = progress.entries.maxOfOrNull { it.maxWeightKg } ?: 0.0

    FitTrackCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = progress.exerciseName,
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (latest != null) {
                    Text(
                        text = "Ultima sesion: ${formatDate(latest.finishedAt)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (latest != null) {
                FitTrackBadge(
                    label = "1RM ${latest.estimatedOneRepMaxKg.toDisplayText()}",
                    tone = FitTrackBadgeTone.Primary
                )
            }
        }

        if (latest != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FitTrackMetric(
                    value = latest.maxWeightKg.toDisplayText(),
                    unit = "kg",
                    label = "peso max",
                    accent = FitTrackMetricAccent.Primary,
                    compact = true
                )
                FitTrackMetric(
                    value = latest.totalReps.toString(),
                    label = "reps",
                    compact = true
                )
            }
            FitTrackProgressBar(
                progress = if (maxWeight == 0.0) 0f else (latest.maxWeightKg / maxWeight).toFloat()
            )
            Text(
                text = "Volumen ${latest.volumeKg.toDisplayText()} kg · mejor peso registrado ${maxWeight.toDisplayText()} kg",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ExerciseRecordsCard(records: ExerciseRecordsUiState) {
    FitTrackCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            FitTrackBadge(
                label = "PR",
                tone = FitTrackBadgeTone.Warm
            )
            Text(
                text = records.exerciseName,
                style = MaterialTheme.typography.titleLarge,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        RecordRow("Peso max", records.maxWeight?.let { "${it.weightKg.toDisplayText()} kg x ${it.reps}" })
        RecordRow("Reps max", records.maxReps?.let { "${it.reps} reps con ${it.weightKg.toDisplayText()} kg" })
        RecordRow("Volumen set", records.bestSetVolume?.let { "${it.setVolumeKg.toDisplayText()} kg" })
        RecordRow("1RM estimado", records.bestEstimatedOneRepMax?.let { "${it.estimatedOneRepMaxKg.toDisplayText()} kg" })
    }
}

@Composable
private fun RecordRow(
    label: String,
    value: String?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.accentSoft, MaterialTheme.shapes.large)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value ?: "-",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.accentWarm
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProgressChartCard(
    exerciseNames: List<String>,
    selectedExerciseName: String?,
    progressPoints: List<Pair<Long, Float>>,
    onSelectExercise: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    FitTrackCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Progreso visual",
            style = MaterialTheme.typography.titleMedium
        )

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = selectedExerciseName ?: "Selecciona un ejercicio",
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                exerciseNames.forEach { name ->
                    DropdownMenuItem(
                        text = { Text(name) },
                        onClick = {
                            onSelectExercise(name)
                            expanded = false
                        }
                    )
                }
            }
        }

        when {
            selectedExerciseName == null -> {
                Text(
                    text = "Selecciona un ejercicio para ver la evolucion de su peso maximo.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            progressPoints.size < 2 -> {
                Text(
                    text = "Se necesitan al menos 2 sesiones de '${selectedExerciseName}' para mostrar el grafico.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            else -> {
                LineChart(
                    points = progressPoints,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .padding(top = 4.dp)
                )
            }
        }
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

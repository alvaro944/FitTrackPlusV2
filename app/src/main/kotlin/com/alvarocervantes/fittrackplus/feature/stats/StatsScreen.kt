package com.alvarocervantes.fittrackplus.feature.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alvarocervantes.fittrackplus.domain.model.HeatmapDay
import com.alvarocervantes.fittrackplus.domain.model.WorkoutStatsPeriod
import com.alvarocervantes.fittrackplus.core.design.components.HeatmapCalendar
import com.alvarocervantes.fittrackplus.core.design.components.LineChart
import com.alvarocervantes.fittrackplus.core.design.FitSpacing
import com.alvarocervantes.fittrackplus.core.design.FitTrackBadge
import com.alvarocervantes.fittrackplus.core.design.FitTrackBadgeTone
import com.alvarocervantes.fittrackplus.core.design.FitTrackCard
import com.alvarocervantes.fittrackplus.core.design.FitTrackEmptyState
import com.alvarocervantes.fittrackplus.core.design.FitTrackMetric
import com.alvarocervantes.fittrackplus.core.design.components.SkeletonBlock
import com.alvarocervantes.fittrackplus.core.design.components.SkeletonCard
import com.alvarocervantes.fittrackplus.core.design.components.SkeletonText
import com.alvarocervantes.fittrackplus.core.design.FitTrackMetricAccent
import com.alvarocervantes.fittrackplus.core.design.FitTrackProgressBar
import com.alvarocervantes.fittrackplus.core.design.FitTrackScreenHeader
import com.alvarocervantes.fittrackplus.core.design.FitTrackSectionLabel
import com.alvarocervantes.fittrackplus.core.design.accentSoft
import com.alvarocervantes.fittrackplus.core.design.accentWarm
import com.alvarocervantes.fittrackplus.core.design.primarySoft
import com.alvarocervantes.fittrackplus.core.design.surfaceAlt
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Date
import java.util.Locale

@Composable
fun StatsScreen(
    viewModel: StatsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.message) {
        val msg = state.message ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(msg)
        viewModel.clearMessage()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        StatsContent(
            state = state,
            contentPadding = padding,
            onPeriodFilterChange = viewModel::setPeriodFilter,
            onSelectExercise = viewModel::selectExercise,
            onSelectProgressPoint = viewModel::selectProgressPoint,
            onClearSelectedProgressPoint = viewModel::clearSelectedProgressPoint,
            onHeatmapDayClick = viewModel::onHeatmapDayClick,
            onPreviousStepsWeek = viewModel::previousWeek,
            onNextStepsWeek = viewModel::nextWeek
        )
    }
}

@Composable
private fun StatsContent(
    state: StatsUiState,
    contentPadding: PaddingValues,
    onPeriodFilterChange: (WorkoutStatsPeriod) -> Unit,
    onSelectExercise: (String) -> Unit,
    onSelectProgressPoint: (Long) -> Unit,
    onClearSelectedProgressPoint: () -> Unit,
    onHeatmapDayClick: (HeatmapDay) -> Unit = {},
    onPreviousStepsWeek: () -> Unit = {},
    onNextStepsWeek: () -> Unit = {}
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
                title = "Datos",
                subtitle = "Estadisticas desde sesiones finalizadas"
            )
        }

        if (!state.isLoading) {
            item {
                StatsPeriodControls(
                    selectedPeriod = state.selectedPeriod,
                    onPeriodFilterChange = onPeriodFilterChange
                )
            }
        }

        when {
            state.isLoading -> {
                item { StatsLoadingSkeleton() }
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

                if (state.weeklyStepsData != null) {
                    item { FitTrackSectionLabel(label = "Actividad") }
                    item {
                        WeeklyStepsCard(
                            data = state.weeklyStepsData,
                            canGoNext = state.canGoToNextWeek,
                            onPrevious = onPreviousStepsWeek,
                            onNext = onNextStepsWeek
                        )
                    }
                }

                if (state.heatmapDays.isNotEmpty()) {
                    item { FitTrackSectionLabel(label = "Constancia") }
                    item {
                        FitTrackCard(modifier = Modifier.fillMaxWidth()) {
                            HeatmapCalendar(
                                days = state.heatmapDays,
                                onDayClick = onHeatmapDayClick
                            )
                        }
                    }
                }

                if (state.exerciseProgress.isNotEmpty()) {
                    item {
                        ProgressChartCard(
                            exerciseNames = state.exerciseProgress.map { it.exerciseName },
                            selectedExerciseName = state.selectedExerciseName,
                            progressPoints = state.progressPoints,
                            selectedProgressPoint = state.selectedProgressPoint,
                            onSelectExercise = onSelectExercise,
                            onSelectProgressPoint = onSelectProgressPoint,
                            onClearSelectedProgressPoint = onClearSelectedProgressPoint
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
private fun StatsPeriodControls(
    selectedPeriod: WorkoutStatsPeriod,
    onPeriodFilterChange: (WorkoutStatsPeriod) -> Unit
) {
    FitTrackCard(modifier = Modifier.fillMaxWidth()) {
        FitTrackSectionLabel(label = "Periodo")
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(FitSpacing.smMd)
        ) {
            WorkoutStatsPeriod.entries.forEach { period ->
                FilterChip(
                    selected = selectedPeriod == period,
                    onClick = { onPeriodFilterChange(period) },
                    label = { Text(period.label) }
                )
            }
        }
    }
}

@Composable
private fun SummaryGrid(state: StatsUiState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(FitSpacing.md)
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
                verticalArrangement = Arrangement.spacedBy(FitSpacing.tiny)
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
                horizontalArrangement = Arrangement.spacedBy(FitSpacing.lg)
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
                progress = if (maxWeight == 0.0) 0f else (latest.maxWeightKg / maxWeight).toFloat(),
                contentDescription = "Progreso de peso maximo de ${progress.exerciseName}"
            )
            Text(
                text = "Volumen ${latest.volumeKg.toDisplayText()} kg - " +
                    "mejor peso registrado ${maxWeight.toDisplayText()} kg",
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
            horizontalArrangement = Arrangement.spacedBy(FitSpacing.smMd)
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
        RecordRow(
            label = "1RM estimado",
            value = records.bestEstimatedOneRepMax?.let {
                "${it.estimatedOneRepMaxKg.toDisplayText()} kg"
            }
        )
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
            .padding(horizontal = FitSpacing.md, vertical = FitSpacing.smMd),
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
    progressPoints: List<ProgressChartPointUiState>,
    selectedProgressPoint: ProgressChartPointUiState?,
    onSelectExercise: (String) -> Unit,
    onSelectProgressPoint: (Long) -> Unit,
    onClearSelectedProgressPoint: () -> Unit
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
                val chartPoints = progressPoints.map { point ->
                    point.finishedAt to point.maxWeightKg.toFloat()
                }
                val selectedIndex = selectedProgressPoint?.let { selected ->
                    progressPoints.indexOfFirst { point -> point.sessionId == selected.sessionId }
                }?.takeIf { index -> index >= 0 }
                LineChart(
                    points = chartPoints,
                    selectedPointIndex = selectedIndex,
                    onPointSelected = { index ->
                        progressPoints.getOrNull(index)?.let { point ->
                            onSelectProgressPoint(point.sessionId)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .padding(top = FitSpacing.xs)
                )
                selectedProgressPoint?.let { point ->
                    ProgressPointDetails(
                        point = point,
                        onClear = onClearSelectedProgressPoint
                    )
                }
            }
        }
    }
}

@Composable
private fun ProgressPointDetails(
    point: ProgressChartPointUiState,
    onClear: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.accentSoft, MaterialTheme.shapes.large)
            .padding(FitSpacing.md),
        verticalArrangement = Arrangement.spacedBy(FitSpacing.xs)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatDate(point.finishedAt),
                style = MaterialTheme.typography.titleSmall
            )
            TextButton(onClick = onClear) {
                Text("Ocultar")
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(FitSpacing.lg)
        ) {
            FitTrackMetric(
                value = point.maxWeightKg.toDisplayText(),
                unit = "kg",
                label = "peso max",
                accent = FitTrackMetricAccent.Primary,
                compact = true
            )
            FitTrackMetric(
                value = point.volumeKg.toDisplayText(),
                unit = "kg",
                label = "volumen",
                compact = true
            )
        }
        Text(
            text = "${point.totalReps} reps - 1RM ${point.estimatedOneRepMaxKg.toDisplayText()} kg",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun WeeklyStepsCard(
    data: WeeklyStepsData,
    canGoNext: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    FitTrackCard(modifier = Modifier.fillMaxWidth()) {
        // Week navigation row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPrevious) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Semana anterior",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = formatWeekRange(data.weekStart, data.weekEnd),
                    style = MaterialTheme.typography.titleSmall
                )
                if (data.isCurrentWeek) {
                    Text(
                        text = "Llevas ${data.daysElapsedInWeek} dias de semana",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            IconButton(onClick = onNext, enabled = canGoNext) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Semana siguiente",
                    tint = if (canGoNext) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.28f)
                )
            }
        }

        // Day-by-day bars
        DayBarsRow(data = data)

        // Summary
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${data.daysGoalMet} de 7 dias con objetivo",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "%,d pasos".format(data.totalSteps),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.accentWarm
            )
        }
    }
}

@Composable
private fun DayBarsRow(data: WeeklyStepsData) {
    val dayLabels = listOf("L", "M", "X", "J", "V", "S", "D")
    val maxSteps = (data.dailySteps.values.maxOrNull() ?: 0L)
        .coerceAtLeast(data.dailyGoal.toLong())
    val barMaxHeight = 56.dp
    val todayIndex = if (data.isCurrentWeek) LocalDate.now().dayOfWeek.value - 1 else -1

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(FitSpacing.xs)
    ) {
        dayLabels.forEachIndexed { index, label ->
            val steps = data.dailySteps[index] ?: 0L
            val fraction = if (maxSteps > 0) (steps.toFloat() / maxSteps).coerceIn(0f, 1f) else 0f
            val goalMet = steps >= data.dailyGoal
            val isToday = index == todayIndex
            val barColor = when {
                goalMet -> MaterialTheme.colorScheme.accentWarm
                isToday -> MaterialTheme.colorScheme.primary
                steps > 0 -> MaterialTheme.colorScheme.primarySoft
                else -> MaterialTheme.colorScheme.surfaceAlt
            }

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(FitSpacing.tiny)
            ) {
                Box(
                    modifier = Modifier.height(barMaxHeight),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    val minBarFraction = if (steps > 0) 0.05f else 0f
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .height(barMaxHeight * fraction.coerceAtLeast(minBarFraction))
                            .clip(MaterialTheme.shapes.extraSmall)
                            .background(barColor)
                    )
                }
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isToday) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun formatWeekRange(weekStart: LocalDate, weekEnd: LocalDate): String {
    val locale = Locale("es", "ES")
    val startDay = weekStart.dayOfMonth
    val endDay = weekEnd.dayOfMonth
    val endMonth = weekEnd.month.getDisplayName(TextStyle.SHORT, locale).lowercase().trimEnd('.')
    return if (weekStart.month == weekEnd.month) {
        "del $startDay al $endDay de $endMonth"
    } else {
        val startMonth = weekStart.month.getDisplayName(TextStyle.SHORT, locale).lowercase().trimEnd('.')
        "del $startDay de $startMonth al $endDay de $endMonth"
    }
}

private val WorkoutStatsPeriod.label: String
    get() = when (this) {
        WorkoutStatsPeriod.All -> "Todo"
        WorkoutStatsPeriod.LastFourWeeks -> "4 semanas"
        WorkoutStatsPeriod.LastTwelveWeeks -> "12 semanas"
    }

@Composable
private fun StatsLoadingSkeleton() {
    Column(verticalArrangement = Arrangement.spacedBy(FitSpacing.lg)) {
        // Period filter controls placeholder
        SkeletonBlock(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = MaterialTheme.shapes.medium
        )
        // Summary metrics grid (3 blocks)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(FitSpacing.md)
        ) {
            repeat(3) {
                SkeletonCard(modifier = Modifier.weight(1f)) {
                    Column(verticalArrangement = Arrangement.spacedBy(FitSpacing.xs)) {
                        SkeletonText(widthFraction = 0.8f, lineHeight = 22.dp)
                        SkeletonText(widthFraction = 0.6f)
                    }
                }
            }
        }
        // Heatmap calendar placeholder
        SkeletonBlock(
            modifier = Modifier
                .fillMaxWidth()
                .height(90.dp),
            shape = MaterialTheme.shapes.large
        )
        // 2 session volume cards
        repeat(2) {
            SkeletonCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(FitSpacing.sm)) {
                    SkeletonText(widthFraction = 0.5f, lineHeight = 18.dp)
                    SkeletonBlock(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        shape = MaterialTheme.shapes.small
                    )
                }
            }
        }
        // 2 exercise progress cards
        repeat(2) {
            SkeletonCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(FitSpacing.sm)) {
                    SkeletonText(widthFraction = 0.4f, lineHeight = 18.dp)
                    SkeletonBlock(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp),
                        shape = MaterialTheme.shapes.small
                    )
                    SkeletonText(widthFraction = 0.6f)
                }
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

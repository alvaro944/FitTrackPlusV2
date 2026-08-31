// Large Compose screen composed of many small private composables; the function-count threshold
// is not meaningful for this kind of screen file.
@file:Suppress("TooManyFunctions")

package com.alvarocervantes.fittrackplus.feature.stats

import androidx.activity.compose.LocalActivity
import androidx.lifecycle.ViewModelStoreOwner
import com.alvarocervantes.fittrackplus.core.navigation.AppRoute
import com.alvarocervantes.fittrackplus.core.navigation.AppShellViewModel
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.semantics.Role
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alvarocervantes.fittrackplus.domain.model.HeatmapDay
import com.alvarocervantes.fittrackplus.domain.model.WorkoutStatsPeriod
import com.alvarocervantes.fittrackplus.domain.model.WeightUnit
import com.alvarocervantes.fittrackplus.core.design.components.LineChart
import com.alvarocervantes.fittrackplus.core.design.FitSpacing
import com.alvarocervantes.fittrackplus.core.design.FitTrackBadge
import com.alvarocervantes.fittrackplus.core.design.FitTrackBadgeTone
import com.alvarocervantes.fittrackplus.core.design.FitTrackCard
import com.alvarocervantes.fittrackplus.core.design.FitTrackEmptyState
import com.alvarocervantes.fittrackplus.core.design.FitTrackErrorState
import com.alvarocervantes.fittrackplus.core.design.FitTrackDropdownField
import com.alvarocervantes.fittrackplus.core.design.FitTrackKeyValueRow
import com.alvarocervantes.fittrackplus.core.design.FitTrackKeyValueRowStyle
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
import com.alvarocervantes.fittrackplus.core.design.success
import com.alvarocervantes.fittrackplus.core.design.primarySoft
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Date
import java.util.Locale

// The UI is fixed Spanish by project design, so number and date formatting stays fixed
// to this locale everywhere too, instead of mixing it with Locale.getDefault()/Locale.US.
private val STATS_LOCALE = Locale("es", "ES")

@Composable
fun StatsScreen(
    viewModel: StatsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val activity = LocalActivity.current
    val appShellOwner = requireNotNull(activity) as ViewModelStoreOwner
    val appShellViewModel: AppShellViewModel = hiltViewModel(appShellOwner)
    val listState = rememberLazyListState()

    LaunchedEffect(state.message) {
        val msg = state.message ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(msg)
        viewModel.clearMessage()
    }

    LaunchedEffect(Unit) {
        appShellViewModel.activeTabReselected.collect { route ->
            if (route == AppRoute.Stats) {
                listState.animateScrollToItem(0)
            }
        }
    }

    Scaffold(
        // The app shell already applies the system bar insets; without this the
        // status bar padding lands twice and leaves a dead band above the content.
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        StatsContent(
            state = state,
            contentPadding = padding,
            listState = listState,
            onRetry = viewModel::retry,
            onPeriodFilterChange = viewModel::setPeriodFilter,
            onSelectRoutine = viewModel::selectRoutine,
            onSelectDay = viewModel::selectDay,
            onSelectExercise = viewModel::selectExerciseScope,
            onSelectProgressMetric = viewModel::selectProgressMetric,
            onSelectProgressPoint = viewModel::selectProgressPoint,
            onClearSelectedProgressPoint = viewModel::clearSelectedProgressPoint,
            onPreviousStepsWeek = viewModel::previousWeek,
            onNextStepsWeek = viewModel::nextWeek,
            onSelectStepsDay = viewModel::selectStepsDay,
            onPreviousCalendarMonth = viewModel::previousCalendarMonth,
            onNextCalendarMonth = viewModel::nextCalendarMonth
        )
    }
}

@Composable
private fun StatsContent(
    state: StatsUiState,
    contentPadding: PaddingValues,
    listState: LazyListState,
    onRetry: () -> Unit,
    onPeriodFilterChange: (WorkoutStatsPeriod) -> Unit,
    onSelectRoutine: (String) -> Unit,
    onSelectDay: (String) -> Unit,
    onSelectExercise: (String) -> Unit,
    onSelectProgressMetric: (ProgressMetric) -> Unit,
    onSelectProgressPoint: (Long) -> Unit,
    onClearSelectedProgressPoint: () -> Unit,
    onPreviousStepsWeek: () -> Unit = {},
    onNextStepsWeek: () -> Unit = {},
    onSelectStepsDay: (Int?) -> Unit = {},
    onPreviousCalendarMonth: () -> Unit = {},
    onNextCalendarMonth: () -> Unit = {}
) {
    LazyColumn(
        state = listState,
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

            state.error != null -> {
                item {
                    FitTrackErrorState(
                        title = "No se pudieron cargar los datos",
                        message = state.error,
                        onRetry = onRetry
                    )
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

                if (state.availableRoutineNames.isNotEmpty()) {
                    item {
                        StatsFocusControls(
                            routineNames = state.availableRoutineNames,
                            selectedRoutineName = state.selectedRoutineName,
                            dayNames = state.availableDayNames,
                            selectedDayName = state.selectedDayName,
                            onSelectRoutine = onSelectRoutine,
                            onSelectDay = onSelectDay
                        )
                    }
                }

                if (state.weeklyStepsData != null) {
                    item { FitTrackSectionLabel(label = "Actividad") }
                    item {
                        WeeklyStepsCard(
                            data = state.weeklyStepsData,
                            canGoNext = state.canGoToNextWeek,
                            onPrevious = onPreviousStepsWeek,
                            onNext = onNextStepsWeek,
                            selectedDayIndex = state.selectedStepsDayIndex,
                            onDaySelect = onSelectStepsDay
                        )
                    }
                }

                if (state.heatmapDays.isNotEmpty()) {
                    item { FitTrackSectionLabel(label = "Constancia") }
                    item {
                        ConsistencyCalendarCard(
                            days = state.heatmapDays,
                            visibleMonth = state.visibleCalendarMonth,
                            onPreviousMonth = onPreviousCalendarMonth,
                            onNextMonth = onNextCalendarMonth
                        )
                    }
                }

                if (state.focusedExerciseProgress.isNotEmpty()) {
                    item {
                        ProgressChartCard(
                            exercises = state.focusedExerciseProgress,
                            selectedExerciseName = state.selectedExerciseName,
                            progressPoints = state.progressPoints,
                            chartValues = state.progressChartValues,
                            selectedMetric = state.selectedProgressMetric,
                            selectedProgressPoint = state.selectedProgressPoint,
                            weightUnit = state.weightUnit,
                            onSelectExercise = onSelectExercise,
                            onSelectProgressMetric = onSelectProgressMetric,
                            onSelectProgressPoint = onSelectProgressPoint,
                            onClearSelectedProgressPoint = onClearSelectedProgressPoint
                        )
                    }
                }

                if (state.focusedSessionVolumesChronological.isNotEmpty()) {
                    item { FitTrackSectionLabel(label = "Volumen por sesion") }
                    item {
                        SessionVolumeTrendCard(
                            sessions = state.focusedSessionVolumesChronological,
                            weightUnit = state.weightUnit
                        )
                    }
                }

                state.selectedExerciseProgress?.let { progress ->
                    item { FitTrackSectionLabel(label = "Progreso del ejercicio") }
                    item {
                        ExerciseProgressCard(progress = progress, weightUnit = state.weightUnit)
                    }
                }

                state.selectedExerciseRecords?.let { records ->
                    item { FitTrackSectionLabel(label = "Mejores marcas") }
                    item {
                        ExerciseRecordsCard(records = records, weightUnit = state.weightUnit)
                    }
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
private fun StatsFocusControls(
    routineNames: List<String>,
    selectedRoutineName: String?,
    dayNames: List<String>,
    selectedDayName: String?,
    onSelectRoutine: (String) -> Unit,
    onSelectDay: (String) -> Unit
) {
    FitTrackCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Vista enfocada",
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = "Elige rutina y dia para ver solo sus sesiones, ejercicios y marcas.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        FitTrackDropdownField(
            label = "Rutina",
            value = selectedRoutineName ?: "Selecciona rutina",
            options = routineNames,
            onSelect = onSelectRoutine
        )
        FitTrackDropdownField(
            label = "Dia",
            value = selectedDayName ?: "Selecciona dia",
            options = dayNames,
            onSelect = onSelectDay
        )
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
                value = state.sessionCount.toString(),
                label = "sesiones",
                accent = FitTrackMetricAccent.Primary,
                compact = true
            )
        }
        FitTrackCard(modifier = Modifier.weight(1f)) {
            FitTrackMetric(
                value = state.exerciseCount.toString(),
                label = "ejercicios",
                compact = true
            )
        }
        FitTrackCard(modifier = Modifier.weight(1f)) {
            FitTrackMetric(
                value = state.personalRecordCount.toString(),
                label = "PRs",
                accent = FitTrackMetricAccent.Warm,
                compact = true
            )
        }
    }
}

@Composable
private fun ConsistencyCalendarCard(
    days: List<HeatmapDay>,
    visibleMonth: YearMonth,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    val currentMonth = YearMonth.now()
    val activeDays = remember(days) {
        days
            .filter { day -> day.sessionCount > 0 }
            .associateBy { day -> LocalDate.ofEpochDay(day.epochDay) }
    }

    FitTrackCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPreviousMonth) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Mes anterior"
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Calendario de entrenos",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Verde marca los dias entrenados",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(
                onClick = onNextMonth,
                enabled = visibleMonth < currentMonth
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Mes siguiente"
                )
            }
        }
        MonthConsistencyGrid(
            month = visibleMonth,
            activeDays = activeDays
        )
    }
}

@Composable
private fun MonthConsistencyGrid(
    month: YearMonth,
    activeDays: Map<LocalDate, HeatmapDay>
) {
    val locale = STATS_LOCALE
    val firstDay = month.atDay(1)
    val leadingBlankDays = firstDay.dayOfWeek.value - 1
    val totalSlots = ((leadingBlankDays + month.lengthOfMonth() + 6) / 7) * 7
    val dayLabels = listOf("L", "M", "X", "J", "V", "S", "D")

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(FitSpacing.sm)
    ) {
        Text(
            text = month.month.getDisplayName(TextStyle.FULL, locale)
                .replaceFirstChar { it.uppercase() } + " ${month.year}",
            style = MaterialTheme.typography.labelLarge
        )
        Row(horizontalArrangement = Arrangement.spacedBy(FitSpacing.xs)) {
            dayLabels.forEach { label ->
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        repeat(totalSlots / 7) { week ->
            Row(horizontalArrangement = Arrangement.spacedBy(FitSpacing.xs)) {
                repeat(7) { dayOfWeek ->
                    val slot = week * 7 + dayOfWeek
                    val dayNumber = slot - leadingBlankDays + 1
                    val date = if (dayNumber in 1..month.lengthOfMonth()) {
                        month.atDay(dayNumber)
                    } else {
                        null
                    }
                    ConsistencyDayCell(
                        modifier = Modifier.weight(1f),
                        date = date,
                        heatmapDay = date?.let { activeDays[it] }
                    )
                }
            }
        }
    }
}

@Composable
private fun ConsistencyDayCell(
    modifier: Modifier = Modifier,
    date: LocalDate?,
    heatmapDay: HeatmapDay?
) {
    val hasWorkout = heatmapDay != null
    val isToday = date == LocalDate.now()
    val backgroundColor = when {
        hasWorkout -> MaterialTheme.colorScheme.primary
        isToday -> MaterialTheme.colorScheme.primarySoft
        date != null -> MaterialTheme.colorScheme.surfaceVariant
        else -> MaterialTheme.colorScheme.surface.copy(alpha = 0f)
    }
    val textColor = when {
        hasWorkout -> MaterialTheme.colorScheme.onPrimary
        isToday -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(
        modifier = modifier
            .height(34.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        if (date != null) {
            Text(
                text = date.dayOfMonth.toString(),
                style = MaterialTheme.typography.labelMedium,
                color = textColor
            )
        }
    }
}

@Composable
private fun SessionVolumeTrendCard(
    sessions: List<SessionVolumeUiState>,
    weightUnit: WeightUnit
) {
    val first = sessions.firstOrNull()
    val last = sessions.lastOrNull()
    val delta = if (first != null && last != null) last.totalVolumeKg - first.totalVolumeKg else 0.0
    val selectedSessions = sessions.takeLast(8)

    FitTrackCard(modifier = Modifier.fillMaxWidth()) {
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
                    text = "Tendencia de volumen",
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${sessions.size} sesiones registradas",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            FitTrackMetric(
                value = last?.totalVolumeKg?.toDisplayText(weightUnit) ?: "0",
                unit = weightUnit.label,
                label = "ultima",
                accent = FitTrackMetricAccent.Primary,
                compact = true
            )
        }

        if (selectedSessions.size >= 2) {
            LineChart(
                points = selectedSessions.map { session ->
                    session.finishedAt to weightUnit.fromKilograms(session.totalVolumeKg).toFloat()
                },
                pointLabels = selectedSessions.map { session ->
                    "${session.totalVolumeKg.toDisplayText(weightUnit)} ${weightUnit.label}"
                },
                xAxisLabels = selectedSessions.map { session -> formatChartDate(session.finishedAt) },
                chartDescription = "Tendencia de volumen a lo largo de ${selectedSessions.size} sesiones, " +
                    "de ${selectedSessions.first().totalVolumeKg.toDisplayText(weightUnit)} a " +
                    "${selectedSessions.last().totalVolumeKg.toDisplayText(weightUnit)} ${weightUnit.label}",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            )
        } else {
            Text(
                text = "Se necesitan al menos 2 sesiones para ver tendencia.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = first?.let {
                    "Inicio ${it.totalVolumeKg.toDisplayText(weightUnit)} ${weightUnit.label}"
                } ?: "Sin datos",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = when {
                    delta > 0.0 -> "+${delta.toDisplayText(weightUnit)} ${weightUnit.label}"
                    delta < 0.0 -> "${delta.toDisplayText(weightUnit)} ${weightUnit.label}"
                    else -> "sin cambio"
                },
                style = MaterialTheme.typography.bodySmall,
                // Aligned with History: rising volume is the positive tone, falling is the warm
                // one. Before, a rise was orange here and green there for the same movement.
                color = when {
                    delta > 0.0 -> MaterialTheme.colorScheme.success
                    delta < 0.0 -> MaterialTheme.colorScheme.accentWarm
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}

@Composable
private fun ExerciseProgressCard(progress: ExerciseProgressUiState, weightUnit: WeightUnit) {
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
                    label = "1RM ${latest.estimatedOneRepMaxKg.toDisplayText(weightUnit)} ${weightUnit.label}",
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
                    value = latest.maxWeightKg.toDisplayText(weightUnit),
                    unit = weightUnit.label,
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
                text = "Volumen ${latest.volumeKg.toDisplayText(weightUnit)} ${weightUnit.label} - " +
                    "mejor peso registrado ${maxWeight.toDisplayText(weightUnit)} ${weightUnit.label}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ExerciseRecordsCard(records: ExerciseRecordsUiState, weightUnit: WeightUnit) {
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

        RecordRow("Peso max", records.maxWeight?.let { "${it.weightKg.toDisplayText(weightUnit)} ${weightUnit.label} x ${it.reps}" })
        RecordRow("Reps max", records.maxReps?.let { "${it.reps} reps con ${it.weightKg.toDisplayText(weightUnit)} ${weightUnit.label}" })
        RecordRow("Volumen set", records.bestSetVolume?.let { "${it.setVolumeKg.toDisplayText(weightUnit)} ${weightUnit.label}" })
        RecordRow(
            label = "1RM estimado",
            value = records.bestEstimatedOneRepMax?.let {
                "${it.estimatedOneRepMaxKg.toDisplayText(weightUnit)} ${weightUnit.label}"
            }
        )
    }
}

@Composable
private fun RecordRow(
    label: String,
    value: String?
) {
    FitTrackKeyValueRow(
        label = label,
        value = value ?: "-",
        style = FitTrackKeyValueRowStyle.Pill
    )
}

@Composable
private fun ProgressChartCard(
    exercises: List<ExerciseProgressUiState>,
    selectedExerciseName: String?,
    progressPoints: List<ProgressChartPointUiState>,
    chartValues: List<Pair<Long, Float>>,
    selectedMetric: ProgressMetric,
    selectedProgressPoint: ProgressChartPointUiState?,
    weightUnit: WeightUnit,
    onSelectExercise: (String) -> Unit,
    onSelectProgressMetric: (ProgressMetric) -> Unit,
    onSelectProgressPoint: (Long) -> Unit,
    onClearSelectedProgressPoint: () -> Unit
) {
    FitTrackCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Progreso visual",
            style = MaterialTheme.typography.titleMedium
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(FitSpacing.sm)
        ) {
            ProgressMetric.entries.forEach { metric ->
                FilterChip(
                    selected = selectedMetric == metric,
                    onClick = { onSelectProgressMetric(metric) },
                    label = { Text(metric.label) }
                )
            }
        }

        FitTrackDropdownField(
            label = "Ejercicio",
            value = selectedExerciseName ?: "Selecciona un ejercicio",
            options = exercises,
            onSelect = { exercise -> onSelectExercise(exercise.scopeKey) },
            optionLabel = { it.exerciseName }
        )

        when {
            selectedExerciseName == null -> {
                Text(
                    text = "Selecciona un ejercicio de este dia para ver la evolucion de su peso maximo.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            progressPoints.size < 2 -> {
                Text(
                    text = "Se necesitan al menos 2 sesiones de '$selectedExerciseName' en esta seleccion para mostrar el grafico.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            else -> {
                val selectedIndex = selectedProgressPoint?.let { selected ->
                    progressPoints.indexOfFirst { point -> point.sessionId == selected.sessionId }
                }?.takeIf { index -> index >= 0 }
                LineChart(
                    points = chartValues,
                    selectedPointIndex = selectedIndex,
                    pointLabels = progressPoints.map { point -> point.toChartLabel(selectedMetric, weightUnit) },
                    xAxisLabels = progressPoints.map { point -> formatChartDate(point.finishedAt) },
                    chartDescription = "Evolucion de ${selectedMetric.label} de $selectedExerciseName " +
                        "a lo largo de ${progressPoints.size} sesiones, de " +
                        "${progressPoints.first().toChartLabel(selectedMetric, weightUnit)} a " +
                        progressPoints.last().toChartLabel(selectedMetric, weightUnit),
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
                        weightUnit = weightUnit,
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
    weightUnit: WeightUnit,
    onClear: () -> Unit
) {
    FitTrackKeyValueRow(
        style = FitTrackKeyValueRowStyle.Pill
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
                value = point.maxWeightKg.toDisplayText(weightUnit),
                unit = weightUnit.label,
                label = "peso max",
                accent = FitTrackMetricAccent.Primary,
                compact = true
            )
            FitTrackMetric(
                value = point.volumeKg.toDisplayText(weightUnit),
                unit = weightUnit.label,
                label = "volumen",
                compact = true
            )
        }
        Text(
            text = "${point.totalReps} reps - 1RM ${point.estimatedOneRepMaxKg.toDisplayText(weightUnit)} ${weightUnit.label}",
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
    onNext: () -> Unit,
    selectedDayIndex: Int?,
    onDaySelect: (Int?) -> Unit
) {
    val effectiveSelectedDayIndex = selectedDayIndex
        ?: if (data.isCurrentWeek) LocalDate.now().dayOfWeek.value - 1 else null

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
        DayBarsRow(
            data = data,
            selectedDayIndex = effectiveSelectedDayIndex,
            onDaySelect = { onDaySelect(it) }
        )

        // Selected day detail
        effectiveSelectedDayIndex?.let { idx ->
            SelectedDayDetail(
                dayIndex = idx,
                weekStart = data.weekStart,
                dailySteps = data.dailySteps,
                dailyGoal = data.dailyGoal
            )
        }

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
                text = String.format(STATS_LOCALE, "%,d pasos", data.totalSteps),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.accentWarm
            )
        }
    }
}

@Composable
private fun DayBarsRow(
    data: WeeklyStepsData,
    selectedDayIndex: Int?,
    onDaySelect: (Int) -> Unit
) {
    val dayLabels = listOf("L", "M", "X", "J", "V", "S", "D")
    val maxSteps = (data.dailySteps.values.maxOrNull() ?: 0L)
        .coerceAtLeast(data.dailyGoal.toLong())
    val barMaxHeight = 56.dp
    val todayIndex = if (data.isCurrentWeek) LocalDate.now().dayOfWeek.value - 1 else -1

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(FitSpacing.xs)
    ) {
        dayLabels.forEachIndexed { index, label ->
            DayBarColumn(
                modifier = Modifier.weight(1f),
                label = label,
                steps = data.dailySteps[index] ?: 0L,
                dailyGoal = data.dailyGoal,
                maxSteps = maxSteps,
                barMaxHeight = barMaxHeight,
                isToday = index == todayIndex,
                isSelected = index == selectedDayIndex,
                onSelect = { onDaySelect(index) }
            )
        }
    }
}

@Composable
private fun DayBarColumn(
    modifier: Modifier = Modifier,
    label: String,
    steps: Long,
    dailyGoal: Int,
    maxSteps: Long,
    barMaxHeight: androidx.compose.ui.unit.Dp,
    isToday: Boolean,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val fraction = if (maxSteps > 0) (steps.toFloat() / maxSteps).coerceIn(0f, 1f) else 0f
    val goalMet = steps >= dailyGoal
    val barColor = when {
        goalMet -> MaterialTheme.colorScheme.success
        isToday -> MaterialTheme.colorScheme.primary
        steps > 0 -> MaterialTheme.colorScheme.primarySoft
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val labelColor = if (isSelected || isToday) MaterialTheme.colorScheme.primary
    else MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = modifier
            .semantics { selected = isSelected }
            .clickable(
                onClickLabel = "Ver pasos del dia $label",
                role = Role.Tab,
                onClick = onSelect
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(FitSpacing.tiny)
    ) {
        Box(
            modifier = Modifier.height(barMaxHeight),
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(if (isSelected) 0.75f else 0.6f)
                    .height(barMaxHeight * fraction.coerceAtLeast(if (steps > 0) 0.05f else 0f))
                    .clip(MaterialTheme.shapes.extraSmall)
                    .background(barColor)
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = labelColor
        )
        formatStepsAbbreviated(steps)?.let { abbrev ->
            Text(
                text = abbrev,
                style = MaterialTheme.typography.labelSmall,
                color = if (goalMet) MaterialTheme.colorScheme.success
                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun SelectedDayDetail(
    dayIndex: Int,
    weekStart: LocalDate,
    dailySteps: Map<Int, Long>,
    dailyGoal: Int
) {
    val locale = STATS_LOCALE
    val date = weekStart.plusDays(dayIndex.toLong())
    val steps = dailySteps[dayIndex] ?: 0L
    val progress = if (dailyGoal > 0) (steps.toFloat() / dailyGoal).coerceIn(0f, 1f) else 0f
    val dayName = date.dayOfWeek.getDisplayName(TextStyle.FULL, locale)
        .replaceFirstChar { it.uppercase() }
    val dateStr = "${date.dayOfMonth} de ${date.month.getDisplayName(TextStyle.FULL, locale)}"

    FitTrackKeyValueRow(
        style = FitTrackKeyValueRowStyle.Pill
    ) {
        Text(
            text = "$dayName, $dateStr",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (steps > 0) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = String.format(STATS_LOCALE, "%,d / %,d pasos", steps, dailyGoal),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            FitTrackProgressBar(
                progress = progress,
                color = if (steps >= dailyGoal) MaterialTheme.colorScheme.success
                else MaterialTheme.colorScheme.primary,
                contentDescription = "Progreso de pasos"
            )
        } else {
            Text(
                text = "Sin datos para este dia",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun formatStepsAbbreviated(steps: Long): String? {
    if (steps <= 0) return null
    if (steps < 1000) return steps.toString()
    val k = steps / 1000.0
    return if (k >= 10) "${k.toInt()}k" else String.format(STATS_LOCALE, "%.1fk", k)
}

private fun formatWeekRange(weekStart: LocalDate, weekEnd: LocalDate): String {
    val locale = STATS_LOCALE
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
    return SimpleDateFormat("dd/MM/yyyy HH:mm", STATS_LOCALE).format(Date(timestamp))
}

private fun formatChartDate(timestamp: Long): String {
    return SimpleDateFormat("dd/MM", STATS_LOCALE).format(Date(timestamp))
}

private fun ProgressChartPointUiState.toChartLabel(metric: ProgressMetric, weightUnit: WeightUnit): String {
    return when (metric) {
        ProgressMetric.MaxWeight -> "${maxWeightKg.toDisplayText(weightUnit)} ${weightUnit.label}"
        ProgressMetric.Reps -> totalReps.toString()
        ProgressMetric.Volume -> "${volumeKg.toDisplayText(weightUnit)} ${weightUnit.label}"
        ProgressMetric.EstimatedOneRepMax -> "${estimatedOneRepMaxKg.toDisplayText(weightUnit)} ${weightUnit.label}"
    }
}

private fun Double.toDisplayText(): String {
    return if (this % 1.0 == 0.0) {
        toInt().toString()
    } else {
        String.format(STATS_LOCALE, "%.1f", this)
    }
}

private fun Double.toDisplayText(weightUnit: WeightUnit): String =
    weightUnit.fromKilograms(this).toDisplayText()

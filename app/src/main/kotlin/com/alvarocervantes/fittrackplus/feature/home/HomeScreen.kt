package com.alvarocervantes.fittrackplus.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alvarocervantes.fittrackplus.core.design.FitSpacing
import com.alvarocervantes.fittrackplus.core.design.FitTrackBadge
import com.alvarocervantes.fittrackplus.core.design.components.SkeletonBlock
import com.alvarocervantes.fittrackplus.core.design.components.SkeletonText
import com.alvarocervantes.fittrackplus.core.design.FitTrackBadgeTone
import com.alvarocervantes.fittrackplus.core.design.FitTrackCard
import com.alvarocervantes.fittrackplus.core.design.FitTrackProgressBar
import com.alvarocervantes.fittrackplus.core.design.FitTrackSectionLabel
import com.alvarocervantes.fittrackplus.core.design.accentWarm
import com.alvarocervantes.fittrackplus.core.design.primaryDark
import com.alvarocervantes.fittrackplus.core.design.primarySoft
import com.alvarocervantes.fittrackplus.core.design.surfaceAlt
import com.alvarocervantes.fittrackplus.core.design.surfaceCard
import com.alvarocervantes.fittrackplus.core.design.textTertiary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    onGoToRoutines: () -> Unit,
    onGoToWorkout: () -> Unit,
    onGoToHistory: () -> Unit,
    onGoToStats: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    val hasActiveRoutine = uiState.activeRoutineId != null

    LaunchedEffect(uiState.message) {
        val message = uiState.message ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        viewModel.clearMessage()
    }

    val quickActions = listOf(
        HomeQuickAction(
            title = "Preparar rutinas",
            description = "Crea y ajusta tus bloques de entrenamiento antes de empezar la semana.",
            icon = Icons.AutoMirrored.Filled.List,
            accentColor = MaterialTheme.colorScheme.primary,
            accentBackground = MaterialTheme.colorScheme.primarySoft,
            onClick = onGoToRoutines
        ),
        HomeQuickAction(
            title = "Entrenar hoy",
            description = "Si ya tienes una rutina activa, entra al siguiente dia sin romper tu historial.",
            icon = Icons.Filled.PlayArrow,
            accentColor = MaterialTheme.colorScheme.primary,
            accentBackground = MaterialTheme.colorScheme.primarySoft,
            onClick = onGoToWorkout
        ),
        HomeQuickAction(
            title = "Revisar historial",
            description = "Consulta sesiones finalizadas y confirma que el historico sigue siendo fiable.",
            icon = Icons.Filled.History,
            accentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            accentBackground = MaterialTheme.colorScheme.surfaceAlt,
            onClick = onGoToHistory
        ),
        HomeQuickAction(
            title = "Consultar datos",
            description = "Mira volumen, progreso y marcas sin mezclar sesiones abiertas.",
            icon = Icons.Filled.BarChart,
            accentColor = MaterialTheme.colorScheme.secondary,
            accentBackground = MaterialTheme.colorScheme.surfaceAlt,
            onClick = onGoToStats
        )
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = FitSpacing.screenHorizontal),
            verticalArrangement = Arrangement.spacedBy(FitSpacing.card)
        ) {
        item {
            Column(
                modifier = Modifier.padding(top = FitSpacing.screenTop),
                verticalArrangement = Arrangement.spacedBy(FitSpacing.tiny)
            ) {
                Text(
                    text = formatToday(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.textTertiary
                )
                Text(
                    text = greetingForNow(),
                    style = MaterialTheme.typography.displayLarge
                )
                Text(
                    text = "Gestiona rutinas, registra sesiones y conserva tu historial local sin tocar el pasado.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            WeekActivityStrip(
                sessionsThisWeek = uiState.sessionsThisWeek,
                trainedDaysThisWeek = uiState.trainedDaysThisWeek,
                stepsDaysCompleted = uiState.stepsDaysCompleted,
                todaySteps = uiState.todaySteps,
                dailyStepGoal = uiState.dailyStepGoal,
                isLoading = uiState.isLoading
            )
        }

        item {
            Box(
                modifier = Modifier
                    .clip(MaterialTheme.shapes.extraLarge)
                    .background(MaterialTheme.colorScheme.primaryDark)
                    .fillMaxWidth()
                    .padding(FitSpacing.cardPadding)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(FitSpacing.mdLg)
                ) {
                    FitTrackBadge(
                        label = "LOCAL-FIRST",
                        tone = FitTrackBadgeTone.Active
                    )
                    Text(
                        text = "FitTrackPlus",
                        style = MaterialTheme.typography.headlineLarge,
                        color = Color.White
                    )

                    if (uiState.isLoading) {
                        Column(verticalArrangement = Arrangement.spacedBy(FitSpacing.xs)) {
                            SkeletonText(
                                widthFraction = 0.55f,
                                lineHeight = 16.dp
                            )
                            SkeletonBlock(
                                modifier = Modifier
                                    .fillMaxWidth(0.35f)
                                    .height(16.dp),
                                shape = MaterialTheme.shapes.small
                            )
                        }
                    } else if (uiState.totalSessions > 0) {
                        Row(horizontalArrangement = Arrangement.spacedBy(FitSpacing.sm)) {
                            MiniHeroTag(
                                if (uiState.sessionsThisWeek == 0) "Sin sesiones esta semana"
                                else "${uiState.sessionsThisWeek} sesion${if (uiState.sessionsThisWeek > 1) "es" else ""} esta semana"
                            )
                            MiniHeroTag("${uiState.totalSessions} en total")
                        }
                    } else {
                        Text(
                            text = "Crea una rutina, activala y empieza a registrar sesiones.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.72f)
                        )
                    }

                    Button(
                        onClick = if (hasActiveRoutine) onGoToWorkout else onGoToRoutines
                    ) {
                        Icon(
                            imageVector = if (hasActiveRoutine) Icons.Filled.PlayArrow else Icons.AutoMirrored.Filled.List,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = if (hasActiveRoutine) "Ir a entrenar" else "Preparar rutina",
                            modifier = Modifier.padding(start = FitSpacing.sm)
                        )
                    }

                    if (!hasActiveRoutine && !uiState.isLoading) {
                        Text(
                            text = "Crea tu primera rutina y activala para empezar a entrenar.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.55f)
                        )
                    }
                }
            }
        }

        item {
            FitTrackSectionLabel(label = "Accesos rapidos")
        }

        items(quickActions) { action ->
            QuickActionCard(action = action)
        }

        item {
            FitTrackSectionLabel(label = "Recorrido base")
        }

        item {
            FitTrackCard {
                homeSteps().forEachIndexed { index, step ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(FitSpacing.md),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(MaterialTheme.shapes.medium)
                                .background(MaterialTheme.colorScheme.primarySoft),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = (index + 1).toString(),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(FitSpacing.xs)
                        ) {
                            Text(
                                text = step.title,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = step.body,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
        }
    }
}

@Composable
private fun WeekActivityStrip(
    sessionsThisWeek: Int,
    trainedDaysThisWeek: Set<Int>,
    stepsDaysCompleted: Set<Int>,
    todaySteps: Long?,
    dailyStepGoal: Int,
    isLoading: Boolean
) {
    FitTrackCard(containerColor = MaterialTheme.colorScheme.surfaceCard) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(FitSpacing.xs)) {
                Text(
                    text = "Esta semana",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = if (isLoading) {
                        "Calculando actividad"
                    } else {
                        weeklySessionLabel(sessionsThisWeek)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            FitTrackBadge(
                label = if (sessionsThisWeek > 0) "ACTIVA" else "SIN SESION",
                tone = if (sessionsThisWeek > 0) FitTrackBadgeTone.Active else FitTrackBadgeTone.Neutral
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(FitSpacing.xs)
        ) {
            val todayIndex = (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) + 5) % 7
            weekDayLabels().forEachIndexed { index, label ->
                val isToday = index == todayIndex
                val isTrained = index in trainedDaysThisWeek
                val isStepsCompleted = index in stepsDaysCompleted
                WeekDayCell(
                    label = label,
                    isToday = isToday,
                    isTrained = isTrained,
                    isStepsCompleted = isStepsCompleted,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        if (todaySteps != null) {
            StepProgressRow(todaySteps = todaySteps, goal = dailyStepGoal)
        }
    }
}

@Composable
private fun WeekDayCell(
    label: String,
    isToday: Boolean,
    isTrained: Boolean,
    isStepsCompleted: Boolean,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    val (background, textColor) = weekDayCellColors(colorScheme, isToday, isTrained)
    val dotColor = weekDayCellDotColor(colorScheme, isToday, isTrained, isStepsCompleted)
    val hasActivity = isTrained || isStepsCompleted || isToday

    Column(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .background(background)
            .padding(vertical = FitSpacing.sm),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(FitSpacing.xs)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = textColor
        )
        Box(
            modifier = Modifier
                .size(if (hasActivity) 6.dp else 4.dp)
                .clip(MaterialTheme.shapes.extraSmall)
                .background(dotColor)
        )
    }
}

@Composable
private fun weekDayCellColors(
    cs: androidx.compose.material3.ColorScheme,
    isToday: Boolean,
    isTrained: Boolean
): Pair<Color, Color> = when {
    isToday -> cs.primary to cs.onPrimary
    isTrained -> cs.primarySoft to cs.primary
    else -> cs.surfaceAlt to cs.onSurfaceVariant
}

@Composable
private fun weekDayCellDotColor(
    cs: androidx.compose.material3.ColorScheme,
    isToday: Boolean,
    isTrained: Boolean,
    isStepsCompleted: Boolean
): Color = when {
    isToday -> cs.onPrimary
    isStepsCompleted -> cs.accentWarm
    isTrained -> cs.primary
    else -> cs.onSurfaceVariant.copy(alpha = 0.35f)
}

@Composable
private fun StepProgressRow(
    todaySteps: Long,
    goal: Int
) {
    val progress = if (goal > 0) (todaySteps.toFloat() / goal).coerceIn(0f, 1f) else 0f
    val goalReached = todaySteps >= goal

    Column(verticalArrangement = Arrangement.spacedBy(FitSpacing.xs)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (goalReached) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(FitSpacing.xs),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.accentWarm
                    )
                    Text(
                        text = "Objetivo completado",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.accentWarm
                    )
                }
            } else {
                Text(
                    text = "%,d / %,d pasos".format(todaySteps, goal),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.textTertiary
            )
        }
        FitTrackProgressBar(
            progress = progress,
            color = if (goalReached) {
                MaterialTheme.colorScheme.accentWarm
            } else {
                MaterialTheme.colorScheme.tertiary
            },
            contentDescription = "Progreso de pasos"
        )
    }
}

private fun weekDayLabels(): List<String> {
    return listOf("L", "M", "X", "J", "V", "S", "D")
}

private fun weeklySessionLabel(sessionsThisWeek: Int): String {
    val sessionWord = if (sessionsThisWeek == 1) "sesion" else "sesiones"
    val registeredWord = if (sessionsThisWeek == 1) "registrada" else "registradas"
    return "$sessionsThisWeek $sessionWord $registeredWord"
}

@Composable
private fun MiniHeroTag(text: String) {
    Box(
        modifier = Modifier
            .clip(MaterialTheme.shapes.medium)
            .background(Color.White.copy(alpha = 0.10f))
            .padding(horizontal = FitSpacing.smMd, vertical = FitSpacing.tiny)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = Color.White.copy(alpha = 0.78f)
        )
    }
}

@Composable
private fun QuickActionCard(action: HomeQuickAction) {
    FitTrackCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                role = Role.Button,
                onClickLabel = "Abrir ${action.title}",
                onClick = action.onClick
            ),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(FitSpacing.mdLg),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(MaterialTheme.shapes.large)
                    .background(action.accentBackground),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = action.icon,
                    contentDescription = null,
                    tint = action.accentColor
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(FitSpacing.xs)
            ) {
                Text(
                    text = action.title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = action.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private data class HomeQuickAction(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val accentColor: Color,
    val accentBackground: Color,
    val onClick: () -> Unit
)

private data class HomeStep(
    val title: String,
    val body: String
)

private fun homeSteps(): List<HomeStep> {
    return listOf(
        HomeStep(
            title = "Crea una rutina en Rutinas",
            body = "Define dias, ejercicios y repeticiones objetivo para tener un punto de partida claro."
        ),
        HomeStep(
            title = "Marcala como activa",
            body = "Entrenar usara esa rutina para preparar el siguiente dia sin tocar datos pasados."
        ),
        HomeStep(
            title = "Finaliza sesiones para alimentar Historial y Datos",
            body = "Solo las sesiones cerradas entran en historico y estadisticas; las abiertas no se mezclan."
        )
    )
}

private fun greetingForNow(): String {
    val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    return when {
        hour < 13 -> "Buenos dias"
        hour < 20 -> "Buenas tardes"
        else -> "Buenas noches"
    }
}

private fun formatToday(): String {
    return SimpleDateFormat("EEEE, d 'de' MMMM", Locale("es", "ES"))
        .format(Date())
        .replaceFirstChar { char -> char.uppercase() }
}

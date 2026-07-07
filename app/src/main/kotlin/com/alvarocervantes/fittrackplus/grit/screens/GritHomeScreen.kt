package com.alvarocervantes.fittrackplus.grit.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alvarocervantes.fittrackplus.feature.home.HomeViewModel
import com.alvarocervantes.fittrackplus.grit.components.GritCard
import com.alvarocervantes.fittrackplus.grit.components.GritPrimaryButton
import com.alvarocervantes.fittrackplus.grit.components.GritSectionLabel
import com.alvarocervantes.fittrackplus.grit.components.GritToast
import com.alvarocervantes.fittrackplus.grit.theme.GritColors
import com.alvarocervantes.fittrackplus.grit.theme.GritShapes
import com.alvarocervantes.fittrackplus.grit.theme.GritType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val weekDayLetters = listOf("L", "M", "X", "J", "V", "S", "D")

@Composable
fun GritHomeScreen(
    onGoToRoutines: () -> Unit,
    onGoToWorkout: () -> Unit,
    onGoToHistory: () -> Unit,
    onGoToStats: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
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
            GreetingHeader()

            HeroWorkoutCard(
                hasActiveRoutine = uiState.activeRoutineId != null,
                onStartWorkout = onGoToWorkout,
                onCreateRoutine = onGoToRoutines
            )

            WeeklyActivityCard(
                trainedDays = uiState.trainedDaysThisWeek,
                sessionsThisWeek = uiState.sessionsThisWeek
            )

            QuickActionsRow(
                onGoToRoutines = onGoToRoutines,
                onGoToHistory = onGoToHistory,
                onGoToStats = onGoToStats
            )

            if (uiState.todaySteps != null) {
                DailyStepsCard(
                    steps = uiState.todaySteps ?: 0L,
                    goal = uiState.dailyStepGoal
                )
            }
        }

        uiState.message?.let { message ->
            GritToast(
                title = "Aviso",
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
private fun GreetingHeader() {
    val today = remember {
        SimpleDateFormat("EEEE, d 'de' MMMM", Locale("es", "ES"))
            .format(Date())
            .uppercase(Locale("es", "ES"))
    }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(text = "A DARLE CAÑA", style = GritType.screenTitle)
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.CalendarToday,
                contentDescription = null,
                tint = GritColors.Lime,
                modifier = Modifier.size(13.dp)
            )
            Text(text = today, style = GritType.monoLabel)
        }
    }
}

@Composable
private fun HeroWorkoutCard(
    hasActiveRoutine: Boolean,
    onStartWorkout: () -> Unit,
    onCreateRoutine: () -> Unit
) {
    GritCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            GritSectionLabel(
                text = "Próximo entrenamiento",
                color = GritColors.Lime
            )
            Text(
                text = if (hasActiveRoutine) "TU RUTINA TE ESPERA" else "CREA UNA RUTINA",
                style = GritType.cardTitle
            )
            Text(
                text = if (hasActiveRoutine) {
                    "SESIÓN LISTA PARA EMPEZAR"
                } else {
                    "SIN RUTINA ACTIVA TODAVÍA"
                },
                style = GritType.monoLabelSmall
            )
            Spacer(modifier = Modifier.height(16.dp))
            if (hasActiveRoutine) {
                GritPrimaryButton(
                    text = "Iniciar Entrenamiento",
                    icon = Icons.Filled.PlayArrow,
                    onClick = onStartWorkout,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                GritPrimaryButton(
                    text = "Crear Rutina",
                    onClick = onCreateRoutine,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun WeeklyActivityCard(
    trainedDays: Set<Int>,
    sessionsThisWeek: Int
) {
    GritCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                GritSectionLabel(text = "Actividad Semanal")
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.LocalFireDepartment,
                        contentDescription = null,
                        tint = GritColors.Lime,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = "$sessionsThisWeek SESIONES",
                        style = GritType.monoStrong,
                        color = GritColors.Lime
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                weekDayLetters.forEachIndexed { index, letter ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(
                                    if (index in trainedDays) GritColors.Lime else GritColors.Border
                                )
                        )
                        Text(text = letter, style = GritType.monoLabelSmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickActionsRow(
    onGoToRoutines: () -> Unit,
    onGoToHistory: () -> Unit,
    onGoToStats: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        QuickActionTile(
            icon = Icons.AutoMirrored.Filled.List,
            label = "Mis\nRutinas",
            onClick = onGoToRoutines,
            modifier = Modifier.weight(1f)
        )
        QuickActionTile(
            icon = Icons.Filled.History,
            label = "Historial\nCompleto",
            onClick = onGoToHistory,
            modifier = Modifier.weight(1f)
        )
        QuickActionTile(
            icon = Icons.Filled.BarChart,
            label = "Explorar\nDatos",
            onClick = onGoToStats,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun QuickActionTile(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(GritShapes.medium)
            .background(GritColors.Surface)
            .clickable(onClick = onClick)
            .padding(16.dp)
            .height(78.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = GritColors.Lime,
            modifier = Modifier.size(22.dp)
        )
        Text(
            text = label.uppercase(),
            style = GritType.monoLabelSmall,
            color = GritColors.TextPrimary
        )
    }
}

@Composable
private fun DailyStepsCard(
    steps: Long,
    goal: Int
) {
    val progress = if (goal > 0) (steps.toFloat() / goal).coerceIn(0f, 1f) else 0f
    GritCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    GritSectionLabel(text = "Pasos Diarios")
                    Text(
                        text = "%,d / %,d".format(steps, goal),
                        style = GritType.cardTitle
                    )
                }
                Icon(
                    imageVector = Icons.Filled.DirectionsWalk,
                    contentDescription = null,
                    tint = GritColors.Lime,
                    modifier = Modifier.size(24.dp)
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(CircleShape)
                    .background(GritColors.Neutral900)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .height(10.dp)
                        .clip(CircleShape)
                        .background(GritColors.Lime)
                )
            }
            Text(
                text = if (steps >= goal) {
                    "¡OBJETIVO DIARIO CUMPLIDO!"
                } else {
                    "${goal - steps} PASOS RESTANTES"
                },
                style = GritType.monoLabelSmall
            )
        }
    }
}

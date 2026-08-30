package com.alvarocervantes.fittrackplus.feature.widget

import android.content.Context
import android.content.Intent
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.material3.ColorProviders
import com.alvarocervantes.fittrackplus.core.design.DarkColors
import com.alvarocervantes.fittrackplus.core.design.LightColors
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.alvarocervantes.fittrackplus.MainActivity
import com.alvarocervantes.fittrackplus.domain.usecase.GetWorkoutStreakUseCase
import dagger.hilt.android.EntryPointAccessors
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.flow.first

class FitTrackPlusWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            WidgetEntryPoint::class.java
        )
        val repo = entryPoint.workoutRepository()

        val streak = GetWorkoutStreakUseCase(repo).invoke()

        val startOfWeek = LocalDate.now().with(DayOfWeek.MONDAY)
        val weekSessions = repo.observeFinishedSessions().first().count { session ->
            session.finishedAt?.let { ts ->
                val date = Instant.ofEpochMilli(ts)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                !date.isBefore(startOfWeek)
            } ?: false
        }

        val launchIntent = Intent(context, MainActivity::class.java)
        val streakDaysLabel = if (streak == 1) "dia" else "dias"
        val sessionsLabel = if (weekSessions == 1) "sesion" else "sesiones"

        provideContent {
            // Without explicit colours GlanceTheme falls back to the system dynamic palette on
            // API 31+, so the widget rendered in Material You purple next to an emerald app.
            GlanceTheme(colors = FitTrackWidgetColors) {
                Column(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(GlanceTheme.colors.surface)
                        .padding(12.dp)
                        .clickable(actionStartActivity(launchIntent)),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Racha: $streak $streakDaysLabel",
                        style = TextStyle(
                            color = GlanceTheme.colors.primary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    )
                    Text(
                        text = "Esta semana: $weekSessions $sessionsLabel",
                        style = TextStyle(
                            color = GlanceTheme.colors.onSurface,
                            fontSize = 12.sp
                        )
                    )
                }
            }
        }
    }
}

private val FitTrackWidgetColors = ColorProviders(
    light = LightColors,
    dark = DarkColors
)

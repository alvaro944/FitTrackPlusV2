package com.alvarocervantes.fittrackplus.feature.launch

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alvarocervantes.fittrackplus.R
import com.alvarocervantes.fittrackplus.core.navigation.AppRoute
import com.alvarocervantes.fittrackplus.core.navigation.FitTrackPlusNavHost
import com.alvarocervantes.fittrackplus.feature.onboarding.OnboardingScreen
import kotlin.math.roundToInt
import kotlinx.coroutines.delay

// The intro remains 1240 ms: it is short enough to avoid delaying app access while preserving
// the entrance, detail reveal, and exit choreography of the launch transition.
private const val INTRO_DETAILS_DELAY_MILLIS = 260L
private const val INTRO_VISIBLE_DURATION_MILLIS = 760L
private const val INTRO_EXIT_DURATION_MILLIS = 220L

@Composable
fun FitTrackPlusAppRoot(
    hasSeenOnboarding: Boolean,
    onOnboardingComplete: () -> Unit,
    initialTab: AppRoute? = null
) {
    var hasCompletedIntro by rememberSaveable { mutableStateOf(false) }

    when {
        !hasCompletedIntro -> LaunchIntroScreen(onFinished = { hasCompletedIntro = true })
        !hasSeenOnboarding -> OnboardingScreen(onComplete = onOnboardingComplete)
        else -> FitTrackPlusNavHost(initialTab = initialTab)
    }
}

@Composable
private fun LaunchIntroScreen(
    onFinished: () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }
    var showDetails by remember { mutableStateOf(false) }
    val colors = MaterialTheme.colorScheme

    LaunchedEffect(Unit) {
        isVisible = true
        delay(INTRO_DETAILS_DELAY_MILLIS)
        showDetails = true
        delay(INTRO_VISIBLE_DURATION_MILLIS)
        isVisible = false
        delay(INTRO_EXIT_DURATION_MILLIS)
        onFinished()
    }

    val rootAlpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
        label = "introRootAlpha"
    )
    val logoScale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.96f,
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label = "introLogoScale"
    )
    val logoOffsetY by animateFloatAsState(
        targetValue = if (isVisible) 0f else 18f,
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label = "introLogoOffset"
    )
    val detailAlpha by animateFloatAsState(
        targetValue = if (showDetails && isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing),
        label = "introDetailAlpha"
    )
    val loaderProgress by animateFloatAsState(
        targetValue = if (showDetails) 1f else 0f,
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label = "introLoaderProgress"
    )

    val pulseTransition = rememberInfiniteTransition(label = "introPulse")
    val emeraldPulse by pulseTransition.animateFloat(
        initialValue = 0.82f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "introEmeraldPulse"
    )
    val shimmerSweep by pulseTransition.animateFloat(
        initialValue = -220f,
        targetValue = 420f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, delayMillis = 500),
            repeatMode = RepeatMode.Restart
        ),
        label = "introShimmerSweep"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .graphicsLayer { alpha = rootAlpha }
    ) {
        IntroBackgroundLayers(
            modifier = Modifier.fillMaxSize(),
            emeraldPulse = emeraldPulse
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(width = 252.dp, height = 260.dp)
                    .graphicsLayer {
                        scaleX = logoScale
                        scaleY = logoScale
                        alpha = rootAlpha
                        translationY = logoOffsetY
                    },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 138.dp, height = 96.dp)
                        .offset(y = (-50).dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    colors.primary.copy(alpha = 0.26f),
                                    colors.primary.copy(alpha = 0.08f),
                                    Color.Transparent
                                ),
                                radius = 180f
                            ),
                            shape = CircleShape
                        )
                        .graphicsLayer {
                            scaleX = emeraldPulse
                            scaleY = emeraldPulse
                        }
                )

                Image(
                    painter = painterResource(id = R.drawable.launch_logo),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                        .drawWithContent {
                            drawContent()
                            drawRect(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        colors.onBackground.copy(alpha = 0.18f),
                                        Color.Transparent
                                    ),
                                    start = Offset(shimmerSweep, 0f),
                                    end = Offset(shimmerSweep + 120f, size.height)
                                )
                            )
                        }
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer { alpha = detailAlpha },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = buildAnnotatedString {
                        append("FIT")
                        withStyle(style = SpanStyle(color = colors.primary)) {
                            append("TRACK")
                        }
                        append("PLUS")
                    },
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 3.4.sp,
                        color = colors.onBackground
                    ),
                    textAlign = TextAlign.Center
                )

                Box(
                    modifier = Modifier
                        .height(1.dp)
                        .fillMaxWidth(0.18f)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    colors.primary.copy(alpha = 0.45f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                Text(
                    text = "Entrena · Progresa · Supera",
                    style = MaterialTheme.typography.labelMedium.copy(
                        letterSpacing = 2.6.sp,
                        color = colors.onBackground.copy(alpha = 0.42f)
                    ),
                    textAlign = TextAlign.Center
                )

                Box(
                    modifier = Modifier
                        .padding(top = 18.dp)
                        .fillMaxWidth(0.16f)
                        .height(2.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(colors.onBackground.copy(alpha = 0.10f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(loaderProgress.coerceIn(0f, 1f))
                            .height(2.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(colors.primary, colors.tertiary)
                                )
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun IntroBackgroundLayers(
    modifier: Modifier,
    emeraldPulse: Float
) {
    val colors = MaterialTheme.colorScheme

    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            colors.surface,
                            colors.background,
                            colors.surfaceVariant
                        ),
                        center = Offset(540f, 520f),
                        radius = 1200f
                    )
                )
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.Transparent,
                            colors.onBackground.copy(alpha = 0.04f),
                            colors.onBackground.copy(alpha = 0.10f)
                        ),
                        center = Offset(540f, 880f),
                        radius = 1250f
                    )
                )
        )

        Box(
            modifier = Modifier
                .size(width = 360.dp, height = 250.dp)
                .offset {
                    IntOffset(
                        x = 0,
                        y = (-22).dp.roundToPx()
                    )
                }
                .align(Alignment.Center)
                .graphicsLayer {
                    scaleX = emeraldPulse
                    scaleY = emeraldPulse
                }
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            colors.primary.copy(alpha = 0.10f),
                            colors.primary.copy(alpha = 0.03f),
                            Color.Transparent
                        ),
                        radius = 420f
                    ),
                    shape = CircleShape
                )
        )

        Box(
            modifier = Modifier
                .size(width = 276.dp, height = 96.dp)
                .offset(y = 40.dp)
                .align(Alignment.Center)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            colors.secondary.copy(alpha = 0.08f),
                            Color.Transparent
                        ),
                        radius = 220f
                    ),
                    shape = CircleShape
                )
        )
    }
}

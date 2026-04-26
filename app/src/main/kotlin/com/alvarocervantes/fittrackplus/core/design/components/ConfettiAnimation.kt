package com.alvarocervantes.fittrackplus.core.design.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private const val PARTICLE_COUNT = 40
private const val DURATION_MS = 1500

private data class Particle(
    val startX: Float,       // fraction of width [0..1]
    val startY: Float,       // fraction of height; spawn slightly above top
    val velocityX: Float,    // fraction/s horizontal drift
    val angle: Float,        // initial rotation in degrees
    val rotationSpeed: Float,// degrees/progress unit
    val size: Float,         // dp-like px, set at draw time
    val color: Color
)

@Composable
fun ConfettiAnimation(
    modifier: Modifier = Modifier,
    colors: List<Color> = defaultConfettiColors,
    durationMs: Int = DURATION_MS,
    onFinished: () -> Unit = {}
) {
    val progress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = durationMs)
        )
        onFinished()
    }

    val particles = remember(colors) {
        val rng = Random(42)
        List(PARTICLE_COUNT) { i ->
            Particle(
                startX = rng.nextFloat(),
                startY = -0.05f - rng.nextFloat() * 0.1f,
                velocityX = (rng.nextFloat() - 0.5f) * 0.3f,
                angle = rng.nextFloat() * 360f,
                rotationSpeed = (rng.nextFloat() - 0.5f) * 720f,
                size = 8f + rng.nextFloat() * 12f,
                color = colors[i % colors.size]
            )
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val p = progress.value
        val gravity = p * p  // simulate gravity: starts slow, accelerates

        particles.forEach { particle ->
            val x = (particle.startX + particle.velocityX * p) * size.width
            val y = (particle.startY + gravity * 1.15f) * size.height
            if (y > size.height) return@forEach  // off-screen

            val alpha = if (p > 0.75f) 1f - ((p - 0.75f) / 0.25f) else 1f
            val rotation = particle.angle + particle.rotationSpeed * p

            rotate(degrees = rotation, pivot = Offset(x, y)) {
                drawRect(
                    color = particle.color.copy(alpha = alpha),
                    topLeft = Offset(x - particle.size / 2, y - particle.size / 2),
                    size = Size(particle.size, particle.size * 0.5f)
                )
            }
        }
    }
}

private val defaultConfettiColors = listOf(
    Color(0xFF4CAF50),
    Color(0xFF2196F3),
    Color(0xFFFF9800),
    Color(0xFFE91E63),
    Color(0xFF9C27B0),
    Color(0xFFFFEB3B)
)

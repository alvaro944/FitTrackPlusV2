package com.alvarocervantes.fittrackplus.core.design.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.alvarocervantes.fittrackplus.core.design.FitSpacing
import com.alvarocervantes.fittrackplus.core.design.FitTrackCard

/**
 * Bloque rectangular con efecto shimmer. Úsalo como building block para componer
 * skeletons de cards más complejas.
 *
 * @param modifier incluir al menos width + height para que sea visible.
 * @param shape forma con la que se recorta el shimmer (por defecto redondeado medio).
 */
@Composable
fun SkeletonBlock(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.medium
) {
    Box(
        modifier = modifier
            .clip(shape)
            .shimmer()
    )
}

/**
 * Línea de texto skeleton — barra horizontal de altura fija que simula una línea de texto.
 *
 * @param widthFraction fracción del ancho padre [0..1].
 * @param lineHeight altura de la barra (por defecto 14.dp, equivalente a bodyMedium).
 */
@Composable
fun SkeletonText(
    widthFraction: Float = 1f,
    lineHeight: Dp = 14.dp,
    modifier: Modifier = Modifier
) {
    SkeletonBlock(
        modifier = modifier
            .fillMaxWidth(widthFraction)
            .height(lineHeight),
        shape = MaterialTheme.shapes.small
    )
}

/**
 * Wrapper de skeleton que replica la estructura visual de [FitTrackCard]:
 * mismo borde, padding y fondo. Úsalo para encapsular contenido skeleton
 * manteniendo la misma apariencia que una card real.
 */
@Composable
fun SkeletonCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    FitTrackCard(modifier = modifier, content = content)
}

/**
 * Skeleton generico de fila de lista: icono/marcador circular + dos lineas de texto
 * (titulo y subtitulo). Pensado para listas de tarjetas simples (sesiones, ejercicios,
 * accesos rapidos) donde no hace falta una forma mas especifica por pantalla.
 */
@Composable
fun SkeletonListItem(modifier: Modifier = Modifier) {
    SkeletonCard(modifier = modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(FitSpacing.xs)) {
            SkeletonBlock(
                modifier = Modifier
                    .width(40.dp)
                    .height(40.dp),
                shape = MaterialTheme.shapes.medium
            )
            SkeletonText(widthFraction = 0.55f, lineHeight = 18.dp)
            SkeletonText(widthFraction = 0.35f)
        }
    }
}

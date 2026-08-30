package com.alvarocervantes.fittrackplus.core.design.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.alvarocervantes.fittrackplus.core.design.FitSpacing

@Composable
fun FitTrackStepper(
    value: String,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    modifier: Modifier = Modifier,
    onLongIncrement: (() -> Unit)? = null,
    onLongDecrement: (() -> Unit)? = null,
    compact: Boolean = false,
    decrementEnabled: Boolean = true,
    incrementEnabled: Boolean = true,
    spacing: Dp = if (compact) FitSpacing.tiny else FitSpacing.sm,
    decrementContentDescription: String = "Reducir",
    incrementContentDescription: String = "Aumentar",
    buttonContainer: Boolean = false,
    content: (@Composable RowScope.() -> Unit)? = null,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(spacing),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FitTrackStepperButton(
            icon = Icons.Filled.Remove,
            contentDescription = decrementContentDescription,
            onClick = onDecrement,
            onLongClick = onLongDecrement,
            compact = compact,
            container = buttonContainer,
            enabled = decrementEnabled
        )
        if (content != null) {
            content()
        } else {
            Text(
                text = value,
                style = if (compact) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.titleLarge
            )
        }
        FitTrackStepperButton(
            icon = Icons.Filled.Add,
            contentDescription = incrementContentDescription,
            onClick = onIncrement,
            onLongClick = onLongIncrement,
            compact = compact,
            container = buttonContainer,
            enabled = incrementEnabled
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FitTrackStepperButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
    compact: Boolean = false,
    container: Boolean = false,
    enabled: Boolean = true,
) {
    val visualSize: Dp = if (compact) 28.dp else 36.dp
    val iconSize: Dp = if (compact) 16.dp else 24.dp
    // Compact steppers sit two-per-row flanking a numeric field (workout weight/reps), which
    // has Modifier.weight(1f) and shrinks to make room. The full 48dp Material touch target
    // (minimumInteractiveComponentSize) takes 20dp more per button than the 28dp visual size,
    // 40dp total, and was found to make the number field too narrow to read comfortably. 40dp
    // is a middle ground: a real improvement over the untouched 28dp, without eating as much of
    // the field's width. Only the roomy non-compact steppers (routine editor, settings) get the
    // full 48dp minimum.
    val sizeModifier: Modifier = if (compact) {
        Modifier.sizeIn(minWidth = 40.dp, minHeight = 40.dp)
    } else {
        Modifier.minimumInteractiveComponentSize()
    }
    Box(
        modifier = modifier
            .then(sizeModifier)
            .combinedClickable(
                enabled = enabled,
                onClick = onClick,
                onLongClick = onLongClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (container) {
                Box(
                    modifier = Modifier
                        .size(visualSize)
                        .background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.medium)
                )
            }
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = if (enabled) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                },
                modifier = Modifier.size(iconSize)
            )
        }
    }
}

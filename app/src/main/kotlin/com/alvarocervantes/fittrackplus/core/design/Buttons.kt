package com.alvarocervantes.fittrackplus.core.design

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

/** Primary filled action button for main calls to action. */
@Composable
fun FitTrackPrimaryButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled
    ) {
        FitTrackButtonContent(label = label, icon = icon)
    }
}

/** Tonal secondary action button for lower-emphasis actions. */
@Composable
fun FitTrackTonalButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null
) {
    FilledTonalButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled
    ) {
        FitTrackButtonContent(label = label, icon = icon)
    }
}

/** Outlined tertiary action button, with optional destructive emphasis. */
@Composable
fun FitTrackOutlinedButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    destructive: Boolean = false
) {
    if (destructive) {
        OutlinedButton(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            )
        ) {
            FitTrackButtonContent(label = label, icon = icon)
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled
        ) {
            FitTrackButtonContent(label = label, icon = icon)
        }
    }
}

/** Outlined add action button for adding routine editor items. */
@Composable
fun FitTrackAddButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    FitTrackOutlinedButton(
        label = label,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        icon = Icons.Default.Add
    )
}

@Composable
private fun FitTrackButtonContent(
    label: String,
    icon: ImageVector?
) {
    if (icon != null) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = label,
            modifier = Modifier.padding(start = FitSpacing.sm)
        )
    } else {
        Text(label)
    }
}

package com.alvarocervantes.fittrackplus.core.design.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.alvarocervantes.fittrackplus.core.design.AppThemeMode
import com.alvarocervantes.fittrackplus.core.design.FitSpacing
import com.alvarocervantes.fittrackplus.core.design.primarySoft
import com.alvarocervantes.fittrackplus.core.design.textTertiary

@Composable
fun FitTrackThemeModeSelector(
    selected: AppThemeMode,
    onSelect: (AppThemeMode) -> Unit,
    modifier: Modifier = Modifier,
    showRadio: Boolean = true,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(FitSpacing.sm)
    ) {
        AppThemeMode.entries.forEach { mode ->
            ThemeModeTile(
                mode = mode,
                selected = selected == mode,
                onClick = { onSelect(mode) },
                showRadio = showRadio,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ThemeModeTile(
    mode: AppThemeMode,
    selected: Boolean,
    onClick: () -> Unit,
    showRadio: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(MaterialTheme.shapes.large)
            .background(
                if (selected) {
                    MaterialTheme.colorScheme.primarySoft
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
            )
            .then(
                if (showRadio) {
                    Modifier.border(
                        width = 1.dp,
                        color = if (selected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outline
                        },
                        shape = MaterialTheme.shapes.large
                    )
                } else {
                    Modifier
                }
            )
            .clickable(
                role = Role.RadioButton,
                onClickLabel = "Seleccionar tema ${mode.label}",
                onClick = onClick
            )
            .padding(if (showRadio) FitSpacing.sm else FitSpacing.md),
        verticalArrangement = Arrangement.spacedBy(FitSpacing.sm),
        horizontalAlignment = Alignment.Start
    ) {
        if (showRadio) {
            ThemeModeRadio(selected = selected)
        } else {
            Icon(
                imageVector = mode.icon(),
                contentDescription = null,
                tint = if (selected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
        Text(
            text = mode.label,
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface
            }
        )
        if (showRadio) {
            Text(
                text = mode.shortLabel().uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.textTertiary
            )
        }
    }
}

@Composable
private fun ThemeModeRadio(selected: Boolean) {
    Box(
        modifier = Modifier
            .size(22.dp)
            .clip(CircleShape)
            .background(
                if (selected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.surface
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        if (selected) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onPrimary)
            )
        }
    }
}

private fun AppThemeMode.shortLabel(): String = when (this) {
    AppThemeMode.System -> "Auto"
    AppThemeMode.Light -> "Claro"
    AppThemeMode.Dark -> "Oscuro"
}

private fun AppThemeMode.icon(): ImageVector = when (this) {
    AppThemeMode.System -> Icons.Filled.Smartphone
    AppThemeMode.Light -> Icons.Filled.LightMode
    AppThemeMode.Dark -> Icons.Filled.DarkMode
}

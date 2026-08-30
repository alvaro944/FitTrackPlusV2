package com.alvarocervantes.fittrackplus.core.design

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/** Shared move, duplicate, and remove actions for routine-editor items. */
@Composable
fun FitTrackReorderActions(
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    canRemove: Boolean,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onDuplicate: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
    moveUpContentDescription: String = "Subir",
    moveDownContentDescription: String = "Bajar",
    duplicateContentDescription: String = "Duplicar",
    removeContentDescription: String = "Quitar",
    extraAction: (@Composable RowScope.() -> Unit)? = null
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(FitSpacing.xs)
    ) {
        IconButton(
            onClick = onMoveUp,
            enabled = canMoveUp,
            modifier = Modifier.minimumInteractiveComponentSize()
        ) {
            Icon(
                imageVector = Icons.Filled.KeyboardArrowUp,
                contentDescription = moveUpContentDescription
            )
        }
        IconButton(
            onClick = onMoveDown,
            enabled = canMoveDown,
            modifier = Modifier.minimumInteractiveComponentSize()
        ) {
            Icon(
                imageVector = Icons.Filled.KeyboardArrowDown,
                contentDescription = moveDownContentDescription
            )
        }
        IconButton(
            onClick = onDuplicate,
            modifier = Modifier.minimumInteractiveComponentSize()
        ) {
            Icon(
                imageVector = Icons.Filled.ContentCopy,
                contentDescription = duplicateContentDescription
            )
        }
        extraAction?.invoke(this)
        IconButton(
            onClick = onRemove,
            enabled = canRemove,
            modifier = Modifier.minimumInteractiveComponentSize()
        ) {
            Icon(
                imageVector = Icons.Filled.Delete,
                contentDescription = removeContentDescription
            )
        }
    }
}

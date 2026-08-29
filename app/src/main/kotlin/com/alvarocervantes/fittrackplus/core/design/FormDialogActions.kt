package com.alvarocervantes.fittrackplus.core.design

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/** Standard cancel/confirm action row for form content shown in a [FitTrackDialog]. */
@Composable
fun FitTrackFormDialogActions(
    cancelLabel: String,
    confirmLabel: String,
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
    confirmEnabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        TextButton(onClick = onCancel) {
            Text(cancelLabel)
        }
        TextButton(
            onClick = onConfirm,
            enabled = confirmEnabled
        ) {
            Text(confirmLabel)
        }
    }
}

package com.alvarocervantes.fittrackplus.core.design

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.window.Dialog
import com.alvarocervantes.fittrackplus.core.design.components.DisableNativeTextToolbar
import com.alvarocervantes.fittrackplus.core.design.components.maybeSelectAllOnFocusValue
import com.alvarocervantes.fittrackplus.core.design.components.syncTextFieldValue

/** Standard two-button confirmation dialog. */
@Composable
fun FitTrackConfirmDialog(
    title: String,
    text: String,
    confirmLabel: String,
    dismissLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    onDismissRequest: () -> Unit = onDismiss,
    confirmEnabled: Boolean = true,
    destructive: Boolean = false,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(title) },
        text = { Text(text) },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = confirmEnabled,
                colors = if (destructive) {
                    ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                } else {
                    ButtonDefaults.textButtonColors()
                }
            ) {
                Text(confirmLabel)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(dismissLabel)
            }
        }
    )
}

/** Dialog with a single text field and confirm/dismiss actions. */
@Composable
fun FitTrackInputDialog(
    title: String,
    value: String,
    onValueChange: (String) -> Unit,
    confirmLabel: String,
    dismissLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    label: String? = null,
    supportingText: String? = null,
    placeholder: String? = null,
    isError: Boolean = false,
    // Free prose is the common case for this dialog, so sentence casing is the sane default.
    // Callers editing names or numbers override it.
    keyboardOptions: KeyboardOptions = KeyboardOptions(
        capitalization = KeyboardCapitalization.Sentences,
        imeAction = ImeAction.Done
    ),
    singleLine: Boolean = true,
    minLines: Int = 1,
    maxLines: Int = Int.MAX_VALUE,
    confirmEnabled: Boolean = true,
    selectAllOnFocus: Boolean = false,
    maxLength: Int? = null,
    extraContent: (@Composable ColumnScope.() -> Unit)? = null,
) {
    var fieldValue by remember { mutableStateOf(TextFieldValue(value)) }
    val interactionSource = remember { MutableInteractionSource() }

    LaunchedEffect(value) {
        fieldValue = syncTextFieldValue(fieldValue, value)
    }

    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { interaction ->
            if (interaction is PressInteraction.Release) {
                fieldValue = maybeSelectAllOnFocusValue(fieldValue, selectAllOnFocus)
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(FitSpacing.sm)) {
                DisableNativeTextToolbar {
                    OutlinedTextField(
                        value = fieldValue,
                        onValueChange = { newValue ->
                            val limited = if (maxLength != null && newValue.text.length > maxLength) {
                                newValue.copy(text = newValue.text.take(maxLength))
                            } else {
                                newValue
                            }
                            fieldValue = limited
                            onValueChange(limited.text)
                        },
                        label = label?.let { { Text(it) } },
                        placeholder = placeholder?.let { { Text(it) } },
                        supportingText = supportingText?.let { { Text(it) } },
                        isError = isError,
                        keyboardOptions = keyboardOptions,
                        singleLine = singleLine,
                        minLines = minLines,
                        maxLines = maxLines,
                        interactionSource = interactionSource,
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { focusState ->
                                if (focusState.isFocused) {
                                    fieldValue = maybeSelectAllOnFocusValue(fieldValue, selectAllOnFocus)
                                }
                            }
                    )
                }
                extraContent?.invoke(this)
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = confirmEnabled
            ) {
                Text(confirmLabel)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(dismissLabel)
            }
        }
    )
}

/** Generic rich dialog shell with a fixed header and scrollable content. */
@Composable
fun FitTrackDialog(
    title: String,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    showCloseButton: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
    actions: (@Composable RowScope.() -> Unit)? = null,
) {
    Dialog(onDismissRequest = onDismissRequest) {
        BoxWithConstraints {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = FitElevation.dialog,
                modifier = modifier
                    .fillMaxWidth()
                    .heightIn(max = maxHeight * 0.85f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(FitSpacing.cardPadding),
                    verticalArrangement = Arrangement.spacedBy(FitSpacing.md)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.weight(1f)
                        )
                        if (showCloseButton) {
                            IconButton(
                                onClick = onDismissRequest,
                                modifier = Modifier.minimumInteractiveComponentSize()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Cerrar dialogo"
                                )
                            }
                        }
                    }
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(FitSpacing.md),
                        content = content
                    )
                    if (actions != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically,
                            content = actions
                        )
                    }
                }
            }
        }
    }
}

package com.alvarocervantes.fittrackplus.core.design.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

/** Selects the entire current value so the next keystroke replaces it instead of appending to it. */
fun selectAllOnFocusValue(current: TextFieldValue): TextFieldValue {
    return current.copy(selection = TextRange(0, current.text.length))
}

/** Applies select-all behavior only for fields where replacing the full value is desired. */
fun maybeSelectAllOnFocusValue(current: TextFieldValue, selectAllOnFocus: Boolean): TextFieldValue {
    return if (selectAllOnFocus) selectAllOnFocusValue(current) else current
}

/** Mirrors an external string into a [TextFieldValue], keeping the cursor at the end when the text changes externally. */
fun syncTextFieldValue(current: TextFieldValue, externalText: String): TextFieldValue {
    return if (current.text == externalText) {
        current
    } else {
        TextFieldValue(text = externalText, selection = TextRange(externalText.length, externalText.length))
    }
}

/**
 * OutlinedTextField wrapper that selects all of its text when focused or tapped, so typing
 * over an existing value (e.g. "8" -> "10") replaces it instead of concatenating into it.
 * Set [selectAllOnFocus] to false for free-text fields where caret placement is preferable.
 */
@Composable
fun FitTrackSelectAllTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    supportingText: @Composable (() -> Unit)? = null,
    singleLine: Boolean = true,
    minLines: Int = 1,
    selectAllOnFocus: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    maxLength: Int? = null,
    colors: TextFieldColors = OutlinedTextFieldDefaults.colors()
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
            modifier = modifier.onFocusChanged { focusState ->
                if (focusState.isFocused) {
                    fieldValue = maybeSelectAllOnFocusValue(fieldValue, selectAllOnFocus)
                }
            },
            label = label,
            placeholder = placeholder,
            isError = isError,
            supportingText = supportingText,
            singleLine = singleLine,
            minLines = minLines,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            colors = colors,
            interactionSource = interactionSource
        )
    }
}

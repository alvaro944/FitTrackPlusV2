package com.alvarocervantes.fittrackplus.core.design

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.alvarocervantes.fittrackplus.core.design.components.DisableNativeTextToolbar
import com.alvarocervantes.fittrackplus.core.design.components.FitTrackSelectAllTextField
import com.alvarocervantes.fittrackplus.core.design.components.FitTrackStepper
import com.alvarocervantes.fittrackplus.core.design.components.selectAllOnFocusValue
import com.alvarocervantes.fittrackplus.core.design.components.syncTextFieldValue

enum class FitTrackSetRowMode {
    Edit,
    ReadOnly
}

enum class FitTrackSetRowEditFieldStyle {
    Stepper,
    TextField
}

@Composable
fun FitTrackSetRow(
    setId: Long,
    setNumber: Int,
    weightText: String,
    repsText: String,
    mode: FitTrackSetRowMode,
    modifier: Modifier = Modifier,
    notes: String? = null,
    isCompleted: Boolean = false,
    isReadyToComplete: Boolean = false,
    showCompletionControl: Boolean = false,
    editFieldStyle: FitTrackSetRowEditFieldStyle = FitTrackSetRowEditFieldStyle.Stepper,
    weightUnitLabel: String = "kg",
    previousWeight: String? = null,
    previousReps: Int? = null,
    onWeightChange: (String) -> Unit = {},
    onRepsChange: (String) -> Unit = {},
    onNotesChange: (String) -> Unit = {},
    onComplete: () -> Unit = {},
    onStepWeight: (Double) -> Unit = {},
    onStepReps: (Int) -> Unit = {},
    footer: (@Composable ColumnScope.() -> Unit)? = null
) {
    val isWorkoutStyle = mode == FitTrackSetRowMode.Edit && showCompletionControl
    val background = if (isWorkoutStyle) {
        when {
            isCompleted -> MaterialTheme.colorScheme.primarySoft
            isReadyToComplete -> MaterialTheme.colorScheme.primarySoft.copy(alpha = 0.45f)
            else -> MaterialTheme.colorScheme.surfaceAlt
        }
    } else {
        MaterialTheme.colorScheme.surfaceAlt
    }
    val borderColor = if (isWorkoutStyle && isReadyToComplete) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
    } else {
        Color.Transparent
    }

    if (isWorkoutStyle) {
        Column(modifier = modifier.fillMaxWidth()) {
            SetRowContent(
                setId = setId,
                setNumber = setNumber,
                weightText = weightText,
                repsText = repsText,
                mode = mode,
                isCompleted = isCompleted,
                isReadyToComplete = isReadyToComplete,
                showCompletionControl = true,
                editFieldStyle = editFieldStyle,
                weightUnitLabel = weightUnitLabel,
                previousWeight = previousWeight,
                previousReps = previousReps,
                onWeightChange = onWeightChange,
                onRepsChange = onRepsChange,
                onComplete = onComplete,
                onStepWeight = onStepWeight,
                onStepReps = onStepReps,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(background, MaterialTheme.shapes.large)
                    .border(1.dp, borderColor, MaterialTheme.shapes.large)
                    .padding(FitSpacing.smMd)
            )
            FitTrackSelectAllTextField(
                value = notes.orEmpty(),
                onValueChange = onNotesChange,
                label = { Text("Notas") },
                singleLine = false,
                minLines = 2,
                selectAllOnFocus = false,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = FitSpacing.xs)
            )
            footer?.invoke(this)
        }
    } else {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .background(background, MaterialTheme.shapes.large)
                .padding(FitSpacing.smMd),
            verticalArrangement = Arrangement.spacedBy(FitSpacing.xs)
        ) {
            SetRowContent(
                setId = setId,
                setNumber = setNumber,
                weightText = weightText,
                repsText = repsText,
                mode = mode,
                isCompleted = isCompleted,
                isReadyToComplete = isReadyToComplete,
                showCompletionControl = false,
                editFieldStyle = editFieldStyle,
                weightUnitLabel = weightUnitLabel,
                previousWeight = previousWeight,
                previousReps = previousReps,
                onWeightChange = onWeightChange,
                onRepsChange = onRepsChange,
                onComplete = onComplete,
                onStepWeight = onStepWeight,
                onStepReps = onStepReps,
                modifier = Modifier.fillMaxWidth()
            )
            notes?.takeIf { it.isNotBlank() }?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            footer?.invoke(this)
        }
    }
}

@Composable
private fun SetRowContent(
    setId: Long,
    setNumber: Int,
    weightText: String,
    repsText: String,
    mode: FitTrackSetRowMode,
    isCompleted: Boolean,
    isReadyToComplete: Boolean,
    showCompletionControl: Boolean,
    editFieldStyle: FitTrackSetRowEditFieldStyle,
    weightUnitLabel: String,
    previousWeight: String?,
    previousReps: Int?,
    onWeightChange: (String) -> Unit,
    onRepsChange: (String) -> Unit,
    onComplete: () -> Unit,
    onStepWeight: (Double) -> Unit,
    onStepReps: (Int) -> Unit,
    modifier: Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(if (showCompletionControl) FitSpacing.sm else FitSpacing.md),
        verticalAlignment = if (showCompletionControl) Alignment.Top else Alignment.CenterVertically
    ) {
        if (showCompletionControl) {
            SetCompletionControl(
                setNumber = setNumber,
                isCompleted = isCompleted,
                isReadyToComplete = isReadyToComplete,
                onClick = onComplete
            )
        } else {
            SetNumberBadge(setNumber)
        }

        when (mode) {
            FitTrackSetRowMode.Edit -> when (editFieldStyle) {
                FitTrackSetRowEditFieldStyle.Stepper -> {
                    SetRowWeightField(
                        setId = setId,
                        weightText = weightText,
                        previousWeight = previousWeight,
                        isCompleted = isCompleted,
                        weightUnitLabel = weightUnitLabel,
                        onWeightChange = onWeightChange,
                        onStepWeight = onStepWeight,
                        modifier = Modifier.weight(1.15f)
                    )
                    SetRowRepsField(
                        setId = setId,
                        setNumber = setNumber,
                        repsText = repsText,
                        previousReps = previousReps,
                        isCompleted = isCompleted,
                        onRepsChange = onRepsChange,
                        onStepReps = onStepReps,
                        modifier = Modifier.weight(1f)
                    )
                }

                FitTrackSetRowEditFieldStyle.TextField -> {
                    FitTrackSelectAllTextField(
                        value = weightText,
                        onValueChange = onWeightChange,
                        label = { Text(weightUnitLabel) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                    FitTrackSelectAllTextField(
                        value = repsText,
                        onValueChange = onRepsChange,
                        label = { Text("reps") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            FitTrackSetRowMode.ReadOnly -> {
                Text(
                    text = weightText,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = repsText,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun SetNumberBadge(setNumber: Int) {
    Box(
        modifier = Modifier
            .size(30.dp)
            .background(MaterialTheme.colorScheme.surface, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = setNumber.toString(),
            style = MaterialTheme.typography.labelMedium
        )
    }
}

@Composable
private fun SetCompletionControl(
    setNumber: Int,
    isCompleted: Boolean,
    isReadyToComplete: Boolean,
    onClick: () -> Unit
) {
    val contentDescription = when {
        isCompleted -> "Serie $setNumber completada"
        isReadyToComplete -> "Completar serie $setNumber"
        else -> "Serie $setNumber pendiente"
    }

    FitTrackIconBadge(
        variant = if (isCompleted) {
            FitTrackIconBadgeVariant.Icon(Icons.Filled.Check)
        } else {
            FitTrackIconBadgeVariant.Number(setNumber.toString())
        },
        tone = if (isCompleted) FitTrackIconBadgeTone.Filled else FitTrackIconBadgeTone.Outlined,
        modifier = Modifier
            .padding(top = 6.dp)
            .clickable(enabled = isReadyToComplete, onClick = onClick)
            .semantics { this.contentDescription = contentDescription }
    )
}

@Composable
private fun SetRowWeightField(
    setId: Long,
    weightText: String,
    previousWeight: String?,
    isCompleted: Boolean,
    weightUnitLabel: String,
    onWeightChange: (String) -> Unit,
    onStepWeight: (Double) -> Unit,
    modifier: Modifier
) {
    var fieldValue by remember(setId) { mutableStateOf(TextFieldValue(weightText)) }
    val interactionSource = remember { MutableInteractionSource() }

    LaunchedEffect(weightText) {
        fieldValue = syncTextFieldValue(fieldValue, weightText)
    }
    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { interaction ->
            if (interaction is PressInteraction.Release) {
                fieldValue = selectAllOnFocusValue(fieldValue)
            }
        }
    }

    Column(modifier = modifier) {
        FitTrackStepper(
            value = weightText,
            onIncrement = { onStepWeight(2.5) },
            onDecrement = { onStepWeight(-2.5) },
            onLongIncrement = { onStepWeight(5.0) },
            onLongDecrement = { onStepWeight(-5.0) },
            compact = true,
            spacing = FitSpacing.xs,
            decrementContentDescription = "Bajar peso de la serie $setId",
            incrementContentDescription = "Subir peso de la serie $setId",
            buttonContainer = true
        ) {
            DisableNativeTextToolbar {
                OutlinedTextField(
                    value = fieldValue,
                    onValueChange = {
                        fieldValue = it
                        onWeightChange(it.text)
                    },
                    placeholder = { Text(weightUnitLabel) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = setFieldColors(isCompleted),
                    interactionSource = interactionSource,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 56.dp)
                        .onFocusChanged {
                            if (it.isFocused) fieldValue = selectAllOnFocusValue(fieldValue)
                        }
                )
            }
        }
        previousWeight?.let {
            Text(
                text = "ant. $it $weightUnitLabel",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.padding(start = 4.dp, top = 2.dp)
            )
        }
    }
}

@Composable
private fun SetRowRepsField(
    setId: Long,
    setNumber: Int,
    repsText: String,
    previousReps: Int?,
    isCompleted: Boolean,
    onRepsChange: (String) -> Unit,
    onStepReps: (Int) -> Unit,
    modifier: Modifier
) {
    var fieldValue by remember(setId) { mutableStateOf(TextFieldValue(repsText)) }
    val interactionSource = remember { MutableInteractionSource() }

    LaunchedEffect(repsText) {
        fieldValue = syncTextFieldValue(fieldValue, repsText)
    }
    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { interaction ->
            if (interaction is PressInteraction.Release) {
                fieldValue = selectAllOnFocusValue(fieldValue)
            }
        }
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(FitSpacing.xs)
    ) {
        FitTrackStepper(
            value = repsText,
            onIncrement = { onStepReps(1) },
            onDecrement = { onStepReps(-1) },
            compact = true,
            decrementContentDescription = "Bajar repeticiones de la serie $setNumber",
            incrementContentDescription = "Subir repeticiones de la serie $setNumber",
            buttonContainer = true
        ) {
            DisableNativeTextToolbar {
                OutlinedTextField(
                    value = fieldValue,
                    onValueChange = {
                        fieldValue = it
                        onRepsChange(it.text)
                    },
                    placeholder = { Text("Reps") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = setFieldColors(isCompleted),
                    interactionSource = interactionSource,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 56.dp)
                        .onFocusChanged {
                            if (it.isFocused) fieldValue = selectAllOnFocusValue(fieldValue)
                        }
                )
            }
        }
        previousReps?.let {
            Text(
                text = "ant. $it",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}

@Composable
private fun setFieldColors(isCompleted: Boolean) = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
    focusedLabelColor = MaterialTheme.colorScheme.primary,
    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
    focusedTextColor = if (isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
    unfocusedTextColor = if (isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
    cursorColor = MaterialTheme.colorScheme.primary,
    focusedContainerColor = Color.Transparent,
    unfocusedContainerColor = Color.Transparent
)

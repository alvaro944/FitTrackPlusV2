package com.alvarocervantes.fittrackplus.core.design

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.alvarocervantes.fittrackplus.core.design.components.FitTrackStepper

private val targetRepsPresetOptions = listOf("5", "6-8", "8-12", "10-15")
private const val TARGET_SETS_LONG_PRESS_STEP = 5

/** Shared target sets and reps selector for routine exercises and alternatives. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FitTrackTargetPrescriptionFields(
    targetSets: String,
    targetRepsText: String,
    onTargetSetsChange: (String) -> Unit,
    onTargetRepsChange: (String) -> Unit,
    isValidTargetReps: (String) -> Boolean,
    targetSetsError: String? = null,
    targetRepsError: String? = null,
    modifier: Modifier = Modifier
) {
    val currentSets = targetSets.toIntOrNull()?.coerceIn(1, 99) ?: 3
    val hasCustomReps = targetRepsText !in targetRepsPresetOptions
    var showCustomRepsDialog by remember { mutableStateOf(false) }
    var customRepsDraft by remember { mutableStateOf("") }

    if (showCustomRepsDialog) {
        val customRepsError = customRepsDraft
            .takeIf { it.isNotBlank() }
            ?.let { draft ->
                if (isValidTargetReps(draft)) null else "Usa 8, 8-12, AMRAP o RPE 8."
            }
        FitTrackInputDialog(
            title = "Reps personalizadas",
            value = customRepsDraft,
            onValueChange = { customRepsDraft = it },
            label = "Valor personalizado",
            placeholder = "12-15 o AMRAP",
            supportingText = customRepsError,
            isError = customRepsError != null,
            confirmLabel = "Guardar",
            dismissLabel = "Cancelar",
            onConfirm = {
                onTargetRepsChange(customRepsDraft.trim())
                showCustomRepsDialog = false
            },
            onDismiss = { showCustomRepsDialog = false },
            confirmEnabled = isValidTargetReps(customRepsDraft),
            selectAllOnFocus = true
        )
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(FitSpacing.xs)
    ) {
        Text(
            text = "Series",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            Box(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.large)
                    .padding(horizontal = FitSpacing.sm, vertical = FitSpacing.xs)
            ) {
                FitTrackStepper(
                    value = currentSets.toString(),
                    onDecrement = {
                        onTargetSetsChange((currentSets - 1).coerceAtLeast(1).toString())
                    },
                    onIncrement = {
                        onTargetSetsChange((currentSets + 1).coerceAtMost(99).toString())
                    },
                    onLongDecrement = {
                        onTargetSetsChange((currentSets - TARGET_SETS_LONG_PRESS_STEP).coerceAtLeast(1).toString())
                    },
                    onLongIncrement = {
                        onTargetSetsChange((currentSets + TARGET_SETS_LONG_PRESS_STEP).coerceAtMost(99).toString())
                    },
                    decrementEnabled = currentSets > 1,
                    incrementEnabled = currentSets < 99
                )
            }
        }
        targetSetsError?.let { error ->
            Text(
                text = error,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }

        Text(
            text = "Reps objetivo",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(FitSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(FitSpacing.sm)
        ) {
            targetRepsPresetOptions.forEach { preset ->
                FilterChip(
                    selected = targetRepsText == preset,
                    onClick = { onTargetRepsChange(preset) },
                    label = { Text(preset) }
                )
            }
            FilterChip(
                selected = hasCustomReps,
                onClick = {
                    customRepsDraft = if (hasCustomReps) targetRepsText else ""
                    showCustomRepsDialog = true
                },
                label = { Text(if (hasCustomReps) targetRepsText else "+") }
            )
        }
        targetRepsError?.let { error ->
            Text(
                text = error,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@file:Suppress("MatchingDeclarationName")

package com.alvarocervantes.fittrackplus.core.design

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle

enum class FitTrackKeyValueRowStyle {
    Flat,
    Pill
}

@Composable
fun FitTrackKeyValueRow(
    label: String? = null,
    value: String? = null,
    style: FitTrackKeyValueRowStyle,
    modifier: Modifier = Modifier,
    valueColor: Color = when (style) {
        FitTrackKeyValueRowStyle.Flat -> MaterialTheme.colorScheme.onSurfaceVariant
        FitTrackKeyValueRowStyle.Pill -> MaterialTheme.colorScheme.accentWarm
    },
    labelTextStyle: TextStyle = when (style) {
        FitTrackKeyValueRowStyle.Flat -> MaterialTheme.typography.bodyMedium
        FitTrackKeyValueRowStyle.Pill -> MaterialTheme.typography.labelSmall
    },
    valueTextStyle: TextStyle = MaterialTheme.typography.bodyMedium,
    content: (@Composable ColumnScope.() -> Unit)? = null
) {
    when (style) {
        FitTrackKeyValueRowStyle.Flat -> {
            Row(
                modifier = modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                label?.let {
                    Text(
                        text = it,
                        style = labelTextStyle,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                value?.let {
                    Text(
                        text = it,
                        style = valueTextStyle,
                        color = valueColor
                    )
                }
            }
        }

        FitTrackKeyValueRowStyle.Pill -> {
            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.accentSoft, MaterialTheme.shapes.large)
                    .padding(FitSpacing.md),
                verticalArrangement = Arrangement.spacedBy(FitSpacing.xs)
            ) {
                label?.let {
                    Text(
                        text = it,
                        style = labelTextStyle,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                value?.let {
                    Text(
                        text = it,
                        style = valueTextStyle,
                        color = valueColor
                    )
                }
                content?.invoke(this)
            }
        }
    }
}

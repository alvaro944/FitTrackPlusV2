@file:Suppress("MatchingDeclarationName")

package com.alvarocervantes.fittrackplus.core.design

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

data class FitTrackEntityListCardBadge(
    val text: String,
    val tone: FitTrackBadgeTone
)

@Composable
fun FitTrackEntityListCard(
    title: String,
    modifier: Modifier = Modifier,
    leadingDot: Color? = null,
    leadingDotContentDescription: String? = null,
    badge: FitTrackEntityListCardBadge? = null,
    meta: String? = null,
    metaContent: (@Composable ColumnScope.() -> Unit)? = null,
    actions: List<@Composable RowScope.() -> Unit> = emptyList()
) {
    FitTrackCard(modifier = modifier) {
        Column(
            verticalArrangement = Arrangement.spacedBy(FitSpacing.mdLg)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(FitSpacing.tiny)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(FitSpacing.xs)
                    ) {
                        leadingDot?.let { color ->
                            val dotModifier = Modifier
                                .size(8.dp)
                                .background(color, CircleShape)
                            androidx.compose.foundation.layout.Box(
                                modifier = if (leadingDotContentDescription == null) {
                                    dotModifier
                                } else {
                                    dotModifier.semantics {
                                        contentDescription = leadingDotContentDescription
                                    }
                                }
                            )
                        }
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    meta?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    metaContent?.invoke(this)
                }
                badge?.let {
                    FitTrackBadge(
                        label = it.text,
                        tone = it.tone
                    )
                }
            }

            actions.forEach { action ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(FitSpacing.sm),
                    content = action
                )
            }
        }
    }
}

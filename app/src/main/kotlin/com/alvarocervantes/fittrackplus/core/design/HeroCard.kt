package com.alvarocervantes.fittrackplus.core.design

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun FitTrackHeroCard(
    badge: String? = null,
    title: @Composable ColumnScope.() -> Unit,
    cta: String,
    onCtaClick: () -> Unit,
    modifier: Modifier = Modifier,
    ctaIcon: ImageVector? = null,
    ctaEnabled: Boolean = true,
    content: @Composable (ColumnScope.() -> Unit)? = null
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.extraLarge)
            .background(MaterialTheme.colorScheme.primaryDark)
            .padding(FitSpacing.cardPadding)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(FitSpacing.mdLg)) {
            badge?.let { FitTrackBadge(label = it, tone = FitTrackBadgeTone.Active) }
            title()
            content?.invoke(this)
            FitTrackPrimaryButton(
                label = cta,
                onClick = onCtaClick,
                enabled = ctaEnabled,
                icon = ctaIcon
            )
        }
    }
}

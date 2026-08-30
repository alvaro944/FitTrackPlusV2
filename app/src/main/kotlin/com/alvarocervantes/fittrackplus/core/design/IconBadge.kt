package com.alvarocervantes.fittrackplus.core.design

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

sealed interface FitTrackIconBadgeVariant {
    data class Icon(val imageVector: ImageVector) : FitTrackIconBadgeVariant
    data class Number(val value: String) : FitTrackIconBadgeVariant
}

enum class FitTrackIconBadgeTone {
    Soft,
    Outlined,
    Filled
}

/**
 * [Small] is for ordinals that sit inline with a label, where a full-size badge would outweigh the
 * text beside it. [Medium] is the standalone badge used as a leading element in cards and rows.
 */
enum class FitTrackIconBadgeSize(
    internal val badgeSize: Dp,
    internal val iconSize: Dp
) {
    Small(badgeSize = 28.dp, iconSize = 16.dp),
    Medium(badgeSize = 40.dp, iconSize = 20.dp)
}

@Composable
fun FitTrackIconBadge(
    variant: FitTrackIconBadgeVariant,
    tone: FitTrackIconBadgeTone,
    modifier: Modifier = Modifier,
    size: FitTrackIconBadgeSize = FitTrackIconBadgeSize.Medium
) {
    val badgeSize = size.badgeSize
    val iconSize = size.iconSize
    val numberStyle = when (size) {
        FitTrackIconBadgeSize.Small -> MaterialTheme.typography.labelMedium
        FitTrackIconBadgeSize.Medium -> MaterialTheme.typography.labelLarge
    }

    val backgroundColor = when (tone) {
        FitTrackIconBadgeTone.Soft -> MaterialTheme.colorScheme.primarySoft
        FitTrackIconBadgeTone.Outlined -> Color.Transparent
        FitTrackIconBadgeTone.Filled -> MaterialTheme.colorScheme.primary
    }
    val contentColor = when (tone) {
        FitTrackIconBadgeTone.Soft -> MaterialTheme.colorScheme.primaryDark
        FitTrackIconBadgeTone.Outlined -> MaterialTheme.colorScheme.primary
        FitTrackIconBadgeTone.Filled -> MaterialTheme.colorScheme.onPrimary
    }

    Box(
        modifier = modifier
            .size(badgeSize)
            .background(backgroundColor, CircleShape)
            .then(
                if (tone == FitTrackIconBadgeTone.Outlined) {
                    Modifier.border(1.dp, MaterialTheme.colorScheme.primary, CircleShape)
                } else {
                    Modifier
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        when (variant) {
            is FitTrackIconBadgeVariant.Icon -> Icon(
                imageVector = variant.imageVector,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(iconSize)
            )

            is FitTrackIconBadgeVariant.Number -> Text(
                text = variant.value,
                style = numberStyle,
                color = contentColor
            )
        }
    }
}

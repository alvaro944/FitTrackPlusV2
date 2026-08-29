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

@Composable
fun FitTrackIconBadge(
    variant: FitTrackIconBadgeVariant,
    tone: FitTrackIconBadgeTone,
    modifier: Modifier = Modifier
) {
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
            .size(40.dp)
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
                modifier = Modifier.size(20.dp)
            )

            is FitTrackIconBadgeVariant.Number -> Text(
                text = variant.value,
                style = MaterialTheme.typography.labelLarge,
                color = contentColor
            )
        }
    }
}

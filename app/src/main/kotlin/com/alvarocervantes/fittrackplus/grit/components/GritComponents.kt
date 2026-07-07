package com.alvarocervantes.fittrackplus.grit.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alvarocervantes.fittrackplus.grit.theme.GritColors
import com.alvarocervantes.fittrackplus.grit.theme.GritShapes
import com.alvarocervantes.fittrackplus.grit.theme.GritType
import kotlinx.coroutines.delay

@Composable
fun GritScreenHeader(
    title: String,
    icon: ImageVector? = null,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = GritColors.Lime,
                    modifier = Modifier.size(24.dp)
                )
            }
            Text(text = title.uppercase(), style = GritType.screenTitle)
        }
        Box(
            modifier = Modifier
                .width(48.dp)
                .height(4.dp)
                .background(GritColors.Lime)
        )
    }
}

@Composable
fun GritSectionLabel(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = GritColors.TextSecondary
) {
    Text(
        text = text.uppercase(),
        style = GritType.monoLabel,
        color = color,
        modifier = modifier
    )
}

@Composable
fun GritCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = GritColors.Surface,
    borderColor: Color = GritColors.Border,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier,
        shape = GritShapes.medium,
        color = backgroundColor,
        border = BorderStroke(1.dp, borderColor),
        content = content
    )
}

@Composable
fun GritPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true
) {
    Surface(
        modifier = modifier,
        shape = GritShapes.small,
        color = if (enabled) GritColors.Lime else GritColors.Lime.copy(alpha = 0.4f),
        onClick = onClick,
        enabled = enabled
    ) {
        Row(
            modifier = Modifier.padding(vertical = 14.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = GritColors.Black,
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                text = text.uppercase(),
                style = GritType.monoStrong,
                color = GritColors.Black,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun GritOutlineButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color = GritColors.Lime,
    enabled: Boolean = true
) {
    Surface(
        modifier = modifier,
        shape = GritShapes.small,
        color = Color.Transparent,
        border = BorderStroke(1.dp, if (enabled) accentColor else GritColors.Border),
        onClick = onClick,
        enabled = enabled
    ) {
        Text(
            text = text.uppercase(),
            style = GritType.monoStrong,
            color = if (enabled) accentColor else GritColors.TextFaint,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp)
        )
    }
}

@Composable
fun GritBadge(
    text: String,
    modifier: Modifier = Modifier,
    filled: Boolean = false
) {
    if (filled) {
        Text(
            text = text.uppercase(),
            style = GritType.monoLabelSmall,
            color = GritColors.Black,
            modifier = modifier
                .clip(GritShapes.small)
                .background(GritColors.Lime)
                .padding(horizontal = 8.dp, vertical = 3.dp)
        )
    } else {
        Text(
            text = text.uppercase(),
            style = GritType.monoLabelSmall,
            color = GritColors.Lime,
            modifier = modifier
                .border(1.dp, GritColors.Lime, GritShapes.small)
                .padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
fun GritStepperField(
    label: String,
    valueText: String,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
    modifier: Modifier = Modifier,
    valueContent: (@Composable () -> Unit)? = null
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        GritSectionLabel(text = label)
        Row(verticalAlignment = Alignment.CenterVertically) {
            GritStepperButton(text = "−", onClick = onDecrement)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .background(GritColors.Background)
                    .border(1.dp, GritColors.Border),
                contentAlignment = Alignment.Center
            ) {
                if (valueContent != null) {
                    valueContent()
                } else {
                    Text(
                        text = valueText,
                        style = GritType.monoStrong.copy(fontSize = 18.sp),
                        color = GritColors.Lime
                    )
                }
            }
            GritStepperButton(text = "+", onClick = onIncrement)
        }
    }
}

@Composable
private fun GritStepperButton(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(width = 44.dp, height = 52.dp)
            .clip(GritShapes.small)
            .background(GritColors.SurfaceContainer)
            .border(1.dp, GritColors.Border, GritShapes.small)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = GritType.itemTitle,
            color = GritColors.TextPrimary
        )
    }
}

@Composable
fun GritEmptyState(
    icon: ImageVector,
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onAction: (() -> Unit)? = null
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = GritShapes.medium,
        color = GritColors.Surface,
        border = BorderStroke(1.dp, GritColors.Border)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 40.dp, horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = GritColors.Lime,
                modifier = Modifier.size(40.dp)
            )
            Text(
                text = title.uppercase(),
                style = GritType.cardTitle,
                textAlign = TextAlign.Center
            )
            Text(
                text = body,
                style = GritType.monoBody,
                textAlign = TextAlign.Center
            )
            if (actionText != null && onAction != null) {
                Spacer(modifier = Modifier.height(8.dp))
                GritPrimaryButton(text = actionText, onClick = onAction)
            }
        }
    }
}

@Composable
fun GritToast(
    title: String,
    message: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    accent: Boolean = false,
    autoDismissMillis: Long = 4_000
) {
    LaunchedEffect(title, message) {
        delay(autoDismissMillis)
        onDismiss()
    }
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = GritShapes.small,
        color = GritColors.Black,
        border = BorderStroke(1.dp, if (accent) GritColors.Lime else GritColors.Neutral800)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = GritColors.Lime,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = title.uppercase(),
                    style = GritType.monoStrong,
                    color = GritColors.TextPrimary
                )
                Text(text = message, style = GritType.monoBody)
            }
            Text(
                text = "✕",
                style = GritType.monoBody,
                color = GritColors.TextFaint,
                modifier = Modifier.clickable(onClick = onDismiss)
            )
        }
    }
}

@Composable
fun GritStatTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    unit: String? = null
) {
    GritCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .height(76.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            GritSectionLabel(text = label)
            Row(verticalAlignment = Alignment.Bottom) {
                Text(text = value, style = GritType.statValue)
                if (unit != null) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = unit.uppercase(),
                        style = GritType.monoLabelSmall,
                        color = GritColors.Lime,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
            }
        }
    }
}

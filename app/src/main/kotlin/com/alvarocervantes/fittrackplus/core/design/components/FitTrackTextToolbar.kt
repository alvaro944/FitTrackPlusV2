package com.alvarocervantes.fittrackplus.core.design.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.LocalTextToolbar
import androidx.compose.ui.platform.TextToolbar
import androidx.compose.ui.platform.TextToolbarStatus
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import kotlin.math.roundToInt

private data class TextToolbarMenuState(
    val rect: Rect,
    val onCopyRequested: (() -> Unit)?,
    val onPasteRequested: (() -> Unit)?,
    val onCutRequested: (() -> Unit)?,
    val onSelectAllRequested: (() -> Unit)?
)

private class FitTrackTextToolbarState : TextToolbar {
    var menuState by mutableStateOf<TextToolbarMenuState?>(null)
        private set

    override val status: TextToolbarStatus
        get() = if (menuState == null) TextToolbarStatus.Hidden else TextToolbarStatus.Shown

    override fun showMenu(
        rect: Rect,
        onCopyRequested: (() -> Unit)?,
        onPasteRequested: (() -> Unit)?,
        onCutRequested: (() -> Unit)?,
        onSelectAllRequested: (() -> Unit)?
    ) {
        menuState = TextToolbarMenuState(
            rect = rect,
            onCopyRequested = onCopyRequested,
            onPasteRequested = onPasteRequested,
            onCutRequested = onCutRequested,
            onSelectAllRequested = onSelectAllRequested
        )
    }

    override fun hide() {
        menuState = null
    }
}

@Composable
fun FitTrackTextToolbarProvider(content: @Composable () -> Unit) {
    val textToolbar = remember { FitTrackTextToolbarState() }

    CompositionLocalProvider(LocalTextToolbar provides textToolbar) {
        content()
        FitTrackTextToolbarPopup(textToolbar = textToolbar)
    }
}

@Composable
private fun FitTrackTextToolbarPopup(textToolbar: FitTrackTextToolbarState) {
    val state = textToolbar.menuState ?: return
    val offset = IntOffset(
        x = state.rect.left.roundToInt(),
        y = (state.rect.top - TEXT_TOOLBAR_VERTICAL_OFFSET_PX).roundToInt().coerceAtLeast(0)
    )

    Popup(
        alignment = Alignment.TopStart,
        offset = offset,
        onDismissRequest = textToolbar::hide,
        properties = PopupProperties(focusable = false)
    ) {
        Surface(
            modifier = Modifier.wrapContentSize(),
            color = MaterialTheme.colorScheme.onSurface,
            contentColor = MaterialTheme.colorScheme.surface,
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 8.dp,
            shadowElevation = 8.dp
        ) {
            Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                state.onCopyRequested?.let { action ->
                    ToolbarAction(label = "Copiar", onClick = { action(); textToolbar.hide() })
                }
                state.onCutRequested?.let { action ->
                    ToolbarAction(label = "Cortar", onClick = { action(); textToolbar.hide() })
                }
                state.onPasteRequested?.let { action ->
                    ToolbarAction(label = "Pegar", onClick = { action(); textToolbar.hide() })
                }
                state.onSelectAllRequested?.let { action ->
                    ToolbarAction(label = "Seleccionar todo", onClick = { action(); textToolbar.hide() })
                }
            }
        }
    }
}

@Composable
private fun ToolbarAction(label: String, onClick: () -> Unit) {
    TextButton(onClick = onClick) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.surface,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

private const val TEXT_TOOLBAR_VERTICAL_OFFSET_PX = 64f

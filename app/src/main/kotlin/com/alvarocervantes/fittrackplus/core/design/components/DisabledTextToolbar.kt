package com.alvarocervantes.fittrackplus.core.design.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.LocalTextToolbar
import androidx.compose.ui.platform.TextToolbar
import androidx.compose.ui.platform.TextToolbarStatus

private class DisabledTextToolbar : TextToolbar {
    override val status: TextToolbarStatus = TextToolbarStatus.Hidden

    override fun showMenu(
        rect: Rect,
        onCopyRequested: (() -> Unit)?,
        onPasteRequested: (() -> Unit)?,
        onCutRequested: (() -> Unit)?,
        onSelectAllRequested: (() -> Unit)?
    ) = Unit

    override fun hide() = Unit
}

@Composable
fun DisableNativeTextToolbar(content: @Composable () -> Unit) {
    val toolbar = remember { DisabledTextToolbar() }
    CompositionLocalProvider(LocalTextToolbar provides toolbar, content = content)
}

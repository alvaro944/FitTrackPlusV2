package com.alvarocervantes.fittrackplus.core.design

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class DialogsApiTest {
    @Test
    fun sharedDialogComponentsAreAvailableFromDesignSystem() {
        val source = File("src/main/kotlin/com/alvarocervantes/fittrackplus/core/design/Dialogs.kt").readText()

        assertTrue(source.contains("fun FitTrackConfirmDialog("))
        assertTrue(source.contains("fun FitTrackInputDialog("))
        assertTrue(source.contains("fun FitTrackDialog("))
        assertTrue(source.contains("destructive: Boolean = false"))
        assertTrue(source.contains("confirmEnabled: Boolean = true"))
        assertTrue(source.contains("extraContent: (@Composable ColumnScope.() -> Unit)? = null"))
    }
}

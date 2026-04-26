package com.alvarocervantes.fittrackplus.feature.settings

import com.alvarocervantes.fittrackplus.core.design.AppThemeMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SettingsPreferenceMessagesTest {

    @Test
    fun doesNotCreateThemeMessageWhenSelectedModeIsAlreadyActive() {
        assertNull(themeModeChangeMessage(AppThemeMode.System, AppThemeMode.System))
        assertNull(themeModeChangeMessage(AppThemeMode.Light, AppThemeMode.Light))
        assertNull(themeModeChangeMessage(AppThemeMode.Dark, AppThemeMode.Dark))
    }

    @Test
    fun createsThemeMessageWhenSelectedModeChanges() {
        assertEquals(
            "Tema cambiado a Claro.",
            themeModeChangeMessage(AppThemeMode.System, AppThemeMode.Light)
        )
    }
}

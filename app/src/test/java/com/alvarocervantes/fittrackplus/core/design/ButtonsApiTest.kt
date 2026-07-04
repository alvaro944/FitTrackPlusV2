package com.alvarocervantes.fittrackplus.core.design

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ButtonsApiTest {
    @Test
    fun sharedButtonComponentsAreAvailableFromDesignSystem() {
        val source = File("src/main/kotlin/com/alvarocervantes/fittrackplus/core/design/Buttons.kt").readText()

        assertTrue(source.contains("fun FitTrackPrimaryButton("))
        assertTrue(source.contains("fun FitTrackTonalButton("))
        assertTrue(source.contains("fun FitTrackOutlinedButton("))
        assertTrue(source.contains("fun FitTrackAddButton("))
    }
}

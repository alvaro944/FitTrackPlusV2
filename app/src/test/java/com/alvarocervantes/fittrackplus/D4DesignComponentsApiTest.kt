package com.alvarocervantes.fittrackplus

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class D4DesignComponentsApiTest {
    private val root = File(".")

    @Test
    fun `d4 public design component APIs exist and feature duplicates are removed`() {
        val stepper = source("app/src/main/kotlin/com/alvarocervantes/fittrackplus/core/design/components/Stepper.kt")
        assertContainsAll(
            stepper,
            "fun FitTrackStepper(",
            "value: String",
            "onIncrement: () -> Unit",
            "onDecrement: () -> Unit",
            "modifier: Modifier = Modifier",
            "onLongIncrement: (() -> Unit)? = null",
            "onLongDecrement: (() -> Unit)? = null",
            "compact: Boolean = false",
            "decrementEnabled: Boolean = true",
            "incrementEnabled: Boolean = true"
        )

        val segmentedSelector = source("app/src/main/kotlin/com/alvarocervantes/fittrackplus/core/design/components/SegmentedSelector.kt")
        assertContainsAll(
            segmentedSelector,
            "fun FitTrackSegmentedSelector(",
            "options: List<String>",
            "selectedIndex: Int",
            "onSelect: (Int) -> Unit",
            "modifier: Modifier = Modifier"
        )

        val themeModeSelector = source("app/src/main/kotlin/com/alvarocervantes/fittrackplus/core/design/components/ThemeModeSelector.kt")
        assertContainsAll(
            themeModeSelector,
            "fun FitTrackThemeModeSelector(",
            "selected: AppThemeMode",
            "onSelect: (AppThemeMode) -> Unit",
            "modifier: Modifier = Modifier",
            "showRadio: Boolean = true"
        )

        val labels = source("app/src/main/kotlin/com/alvarocervantes/fittrackplus/core/design/Labels.kt")
        assertTrue(labels.contains("fun FitTrackHeroTag(text: String, modifier: Modifier = Modifier)"))

        val featureSources = listOf(
            source("app/src/main/kotlin/com/alvarocervantes/fittrackplus/feature/workout/WorkoutScreen.kt"),
            source("app/src/main/kotlin/com/alvarocervantes/fittrackplus/feature/routines/RoutinesScreen.kt"),
            source("app/src/main/kotlin/com/alvarocervantes/fittrackplus/feature/settings/SettingsScreen.kt"),
            source("app/src/main/kotlin/com/alvarocervantes/fittrackplus/feature/home/HomeScreen.kt"),
            source("app/src/main/kotlin/com/alvarocervantes/fittrackplus/core/design/AppShell.kt")
        ).joinToString("\n")

        listOf(
            "private fun SetStepperButton(",
            "private fun ExerciseSetsStepper(",
            "private fun StepGoalStepper(",
            "private fun UnitSelector(",
            "private fun UnitSegment(",
            "private fun WeightUnitInlineSelector(",
            "private fun ThemeModeSelector(",
            "private fun ThemeModeOption(",
            "private fun ThemeModeInlineSelector(",
            "private fun MiniHeroTag(",
            "private fun HeroTag("
        ).forEach { duplicate ->
            assertFalse("Removed private duplicate $duplicate", featureSources.contains(duplicate))
        }

        assertFalse(
            "Workout long-press handling must live in the shared stepper component",
            source("app/src/main/kotlin/com/alvarocervantes/fittrackplus/feature/workout/WorkoutScreen.kt").contains("combinedClickable")
        )
    }

    private fun source(path: String): String {
        val file = listOf(File(root, path), File(root, "../$path")).first { it.isFile }
        assertTrue("Missing source file: $path", file.isFile)
        return file.readText()
    }

    private fun assertContainsAll(source: String, vararg snippets: String) {
        snippets.forEach { snippet ->
            assertTrue("Missing snippet: $snippet", source.contains(snippet))
        }
    }
}

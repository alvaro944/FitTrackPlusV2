package com.alvarocervantes.fittrackplus.feature.workout

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExerciseAlternativesUiStateTest {

    @Test
    fun detectsAnExistingVariantNameIgnoringCaseAndSurroundingSpaces() {
        val picker = picker(optionNames = listOf("Press banca", "Press inclinado"))

        assertTrue(picker.hasVariantNamed("press banca"))
        assertTrue(picker.hasVariantNamed("  Press Inclinado  "))
    }

    @Test
    fun allowsANameThatIsNotUsedByAnyVariant() {
        val picker = picker(optionNames = listOf("Press banca"))

        assertFalse(picker.hasVariantNamed("Press declinado"))
    }

    @Test
    fun matchesTheBaseExerciseNameToo() {
        val picker = picker(optionNames = listOf("Sentadilla"))

        assertTrue(picker.hasVariantNamed("Sentadilla"))
    }

    private fun picker(optionNames: List<String>): ExerciseAlternativesUiState {
        val options = optionNames.mapIndexed { index, name ->
            ExerciseVariantOptionUiState(
                variantKey = "variant-$index",
                name = name,
                targetSets = 3,
                targetRepsText = "8-12",
                notes = null,
                isDefault = index == 0,
                isCurrent = index == 0
            )
        }
        return ExerciseAlternativesUiState(
            workoutExerciseId = 1L,
            routineExerciseId = 2L,
            title = optionNames.first(),
            currentVariantKey = "variant-0",
            defaultVariantKey = "variant-0",
            options = options
        )
    }
}

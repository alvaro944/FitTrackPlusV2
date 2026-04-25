package com.alvarocervantes.fittrackplus.feature.routines

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RoutineTemplatesTest {

    @Test
    fun everyTemplateProducesAValidEditorDraft() {
        assertEquals(3, routineTemplates.size)

        routineTemplates.forEach { template ->
            assertTrue(template.id.isNotBlank())
            assertTrue(template.name.isNotBlank())
            assertTrue(template.description.isNotBlank())
            assertTrue(template.days.isNotEmpty())

            val editor = template.toEditorState()

            assertTrue("Template ${template.id} should be valid", editor.canSave)
            assertTrue(editor.name.isNotBlank())
            assertTrue(editor.days.all { day -> day.name.isNotBlank() })
            assertTrue(
                editor.days.all { day ->
                    day.exercises.isNotEmpty() &&
                        day.exercises.all { exercise ->
                            exercise.name.isNotBlank() &&
                                exercise.targetSetsError == null &&
                                exercise.targetRepsError == null
                        }
                }
            )
        }
    }

    @Test
    fun pplTemplateContainsExpectedRoutineStructure() {
        val template = routineTemplates.firstOrNull { it.id == "ppl" }

        assertNotNull(template)
        val editor = requireNotNull(template).toEditorState()

        assertEquals("Push Pull Legs", editor.name)
        assertEquals(listOf("Push", "Pull", "Legs"), editor.days.map { it.name })
        assertTrue(editor.days.all { day -> day.exercises.size >= 3 })
    }
}

package com.alvarocervantes.fittrackplus.domain.model.progression

import org.junit.Assert.assertEquals
import org.junit.Test

/** Covers R17 and acceptance criterion 17 of the progression engine spec. */
class IntraSessionAdjustmentTest {

    @Test
    fun `the R17 table maps every surplus band`() {
        assertEquals(2, stepsFor(probeSurplus = 4))
        assertEquals(2, stepsFor(probeSurplus = 3))
        assertEquals(1, stepsFor(probeSurplus = 2))
        assertEquals(0, stepsFor(probeSurplus = 1))
        assertEquals(0, stepsFor(probeSurplus = 0))
        assertEquals(0, stepsFor(probeSurplus = -1))
        assertEquals(-1, stepsFor(probeSurplus = -2))
        assertEquals(-1, stepsFor(probeSurplus = -5))
    }

    @Test
    fun `a missing probe surplus adjusts nothing`() {
        assertEquals(0, stepsFor(probeSurplus = null))
    }

    @Test
    fun `adjustment never exceeds the cap`() {
        val steps = stepsFor(probeSurplus = 99)

        assertEquals(ProgressionTuning.INTRA_MAX_STEPS, steps)
    }

    @Test
    fun `calibration recovery and evaluation are never adjusted`() {
        assertEquals(0, stepsFor(probeSurplus = 4, state = ProgressionState.CALIBRATING))
        assertEquals(0, stepsFor(probeSurplus = 4, state = ProgressionState.RECOVERING))
        assertEquals(0, stepsFor(probeSurplus = 4, state = ProgressionState.EVALUATING))
        assertEquals(0, stepsFor(probeSurplus = 4, exposureType = ExposureType.RECOVERY))
        assertEquals(0, stepsFor(probeSurplus = 4, exposureType = ExposureType.EVALUATION))
    }

    @Test
    fun `the suggested load moves by whole increments and never goes to zero`() {
        assertEquals(75.0, intraSessionSuggestedLoadKg(70.0, 2.5, 2), 0.0)
        assertEquals(67.5, intraSessionSuggestedLoadKg(70.0, 2.5, -1), 0.0)
        assertEquals(70.0, intraSessionSuggestedLoadKg(70.0, 2.5, 0), 0.0)
        assertEquals(2.0, intraSessionSuggestedLoadKg(2.0, 2.5, -1), 0.0)
    }

    @Test
    fun `the next exposure decision is not fed by the adjusted sets`() {
        // The engine reads probeSurplus only. Two exposures with the same probe must decide the
        // same thing regardless of what the adjusted sets ended up weighing.
        val easyProbe = stepsFor(probeSurplus = 3)
        assertEquals(2, easyProbe)

        // Re-running the table with the ALREADY adjusted load would be double counting. The
        // function has no parameter for it, which is the point: there is no way to leak it in.
        assertEquals(2, stepsFor(probeSurplus = 3))
    }

    private fun stepsFor(
        probeSurplus: Int?,
        state: ProgressionState = ProgressionState.PROGRESSING,
        exposureType: ExposureType = ExposureType.STRENGTH
    ): Int = intraSessionAdjustmentSteps(probeSurplus, state, exposureType)
}

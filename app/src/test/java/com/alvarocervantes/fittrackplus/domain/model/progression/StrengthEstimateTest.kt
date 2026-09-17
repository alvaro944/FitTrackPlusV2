package com.alvarocervantes.fittrackplus.domain.model.progression

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class StrengthEstimateTest {

    @Test
    fun `100x3 at rir2 produces a high-confidence estimate`() {
        val estimate = calculateStrengthEstimate(loadKg = 100.0, reps = 3, rir = 2)

        requireNotNull(estimate)
        assertEquals(116.666, estimate.e1rmKg, 0.01)
        assertEquals(E1rmConfidence.HIGH, estimate.confidence)
    }

    @Test
    fun `100x8 at rir2 produces a medium-confidence estimate`() {
        val estimate = calculateStrengthEstimate(loadKg = 100.0, reps = 8, rir = 2)

        requireNotNull(estimate)
        assertEquals(133.333, estimate.e1rmKg, 0.01)
        assertEquals(E1rmConfidence.MEDIUM, estimate.confidence)
    }

    @Test
    fun `100x8 at rir5 does not produce an estimate`() {
        assertNull(calculateStrengthEstimate(loadKg = 100.0, reps = 8, rir = 5))
    }

    @Test
    fun `100x8 without rir does not produce an estimate`() {
        assertNull(calculateStrengthEstimate(loadKg = 100.0, reps = 8, rir = null))
    }
}

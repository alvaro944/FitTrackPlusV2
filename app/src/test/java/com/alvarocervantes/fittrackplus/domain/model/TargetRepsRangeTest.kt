package com.alvarocervantes.fittrackplus.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TargetRepsRangeTest {

    @Test
    fun parsesExactReps() {
        assertEquals(TargetRepsRange(min = 10, max = 10), TargetRepsRange.parse("10"))
    }

    @Test
    fun parsesCompactRange() {
        assertEquals(TargetRepsRange(min = 8, max = 12), TargetRepsRange.parse("8-12"))
    }

    @Test
    fun parsesRangeWithSpaces() {
        assertEquals(TargetRepsRange(min = 8, max = 12), TargetRepsRange.parse("8 - 12"))
    }

    @Test
    fun rejectsInvertedRange() {
        assertNull(TargetRepsRange.parse("12-8"))
    }

    @Test
    fun rejectsZero() {
        assertNull(TargetRepsRange.parse("0"))
    }

    @Test
    fun rejectsThreeDigitValue() {
        assertNull(TargetRepsRange.parse("100"))
    }

    @Test
    fun rejectsUnstructuredText() {
        assertNull(TargetRepsRange.parse("AMRAP"))
    }

    @Test
    fun rejectsEmptyText() {
        assertNull(TargetRepsRange.parse(""))
    }

    @Test
    fun rejectsNullText() {
        assertNull(TargetRepsRange.parse(null))
    }

    @Test
    fun parsesExplicitPositiveSignLikeLegacyParser() {
        assertEquals(TargetRepsRange(min = 10, max = 10), TargetRepsRange.parse("+10"))
    }

    @Test
    fun rejectsTextInsideRangeBoundary() {
        assertNull(TargetRepsRange.parse("8x-12"))
    }

    @Test
    fun rejectsAdditionalRangeBoundary() {
        assertNull(TargetRepsRange.parse("8-12-15"))
    }
}

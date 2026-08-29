package com.alvarocervantes.fittrackplus.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class WeightUnitTest {

    @Test
    fun poundsConvertToAndFromPersistedKilograms() {
        val pounds = WeightUnit.Pounds

        assertEquals(220.46226218, pounds.fromKilograms(100.0), 0.000001)
        assertEquals(100.0, pounds.toKilograms(220.46226218), 0.000001)
    }

    @Test
    fun unknownPreferenceDefaultsToKilograms() {
        assertEquals(WeightUnit.Kilograms, WeightUnit.fromPreference("stones"))
    }
}

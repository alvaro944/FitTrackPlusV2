package com.alvarocervantes.fittrackplus.domain.model

/**
 * Presentation unit for weights. Persisted workout values are always kilograms so changing this
 * preference never changes historical data.
 */
enum class WeightUnit(
    val preferenceValue: String,
    val label: String
) {
    Kilograms(preferenceValue = "kg", label = "kg"),
    Pounds(preferenceValue = "lb", label = "lb");

    fun fromKilograms(weightKg: Double): Double = when (this) {
        Kilograms -> weightKg
        Pounds -> weightKg * KILOGRAMS_TO_POUNDS
    }

    fun toKilograms(displayWeight: Double): Double = when (this) {
        Kilograms -> displayWeight
        Pounds -> displayWeight / KILOGRAMS_TO_POUNDS
    }

    companion object {
        private const val KILOGRAMS_TO_POUNDS = 2.2046226218

        fun fromPreference(value: String): WeightUnit = entries.firstOrNull {
            it.preferenceValue == value
        } ?: Kilograms
    }
}

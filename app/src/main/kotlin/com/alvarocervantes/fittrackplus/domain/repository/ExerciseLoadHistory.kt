package com.alvarocervantes.fittrackplus.domain.repository

/**
 * The single thing the progression engine needs from workout history: the heaviest load ever logged
 * for a variant.
 *
 * Deliberately narrow. Depending on the whole `WorkoutRepository` here would force every test of the
 * engine to stub about thirty methods it does not care about, which is the same boilerplate that
 * made someone reach for silent `= Unit` defaults in the first place.
 */
fun interface ExerciseLoadHistory {
    suspend fun heaviestLoggedKg(variantKey: String): Double?
}

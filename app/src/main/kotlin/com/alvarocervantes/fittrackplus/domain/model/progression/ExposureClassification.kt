package com.alvarocervantes.fittrackplus.domain.model.progression

data class ExposureClassification(
    val surplus: Int?,
    val outcomeClass: OutcomeClass,
    val setCompletionRatio: Double,
    val excludeFromTrend: Boolean
)

package com.alvarocervantes.fittrackplus.core.design

/** Which way a metric moved between two points in time. */
enum class FitTrackDeltaDirection {
    Up,
    Down,
    Flat
}

/**
 * Whether moving up is an improvement for this metric.
 *
 * [Neutral] exists because not every increase is progress: a longer workout is just longer, and
 * painting it with the same "good" tone as more volume tells the user something untrue.
 */
enum class FitTrackDeltaMeaning {
    HigherIsBetter,
    Neutral
}

/**
 * Single source of truth for the colour a delta badge gets, so the same movement does not read as
 * good on one screen and as a warning on another.
 */
fun fitTrackDeltaTone(
    direction: FitTrackDeltaDirection,
    meaning: FitTrackDeltaMeaning = FitTrackDeltaMeaning.HigherIsBetter
): FitTrackBadgeTone {
    if (meaning == FitTrackDeltaMeaning.Neutral) return FitTrackBadgeTone.Neutral
    return when (direction) {
        FitTrackDeltaDirection.Up -> FitTrackBadgeTone.Active
        FitTrackDeltaDirection.Down -> FitTrackBadgeTone.Warm
        FitTrackDeltaDirection.Flat -> FitTrackBadgeTone.Neutral
    }
}

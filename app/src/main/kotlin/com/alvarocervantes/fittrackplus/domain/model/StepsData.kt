package com.alvarocervantes.fittrackplus.domain.model

data class StepsData(
    val todaySteps: Long,
    val weekDaySteps: Map<Int, Long>
)

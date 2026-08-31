package com.alvarocervantes.fittrackplus.core.design.components

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LineChartTest {

    @Test
    fun `zero anchored axis starts at zero and produces different offsets`() {
        val points = listOf(1L to 100f, 2L to 101f)
        val automaticAxis = calculateLineChartAxisRange(
            values = points.map { it.second },
            baselineMode = LineChartBaselineMode.AutoRange
        )
        val zeroAnchoredAxis = calculateLineChartAxisRange(
            values = points.map { it.second },
            baselineMode = LineChartBaselineMode.ZeroBaseline
        )

        assertEquals(100f, automaticAxis.minimum)
        assertEquals(0f, zeroAnchoredAxis.minimum)
        assertNotEquals(
            points.chartOffsets(
                width = 200f,
                height = 120f,
                padLeft = 18f,
                padRight = 18f,
                padTop = 22f,
                padBottom = 26f,
                axisRange = automaticAxis
            ),
            points.chartOffsets(
                width = 200f,
                height = 120f,
                padLeft = 18f,
                padRight = 18f,
                padTop = 22f,
                padBottom = 26f,
                axisRange = zeroAnchoredAxis
            )
        )
    }

    @Test
    fun `repeated values keep a nonzero axis span`() {
        val axis = calculateLineChartAxisRange(
            values = listOf(42f, 42f),
            baselineMode = LineChartBaselineMode.AutoRange
        )

        assertEquals(42f, axis.minimum)
        assertEquals(42f, axis.maximum)
        assertEquals(1f, axis.span)
    }

    @Test
    fun `point labels are limited to significant unique indices`() {
        val indices = lineChartLabelIndices(
            values = listOf(2f, 6f, 4f, 5f, 1f, 3f, 7f, 2f),
            selectedPointIndex = 3
        )

        assertTrue(indices.size <= 5)
        assertEquals(setOf(0, 3, 4, 6, 7), indices)
    }

}

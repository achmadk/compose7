package io.github.compose7.components.forms

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Step snapping for [Slider]/[RangeSlider] (PRD §8.5): discrete values must
 * land exactly on the endpoints and on evenly spaced interior points.
 */
class QuantizeTest {

    @Test
    fun continuousRangePassesThrough() {
        assertEquals(0.37f, quantize(0.37f, 0f..1f, steps = 0))
        assertEquals(12f, quantize(12f, 0f..100f, steps = 0))
    }

    @Test
    fun endpointsStayExact() {
        assertEquals(0f, quantize(-3f, 0f..1f, steps = 4))
        assertEquals(1f, quantize(9f, 0f..1f, steps = 4))
    }

    @Test
    fun stepsSplitRangeIntoSegments() {
        // Material convention: steps = interior stops, so 4 steps -> 0,20,40,60,80,100.
        assertEquals(0f, quantize(2f, 0f..100f, steps = 4))
        assertEquals(20f, quantize(20f, 0f..100f, steps = 4))
        assertEquals(60f, quantize(51f, 0f..100f, steps = 4))
        assertEquals(80f, quantize(72f, 0f..100f, steps = 4))
        assertEquals(100f, quantize(99f, 0f..100f, steps = 4))
    }

    @Test
    fun degenerateRangeIsSafe() {
        assertEquals(5f, quantize(5f, 5f..5f, steps = 3))
    }
}

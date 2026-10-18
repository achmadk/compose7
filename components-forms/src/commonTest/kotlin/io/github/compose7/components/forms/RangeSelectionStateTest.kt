package io.github.compose7.components.forms

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Two-tap state machine behind [CalendarRange] (PRD §8.5: range selection).
 */
class RangeSelectionStateTest {

    private val jan10 = CalendarDate(2026, 1, 10)
    private val jan12 = CalendarDate(2026, 1, 12)

    @Test
    fun firstTapOnlyAnchorsTheStart() {
        val state = RangeSelectionState()
        assertNull(state.tap(jan10))
        assertEquals(jan10, state.pendingStart)
    }

    @Test
    fun secondTapEmitsRangeAndResets() {
        val state = RangeSelectionState()
        state.tap(jan10)
        assertEquals(jan10..jan12, state.tap(jan12))
        assertNull(state.pendingStart)
    }

    @Test
    fun tapsAreNormalizedWhenTheEndComesFirst() {
        val state = RangeSelectionState()
        state.tap(jan12)
        assertEquals(jan10..jan12, state.tap(jan10))
    }

    @Test
    fun aThirdTapStartsOver() {
        val state = RangeSelectionState()
        state.tap(jan10)
        state.tap(jan12)
        assertNull(state.tap(jan10))
        assertEquals(jan10, state.pendingStart)
    }

    @Test
    fun sameDayProducesASingleDayRange() {
        val state = RangeSelectionState()
        state.tap(jan10)
        assertEquals(jan10..jan10, state.tap(jan10))
    }

    @Test
    fun clearDropsThePendingAnchor() {
        val state = RangeSelectionState()
        state.tap(jan10)
        state.clear()
        assertNull(state.pendingStart)
        assertNull(state.tap(jan12))
    }
}

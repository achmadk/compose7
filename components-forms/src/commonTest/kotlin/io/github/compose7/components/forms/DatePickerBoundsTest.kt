package io.github.compose7.components.forms

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Bounds shared by [DatePicker]'s pure calendar and the iOS wheel
 * (`UIDatePicker.minimumDate`/`maximumDate`), so both paths accept the same
 * dates (PRD §8.7: no silent divergence).
 */
class DatePickerBoundsTest {

    private val min = CalendarDate(2026, 1, 1)
    private val max = CalendarDate(2026, 12, 31)

    @Test
    fun noBoundsAllowsEverything() {
        assertTrue(CalendarDate(1900, 1, 1).isWithin(null, null))
        assertTrue(CalendarDate(2100, 12, 31).isWithin(null, null))
    }

    @Test
    fun boundsAreInclusive() {
        assertTrue(min.isWithin(min, max))
        assertTrue(max.isWithin(min, max))
        assertTrue(CalendarDate(2026, 6, 15).isWithin(min, max))
    }

    @Test
    fun datesOutsideBoundsAreRejected() {
        assertFalse(CalendarDate(2025, 12, 31).isWithin(min, max))
        assertFalse(CalendarDate(2027, 1, 1).isWithin(min, max))
    }

    @Test
    fun oneSidedBoundsOnlyConstrainThatSide() {
        assertFalse(CalendarDate(2025, 12, 31).isWithin(min, null))
        assertTrue(CalendarDate(2099, 1, 1).isWithin(min, null))

        assertTrue(CalendarDate(1900, 1, 1).isWithin(null, max))
        assertFalse(CalendarDate(2027, 1, 1).isWithin(null, max))
    }
}

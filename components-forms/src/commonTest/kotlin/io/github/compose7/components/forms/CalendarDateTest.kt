package io.github.compose7.components.forms

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CalendarDateTest {

    @Test
    fun leapYearRules() {
        assertTrue(isLeapYear(2024))
        assertTrue(isLeapYear(2000))
        assertTrue(isLeapYear(1600))
        assertFalse(isLeapYear(1900))
        assertFalse(isLeapYear(2100))
        assertFalse(isLeapYear(2023))
    }

    @Test
    fun daysInMonthTable() {
        assertEquals(31, daysInMonth(2026, 1))
        assertEquals(28, daysInMonth(2023, 2))
        assertEquals(29, daysInMonth(2024, 2))
        assertEquals(30, daysInMonth(2024, 4))
        assertEquals(31, daysInMonth(2024, 12))
        assertEquals(30, daysInMonth(1900, 9))
        assertEquals(31, daysInMonth(1900, 8))
    }

    @Test
    fun dayOfWeekIsIso() {
        assertEquals(6, CalendarDate(2000, 1, 1).dayOfWeek())
        assertEquals(4, CalendarDate(1970, 1, 1).dayOfWeek())
        assertEquals(7, CalendarDate(2026, 10, 18).dayOfWeek())
        assertEquals(1, CalendarDate(2026, 10, 19).dayOfWeek())
        assertEquals(2, CalendarDate(1789, 7, 14).dayOfWeek())
    }

    @Test
    fun plusDaysCrossesBoundaries() {
        assertEquals(CalendarDate(2024, 2, 29), CalendarDate(2024, 2, 28).plusDays(1))
        assertEquals(CalendarDate(2023, 2, 28), CalendarDate(2023, 3, 1).minusDays(1))
        assertEquals(CalendarDate(2024, 3, 1), CalendarDate(2024, 2, 29).plusDays(1))
        assertEquals(CalendarDate(2025, 1, 1), CalendarDate(2024, 12, 31).plusDays(1))
        assertEquals(CalendarDate(2023, 12, 31), CalendarDate(2024, 1, 1).minusDays(1))
        assertEquals(CalendarDate(2026, 10, 25), CalendarDate(2026, 10, 18).plusDays(7))
    }

    @Test
    fun epochDayRoundTrips() {
        val samples = listOf(
            CalendarDate(1970, 1, 1),
            CalendarDate(2000, 2, 29),
            CalendarDate(2024, 2, 29),
            CalendarDate(2026, 10, 18),
        )
        for (date in samples) {
            assertEquals(date, epochDayToCalendarDate(date.toEpochDay()))
        }
    }

    @Test
    fun plusMonthsClampsDay() {
        assertEquals(CalendarDate(2024, 2, 29), CalendarDate(2024, 1, 31).plusMonths(1))
        assertEquals(CalendarDate(2023, 2, 28), CalendarDate(2023, 3, 31).minusMonths(1))
        assertEquals(CalendarDate(2025, 1, 15), CalendarDate(2024, 12, 15).plusMonths(1))
        assertEquals(CalendarDate(2023, 12, 15), CalendarDate(2024, 1, 15).minusMonths(1))
        assertEquals(CalendarDate(2025, 4, 10), CalendarDate(2024, 4, 10).plusMonths(12))
    }

    @Test
    fun formattingAndParsingRoundTrip() {
        assertEquals("2026-10-18", CalendarDate(2026, 10, 18).formatted())
        assertEquals("2024-02-29", CalendarDate(2024, 2, 29).formatted())
        assertEquals(CalendarDate(2024, 2, 29), parseCalendarDate("2024-02-29"))
        assertNull(parseCalendarDate("2023-02-29"))
        assertNull(parseCalendarDate("2024-13-01"))
        assertNull(parseCalendarDate("not-a-date"))
    }

    @Test
    fun comparisonsAndMonthEdges() {
        assertTrue(CalendarDate(2024, 12, 31).isAfter(CalendarDate(2025, 1, 1)).not())
        assertTrue(CalendarDate(2024, 12, 31).isBefore(CalendarDate(2025, 1, 1)))
        assertEquals(CalendarDate(2026, 10, 1), CalendarDate(2026, 10, 18).startOfMonth())
        assertEquals(CalendarDate(2026, 10, 31), CalendarDate(2026, 10, 18).endOfMonth())
        assertEquals(CalendarDate(2026, 10, 31), CalendarDate(2026, 10, 31).withDay(45))
    }
}

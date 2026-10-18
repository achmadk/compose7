package io.github.compose7.components.forms

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver

/**
 * Minimal calendar date (PRD §8.5, §11) — year, 1-based month, day — with no
 * kotlinx-datetime dependency. All math is proleptic Gregorian and covered by
 * `commonTest` (PRD §13: unit-tested date logic for the calendar component).
 */
data class CalendarDate(val year: Int, val month: Int, val day: Int) {
    init {
        require(month in 1..12) { "month must be 1..12, was $month" }
        require(day in 1..daysInMonth(year, month)) {
            "day must be 1..${daysInMonth(year, month)} for $year-$month, was $day"
        }
    }
}

/** Gregorian leap-year rule (centuries are leap only when divisible by 400). */
fun isLeapYear(year: Int): Boolean =
    year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)

/** Number of days in 1-based [month] of [year]. */
fun daysInMonth(year: Int, month: Int): Int = when (month) {
    1, 3, 5, 7, 8, 10, 12 -> 31
    4, 6, 9, 11 -> 30
    else -> if (isLeapYear(year)) 29 else 28
}

private val DayOfWeekShifts = intArrayOf(0, 3, 2, 5, 0, 3, 5, 1, 4, 6, 2, 4)

/** ISO-8601 day of week: 1 = Monday … 7 = Sunday. */
fun CalendarDate.dayOfWeek(): Int {
    val y = if (month < 3) year - 1 else year
    val dow = (y + y / 4 - y / 100 + y / 400 + DayOfWeekShifts[month - 1] + day) % 7
    return if (dow == 0) 7 else dow
}

/** Proleptic-Gregorian day number (Fliegel–Van Flandern), used for date arithmetic. */
internal fun CalendarDate.toEpochDay(): Int {
    val a = (14 - month) / 12
    val y = year + 4800 - a
    val m = month + 12 * a - 3
    return day + (153 * m + 2) / 5 + 365 * y + y / 4 - y / 100 + y / 400 - 32045
}

/** Inverse of [toEpochDay]. */
internal fun epochDayToCalendarDate(epochDay: Int): CalendarDate {
    val a = epochDay + 32044
    val b = (4 * a + 3) / 146097
    val c = a - 146097 * b / 4
    val d = (4 * c + 3) / 1461
    val e = c - 1461 * d / 4
    val m = (5 * e + 2) / 153
    val day = e - (153 * m + 2) / 5 + 1
    val month = m + 3 - 12 * (m / 10)
    val year = 100 * b + d - 4800 + m / 10
    return CalendarDate(year, month, day)
}

/** [days] may be negative; crosses month, year, and leap-day boundaries. */
fun CalendarDate.plusDays(days: Int): CalendarDate =
    epochDayToCalendarDate(toEpochDay() + days)

/** Negative-safe counterpart of [plusDays]. */
fun CalendarDate.minusDays(days: Int): CalendarDate = plusDays(-days)

/**
 * Adds [months] (may be negative). The day-of-month is clamped to the target
 * month's length (2024-01-31 + 1 month = 2024-02-29).
 */
fun CalendarDate.plusMonths(months: Int): CalendarDate {
    val total = year * 12 + (month - 1) + months
    val newYear = total.floorDiv(12)
    val newMonth = total.mod(12) + 1
    return CalendarDate(newYear, newMonth, day.coerceAtMost(daysInMonth(newYear, newMonth)))
}

/** Negative-safe counterpart of [plusMonths]. */
fun CalendarDate.minusMonths(months: Int): CalendarDate = plusMonths(-months)

/** Same date with [day] clamped to this month's length. */
fun CalendarDate.withDay(day: Int): CalendarDate =
    copy(day = day.coerceIn(1, daysInMonth(year, month)))

/** First day of this date's month. */
fun CalendarDate.startOfMonth(): CalendarDate = copy(day = 1)

/** Last day of this date's month. */
fun CalendarDate.endOfMonth(): CalendarDate = copy(day = daysInMonth(year, month))

/** True when this date lies strictly before [other]. */
fun CalendarDate.isBefore(other: CalendarDate): Boolean = toEpochDay() < other.toEpochDay()

/** True when this date lies strictly after [other]. */
fun CalendarDate.isAfter(other: CalendarDate): Boolean = toEpochDay() > other.toEpochDay()

/** ISO-ish `yyyy-MM-dd` rendering. */
fun CalendarDate.formatted(): String {
    val y = year.toString().padStart(4, '0')
    val m = month.toString().padStart(2, '0')
    val d = day.toString().padStart(2, '0')
    return "$y-$m-$d"
}

/** Parses [text] produced by [formatted]; null when malformed or out of range. */
fun parseCalendarDate(text: String): CalendarDate? {
    val parts = text.split('-')
    if (parts.size != 3) return null
    val year = parts[0].toIntOrNull() ?: return null
    val month = parts[1].toIntOrNull() ?: return null
    val day = parts[2].toIntOrNull() ?: return null
    if (month !in 1..12) return null
    if (day !in 1..daysInMonth(year, month)) return null
    return CalendarDate(year, month, day)
}

internal val CalendarDateSaver: Saver<CalendarDate, Any> = listSaver(
    save = { listOf(it.year, it.month, it.day) },
    restore = { CalendarDate(it[0], it[1], it[2]) },
)

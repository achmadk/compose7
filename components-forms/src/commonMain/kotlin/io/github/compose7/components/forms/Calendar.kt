package io.github.compose7.components.forms

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.compose7.core.theme.Compose7ThemeColors
import io.github.compose7.core.theme.Compose7ThemeTypography
import io.github.compose7.foundation.Chevron
import io.github.compose7.foundation.compose7Clickable
import io.github.compose7.foundation.currentMinTouchTarget

private val WeekdayInitials = listOf("M", "T", "W", "T", "F", "S", "S")
private val MonthNames = listOf(
    "January", "February", "March", "April", "May", "June",
    "July", "August", "September", "October", "November", "December",
)

internal fun defaultMonthTitle(date: CalendarDate): String =
    "${MonthNames[date.month - 1]} ${date.year}"

/**
 * Month-grid calendar (PRD §7 P1 / Appendix A "Calendar", §8.5, §13: date math
 * covered by `commonTest`). Shows one month with prev/next navigation, a
 * selected-date highlight, and a [isDateEnabled] predicate for blackouts.
 *
 * The displayed month is owned internally and initialized from
 * [initialMonth] (defaults to the selected date's month).
 *
 * For a two-ended selection use [CalendarRange], the same grid with range
 * semantics (PRD §8.5: single date and range selection).
 *
 * ```kotlin
 * Calendar(selectedDate = picked, onDateSelected = { picked = it })
 * ```
 *
 * @param weekdayLabels Seven labels starting at Monday; override for
 *   localization (PRD §12: no hard-coded copy).
 */
@Composable
fun Calendar(
    selectedDate: CalendarDate?,
    onDateSelected: (CalendarDate) -> Unit,
    modifier: Modifier = Modifier,
    initialMonth: CalendarDate = selectedDate ?: CalendarDate(2000, 1, 1),
    firstDayOfWeek: Int = 1,
    isDateEnabled: (CalendarDate) -> Boolean = { true },
    monthTitle: (CalendarDate) -> String = { defaultMonthTitle(it) },
    weekdayLabels: List<String> = WeekdayInitials,
) {
    val startOfWeek = firstDayOfWeek.coerceIn(1, 7)
    val labels = usableWeekdayLabels(weekdayLabels)
    var month by rememberSaveable(stateSaver = CalendarDateSaver) {
        mutableStateOf(initialMonth.startOfMonth())
    }

    Column(modifier = modifier) {
        CalendarHeader(
            month = month,
            monthTitle = monthTitle,
            onMonthChange = { month = month.plusMonths(it) },
        )
        CalendarWeekdays(startOfWeek, labels)
        CalendarGrid(
            month = month,
            startOfWeek = startOfWeek,
            cellHeight = currentMinTouchTarget(),
            isDateEnabled = isDateEnabled,
            dayState = { date -> DayState(selected = date == selectedDate, inRange = false) },
            onDateClick = onDateSelected,
        )
    }
}

/**
 * Two-ended variant of [Calendar] (PRD §8.5: range selection). Tap once to set
 * the start, again to set the end (taps before the start produce a normalized
 * range), and a third time to start over. The live pending start is highlighted
 * like a single selection.
 *
 * ```kotlin
 * CalendarRange(selectedRange = trip, onRangeSelected = { trip = it })
 * ```
 */
@Composable
fun CalendarRange(
    selectedRange: ClosedRange<CalendarDate>?,
    onRangeSelected: (ClosedRange<CalendarDate>) -> Unit,
    modifier: Modifier = Modifier,
    initialMonth: CalendarDate = selectedRange?.start ?: CalendarDate(2000, 1, 1),
    firstDayOfWeek: Int = 1,
    isDateEnabled: (CalendarDate) -> Boolean = { true },
    monthTitle: (CalendarDate) -> String = { defaultMonthTitle(it) },
    weekdayLabels: List<String> = WeekdayInitials,
) {
    val startOfWeek = firstDayOfWeek.coerceIn(1, 7)
    val labels = usableWeekdayLabels(weekdayLabels)
    var month by rememberSaveable(stateSaver = CalendarDateSaver) {
        mutableStateOf(initialMonth.startOfMonth())
    }
    val selection = remember { RangeSelectionState() }

    Column(modifier = modifier) {
        CalendarHeader(
            month = month,
            monthTitle = monthTitle,
            onMonthChange = { month = month.plusMonths(it) },
        )
        CalendarWeekdays(startOfWeek, labels)
        CalendarGrid(
            month = month,
            startOfWeek = startOfWeek,
            cellHeight = currentMinTouchTarget(),
            isDateEnabled = isDateEnabled,
            dayState = { date -> dayStateOf(date, selection.pendingStart, selectedRange) },
            onDateClick = { date ->
                selection.tap(date)?.let(onRangeSelected)
            },
        )
    }
}

/** Selection bookkeeping for [CalendarRange], extracted so the state machine is unit-tested. */
internal class RangeSelectionState {
    var pendingStart: CalendarDate? = null
        private set

    /**
     * Records a tap. Returns `null` while waiting for the end date, then a
     * normalized (start <= end) range and resets.
     */
    fun tap(date: CalendarDate): ClosedRange<CalendarDate>? {
        val start = pendingStart
        if (start == null) {
            pendingStart = date
            return null
        }
        pendingStart = null
        return if (date < start) date..start else start..date
    }

    fun clear() {
        pendingStart = null
    }
}

private data class DayState(val selected: Boolean, val inRange: Boolean)

private fun dayStateOf(
    date: CalendarDate,
    pendingStart: CalendarDate?,
    range: ClosedRange<CalendarDate>?,
): DayState {
    if (date == pendingStart) return DayState(selected = true, inRange = false)
    if (range == null) return DayState(selected = false, inRange = false)
    val selected = date == range.start || date == range.endInclusive
    val inRange = date > range.start && date < range.endInclusive
    return DayState(selected = selected, inRange = inRange)
}

private fun usableWeekdayLabels(labels: List<String>): List<String> =
    if (labels.size >= 7) labels else WeekdayInitials

@Composable
private fun CalendarHeader(
    month: CalendarDate,
    monthTitle: (CalendarDate) -> String,
    onMonthChange: (Int) -> Unit,
) {
    val colors = Compose7ThemeColors
    val type = Compose7ThemeTypography

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CalendarNavButton(previous = true, onClick = { onMonthChange(-1) })
        BasicText(
            text = monthTitle(month),
            style = type.titleMedium.copy(
                color = colors.onSurface,
                textAlign = TextAlign.Center,
            ),
            modifier = Modifier.weight(1f),
        )
        CalendarNavButton(previous = false, onClick = { onMonthChange(1) })
    }
}

@Composable
private fun CalendarWeekdays(startOfWeek: Int, labels: List<String>) {
    Row(modifier = Modifier.fillMaxWidth()) {
        for (iso in startOfWeek..7) {
            WeekdayLabel(labels[iso - 1], Modifier.weight(1f).padding(bottom = 4.dp))
        }
        for (iso in 1 until startOfWeek) {
            WeekdayLabel(labels[iso - 1], Modifier.weight(1f).padding(bottom = 4.dp))
        }
    }
}

@Composable
private fun CalendarGrid(
    month: CalendarDate,
    startOfWeek: Int,
    cellHeight: Dp,
    isDateEnabled: (CalendarDate) -> Boolean,
    dayState: (CalendarDate) -> DayState,
    onDateClick: (CalendarDate) -> Unit,
) {
    val monthDays = daysInMonth(month.year, month.month)
    val leadingBlanks = (month.dayOfWeek() - startOfWeek + 7) % 7
    val cells = List(leadingBlanks + monthDays) { index ->
        val day = index - leadingBlanks + 1
        if (day in 1..monthDays) CalendarDate(month.year, month.month, day) else null
    }
    cells.chunked(7).forEach { week ->
        Row(modifier = Modifier.fillMaxWidth()) {
            repeat(7) { column ->
                CalendarDayCell(
                    date = week.getOrNull(column),
                    state = week.getOrNull(column)?.let(dayState),
                    enabled = week.getOrNull(column)?.let(isDateEnabled) ?: false,
                    indicatorSize = cellHeight,
                    onClick = { week.getOrNull(column)?.let(onDateClick) },
                )
            }
        }
    }
}

@Composable
private fun CalendarNavButton(previous: Boolean, onClick: () -> Unit) {
    val colors = Compose7ThemeColors
    Box(
        modifier = Modifier
            .size(currentMinTouchTarget())
            .compose7Clickable(onClick = onClick, role = Role.Button),
        contentAlignment = Alignment.Center,
    ) {
        Chevron(
            color = colors.primary,
            modifier = Modifier.graphicsLayer {
                rotationZ = if (previous) 180f else 0f
            },
        )
    }
}

@Composable
private fun WeekdayLabel(label: String, modifier: Modifier) {
    BasicText(
        text = label,
        style = Compose7ThemeTypography.labelSmall.copy(
            color = Compose7ThemeColors.onSurfaceVariant,
            textAlign = TextAlign.Center,
        ),
        modifier = modifier,
    )
}

@Composable
private fun RowScope.CalendarDayCell(
    date: CalendarDate?,
    state: DayState?,
    enabled: Boolean,
    indicatorSize: Dp,
    onClick: () -> Unit,
) {
    val colors = Compose7ThemeColors
    val type = Compose7ThemeTypography
    val selected = state?.selected == true
    val inRange = state?.inRange == true

    Box(
        modifier = Modifier
            .weight(1f)
            .height(indicatorSize)
            .let {
                if (date != null) {
                    it.semantics { contentDescription = date.formatted() }
                        .compose7Clickable(enabled = enabled, onClick = onClick, role = Role.Button)
                } else {
                    it
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        if (date != null) {
            if (inRange) {
                Box(Modifier.fillMaxSize().background(colors.primaryContainer))
            }
            if (selected) {
                Box(
                    Modifier
                        .size(indicatorSize * 0.8f)
                        .clip(CircleShape)
                        .background(colors.primary),
                )
            }
            BasicText(
                text = date.day.toString(),
                style = type.bodyMedium.copy(
                    color = when {
                        !enabled -> colors.onSurfaceVariant.copy(alpha = 0.38f)
                        selected -> colors.onPrimary
                        inRange -> colors.onPrimaryContainer
                        else -> colors.onSurface
                    },
                ),
            )
        }
    }
}

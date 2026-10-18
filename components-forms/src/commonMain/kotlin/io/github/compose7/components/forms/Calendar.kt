package io.github.compose7.components.forms

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.CircleShape
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
 * ```kotlin
 * Calendar(selectedDate = picked, onDateSelected = { picked = it })
 * ```
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
) {
    val colors = Compose7ThemeColors
    val type = Compose7ThemeTypography

    var month by rememberSaveable(stateSaver = CalendarDateSaver) {
        mutableStateOf(initialMonth.startOfMonth())
    }
    val startOfWeek = firstDayOfWeek.coerceIn(1, 7)
    val cellHeight = currentMinTouchTarget()

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CalendarNavButton(previous = true, onClick = { month = month.minusMonths(1) })
            BasicText(
                text = monthTitle(month),
                style = type.titleMedium.copy(
                    color = colors.onSurface,
                    textAlign = TextAlign.Center,
                ),
                modifier = Modifier.weight(1f),
            )
            CalendarNavButton(previous = false, onClick = { month = month.plusMonths(1) })
        }

        Row(modifier = Modifier.fillMaxWidth()) {
            for (iso in startOfWeek..7) {
                WeekdayLabel(WeekdayInitials[iso - 1], Modifier.weight(1f).padding(bottom = 4.dp))
            }
            for (iso in 1 until startOfWeek) {
                WeekdayLabel(WeekdayInitials[iso - 1], Modifier.weight(1f).padding(bottom = 4.dp))
            }
        }

        val monthDays = daysInMonth(month.year, month.month)
        val leadingBlanks = (month.dayOfWeek() - startOfWeek + 7) % 7
        val cells = List(leadingBlanks + monthDays) { index ->
            val day = index - leadingBlanks + 1
            if (day in 1..monthDays) CalendarDate(month.year, month.month, day) else null
        }
        cells.chunked(7).forEach { week ->
            Row(modifier = Modifier.fillMaxWidth()) {
                repeat(7) { column ->
                    val date = week.getOrNull(column)
                    CalendarDayCell(
                        date = date,
                        selected = date == selectedDate,
                        enabled = date != null && isDateEnabled(date),
                        indicatorSize = cellHeight,
                        onClick = { if (date != null) onDateSelected(date) },
                    )
                }
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
    selected: Boolean,
    enabled: Boolean,
    indicatorSize: Dp,
    onClick: () -> Unit,
) {
    val colors = Compose7ThemeColors
    val type = Compose7ThemeTypography

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
                        else -> colors.onSurface
                    },
                ),
            )
        }
    }
}

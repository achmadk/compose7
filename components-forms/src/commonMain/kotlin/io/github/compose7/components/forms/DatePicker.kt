package io.github.compose7.components.forms

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import io.github.compose7.components.overlays.SheetModal
import io.github.compose7.core.nativebridge.resolveNativeBridge
import io.github.compose7.core.theme.Compose7StyleInfo
import io.github.compose7.core.theme.Compose7ThemeColors
import io.github.compose7.core.theme.Compose7ThemeTypography
import io.github.compose7.foundation.Chevron
import io.github.compose7.foundation.compose7Clickable
import io.github.compose7.foundation.currentMinTouchTarget

/**
 * Date field that opens a picker (PRD §7 / Appendix A "Calendar",
 * "DatePicker"). The pure-Compose path uses [Calendar] in a dialog; on iOS the
 * opt-in Native Interop Bridge presents a real UIKit `UIDatePicker` in wheels
 * style instead (PRD §8.7).
 *
 * The API is data-only so both paths see the same inputs: a concrete date and
 * optional [minDate]/[maxDate] bounds. For arbitrary blackout rules use
 * [Calendar] directly - a predicate cannot be represented by `UIDatePicker`
 * and would silently diverge.
 *
 * ```kotlin
 * DatePicker(
 *     selectedDate = due,
 *     onDateSelected = { due = it },
 *     label = "Due date",
 *     minDate = CalendarDate(2026, 1, 1),
 * )
 * ```
 *
 * @param initialMonth Month shown by the pure-Compose calendar before the
 *   selected date is set. Ignored by the wheel.
 * @param nativeBridge Per-component switch; `null` follows the theme.
 */
@Composable
fun DatePicker(
    selectedDate: CalendarDate?,
    onDateSelected: (CalendarDate) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String = "Select date",
    enabled: Boolean = true,
    initialMonth: CalendarDate = selectedDate ?: CalendarDate(2000, 1, 1),
    firstDayOfWeek: Int = 1,
    minDate: CalendarDate? = null,
    maxDate: CalendarDate? = null,
    nativeBridge: Boolean? = null,
) {
    val colors = Compose7ThemeColors
    val type = Compose7ThemeTypography
    var expanded by rememberSaveable { mutableStateOf(false) }

    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = currentMinTouchTarget())
                .compose7Clickable(
                    enabled = enabled,
                    onClick = { expanded = true },
                    role = Role.Button,
                )
                .padding(horizontal = Compose7StyleInfo.listContentInset, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                if (label != null) {
                    BasicText(
                        text = label,
                        style = type.labelMedium.copy(color = colors.onSurfaceVariant),
                    )
                }
                BasicText(
                    text = selectedDate?.formatted() ?: placeholder,
                    style = type.bodyLarge.copy(
                        color = if (selectedDate != null) colors.onSurface else colors.onSurfaceVariant,
                    ),
                )
            }
            Chevron(color = colors.onSurfaceVariant)
        }

        if (expanded) {
            if (resolveNativeBridge(nativeBridge)) {
                NativeDatePicker(
                    selectedDate = selectedDate,
                    title = label ?: placeholder,
                    initialMonth = initialMonth,
                    firstDayOfWeek = firstDayOfWeek,
                    minDate = minDate,
                    maxDate = maxDate,
                    onDateSelected = {
                        onDateSelected(it)
                        expanded = false
                    },
                    onDismissRequest = { expanded = false },
                )
            } else {
                PureDatePickerSheet(
                    selectedDate = selectedDate,
                    title = label ?: placeholder,
                    initialMonth = initialMonth,
                    firstDayOfWeek = firstDayOfWeek,
                    minDate = minDate,
                    maxDate = maxDate,
                    onDateSelected = {
                        onDateSelected(it)
                        expanded = false
                    },
                    onDismissRequest = { expanded = false },
                )
            }
        }
    }
}

@Composable
internal fun PureDatePickerSheet(
    selectedDate: CalendarDate?,
    title: String?,
    initialMonth: CalendarDate,
    firstDayOfWeek: Int,
    minDate: CalendarDate?,
    maxDate: CalendarDate?,
    onDateSelected: (CalendarDate) -> Unit,
    onDismissRequest: () -> Unit,
) {
    // A sheet rather than a dialog: the dialog card is too narrow for seven
    // calendar columns to keep the minimum touch target.
    SheetModal(
        visible = true,
        onDismissRequest = onDismissRequest,
        title = title,
    ) {
        Calendar(
            selectedDate = selectedDate,
            onDateSelected = onDateSelected,
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            initialMonth = initialMonth,
            firstDayOfWeek = firstDayOfWeek,
            isDateEnabled = { date -> date.isWithin(minDate, maxDate) },
        )
    }
}

/**
 * Bounds check shared by the pure calendar's enabled predicate. The iOS wheel
 * receives the same bounds through `UIDatePicker.minimumDate`/`maximumDate`,
 * so both paths agree on what is selectable.
 */
internal fun CalendarDate.isWithin(minDate: CalendarDate?, maxDate: CalendarDate?): Boolean =
    (minDate == null || !isBefore(minDate)) && (maxDate == null || !isAfter(maxDate))
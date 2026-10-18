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
import io.github.compose7.core.theme.Compose7StyleInfo
import io.github.compose7.core.theme.Compose7ThemeColors
import io.github.compose7.core.theme.Compose7ThemeTypography
import io.github.compose7.foundation.Chevron
import io.github.compose7.foundation.compose7Clickable
import io.github.compose7.foundation.currentMinTouchTarget

/**
 * Date-range field that opens [CalendarRange] in a sheet (PRD §8.5: the
 * calendar supports "single date, range, and inline vs. modal presentation" -
 * [Calendar] is the inline single, [DatePicker] the modal single, this the
 * modal range).
 *
 * The sheet closes as soon as the second endpoint is tapped; tapping outside
 * cancels without changing [selectedRange].
 *
 * @param isError Tints the label/chevron and validation slot with the error color.
 * @param supportingText Validation message slot (PRD §8.5).
 *
 * ```kotlin
 * DateRangePicker(
 *     selectedRange = trip,
 *     onRangeSelected = { trip = it },
 *     label = "Trip",
 * )
 * ```
 */
@Composable
fun DateRangePicker(
    selectedRange: ClosedRange<CalendarDate>?,
    onRangeSelected: (ClosedRange<CalendarDate>) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String = "Select dates",
    enabled: Boolean = true,
    initialMonth: CalendarDate = selectedRange?.start ?: CalendarDate(2000, 1, 1),
    firstDayOfWeek: Int = 1,
    minDate: CalendarDate? = null,
    maxDate: CalendarDate? = null,
    isError: Boolean = false,
    supportingText: (@Composable () -> Unit)? = null,
) {
    val colors = Compose7ThemeColors
    val type = Compose7ThemeTypography
    var expanded by rememberSaveable { mutableStateOf(false) }

    val summary = selectedRange?.let { range ->
        "${range.start.formatted()} - ${range.endInclusive.formatted()}"
    }

    Column(modifier = modifier) {
        Box {
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
                            style = type.labelMedium.copy(
                                color = if (isError) colors.error else colors.onSurfaceVariant,
                            ),
                        )
                    }
                    BasicText(
                        text = summary ?: placeholder,
                        style = type.bodyLarge.copy(
                            color = if (summary != null) colors.onSurface else colors.onSurfaceVariant,
                        ),
                    )
                }
                Chevron(color = if (isError) colors.error else colors.onSurfaceVariant)
            }

            if (expanded) {
                SheetModal(
                    visible = true,
                    onDismissRequest = { expanded = false },
                    title = label ?: placeholder,
                ) {
                    CalendarRange(
                        selectedRange = selectedRange,
                        onRangeSelected = { range ->
                            onRangeSelected(range)
                            expanded = false
                        },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        initialMonth = initialMonth,
                        firstDayOfWeek = firstDayOfWeek,
                        isDateEnabled = { date -> date.isWithin(minDate, maxDate) },
                    )
                }
            }
        }
        FormFieldSupportingText(isError = isError, supportingText = supportingText)
    }
}

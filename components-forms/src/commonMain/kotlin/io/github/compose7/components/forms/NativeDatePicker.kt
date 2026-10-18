package io.github.compose7.components.forms

import androidx.compose.runtime.Composable

/**
 * Native Interop Bridge entry point for [DatePicker] (PRD §8.7, bridge list
 * item 3). `iosMain` presents a real `UIDatePicker` in wheels style;
 * `androidMain`/`desktopMain` delegate to [PureDatePickerSheet] because the
 * bridge is iOS-only (`resolveNativeBridge` already gates the call).
 *
 * Data-only contract: a date plus optional bounds - never a composable slot or
 * arbitrary predicate, both of which `UIDatePicker` cannot express.
 */
@Composable
internal expect fun NativeDatePicker(
    selectedDate: CalendarDate?,
    title: String?,
    initialMonth: CalendarDate,
    firstDayOfWeek: Int,
    minDate: CalendarDate?,
    maxDate: CalendarDate?,
    onDateSelected: (CalendarDate) -> Unit,
    onDismissRequest: () -> Unit,
)

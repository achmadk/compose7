package io.github.compose7.components.forms

import androidx.compose.runtime.Composable

// The Native Interop Bridge is iOS-only (PRD §8.7). On Desktop this actual
// delegates to the pure-Compose dialog; it is only reachable when a caller
// forces `nativeBridge = true`, which `resolveNativeBridge` gates to iOS.

@Composable
internal actual fun NativeDatePicker(
    selectedDate: CalendarDate?,
    title: String?,
    initialMonth: CalendarDate,
    firstDayOfWeek: Int,
    minDate: CalendarDate?,
    maxDate: CalendarDate?,
    onDateSelected: (CalendarDate) -> Unit,
    onDismissRequest: () -> Unit,
) {
    PureDatePickerSheet(
        selectedDate = selectedDate,
        title = title,
        initialMonth = initialMonth,
        firstDayOfWeek = firstDayOfWeek,
        minDate = minDate,
        maxDate = maxDate,
        onDateSelected = onDateSelected,
        onDismissRequest = onDismissRequest,
    )
}

package io.github.compose7.components.overlays

import androidx.compose.runtime.Composable

// The Native Interop Bridge is iOS-only (PRD §8.7). On Desktop these actuals
// delegate to the pure-Compose implementations; they are only reachable when
// a caller forces `nativeBridge = true`, which `resolveNativeBridge` already
// gates to iOS — so this is a compile-time formality, not a runtime path.

@Composable
internal actual fun NativeDialog(
    onDismissRequest: () -> Unit,
    title: String?,
    message: String?,
    actions: List<DialogAction>,
) {
    PureDialog(
        onDismissRequest = onDismissRequest,
        modifier = androidx.compose.ui.Modifier,
        title = title,
        message = message,
        actions = actions,
        content = null,
    )
}

@Composable
internal actual fun NativeActionSheet(
    onDismissRequest: () -> Unit,
    title: String?,
    message: String?,
    actions: List<ActionSheetAction>,
) {
    PureActionSheet(
        visible = true,
        onDismissRequest = onDismissRequest,
        modifier = androidx.compose.ui.Modifier,
        title = title,
        message = message,
        actions = actions,
        content = null,
    )
}

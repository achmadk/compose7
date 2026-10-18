package io.github.compose7.components.overlays

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

// The Native Interop Bridge is iOS-only (PRD §8.7). On Android this actual
// delegates to the pure-Compose sheet; it is only reachable when a caller
// forces `nativeBridge = true`, which `resolveNativeBridge` gates to iOS.

@Composable
internal actual fun NativeShareSheet(
    items: List<ShareItem>,
    onDismissRequest: () -> Unit,
) {
    PureShareSheet(
        visible = true,
        onDismissRequest = onDismissRequest,
        items = items,
        modifier = Modifier,
        onItemSelected = {},
    )
}

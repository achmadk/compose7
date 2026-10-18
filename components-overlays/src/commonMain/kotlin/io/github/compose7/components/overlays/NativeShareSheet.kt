package io.github.compose7.components.overlays

import androidx.compose.runtime.Composable

/**
 * Native Interop Bridge entry point for [ShareSheet] (PRD §8.7, bridge list
 * item 4). `iosMain` presents a real `UIActivityViewController`; the other
 * actuals delegate to [PureShareSheet] because the bridge is iOS-only
 * (`resolveNativeBridge` already gates the call).
 *
 * Data-only contract: the activity items - no composable slots, no callbacks
 * that only one platform could honour.
 */
@Composable
internal expect fun NativeShareSheet(
    items: List<ShareItem>,
    onDismissRequest: () -> Unit,
)

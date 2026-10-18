package io.github.compose7.components.overlays

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.IntRect

/**
 * Native Interop Bridge entry point for [ContextMenu] (PRD §8.7, bridge list
 * item 5). `iosMain` registers the anchor with one `UIContextMenuInteraction`
 * on the Compose host view; the other actuals do nothing because
 * `resolveNativeBridge` keeps the bridge iOS-only and the pure menu is already
 * rendered by the caller.
 *
 * Data-only contract: menu items plus the anchor rectangle in window pixels -
 * never a composable or a UIView handle.
 */
@Composable
internal expect fun NativeContextMenu(
    items: List<ContextMenuItem>,
    anchorBounds: IntRect,
    onDismissRequest: () -> Unit,
)

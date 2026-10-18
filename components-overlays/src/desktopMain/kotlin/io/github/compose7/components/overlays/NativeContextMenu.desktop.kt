package io.github.compose7.components.overlays

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.IntRect

// The Native Interop Bridge is iOS-only (PRD §8.7). The pure menu is already
// rendered by ContextMenu whenever resolveNativeBridge() is false, which is
// always the case on Desktop - this actual only satisfies the expect.

@Composable
internal actual fun NativeContextMenu(
    items: List<ContextMenuItem>,
    anchorBounds: IntRect,
    onDismissRequest: () -> Unit,
) = Unit

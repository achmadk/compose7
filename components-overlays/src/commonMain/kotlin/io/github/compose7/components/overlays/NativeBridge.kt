package io.github.compose7.components.overlays

import androidx.compose.runtime.Composable

/**
 * Native Interop Bridge entry points (PRD §8.7) for the Phase-0 components
 * (Alert/Dialog, ActionSheet). `iosMain` renders real UIKit controllers;
 * `androidMain`/`desktopMain` delegate to the pure-Compose implementations
 * (the bridge is iOS-only — `resolveNativeBridge` already gates the call, the
 * actuals only exist so the expect compiles everywhere).
 *
 * Data-only contract: these take title/message/actions, never composable
 * slots (PRD §8.7 — no silent divergence between native and pure paths).
 */
@Composable
internal expect fun NativeDialog(
    onDismissRequest: () -> Unit,
    title: String?,
    message: String?,
    actions: List<DialogAction>,
)

@Composable
internal expect fun NativeActionSheet(
    onDismissRequest: () -> Unit,
    title: String?,
    message: String?,
    actions: List<ActionSheetAction>,
)

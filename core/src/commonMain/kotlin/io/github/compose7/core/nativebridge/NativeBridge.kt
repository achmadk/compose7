package io.github.compose7.core.nativebridge

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf

/**
 * Global opt-in for the iOS Native Interop Bridge (PRD §8.7), set via
 * `Compose7Theme(nativeBridge = true)`. Pure Compose is the default everywhere.
 */
val LocalNativeBridgeEnabled = compositionLocalOf { false }

/** Whether the current platform can host native UIKit views (iOS only, PRD §8.7). */
expect val isNativeBridgePlatform: Boolean

/**
 * Resolves whether a component should render through the native bridge.
 *
 * @param override Per-component switch: `true` forces native, `false` forces
 *   pure Compose, `null` follows the theme-level setting. Native is always
 *   gated to platforms that support it — forcing it elsewhere silently falls
 *   back to pure Compose rather than crashing.
 */
@Composable
@ReadOnlyComposable
fun resolveNativeBridge(override: Boolean? = null): Boolean {
    val platformSupports = isNativeBridgePlatform
    if (!platformSupports) return false
    return override ?: LocalNativeBridgeEnabled.current
}

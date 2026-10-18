package io.github.compose7.core.platform

import androidx.compose.runtime.Composable
import io.github.compose7.core.theme.Compose7ColorScheme

/**
 * Android 12+ dynamic color as an opt-in token source (PRD §8.1).
 *
 * Returns null when the platform has no dynamic-color source, when the API
 * level is too low, or when the system declines to expose wallpaper colors —
 * callers fall back to the style's static tokens.
 */
@Composable
expect fun platformDynamicColorScheme(darkTheme: Boolean): Compose7ColorScheme?

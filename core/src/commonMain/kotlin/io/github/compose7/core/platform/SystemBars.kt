package io.github.compose7.core.platform

import androidx.compose.runtime.Composable

/**
 * Requests light (`darkContent = true`) or dark (`false`) system-bar content
 * over the app's content (PRD §10: system bar tinting is a genuine platform
 * need kept in `expect`/`actual`).
 *
 * Android flips status/navigation-bar appearance flags. iOS derives status-bar
 * content from the interface style, which [io.github.compose7.core.theme.Compose7Theme]
 * already follows — so the actual is a no-op there. Desktop has no system bars.
 */
@Composable
expect fun Compose7SystemBarAppearance(darkContent: Boolean)

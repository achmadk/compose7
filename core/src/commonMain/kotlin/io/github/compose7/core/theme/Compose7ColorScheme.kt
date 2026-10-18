package io.github.compose7.core.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Color tokens for one style in one brightness.
 *
 * Names follow Material3's `ColorScheme` vocabulary so teams can transfer
 * knowledge, but the values differ per [Compose7Style]. Every `onX` token is
 * required to clear WCAG-AA contrast (4.5:1) against its base — enforced by
 * unit tests in `core/src/commonTest`.
 */
@Immutable
class Compose7ColorScheme(
    val primary: Color,
    val onPrimary: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val secondary: Color,
    val onSecondary: Color,
    val secondaryContainer: Color,
    val onSecondaryContainer: Color,
    val background: Color,
    val onBackground: Color,
    val surface: Color,
    val onSurface: Color,
    /** Raised surfaces: cards, sheets, popups. */
    val surfaceVariant: Color,
    val onSurfaceVariant: Color,
    val error: Color,
    val onError: Color,
    val errorContainer: Color,
    val onErrorContainer: Color,
    /** Strong separators and control borders. */
    val outline: Color,
    /** Hairline dividers, input borders, grouped-list separators. */
    val outlineVariant: Color,
    /** Overlay color behind modals. */
    val scrim: Color,
    /** Pressed-state overlay laid over interactive surfaces. Alpha baked in. */
    val highlight: Color,
) {
    /**
     * Returns this scheme with a new accent pair (Android dynamic color),
     * regenerating container/on-colors so contrast holds on the current
     * background.
     */
    fun withDynamicColors(newPrimary: Color, newSecondary: Color): Compose7ColorScheme {
        val light = background.luminance() > 0.5f
        val primaryContainer = newPrimary.blendWith(background, if (light) 0.82f else 0.78f)
        val secondaryContainer = newSecondary.blendWith(background, if (light) 0.85f else 0.8f)
        return Compose7ColorScheme(
            primary = newPrimary,
            // Text on an accent must be judged against THAT accent, not the page
            // background - otherwise a loud wallpaper accent ships unreadable buttons.
            onPrimary = newPrimary.bestTextColor(newPrimary),
            primaryContainer = primaryContainer,
            onPrimaryContainer = newPrimary.bestTextColor(primaryContainer),
            secondary = newSecondary,
            onSecondary = newSecondary.bestTextColor(newSecondary),
            secondaryContainer = secondaryContainer,
            onSecondaryContainer = newSecondary.bestTextColor(secondaryContainer),
            background = background,
            onBackground = onBackground,
            surface = surface,
            onSurface = onSurface,
            surfaceVariant = surfaceVariant,
            onSurfaceVariant = onSurfaceVariant,
            error = error,
            onError = onError,
            errorContainer = errorContainer,
            onErrorContainer = onErrorContainer,
            outline = outline,
            outlineVariant = outlineVariant,
            scrim = scrim,
            highlight = highlight,
        )
    }
}

/** The active color token set. Provided by [Compose7Theme]. */
val LocalCompose7ColorScheme = compositionLocalOf { lightMaterialColorScheme() }

/** Convenience accessor for the active color token set. */
val Compose7ThemeColors: Compose7ColorScheme
    @Composable get() = LocalCompose7ColorScheme.current

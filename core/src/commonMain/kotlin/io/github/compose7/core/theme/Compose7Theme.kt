package io.github.compose7.core.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.compose7.core.nativebridge.LocalNativeBridgeEnabled
import io.github.compose7.core.platform.platformDefaultStyle
import io.github.compose7.core.platform.platformDynamicColorScheme

/**
 * Fixed traits of the active style that components branch on (PRD §6: adaptive
 * by default, override-able; PRD §8.3: platform-correct divider insets).
 */
@Immutable
data class Compose7StyleProperties(
    /** Inset from the list container edge to the content start. */
    val listContentInset: Dp,
    /** Base inset from the container edge to the divider start — 0 on Material (edge-to-edge, PRD §8.3). */
    val listDividerInset: Dp,
    /** Cupertino presents sheets card-style; Material uses standard bottom-sheet motion (PRD §8.4). */
    val cardStyleSheetMotion: Boolean,
    /** Whether Navbar large titles collapse on scroll (PRD §8.2). */
    val supportsLargeTitleCollapse: Boolean,
    /** Whether interactive components give haptic feedback on press. */
    val usesHapticFeedback: Boolean,
)

/** The active style properties. Provided by [Compose7Theme]. */
val LocalCompose7StyleProperties = compositionLocalOf {
    stylePropertiesFor(platformDefaultStyle())
}

/** Convenience accessor for the active style properties. */
val Compose7StyleInfo: Compose7StyleProperties
    @Composable @ReadOnlyComposable get() = LocalCompose7StyleProperties.current

internal fun stylePropertiesFor(style: Compose7Style): Compose7StyleProperties = when (style) {
    Compose7Style.Material -> Compose7StyleProperties(
        listContentInset = 16.dp,
        listDividerInset = 0.dp,
        cardStyleSheetMotion = false,
        supportsLargeTitleCollapse = true,
        usesHapticFeedback = true,
    )
    Compose7Style.Cupertino -> Compose7StyleProperties(
        listContentInset = 16.dp,
        listDividerInset = 16.dp,
        cardStyleSheetMotion = true,
        supportsLargeTitleCollapse = true,
        usesHapticFeedback = true,
    )
    Compose7Style.DesktopUI7 -> Compose7StyleProperties(
        listContentInset = 12.dp,
        listDividerInset = 0.dp,
        cardStyleSheetMotion = false,
        supportsLargeTitleCollapse = false,
        usesHapticFeedback = false,
    )
    Compose7Style.Auto -> stylePropertiesFor(platformDefaultStyle())
}

/**
 * Compose7's theme entry point (PRD §8.1).
 *
 * Resolves [style] (Auto → host platform), assembles the token set for the
 * active brightness, optionally layers Android dynamic color, and provides
 * everything components read through `CompositionLocal`s.
 *
 * ```kotlin
 * Compose7Theme {
 *     // your app
 * }
 * // Force one look everywhere (PRD §9: no silent divergence):
 * Compose7Theme(style = Compose7Style.Material) { ... }
 * ```
 *
 * @param style Visual language; [Compose7Style.Auto] follows the host platform.
 * @param darkTheme Light/dark; follows the system by default.
 * @param colorScheme Full color-token override (rebranding escape hatch, PRD §9).
 * @param dynamicColor Android 12+ dynamic color from the wallpaper as an opt-in
 *   token source (PRD §8.1); ignored on other platforms.
 * @param nativeBridge Enables the opt-in iOS Native Interop Bridge for
 *   components that offer it (PRD §8.7). Pure Compose remains the default.
 * @param typography Full type-token override.
 * @param shapes Full shape-token override.
 * @param motion Full motion-token override.
 */
@Composable
fun Compose7Theme(
    style: Compose7Style = Compose7Style.Auto,
    darkTheme: Boolean = isSystemInDarkTheme(),
    colorScheme: Compose7ColorScheme? = null,
    dynamicColor: Boolean = false,
    nativeBridge: Boolean = false,
    typography: Compose7Typography? = null,
    shapes: Compose7Shapes? = null,
    motion: Compose7Motion? = null,
    content: @Composable () -> Unit,
) {
    val resolvedStyle = if (style == Compose7Style.Auto) platformDefaultStyle() else style
    val baseColors = colorSchemeFor(resolvedStyle, darkTheme)
    val dynamic = if (dynamicColor) platformDynamicColorScheme(darkTheme) else null
    val colors = colorScheme ?: dynamic ?: baseColors

    CompositionLocalProvider(
        LocalCompose7Style provides resolvedStyle,
        LocalCompose7ColorScheme provides colors,
        LocalCompose7Typography provides (typography ?: typographyFor(resolvedStyle)),
        LocalCompose7Shapes provides (shapes ?: shapesFor(resolvedStyle)),
        LocalCompose7Motion provides (motion ?: motionFor(resolvedStyle)),
        LocalCompose7StyleProperties provides stylePropertiesFor(resolvedStyle),
        LocalNativeBridgeEnabled provides nativeBridge,
        content = content,
    )
}

/** The active style, always concrete — never [Compose7Style.Auto]. */
val currentCompose7Style: Compose7Style
    @Composable @ReadOnlyComposable get() = LocalCompose7Style.current

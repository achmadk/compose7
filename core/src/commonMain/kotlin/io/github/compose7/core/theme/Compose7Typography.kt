package io.github.compose7.core.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Type tokens for one style. Brightness does not affect type — light and dark
 * share a token set; color comes from [Compose7ColorScheme].
 *
 * Role names follow Material3 (`titleLarge`, `bodyMedium`, …) for transferable
 * knowledge; scale, weight, and tracking differ per visual language.
 */
@Immutable
class Compose7Typography(
    val displayLarge: TextStyle,
    val displaySmall: TextStyle,
    val headlineLarge: TextStyle,
    val headlineMedium: TextStyle,
    val headlineSmall: TextStyle,
    val titleLarge: TextStyle,
    val titleMedium: TextStyle,
    val titleSmall: TextStyle,
    val bodyLarge: TextStyle,
    val bodyMedium: TextStyle,
    val bodySmall: TextStyle,
    val labelLarge: TextStyle,
    val labelMedium: TextStyle,
    val labelSmall: TextStyle,
)

/** The active type token set. Provided by [Compose7Theme]. */
val LocalCompose7Typography = compositionLocalOf { materialTypography() }

/** Convenience accessor for the active type token set. */
val Compose7ThemeTypography: Compose7Typography
    @Composable get() = LocalCompose7Typography.current

private fun base(size: Int, lineHeight: Int, weight: FontWeight, tracking: Float = 0f) = TextStyle(
    fontFamily = FontFamily.Default,
    fontSize = size.sp,
    lineHeight = lineHeight.sp,
    fontWeight = weight,
    letterSpacing = tracking.sp,
)

/** Material3-flavored type scale (Roboto on Android, sans-serif elsewhere). */
fun materialTypography() = Compose7Typography(
    displayLarge = base(57, 64, FontWeight.Normal, (-0.25f)),
    displaySmall = base(36, 44, FontWeight.Normal),
    headlineLarge = base(32, 40, FontWeight.Normal),
    headlineMedium = base(28, 36, FontWeight.Normal),
    headlineSmall = base(24, 32, FontWeight.Normal),
    titleLarge = base(22, 28, FontWeight.Normal),
    titleMedium = base(16, 24, FontWeight.Medium, 0.15f),
    titleSmall = base(14, 20, FontWeight.Medium, 0.1f),
    bodyLarge = base(16, 24, FontWeight.Normal, 0.5f),
    bodyMedium = base(14, 20, FontWeight.Normal, 0.25f),
    bodySmall = base(12, 16, FontWeight.Normal, 0.4f),
    labelLarge = base(14, 20, FontWeight.Medium, 0.1f),
    labelMedium = base(12, 16, FontWeight.Medium, 0.5f),
    labelSmall = base(11, 16, FontWeight.Medium, 0.5f),
)

/** Cupertino-flavored type scale (SF Pro; compact, semibold titles, tight tracking). */
fun cupertinoTypography() = Compose7Typography(
    displayLarge = base(34, 41, FontWeight.Bold, (-0.4f)),
    displaySmall = base(22, 28, FontWeight.Bold, (-0.2f)),
    headlineLarge = base(28, 34, FontWeight.Bold, (-0.3f)),
    headlineMedium = base(24, 29, FontWeight.Bold, (-0.2f)),
    headlineSmall = base(20, 25, FontWeight.SemiBold),
    titleLarge = base(17, 22, FontWeight.SemiBold, (-0.4f)),
    titleMedium = base(15, 20, FontWeight.SemiBold),
    titleSmall = base(13, 18, FontWeight.SemiBold),
    bodyLarge = base(17, 22, FontWeight.Normal, (-0.4f)),
    bodyMedium = base(15, 20, FontWeight.Normal),
    bodySmall = base(13, 18, FontWeight.Normal),
    labelLarge = base(15, 20, FontWeight.SemiBold),
    labelMedium = base(12, 16, FontWeight.Normal),
    labelSmall = base(11, 13, FontWeight.Normal),
)

/** DesktopUI7 — Compose7's original desktop language: roomier leading, crisp labels. */
fun desktopUi7Typography() = Compose7Typography(
    displayLarge = base(40, 48, FontWeight.Bold, (-0.5f)),
    displaySmall = base(24, 32, FontWeight.SemiBold),
    headlineLarge = base(30, 38, FontWeight.Bold, (-0.3f)),
    headlineMedium = base(26, 34, FontWeight.SemiBold),
    headlineSmall = base(22, 30, FontWeight.SemiBold),
    titleLarge = base(18, 26, FontWeight.SemiBold),
    titleMedium = base(16, 24, FontWeight.SemiBold),
    titleSmall = base(14, 20, FontWeight.SemiBold),
    bodyLarge = base(15, 24, FontWeight.Normal),
    bodyMedium = base(14, 22, FontWeight.Normal),
    bodySmall = base(13, 18, FontWeight.Normal),
    labelLarge = base(13, 20, FontWeight.SemiBold, 0.2f),
    labelMedium = base(12, 18, FontWeight.Medium, 0.4f),
    labelSmall = base(11, 16, FontWeight.Medium, 0.6f),
)

internal fun typographyFor(style: Compose7Style): Compose7Typography = when (style) {
    Compose7Style.Material -> materialTypography()
    Compose7Style.Cupertino -> cupertinoTypography()
    Compose7Style.DesktopUI7 -> desktopUi7Typography()
    Compose7Style.Auto -> materialTypography()
}

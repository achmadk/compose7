package io.github.compose7.core.theme

import androidx.compose.ui.graphics.Color

/**
 * Per-style, per-brightness token factories (PRD §9: `ColorTokens` per style,
 * each with light/dark variants).
 *
 * Palette sources:
 *  - [Material]  — Material 3 baseline tones (purple primary).
 *  - [Cupertino] — iOS system semantic colors (systemBlue, systemGray, …).
 *  - [DesktopUI7] — an original Compose7 language: teal primary, cool
 *    neutrals, warm amber secondary. Not modeled on Framework7's Aurora.
 */
fun lightMaterialColorScheme() = Compose7ColorScheme(
    primary = Color(0xFF6750A4),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFEADDFF),
    onPrimaryContainer = Color(0xFF21005D),
    secondary = Color(0xFF625B71),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE8DEF8),
    onSecondaryContainer = Color(0xFF1D192B),
    background = Color(0xFFFEF7FF),
    onBackground = Color(0xFF1C1B1F),
    surface = Color(0xFFFEF7FF),
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFE7E0EC),
    onSurfaceVariant = Color(0xFF49454F),
    error = Color(0xFFB3261E),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B),
    outline = Color(0xFF79747E),
    outlineVariant = Color(0xFFCAC4D0),
    scrim = Color(0x99000000),
    highlight = Color(0x1F000000),
)

fun darkMaterialColorScheme() = Compose7ColorScheme(
    primary = Color(0xFFD0BCFF),
    onPrimary = Color(0xFF381E72),
    primaryContainer = Color(0xFF4F378B),
    onPrimaryContainer = Color(0xFFEADDFF),
    secondary = Color(0xFFCCC2DC),
    onSecondary = Color(0xFF332D41),
    secondaryContainer = Color(0xFF4A4458),
    onSecondaryContainer = Color(0xFFE8DEF8),
    background = Color(0xFF141218),
    onBackground = Color(0xFFE6E0E9),
    surface = Color(0xFF141218),
    onSurface = Color(0xFFE6E0E9),
    surfaceVariant = Color(0xFF49454F),
    onSurfaceVariant = Color(0xFFCAC4D0),
    error = Color(0xFFF2B8B5),
    onError = Color(0xFF601410),
    errorContainer = Color(0xFF8C1D18),
    onErrorContainer = Color(0xFFF9DEDC),
    outline = Color(0xFF938F99),
    outlineVariant = Color(0xFF49454F),
    scrim = Color(0x99000000),
    highlight = Color(0x29FFFFFF),
)

// iOS system blue (#007AFF) with white text is 4.02:1 - below WCAG AA, and it
// also fails as link text on the grouped background. Deepened to the nearest
// blue that passes both (5.6:1 on white text, 5.0:1 on the background); PRD §12
// requires contrast-safe defaults.
fun lightCupertinoColorScheme() = Compose7ColorScheme(
    primary = Color(0xFF0066CC),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFE1F0FF),
    onPrimaryContainer = Color(0xFF003E7A),
    secondary = Color(0xFF5856D6),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE7E6FC),
    onSecondaryContainer = Color(0xFF201C6B),
    background = Color(0xFFF2F2F7),
    onBackground = Color(0xFF000000),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF000000),
    surfaceVariant = Color(0xFFF2F2F7),
    onSurfaceVariant = Color(0xFF6D6D72),
    // iOS system red (#FF3B30) with white text is 3.55:1; deepened to
    // #D70015 (5.4:1) so destructive fills stay AA while reading as iOS red.
    error = Color(0xFFD70015),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDDD9),
    onErrorContainer = Color(0xFF8A1509),
    outline = Color(0xFFC6C6C8),
    outlineVariant = Color(0xFFE5E5EA),
    scrim = Color(0x66000000),
    highlight = Color(0x14000000),
)

fun darkCupertinoColorScheme() = Compose7ColorScheme(
    // Bright iOS dark-mode blue stays (it is the link/tint color on black), so
    // the label on a filled control is dark - the same pairing Material's dark
    // scheme uses. White on #0A84FF is only 3.65:1.
    primary = Color(0xFF0A84FF),
    onPrimary = Color(0xFF001428),
    primaryContainer = Color(0xFF003A75),
    onPrimaryContainer = Color(0xFFC9E5FF),
    secondary = Color(0xFF5E5CE6),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFF2B2A6B),
    onSecondaryContainer = Color(0xFFDDDAFF),
    background = Color(0xFF000000),
    onBackground = Color(0xFFFFFFFF),
    surface = Color(0xFF1C1C1E),
    onSurface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFF2C2C2E),
    onSurfaceVariant = Color(0xFF98989E),
    // Bright iOS red stays legible as error text on black (6.2:1), so the
    // label on a filled error control is dark - white would be 3.4:1.
    error = Color(0xFFFF453A),
    onError = Color(0xFF2B0705),
    errorContainer = Color(0xFF60130E),
    onErrorContainer = Color(0xFFFFDAD5),
    outline = Color(0xFF48484A),
    outlineVariant = Color(0xFF2C2C2E),
    scrim = Color(0x99000000),
    highlight = Color(0x1AFFFFFF),
)

fun lightDesktopUi7ColorScheme() = Compose7ColorScheme(
    primary = Color(0xFF0F766E),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFC6EDE9),
    onPrimaryContainer = Color(0xFF053531),
    secondary = Color(0xFFB45309),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFDE8CE),
    onSecondaryContainer = Color(0xFF4A2305),
    background = Color(0xFFF7F8F7),
    onBackground = Color(0xFF16211F),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF16211F),
    surfaceVariant = Color(0xFFEBEFED),
    onSurfaceVariant = Color(0xFF4F5D5A),
    error = Color(0xFFB42318),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFEE4E2),
    onErrorContainer = Color(0xFF7A271A),
    outline = Color(0xFF8E9D99),
    outlineVariant = Color(0xFFDDE3E1),
    scrim = Color(0x99000000),
    highlight = Color(0x1A000000),
)

fun darkDesktopUi7ColorScheme() = Compose7ColorScheme(
    primary = Color(0xFF5EEAD4),
    onPrimary = Color(0xFF00332E),
    primaryContainer = Color(0xFF134E4A),
    onPrimaryContainer = Color(0xFFC6EDE9),
    secondary = Color(0xFFF0A44B),
    onSecondary = Color(0xFF3B1D02),
    secondaryContainer = Color(0xFF54320F),
    onSecondaryContainer = Color(0xFFFDE8CE),
    background = Color(0xFF0E1513),
    onBackground = Color(0xFFE9F0EE),
    surface = Color(0xFF17221F),
    onSurface = Color(0xFFE9F0EE),
    surfaceVariant = Color(0xFF20302B),
    onSurfaceVariant = Color(0xFFA7B5B1),
    error = Color(0xFFF97066),
    onError = Color(0xFF551009),
    errorContainer = Color(0xFF7A271A),
    onErrorContainer = Color(0xFFFEE4E2),
    outline = Color(0xFF596864),
    outlineVariant = Color(0xFF2C3A37),
    scrim = Color(0x99000000),
    highlight = Color(0x1AFFFFFF),
)

/** Color token set for [style] at the given brightness. */
fun colorSchemeFor(style: Compose7Style, darkTheme: Boolean): Compose7ColorScheme = when (style) {
    Compose7Style.Material -> if (darkTheme) darkMaterialColorScheme() else lightMaterialColorScheme()
    Compose7Style.Cupertino -> if (darkTheme) darkCupertinoColorScheme() else lightCupertinoColorScheme()
    Compose7Style.DesktopUI7 -> if (darkTheme) darkDesktopUi7ColorScheme() else lightDesktopUi7ColorScheme()
    Compose7Style.Auto -> if (darkTheme) darkMaterialColorScheme() else lightMaterialColorScheme()
}

package io.github.compose7.core.theme

import androidx.compose.ui.graphics.Color
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * PRD §12: default tokens must meet WCAG-AA contrast where feasible.
 * Every `onX` token is checked against its base in all six palettes.
 */
class TokenContrastTest {

    private fun assertAccessible(palette: String, scheme: Compose7ColorScheme) {
        fun check(name: String, fg: Color, bg: Color, min: Float = 4.5f) {
            val ratio = contrastRatio(fg, bg)
            assertTrue(
                ratio >= min,
                "$palette: $name contrast $ratio < $min (fg=$fg bg=$bg)",
            )
        }
        check("onPrimary", scheme.onPrimary, scheme.primary)
        check("onPrimaryContainer", scheme.onPrimaryContainer, scheme.primaryContainer)
        check("onSecondary", scheme.onSecondary, scheme.secondary)
        check("onSecondaryContainer", scheme.onSecondaryContainer, scheme.secondaryContainer)
        check("onBackground", scheme.onBackground, scheme.background)
        check("onSurface", scheme.onSurface, scheme.surface)
        check("onSurfaceVariant", scheme.onSurfaceVariant, scheme.surfaceVariant)
        check("onError", scheme.onError, scheme.error)
        check("onErrorContainer", scheme.onErrorContainer, scheme.errorContainer)
    }

    @Test
    fun materialLightIsAccessible() = assertAccessible("material-light", lightMaterialColorScheme())

    @Test
    fun materialDarkIsAccessible() = assertAccessible("material-dark", darkMaterialColorScheme())

    @Test
    fun cupertinoLightIsAccessible() = assertAccessible("cupertino-light", lightCupertinoColorScheme())

    @Test
    fun cupertinoDarkIsAccessible() = assertAccessible("cupertino-dark", darkCupertinoColorScheme())

    @Test
    fun desktopUi7LightIsAccessible() = assertAccessible("desktopui7-light", lightDesktopUi7ColorScheme())

    @Test
    fun desktopUi7DarkIsAccessible() = assertAccessible("desktopui7-dark", darkDesktopUi7ColorScheme())

    @Test
    fun styleAndBrightnessSelectDistinctPalettes() {
        val seen = setOf(
            colorSchemeFor(Compose7Style.Material, darkTheme = false),
            colorSchemeFor(Compose7Style.Material, darkTheme = true),
            colorSchemeFor(Compose7Style.Cupertino, darkTheme = false),
            colorSchemeFor(Compose7Style.Cupertino, darkTheme = true),
            colorSchemeFor(Compose7Style.DesktopUI7, darkTheme = false),
            colorSchemeFor(Compose7Style.DesktopUI7, darkTheme = true),
        )
        // reference equality by design (fresh instances), distinct values
        assertTrue(seen.map { it.primary to it.background }.distinct().size == 6)
    }

    @Test
    fun dynamicColorKeepsTextReadable() {
        val base = lightDesktopUi7ColorScheme()
        val loud = base.withDynamicColors(
            newPrimary = Color(0xFFFF0000),
            newSecondary = Color(0xFFFFFF00),
        )
        assertTrue(contrastRatio(loud.onPrimary, loud.primary) >= 4.5f)
        assertTrue(contrastRatio(loud.onSecondary, loud.secondary) >= 4.5f)
        assertTrue(contrastRatio(loud.onPrimaryContainer, loud.primaryContainer) >= 4.5f)
        assertTrue(contrastRatio(loud.onSecondaryContainer, loud.secondaryContainer) >= 4.5f)
    }

    @Test
    fun minimumContrastHelperMovesColorWhenNeeded() {
        val bg = Color.White
        val faint = Color(0xFFEEEEEE)
        assertTrue(contrastRatio(faint, bg) < 1.5f)
        val fixed = faint.withMinimumContrast(bg, 4.5f)
        assertTrue(contrastRatio(fixed, bg) >= 4.5f)
    }
}

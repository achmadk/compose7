package io.github.compose7.components.indicators

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import io.github.compose7.core.theme.Compose7Style
import io.github.compose7.core.theme.Compose7Theme
import io.github.compose7.core.theme.Compose7ThemeColors
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image
import java.io.File
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Golden/screenshot regression tests (PRD §13: "per platform and per style x
 * light/dark"). Rendered offscreen through [ImageComposeScene] at density 1,
 * so no window, display, or platform font is involved - the scenes below draw
 * only token-driven shapes.
 *
 * Baselines live in `src/desktopTest/resources/goldens`. Regenerate them with:
 *
 * ```
 * COMPOSE7_GOLDENS_UPDATE=true ./gradlew :components-indicators:desktopTest
 * ```
 *
 * Review the changed PNGs before committing; a diff means a token, shape, or
 * layout change reached the UI.
 */
@OptIn(ExperimentalComposeUiApi::class)
class GoldenRenderTest {

    private val goldens = File("src/desktopTest/resources/goldens")
    private val update = System.getenv("COMPOSE7_GOLDENS_UPDATE") == "true"

    @Test
    fun materialLight() = assertGolden("indicators-material-light", Compose7Style.Material, dark = false)

    @Test
    fun materialDark() = assertGolden("indicators-material-dark", Compose7Style.Material, dark = true)

    @Test
    fun cupertinoLight() = assertGolden("indicators-cupertino-light", Compose7Style.Cupertino, dark = false)

    @Test
    fun cupertinoDark() = assertGolden("indicators-cupertino-dark", Compose7Style.Cupertino, dark = true)

    @Test
    fun desktopUi7Light() = assertGolden("indicators-desktopui7-light", Compose7Style.DesktopUI7, dark = false)

    @Test
    fun desktopUi7Dark() = assertGolden("indicators-desktopui7-dark", Compose7Style.DesktopUI7, dark = true)

    private fun assertGolden(name: String, style: Compose7Style, dark: Boolean) {
        val actualBytes = renderScene(style, dark)
        val actual = Image.makeFromEncoded(actualBytes).toComposeImageBitmap()
        val actualPixels = actual.toPixelMap()
        assertScenePainted(actualPixels, name)

        val baseline = File(goldens, "$name.png")

        if (update) {
            baseline.parentFile.mkdirs()
            baseline.writeBytes(actualBytes)
            return
        }

        assertTrue(
            baseline.isFile,
            "Missing golden '$name'. Regenerate with COMPOSE7_GOLDENS_UPDATE=true.",
        )
        val expected = Image.makeFromEncoded(baseline.readBytes()).toComposeImageBitmap()

        assertEquals(expected.width, actual.width, "width differs for $name")
        assertEquals(expected.height, actual.height, "height differs for $name")

        val expectedPixels = expected.toPixelMap()
        var differing = 0
        for (y in 0 until expectedPixels.height) {
            for (x in 0 until expectedPixels.width) {
                val delta = channelDelta(expectedPixels[x, y].toArgb(), actualPixels[x, y].toArgb())
                // Anti-aliasing may differ by a hair between hosts; a token or
                // layout change moves whole regions well past this threshold.
                if (delta > 24) differing++
            }
        }
        val ratio = differing.toDouble() / (expectedPixels.width * expectedPixels.height)
        assertTrue(ratio <= 0.01, "$name differs from its golden (${(ratio * 100).roundToInt()}% of pixels)")
    }

    /**
     * Guards against a blank/transparent scene: the indicators cover a real
     * area, so most pixels must differ from the top-left background pixel and
     * the background itself must be opaque.
     */
    private fun assertScenePainted(pixels: androidx.compose.ui.graphics.PixelMap, name: String) {
        val background = pixels[0, 0]
        assertEquals(1f, background.alpha, "$name has a transparent background")

        var painted = 0
        for (y in 0 until pixels.height) {
            for (x in 0 until pixels.width) {
                if (channelDelta(background.toArgb(), pixels[x, y].toArgb()) > 8) painted++
            }
        }
        val ratio = painted.toDouble() / (pixels.width * pixels.height)
        assertTrue(ratio >= 0.01, "$name rendered an almost empty scene (${(ratio * 100).roundToInt()}% painted)")
    }

    /** Largest per-channel difference between two packed ARGB colors. */
    private fun channelDelta(first: Int, second: Int): Int {
        fun channel(color: Int, shift: Int) = (color shr shift) and 0xFF
        return maxOf(
            abs(channel(first, 16) - channel(second, 16)),
            abs(channel(first, 8) - channel(second, 8)),
            abs(channel(first, 0) - channel(second, 0)),
            abs(channel(first, 24) - channel(second, 24)),
        )
    }

    private fun renderScene(style: Compose7Style, dark: Boolean): ByteArray {
        val scene = ImageComposeScene(
            width = Width,
            height = Height,
            density = Density(1f),
            content = {
                Compose7Theme(style = style, darkTheme = dark, dynamicColor = false) {
                    IndicatorsSample()
                }
            },
        )
        val image = scene.render(SettleNanos)
        val bytes = image.encodeToData(EncodedImageFormat.PNG)!!.bytes
        image.close()
        scene.close()
        return bytes
    }

    @Composable
    private fun IndicatorsSample() {
        Box(Modifier.fillMaxSize().background(Compose7ThemeColors.background)) {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                ProgressBar(progress = 0.6f, modifier = Modifier.fillMaxWidth())
                // showValue = false: text would pull in platform fonts, which
                // is exactly the kind of host-dependent rendering this suite
                // must not depend on.
                Gauge(
                    value = 0.7f,
                    showValue = false,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
            }
        }
    }

    private companion object {
        const val Width = 360
        const val Height = 220

        /** Long enough for the token-duration progress/gauge animations to settle. */
        const val SettleNanos = 2_000_000_000L
    }
}

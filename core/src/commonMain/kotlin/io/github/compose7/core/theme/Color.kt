package io.github.compose7.core.theme

import androidx.compose.ui.graphics.Color
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

/** Relative luminance per WCAG 2.1 (0 = black, 1 = white). */
fun Color.luminance(): Float {
    fun channel(c: Float): Float = if (c <= 0.03928f) c / 12.92f else ((c + 0.055f) / 1.055f).pow(2.4f)
    return 0.2126f * channel(red) + 0.7152f * channel(green) + 0.0722f * channel(blue)
}

/** WCAG contrast ratio between two colors (1..21). */
fun contrastRatio(a: Color, b: Color): Float {
    val la = a.luminance()
    val lb = b.luminance()
    val lighter = max(la, lb)
    val darker = min(la, lb)
    return (lighter + 0.05f) / (darker + 0.05f)
}

/** Linear interpolation in sRGB space. [amount] 0 returns [this], 1 returns [other]. */
fun Color.blendWith(other: Color, amount: Float): Color = Color(
    red = red + (other.red - red) * amount,
    green = green + (other.green - green) * amount,
    blue = blue + (other.blue - blue) * amount,
    alpha = alpha + (other.alpha - alpha) * amount,
)

/**
 * Black or white - whichever contrasts more against [background]. Used for
 * text placed on accent surfaces (e.g. dynamic color); the receiver is ignored
 * so call sites read naturally on either color of the pair.
 */
fun Color.bestTextColor(background: Color): Color =
    if (contrastRatio(Color.Black, background) >= contrastRatio(Color.White, background)) {
        Color.Black
    } else {
        Color.White
    }

/**
 * Darkens or lightens toward whichever of black/white sits further from
 * [background], until the pair reaches [target]. Returns the original color if
 * already compliant, and the extreme (black or white) if the target is
 * unreachable.
 */
fun Color.withMinimumContrast(background: Color, target: Float): Color {
    if (contrastRatio(this, background) >= target) return this
    // Move AWAY from the background: toward black on light backgrounds.
    val toward = if (contrastRatio(Color.Black, background) >= contrastRatio(Color.White, background)) {
        Color.Black
    } else {
        Color.White
    }
    if (contrastRatio(toward, background) < target) return toward
    var lo = 0f
    var hi = 1f
    repeat(24) {
        val mid = (lo + hi) / 2f
        val candidate = blendWith(toward, mid)
        if (contrastRatio(candidate, background) >= target) hi = mid else lo = mid
    }
    return blendWith(toward, hi)
}

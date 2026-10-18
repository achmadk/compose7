package io.github.compose7.components.forms

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import io.github.compose7.core.theme.Compose7ThemeColors
import io.github.compose7.core.theme.Compose7ThemeShapes
import io.github.compose7.core.theme.Compose7ThemeTypography
import io.github.compose7.foundation.compose7Clickable
import kotlin.math.max
import kotlin.math.min

/**
 * HSV colour picker: saturation/value square, hue bar, optional alpha bar and
 * preset swatches (PRD §7, P2 / Appendix A "Color Picker").
 *
 * Controlled when [onColorChange] is non-null; otherwise the selection is kept
 * internally with `remember` (PRD §8.5 nullable callback convention). All chrome
 * comes from theme tokens; no icon assets are involved (PRD §4).
 *
 * ```kotlin
 * ColorPicker(color = accent, onColorChange = { accent = it }, showAlpha = true)
 * ```
 *
 * @param showAlpha Adds an opacity bar under the hue bar; the emitted [Color]
 *   carries the selected alpha.
 * @param presets Quick-pick swatches rendered below the bars. Empty hides them.
 */
@Composable
fun ColorPicker(
    color: Color,
    onColorChange: ((Color) -> Unit)? = null,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    showAlpha: Boolean = false,
    presets: List<Color> = emptyList(),
) {
    val colors = Compose7ThemeColors
    val shapes = Compose7ThemeShapes
    val type = Compose7ThemeTypography

    val uncontrolled = remember { mutableStateOf(color) }
    val current = if (onColorChange != null) color else uncontrolled.value
    val emit: (Color) -> Unit = { picked ->
        if (onColorChange != null) onColorChange(picked) else uncontrolled.value = picked
    }

    var hue by remember { mutableStateOf(current.hue()) }
    var saturation by remember { mutableStateOf(current.saturation()) }
    var brightness by remember { mutableStateOf(current.value()) }
    var alpha by remember { mutableStateOf(current.alpha) }

    // Re-sync when the caller drives the value, but keep the previous hue for
    // greys (saturation 0 loses the hue channel on the round trip).
    LaunchedEffect(current) {
        val h = current.hue()
        if (current.saturation() > 0f) hue = h
        saturation = current.saturation()
        brightness = current.value()
        alpha = current.alpha
    }

    fun publish() {
        val picked = Color.hsv(hue * 360f, saturation, brightness, alpha)
        emit(picked)
    }

    Column(modifier = modifier) {
        PickerCanvas(
            modifier = Modifier.fillMaxWidth().height(180.dp),
            enabled = enabled,
            onPosition = { offset, area ->
                saturation = (offset.x / max(area.width, 1f)).coerceIn(0f, 1f)
                brightness = 1f - (offset.y / max(area.height, 1f)).coerceIn(0f, 1f)
                publish()
            },
        ) {
            drawRect(Brush.horizontalGradient(listOf(Color.White, Color.hsv(hue * 360f, 1f, 1f))))
            drawRect(Brush.verticalGradient(listOf(Color.Transparent, Color.Black)))
            val thumbX = saturation * size.width
            val thumbY = (1f - brightness) * size.height
            val radius = 8.dp.toPx()
            drawCircle(Color.White, radius = radius, center = Offset(thumbX, thumbY), style = Stroke(2.dp.toPx()))
            drawCircle(Color.hsv(hue * 360f, saturation, brightness, alpha), radius = radius - 2.dp.toPx(), center = Offset(thumbX, thumbY))
        }

        Spacer(Modifier.height(12.dp))

        PickerCanvas(
            modifier = Modifier.fillMaxWidth().height(14.dp).clip(shapes.small),
            enabled = enabled,
            onPosition = { offset, area ->
                hue = (offset.x / max(area.width, 1f)).coerceIn(0f, 0.9999f)
                publish()
            },
        ) {
            drawRect(
                Brush.horizontalGradient(
                    listOf(
                        Color.Red,
                        Color.Yellow,
                        Color.Green,
                        Color.Cyan,
                        Color.Blue,
                        Color.Magenta,
                        Color.Red,
                    ),
                ),
            )
            val x = hue * size.width
            drawCircle(Color.White, radius = 7.dp.toPx(), center = Offset(x, size.height / 2f), style = Stroke(2.dp.toPx()))
        }

        if (showAlpha) {
            Spacer(Modifier.height(8.dp))
            PickerCanvas(
                modifier = Modifier.fillMaxWidth().height(14.dp).clip(shapes.small),
                enabled = enabled,
                onPosition = { offset, area ->
                    alpha = (offset.x / max(area.width, 1f)).coerceIn(0f, 1f)
                    publish()
                },
            ) {
                drawCheckerboard()
                val opaque = Color.hsv(hue * 360f, saturation, brightness, 1f)
                drawRect(Brush.horizontalGradient(listOf(opaque.copy(alpha = 0f), opaque)))
                val x = alpha * size.width
                drawCircle(Color.White, radius = 7.dp.toPx(), center = Offset(x, size.height / 2f), style = Stroke(2.dp.toPx()))
            }
        }

        if (presets.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                presets.forEach { preset ->
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(shapes.badge)
                            .border(1.dp, colors.outlineVariant, shapes.badge)
                            .compose7Clickable(
                                enabled = enabled,
                                role = Role.Button,
                                shape = shapes.badge,
                            ) {
                                hue = preset.hue()
                                saturation = preset.saturation()
                                brightness = preset.value()
                                alpha = preset.alpha
                                emit(preset)
                            },
                    ) {
                        Canvas(Modifier.fillMaxWidth().height(28.dp)) {
                            drawRect(preset)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        BasicText(
            text = current.toHex(),
            style = type.labelMedium.copy(color = colors.onSurfaceVariant),
        )
    }
}

/**
 * Square capture surface used by [ColorPicker]: runs [onPosition] immediately on
 * press (no touch slop) and on every drag sample, in canvas-local coordinates.
 */
@Composable
private fun PickerCanvas(
    modifier: Modifier,
    enabled: Boolean,
    onPosition: (Offset, Size) -> Unit,
    draw: androidx.compose.ui.graphics.drawscope.DrawScope.() -> Unit,
) {
    Canvas(
        modifier = modifier
            .then(
                if (enabled) {
                    Modifier.pointerInput(onPosition) {
                        awaitEachGesture {
                            val area = Size(size.width.toFloat(), size.height.toFloat())
                            val down = awaitFirstDown()
                            down.consume()
                            onPosition(down.position, area)
                            var event = awaitPointerEvent()
                            while (event.changes.any { it.pressed }) {
                                event.changes.forEach { it.consume() }
                                onPosition(event.changes.first().position, area)
                                event = awaitPointerEvent()
                            }
                        }
                    }
                } else {
                    Modifier
                },
            ),
        onDraw = draw,
    )
}

/** Small neutral checkerboard behind the alpha bar (token-derived greys). */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawCheckerboard() {
    val light = Color.White
    val dark = Color(0xFFD4D4D4)
    val cell = 6.dp.toPx()
    var y = 0f
    var row = 0
    while (y < size.height) {
        var x = 0f
        var column = 0
        while (x < size.width) {
            drawRect(
                color = if ((row + column) % 2 == 0) light else dark,
                topLeft = Offset(x, y),
                size = Size(cell, cell),
            )
            x += cell
            column++
        }
        y += cell
        row++
    }
}

private fun Color.hue(): Float {
    val max = max(red, max(green, blue))
    val min = min(red, min(green, blue))
    val delta = max - min
    if (delta == 0f) return 0f
    val raw = when (max) {
        red -> ((green - blue) / delta).mod(6f)
        green -> (blue - red) / delta + 2f
        else -> (red - green) / delta + 4f
    }
    return raw / 6f
}

private fun Color.saturation(): Float {
    val max = max(red, max(green, blue))
    return if (max == 0f) 0f else (max - min(red, min(green, blue))) / max
}

private fun Color.value(): Float = max(red, max(green, blue))

private fun Color.toHex(): String {
    fun channel(value: Float): String =
        (value * 255f).toInt().coerceIn(0, 255).toString(16).padStart(2, '0').uppercase()
    val rgb = "#${channel(red)}${channel(green)}${channel(blue)}"
    return if (alpha >= 1f) rgb else "$rgb${channel(alpha)}"
}

package io.github.compose7.components.indicators

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.compose7.core.theme.Compose7MotionTokens
import io.github.compose7.core.theme.Compose7Style
import io.github.compose7.core.theme.Compose7ThemeColors
import io.github.compose7.core.theme.Compose7ThemeTypography
import io.github.compose7.core.theme.currentCompose7Style
import kotlin.math.roundToInt

/** Gauge stroke thickness per style; sized against a 96dp default dial. */
@Composable
internal fun currentGaugeStrokeWidth(): Dp = when (currentCompose7Style) {
    Compose7Style.Cupertino -> 8.dp
    Compose7Style.DesktopUI7 -> 8.dp
    else -> 10.dp
}

/**
 * Circular gauge (PRD §7, P1: "Gauge").
 *
 * Arc track plus an animated value arc and an optional centered label. Value
 * changes ease with the active motion tokens. One API, three renderings
 * (PRD §9): Material/Cupertino — rounded arc caps; DesktopUI7 — crisp flat
 * caps. Defaults to a 240° sweep from 150°, leaving a gap at the bottom.
 *
 * ```kotlin
 * Gauge(value = batteryFraction)                       // "87%" in the middle
 * Gauge(value = speed / maxSpeed, valueText = "$speed km/h")
 * Gauge(value = v, label = { BasicText("CPU", …) })
 * ```
 *
 * @param value Completion in `0f..1f`; values are clamped.
 * @param showValue Whether the centered value text is drawn.
 * @param valueText Explicit center text; `null` renders value as a percentage.
 * @param label Optional slot rendered above the value text (e.g. a caption).
 * @param startAngle Angle of the arc's start, degrees (0° = 3 o'clock).
 * @param sweepAngle Total sweep of the track, degrees.
 * @param size Dial diameter.
 * @param strokeWidth Arc thickness; defaults per style.
 * @param trackColor Empty-arc color token.
 * @param valueColor Filled-arc color token.
 * @param contentColor Color for the centered label/value text.
 */
@Composable
fun Gauge(
    value: Float,
    modifier: Modifier = Modifier,
    showValue: Boolean = true,
    valueText: String? = null,
    label: (@Composable ColumnScope.() -> Unit)? = null,
    startAngle: Float = 150f,
    sweepAngle: Float = 240f,
    size: Dp = 96.dp,
    strokeWidth: Dp = currentGaugeStrokeWidth(),
    trackColor: Color = Compose7ThemeColors.surfaceVariant,
    valueColor: Color = Compose7ThemeColors.primary,
    contentColor: Color = Compose7ThemeColors.onSurface,
) {
    val style = currentCompose7Style
    val motion = Compose7MotionTokens
    val coerced = value.coerceIn(0f, 1f)
    val animated by animateFloatAsState(
        targetValue = coerced,
        animationSpec = tween(motion.mediumDurationMs, easing = motion.standardEasing),
        label = "gauge",
    )
    val cap = if (style == Compose7Style.DesktopUI7) StrokeCap.Butt else StrokeCap.Round

    Box(
        modifier = modifier
            .size(size)
            .semantics { progressBarRangeInfo = ProgressBarRangeInfo(coerced, 0f..1f) },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = strokeWidth.toPx()
            val inset = stroke / 2f
            val arcTopLeft = Offset(inset, inset)
            val arcSize = Size(this.size.width - stroke, this.size.height - stroke)
            drawArc(
                color = trackColor,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = arcTopLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = cap),
            )
            val valueSweep = sweepAngle * animated
            if (valueSweep > 0f) {
                drawArc(
                    color = valueColor,
                    startAngle = startAngle,
                    sweepAngle = valueSweep,
                    useCenter = false,
                    topLeft = arcTopLeft,
                    size = arcSize,
                    style = Stroke(width = stroke, cap = cap),
                )
            }
        }
        if (showValue || label != null) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                label?.invoke(this)
                if (showValue) {
                    BasicText(
                        text = valueText ?: "${(coerced * 100).roundToInt()}%",
                        style = Compose7ThemeTypography.titleMedium.copy(color = contentColor),
                    )
                }
            }
        }
    }
}

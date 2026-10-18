package io.github.compose7.components.indicators

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import io.github.compose7.core.theme.Compose7Motion
import io.github.compose7.core.theme.Compose7MotionTokens
import io.github.compose7.core.theme.Compose7Style
import io.github.compose7.core.theme.Compose7ThemeColors
import io.github.compose7.core.theme.currentCompose7Style

/** Stroke thickness per style: bold Material, hairline Cupertino, compact DesktopUI7. */
@Composable
internal fun currentPreloaderStrokeWidth(): Dp = when (currentCompose7Style) {
    Compose7Style.Cupertino -> 2.5.dp
    Compose7Style.DesktopUI7 -> 3.dp
    else -> 4.dp
}

/**
 * Activity spinner / preloader (PRD §7, P0: "Preloader/spinner").
 *
 * A drawn rotating arc — never an icon asset (PRD §4). One API, three
 * renderings (PRD §9): Material — constant-speed rounded-cap arc;
 * Cupertino — iOS-style tapered arc (sweep swells and shrinks while the
 * rotation keeps a steady pace); DesktopUI7 — minimal arc on a snappier loop.
 * Loop durations derive from [io.github.compose7.core.theme.Compose7Motion].
 *
 * ```kotlin
 * Preloader()
 * Preloader(size = 40.dp, color = Compose7ThemeColors.primary)
 * ```
 *
 * @param size Overall diameter of the spinner.
 * @param color Arc color token.
 * @param strokeWidth Arc thickness; defaults per style.
 */
@Composable
fun Preloader(
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    color: Color = Compose7ThemeColors.primary,
    strokeWidth: Dp = currentPreloaderStrokeWidth(),
) {
    val style = currentCompose7Style
    val motion = Compose7MotionTokens
    val transition = rememberInfiniteTransition(label = "preloader")

    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(rotationLoopMs(style, motion), easing = LinearEasing),
        ),
        label = "rotation",
    )
    val targetSweep = if (style == Compose7Style.DesktopUI7) 200f else 270f
    // Cupertino swells from a short segment to a long one; other styles keep a constant sweep
    // (initial == target renders as a steady value on those).
    val sweep by transition.animateFloat(
        initialValue = if (style == Compose7Style.Cupertino) 40f else targetSweep,
        targetValue = targetSweep,
        animationSpec = if (style == Compose7Style.Cupertino) {
            infiniteRepeatable(
                animation = tween(motion.mediumDurationMs, easing = motion.standardEasing),
                repeatMode = RepeatMode.Reverse,
            )
        } else {
            infiniteRepeatable(animation = tween(motion.slowDurationMs, easing = LinearEasing))
        },
        label = "sweep",
    )

    Canvas(
        modifier = modifier
            .semantics { progressBarRangeInfo = ProgressBarRangeInfo.Indeterminate }
            .size(size),
    ) {
        val stroke = strokeWidth.toPx()
        val inset = stroke / 2f
        val arcSize = Size(this.size.width - stroke, this.size.height - stroke)
        drawArc(
            color = color,
            startAngle = rotation,
            sweepAngle = sweep,
            useCenter = false,
            topLeft = Offset(inset, inset),
            size = arcSize,
            style = Stroke(width = stroke, cap = StrokeCap.Round),
        )
    }
}

/** Full-turn loop duration from motion tokens; DesktopUI7 turns snappier. */
private fun rotationLoopMs(style: Compose7Style, motion: Compose7Motion): Int =
    if (style == Compose7Style.DesktopUI7) motion.slowDurationMs * 2 else motion.slowDurationMs * 3

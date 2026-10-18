package io.github.compose7.components.indicators

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import io.github.compose7.core.theme.Compose7MotionTokens
import io.github.compose7.core.theme.Compose7Style
import io.github.compose7.core.theme.Compose7ThemeColors
import io.github.compose7.core.theme.Compose7ThemeShapes
import io.github.compose7.core.theme.currentCompose7Style

/** Track height per style: fat rounded Material, hairline Cupertino, crisp DesktopUI7. */
@Composable
internal fun currentProgressBarHeight(): Dp = when (currentCompose7Style) {
    Compose7Style.Cupertino -> 2.5.dp
    Compose7Style.DesktopUI7 -> 6.dp
    else -> 8.dp
}

/**
 * Determinate progress bar (PRD §7, P0: "Progressbar").
 *
 * One API, three renderings (PRD §9): Material — rounded 8dp track with a
 * primary fill; Cupertino — hairline iOS-style bar; DesktopUI7 — crisp 6dp
 * track with small corners. Fills start-to-end, so RTL mirrors automatically.
 *
 * ```kotlin
 * ProgressBar(progress = uploadFraction, modifier = Modifier.fillMaxWidth())
 * ```
 *
 * @param progress Completion in `0f..1f`; values are clamped.
 * @param height Track thickness; defaults per style.
 * @param trackColor Empty-track color token.
 * @param fillColor Filled-portion color token.
 */
@Composable
fun ProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    height: Dp = currentProgressBarHeight(),
    trackColor: Color = Compose7ThemeColors.secondaryContainer,
    fillColor: Color = Compose7ThemeColors.primary,
) {
    val motion = Compose7MotionTokens
    val cornerDp = currentTrackCorner()
    val coerced = progress.coerceIn(0f, 1f)
    val animated by animateFloatAsState(
        targetValue = coerced,
        animationSpec = tween(motion.mediumDurationMs, easing = motion.standardEasing),
        label = "progress",
    )
    Canvas(
        modifier = modifier.semantics {
            progressBarRangeInfo = ProgressBarRangeInfo(coerced, 0f..1f)
        },
    ) {
        val h = size.height
        val radius = trackCornerRadiusPx(h, cornerDp)
        drawRoundRect(color = trackColor, cornerRadius = radius)
        val fillWidth = size.width * animated
        if (fillWidth > 0f) {
            val x = if (layoutDirection == LayoutDirection.Rtl) size.width - fillWidth else 0f
            drawRoundRect(
                color = fillColor,
                topLeft = Offset(x, 0f),
                size = Size(fillWidth, h),
                cornerRadius = CornerRadius(
                    minOf(radius.x, fillWidth / 2f),
                    minOf(radius.y, h / 2f),
                ),
            )
        }
    }
}

/**
 * Indeterminate (infinite) progress bar (PRD §7, P0: "Progressbar").
 *
 * Material — wide rounded segment sliding end-to-end at constant speed;
 * Cupertino — thin bar with a slower slide plus a subtle alpha pulse;
 * DesktopUI7 — narrow segment with a snappier pass.
 *
 * ```kotlin
 * IndeterminateProgressBar(modifier = Modifier.fillMaxWidth())
 * ```
 *
 * @param height Track thickness; defaults per style.
 * @param trackColor Empty-track color token.
 * @param fillColor Sliding-segment color token.
 */
@Composable
fun IndeterminateProgressBar(
    modifier: Modifier = Modifier,
    height: Dp = currentProgressBarHeight(),
    trackColor: Color = Compose7ThemeColors.secondaryContainer,
    fillColor: Color = Compose7ThemeColors.primary,
) {
    val style = currentCompose7Style
    val motion = Compose7MotionTokens
    val cornerDp = currentTrackCorner()
    val transition = rememberInfiniteTransition(label = "progress-indeterminate")
    val slide by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(motion.slowDurationMs * 2, easing = LinearEasing),
        ),
        label = "slide",
    )
    // Cupertino only: a constant target equal to the initial value keeps other styles static.
    val pulse by transition.animateFloat(
        initialValue = 1f,
        targetValue = if (style == Compose7Style.Cupertino) 0.35f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(motion.mediumDurationMs, easing = motion.standardEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse",
    )
    val segmentFraction = when (style) {
        Compose7Style.Cupertino -> 0.3f
        Compose7Style.DesktopUI7 -> 0.25f
        else -> 0.4f
    }
    Canvas(
        modifier = modifier.semantics {
            progressBarRangeInfo = ProgressBarRangeInfo.Indeterminate
        },
    ) {
        val h = size.height
        val radius = trackCornerRadiusPx(h, cornerDp)
        drawRoundRect(color = trackColor, cornerRadius = radius)
        val segmentWidth = size.width * segmentFraction
        val startX = slide * (size.width - segmentWidth)
        val x = if (layoutDirection == LayoutDirection.Rtl) {
            size.width - startX - segmentWidth
        } else {
            startX
        }
        drawRoundRect(
            color = fillColor,
            topLeft = Offset(x, 0f),
            size = Size(segmentWidth, h),
            cornerRadius = CornerRadius(minOf(radius.x, segmentWidth / 2f), radius.y),
            alpha = pulse,
        )
    }
}

/** Track corner radius in px: full pill everywhere except crisp-corned DesktopUI7. */
@Composable
private fun currentTrackCorner(): CornerSize? =
    if (currentCompose7Style == Compose7Style.DesktopUI7) {
        Compose7ThemeShapes.extraSmall.topStart
    } else {
        null
    }

/** Resolves a track [CornerRadius] in px; `null` [corner] means fully rounded (pill). */
private fun Density.trackCornerRadiusPx(heightPx: Float, corner: CornerSize?): CornerRadius {
    val cornerPx = if (corner != null) {
        minOf(heightPx / 2f, corner.toPx(Size(heightPx, heightPx), this))
    } else {
        heightPx / 2f
    }
    return CornerRadius(cornerPx, cornerPx)
}

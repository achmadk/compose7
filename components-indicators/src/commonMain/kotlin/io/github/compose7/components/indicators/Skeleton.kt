package io.github.compose7.components.indicators

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.compose7.core.theme.Compose7Motion
import io.github.compose7.core.theme.Compose7MotionTokens
import io.github.compose7.core.theme.Compose7Style
import io.github.compose7.core.theme.Compose7ThemeColors
import io.github.compose7.core.theme.Compose7ThemeShapes
import io.github.compose7.core.theme.currentCompose7Style

/** Shimmer loop duration from motion tokens: slow and soft on Material, quicker elsewhere. */
private fun shimmerLoopMs(style: Compose7Style, motion: Compose7Motion): Int =
    if (style == Compose7Style.DesktopUI7) {
        motion.mediumDurationMs * 2
    } else {
        motion.slowDurationMs * 2
    }

/**
 * Shimmering skeleton placeholder (PRD §7, P1: Loading states; PRD §16,
 * Phase 3: "Skeleton").
 *
 * A token-colored base block with a moving highlight gradient sweeping
 * across it. Loop speed derives from the active motion tokens. Set
 * [shimmerEnabled] to `false` for a static placeholder (e.g. honoring
 * reduced-motion preferences).
 *
 * ```kotlin
 * Skeleton(Modifier.fillMaxWidth().height(120.dp), shape = Compose7ThemeShapes.card)
 * SkeletonText(lines = 3, modifier = Modifier.fillMaxWidth())
 * ```
 *
 * @param shape Clip shape for the placeholder block.
 * @param baseColor Base fill color token.
 * @param highlightColor Swept highlight color token (baked-alpha overlay).
 * @param shimmerEnabled Whether the highlight animates; `false` draws the flat base.
 */
@Composable
fun Skeleton(
    modifier: Modifier = Modifier,
    shape: Shape = Compose7ThemeShapes.small,
    baseColor: Color = Compose7ThemeColors.surfaceVariant,
    highlightColor: Color = Compose7ThemeColors.highlight,
    shimmerEnabled: Boolean = true,
) {
    val style = currentCompose7Style
    val motion = Compose7MotionTokens
    val transition = rememberInfiniteTransition(label = "skeleton")
    // Constant target when disabled keeps the placeholder static without conditional calls.
    val shimmer by transition.animateFloat(
        initialValue = 0f,
        targetValue = if (shimmerEnabled) 1f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(shimmerLoopMs(style, motion), easing = LinearEasing),
        ),
        label = "shimmer",
    )
    Box(
        modifier = modifier
            .clip(shape)
            .drawBehind {
                drawRect(baseColor)
                if (shimmerEnabled) {
                    val band = size.width
                    val startX = -band + 2f * band * shimmer
                    drawRect(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color.Transparent, highlightColor, Color.Transparent),
                            startX = startX,
                            endX = startX + band,
                        ),
                    )
                }
            },
    )
}

/**
 * Multi-line text placeholder: [lines] rounded bars, the last one shorter —
 * the classic loading-skeleton pattern (PRD §7, P1: Loading states).
 *
 * ```kotlin
 * SkeletonText(lines = 3, modifier = Modifier.fillMaxWidth())
 * ```
 *
 * @param lines Number of placeholder bars.
 * @param lineHeight Thickness of each bar.
 * @param lineSpacing Vertical gap between bars.
 * @param lastLineWidthFraction Width of the final bar relative to the full width.
 */
@Composable
fun SkeletonText(
    lines: Int,
    modifier: Modifier = Modifier,
    lineHeight: Dp = 12.dp,
    lineSpacing: Dp = 8.dp,
    lastLineWidthFraction: Float = 0.62f,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(lineSpacing),
    ) {
        repeat(lines) { index ->
            val isLast = lines > 1 && index == lines - 1
            Skeleton(
                modifier = Modifier
                    .fillMaxWidth(if (isLast) lastLineWidthFraction else 1f)
                    .height(lineHeight),
                shape = RoundedCornerShape(lineHeight / 2f),
            )
        }
    }
}

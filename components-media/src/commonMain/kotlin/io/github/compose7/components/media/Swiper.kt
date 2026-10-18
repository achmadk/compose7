package io.github.compose7.components.media

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerScope
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import io.github.compose7.core.theme.Compose7MotionTokens
import io.github.compose7.core.theme.Compose7Style
import io.github.compose7.core.theme.Compose7ThemeColors
import io.github.compose7.core.theme.currentCompose7Style
import kotlin.math.absoluteValue

/**
 * Paging carousel (PRD §7 P1 / Appendix A "Swiper" / §8.6).
 *
 * A deliberately thin wrapper over Compose's `HorizontalPager`/`VerticalPager`
 * (PRD §8.3 philosophy): paging, snapping, and scrolling stay Compose-native —
 * bounded, never looping — while Compose7 adds the style-token-driven
 * indicator dots. Both axes are supported via [orientation].
 *
 * ```kotlin
 * val state = rememberPagerState { banners.size }
 * Swiper(state = state) { page -> Banner(banners[page]) }
 * Swiper(pageCount = 4, orientation = Orientation.Vertical) { … }
 * ```
 *
 * @param state Pager state. When omitted, an internal state is remembered
 *   from [pageCount]. When both are supplied, the state's own page count wins.
 * @param pageCount Number of pages for the internally remembered state;
 *   ignored when [state] is provided.
 * @param orientation [Orientation.Horizontal] (default) or [Orientation.Vertical].
 * @param indicator Whether to draw indicator dots; hidden for 0–1 pages.
 * @param indicatorAlignment Placement of the dots within the swiper bounds.
 * @param indicatorColor Inactive dot color; defaults to the `onSurface` token
 *   at a low alpha.
 * @param indicatorActiveColor Active dot color; defaults to the `primary` token.
 * @param content Page content; receives the zero-based page index.
 */
@Composable
fun Swiper(
    modifier: Modifier = Modifier,
    pageCount: Int? = null,
    state: PagerState = rememberPagerState { pageCount ?: 0 },
    orientation: Orientation = Orientation.Horizontal,
    indicator: Boolean = true,
    indicatorAlignment: Alignment = Alignment.BottomCenter,
    indicatorColor: Color = Compose7ThemeColors.onSurface.copy(alpha = 0.30f),
    indicatorActiveColor: Color = Compose7ThemeColors.primary,
    content: @Composable PagerScope.(page: Int) -> Unit,
) {
    // The state's page count is authoritative whether the state was passed
    // in or remembered from `pageCount`.
    val count = state.pageCount

    Box(modifier = modifier) {
        if (orientation == Orientation.Horizontal) {
            HorizontalPager(
                state = state,
                userScrollEnabled = true,
            ) { page -> content(page) }
        } else {
            VerticalPager(
                state = state,
                userScrollEnabled = true,
            ) { page -> content(page) }
        }

        if (indicator && count > 1) {
            SwiperIndicator(
                pageCount = count,
                currentPage = state.currentPage,
                currentPageOffsetFraction = state.currentPageOffsetFraction,
                color = indicatorColor,
                activeColor = indicatorActiveColor,
                modifier = Modifier.align(indicatorAlignment),
            )
        }
    }
}

/**
 * Indicator dots: inactive dots are circles in [color], and proximity to the
 * active page morphs a dot into a pill tinted with [activeColor] — the active
 * dot is always the widest. Dot geometry varies per style token
 * (PRD §9: single API, style-specific rendering).
 */
@Composable
private fun SwiperIndicator(
    pageCount: Int,
    currentPage: Int,
    currentPageOffsetFraction: Float,
    color: Color,
    activeColor: Color,
    modifier: Modifier = Modifier,
) {
    val style = currentCompose7Style
    val motion = Compose7MotionTokens
    val dotSize = swiperDotSize(style)
    val activeWidthFactor = swiperActiveWidthFactor(style)
    val pagePosition = currentPage + currentPageOffsetFraction

    Row(
        modifier = modifier.padding(vertical = 8.dp, horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(pageCount) { page ->
            val proximity = (1f - (pagePosition - page).absoluteValue).coerceIn(0f, 1f)
            val active by animateFloatAsState(
                targetValue = proximity,
                animationSpec = tween(motion.mediumDurationMs, easing = motion.standardEasing),
                label = "swiperDotActive",
            )
            val width by animateDpAsState(
                targetValue = lerp(dotSize, dotSize * activeWidthFactor, active),
                animationSpec = tween(motion.mediumDurationMs, easing = motion.standardEasing),
                label = "swiperDotWidth",
            )
            Canvas(
                modifier = Modifier
                    .width(width)
                    .height(dotSize),
            ) {
                val diameter = size.height
                drawRoundRect(
                    color = lerp(color, activeColor, active),
                    size = Size(size.width, diameter),
                    cornerRadius = CornerRadius(diameter / 2f),
                )
            }
        }
    }
}

/** Inactive dot diameter — Cupertino mirrors `UIPageControl`, Material the page indicator. */
private fun swiperDotSize(style: Compose7Style): Dp = when (style) {
    Compose7Style.Cupertino -> 7.dp
    Compose7Style.DesktopUI7 -> 6.dp
    else -> 8.dp // Material and Auto
}

/** Active pill width as a multiple of the dot diameter. */
private fun swiperActiveWidthFactor(style: Compose7Style): Float = when (style) {
    Compose7Style.Cupertino -> 2.5f
    Compose7Style.DesktopUI7 -> 1.75f
    else -> 2f // Material and Auto
}

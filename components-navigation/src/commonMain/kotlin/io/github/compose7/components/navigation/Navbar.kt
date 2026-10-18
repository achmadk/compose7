package io.github.compose7.components.navigation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.compose7.core.theme.Compose7MotionTokens
import io.github.compose7.core.theme.Compose7Style
import io.github.compose7.core.theme.Compose7StyleInfo
import io.github.compose7.core.theme.Compose7ThemeColors
import io.github.compose7.core.theme.Compose7ThemeTypography
import io.github.compose7.core.theme.currentCompose7Style
import io.github.compose7.foundation.Compose7Surface
import io.github.compose7.foundation.Divider
import io.github.compose7.foundation.cardElevation

/**
 * Top navigation bar (PRD §8.2 / Appendix A "Navbar"): title, optional
 * subtitle, leading/trailing action slots, and an iOS-style large-title
 * collapse tied to [scrollState].
 *
 * Applies the top system-bar inset itself, so the page below should pass
 * `topSafeArea = false` to `foundation.Page` to avoid doubling the inset.
 * The status-bar strip takes [containerColor].
 *
 * Collapse behavior per style (PRD §9): Cupertino collapses a left-aligned
 * large title up into a centered compact title; Material collapses into a
 * left-aligned compact title; DesktopUI7 renders a static compact title
 * (`supportsLargeTitleCollapse` is false). A hairline shows on scroll
 * (Cupertino) or always (DesktopUI7); Material elevates 1dp when scrolled.
 *
 * ```kotlin
 * Navbar(title = "Inbox", scrollState = scrollState, largeTitle = true)
 * ```
 */
@Composable
fun Navbar(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    scrollState: ScrollState? = null,
    largeTitle: Boolean = false,
    containerColor: Color = Compose7ThemeColors.surface,
    contentColor: Color = Compose7ThemeColors.onSurface,
    leading: (@Composable RowScope.() -> Unit)? = null,
    trailing: (@Composable RowScope.() -> Unit)? = null,
) {
    val style = currentCompose7Style
    val colors = Compose7ThemeColors
    val type = Compose7ThemeTypography
    val motion = Compose7MotionTokens

    val collapseActive = largeTitle && scrollState != null && Compose7StyleInfo.supportsLargeTitleCollapse
    val showLargeRow = largeTitle && style != Compose7Style.DesktopUI7
    val collapseRangePx = with(LocalDensity.current) { LargeTitleRowHeight.toPx() }
    val collapseProgress by remember(scrollState, collapseActive, collapseRangePx) {
        val scrolling = if (collapseActive) scrollState else null
        if (scrolling != null) {
            derivedStateOf { (scrolling.value / collapseRangePx).coerceIn(0f, 1f) }
        } else {
            derivedStateOf { 0f }
        }
    }
    val scrolled by remember(scrollState) { derivedStateOf { (scrollState?.value ?: 0) > 0 } }

    val showHairline = when (style) {
        Compose7Style.Cupertino -> scrolled
        Compose7Style.DesktopUI7 -> true
        else -> false
    }

    Compose7Surface(
        modifier = modifier.fillMaxWidth(),
        color = containerColor,
        contentColor = contentColor,
        elevation = if (scrolled) cardElevation() else 0.dp,
    ) {
        Column(
            modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars.only(WindowInsetsSides.Top)),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(topBarHeight(style))
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                leading?.invoke(this)
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = if (collapseActive && style == Compose7Style.Cupertino) {
                        Alignment.Center
                    } else {
                        Alignment.CenterStart
                    },
                ) {
                    val compactAlpha = when {
                        collapseActive -> collapseProgress
                        showLargeRow -> 0f
                        else -> 1f
                    }
                    Column(
                        modifier = Modifier
                            .graphicsLayer {
                                alpha = compactAlpha
                                if (collapseActive && style == Compose7Style.Cupertino) {
                                    translationY = (1f - compactAlpha) * 10.dp.toPx()
                                }
                            }
                            .semantics { heading() },
                        horizontalAlignment = if (collapseActive && style == Compose7Style.Cupertino) {
                            Alignment.CenterHorizontally
                        } else {
                            Alignment.Start
                        },
                    ) {
                        BasicText(
                            text = title,
                            style = (if (style == Compose7Style.DesktopUI7) {
                                type.titleMedium
                            } else {
                                type.titleLarge
                            }).copy(color = contentColor),
                        )
                        if (subtitle != null && !showLargeRow) {
                            BasicText(
                                text = subtitle,
                                style = type.bodySmall.copy(color = colors.onSurfaceVariant),
                            )
                        }
                    }
                }
                trailing?.invoke(this)
            }
            if (showLargeRow) {
                val largeAlpha = 1f - collapseProgress
                val largeSubtitleExtra = if (subtitle != null) 22.dp else 0.dp
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height((LargeTitleRowHeight + largeSubtitleExtra) * largeAlpha)
                        .graphicsLayer {
                            alpha = largeAlpha
                            translationY = -LargeTitleRowHeight.toPx() * collapseProgress
                        }
                        .padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
                ) {
                    BasicText(
                        text = title,
                        style = (if (style == Compose7Style.Cupertino) {
                            type.displayLarge
                        } else {
                            type.headlineMedium
                        }).copy(color = contentColor),
                    )
                    if (subtitle != null) {
                        BasicText(
                            text = subtitle,
                            style = type.bodyMedium.copy(color = colors.onSurfaceVariant),
                        )
                    }
                }
            }
            if (showHairline) {
                Divider(color = colors.outlineVariant, insetStart = 0.dp)
            }
        }
    }
}

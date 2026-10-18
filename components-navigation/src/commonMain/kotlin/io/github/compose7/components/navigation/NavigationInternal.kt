package io.github.compose7.components.navigation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.compose7.core.theme.Compose7Style

/** Content height of Navbar/Toolbar: 44pt iOS top bar, 56dp Material top app bar, 48dp DesktopUI7. */
internal fun topBarHeight(style: Compose7Style): Dp = when (style) {
    Compose7Style.Cupertino -> 44.dp
    Compose7Style.DesktopUI7 -> 48.dp
    else -> 56.dp
}

/** Content height of Subnavbar per style. */
internal fun subnavbarHeight(style: Compose7Style): Dp = when (style) {
    Compose7Style.DesktopUI7 -> 36.dp
    else -> 40.dp
}

/** Content height of Tabbar: 49pt iOS, 56dp Material, 48dp DesktopUI7 — safe-area inset stacks on top. */
internal fun tabbarHeight(style: Compose7Style): Dp = when (style) {
    Compose7Style.Cupertino -> 49.dp
    Compose7Style.DesktopUI7 -> 48.dp
    else -> 56.dp
}

/** Content height of Tabs per style — meets the platform minimum touch target. */
internal fun tabsHeight(style: Compose7Style): Dp = when (style) {
    Compose7Style.Cupertino -> 44.dp
    else -> 48.dp
}

/** Content height of SegmentedControl: 32pt iOS, 40dp M3 segmented button, 32dp DesktopUI7. */
internal fun segmentedControlHeight(style: Compose7Style): Dp = when (style) {
    Compose7Style.Material -> 40.dp
    else -> 32.dp
}

/** Height of the Searchbar field per style. */
internal fun searchbarHeight(style: Compose7Style): Dp = when (style) {
    Compose7Style.Cupertino -> 36.dp
    Compose7Style.DesktopUI7 -> 32.dp
    else -> 40.dp
}

/** Default Panel width per style. */
internal fun defaultPanelWidth(style: Compose7Style): Dp = when (style) {
    Compose7Style.Cupertino -> 260.dp
    Compose7Style.DesktopUI7 -> 300.dp
    else -> 280.dp
}

/** Fixed height of the Navbar large-title row that collapses on scroll. */
internal val LargeTitleRowHeight = 52.dp

/**
 * Drawn magnifier — a canvas primitive, never an icon asset (PRD §4:
 * bring-your-own-icons, no bundled set).
 */
@Composable
internal fun MagnifierGlyph(
    color: Color,
    modifier: Modifier = Modifier,
    size: Dp = 16.dp,
    thickness: Dp = 1.5.dp,
) {
    Canvas(modifier = modifier.size(size)) {
        val s = size.toPx()
        val t = thickness.toPx()
        val radius = s * 0.30f
        val center = Offset(s * 0.40f, s * 0.40f)
        drawCircle(color = color, radius = radius, center = center, style = Stroke(width = t))
        drawLine(
            color = color,
            start = center + Offset(radius * 0.72f, radius * 0.72f),
            end = Offset(s * 0.86f, s * 0.86f),
            strokeWidth = t,
            cap = StrokeCap.Round,
        )
    }
}

/** Drawn clear (×) glyph — see [MagnifierGlyph] for the no-icons rule. */
@Composable
internal fun CloseGlyph(
    color: Color,
    modifier: Modifier = Modifier,
    size: Dp = 12.dp,
    thickness: Dp = 1.5.dp,
) {
    Canvas(modifier = modifier.size(size)) {
        val s = size.toPx()
        val t = thickness.toPx()
        drawLine(
            color = color,
            start = Offset(s * 0.18f, s * 0.18f),
            end = Offset(s * 0.82f, s * 0.82f),
            strokeWidth = t,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = color,
            start = Offset(s * 0.82f, s * 0.18f),
            end = Offset(s * 0.18f, s * 0.82f),
            strokeWidth = t,
            cap = StrokeCap.Round,
        )
    }
}

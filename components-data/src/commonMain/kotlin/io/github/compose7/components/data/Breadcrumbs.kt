package io.github.compose7.components.data

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import io.github.compose7.core.haptics.Compose7Haptic
import io.github.compose7.core.theme.Compose7StyleInfo
import io.github.compose7.core.theme.Compose7ThemeColors
import io.github.compose7.core.theme.Compose7ThemeTypography
import io.github.compose7.foundation.compose7Clickable
import io.github.compose7.foundation.currentMinTouchTarget

/** One breadcrumb: a plain label, rendered as text (no icons — PRD §4). */
data class Crumb(val label: String)

/**
 * Breadcrumbs (PRD §7 "Data display" P1 / Appendix A "Breadcrumbs").
 *
 * A horizontal trail of labels separated by a drawn chevron glyph — never an
 * icon asset (PRD §4). The last [Crumb] is the current page: it renders in
 * `onSurface` with the heavier `labelLarge` role; earlier crumbs render in
 * `onSurfaceVariant`/`labelMedium` and are tappable through
 * [io.github.compose7.foundation.compose7Clickable] when [onCrumbClick] is set.
 * The row scrolls horizontally when it overflows and mirrors start/end under
 * RTL (PRD §12).
 *
 * ```kotlin
 * Breadcrumbs(
 *     items = listOf(Crumb("Home"), Crumb("Docs"), Crumb("Accordion")),
 *     onCrumbClick = { index -> navigator.popTo(index) },
 * )
 * ```
 *
 * @param items Trail to render; the last entry is the current location and is
 *   never clickable.
 * @param onCrumbClick Invoked with the clicked crumb index (non-current crumbs
 *   only); null renders a non-interactive trail.
 */
@Composable
fun Breadcrumbs(
    items: List<Crumb>,
    modifier: Modifier = Modifier,
    onCrumbClick: ((Int) -> Unit)? = null,
) {
    val colors = Compose7ThemeColors
    val type = Compose7ThemeTypography
    val minTouch = currentMinTouchTarget()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = minTouch)
            .padding(horizontal = Compose7StyleInfo.listContentInset)
            .horizontalScroll(rememberScrollState()),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        items.forEachIndexed { index, crumb ->
            val isCurrent = index == items.lastIndex
            key(index) {
                Box(
                    modifier = Modifier
                        .then(
                            if (isCurrent || onCrumbClick == null) {
                                Modifier
                            } else {
                                Modifier.compose7Clickable(
                                    role = Role.Button,
                                    haptic = Compose7Haptic.Light,
                                    onClick = { onCrumbClick(index) },
                                )
                            }
                        )
                        .sizeIn(minWidth = minTouch, minHeight = minTouch)
                        .padding(horizontal = 6.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText(
                        text = crumb.label,
                        style = if (isCurrent) {
                            type.labelLarge.copy(color = colors.onSurface)
                        } else {
                            type.labelMedium.copy(color = colors.onSurfaceVariant)
                        },
                    )
                }
                if (!isCurrent) {
                    CrumbSeparator(color = colors.onSurfaceVariant)
                }
            }
        }
    }
}

/**
 * Drawn "›" separator between crumbs — a canvas primitive that mirrors under
 * RTL, never an icon asset (PRD §4).
 */
@Composable
private fun CrumbSeparator(color: Color, modifier: Modifier = Modifier) {
    val thickness = 1.6.dp
    Canvas(
        modifier = modifier
            .padding(horizontal = 2.dp)
            .size(width = 7.dp, height = 14.dp),
    ) {
        val w = size.width
        val h = size.height
        val mirror = layoutDirection == LayoutDirection.Rtl
        fun x(value: Float): Float = if (mirror) w - value else value
        val path = Path().apply {
            moveTo(x(w * 0.24f), h * 0.16f)
            lineTo(x(w * 0.76f), h * 0.5f)
            lineTo(x(w * 0.24f), h * 0.84f)
        }
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = thickness.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
        )
    }
}

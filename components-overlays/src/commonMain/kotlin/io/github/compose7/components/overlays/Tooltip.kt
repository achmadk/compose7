package io.github.compose7.components.overlays

import androidx.compose.foundation.BasicTooltipBox
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberBasicTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupPositionProvider
import io.github.compose7.core.theme.Compose7ThemeColors
import io.github.compose7.core.theme.Compose7ThemeShapes
import io.github.compose7.foundation.Compose7Surface

/**
 * Long-press/hover hint bubble (PRD §7, P2 / Appendix A "Tooltip").
 *
 * Wraps [BasicTooltipBox] (foundation): shows [content] in a small raised
 * bubble while the anchor is long-pressed (touch/stylus) or hovered (mouse).
 * Purely presentational - a tooltip never carries actions and is never the only
 * affordance for an action (PRD §12).
 *
 * ```kotlin
 * Tooltip(positionProvider = myProvider, content = { BasicText("Delete") }) {
 *     Button(onClick = ::delete, text = "X")
 * }
 * ```
 *
 * The underlying `BasicTooltipState` is deliberately not exposed: it is an
 * experimental foundation type, and publishing it would make every call site
 * opt into an unstable API forever.
 *
 * @param isPersistent `true` keeps the bubble up until the pointer leaves or
 *   the long press ends; `false` auto-hides after a short dwell.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Tooltip(
    positionProvider: PopupPositionProvider,
    modifier: Modifier = Modifier,
    isPersistent: Boolean = true,
    content: @Composable () -> Unit,
    anchor: @Composable () -> Unit,
) {
    BasicTooltipBox(
        positionProvider = positionProvider,
        state = rememberBasicTooltipState(isPersistent = isPersistent),
        modifier = modifier,
        tooltip = { TooltipBubble(content = content) },
        content = anchor,
    )
}

/**
 * The default bubble chrome for [Tooltip]: a compact surface using the active
 * style's popover shape and the `surfaceVariant` pair (PRD §4, tokens only).
 */
@Composable
fun TooltipBubble(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val colors = Compose7ThemeColors
    val shapes = Compose7ThemeShapes

    Compose7Surface(
        modifier = modifier.padding(4.dp).widthIn(max = 280.dp),
        shape = shapes.popover,
        color = colors.surfaceVariant,
        contentColor = colors.onSurfaceVariant,
        elevation = 4.dp,
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
            content()
        }
    }
}

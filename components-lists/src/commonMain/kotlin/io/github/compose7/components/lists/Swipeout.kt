package io.github.compose7.components.lists

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import io.github.compose7.core.haptics.Compose7Haptic
import io.github.compose7.core.theme.Compose7MotionTokens
import io.github.compose7.core.theme.Compose7ThemeColors
import io.github.compose7.core.theme.Compose7ThemeTypography
import io.github.compose7.foundation.compose7Clickable
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

/** One revealed swipe action (PRD §7, P1 / §8.3). */
data class SwipeAction(
    /** Short label rendered under the optional [icon]. */
    val title: String,
    /** Invoked on tap; the swipeout closes itself afterwards. */
    val onClick: () -> Unit,
    /** Block background; [Color.Unspecified] (default) resolves to the style's `error` token. */
    val color: Color = Color.Unspecified,
    /** Label color; [Color.Unspecified] (default) resolves to `onError`. */
    val contentColor: Color = Color.Unspecified,
    /** Optional drawn/caller-supplied glyph above the title (no bundled icons, PRD §4). */
    val icon: (@Composable RowScope.() -> Unit)? = null,
)

/**
 * Swipe-to-reveal actions container (PRD §7, P1 / §8.3: "Swipeout exposes
 * leading/trailing swipe actions with configurable thresholds").
 *
 * Drag the [content] horizontally to reveal [startActions] on the start side
 * and [endActions] on the end side (RTL-safe). The drag settles open past
 * [threshold] and closed below it, animated with motion tokens; tapping the
 * revealed content closes it, and tapping an action fires a Medium haptic
 * (PRD §6.5) plus its callback. Actions render as full-height color blocks on
 * every style — only radius/threshold feel differs via tokens and parameters.
 *
 * ```kotlin
 * Swipeout(
 *     startActions = listOf(SwipeAction("Read", color = colors.primary, contentColor = colors.onPrimary, onClick = { … })),
 *     endActions = listOf(SwipeAction("Delete", onClick = { delete() })),
 * ) {
 *     ListItem("Inbox", subtitle = "1 new message", showDivider = false)
 * }
 * ```
 *
 * @param threshold Distance the content must travel before the drag settles
 *   open rather than closed.
 * @param actionWidth Fixed width of each action block (actions are laid out
 *   without measurement to keep the drag math simple and stable).
 */
@Composable
fun Swipeout(
    modifier: Modifier = Modifier,
    startActions: List<SwipeAction> = emptyList(),
    endActions: List<SwipeAction> = emptyList(),
    threshold: Dp = 64.dp,
    actionWidth: Dp = 88.dp,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    val motion = Compose7MotionTokens
    val scope = rememberCoroutineScope()
    val offset = remember { Animatable(0f) }
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl

    val density = LocalDensity.current
    val startWidthPx = with(density) { (actionWidth * startActions.size).toPx() }
    val endWidthPx = with(density) { (actionWidth * endActions.size).toPx() }
    val thresholdPx = with(density) { threshold.toPx() }
    // Physical content-translation bounds: start actions live on the layout start
    // edge, so the sign that reveals them flips with layout direction.
    val minX = if (isRtl) -startWidthPx else -endWidthPx
    val maxX = if (isRtl) endWidthPx else startWidthPx

    val settle: suspend () -> Unit = {
        val startOpen = if (isRtl) -offset.value else offset.value
        val target = when {
            startOpen >= thresholdPx -> if (isRtl) -startWidthPx else startWidthPx
            -startOpen >= thresholdPx -> if (isRtl) endWidthPx else -endWidthPx
            else -> 0f
        }
        offset.animateTo(target, tween(motion.mediumDurationMs, easing = motion.standardEasing))
    }
    val close: () -> Unit = {
        scope.launch {
            offset.animateTo(0f, tween(motion.mediumDurationMs, easing = motion.standardEasing))
        }
    }

    val dragState = rememberDraggableState { delta ->
        scope.launch {
            offset.snapTo((offset.value + delta).coerceIn(minX, maxX))
        }
    }
    val isOpen by remember { derivedStateOf { offset.value != 0f } }

    Box(
        modifier = modifier
            .clipToBounds()
            .draggable(
                orientation = Orientation.Horizontal,
                state = dragState,
                enabled = enabled && (startActions.isNotEmpty() || endActions.isNotEmpty()),
                onDragStopped = { settle() },
            ),
    ) {
        if (startActions.isNotEmpty()) {
            Row(modifier = Modifier.align(Alignment.CenterStart).fillMaxHeight()) {
                startActions.forEach { action ->
                    SwipeActionCell(action = action, width = actionWidth, onClose = close)
                }
            }
        }
        if (endActions.isNotEmpty()) {
            Row(modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight()) {
                endActions.forEach { action ->
                    SwipeActionCell(action = action, width = actionWidth, onClose = close)
                }
            }
        }
        Box(
            modifier = Modifier
                .offset { IntOffset(offset.value.roundToInt(), 0) }
                .let {
                    if (isOpen) {
                        it.compose7Clickable(haptic = null, onClick = close)
                    } else it
                },
        ) {
            content()
        }
    }
}

/** Full-height action block revealed behind swipeout content. */
@Composable
private fun RowScope.SwipeActionCell(
    action: SwipeAction,
    width: Dp,
    onClose: () -> Unit,
) {
    val colors = Compose7ThemeColors
    val background = if (action.color == Color.Unspecified) colors.error else action.color
    val foreground = if (action.contentColor == Color.Unspecified) colors.onError else action.contentColor
    Box(
        modifier = Modifier
            .width(width)
            .fillMaxHeight()
            .background(background)
            .compose7Clickable(
                role = Role.Button,
                haptic = Compose7Haptic.Medium,
                onClick = {
                    action.onClick()
                    onClose()
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            action.icon?.let { icon -> Row(content = icon) }
            BasicText(
                text = action.title,
                style = Compose7ThemeTypography.labelMedium.copy(color = foreground),
                maxLines = 1,
            )
        }
    }
}

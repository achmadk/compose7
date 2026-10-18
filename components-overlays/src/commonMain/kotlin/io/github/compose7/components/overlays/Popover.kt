package io.github.compose7.components.overlays

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup as ComposePopup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import io.github.compose7.core.theme.Compose7MotionTokens
import io.github.compose7.core.theme.Compose7Style
import io.github.compose7.core.theme.Compose7ThemeColors
import io.github.compose7.core.theme.Compose7ThemeShapes
import io.github.compose7.core.theme.currentCompose7Style
import io.github.compose7.core.theme.Compose7Motion
import io.github.compose7.foundation.Compose7Surface

/**
 * Light-weight anchored bubble overlay (PRD §7, P0 / Appendix A "Popover").
 *
 * Positioned by a caller-supplied [PopupPositionProvider] (typically computed
 * from the anchor's bounds), dismissed by tapping outside or via
 * [onDismissRequest]. Content is a plain column slot styled as a raised
 * surface per the active style's [io.github.compose7.core.theme.Compose7Shapes.popover]
 * token.
 *
 * ```kotlin
 * Popup(onDismissRequest = { expanded = false }) { … } // generic popup
 * // Popover, anchored under a measured anchor:
 * Popover(
 *     visible = expanded,
 *     onDismissRequest = { expanded = false },
 *     positionProvider = myProvider,
 * ) { BasicText("Details") }
 * ```
 *
 * @param positionProvider Where the bubble lands relative to its anchor;
 *   write one with `remember { … }` off the anchor's `onGloballyPositioned`.
 */
@Composable
fun Popover(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    positionProvider: PopupPositionProvider,
    properties: PopupProperties = PopupProperties(focusable = true),
    content: @Composable ColumnScope.() -> Unit,
) {
    if (!visible) return

    val colors = Compose7ThemeColors
    val shapes = Compose7ThemeShapes
    val motion = Compose7MotionTokens
    val style = currentCompose7Style

    ComposePopup(
        onDismissRequest = onDismissRequest,
        popupPositionProvider = positionProvider,
        properties = properties,
    ) {
        AnimatedVisibility(
            visible = true,
            enter = popupEnter(style, motion),
            exit = popupExit(style, motion),
        ) {
            Compose7Surface(
                modifier = modifier.padding(8.dp).widthIn(max = 320.dp),
                shape = shapes.popover,
                color = colors.surface,
                contentColor = colors.onSurface,
                elevation = 6.dp,
                border = if (currentCompose7Style == Compose7Style.DesktopUI7) {
                    BorderStroke(1.dp, colors.outlineVariant)
                } else {
                    null
                },
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    content = content,
                )
            }
        }
    }
}

/**
 * Generic styled popup (PRD §7, P0 / Appendix A "Popup") — a window-level
 * surface anchored by [alignment], dismissable by tapping outside. Use
 * [Popover] when you need explicit anchor-relative placement.
 */
@Composable
fun Popup(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.Center,
    properties: PopupProperties = PopupProperties(focusable = true),
    content: @Composable ColumnScope.() -> Unit,
) {
    if (!visible) return

    val colors = Compose7ThemeColors
    val shapes = Compose7ThemeShapes
    val motion = Compose7MotionTokens
    val style = currentCompose7Style

    ComposePopup(
        onDismissRequest = onDismissRequest,
        alignment = alignment,
        properties = properties,
    ) {
        AnimatedVisibility(
            visible = true,
            enter = popupEnter(style, motion),
            exit = popupExit(style, motion),
        ) {
            Compose7Surface(
                modifier = modifier.padding(8.dp).widthIn(max = 360.dp),
                shape = shapes.popover,
                color = colors.surface,
                contentColor = colors.onSurface,
                elevation = 6.dp,
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    content = content,
                )
            }
        }
    }
}

/**
 * Style-specific popup motion (PRD §8.4: platform-idiomatic presentation).
 * Cupertino grows from slightly larger (the native popover zoom), Material
 * scales up from below the resting size, DesktopUI7 is a quiet quick scale.
 */
private fun popupEnter(style: Compose7Style, motion: Compose7Motion): EnterTransition {
    val spec = tween<Float>(motion.fastDurationMs, easing = motion.emphasizedEasing)
    return fadeIn(tween(motion.fastDurationMs, easing = motion.emphasizedEasing)) +
        when (style) {
            Compose7Style.Cupertino -> scaleIn(initialScale = 1.04f, animationSpec = spec)
            Compose7Style.DesktopUI7 -> scaleIn(initialScale = 0.98f, animationSpec = spec)
            else -> scaleIn(initialScale = 0.92f, animationSpec = spec)
        }
}

private fun popupExit(style: Compose7Style, motion: Compose7Motion): ExitTransition {
    val spec = tween<Float>(motion.fastDurationMs, easing = motion.standardEasing)
    val target = if (style == Compose7Style.Cupertino) 1.02f else 0.96f
    return fadeOut(tween(motion.fastDurationMs, easing = motion.standardEasing)) +
        scaleOut(targetScale = target, animationSpec = spec)
}

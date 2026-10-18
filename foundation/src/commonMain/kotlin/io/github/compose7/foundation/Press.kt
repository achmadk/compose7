package io.github.compose7.foundation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.selection.toggleable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.semantics.Role
import io.github.compose7.core.haptics.Compose7Haptic
import io.github.compose7.core.haptics.rememberHapticFeedback
import io.github.compose7.core.theme.LocalCompose7ColorScheme
import io.github.compose7.core.theme.LocalCompose7Motion
import io.github.compose7.core.theme.LocalCompose7Style
import io.github.compose7.core.theme.Compose7Style
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Shared press feedback for every interactive Compose7 component: platform-
 * correct haptics on activation, a token-driven highlight overlay while
 * pressed, and correct semantics/disabled handling (PRD §6, §12).
 *
 * Unlike Material's ripple this is deliberately style-neutral — Cupertino
 * presses are a flat dim, Material a subtle tint, DesktopUI7 a quiet wash —
 * all driven by [io.github.compose7.core.theme.Compose7ColorScheme.highlight].
 *
 * ```kotlin
 * val interaction = remember { MutableInteractionSource() }
 * Box(
 *     Modifier.compose7Clickable(interaction, onClick = { … }, shape = shapes.card)
 *         .background(colors.primary, shapes.card)
 * )
 * ```
 */
@Composable
fun Modifier.compose7Clickable(
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    enabled: Boolean = true,
    role: Role? = null,
    haptic: Compose7Haptic? = Compose7Haptic.Light,
    overlayColor: Color = LocalCompose7ColorScheme.current.highlight,
    shape: Shape? = null,
    onClick: () -> Unit,
): Modifier {
    val pressed by interactionSource.collectIsPressedAsState()
    val emitHaptic = rememberHapticFeedback()
    val motion = LocalCompose7Motion.current
    val progress by animateFloatAsState(
        targetValue = if (pressed && enabled) 1f else 0f,
        animationSpec = tween(motion.fastDurationMs, easing = motion.standardEasing),
        label = "press",
    )
    return this
        .clickable(
            interactionSource = interactionSource,
            indication = null,
            enabled = enabled,
            role = role,
            onClick = {
                if (haptic != null) emitHaptic(haptic)
                onClick()
            },
        )
        .drawWithContent {
            drawContent()
            if (progress > 0.01f && overlayColor.alpha > 0f) {
                if (shape != null) {
                    clipPath(shapeOutlinePath(shape)) { drawRect(overlayColor, alpha = progress) }
                } else {
                    drawRect(overlayColor, alpha = progress)
                }
            }
        }
}

/**
 * Minimum interactive target size for the active style (PRD §12: 48dp on
 * Material-style platforms, 44pt on Cupertino-style).
 */
@Composable
fun currentMinTouchTarget(): Dp =
    if (LocalCompose7Style.current == Compose7Style.Cupertino) 44.dp else 48.dp

/** Applies [currentMinTouchTarget] as a minimum size. */
@Composable
fun Modifier.compose7MinimumTouchTarget(): Modifier =
    sizeIn(minWidth = currentMinTouchTarget(), minHeight = currentMinTouchTarget())

/**
 * Toggle semantics for on/off controls (Checkbox, Switch, Radio): correct
 * `Role`, `selected`/`checked` state, press overlay, and haptics — the toggle
 * equivalent of [compose7Clickable] (PRD §12).
 */
@Composable
fun Modifier.compose7Toggleable(
    value: Boolean,
    onValueChange: (Boolean) -> Unit,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    enabled: Boolean = true,
    role: Role = Role.Switch,
    haptic: Compose7Haptic = Compose7Haptic.Selection,
    overlayColor: Color = LocalCompose7ColorScheme.current.highlight,
    shape: Shape? = null,
): Modifier {
    val pressed by interactionSource.collectIsPressedAsState()
    val emitHaptic = rememberHapticFeedback()
    val motion = LocalCompose7Motion.current
    val progress by animateFloatAsState(
        targetValue = if (pressed && enabled) 1f else 0f,
        animationSpec = tween(motion.fastDurationMs, easing = motion.standardEasing),
        label = "press",
    )
    return this
        .toggleable(
            value = value,
            onValueChange = {
                emitHaptic(haptic)
                onValueChange(it)
            },
            interactionSource = interactionSource,
            indication = null,
            enabled = enabled,
            role = role,
        )
        .drawWithContent {
            drawContent()
            if (progress > 0.01f && overlayColor.alpha > 0f) {
                if (shape != null) {
                    clipPath(shapeOutlinePath(shape)) { drawRect(overlayColor, alpha = progress) }
                } else {
                    drawRect(overlayColor, alpha = progress)
                }
            }
        }
}

/** Builds a [Path] for [shape] at the current draw size — CMP 1.12 dropped `DrawScope.clip(Shape)`. */
private fun DrawScope.shapeOutlinePath(shape: Shape): Path {
    val outline = shape.createOutline(size, layoutDirection, this)
    return Path().apply {
        when (outline) {
            is Outline.Rectangle -> addRect(outline.rect)
            is Outline.Rounded -> addRoundRect(outline.roundRect)
            is Outline.Generic -> addPath(outline.path)
        }
    }
}

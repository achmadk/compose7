package io.github.compose7.components.forms

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import io.github.compose7.core.theme.Compose7MotionTokens
import io.github.compose7.core.theme.Compose7Style
import io.github.compose7.core.theme.Compose7ThemeColors
import io.github.compose7.core.theme.currentCompose7Style
import io.github.compose7.foundation.compose7MinimumTouchTarget
import io.github.compose7.foundation.compose7Toggleable

/**
 * On/off switch (PRD §7 P0 / Appendix A "Toggle" → Switch, PRD §8.5).
 *
 * iOS-flavored when the active style is Cupertino (light track + sliding knob,
 * token `primary` when on); Material-3-flavored otherwise (expanding track,
 * growing knob); DesktopUI7 follows the Material geometry with its own
 * tokens. All motion is token-driven (PRD §9: single API, style-specific
 * rendering). Controlled by default; a null [onCheckedChange] keeps state
 * internally via `rememberSaveable`.
 *
 * ```kotlin
 * Switch(checked = on, onCheckedChange = { on = it })
 * ```
 */
@Composable
fun Switch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)? = null,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val style = currentCompose7Style
    val colors = Compose7ThemeColors
    val motion = Compose7MotionTokens

    val state = rememberSaveable { mutableStateOf(false) }
    val current = if (onCheckedChange != null) checked else state.value

    val cupertino = style == Compose7Style.Cupertino
    val trackWidth = if (cupertino) 51.dp else 52.dp
    val trackHeight = if (cupertino) 31.dp else 32.dp
    val restingInset = if (cupertino) 2.dp else 4.dp
    val knobSizeTarget = if (cupertino) 27.dp else if (current) 24.dp else 16.dp

    val knobSize by animateDpAsState(
        targetValue = knobSizeTarget,
        animationSpec = tween(motion.mediumDurationMs, easing = motion.standardEasing),
        label = "switchKnobSize",
    )
    val knobOffset by animateDpAsState(
        targetValue = if (current) trackWidth - restingInset - knobSizeTarget else restingInset,
        animationSpec = tween(motion.mediumDurationMs, easing = motion.standardEasing),
        label = "switchKnobOffset",
    )
    val trackColor by animateColorAsState(
        targetValue = when {
            !enabled -> colors.surfaceVariant
            cupertino -> if (current) colors.primary else colors.outline
            current -> colors.primary
            else -> colors.surfaceVariant
        },
        animationSpec = tween(motion.mediumDurationMs, easing = motion.standardEasing),
        label = "switchTrack",
    )

    val knobColor = when {
        cupertino -> colors.surface
        current -> colors.onPrimary
        else -> colors.outline
    }

    Box(
        modifier = modifier
            .compose7Toggleable(
                value = current,
                onValueChange = { if (onCheckedChange == null) state.value = it else onCheckedChange(it) },
                enabled = enabled,
                role = Role.Switch,
            )
            .compose7MinimumTouchTarget(),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.size(trackWidth, trackHeight)) {
            drawRoundRect(
                color = trackColor,
                cornerRadius = CornerRadius(size.height / 2f),
            )
            if (!cupertino && !current && enabled) {
                drawRoundRect(
                    color = colors.outline,
                    style = Stroke(width = 2.dp.toPx()),
                    cornerRadius = CornerRadius(size.height / 2f),
                )
            }
            drawCircle(
                color = knobColor,
                radius = knobSize.toPx() / 2f,
                center = Offset(knobOffset.toPx() + knobSize.toPx() / 2f, size.height / 2f),
            )
        }
    }
}

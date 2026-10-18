package io.github.compose7.components.forms

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.compose7.core.theme.Compose7Style
import io.github.compose7.core.theme.Compose7ThemeColors
import io.github.compose7.core.theme.Compose7ThemeShapes
import io.github.compose7.core.theme.Compose7ThemeTypography
import io.github.compose7.core.theme.currentCompose7Style
import io.github.compose7.foundation.Minus
import io.github.compose7.foundation.Plus
import io.github.compose7.foundation.compose7Clickable
import io.github.compose7.foundation.currentMinTouchTarget

/**
 * Numeric stepper (PRD §7 P0 / Appendix A "Stepper", PRD §8.5) — drawn
 * minus/plus glyphs (PRD §4: no icon assets) around a centered value, clamped
 * to [min]/[max] in [step] increments. Controlled by default; a null
 * [onValueChange] keeps the value internally via `rememberSaveable`.
 *
 * ```kotlin
 * Stepper(value = count, onValueChange = { count = it }, min = 1, max = 10)
 * ```
 */
@Composable
fun Stepper(
    value: Int,
    onValueChange: ((Int) -> Unit)? = null,
    modifier: Modifier = Modifier,
    min: Int = 0,
    max: Int = Int.MAX_VALUE,
    step: Int = 1,
    enabled: Boolean = true,
    decreaseContentDescription: String = "Decrease",
    increaseContentDescription: String = "Increase",
    valueFormat: (Int) -> String = { it.toString() },
) {
    val style = currentCompose7Style
    val colors = Compose7ThemeColors
    val type = Compose7ThemeTypography
    val shapes = Compose7ThemeShapes

    val state = rememberSaveable { mutableStateOf(value) }
    val current = if (onValueChange != null) value else state.value
    val commit: (Int) -> Unit = { if (onValueChange == null) state.value = it else onValueChange(it) }

    val stepValue = step.coerceAtLeast(1)
    val canDecrease = enabled && current > min
    val canIncrease = enabled && current < max
    val glyphColor = if (enabled) colors.onSurface else colors.onSurface.copy(alpha = 0.38f)

    val controlHeight = currentMinTouchTarget()
    val containerShape = when (style) {
        Compose7Style.Cupertino -> shapes.chip
        else -> shapes.input
    }

    Row(
        modifier = modifier
            .clip(containerShape)
            .background(colors.surfaceVariant)
            .let {
                if (style != Compose7Style.Cupertino) {
                    it.border(BorderStroke(1.dp, colors.outlineVariant), containerShape)
                } else {
                    it
                }
            }
            .height(controlHeight),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StepperButton(
            enabled = canDecrease,
            contentDescription = decreaseContentDescription,
            size = controlHeight,
            glyphColor = glyphColor,
            onClick = { commit((current - stepValue).coerceAtLeast(min)) },
            glyph = { Minus(color = it) },
        )
        StepperDivider(style, controlHeight)
        Box(
            modifier = Modifier.widthIn(min = 48.dp).padding(horizontal = 4.dp),
            contentAlignment = Alignment.Center,
        ) {
            BasicText(
                text = valueFormat(current),
                style = type.titleMedium.copy(
                    color = glyphColor,
                    textAlign = TextAlign.Center,
                ),
            )
        }
        StepperDivider(style, controlHeight)
        StepperButton(
            enabled = canIncrease,
            contentDescription = increaseContentDescription,
            size = controlHeight,
            glyphColor = glyphColor,
            onClick = { commit((current + stepValue).coerceAtMost(max)) },
            glyph = { Plus(color = it) },
        )
    }
}

@Composable
private fun RowScope.StepperButton(
    enabled: Boolean,
    contentDescription: String,
    size: Dp,
    glyphColor: Color,
    onClick: () -> Unit,
    glyph: @Composable (Color) -> Unit,
) {
    Box(
        modifier = Modifier
            .size(size)
            .semantics { this.contentDescription = contentDescription }
            .compose7Clickable(enabled = enabled, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        glyph(glyphColor)
    }
}

@Composable
private fun RowScope.StepperDivider(style: Compose7Style, height: Dp) {
    if (style == Compose7Style.Cupertino) return
    Box(
        Modifier
            .width(1.dp)
            .height(height)
            .background(Compose7ThemeColors.outlineVariant),
    )
}

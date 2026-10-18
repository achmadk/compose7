package io.github.compose7.components.forms

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import io.github.compose7.core.theme.Compose7Style
import io.github.compose7.core.theme.Compose7ThemeColors
import io.github.compose7.core.theme.Compose7ThemeShapes
import io.github.compose7.core.theme.Compose7ThemeTypography
import io.github.compose7.core.theme.currentCompose7Style
import io.github.compose7.foundation.Check
import io.github.compose7.foundation.Minus
import io.github.compose7.foundation.compose7MinimumTouchTarget
import io.github.compose7.foundation.compose7Toggleable

/**
 * Checkbox row with a drawn check glyph (PRD §7 P0 / Appendix A "Checkbox",
 * PRD §8.5, §4: no icon assets — the mark is a canvas primitive).
 *
 * Supports an indeterminate state (drawn minus); toggling from indeterminate
 * goes to checked. Controlled by default; a null [onCheckedChange] keeps the
 * state internally via `rememberSaveable`.
 *
 * ```kotlin
 * Checkbox(checked = agreed, onCheckedChange = { agreed = it }, label = "I agree")
 * ```
 *
 * @param isError Tints the box and validation slot with the error color.
 * @param supportingText Validation message slot (PRD §8.5).
 */
@Composable
fun Checkbox(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)? = null,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    indeterminate: Boolean = false,
    label: String? = null,
    isError: Boolean = false,
    supportingText: (@Composable () -> Unit)? = null,
) {
    val colors = Compose7ThemeColors
    val type = Compose7ThemeTypography
    val shapes = Compose7ThemeShapes
    val style = currentCompose7Style

    val state = rememberSaveable { mutableStateOf(false) }
    val current = if (onCheckedChange != null) checked else state.value
    val toggle: (Boolean) -> Unit = {
        if (onCheckedChange == null) state.value = it else onCheckedChange(it)
    }

    val boxSize = 20.dp
    val markSize = 14.dp
    val boxShape = if (style == Compose7Style.Cupertino) shapes.medium else shapes.extraSmall
    val markColor: Color = colors.onPrimary

    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .compose7Toggleable(
                    value = current || indeterminate,
                    onValueChange = { toggle(if (indeterminate) true else it) },
                    enabled = enabled,
                    role = Role.Checkbox,
                )
                .compose7MinimumTouchTarget()
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(boxSize)
                    .clip(boxShape)
                    .let {
                        if (current || indeterminate) {
                            it.background(colors.primary)
                        } else {
                            it.border(
                                BorderStroke(
                                    2.dp,
                                    when {
                                        !enabled -> colors.outline.copy(alpha = 0.38f)
                                        isError -> colors.error
                                        else -> colors.outline
                                    },
                                ),
                                boxShape,
                            )
                        }
                    },
                contentAlignment = Alignment.Center,
            ) {
                when {
                    indeterminate -> Minus(color = markColor, size = markSize)
                    current -> Check(color = markColor, size = markSize)
                }
            }
            if (label != null) {
                Spacer(Modifier.width(12.dp))
                BasicText(
                    text = label,
                    style = type.bodyLarge.copy(
                        color = when {
                            !enabled -> colors.onSurface.copy(alpha = 0.38f)
                            isError -> colors.error
                            else -> colors.onSurface
                        },
                    ),
                )
            }
        }
        FormFieldSupportingText(isError = isError, supportingText = supportingText)
    }
}

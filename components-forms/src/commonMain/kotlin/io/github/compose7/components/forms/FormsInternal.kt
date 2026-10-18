package io.github.compose7.components.forms

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.compose7.core.theme.Compose7MotionTokens
import io.github.compose7.core.theme.Compose7Style
import io.github.compose7.core.theme.Compose7StyleInfo
import io.github.compose7.core.theme.Compose7ThemeColors
import io.github.compose7.core.theme.Compose7ThemeShapes
import io.github.compose7.core.theme.Compose7ThemeTypography
import io.github.compose7.core.theme.currentCompose7Style
import io.github.compose7.foundation.LocalContentColor
import kotlin.math.roundToInt

/**
 * Validation slot shared by every form control (PRD §8.5: all inputs expose
 * `isError` plus a supporting-text slot). Renders nothing when the caller
 * passes `null`.
 */
@Composable
internal fun FormFieldSupportingText(
    isError: Boolean,
    supportingText: (@Composable () -> Unit)?,
    modifier: Modifier = Modifier,
) {
    if (supportingText == null) return
    val colors = Compose7ThemeColors
    Column(modifier = modifier.padding(start = 4.dp, top = 4.dp)) {
        CompositionLocalProvider(
            LocalContentColor provides if (isError) colors.error else colors.onSurfaceVariant,
        ) {
            supportingText()
        }
    }
}

/**
 * Shared text-input core behind [TextField], [TextArea] (PRD §8.5: every input
 * gets `value`/`onValueChange`, an `isError` flag, and a supporting-text slot).
 *
 * Rendering branches on [currentCompose7Style] (PRD §9):
 * Material — outlined box, floating label above when focused/filled;
 * Cupertino — inset filled grouped-row style (filled [shapes.input], hairline
 * tint on error), static small label above;
 * DesktopUI7 — bordered box on the surface with a floating label.
 */
@Composable
internal fun FormsTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier,
    enabled: Boolean,
    readOnly: Boolean,
    isError: Boolean,
    label: String?,
    placeholder: String?,
    leading: (@Composable RowScope.() -> Unit)?,
    trailing: (@Composable RowScope.() -> Unit)?,
    supportingText: (@Composable () -> Unit)?,
    singleLine: Boolean,
    minLines: Int,
    maxLines: Int,
    contentMinHeight: Dp,
    keyboardOptions: KeyboardOptions,
    keyboardActions: KeyboardActions,
    visualTransformation: VisualTransformation,
    interactionSource: MutableInteractionSource,
) {
    val style = currentCompose7Style
    val colors = Compose7ThemeColors
    val type = Compose7ThemeTypography
    val shapes = Compose7ThemeShapes
    val motion = Compose7MotionTokens
    val insets = Compose7StyleInfo

    var focused by remember { mutableStateOf(false) }

    val labelAbove = label != null &&
        (style == Compose7Style.Cupertino || focused || value.isNotEmpty())
    // Non-null exactly when the label is rendered above the input.
    val floatingLabel = label.takeIf { labelAbove }

    val restingBorder = when (style) {
        Compose7Style.Material, Compose7Style.Auto -> colors.outline
        else -> colors.outlineVariant
    }
    val borderColor by animateColorAsState(
        targetValue = if (isError) colors.error else if (focused) colors.primary else restingBorder,
        animationSpec = tween(motion.fastDurationMs, easing = motion.standardEasing),
        label = "formsFieldBorder",
    )
    val borderWidth by animateDpAsState(
        targetValue = if (style == Compose7Style.Material && focused) 2.dp else 1.dp,
        animationSpec = tween(motion.fastDurationMs, easing = motion.standardEasing),
        label = "formsFieldBorderWidth",
    )
    val cupertinoFill by animateColorAsState(
        targetValue = if (isError) colors.errorContainer else colors.surfaceVariant,
        animationSpec = tween(motion.fastDurationMs, easing = motion.standardEasing),
        label = "formsFieldFill",
    )
    val labelColor by animateColorAsState(
        targetValue = when {
            isError -> colors.error
            focused -> colors.primary
            else -> colors.onSurfaceVariant
        },
        animationSpec = tween(motion.fastDurationMs, easing = motion.standardEasing),
        label = "formsFieldLabel",
    )

    val frameModifier = when (style) {
        Compose7Style.Cupertino -> Modifier.background(cupertinoFill, shapes.input)
        else -> Modifier.border(BorderStroke(borderWidth, borderColor), shapes.input)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .then(frameModifier)
            .onFocusChanged { focused = it.isFocused }
            .padding(
                horizontal = insets.listContentInset,
                vertical = if (singleLine) 10.dp else 12.dp,
            ),
    ) {
        if (floatingLabel != null) {
            BasicText(text = floatingLabel, style = type.labelMedium.copy(color = labelColor))
            Spacer(Modifier.height(4.dp))
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = if (singleLine) Alignment.CenterVertically else Alignment.Top,
        ) {
            leading?.invoke(this)
            Box(
                modifier = Modifier.weight(1f).heightIn(min = contentMinHeight),
                contentAlignment = if (singleLine) Alignment.CenterStart else Alignment.TopStart,
            ) {
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = enabled,
                    readOnly = readOnly,
                    textStyle = type.bodyLarge.copy(color = colors.onSurface),
                    keyboardOptions = keyboardOptions,
                    keyboardActions = keyboardActions,
                    singleLine = singleLine,
                    minLines = minLines,
                    maxLines = maxLines,
                    visualTransformation = visualTransformation,
                    interactionSource = interactionSource,
                    cursorBrush = SolidColor(colors.primary),
                    decorationBox = { innerTextField ->
                        Box(
                            Modifier.fillMaxWidth(),
                            contentAlignment = if (singleLine) Alignment.CenterStart else Alignment.TopStart,
                        ) {
                            if (value.isEmpty() && placeholder != null && (label == null || labelAbove)) {
                                BasicText(
                                    text = placeholder,
                                    style = type.bodyLarge.copy(color = colors.onSurfaceVariant),
                                )
                            }
                            innerTextField()
                            if (!labelAbove && label != null && value.isEmpty()) {
                                BasicText(
                                    text = label,
                                    style = type.bodyLarge.copy(color = colors.onSurfaceVariant),
                                )
                            }
                        }
                    },
                )
            }
            trailing?.invoke(this)
        }
        if (supportingText != null) {
            Spacer(Modifier.height(4.dp))
            CompositionLocalProvider(
                LocalContentColor provides if (isError) colors.error else colors.onSurfaceVariant,
            ) {
                supportingText()
            }
        }
    }
}

/**
 * Snaps [value] to one of the [steps] + 1 discrete points inside [range]
 * (endpoints inclusive), per Material's slider step convention.
 */
internal fun quantize(value: Float, range: ClosedFloatingPointRange<Float>, steps: Int): Float {
    val span = range.endInclusive - range.start
    if (steps <= 0 || span == 0f) return value.coerceIn(range.start, range.endInclusive)
    val divisions = steps + 1
    val fraction = ((value - range.start) / span).coerceIn(0f, 1f)
    return range.start + (fraction * divisions).roundToInt() * span / divisions
}

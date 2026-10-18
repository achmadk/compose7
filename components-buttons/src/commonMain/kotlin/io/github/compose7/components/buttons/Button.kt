package io.github.compose7.components.buttons

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.compose7.core.haptics.Compose7Haptic
import io.github.compose7.core.theme.Compose7ColorScheme
import io.github.compose7.core.theme.Compose7Style
import io.github.compose7.core.theme.Compose7ThemeColors
import io.github.compose7.core.theme.Compose7ThemeShapes
import io.github.compose7.core.theme.Compose7ThemeTypography
import io.github.compose7.core.theme.currentCompose7Style
import io.github.compose7.foundation.LocalContentColor
import io.github.compose7.foundation.compose7Clickable

/** Visual variants shared by every style, mapped to that style's language. */
enum class ButtonVariant { Filled, Tonal, Outline, Text }

/** Height classes, resolved per style (PRD §9: single API, style-specific rendering). */
enum class ButtonSize { Small, Medium, Large }

/**
 * Action button (PRD §7, P0 / Appendix A "Button").
 *
 * One API, three renderings from identical call-site code (PRD §9):
 * Material — filled/tonal/outline/text with Material type & motion;
 * Cupertino — tinted pill with press dim and SF-style type;
 * DesktopUI7 — squared-off geometry with hairline borders.
 *
 * ```kotlin
 * Button(onClick = { submit() }) { BasicText("Save") }
 * Button(onClick = { … }, variant = ButtonVariant.Outline) { BasicText("Cancel") }
 * ```
 */
@Composable
fun Button(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    variant: ButtonVariant = ButtonVariant.Filled,
    size: ButtonSize = ButtonSize.Medium,
    haptic: Compose7Haptic? = Compose7Haptic.Light,
    leadingIcon: (@Composable RowScope.() -> Unit)? = null,
    trailingIcon: (@Composable RowScope.() -> Unit)? = null,
    shape: Shape = Compose7ThemeShapes.button,
    content: @Composable RowScope.() -> Unit,
) {
    val style = currentCompose7Style
    val colors = Compose7ThemeColors
    val interaction = remember { MutableInteractionSource() }

    val palette = if (enabled) {
        buttonPalette(style, colors, variant)
    } else {
        ButtonPalette(
            container = colors.onSurface.copy(alpha = 0.12f),
            content = colors.onSurface.copy(alpha = 0.38f),
            border = if (variant == ButtonVariant.Outline) {
                BorderStroke(1.dp, colors.outline.copy(alpha = 0.38f))
            } else null,
        )
    }

    val horizontal = when (size) {
        ButtonSize.Small -> 12.dp
        ButtonSize.Medium -> 16.dp
        ButtonSize.Large -> 24.dp
    }

    androidx.compose.runtime.CompositionLocalProvider(LocalContentColor provides palette.content) {
        Row(
            modifier = modifier
                .height(buttonHeight(style, size))
                .background(palette.container, shape)
                .let { if (palette.border != null) it.border(palette.border, shape) else it }
                .compose7Clickable(
                    interactionSource = interaction,
                    enabled = enabled,
                    role = Role.Button,
                    haptic = haptic,
                    shape = shape,
                    onClick = onClick,
                )
                .padding(horizontal = horizontal),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            leadingIcon?.invoke(this)
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
                content = content,
            )
            trailingIcon?.invoke(this)
        }
    }
}

/** Convenience overload with a plain string label. */
@Composable
fun Button(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    variant: ButtonVariant = ButtonVariant.Filled,
    size: ButtonSize = ButtonSize.Medium,
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        variant = variant,
        size = size,
    ) {
        BasicText(
            text = text,
            style = Compose7ThemeTypography.labelLarge.copy(color = LocalContentColor.current),
        )
    }
}

internal data class ButtonPalette(val container: Color, val content: Color, val border: BorderStroke?)

internal fun buttonPalette(
    style: Compose7Style,
    colors: Compose7ColorScheme,
    variant: ButtonVariant,
): ButtonPalette = when (variant) {
    ButtonVariant.Filled -> when (style) {
        Compose7Style.Cupertino, Compose7Style.DesktopUI7 ->
            ButtonPalette(colors.primary, colors.onPrimary, null)
        else -> ButtonPalette(colors.primary, colors.onPrimary, null)
    }
    ButtonVariant.Tonal -> when (style) {
        Compose7Style.Material, Compose7Style.Auto ->
            ButtonPalette(colors.secondaryContainer, colors.onSecondaryContainer, null)
        else -> ButtonPalette(colors.primaryContainer, colors.onPrimaryContainer, null)
    }
    ButtonVariant.Outline -> ButtonPalette(
        container = Color.Transparent,
        content = colors.primary,
        border = BorderStroke(1.dp, colors.outline),
    )
    ButtonVariant.Text -> ButtonPalette(Color.Transparent, colors.primary, null)
}

internal fun buttonHeight(style: Compose7Style, size: ButtonSize): Dp = when (style) {
    Compose7Style.Cupertino -> when (size) {
        ButtonSize.Small -> 32.dp
        ButtonSize.Medium -> 44.dp
        ButtonSize.Large -> 50.dp
    }
    Compose7Style.DesktopUI7 -> when (size) {
        ButtonSize.Small -> 28.dp
        ButtonSize.Medium -> 36.dp
        ButtonSize.Large -> 44.dp
    }
    else -> when (size) {
        ButtonSize.Small -> 32.dp
        ButtonSize.Medium -> 40.dp
        ButtonSize.Large -> 48.dp
    }
}

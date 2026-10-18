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
import androidx.compose.ui.unit.dp
import io.github.compose7.core.haptics.Compose7Haptic
import io.github.compose7.core.theme.Compose7Style
import io.github.compose7.core.theme.Compose7ThemeColors
import io.github.compose7.core.theme.Compose7ThemeShapes
import io.github.compose7.core.theme.Compose7ThemeTypography
import io.github.compose7.core.theme.currentCompose7Style
import io.github.compose7.foundation.Check
import io.github.compose7.foundation.LocalContentColor
import io.github.compose7.foundation.compose7Clickable
import io.github.compose7.foundation.compose7Toggleable

/**
 * Chip / tag (PRD §7, P0 / Appendix A "Chips / Tags").
 *
 * Three modes from one API:
 *  - `onClick != null` → clickable chip (filter/action),
 *  - `onSelectedChange != null` → toggleable chip (controlled selection),
 *  - neither → static chip (display only).
 *
 * The selected state draws a **canvas check**, not an icon (PRD §4).
 */
@Composable
fun Chip(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    selected: Boolean = false,
    onSelectedChange: ((Boolean) -> Unit)? = null,
    enabled: Boolean = true,
    leadingIcon: (@Composable RowScope.() -> Unit)? = null,
    showSelectionCheck: Boolean = true,
    shape: Shape = Compose7ThemeShapes.chip,
    content: @Composable RowScope.() -> Unit,
) {
    val style = currentCompose7Style
    val colors = Compose7ThemeColors
    val interaction = remember { MutableInteractionSource() }

    val container: Color = when {
        !enabled -> colors.onSurface.copy(alpha = 0.12f)
        selected -> when (style) {
            Compose7Style.Material, Compose7Style.Auto -> colors.secondaryContainer
            Compose7Style.Cupertino -> colors.primary
            Compose7Style.DesktopUI7 -> colors.primaryContainer
        }
        else -> when (style) {
            Compose7Style.Material, Compose7Style.Auto -> Color.Transparent
            Compose7Style.Cupertino -> colors.surfaceVariant
            Compose7Style.DesktopUI7 -> colors.surface
        }
    }
    val contentColor: Color = when {
        !enabled -> colors.onSurface.copy(alpha = 0.38f)
        selected && style == Compose7Style.Cupertino -> colors.onPrimary
        selected -> when (style) {
            Compose7Style.Material, Compose7Style.Auto -> colors.onSecondaryContainer
            else -> colors.onPrimaryContainer
        }
        else -> colors.onSurfaceVariant
    }
    val border: BorderStroke? = when {
        !enabled -> BorderStroke(1.dp, colors.outline.copy(alpha = 0.38f))
        selected -> null
        style == Compose7Style.Material || style == Compose7Style.Auto ->
            BorderStroke(1.dp, colors.outline)
        style == Compose7Style.DesktopUI7 ->
            BorderStroke(1.dp, colors.outlineVariant)
        else -> null
    }

    val baseModifier = modifier
        .height(32.dp)
        .background(container, shape)
        .let { if (border != null) it.border(border, shape) else it }

    val interactiveModifier = when {
        onSelectedChange != null -> baseModifier.compose7Toggleable(
            value = selected,
            onValueChange = onSelectedChange,
            interactionSource = interaction,
            enabled = enabled,
            role = Role.Checkbox,
            haptic = Compose7Haptic.Selection,
            shape = shape,
        )
        onClick != null -> baseModifier.compose7Clickable(
            interactionSource = interaction,
            enabled = enabled,
            role = Role.Button,
            haptic = Compose7Haptic.Selection,
            shape = shape,
            onClick = onClick,
        )
        else -> baseModifier
    }

    androidx.compose.runtime.CompositionLocalProvider(LocalContentColor provides contentColor) {
        Row(
            modifier = interactiveModifier.padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (selected && showSelectionCheck && leadingIcon == null) {
                Check(color = contentColor, size = 14.dp, thickness = 1.8.dp)
            } else {
                leadingIcon?.invoke(this)
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
                content = content,
            )
        }
    }
}

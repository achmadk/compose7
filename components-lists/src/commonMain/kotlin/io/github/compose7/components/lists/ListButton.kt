package io.github.compose7.components.lists

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import io.github.compose7.core.haptics.Compose7Haptic
import io.github.compose7.core.theme.Compose7ThemeColors
import io.github.compose7.core.theme.Compose7ThemeShapes
import io.github.compose7.core.theme.Compose7ThemeTypography
import io.github.compose7.foundation.compose7Clickable
import io.github.compose7.foundation.currentMinTouchTarget

/**
 * Full-width action row for lists (PRD §7, P0 / Appendix A "List Button").
 *
 * A Framework7-style list button: a centered, primary-colored label on a
 * full-width block that meets the minimum touch target, with a rounded
 * background inside grouped lists and edge-to-edge treatment in plain ones.
 *
 * ```kotlin
 * List {
 *     ListSection("Actions") { ListItem("Delete account", onClick = { … }) }
 *     ListButton("Add Item", onClick = { … })
 * }
 * ```
 *
 * @param color Container color; defaults to the style's primary token.
 * @param contentColor Label color; defaults to the primary pair's on-color.
 * @param shape `null` resolves per list style: rounded ([Compose7ThemeShapes.medium])
 *   in grouped lists, square edge-to-edge in plain ones.
 */
@Composable
fun ListButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    color: Color = Compose7ThemeColors.primary,
    contentColor: Color = Compose7ThemeColors.onPrimary,
    shape: Shape? = null,
) {
    val colors = Compose7ThemeColors
    val shapes = Compose7ThemeShapes
    val interaction = remember { MutableInteractionSource() }

    val resolvedShape = shape ?: when (currentListStyle()) {
        ListStyle.Grouped -> shapes.medium
        ListStyle.Plain -> RectangleShape
    }
    val container = if (enabled) color else colors.onSurface.copy(alpha = 0.12f)
    val labelColor = if (enabled) contentColor else colors.onSurface.copy(alpha = 0.38f)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = currentMinTouchTarget())
            .background(container, resolvedShape)
            .compose7Clickable(
                interactionSource = interaction,
                enabled = enabled,
                role = Role.Button,
                haptic = Compose7Haptic.Light,
                shape = resolvedShape,
                onClick = onClick,
            )
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BasicText(
            text = text,
            style = Compose7ThemeTypography.labelLarge.copy(color = labelColor),
        )
    }
}

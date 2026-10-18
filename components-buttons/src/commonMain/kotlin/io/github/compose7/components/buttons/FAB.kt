package io.github.compose7.components.buttons

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.compose7.core.haptics.Compose7Haptic
import io.github.compose7.core.theme.Compose7Style
import io.github.compose7.core.theme.Compose7ThemeColors
import io.github.compose7.core.theme.Compose7ThemeShapes
import io.github.compose7.core.theme.Compose7ThemeTypography
import io.github.compose7.core.theme.currentCompose7Style
import io.github.compose7.foundation.LocalContentColor
import io.github.compose7.foundation.compose7Clickable

/** FAB size classes. */
enum class FabSize { Small, Regular, Large }

/**
 * Floating Action Button (PRD §7, P0 / Appendix A "Floating Action Button").
 *
 * Provide the icon via the [content] slot — Compose7 ships no icons
 * (PRD §4). Set [label] for an extended (labeled) FAB.
 */
@Composable
fun FAB(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: FabSize = FabSize.Regular,
    enabled: Boolean = true,
    label: String? = null,
    containerColor: Color? = null,
    contentColor: Color? = null,
    border: BorderStroke? = null,
    elevation: Dp? = null,
    shape: Shape = Compose7ThemeShapes.fab,
    content: @Composable () -> Unit,
) {
    val style = currentCompose7Style
    val colors = Compose7ThemeColors
    val interaction = remember { MutableInteractionSource() }

    val bg = containerColor ?: when (style) {
        Compose7Style.Material, Compose7Style.Auto -> colors.secondaryContainer
        else -> colors.primary
    }
    val fg = contentColor ?: when (style) {
        Compose7Style.Material, Compose7Style.Auto -> colors.onSecondaryContainer
        else -> colors.onPrimary
    }
    val effectiveElevation = elevation ?: when (style) {
        Compose7Style.Material, Compose7Style.Auto -> 6.dp
        else -> 0.dp
    }
    val defaultBorder = border ?: when (style) {
        Compose7Style.DesktopUI7 -> BorderStroke(1.dp, colors.outlineVariant)
        else -> null
    }
    val pressShape: Shape = shape

    Box(modifier = Modifier.size(fabSizeDp(size, label, style))) {
        androidx.compose.runtime.CompositionLocalProvider(LocalContentColor provides fg) {
            Row(
                modifier = modifier
                    .size(fabSizeDp(size, label, style))
                    .shadow(effectiveElevation, shape, clip = effectiveElevation > 0.dp)
                    .background(bg, shape)
                    .let { if (defaultBorder != null) it.border(defaultBorder, shape) else it }
                    .compose7Clickable(
                        interactionSource = interaction,
                        enabled = enabled,
                        role = Role.Button,
                        haptic = Compose7Haptic.Medium,
                        shape = pressShape,
                        onClick = onClick,
                    )
                    .padding(horizontal = if (label != null) 16.dp else 0.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(contentAlignment = Alignment.Center) { content() }
                if (label != null) {
                    BasicText(
                        text = label,
                        style = Compose7ThemeTypography.labelLarge.copy(color = fg),
                    )
                }
            }
        }
    }
}

private fun fabSizeDp(size: FabSize, label: String?, style: Compose7Style): Dp {
    if (label != null) {
        return when (style) {
            Compose7Style.Cupertino -> 52.dp
            Compose7Style.DesktopUI7 -> 44.dp
            else -> 48.dp
        }
    }
    return when (style) {
        Compose7Style.Cupertino -> when (size) {
            FabSize.Small -> 40.dp
            FabSize.Regular -> 52.dp
            FabSize.Large -> 72.dp
        }
        Compose7Style.DesktopUI7 -> when (size) {
            FabSize.Small -> 36.dp
            FabSize.Regular -> 48.dp
            FabSize.Large -> 64.dp
        }
        else -> when (size) {
            FabSize.Small -> 40.dp
            FabSize.Regular -> 56.dp
            FabSize.Large -> 96.dp
        }
    }
}

package io.github.compose7.components.lists

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.compose7.core.haptics.Compose7Haptic
import io.github.compose7.core.theme.Compose7Style
import io.github.compose7.core.theme.Compose7ThemeColors
import io.github.compose7.core.theme.Compose7ThemeTypography
import io.github.compose7.core.theme.currentCompose7Style
import io.github.compose7.foundation.Chevron
import io.github.compose7.foundation.LocalContentColor
import io.github.compose7.foundation.compose7Clickable
import io.github.compose7.foundation.currentMinTouchTarget

/**
 * List row (PRD §7, P0 / Appendix A "List Item").
 *
 * Leading-media slot, title/subtitle, trailing detail text + slot, optional
 * disclosure chevron, and platform-correct dividers (PRD §8.3): inset on
 * grouped/Cupertino rows, edge-to-edge on Material. Rows taller on iOS
 * (44pt minimum), title/subtitle type from the active style's tokens.
 *
 * When `onClick != null` the whole row is one press target with token-driven
 * press feedback, haptics, and semantics.
 *
 * ```kotlin
 * ListItem(
 *     title = "Wi-Fi",
 *     valueText = "Casa",
 *     leading = { CallerProvidedWifiIcon() },
 *     showChevron = true,
 *     leadingSize = 32.dp, // keeps the divider aligned with the title
 *     onClick = { … },
 * )
 * ```
 *
 * @param subtitle Optional second line, `bodySmall` in the secondary text color.
 * @param onClick Row action; `null` renders a static, non-interactive row.
 * @param enabled False dims the row and disables the click target (PRD §12).
 * @param leading Leading-media slot (icon/avatar); pair with [leadingSize] so
 *   dividers inset past it on grouped rows.
 * @param trailing Trailing-content slot, rendered after [valueText].
 * @param valueText Right-aligned detail text (iOS "value" text).
 * @param showChevron Draws the shared [Chevron] disclosure glyph (no icon assets, PRD §4).
 * @param showDivider Draws the row's bottom separator (usually hide on the last row).
 * @param leadingSize Total width reserved for the [leading] slot, including its
 *   trailing gap; also added to the divider inset. `0.dp` sizes the slot naturally.
 * @param shape Clip/press-overlay shape for the row; square by default — pass
 *   card corners to round the first/last row of a grouped block.
 */
@Composable
fun ListItem(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    leading: (@Composable RowScope.() -> Unit)? = null,
    trailing: (@Composable RowScope.() -> Unit)? = null,
    valueText: String? = null,
    showChevron: Boolean = false,
    showDivider: Boolean = true,
    leadingSize: Dp = 0.dp,
    shape: Shape = RectangleShape,
) {
    val style = currentListStyle()
    val platformStyle = currentCompose7Style
    val colors = Compose7ThemeColors
    val type = Compose7ThemeTypography
    val interaction = remember { MutableInteractionSource() }

    val contentColor = if (enabled) colors.onSurface else colors.onSurface.copy(alpha = 0.38f)
    val secondaryColor =
        if (enabled) colors.onSurfaceVariant else colors.onSurfaceVariant.copy(alpha = 0.38f)

    // Cupertino/grouped rows meet the 44pt iOS minimum (PRD §12); Material rows
    // follow the 56dp list-item spec; desktop stays compact but target-safe.
    val minHeight = when {
        style == ListStyle.Grouped -> currentMinTouchTarget()
        platformStyle == Compose7Style.DesktopUI7 -> currentMinTouchTarget()
        else -> 56.dp
    }
    val verticalPadding = if (style == ListStyle.Grouped) 8.dp else 12.dp
    val contentInset = currentListContentInset()
    val titleStyle =
        if (platformStyle == Compose7Style.DesktopUI7) type.bodyMedium else type.bodyLarge

    Column(modifier = modifier) {
        CompositionLocalProvider(LocalContentColor provides contentColor) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = minHeight)
                    .let {
                        if (onClick != null) {
                            it.compose7Clickable(
                                interactionSource = interaction,
                                enabled = enabled,
                                role = Role.Button,
                                haptic = Compose7Haptic.Light,
                                shape = shape,
                                onClick = onClick,
                            )
                        } else it
                    }
                    .padding(horizontal = contentInset)
                    .padding(vertical = verticalPadding),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (leading != null) {
                    Box(
                        modifier = if (leadingSize > 0.dp) Modifier.width(leadingSize) else Modifier,
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            content = leading,
                        )
                    }
                    if (leadingSize <= 0.dp) Spacer(Modifier.width(12.dp))
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = if (leading != null && leadingSize <= 0.dp) 12.dp else 0.dp),
                ) {
                    BasicText(text = title, style = titleStyle.copy(color = contentColor))
                    if (subtitle != null) {
                        BasicText(
                            text = subtitle,
                            style = type.bodySmall.copy(color = secondaryColor),
                            modifier = Modifier.padding(top = 2.dp),
                        )
                    }
                }

                if (valueText != null) {
                    BasicText(
                        text = valueText,
                        style = type.bodyMedium.copy(color = secondaryColor),
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
                trailing?.invoke(this)
                if (showChevron) {
                    Chevron(
                        color = secondaryColor,
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
            }
        }
        if (showDivider) {
            ListRowDivider(leadingInset = if (leading != null) leadingSize else 0.dp)
        }
    }
}

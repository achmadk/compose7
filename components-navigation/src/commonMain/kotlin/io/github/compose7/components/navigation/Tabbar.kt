package io.github.compose7.components.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import io.github.compose7.core.haptics.Compose7Haptic
import io.github.compose7.core.theme.Compose7MotionTokens
import io.github.compose7.core.theme.Compose7Style
import io.github.compose7.core.theme.Compose7ThemeColors
import io.github.compose7.core.theme.Compose7ThemeShapes
import io.github.compose7.core.theme.Compose7ThemeTypography
import io.github.compose7.core.theme.currentCompose7Style
import io.github.compose7.foundation.Compose7Surface
import io.github.compose7.foundation.Divider
import io.github.compose7.foundation.compose7Clickable

/**
 * One destination in a [Tabbar] (PRD §8.2). Icons and badges are
 * caller-provided slots — Compose7 bundles no icon set, ever (PRD §11).
 *
 * @param contentDescription Optional spoken label for the whole tab; falls
 *   back to the visible label when null.
 */
@Immutable
class TabData(
    val label: String,
    val icon: (@Composable () -> Unit)? = null,
    val badge: (@Composable () -> Unit)? = null,
    val contentDescription: String? = null,
)

/**
 * Bottom tab bar (PRD §8.2 / Appendix A "Tabbar"): icon+label destinations
 * with badge overlays and correct bottom safe-area inset.
 *
 * Style branch (PRD §9): Material — surface background, primary-color active
 * label/icon, no indicator; Cupertino — flat surface with a top hairline,
 * primary active / onSurfaceVariant inactive; DesktopUI7 — top border plus a
 * small primary pill under the active label. All heights are 56/49/48dp plus
 * the navigation-bar inset.
 *
 * ```kotlin
 * Tabbar(tabs, selectedIndex = index, onSelect = { index = it })
 * ```
 */
@Composable
fun Tabbar(
    tabs: List<TabData>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color? = null,
    contentColor: Color? = null,
    applyBottomInset: Boolean = true,
) {
    val style = currentCompose7Style
    val colors = Compose7ThemeColors
    val type = Compose7ThemeTypography
    val motion = Compose7MotionTokens
    val labelStyle = when (style) {
        Compose7Style.Cupertino -> type.labelSmall
        else -> type.labelMedium
    }

    Compose7Surface(
        modifier = modifier.fillMaxWidth(),
        color = containerColor ?: colors.surface,
        contentColor = contentColor ?: colors.onSurface,
    ) {
        Column(
            modifier = if (applyBottomInset) {
                Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            } else {
                Modifier
            },
        ) {
            if (style != Compose7Style.Material) {
                Divider(color = colors.outlineVariant, insetStart = 0.dp)
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(tabbarHeight(style)),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                tabs.forEachIndexed { index, tab ->
                    val icon = tab.icon
                    val badge = tab.badge
                    val itemSelected = index == selectedIndex
                    val active by animateColorAsState(
                        targetValue = if (itemSelected) colors.primary else colors.onSurfaceVariant,
                        animationSpec = tween(motion.fastDurationMs, easing = motion.standardEasing),
                        label = "tabbarColor",
                    )
                    val pillAlpha by animateFloatAsState(
                        targetValue = if (itemSelected) 1f else 0f,
                        animationSpec = tween(motion.fastDurationMs, easing = motion.standardEasing),
                        label = "tabbarPill",
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .semantics {
                                selected = itemSelected
                                if (tab.contentDescription != null) {
                                    contentDescription = tab.contentDescription
                                }
                            }
                            .compose7Clickable(
                                role = Role.Tab,
                                haptic = Compose7Haptic.Selection,
                                onClick = { onSelect(index) },
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                        ) {
                            if (icon != null) {
                                Box(contentAlignment = Alignment.Center) {
                                    icon()
                                    if (badge != null) {
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .offset(x = 8.dp, y = (-4).dp),
                                        ) {
                                            badge()
                                        }
                                    }
                                }
                                BasicText(text = tab.label, style = labelStyle.copy(color = active))
                            } else {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    BasicText(text = tab.label, style = labelStyle.copy(color = active))
                                    badge?.invoke()
                                }
                            }
                            if (style == Compose7Style.DesktopUI7) {
                                Box(
                                    modifier = Modifier
                                        .width(24.dp)
                                        .height(3.dp)
                                        .graphicsLayer { alpha = pillAlpha }
                                        .background(colors.primary, Compose7ThemeShapes.badge),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

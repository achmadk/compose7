package io.github.compose7.components.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.compose7.core.haptics.Compose7Haptic
import io.github.compose7.core.theme.Compose7MotionTokens
import io.github.compose7.core.theme.Compose7Style
import io.github.compose7.core.theme.Compose7ThemeColors
import io.github.compose7.core.theme.Compose7ThemeShapes
import io.github.compose7.core.theme.Compose7ThemeTypography
import io.github.compose7.core.theme.currentCompose7Style
import io.github.compose7.foundation.Divider
import io.github.compose7.foundation.compose7Clickable

/** One tab in a [Tabs] row (PRD §7 P0 "Tabs"): a label plus an optional trailing badge slot. */
@Immutable
class TabSpec(
    val label: String,
    val badge: (@Composable () -> Unit)? = null,
)

/**
 * Horizontal tab row (PRD §7 P0 / §8.2 / Appendix A "Tabs"), for pairing
 * with content panes. Pass `scrollable = true` for many tabs — they render
 * in a horizontally scrollable row instead of equal-width distribution.
 *
 * Style branch (PRD §9): Material/Cupertino slide a primary underline
 * indicator under the selected tab (2–3dp pill, medium motion); Cupertino
 * uses semibold labels; DesktopUI7 drops the underline for a primary-
 * container pill behind the selected label.
 *
 * ```kotlin
 * Tabs(tabs, selectedIndex = page, onSelect = { page = it })
 * ```
 */
@Composable
fun Tabs(
    tabs: List<TabSpec>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    scrollable: Boolean = false,
    containerColor: Color? = null,
    contentColor: Color? = null,
) {
    val style = currentCompose7Style
    val colors = Compose7ThemeColors
    val type = Compose7ThemeTypography
    val motion = Compose7MotionTokens
    val positions = remember { mutableStateMapOf<Int, Rect>() }
    val labelStyle = when (style) {
        Compose7Style.Cupertino -> type.labelLarge
        else -> type.labelMedium
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(containerColor ?: colors.surface),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(tabsHeight(style)),
        ) {
            val density = LocalDensity.current
            val target = positions[selectedIndex]
            val x by animateDpAsState(
                targetValue = with(density) { (target?.left ?: 0f).toDp() },
                animationSpec = tween(motion.mediumDurationMs, easing = motion.standardEasing),
                label = "tabIndicatorX",
            )
            val w by animateDpAsState(
                targetValue = with(density) { (target?.width ?: 0f).toDp() },
                animationSpec = tween(motion.mediumDurationMs, easing = motion.standardEasing),
                label = "tabIndicatorW",
            )
            Box(
                modifier = if (style == Compose7Style.DesktopUI7) {
                    Modifier
                        .align(Alignment.Center)
                        .offset(x = x + w * 0.15f)
                        .width(w * 0.7f)
                        .height(28.dp)
                        .background(colors.primaryContainer, Compose7ThemeShapes.medium)
                } else {
                    Modifier
                        .align(Alignment.BottomCenter)
                        .offset(x = x)
                        .width(w)
                        .height(if (style == Compose7Style.Material) 3.dp else 2.dp)
                        .background(colors.primary, Compose7ThemeShapes.badge)
                },
            )
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .let { if (scrollable) it.horizontalScroll(rememberScrollState()) else it },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                tabs.forEachIndexed { index, tab ->
                    val badge = tab.badge
                    val itemSelected = index == selectedIndex
                    val labelColor by animateColorAsState(
                        targetValue = if (itemSelected) {
                            colors.primary
                        } else {
                            contentColor ?: colors.onSurfaceVariant
                        },
                        animationSpec = tween(motion.fastDurationMs, easing = motion.standardEasing),
                        label = "tabLabelColor",
                    )
                    val tabModifier = if (scrollable) {
                        Modifier
                            .defaultMinSize(minWidth = 72.dp)
                            .padding(horizontal = 16.dp)
                    } else {
                        Modifier.weight(1f)
                    }
                    Box(
                        modifier = tabModifier
                            .fillMaxHeight()
                            .onGloballyPositioned { coords -> positions[index] = coords.boundsInParent() }
                            .semantics { selected = itemSelected }
                            .compose7Clickable(
                                role = Role.Tab,
                                haptic = Compose7Haptic.Selection,
                                onClick = { onSelect(index) },
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            BasicText(
                                text = tab.label,
                                style = labelStyle.copy(
                                    color = labelColor,
                                    fontWeight = if (itemSelected) FontWeight.SemiBold else null,
                                ),
                            )
                            badge?.invoke()
                        }
                    }
                }
            }
        }
        Divider(color = colors.outlineVariant, insetStart = 0.dp)
    }
}

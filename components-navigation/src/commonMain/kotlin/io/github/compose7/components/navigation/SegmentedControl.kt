package io.github.compose7.components.navigation

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import io.github.compose7.foundation.compose7Clickable

/**
 * iOS `UISegmentedControl` equivalent (PRD §7 P0 / §9): a shared-track
 * choice row with a sliding selected capsule. Requires bounded width (place
 * it in a Row slot or a padded container, not an unbounded column).
 *
 * Style branch: Cupertino — rounded pill track (surfaceVariant) with a
 * surface capsule and hairline border; Material — outlined track with a
 * primaryContainer capsule (M3 segmented button); DesktopUI7 — flat bordered
 * track with a primaryContainer capsule. Changing selection emits a
 * Selection haptic and animates the capsule with the emphasized easing.
 *
 * Note: the control keeps its platform-native visual height (32/40dp), which
 * is under the 44/48dp touch-target guideline — its full-width segments keep
 * the practical hit area generous.
 *
 * ```kotlin
 * SegmentedControl(segments = listOf("Day", "Week", "Month"), selectedIndex = i, onSelect = { i = it })
 * ```
 *
 * @param enabled Per-segment enabled flags; null enables every segment.
 */
@Composable
fun SegmentedControl(
    segments: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    enabled: List<Boolean>? = null,
    containerColor: Color? = null,
    contentColor: Color? = null,
) {
    if (segments.isEmpty()) return

    val style = currentCompose7Style
    val colors = Compose7ThemeColors
    val type = Compose7ThemeTypography
    val motion = Compose7MotionTokens
    val trackShape = Compose7ThemeShapes.chip

    val trackColor = containerColor ?: colors.surfaceVariant
    val trackBorder = when (style) {
        Compose7Style.Material -> BorderStroke(1.dp, colors.outline)
        Compose7Style.DesktopUI7 -> BorderStroke(1.dp, colors.outlineVariant)
        else -> null
    }
    val trackPadding = if (style == Compose7Style.Cupertino) 2.dp else 3.dp
    val capsuleColor = when (style) {
        Compose7Style.Cupertino -> colors.surface
        else -> colors.primaryContainer
    }
    val capsuleBorder = if (style == Compose7Style.Cupertino) {
        BorderStroke(0.5.dp, colors.outlineVariant)
    } else {
        null
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(segmentedControlHeight(style))
            .clip(trackShape)
            .background(trackColor)
            .let { if (trackBorder != null) it.border(trackBorder, trackShape) else it }
            .padding(trackPadding),
    ) {
        val segmentWidth = maxWidth / segments.size
        val capsuleX by animateDpAsState(
            targetValue = segmentWidth * selectedIndex.coerceIn(0, segments.size - 1),
            animationSpec = tween(motion.mediumDurationMs, easing = motion.emphasizedEasing),
            label = "segmentCapsule",
        )
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = capsuleX)
                .size(width = segmentWidth, height = maxHeight)
                .background(capsuleColor, trackShape)
                .let { if (capsuleBorder != null) it.border(capsuleBorder, trackShape) else it },
        )
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val fallbackContentColor = contentColor ?: colors.onSurface
            segments.forEachIndexed { index, label ->
                val segmentEnabled = enabled?.getOrNull(index) ?: true
                val itemSelected = index == selectedIndex
                val textColor = when {
                    !segmentEnabled -> fallbackContentColor.copy(alpha = 0.38f)
                    itemSelected && style == Compose7Style.Cupertino -> colors.onSurface
                    itemSelected -> colors.onPrimaryContainer
                    style == Compose7Style.Cupertino -> colors.onSurfaceVariant
                    else -> fallbackContentColor
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .semantics { selected = itemSelected }
                        .compose7Clickable(
                            enabled = segmentEnabled,
                            role = Role.RadioButton,
                            haptic = Compose7Haptic.Selection,
                            onClick = { onSelect(index) },
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText(
                        text = label,
                        style = type.labelLarge.let {
                            if (itemSelected && style == Compose7Style.Cupertino) {
                                it.copy(fontWeight = FontWeight.SemiBold)
                            } else {
                                it
                            }
                        }.copy(color = textColor),
                    )
                }
            }
        }
    }
}

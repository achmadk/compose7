package io.github.compose7.foundation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.compose7.core.haptics.Compose7Haptic
import io.github.compose7.core.theme.Compose7MotionTokens
import io.github.compose7.core.theme.Compose7Style
import io.github.compose7.core.theme.Compose7ThemeColors
import io.github.compose7.core.theme.Compose7ThemeShapes
import io.github.compose7.core.theme.Compose7ThemeTypography
import io.github.compose7.core.theme.LocalCompose7Style

/** Card elevation per style: Material elevates flat, the others sit flat (DesktopUI7 borders instead). */
@Composable
fun cardElevation(): Dp =
    if (LocalCompose7Style.current == Compose7Style.Material) 1.dp else 0.dp

/** Card border per style: DesktopUI7 uses a hairline border; Material/Cupertino are borderless. */
@Composable
fun cardBorder(): BorderStroke? =
    if (LocalCompose7Style.current == Compose7Style.DesktopUI7) {
        BorderStroke(1.dp, Compose7ThemeColors.outlineVariant)
    } else null

/**
 * Card container (PRD §7, P0). Optionally clickable with correct press
 * feedback, haptics, and semantics.
 *
 * No internal padding by design — full-bleed media cards and padded text
 * cards are both common; pad the content column yourself.
 */
@Composable
fun Card(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    shape: Shape = Compose7ThemeShapes.card,
    color: Color = Compose7ThemeColors.surface,
    contentColor: Color = Compose7ThemeColors.onSurface,
    border: BorderStroke? = cardBorder(),
    elevation: Dp = cardElevation(),
    content: @Composable ColumnScope.() -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    Compose7Surface(
        modifier = modifier.then(
            if (onClick != null) {
                Modifier.compose7Clickable(
                    interactionSource = interaction,
                    role = Role.Button,
                    haptic = Compose7Haptic.Light,
                    shape = shape,
                    onClick = onClick,
                )
            } else Modifier
        ),
        shape = shape,
        color = color,
        contentColor = contentColor,
        border = border,
        elevation = elevation,
    ) {
        Column(modifier = Modifier.fillMaxWidth(), content = content)
    }
}

/**
 * Expandable card (PRD §7, P0): a [Card] with a tappable header and animated
 * content reveal. Header shows title/subtitle plus an optional trailing slot
 * and a drawn disclosure chevron (no bundled icon assets — PRD §4).
 *
 * Behavior is identical across styles by default; only shape/type tokens
 * differ.
 */
@Composable
fun ExpandableCard(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    subtitle: String? = null,
    headerContent: (@Composable RowScope.() -> Unit)? = null,
    headerPadding: PaddingValues = PaddingValues(16.dp),
    chevronColor: Color = Compose7ThemeColors.onSurfaceVariant,
    shape: Shape = Compose7ThemeShapes.card,
    color: Color = Compose7ThemeColors.surface,
    contentColor: Color = Compose7ThemeColors.onSurface,
    border: BorderStroke? = cardBorder(),
    elevation: Dp = cardElevation(),
    content: @Composable ColumnScope.() -> Unit,
) {
    val motion = Compose7MotionTokens
    val chevronRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(motion.mediumDurationMs, easing = motion.standardEasing),
        label = "chevron",
    )
    val interaction = remember { MutableInteractionSource() }

    Compose7Surface(
        modifier = modifier,
        shape = shape,
        color = color,
        contentColor = contentColor,
        border = border,
        elevation = elevation,
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .compose7Clickable(
                        interactionSource = interaction,
                        role = Role.Button,
                        haptic = Compose7Haptic.Selection,
                        onClick = { onExpandedChange(!expanded) },
                    )
                    .padding(headerPadding),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    if (title != null) {
                        BasicText(
                            text = title,
                            style = Compose7ThemeTypography.titleMedium.copy(color = contentColor),
                        )
                    }
                    if (subtitle != null) {
                        BasicText(
                            text = subtitle,
                            style = Compose7ThemeTypography.bodySmall.copy(color = Compose7ThemeColors.onSurfaceVariant),
                        )
                    }
                }
                headerContent?.invoke(this)
                Chevron(
                    color = chevronColor,
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .rotate(chevronRotation),
                )
            }
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(tween(motion.mediumDurationMs, easing = motion.emphasizedEasing)),
                exit = shrinkVertically(tween(motion.mediumDurationMs, easing = motion.emphasizedEasing)),
            ) {
                Column(content = content)
            }
        }
    }
}

/**
 * Drawn disclosure chevron — a canvas primitive, never an icon asset
 * (PRD §4: bring-your-own-icons, no bundled set).
 */
@Composable
fun Chevron(
    color: Color = Compose7ThemeColors.onSurfaceVariant,
    modifier: Modifier = Modifier,
    size: Dp = 14.dp,
    thickness: Dp = 2.dp,
) {
    Canvas(modifier = modifier.size(size)) {
        val s = size.toPx()
        val t = thickness.toPx()
        val path = Path().apply {
            moveTo(s * 0.28f, s * 0.16f)
            lineTo(s * 0.66f, s * 0.5f)
            lineTo(s * 0.28f, s * 0.84f)
        }
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = t, cap = StrokeCap.Round, join = StrokeJoin.Round),
        )
    }
}

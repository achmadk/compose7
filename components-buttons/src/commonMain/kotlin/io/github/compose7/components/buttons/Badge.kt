package io.github.compose7.components.buttons

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.compose7.core.theme.Compose7ThemeColors
import io.github.compose7.core.theme.Compose7ThemeShapes
import io.github.compose7.core.theme.Compose7ThemeTypography
import io.github.compose7.foundation.LocalContentColor

/**
 * Badge (PRD §7, P0 / Appendix A "Badge") — counts and dots over any slot.
 *
 * ```kotlin
 * Box {
 *     Icon(notificationsVector, "Notifications")
 *     BadgeCount(3, Modifier.align(Alignment.TopEnd))
 * }
 * ```
 */
@Composable
fun Badge(
    modifier: Modifier = Modifier,
    containerColor: Color = Compose7ThemeColors.primary,
    contentColor: Color = Compose7ThemeColors.onPrimary,
    shape: Shape = Compose7ThemeShapes.badge,
    content: @Composable RowScope.() -> Unit,
) {
    Box(
        modifier = modifier.background(containerColor, shape),
        contentAlignment = Alignment.Center,
    ) {
        CompositionLocalProvider(LocalContentColor provides contentColor) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 5.dp, vertical = 1.dp)
                    .defaultMinSize(minWidth = 12.dp, minHeight = 14.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                content = content,
            )
        }
    }
}

/** Count badge rendering a number, capped at [max] with a `+` suffix. */
@Composable
fun BadgeCount(
    count: Int,
    modifier: Modifier = Modifier,
    max: Int = 99,
    containerColor: Color = Compose7ThemeColors.error,
    contentColor: Color = Compose7ThemeColors.onError,
    shape: Shape = Compose7ThemeShapes.badge,
) {
    val text = if (count > max) "$max+" else count.toString()
    Badge(
        modifier = modifier,
        containerColor = containerColor,
        contentColor = contentColor,
        shape = shape,
    ) {
        BasicText(
            text = text,
            style = Compose7ThemeTypography.labelSmall.copy(color = contentColor),
        )
    }
}

/** Single unread dot badge ([size] across). */
@Composable
fun BadgeDot(
    modifier: Modifier = Modifier,
    color: Color = Compose7ThemeColors.error,
    size: Dp = 8.dp,
) {
    Box(
        modifier = modifier
            .size(size)
            .background(color, Compose7ThemeShapes.badge),
    )
}

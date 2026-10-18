package io.github.compose7.foundation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.compose7.core.theme.Compose7ThemeColors

/**
 * Content color for the current surface — provided by [Compose7Surface] and
 * consumed by default-colored content inside it (mirrors Material3's
 * `LocalContentColor` pattern). Defaults to [Compose7ThemeColors.onSurface]
 * when no surface provides one.
 */
val LocalContentColor = compositionLocalOf { Color.Unspecified }

/** Convenience accessor for [LocalContentColor] with the theme fallback applied. */
val CurrentContentColor: Color
    @Composable get() = LocalContentColor.current.takeOrElse { Compose7ThemeColors.onSurface }

/**
 * The colored, shaped container every Compose7 surface builds on: cards,
 * sheets, dialogs, panels. Applies elevation (platform-correct shadow),
 * background, optional border, and provides [LocalContentColor].
 */
@Composable
fun Compose7Surface(
    modifier: Modifier = Modifier,
    shape: Shape = RectangleShape,
    color: Color = Compose7ThemeColors.surface,
    contentColor: Color = Compose7ThemeColors.onSurface,
    border: BorderStroke? = null,
    elevation: Dp = 0.dp,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .shadow(elevation, shape, clip = elevation > 0.dp)
            .clip(shape)
            .background(color)
            .let { if (border != null) it.border(border, shape) else it },
    ) {
        CompositionLocalProvider(LocalContentColor provides contentColor) {
            content()
        }
    }
}

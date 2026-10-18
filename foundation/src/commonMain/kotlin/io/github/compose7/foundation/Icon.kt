package io.github.compose7.foundation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.compose7.core.theme.Compose7Style
import io.github.compose7.core.theme.LocalCompose7Style

/**
 * Icon wrapper (PRD §7 "Icon wrapper" / §11: icons are bring-your-own —
 * Compose7 bundles no icon set, ever).
 *
 * ```kotlin
 * Icon(Icons.Default.Settings, contentDescription = "Settings")
 * ```
 *
 * @param tint Color applied to the vector; defaults to the current content color.
 * @param size Defaults to 20dp on Cupertino style, 24dp otherwise.
 */
@Composable
fun Icon(
    imageVector: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = CurrentContentColor,
    size: Dp = defaultIconSize(),
) {
    Image(
        imageVector = imageVector,
        contentDescription = contentDescription,
        modifier = modifier.size(size),
        colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(tint),
    )
}

/** [Icon] overload for painter-based (e.g. resource/async-loaded) icons. */
@Composable
fun Icon(
    painter: Painter,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = CurrentContentColor,
    size: Dp = defaultIconSize(),
) {
    Image(
        painter = painter,
        contentDescription = contentDescription,
        modifier = modifier.size(size),
        colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(tint),
    )
}

/** Default icon box size for the active style. */
@Composable
fun defaultIconSize(): Dp =
    if (LocalCompose7Style.current == Compose7Style.Cupertino) 20.dp else 24.dp

/**
 * Renders [content] centered in a fixed box — for icon-only affordances where
 * the caller still controls the visual size (PRD §12: touch targets stay
 * correct via [Modifier.compose7MinimumTouchTarget]).
 */
@Composable
fun IconBox(
    size: Dp,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        content()
    }
}

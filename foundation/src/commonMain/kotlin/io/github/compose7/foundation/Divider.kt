package io.github.compose7.foundation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.compose7.core.theme.Compose7Style
import io.github.compose7.core.theme.Compose7StyleInfo
import io.github.compose7.core.theme.LocalCompose7Style
import io.github.compose7.core.theme.Compose7ThemeColors

/**
 * Hairline separator (Framework7's list divider / `hairlines`).
 *
 * Platform-correct by default (PRD §8.3): edge-to-edge and 1dp on Material,
 * content-inset and a half-pixel hairline on Cupertino, edge-to-edge on
 * DesktopUI7. [insetStart] defaults to the active style's divider inset.
 */
@Composable
fun Divider(
    modifier: Modifier = Modifier,
    color: Color = Compose7ThemeColors.outlineVariant,
    thickness: Dp = if (LocalCompose7Style.current == Compose7Style.Cupertino) 0.5.dp else 1.dp,
    insetStart: Dp = Compose7StyleInfo.listDividerInset,
    insetEnd: Dp = 0.dp,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = insetStart, end = insetEnd)
            .height(thickness)
            .background(color),
    )
}

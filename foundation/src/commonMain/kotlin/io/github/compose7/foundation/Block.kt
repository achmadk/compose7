package io.github.compose7.foundation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import io.github.compose7.core.theme.Compose7StyleInfo

/**
 * Framework7-style content block: a column of content inset from the screen
 * edges by the active style's content inset (16dp on Material/Cupertino,
 * 12dp on DesktopUI7).
 */
@Composable
fun Block(
    modifier: Modifier = Modifier,
    contentPadding: Dp = Compose7StyleInfo.listContentInset,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier.padding(horizontal = contentPadding),
        content = content,
    )
}

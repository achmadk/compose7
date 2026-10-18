package io.github.compose7.components.lists

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import io.github.compose7.core.theme.Compose7StyleInfo
import io.github.compose7.core.theme.Compose7ThemeColors

/**
 * Visual modes a [List] can render in (Framework7 "Simple List" vs
 * "Grouped List"; PRD §8.3).
 */
enum class ListStyle {
    /** Edge-to-edge rows on `colors.surface` with full-width dividers. */
    Plain,

    /** iOS-style grouped card: content inset from the edges, rounded surface, section gaps. */
    Grouped,
}

/**
 * List container (PRD §7, P0 / Appendix A "List View"): the styled block rows,
 * sections, and buttons are placed into.
 *
 * One API, two renderings (PRD §9):
 *  - **Plain** (default on Material/DesktopUI7) — edge-to-edge rows on
 *    `colors.surface`, dividers span the full width.
 *  - **Grouped** (default on Cupertino) — content inset by
 *    [Compose7StyleProperties.listContentInset][io.github.compose7.core.theme.Compose7StyleProperties.listContentInset]
 *    over the `colors.background` field, with rows placed inside
 *    [ListSection] cards and gaps between sections.
 *
 * Rows placed inside a list automatically pick up its style and insets; rows
 * used standalone fall back to per-style defaults.
 *
 * ```kotlin
 * List {
 *     ListSection("Account") {
 *         ListItem("Profile", showChevron = true, onClick = { … })
 *         ListItem("Notifications", showChevron = true, onClick = { … })
 *     }
 * }
 * ```
 *
 * @param style Plain or grouped; `null` follows the active style's default.
 * @param inset Content inset for grouped lists; `null` uses the style's token.
 */
@Composable
fun List(
    modifier: Modifier = Modifier,
    style: ListStyle? = null,
    inset: Dp? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = Compose7ThemeColors
    val resolvedStyle = style ?: defaultListStyle()
    val contentInset = inset ?: Compose7StyleInfo.listContentInset
    val layout = ListLayoutInfo(resolvedStyle, contentInset)

    when (resolvedStyle) {
        ListStyle.Plain -> Column(modifier = modifier.background(colors.surface)) {
            CompositionLocalProvider(LocalListLayout provides layout) {
                content()
            }
        }
        ListStyle.Grouped -> Column(
            modifier = modifier
                .background(colors.background)
                .padding(horizontal = contentInset)
                .padding(vertical = contentInset),
            verticalArrangement = Arrangement.spacedBy(contentInset),
        ) {
            CompositionLocalProvider(LocalListLayout provides layout) {
                content()
            }
        }
    }
}

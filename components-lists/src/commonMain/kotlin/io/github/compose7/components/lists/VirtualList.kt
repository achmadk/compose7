package io.github.compose7.components.lists

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.compose7.core.theme.Compose7StyleInfo
import io.github.compose7.core.theme.Compose7ThemeColors
import io.github.compose7.core.theme.Compose7ThemeShapes

/**
 * Virtual list (PRD §8.3 / Appendix A "Virtual List"): a thin, styled wrapper
 * over `LazyColumn` — no independent virtualization engine.
 *
 * Renders exactly like [List] (same style resolution, insets, surface
 * treatment, and row/divider behavior for nested [ListItem]s); the
 * difference is purely in the API surface: [content] is a [LazyListScope]
 * for large datasets. Relationship to `foundation`: `Page`/`LazyPage` are
 * screen scaffolds with safe-area handling; `VirtualList` is the
 * Framework7-named *list* entry point — put it inside a `Page` when it needs
 * safe-area insets.
 *
 * ```kotlin
 * VirtualList(itemSpacing = 8.dp) {
 *     items(contacts.size) { i -> ListItem(contacts[i].name, showChevron = true) }
 * }
 * ```
 *
 * @param state Scroll state; [rememberLazyListState] by default.
 * @param style Plain or grouped; `null` follows the active style's default.
 * @param contentPadding Padding around the scrollable content.
 * @param itemSpacing Vertical gap between adjacent items.
 */
@Composable
fun VirtualList(
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    style: ListStyle? = null,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    itemSpacing: Dp = 0.dp,
    content: LazyListScope.() -> Unit,
) {
    val colors = Compose7ThemeColors
    val shapes = Compose7ThemeShapes
    val resolvedStyle = style ?: defaultListStyle()
    val inset = Compose7StyleInfo.listContentInset
    val layout = ListLayoutInfo(resolvedStyle, inset)
    val arrangement = Arrangement.spacedBy(itemSpacing)

    CompositionLocalProvider(LocalListLayout provides layout) {
        when (resolvedStyle) {
            ListStyle.Plain -> LazyColumn(
                modifier = modifier.background(colors.surface),
                state = state,
                contentPadding = contentPadding,
                verticalArrangement = arrangement,
                content = content,
            )
            ListStyle.Grouped -> LazyColumn(
                modifier = modifier
                    .background(colors.background)
                    .padding(horizontal = inset)
                    .clip(shapes.card)
                    .background(colors.surface),
                state = state,
                contentPadding = contentPadding,
                verticalArrangement = arrangement,
                content = content,
            )
        }
    }
}

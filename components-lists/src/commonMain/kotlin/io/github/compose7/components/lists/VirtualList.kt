package io.github.compose7.components.lists

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
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

/** Scroll axis for [VirtualList] (PRD §8.3: a wrapper over `LazyColumn`/`LazyRow`). */
enum class ListOrientation { Vertical, Horizontal }

/**
 * Virtual list (PRD §8.3 / Appendix A "Virtual List"): a thin, styled wrapper
 * over `LazyColumn` (vertical) or `LazyRow` (horizontal) - no independent
 * virtualization engine.
 *
 * Renders exactly like [List] (same style resolution, insets, surface
 * treatment, and row/divider behavior for nested [ListItem]s); the
 * difference is purely in the API surface: [content] is a [LazyListScope]
 * for large datasets. Relationship to `foundation`: `Page`/`LazyPage` are
 * screen scaffolds with safe-area handling; `VirtualList` is the
 * Framework7-named *list* entry point - put it inside a `Page` when it needs
 * safe-area insets.
 *
 * ```kotlin
 * VirtualList(itemSpacing = 8.dp) {
 *     items(contacts.size) { i -> ListItem(contacts[i].name, showChevron = true) }
 * }
 * ```
 *
 * @param state Scroll state; [rememberLazyListState] by default. The same
 *   state type serves both axes.
 * @param style Plain or grouped; `null` follows the active style's default.
 * @param orientation Vertical (default) or horizontal.
 * @param contentPadding Padding around the scrollable content.
 * @param itemSpacing Gap between adjacent items, along [orientation].
 */
@Composable
fun VirtualList(
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    style: ListStyle? = null,
    orientation: ListOrientation = ListOrientation.Vertical,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    itemSpacing: Dp = 0.dp,
    content: LazyListScope.() -> Unit,
) {
    val colors = Compose7ThemeColors
    val shapes = Compose7ThemeShapes
    val resolvedStyle = style ?: defaultListStyle()
    val inset = Compose7StyleInfo.listContentInset
    val layout = ListLayoutInfo(resolvedStyle, inset)
    val vertical = orientation == ListOrientation.Vertical

    // Grouped lists inset on the cross axis: horizontal padding for a column,
    // vertical padding for a row.
    val groupedModifier = Modifier
        .background(colors.background)
        .padding(
            start = if (vertical) inset else 0.dp,
            end = if (vertical) inset else 0.dp,
            top = if (vertical) 0.dp else inset,
            bottom = if (vertical) 0.dp else inset,
        )
        .clip(shapes.card)
        .background(colors.surface)

    CompositionLocalProvider(LocalListLayout provides layout) {
        when {
            resolvedStyle == ListStyle.Plain && vertical -> LazyColumn(
                modifier = modifier.background(colors.surface),
                state = state,
                contentPadding = contentPadding,
                verticalArrangement = Arrangement.spacedBy(itemSpacing),
                content = content,
            )
            resolvedStyle == ListStyle.Plain -> LazyRow(
                modifier = modifier.background(colors.surface),
                state = state,
                contentPadding = contentPadding,
                horizontalArrangement = Arrangement.spacedBy(itemSpacing),
                content = content,
            )
            vertical -> LazyColumn(
                modifier = modifier.then(groupedModifier),
                state = state,
                contentPadding = contentPadding,
                verticalArrangement = Arrangement.spacedBy(itemSpacing),
                content = content,
            )
            else -> LazyRow(
                modifier = modifier.then(groupedModifier),
                state = state,
                contentPadding = contentPadding,
                horizontalArrangement = Arrangement.spacedBy(itemSpacing),
                content = content,
            )
        }
    }
}

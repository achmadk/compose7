package io.github.compose7.foundation

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter

/**
 * Infinite-scroll trigger (PRD §7 "Scroll behaviors", P1).
 *
 * Fires [onLoadMore] once each time the user scrolls within [threshold] items of
 * the end of [state]'s list, then re-arms only after the list grows. It is an
 * effect, not a wrapper: attach it to any `LazyColumn`/`LazyPage` you already
 * own so paging stays the caller's decision (PRD §8.3).
 *
 * ```kotlin
 * val state = rememberLazyListState()
 * InfiniteScrollEffect(state, enabled = !loading && hasMore, onLoadMore = viewModel::page)
 * LazyPage(state = state) { items(rows) { … } }
 * ```
 *
 * @param threshold How many items from the end the trigger fires; `0` fires
 *   only when the last item is visible.
 */
@Composable
fun InfiniteScrollEffect(
    state: LazyListState,
    onLoadMore: () -> Unit,
    enabled: Boolean = true,
    threshold: Int = 3,
) {
    val currentOnLoadMore by rememberUpdatedState(onLoadMore)
    val currentEnabled by rememberUpdatedState(enabled)
    val currentThreshold by rememberUpdatedState(threshold)

    LaunchedEffect(state) {
        snapshotFlow {
            val layout = state.layoutInfo
            val total = layout.totalItemsCount
            val lastVisible = layout.visibleItemsInfo.lastOrNull()?.index ?: -1
            // Emit the count too, so a list that grows re-arms the trigger.
            Triple(total, lastVisible, total > 0 && lastVisible >= total - 1 - currentThreshold)
        }
            .distinctUntilChanged()
            .filter { it.third && currentEnabled }
            .collect { currentOnLoadMore() }
    }
}

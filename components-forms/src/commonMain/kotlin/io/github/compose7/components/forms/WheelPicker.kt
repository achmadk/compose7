package io.github.compose7.components.forms

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.compose7.core.theme.Compose7Style
import io.github.compose7.core.theme.Compose7ThemeColors
import io.github.compose7.core.theme.Compose7ThemeTypography
import io.github.compose7.core.theme.currentCompose7Style
import kotlinx.coroutines.flow.filter

/**
 * Cupertino-style wheel picker (PRD §8.5: Smart Select uses a wheel-picker
 * sheet on the Cupertino style). Snaps one row per [itemHeight] and reports the
 * centered row once scrolling settles.
 *
 * Internal on purpose: the public surface is [SmartSelect]; a standalone wheel
 * would need its own date/time variants to be worth publishing.
 *
 * @param selectedIndex Initially centered row; updates from outside are
 *   scrolled into view.
 * @param visibleItems Odd row count in the viewport (5 by default).
 */
@Composable
internal fun WheelPicker(
    options: List<String>,
    selectedIndex: Int,
    onSelectedIndexChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    itemHeight: Dp = 44.dp,
    visibleItems: Int = 5,
) {
    if (options.isEmpty()) return

    val colors = Compose7ThemeColors
    val type = Compose7ThemeTypography
    val rows = visibleItems.coerceAtLeast(1)
    val state = rememberLazyListState()
    val fling = rememberSnapFlingBehavior(lazyListState = state)

    var reportedIndex by remember { mutableIntStateOf(selectedIndex.coerceIn(options.indices)) }

    LaunchedEffect(selectedIndex, options.size) {
        val target = selectedIndex.coerceIn(options.indices)
        if (state.firstVisibleItemIndex != target) state.scrollToItem(target)
        reportedIndex = target
    }

    LaunchedEffect(state, options.size) {
        snapshotFlow { state.isScrollInProgress to state.firstVisibleItemIndex }
            .filter { (scrolling, _) -> !scrolling }
            .collect { (_, index) ->
                if (index != reportedIndex) {
                    reportedIndex = index
                    onSelectedIndexChange(index)
                }
            }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(itemHeight * rows),
        contentAlignment = Alignment.Center,
    ) {
        // Center band under the wheel rows.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(itemHeight)
                .background(colors.surfaceVariant.copy(alpha = 0.6f)),
        )
        LazyColumn(
            state = state,
            flingBehavior = fling,
            contentPadding = PaddingValues(vertical = itemHeight * (rows / 2)),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            items(options.size) { index ->
                val selected = index == reportedIndex
                Box(
                    modifier = Modifier.fillMaxWidth().height(itemHeight),
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText(
                        text = options[index],
                        style = type.bodyLarge.copy(
                            color = if (selected) colors.primary else colors.onSurfaceVariant,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                            textAlign = TextAlign.Center,
                        ),
                    )
                }
            }
        }
    }
}

/** Style hook used by [SmartSelect]: only the Cupertino language gets a wheel. */
internal val usesWheelPicker: Boolean
    @Composable get() = currentCompose7Style == Compose7Style.Cupertino

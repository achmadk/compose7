package io.github.compose7.foundation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.compose7.core.theme.Compose7ThemeColors

/**
 * A page: the scrollable content area of an app screen (Framework7's Page).
 *
 * Applies safe-area handling (PRD §7): top system-bar inset, horizontal safe
 * areas, and bottom inset. When the page is headed by a Navbar (which draws
 * its own status-bar background), pass `topSafeArea = false` so the inset
 * isn't applied twice.
 *
 * Give the same [scrollState] to a large-title Navbar to drive its collapse
 * animation (PRD §8.2).
 */
@Composable
fun Page(
    modifier: Modifier = Modifier,
    scrollState: ScrollState = rememberScrollState(),
    topSafeArea: Boolean = true,
    background: Color = Compose7ThemeColors.background,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    val insets = WindowInsets.safeDrawing
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(background)
            .let {
                if (topSafeArea) it.windowInsetsPadding(insets.only(WindowInsetsSides.Top)) else it
            }
            .windowInsetsPadding(insets.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom))
            .imePadding()
            .verticalScroll(scrollState)
            .padding(contentPadding),
        content = content,
    )
}

/**
 * Lazy variant of [Page] for long lists — a styled wrapper over `LazyColumn`
 * (PRD §8.3: virtual lists stay thin wrappers, no independent engine).
 */
@Composable
fun LazyPage(
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    topSafeArea: Boolean = true,
    background: Color = Compose7ThemeColors.background,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: LazyListScope.() -> Unit,
) {
    val insets = WindowInsets.safeDrawing
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(background)
            .let {
                if (topSafeArea) it.windowInsetsPadding(insets.only(WindowInsetsSides.Top)) else it
            }
            .windowInsetsPadding(insets.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom)),
        state = state,
        contentPadding = contentPadding,
        verticalArrangement = verticalArrangement,
        content = content,
    )
}

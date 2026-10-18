package io.github.compose7.sample.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import io.github.compose7.components.buttons.BadgeCount
import io.github.compose7.components.data.Accordion
import io.github.compose7.components.data.Breadcrumbs
import io.github.compose7.components.data.Crumb
import io.github.compose7.components.data.ExpandMode
import io.github.compose7.components.data.Timeline
import io.github.compose7.components.lists.ContactRow
import io.github.compose7.components.lists.ContactSection
import io.github.compose7.components.lists.ContactsList
import io.github.compose7.components.lists.List
import io.github.compose7.components.lists.ListButton
import io.github.compose7.components.lists.ListIndex
import io.github.compose7.components.lists.ListItem
import io.github.compose7.components.lists.ListSection
import io.github.compose7.components.lists.ListStyle
import io.github.compose7.components.lists.SwipeAction
import io.github.compose7.components.lists.Swipeout
import io.github.compose7.components.lists.VirtualList
import io.github.compose7.core.theme.Compose7ThemeColors
import io.github.compose7.core.theme.Compose7ThemeTypography
import io.github.compose7.foundation.Check
import io.github.compose7.foundation.Divider
import io.github.compose7.foundation.InfiniteScrollEffect
import io.github.compose7.foundation.PullToRefresh

private val InboxRows = listOf(
    "Design review notes",
    "Q4 roadmap sync",
    "Invoice #2291",
    "Welcome to Compose7",
    "Team offsite",
).mapIndexed { index, title -> title to if (index % 2 == 0) "Unread" else "Yesterday" }

/** Lists, data display, and list interactions (PRD §7: Lists / List interactions / Data display). */
@Composable
fun ListsScreen() {
    var expanded by remember { mutableStateOf(setOf(0)) }
    var refreshing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var virtualCount by remember { mutableIntStateOf(12) }
    val virtualState = rememberLazyListState()

    // Triggers once the user nears the end; the list then grows by a page.
    InfiniteScrollEffect(
        state = virtualState,
        enabled = virtualCount < 60,
        onLoadMore = { virtualCount = (virtualCount + 12).coerceAtMost(60) },
    )

    DemoSection("List rows") {
        List(style = ListStyle.Plain) {
            ListItem(
                title = "Notifications",
                valueText = "On",
                showChevron = true,
                onClick = {},
            )
            ListItem(
                title = "Storage",
                subtitle = "12.4 GB of 64 GB used",
                trailing = { BadgeCount(count = 3, modifier = Modifier.size(20.dp)) },
                onClick = {},
            )
            ListItem(
                title = "Disabled row",
                enabled = false,
                showDivider = false,
            )
        }
    }

    DemoSection("Grouped list with sections") {
        List(style = ListStyle.Grouped) {
            ListSection(title = "Mail") {
                InboxRows.forEach { (title, subtitle) ->
                    ListItem(
                        title = title,
                        subtitle = subtitle,
                        onClick = {},
                        trailing = {
                            BasicText(
                                text = ">",
                                style = Compose7ThemeTypography.bodyMedium.copy(
                                    color = Compose7ThemeColors.onSurfaceVariant,
                                ),
                            )
                        },
                    )
                }
            }
            ListSection(title = "Actions") {
                ListButton(text = "Mark all read", onClick = {})
            }
        }
    }

    DemoSection("Swipeout") {
        DemoCaption("Swipe a row horizontally to reveal actions.")
        Swipeout(
            endActions = listOf(
                SwipeAction(title = "Done", onClick = {}) {
                    Check(color = Compose7ThemeColors.onPrimary)
                },
                SwipeAction(title = "Delete", onClick = {}) {
                    Check(color = Compose7ThemeColors.onPrimary)
                },
            ),
        ) {
            ListItem(title = "Swipe me", showDivider = false, onClick = {})
        }
    }

    DemoSection("Contacts list with index") {
        ContactsList(
            sections = listOf(
                ContactSection(
                    title = "A",
                    items = listOf(
                        ContactRow("Ada Lovelace", "AL", "ada@compose7.dev"),
                        ContactRow("Alan Turing", "AT"),
                    ),
                ),
                ContactSection(
                    title = "G",
                    items = listOf(ContactRow("Grace Hopper", "GH", "grace@compose7.dev")),
                ),
            ),
        )
        ListIndex(
            letters = listOf("A", "G", "L", "T"),
            onLetterSelected = {},
            modifier = Modifier.padding(top = 8.dp),
        )
    }

    DemoSection("Virtual list + infinite scroll") {
        DemoCaption("Rows append as you approach the end (12 → 60).")
        VirtualList(
            state = virtualState,
            modifier = Modifier.size(width = 320.dp, height = 180.dp),
        ) {
            items(count = virtualCount) { index ->
                ListItem(
                    title = "Virtual row ${index + 1}",
                    subtitle = "Only visible rows compose",
                    showDivider = index < virtualCount - 1,
                    onClick = {},
                )
            }
        }
    }

    DemoSection("Pull to refresh") {
        DemoCaption("Drag the list down past the threshold, then release.")
        PullToRefresh(
            isRefreshing = refreshing,
            onRefresh = {
                refreshing = true
                scope.launch {
                    delay(1_200)
                    refreshing = false
                }
            },
            modifier = Modifier.size(width = 320.dp, height = 200.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
            ) {
                repeat(12) { index ->
                    ListItem(
                        title = "Refreshable row ${index + 1}",
                        showDivider = index < 11,
                    )
                }
            }
        }
    }

    DemoSection("Accordion") {
        Accordion(
            items = listOf(
                "Tokens, not hardcoded values",
                "One API, three renderings",
                "No bundled icons",
            ),
            expandedIndices = expanded,
            onExpandedIndicesChange = { expanded = it },
            expandMode = ExpandMode.Multiple,
            title = { it },
        ) { item ->
            BasicText(
                text = "Expanded: $item",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                style = Compose7ThemeTypography.bodyMedium.copy(color = Compose7ThemeColors.onSurfaceVariant),
            )
        }
    }

    DemoSection("Timeline") {
        Timeline(items = listOf("v0.1 core + foundation", "v0.2 forms & overlays", "v1.0 stable")) { item, position ->
            BasicText(
                text = item,
                modifier = Modifier.padding(start = 12.dp, bottom = 12.dp),
                style = Compose7ThemeTypography.bodyMedium.copy(color = Compose7ThemeColors.onSurface),
            )
        }
    }

    DemoSection("Breadcrumbs") {
        Breadcrumbs(
            items = listOf(Crumb("Home"), Crumb("Components"), Crumb("Lists")),
            onCrumbClick = {},
        )
        Divider(modifier = Modifier.padding(top = 12.dp))
    }
}

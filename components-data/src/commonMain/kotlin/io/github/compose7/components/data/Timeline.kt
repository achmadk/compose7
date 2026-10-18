package io.github.compose7.components.data

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import io.github.compose7.core.theme.Compose7ThemeColors

/** Where an item sits on a [Timeline] rail — the first/last rows adjust the drawn line. */
enum class TimelinePosition {
    /** First item: the rail only runs downward from its node. */
    Start,

    /** Middle item: the rail runs through the whole row. */
    Middle,

    /** Last item: the rail only runs downward into its node, never past it. */
    End,
}

/**
 * Timeline (PRD §7 "Data display" P1): a vertical rail of token-colored nodes
 * with each item's [content] start-padded past it.
 *
 * The rail is drawn per row — a solid `outlineVariant` line 2.dp thick with a
 * 10.dp `primary` node aligned to the row's first line — so it stays
 * continuous as content resizes. Node and rail flip sides with layout
 * direction (PRD §12: automatic RTL mirroring).
 *
 * ```kotlin
 * Timeline(items = events, key = { it.id }) { event, position ->
 *     BasicText(event.title)
 * }
 * ```
 *
 * @param items Entries to render, in order.
 * @param key Optional stable key per item, used to preserve row state across
 *   list mutations.
 * @param content Row body for an item; [TimelinePosition] reports whether it
 *   is first ([TimelinePosition.Start] — a lone item also reports Start),
 *   last ([TimelinePosition.End]), or in between.
 */
@Composable
fun <T> Timeline(
    items: List<T>,
    modifier: Modifier = Modifier,
    key: ((T) -> Any)? = null,
    content: @Composable ColumnScope.(T, TimelinePosition) -> Unit,
) {
    val colors = Compose7ThemeColors
    val lastIndex = items.lastIndex

    Column(modifier = modifier) {
        items.forEachIndexed { index, item ->
            val itemKey = key?.invoke(item) ?: index
            val position = when {
                index == 0 -> TimelinePosition.Start
                index == lastIndex -> TimelinePosition.End
                else -> TimelinePosition.Middle
            }
            key(itemKey) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .drawBehind {
                            val railX =
                                if (layoutDirection == LayoutDirection.Ltr) {
                                    TimelineRailCenter.toPx()
                                } else {
                                    size.width - TimelineRailCenter.toPx()
                                }
                            val nodeY = TimelineNodeTop.toPx()
                            val stroke = TimelineRailThickness.toPx()
                            if (index > 0) {
                                drawLine(
                                    color = colors.outlineVariant,
                                    start = Offset(railX, 0f),
                                    end = Offset(railX, nodeY),
                                    strokeWidth = stroke,
                                    cap = StrokeCap.Round,
                                )
                            }
                            if (index < lastIndex) {
                                drawLine(
                                    color = colors.outlineVariant,
                                    start = Offset(railX, nodeY),
                                    end = Offset(railX, size.height),
                                    strokeWidth = stroke,
                                    cap = StrokeCap.Round,
                                )
                            }
                            drawCircle(
                                color = colors.primary,
                                radius = TimelineNodeRadius.toPx(),
                                center = Offset(railX, nodeY),
                            )
                        }
                        .padding(
                            start = TimelineContentInset,
                            top = 10.dp,
                            bottom = 10.dp,
                        ),
                ) {
                    content(item, position)
                }
            }
        }
    }
}

/** Distance from the row's start edge to the rail centre. */
private val TimelineRailCenter = 12.dp

/** Node radius — 10.dp filled dots, token-colored. */
private val TimelineNodeRadius = 5.dp

/** Rail thickness. */
private val TimelineRailThickness = 2.dp

/** Content start padding — clears the rail and its node. */
private val TimelineContentInset = 28.dp

/** Node centre measured from the row top, aligned with a typical first line. */
private val TimelineNodeTop = 20.dp

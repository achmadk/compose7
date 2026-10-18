package io.github.compose7.components.lists

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key as runtimeKey
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import io.github.compose7.core.theme.Compose7MotionTokens
import io.github.compose7.core.theme.Compose7ThemeShapes
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

/**
 * Drag-reorderable list (PRD §7, P1 / Appendix A "Sortable").
 *
 * A non-lazy `Column` where each item can be long-pressed and dragged to a
 * new position: items swap as the dragged row crosses a neighbor's midpoint,
 * the dragged row lifts with a token-shaped shadow while moving, and settles
 * back with standard motion on drop. Reordering is hoisted — [items] never
 * change inside the component; swaps are reported through
 * [onItemsReordered] and the caller supplies the next [items] value
 * (PRD §11). A local swap is applied immediately so the drag stays coherent
 * even before the hoisted value lands.
 *
 * ```kotlin
 * var tasks by remember { mutableStateOf(taskList) }
 * SortableList(
 *     items = tasks,
 *     onItemsReordered = { tasks = it },
 *     key = { it.id },
 * ) { task, isDragging -> TaskRow(task, elevated = isDragging) }
 * ```
 *
 * @param key Stable identity per item; falls back to list position.
 * @param itemContent Row content; [isDragging] is true for the row currently
 *   being dragged so callers can add elevation/highlight.
 */
@Composable
fun <T> SortableList(
    items: List<T>,
    onItemsReordered: (List<T>) -> Unit,
    modifier: Modifier = Modifier,
    key: ((T) -> Any)? = null,
    itemContent: @Composable (item: T, isDragging: Boolean) -> Unit,
) {
    val motion = Compose7MotionTokens
    val shapes = Compose7ThemeShapes
    val density = LocalDensity.current
    val dragElevationPx = remember(density) { with(density) { 6.dp.toPx() } }
    val scope = rememberCoroutineScope()
    val offsetY = remember { Animatable(0f) }
    val dragY = remember { mutableFloatStateOf(0f) }
    val heights = remember { mutableStateMapOf<Int, Int>() }

    var localItems by remember(items) { mutableStateOf(items) }
    var draggingIndex by remember { mutableStateOf(-1) }

    // pointerInput does not restart on recomposition — read callbacks fresh.
    val reorder = rememberUpdatedState(onItemsReordered)

    Column(modifier = modifier) {
        localItems.forEachIndexed { index, item ->
            val isDragging = index == draggingIndex
            runtimeKey(key?.invoke(item) ?: index) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .zIndex(if (isDragging) 1f else 0f)
                        .onSizeChanged { heights[index] = it.height }
                        .offset { IntOffset(0, if (isDragging) offsetY.value.roundToInt() else 0) }
                        .graphicsLayer {
                            if (isDragging) {
                                shadowElevation = dragElevationPx
                                shape = shapes.card
                                clip = true
                            }
                        }
                        .pointerInput(item) {
                            detectDragGesturesAfterLongPress(
                                onDragStart = {
                                    val start = localItems.indexOfFirst { it === item }
                                    if (start >= 0) {
                                        draggingIndex = start
                                        dragY.floatValue = 0f
                                        scope.launch { offsetY.snapTo(0f) }
                                    }
                                },
                                onDrag = { change, amount ->
                                    change.consume()
                                    val i = draggingIndex
                                    if (i >= 0) {
                                        dragY.floatValue += amount.y
                                        if (dragY.floatValue < 0f && i > 0) {
                                            val neighbor = heights[i - 1] ?: 0
                                            if (neighbor > 0 && -dragY.floatValue > neighbor / 2f) {
                                                val moved = localItems.toMutableList().apply {
                                                    add(i - 1, removeAt(i))
                                                }
                                                localItems = moved
                                                reorder.value(moved)
                                                draggingIndex = i - 1
                                                dragY.floatValue += neighbor
                                            }
                                        } else if (dragY.floatValue > 0f && i < localItems.size - 1) {
                                            val neighbor = heights[i + 1] ?: 0
                                            if (neighbor > 0 && dragY.floatValue > neighbor / 2f) {
                                                val moved = localItems.toMutableList().apply {
                                                    add(i + 1, removeAt(i))
                                                }
                                                localItems = moved
                                                reorder.value(moved)
                                                draggingIndex = i + 1
                                                dragY.floatValue -= neighbor
                                            }
                                        }
                                        scope.launch { offsetY.snapTo(dragY.floatValue) }
                                    }
                                },
                                onDragEnd = {
                                    scope.launch {
                                        offsetY.animateTo(
                                            0f,
                                            tween(motion.fastDurationMs, easing = motion.standardEasing),
                                        )
                                        draggingIndex = -1
                                        dragY.floatValue = 0f
                                    }
                                },
                                onDragCancel = {
                                    draggingIndex = -1
                                    dragY.floatValue = 0f
                                    scope.launch { offsetY.snapTo(0f) }
                                },
                            )
                        },
                ) {
                    itemContent(item, isDragging)
                }
            }
        }
    }
}

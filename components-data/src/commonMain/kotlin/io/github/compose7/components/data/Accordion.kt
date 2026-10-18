package io.github.compose7.components.data

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import io.github.compose7.core.haptics.Compose7Haptic
import io.github.compose7.core.theme.Compose7MotionTokens
import io.github.compose7.core.theme.Compose7StyleInfo
import io.github.compose7.core.theme.Compose7ThemeColors
import io.github.compose7.core.theme.Compose7ThemeTypography
import io.github.compose7.foundation.Chevron
import io.github.compose7.foundation.Divider
import io.github.compose7.foundation.compose7Clickable

/** How many rows an [Accordion] may open at once (PRD §7: Data display, P1). */
enum class ExpandMode {
    /** Only one row open — opening a row closes the previously open one (default). */
    Single,

    /** Rows accumulate; any number of rows may be open. */
    Multiple,
}

/**
 * Accordion (PRD §7 "Data display" P1 / Appendix A "Accordion").
 *
 * A stack of tappable rows; each reveals its [content] with an animated
 * expand/collapse and a drawn [Chevron] that rotates 180° while open. Rows are
 * separated by platform-correct [io.github.compose7.foundation.Divider]s.
 *
 * State is controlled by default via [expandedIndices] +
 * [onExpandedIndicesChange]; pass a null [onExpandedIndicesChange] to let the
 * accordion own its state (remembered with `rememberSaveable`, PRD §11).
 *
 * ```kotlin
 * Accordion(
 *     items = sections,
 *     title = { it.header },
 * ) { section -> BasicText(section.body) }
 * ```
 *
 * @param items Rows to render, in order.
 * @param expandedIndices Indices of the open rows; with [ExpandMode.Single]
 *   this holds at most one index.
 * @param onExpandedIndicesChange Receives the next open-index set; null makes
 *   the component uncontrolled (internal remembered state).
 * @param expandMode Single (default) or [ExpandMode.Multiple] open rows.
 * @param key Optional stable key per item, used to preserve row state across
 *   list mutations.
 * @param title Row header text for an item.
 * @param content Revealed body for an item, laid out in a `ColumnScope`.
 */
@Composable
fun <T> Accordion(
    items: List<T>,
    modifier: Modifier = Modifier,
    expandedIndices: Set<Int> = emptySet(),
    onExpandedIndicesChange: ((Set<Int>) -> Unit)? = null,
    expandMode: ExpandMode = ExpandMode.Single,
    key: ((T) -> Any)? = null,
    title: (T) -> String,
    content: @Composable ColumnScope.(T) -> Unit,
) {
    val savedIndices = rememberSaveable(stateSaver = IntSetSaver) { mutableStateOf(expandedIndices) }
    val open = if (onExpandedIndicesChange != null) expandedIndices else savedIndices.value
    val motion = Compose7MotionTokens
    val colors = Compose7ThemeColors
    val type = Compose7ThemeTypography
    val insets = Compose7StyleInfo

    fun toggle(index: Int) {
        val next = toggledIndices(open, index, expandMode)
        if (onExpandedIndicesChange != null) {
            onExpandedIndicesChange(next)
        } else {
            savedIndices.value = next
        }
    }

    Column(modifier = modifier) {
        items.forEachIndexed { index, item ->
            val itemKey = key?.invoke(item) ?: index
            val isOpen = index in open
            key(itemKey) {
                val interaction = remember { MutableInteractionSource() }
                val rotation by animateFloatAsState(
                    targetValue = if (isOpen) 180f else 0f,
                    animationSpec = tween(motion.mediumDurationMs, easing = motion.standardEasing),
                    label = "accordionChevron",
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .compose7Clickable(
                            interactionSource = interaction,
                            role = Role.Button,
                            haptic = Compose7Haptic.Selection,
                            onClick = { toggle(index) },
                        )
                        .padding(
                            start = insets.listContentInset,
                            end = insets.listContentInset,
                            top = 14.dp,
                            bottom = 14.dp,
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    BasicText(
                        text = title(item),
                        modifier = Modifier.weight(1f),
                        style = type.titleMedium.copy(color = colors.onSurface),
                    )
                    Chevron(
                        color = colors.onSurfaceVariant,
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .rotate(rotation),
                    )
                }
                AnimatedVisibility(
                    visible = isOpen,
                    enter = expandVertically(tween(motion.mediumDurationMs, easing = motion.emphasizedEasing)),
                    exit = shrinkVertically(tween(motion.mediumDurationMs, easing = motion.emphasizedEasing)),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = insets.listContentInset,
                                end = insets.listContentInset,
                                bottom = 14.dp,
                            ),
                        content = { content(item) },
                    )
                }
                if (index != items.lastIndex) {
                    Divider()
                }
            }
        }
    }
}

/** Next open-index set for a row toggle under [mode] — the accordion state reducer. */
internal fun toggledIndices(current: Set<Int>, index: Int, mode: ExpandMode): Set<Int> =
    when (mode) {
        ExpandMode.Single -> if (index in current) emptySet() else setOf(index)
        ExpandMode.Multiple -> if (index in current) current - index else current + index
    }

/** [Set]<[Int]> round-trips through a `String` so it survives `rememberSaveable` on every platform. */
private val IntSetSaver: Saver<Set<Int>, String> = Saver(
    save = { open -> open.sorted().joinToString(",") },
    restore = { text ->
        if (text.isEmpty()) emptySet() else text.split(',').mapNotNull { it.toIntOrNull() }.toSet()
    },
)

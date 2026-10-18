package io.github.compose7.components.forms

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupPositionProvider
import io.github.compose7.components.overlays.Popover
import io.github.compose7.core.theme.Compose7ThemeColors
import io.github.compose7.core.theme.Compose7ThemeTypography
import io.github.compose7.foundation.compose7Clickable
import kotlin.math.roundToInt

/**
 * Text field with a filtered suggestion dropdown (PRD §7 P1 / Appendix A
 * "Autocomplete", §8.5). Suggestions appear in an anchored [Popover] once the
 * query reaches [minQueryLength]; picking one fills the field (via
 * [onValueChange]) and notifies [onSuggestionSelected].
 *
 * ```kotlin
 * Autocomplete(
 *     value = query,
 *     onValueChange = { query = it },
 *     options = cities,
 *     onSuggestionSelected = { query = it },
 *     label = "City",
 * )
 * ```
 */
@Composable
fun Autocomplete(
    value: String,
    onValueChange: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier,
    options: List<String> = emptyList(),
    onSuggestionSelected: (String) -> Unit = {},
    minQueryLength: Int = 1,
    suggestionLimit: Int = 8,
    enabled: Boolean = true,
    isError: Boolean = false,
    label: String? = null,
    placeholder: String? = null,
    supportingText: (@Composable () -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
) {
    val colors = Compose7ThemeColors
    val type = Compose7ThemeTypography
    val density = LocalDensity.current

    val state = rememberSaveable { mutableStateOf(value) }
    val current = if (onValueChange != null) value else state.value

    var focused by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    var anchorBounds by remember { mutableStateOf(IntRect.Zero) }

    val suggestions = remember(current, options, minQueryLength, suggestionLimit) {
        filterSuggestions(current, options, minQueryLength, suggestionLimit)
    }
    val dropdownVisible = expanded && focused && suggestions.isNotEmpty()
    val positionProvider = remember(anchorBounds) { BelowAnchorPositionProvider(anchorBounds) }

    Box(
        modifier = modifier
            .onFocusChanged { focused = it.isFocused }
            .onGloballyPositioned { coordinates ->
                val windowPosition = coordinates.positionInWindow()
                anchorBounds = IntRect(
                    offset = IntOffset(windowPosition.x.roundToInt(), windowPosition.y.roundToInt()),
                    size = coordinates.size,
                )
            },
    ) {
        TextField(
            value = current,
            onValueChange = {
                if (onValueChange == null) state.value = it else onValueChange(it)
                expanded = true
            },
            enabled = enabled,
            isError = isError,
            label = label,
            placeholder = placeholder,
            supportingText = supportingText,
            keyboardOptions = keyboardOptions,
        )

        Popover(
            visible = dropdownVisible,
            onDismissRequest = { expanded = false },
            positionProvider = positionProvider,
        ) {
            Column(
                modifier = Modifier.widthIn(
                    min = with(density) { anchorBounds.width.toDp() },
                ),
            ) {
                suggestions.forEach { suggestion ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .compose7Clickable(
                                onClick = {
                                    if (onValueChange == null) {
                                        state.value = suggestion
                                    } else {
                                        onValueChange(suggestion)
                                    }
                                    onSuggestionSelected(suggestion)
                                    expanded = false
                                },
                                role = Role.Button,
                            )
                            .padding(vertical = 8.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        BasicText(text = suggestion, style = type.bodyLarge.copy(color = colors.onSurface))
                    }
                }
            }
        }
    }
}

/**
 * Suggestions for [Autocomplete]: nothing below [minQueryLength], otherwise
 * case-insensitive substring matches in source order, capped at [limit].
 * Extracted so the filtering rule is unit-tested rather than baked into the
 * composable (PRD §13).
 */
internal fun filterSuggestions(
    query: String,
    options: List<String>,
    minQueryLength: Int = 1,
    limit: Int = 8,
): List<String> {
    if (query.length < minQueryLength || limit <= 0) return emptyList()
    return options.filter { it.contains(query, ignoreCase = true) }.take(limit)
}

/** Places the popup directly under the anchor, flipping above when out of room. */
private class BelowAnchorPositionProvider(
    private val anchor: IntRect,
) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset {
        val below = anchor.top + anchor.height
        val y = if (below + popupContentSize.height <= windowSize.height) {
            below
        } else {
            (anchor.top - popupContentSize.height).coerceAtLeast(0)
        }
        val x = if (layoutDirection == LayoutDirection.Ltr) {
            anchor.left
        } else {
            (windowSize.width - anchor.right - popupContentSize.width).coerceAtLeast(0)
        }
        return IntOffset(x, y)
    }
}

package io.github.compose7.components.forms

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import io.github.compose7.core.theme.Compose7StyleInfo
import io.github.compose7.core.theme.Compose7ThemeColors
import io.github.compose7.core.theme.Compose7ThemeTypography
import io.github.compose7.foundation.compose7Clickable
import io.github.compose7.foundation.compose7MinimumTouchTarget
import io.github.compose7.foundation.compose7Toggleable
import io.github.compose7.foundation.currentMinTouchTarget

/** One choice in a [RadioGroup] (PRD §11: data-shaped where the API is list-driven). */
data class RadioOption(
    val value: String,
    val label: String,
    val enabled: Boolean = true,
)

/**
 * Radio selection control (PRD §7 P0 / Appendix A "Radio", PRD §8.5) — drawn
 * ring + dot, no icon assets (PRD §4). Toggling an already-selected radio is
 * a no-op, per platform radio semantics; role is [Role.RadioButton].
 *
 * Use [RadioGroup] for the common label-row case; this control exists for
 * custom layouts.
 */
@Composable
fun Radio(
    selected: Boolean,
    onSelectedChange: ((Boolean) -> Unit)? = null,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = Compose7ThemeColors

    val state = rememberSaveable { mutableStateOf(false) }
    val current = if (onSelectedChange != null) selected else state.value

    val ringColor = when {
        current -> colors.primary
        enabled -> colors.outline
        else -> colors.outline.copy(alpha = 0.38f)
    }

    Box(
        modifier = modifier
            .let {
                if (onSelectedChange != null) {
                    it.compose7Toggleable(
                        value = current,
                        onValueChange = { if (!current) onSelectedChange(true) },
                        enabled = enabled,
                        role = Role.RadioButton,
                    )
                } else {
                    it
                }
            }
            .compose7MinimumTouchTarget(),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .border(BorderStroke(2.dp, ringColor), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            if (current) {
                Box(
                    Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(colors.primary),
                )
            }
        }
    }
}

/**
 * Group of radio rows with mutually exclusive selection (PRD §8.5, §12: rows
 * carry `selected` semantics and [Role.RadioButton]).
 *
 * ```kotlin
 * RadioGroup(
 *     options = listOf(RadioOption("a", "Option A"), RadioOption("b", "Option B")),
 *     selectedValue = choice,
 *     onSelectedChange = { choice = it },
 * )
 * ```
 */
@Composable
fun RadioGroup(
    options: List<RadioOption>,
    selectedValue: String?,
    onSelectedChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = Compose7ThemeColors
    val type = Compose7ThemeTypography

    Column(modifier = modifier) {
        options.forEach { option ->
            val rowEnabled = enabled && option.enabled
            val isSelected = option.value == selectedValue
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = currentMinTouchTarget())
                    .semantics { selected = isSelected }
                    .let {
                        if (rowEnabled) {
                            it.compose7Clickable(
                                onClick = { onSelectedChange(option.value) },
                                role = Role.RadioButton,
                            )
                        } else {
                            it
                        }
                    }
                    .padding(horizontal = Compose7StyleInfo.listContentInset, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Radio(selected = isSelected, enabled = rowEnabled)
                Spacer(Modifier.width(12.dp))
                BasicText(
                    text = option.label,
                    style = type.bodyLarge.copy(
                        color = if (rowEnabled) colors.onSurface else colors.onSurface.copy(alpha = 0.38f),
                    ),
                )
            }
        }
    }
}

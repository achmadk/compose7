package io.github.compose7.components.forms

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import io.github.compose7.components.overlays.SheetModal
import io.github.compose7.core.theme.Compose7StyleInfo
import io.github.compose7.core.theme.Compose7ThemeColors
import io.github.compose7.core.theme.Compose7ThemeTypography
import io.github.compose7.foundation.Check
import io.github.compose7.foundation.Chevron
import io.github.compose7.foundation.compose7Clickable
import io.github.compose7.foundation.currentMinTouchTarget

/** One selectable choice for [SmartSelect] (PRD §11: list-driven APIs are data classes). */
data class SmartSelectOption(
    val value: String,
    val label: String,
    val subtitle: String? = null,
    val enabled: Boolean = true,
)

/**
 * Selectable row that opens an option list in a [SheetModal] (PRD §7 P1 /
 * Appendix A "Smart Select", §8.5). The trigger shows [label] + the selected
 * option (or [placeholder]) with a drawn chevron; picking a row commits the
 * option's value and closes the sheet.
 *
 * ```kotlin
 * SmartSelect(
 *     value = fruit,
 *     options = listOf(SmartSelectOption("apple", "Apple"), SmartSelectOption("pear", "Pear")),
 *     onValueChange = { fruit = it },
 *     label = "Fruit",
 * )
 * ```
 */
@Composable
fun SmartSelect(
    value: String?,
    options: List<SmartSelectOption>,
    onValueChange: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    enabled: Boolean = true,
    sheetTitle: String? = null,
) {
    val colors = Compose7ThemeColors
    val type = Compose7ThemeTypography

    val state = rememberSaveable { mutableStateOf(value) }
    val current = if (onValueChange != null) value else state.value
    var expanded by remember { mutableStateOf(false) }

    val selectedOption = options.firstOrNull { it.value == current }

    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = currentMinTouchTarget())
                .compose7Clickable(
                    enabled = enabled,
                    onClick = { expanded = true },
                    role = Role.Button,
                )
                .padding(horizontal = Compose7StyleInfo.listContentInset, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                if (label != null) {
                    BasicText(
                        text = label,
                        style = type.labelMedium.copy(color = colors.onSurfaceVariant),
                    )
                }
                BasicText(
                    text = selectedOption?.label ?: placeholder.orEmpty(),
                    style = type.bodyLarge.copy(
                        color = if (selectedOption != null) colors.onSurface else colors.onSurfaceVariant,
                    ),
                )
            }
            Chevron(color = colors.onSurfaceVariant)
        }

        SheetModal(
            visible = expanded,
            onDismissRequest = { expanded = false },
            title = sheetTitle,
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                options.forEachIndexed { index, option ->
                    if (index > 0) {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(colors.outlineVariant),
                        )
                    }
                    val optionSelected = option.value == current
                    val optionEnabled = enabled && option.enabled
                    val subtitle = option.subtitle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = currentMinTouchTarget())
                            .semantics { selected = optionSelected }
                            .let {
                                if (optionEnabled) {
                                    it.compose7Clickable(
                                        onClick = {
                                            if (onValueChange == null) {
                                                state.value = option.value
                                            } else {
                                                onValueChange(option.value)
                                            }
                                            expanded = false
                                        },
                                        role = Role.Button,
                                    )
                                } else {
                                    it
                                }
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f)) {
                            BasicText(
                                text = option.label,
                                style = type.bodyLarge.copy(
                                    color = if (optionEnabled) {
                                        colors.onSurface
                                    } else {
                                        colors.onSurface.copy(alpha = 0.38f)
                                    },
                                ),
                            )
                            if (subtitle != null) {
                                BasicText(
                                    text = subtitle,
                                    style = type.bodySmall.copy(color = colors.onSurfaceVariant),
                                )
                            }
                        }
                        if (optionSelected) {
                            Check(color = colors.primary)
                        }
                    }
                }
            }
        }
    }
}

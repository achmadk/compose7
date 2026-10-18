package io.github.compose7.components.forms

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.compose7.components.overlays.Dialog
import io.github.compose7.components.overlays.DialogAction
import io.github.compose7.components.overlays.DialogActionRole
import io.github.compose7.components.overlays.SheetModal
import io.github.compose7.core.theme.Compose7Style
import io.github.compose7.core.theme.Compose7StyleInfo
import io.github.compose7.core.theme.Compose7ThemeColors
import io.github.compose7.core.theme.Compose7ThemeTypography
import io.github.compose7.core.theme.currentCompose7Style
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
 * Selectable row that opens an option list (PRD §7 P1 / Appendix A "Smart
 * Select", §8.5). The trigger shows [label] + the selected option (or
 * [placeholder]) with a drawn chevron.
 *
 * Style split (PRD §8.5, "no silent divergence" in §9): Material opens a list
 * dialog; Cupertino opens a wheel-picker sheet with a Done row; DesktopUI7
 * keeps the list in a sheet. On the wheel, disabled options are omitted (a
 * wheel cannot represent holes); the list keeps them visible but disabled.
 *
 * ```kotlin
 * SmartSelect(
 *     value = fruit,
 *     options = listOf(SmartSelectOption("apple", "Apple"), SmartSelectOption("pear", "Pear")),
 *     onValueChange = { fruit = it },
 *     label = "Fruit",
 * )
 * ```
 *
 * @param doneLabel Commit label for the Cupertino wheel.
 * @param cancelLabel Dismiss label for the Material list dialog.
 * @param isError Renders the chevron/validation slot in the error color.
 * @param supportingText Validation message slot.
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
    doneLabel: String = "Done",
    cancelLabel: String = "Cancel",
    isError: Boolean = false,
    supportingText: (@Composable () -> Unit)? = null,
) {
    val colors = Compose7ThemeColors
    val type = Compose7ThemeTypography
    val style = currentCompose7Style

    val state = rememberSaveable { mutableStateOf(value) }
    val current = if (onValueChange != null) value else state.value
    var expanded by remember { mutableStateOf(false) }

    val selectedOption = options.firstOrNull { it.value == current }
    val commit: (String) -> Unit = { picked ->
        if (onValueChange == null) state.value = picked else onValueChange(picked)
    }

    Column(modifier = modifier) {
        Box {
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
                            style = type.labelMedium.copy(
                                color = if (isError) colors.error else colors.onSurfaceVariant,
                            ),
                        )
                    }
                    BasicText(
                        text = selectedOption?.label ?: placeholder.orEmpty(),
                        style = type.bodyLarge.copy(
                            color = if (selectedOption != null) colors.onSurface else colors.onSurfaceVariant,
                        ),
                    )
                }
                Chevron(color = if (isError) colors.error else colors.onSurfaceVariant)
            }

            when {
                style == Compose7Style.Cupertino -> CupertinoWheelSheet(
                    visible = expanded,
                    title = sheetTitle,
                    options = options,
                    currentValue = current,
                    doneLabel = doneLabel,
                    onCommit = { commit(it); expanded = false },
                    onDismissRequest = { expanded = false },
                )
                style == Compose7Style.Material -> MaterialListDialog(
                    visible = expanded,
                    title = sheetTitle,
                    options = options,
                    currentValue = current,
                    enabled = enabled,
                    cancelLabel = cancelLabel,
                    onCommit = { commit(it); expanded = false },
                    onDismissRequest = { expanded = false },
                )
                else -> ListSheet(
                    visible = expanded,
                    title = sheetTitle,
                    options = options,
                    currentValue = current,
                    enabled = enabled,
                    onCommit = { commit(it); expanded = false },
                    onDismissRequest = { expanded = false },
                )
            }
        }

        FormFieldSupportingText(isError = isError, supportingText = supportingText)
    }
}

/** Cupertino presentation: wheel + commit row (PRD §8.5). */
@Composable
private fun CupertinoWheelSheet(
    visible: Boolean,
    title: String?,
    options: List<SmartSelectOption>,
    currentValue: String?,
    doneLabel: String,
    onCommit: (String) -> Unit,
    onDismissRequest: () -> Unit,
) {
    val colors = Compose7ThemeColors
    val type = Compose7ThemeTypography
    val wheelOptions = options.filter { it.enabled }
    if (wheelOptions.isEmpty()) return

    SheetModal(
        visible = visible,
        onDismissRequest = onDismissRequest,
        title = title,
    ) {
        val initialIndex = wheelOptions.indexOfFirst { it.value == currentValue }.coerceAtLeast(0)
        var pendingIndex by remember { mutableStateOf(initialIndex) }

        WheelPicker(
            options = wheelOptions.map { it.label },
            selectedIndex = pendingIndex,
            onSelectedIndexChange = { pendingIndex = it },
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(colors.outlineVariant),
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = currentMinTouchTarget())
                .compose7Clickable(
                    role = Role.Button,
                    onClick = { onCommit(wheelOptions[pendingIndex.coerceIn(wheelOptions.indices)].value) },
                ),
            contentAlignment = Alignment.Center,
        ) {
            BasicText(
                text = doneLabel,
                style = type.labelLarge.copy(
                    color = colors.primary,
                    fontWeight = FontWeight.SemiBold,
                ),
            )
        }
    }
}

/** Material presentation: a list dialog (PRD §8.5). */
@Composable
private fun MaterialListDialog(
    visible: Boolean,
    title: String?,
    options: List<SmartSelectOption>,
    currentValue: String?,
    enabled: Boolean,
    cancelLabel: String,
    onCommit: (String) -> Unit,
    onDismissRequest: () -> Unit,
) {
    if (!visible) return

    Dialog(
        onDismissRequest = onDismissRequest,
        title = title,
        actions = listOf(DialogAction(cancelLabel, onDismissRequest, DialogActionRole.Cancel)),
        content = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 360.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                OptionRows(options, currentValue, enabled, onCommit)
            }
        },
    )
}

/** DesktopUI7 presentation: the list in a sheet. */
@Composable
private fun ListSheet(
    visible: Boolean,
    title: String?,
    options: List<SmartSelectOption>,
    currentValue: String?,
    enabled: Boolean,
    onCommit: (String) -> Unit,
    onDismissRequest: () -> Unit,
) {
    SheetModal(
        visible = visible,
        onDismissRequest = onDismissRequest,
        title = title,
    ) {
        OptionRows(options, currentValue, enabled, onCommit)
    }
}

@Composable
private fun OptionRows(
    options: List<SmartSelectOption>,
    currentValue: String?,
    enabled: Boolean,
    onCommit: (String) -> Unit,
) {
    val colors = Compose7ThemeColors
    val type = Compose7ThemeTypography

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
            val optionSelected = option.value == currentValue
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
                                onClick = { onCommit(option.value) },
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

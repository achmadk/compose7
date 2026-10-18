package io.github.compose7.components.forms

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

/**
 * Single-line text input (PRD §7 P0 / Appendix A "Inputs / Form Inputs",
 * PRD §8.5).
 *
 * Controlled by default (`value` + [onValueChange]); pass a null callback for
 * the uncontrolled convenience form, which remembers the text internally.
 * Validation messages go in [supportingText] with `isError = true` — the slot
 * is tinted with the error token automatically.
 *
 * ```kotlin
 * TextField(value = name, onValueChange = { name = it }, label = "Name")
 * TextField(value = "", placeholder = "Search…") // uncontrolled
 * ```
 */
@Composable
fun TextField(
    value: String,
    onValueChange: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    isError: Boolean = false,
    label: String? = null,
    placeholder: String? = null,
    leading: (@Composable RowScope.() -> Unit)? = null,
    trailing: (@Composable RowScope.() -> Unit)? = null,
    supportingText: (@Composable () -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    val state = rememberSaveable { mutableStateOf(value) }
    val current = if (onValueChange != null) value else state.value

    FormsTextField(
        value = current,
        onValueChange = { if (onValueChange == null) state.value = it else onValueChange(it) },
        modifier = modifier,
        enabled = enabled,
        readOnly = readOnly,
        isError = isError,
        label = label,
        placeholder = placeholder,
        leading = leading,
        trailing = trailing,
        supportingText = supportingText,
        singleLine = true,
        minLines = 1,
        maxLines = 1,
        contentMinHeight = 28.dp,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        visualTransformation = visualTransformation,
        interactionSource = interactionSource,
    )
}

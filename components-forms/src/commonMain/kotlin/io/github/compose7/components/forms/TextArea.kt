package io.github.compose7.components.forms

import androidx.compose.foundation.interaction.MutableInteractionSource
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
 * Multiline text input (PRD §7 P0 "Text input" variant, PRD §8.5) — shares
 * [FormsTextField] with [TextField]: same label/placeholder/error/supporting
 * behavior, [minLines]–[maxLines] growth and top-aligned content.
 *
 * ```kotlin
 * TextArea(value = notes, onValueChange = { notes = it }, label = "Notes")
 * ```
 */
@Composable
fun TextArea(
    value: String,
    onValueChange: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    isError: Boolean = false,
    label: String? = null,
    placeholder: String? = null,
    supportingText: (@Composable () -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    minLines: Int = 3,
    maxLines: Int = 8,
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
        leading = null,
        trailing = null,
        supportingText = supportingText,
        singleLine = false,
        minLines = minLines,
        maxLines = maxLines,
        contentMinHeight = 96.dp,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        visualTransformation = visualTransformation,
        interactionSource = interactionSource,
    )
}

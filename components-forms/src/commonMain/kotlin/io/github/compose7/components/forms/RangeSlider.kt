package io.github.compose7.components.forms

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

/**
 * Two-thumb range slider (PRD §7 P0 / Appendix A "Range Slider", §8.5) —
 * built on the same custom-drawn [SliderImpl] as [Slider]: no Material
 * dependency, drag either thumb (the nearer one to the press wins), optional
 * discrete [steps]. Controlled by default; a null [onValueChange] keeps the
 * range internally.
 *
 * ```kotlin
 * RangeSlider(value = priceRange, onValueChange = { priceRange = it }, valueRange = 0f..100f)
 * ```
 */
@Composable
fun RangeSlider(
    value: ClosedFloatingPointRange<Float>,
    onValueChange: ((ClosedFloatingPointRange<Float>) -> Unit)? = null,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
    onValueChangeFinished: (() -> Unit)? = null,
    contentDescription: String? = null,
    isError: Boolean = false,
    supportingText: (@Composable () -> Unit)? = null,
) {
    val internalValue = remember { mutableStateOf(value) }
    LaunchedEffect(value) { if (onValueChange == null) internalValue.value = value }
    val current = if (onValueChange == null) internalValue.value else value

    Column(modifier = modifier) {
        SliderImpl(
            values = listOf(current.start, current.endInclusive),
            onValuesChange = { list ->
                val newRange = list[0]..list[1]
                if (onValueChange == null) internalValue.value = newRange else onValueChange(newRange)
            },
            onValuesChangeFinished = onValueChangeFinished,
            enabled = enabled,
            valueRange = valueRange,
            steps = steps,
            contentDescription = contentDescription,
            isError = isError,
            modifier = Modifier.fillMaxWidth(),
        )
        FormFieldSupportingText(isError = isError, supportingText = supportingText)
    }
}

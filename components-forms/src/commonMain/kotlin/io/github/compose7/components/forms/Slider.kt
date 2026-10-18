package io.github.compose7.components.forms

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import io.github.compose7.core.theme.Compose7MotionTokens
import io.github.compose7.core.theme.Compose7Style
import io.github.compose7.core.theme.Compose7ThemeColors
import io.github.compose7.core.theme.currentCompose7Style
import io.github.compose7.foundation.compose7MinimumTouchTarget
import io.github.compose7.foundation.currentMinTouchTarget
import kotlin.math.abs

/**
 * Single-value slider (PRD §7 P0 / Appendix A "Range Slider" family, §8.5) —
 * custom-drawn track and thumb (PRD §4: no Material dependency, no icon
 * assets), draggable via horizontal drag gestures, tap-to-jump, optional
 * discrete [steps]. Controlled by default; a null [onValueChange] keeps the
 * value internally.
 *
 * ```kotlin
 * Slider(value = volume, onValueChange = { volume = it }, steps = 4)
 * ```
 */
@Composable
fun Slider(
    value: Float,
    onValueChange: ((Float) -> Unit)? = null,
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
            values = listOf(current),
            onValuesChange = { list ->
                if (onValueChange == null) internalValue.value = list.first() else onValueChange(list.first())
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

@Composable
internal fun SliderImpl(
    values: List<Float>,
    onValuesChange: (List<Float>) -> Unit,
    onValuesChangeFinished: (() -> Unit)?,
    enabled: Boolean,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    contentDescription: String?,
    modifier: Modifier,
    isError: Boolean = false,
) {
    val style = currentCompose7Style
    val colors = Compose7ThemeColors
    val motion = Compose7MotionTokens

    val latestValues by rememberUpdatedState(values)
    val latestOnValuesChange by rememberUpdatedState(onValuesChange)
    val latestOnValuesChangeFinished by rememberUpdatedState(onValuesChangeFinished)

    var widthPx by remember { mutableFloatStateOf(1f) }
    var draggingIndex by remember { mutableIntStateOf(-1) }
    val thumbScale by animateFloatAsState(
        targetValue = if (draggingIndex >= 0) 1.15f else 1f,
        animationSpec = tween(motion.fastDurationMs, easing = motion.standardEasing),
        label = "sliderThumb",
    )

    val span = (valueRange.endInclusive - valueRange.start).takeIf { it != 0f } ?: 1f
    val thumbRadius = when (style) {
        Compose7Style.Cupertino -> 12.dp
        Compose7Style.DesktopUI7 -> 8.dp
        else -> 10.dp
    }
    val trackHeight = 4.dp
    val density = LocalDensity.current
    val thumbPx = with(density) { thumbRadius.toPx() }
    val trackHeightPx = with(density) { trackHeight.toPx() }

    fun usablePx(): Float = (widthPx - 2 * thumbPx).coerceAtLeast(1f)
    fun fractionOf(x: Float): Float = ((x - thumbPx) / usablePx()).coerceIn(0f, 1f)
    fun valueAt(x: Float): Float = quantize(valueRange.start + fractionOf(x) * span, valueRange, steps)
    fun nearestIndex(v: Float): Int {
        var best = 0
        for (i in latestValues.indices) {
            if (abs(latestValues[i] - v) < abs(latestValues[best] - v)) best = i
        }
        return best
    }
    fun updateWith(index: Int, v: Float): List<Float> =
        if (latestValues.size < 2) {
            listOf(v)
        } else if (index == 0) {
            listOf(v.coerceAtMost(latestValues[1]), latestValues[1])
        } else {
            listOf(latestValues[0], v.coerceAtLeast(latestValues[0]))
        }

    val semanticsModifier = Modifier.semantics {
        progressBarRangeInfo = ProgressBarRangeInfo(values.first(), valueRange, steps)
        if (contentDescription != null) this.contentDescription = contentDescription
    }

    Box(
        modifier = modifier
            .height(currentMinTouchTarget())
            .compose7MinimumTouchTarget()
            .onSizeChanged { widthPx = it.width.toFloat() }
            .then(semanticsModifier)
            .pointerInput(enabled, valueRange, steps, widthPx) {
                if (enabled) {
                    detectTapGestures { position ->
                        latestOnValuesChange(listOf(valueAt(position.x)))
                        latestOnValuesChangeFinished?.invoke()
                    }
                }
            }
            .pointerInput(enabled, valueRange, steps, widthPx) {
                if (enabled) {
                    detectHorizontalDragGestures(
                        onDragStart = { position ->
                            val v = valueAt(position.x)
                            draggingIndex = nearestIndex(v)
                            latestOnValuesChange(updateWith(draggingIndex, v))
                        },
                        onHorizontalDrag = { change, _ ->
                            val v = valueAt(change.position.x)
                            latestOnValuesChange(updateWith(draggingIndex, v))
                            change.consume()
                        },
                        onDragEnd = {
                            draggingIndex = -1
                            latestOnValuesChangeFinished?.invoke()
                        },
                        onDragCancel = { draggingIndex = -1 },
                    )
                }
            },
        contentAlignment = Alignment.CenterStart,
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val cy = size.height / 2f
            val left = thumbPx
            val usable = usablePx()
            val trackTop = cy - trackHeightPx / 2f
            val trackCornerRadius = CornerRadius(trackHeightPx / 2f)

            drawRoundRect(
                color = colors.outlineVariant,
                topLeft = Offset(left, trackTop),
                size = Size(usable, trackHeightPx),
                cornerRadius = trackCornerRadius,
            )
            val xFor: (Float) -> Float = { v -> left + usable * ((v - valueRange.start) / span) }
            val minV = if (latestValues.size > 1 && latestValues[1] < latestValues[0]) latestValues[1] else latestValues[0]
            val maxV = if (latestValues.size > 1 && latestValues[1] > latestValues[0]) latestValues[1] else latestValues[0]
            val activeLeft = xFor(minV)
            val activeRight = xFor(maxV)
            if (activeRight > activeLeft) {
                drawRoundRect(
                    color = if (isError) colors.error else colors.primary,
                    topLeft = Offset(activeLeft, trackTop),
                    size = Size(activeRight - activeLeft, trackHeightPx),
                    cornerRadius = trackCornerRadius,
                )
            }
            if (steps > 0) {
                for (i in 1..steps) {
                    drawCircle(
                        color = colors.surface,
                        radius = trackHeightPx / 3f,
                        center = Offset(left + usable * i / (steps + 1), cy),
                    )
                }
            }
            for (v in latestValues) {
                val x = xFor(v)
                val r = thumbPx * thumbScale
                when (style) {
                    Compose7Style.Cupertino -> {
                        drawCircle(color = colors.surface, radius = r, center = Offset(x, cy))
                        drawCircle(
                            color = if (isError) colors.error else colors.outline,
                            radius = r,
                            center = Offset(x, cy),
                            style = Stroke(width = 1.dp.toPx()),
                        )
                    }
                    else -> drawCircle(
                        color = if (isError) colors.error else colors.primary,
                        radius = r,
                        center = Offset(x, cy),
                    )
                }
            }
        }
    }
}

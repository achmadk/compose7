package io.github.compose7.components.overlays

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import kotlin.math.roundToInt
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.foundation.text.BasicText
import io.github.compose7.core.theme.Compose7MotionTokens
import io.github.compose7.core.theme.Compose7Style
import io.github.compose7.core.theme.Compose7ThemeColors
import io.github.compose7.core.theme.Compose7ThemeShapes
import io.github.compose7.core.theme.Compose7ThemeTypography
import io.github.compose7.core.theme.currentCompose7Style
import io.github.compose7.foundation.Compose7Surface

/** Where a [Toast] is anchored (PRD §7: toast auto-dismiss + swipe-to-dismiss). */
enum class ToastPosition { Top, Bottom }

/** How long a [Toast] stays on screen before auto-dismissing. */
enum class ToastDuration(val millis: Long) {
    Short(2_500),
    Long(4_000),
}

/**
 * Transient notification toast (PRD §7, P0 / Appendix A "Toast / Notification").
 *
 * Auto-dismisses after [duration], swipe-left/right to dismiss early, and
 * appears/disappears with the active style's motion. Rendered in a window-level
 * popup so it floats above the current screen; position follows [position]
 * with status/navigation-bar insets applied automatically. Compose it
 * conditionally — remove it from composition when [onDismissRequest] fires.
 *
 * ```kotlin
 * var toast by remember { mutableStateOf<String?>(null) }
 * toast?.let { message ->
 *     Toast(message, onDismissRequest = { toast = null })
 * }
 * ```
 *
 * For richer, action-bearing notifications use the components in
 * `components-messaging` (PRD §7 "Notification view").
 */
@Composable
fun Toast(
    text: String,
    modifier: Modifier = Modifier,
    position: ToastPosition = ToastPosition.Bottom,
    duration: ToastDuration = ToastDuration.Short,
    dismissOnSwipe: Boolean = true,
    onDismissRequest: (() -> Unit)? = null,
) {
    val style = currentCompose7Style
    val colors = Compose7ThemeColors
    val type = Compose7ThemeTypography
    val shapes = Compose7ThemeShapes
    val motion = Compose7MotionTokens
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    var visible by remember { mutableStateOf(false) }
    val offsetX = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        visible = true
        delay(duration.millis)
        visible = false
        onDismissRequest?.invoke()
    }

    val slideSpec = tween<IntOffset>(motion.mediumDurationMs, easing = motion.emphasizedEasing)

    Popup(
        alignment = if (position == ToastPosition.Top) Alignment.TopCenter else Alignment.BottomCenter,
        properties = PopupProperties(focusable = false),
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = slideInVertically(slideSpec) { if (position == ToastPosition.Top) -it else it } +
                fadeIn(tween(motion.mediumDurationMs, easing = motion.emphasizedEasing)),
            exit = slideOutVertically(slideSpec) { if (position == ToastPosition.Top) -it else it } +
                fadeOut(tween(motion.fastDurationMs, easing = motion.standardEasing)),
        ) {
            val dismissWidth = with(density) { 72.dp.toPx() }
            Compose7Surface(
                modifier = modifier
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .widthIn(max = 420.dp)
                    .fillMaxWidth()
                    .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                    .let {
                        if (dismissOnSwipe) {
                            it.pointerInput(Unit) {
                                var dragged = 0f
                                detectHorizontalDragGestures(
                                    onDragStart = { dragged = 0f },
                                    onHorizontalDrag = { change, dragAmount ->
                                        change.consume()
                                        dragged += dragAmount
                                        scope.launch { offsetX.snapTo(dragged) }
                                    },
                                    onDragEnd = {
                                        if (kotlin.math.abs(dragged) > dismissWidth) {
                                            scope.launch {
                                                offsetX.animateTo(
                                                    if (dragged > 0) size.width.toFloat() else -size.width.toFloat(),
                                                    tween(motion.fastDurationMs, easing = motion.standardEasing),
                                                )
                                                visible = false
                                                onDismissRequest?.invoke()
                                            }
                                        } else {
                                            scope.launch {
                                                offsetX.animateTo(
                                                    0f,
                                                    tween(motion.mediumDurationMs, easing = motion.emphasizedEasing),
                                                )
                                            }
                                        }
                                    },
                                )
                            }
                        } else {
                            it
                        }
                    },
                shape = shapes.snackbar,
                color = toastContainer(style, colors),
                contentColor = toastContentColor(style, colors),
            ) {
                BasicText(
                    text = text,
                    style = type.bodyMedium.copy(
                        color = toastContentColor(style, colors),
                        textAlign = TextAlign.Start,
                    ),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                )
            }
        }
    }
}

/** Toast container: inverse-surface on Material/Cupertino, raised panel on DesktopUI7. */
private fun toastContainer(style: Compose7Style, colors: io.github.compose7.core.theme.Compose7ColorScheme) =
    when (style) {
        Compose7Style.DesktopUI7 -> colors.surfaceVariant
        else -> colors.onSurface
    }

private fun toastContentColor(style: Compose7Style, colors: io.github.compose7.core.theme.Compose7ColorScheme) =
    when (style) {
        Compose7Style.DesktopUI7 -> colors.onSurface
        else -> colors.surface
    }

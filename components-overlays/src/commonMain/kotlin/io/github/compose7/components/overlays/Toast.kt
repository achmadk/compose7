package io.github.compose7.components.overlays

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
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
import io.github.compose7.core.theme.Compose7MotionTokens
import io.github.compose7.core.theme.Compose7Style
import io.github.compose7.core.theme.Compose7ThemeColors
import io.github.compose7.core.theme.Compose7ThemeShapes
import io.github.compose7.core.theme.Compose7ThemeTypography
import io.github.compose7.core.theme.currentCompose7Style
import io.github.compose7.foundation.Compose7Surface
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
 * conditionally - remove it from composition when [onDismissRequest] fires.
 *
 * For several toasts at once use [ToastHost] with a [ToastState] (PRD §8.4:
 * stacking). For a richer banner with a title and actions use [Notification].
 *
 * ```kotlin
 * var toast by remember { mutableStateOf<String?>(null) }
 * toast?.let { message ->
 *     Toast(message, onDismissRequest = { toast = null })
 * }
 * ```
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
    val motion = Compose7MotionTokens
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
        delay(duration.millis)
        visible = false
        onDismissRequest?.invoke()
    }

    ToastPopup(position) {
        AnimatedVisibility(
            visible = visible,
            enter = toastEnter(position, motion),
            exit = toastExit(position, motion),
        ) {
            ToastCard(
                text = text,
                modifier = modifier,
                dismissOnSwipe = dismissOnSwipe,
                onDismiss = {
                    visible = false
                    onDismissRequest?.invoke()
                },
            )
        }
    }
}

/** One toast queued in a [ToastState]. */
data class ToastMessage(
    val id: Long,
    val text: String,
    val duration: ToastDuration = ToastDuration.Short,
    val dismissOnSwipe: Boolean = true,
)

/**
 * Stacking toast queue (PRD §8.4: toast stacking). Add messages with [show];
 * every message auto-dismisses after its own duration, and the newest one sits
 * nearest the screen edge.
 *
 * ```kotlin
 * val toasts = rememberToastState()
 * ToastHost(toasts)
 * // ...
 * toasts.show("Saved")
 * ```
 */
@Stable
class ToastState internal constructor() {
    var messages: List<ToastMessage> by mutableStateOf(emptyList())
        private set

    private var nextId = 0L

    /** Queues [text]; returns the message id so a caller can dismiss it early. */
    fun show(
        text: String,
        duration: ToastDuration = ToastDuration.Short,
        dismissOnSwipe: Boolean = true,
    ): Long {
        val message = ToastMessage(
            id = ++nextId,
            text = text,
            duration = duration,
            dismissOnSwipe = dismissOnSwipe,
        )
        messages = messages + message
        return message.id
    }

    fun dismiss(id: Long) {
        messages = messages.filterNot { it.id == id }
    }

    fun clear() {
        messages = emptyList()
    }
}

/** Remembers a [ToastState] for [ToastHost]. */
@Composable
fun rememberToastState(): ToastState = remember { ToastState() }

/**
 * Renders a [ToastState] as a stack of toasts (PRD §8.4). At most [maxVisible]
 * are shown; older ones stay queued until a slot frees up. Each toast
 * auto-dismisses on its own timer and can be swiped away.
 */
@Composable
fun ToastHost(
    state: ToastState,
    modifier: Modifier = Modifier,
    position: ToastPosition = ToastPosition.Bottom,
    maxVisible: Int = 3,
) {
    val visible = visibleMessages(state.messages, position, maxVisible)
    if (visible.isEmpty()) return

    ToastPopup(position) {
        Column(
            modifier = modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .statusBarsPadding()
                .navigationBarsPadding()
                .widthIn(max = 420.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            visible.forEach { message ->
                key(message.id) {
                    LaunchedEffect(message.id) {
                        delay(message.duration.millis)
                        state.dismiss(message.id)
                    }
                    ToastCard(
                        text = message.text,
                        modifier = Modifier.fillMaxWidth(),
                        dismissOnSwipe = message.dismissOnSwipe,
                        onDismiss = { state.dismiss(message.id) },
                    )
                }
            }
        }
    }
}

/**
 * The subset of [messages] to render, oldest-first, so the newest lands on the
 * screen edge: last items for [ToastPosition.Bottom], first items (reversed)
 * for [ToastPosition.Top]. Extracted so the ordering is unit-tested.
 */
internal fun visibleMessages(
    messages: List<ToastMessage>,
    position: ToastPosition,
    maxVisible: Int,
): List<ToastMessage> = visibleWindow(messages, position, maxVisible)

/**
 * Newest-near-the-edge window over any stacked overlay. Bottom anchors keep
 * source order (newest last, nearest the bottom); top anchors reverse it so
 * the newest sits nearest the top.
 */
internal fun <T> visibleWindow(
    items: List<T>,
    position: ToastPosition,
    maxVisible: Int,
): List<T> {
    if (maxVisible <= 0) return emptyList()
    val window = items.takeLast(maxVisible)
    return if (position == ToastPosition.Top) window.reversed() else window
}

@Composable
private fun ToastPopup(position: ToastPosition, content: @Composable () -> Unit) {
    Popup(
        alignment = if (position == ToastPosition.Top) Alignment.TopCenter else Alignment.BottomCenter,
        properties = PopupProperties(focusable = false),
        content = content,
    )
}

@Composable
private fun ToastCard(
    text: String,
    modifier: Modifier,
    dismissOnSwipe: Boolean,
    onDismiss: () -> Unit,
) {
    val style = currentCompose7Style
    val colors = Compose7ThemeColors
    val type = Compose7ThemeTypography
    val shapes = Compose7ThemeShapes
    val motion = Compose7MotionTokens
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val offsetX = remember { Animatable(0f) }

    val dismissWidth = with(density) { 72.dp.toPx() }

    Compose7Surface(
        modifier = modifier
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
                                if (abs(dragged) > dismissWidth) {
                                    scope.launch {
                                        offsetX.animateTo(
                                            if (dragged > 0) size.width.toFloat() else -size.width.toFloat(),
                                            tween(motion.fastDurationMs, easing = motion.standardEasing),
                                        )
                                        onDismiss()
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

private fun toastEnter(position: ToastPosition, motion: io.github.compose7.core.theme.Compose7Motion) =
    slideInVertically(tween<IntOffset>(motion.mediumDurationMs, easing = motion.emphasizedEasing)) {
        if (position == ToastPosition.Top) -it else it
    } + fadeIn(tween(motion.mediumDurationMs, easing = motion.emphasizedEasing))

private fun toastExit(position: ToastPosition, motion: io.github.compose7.core.theme.Compose7Motion) =
    slideOutVertically(tween<IntOffset>(motion.fastDurationMs, easing = motion.standardEasing)) {
        if (position == ToastPosition.Top) -it else it
    } + fadeOut(tween(motion.fastDurationMs, easing = motion.standardEasing))

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

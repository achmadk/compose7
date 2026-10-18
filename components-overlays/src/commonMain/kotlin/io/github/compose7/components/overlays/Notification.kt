package io.github.compose7.components.overlays

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
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
import io.github.compose7.foundation.compose7Clickable
import io.github.compose7.foundation.currentMinTouchTarget
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** One banner queued in a [NotificationState]. */
data class NotificationMessage(
    val id: Long,
    val title: String,
    val text: String? = null,
    /** `null` keeps the banner until the user closes or swipes it. */
    val durationMillis: Long? = null,
    val dismissOnSwipe: Boolean = true,
)

/**
 * Banner notification (PRD §7, Appendix A "Toast / Notification", §8.4).
 *
 * Richer than [Toast]: a title, optional body text, an icon slot, an action
 * slot, and a close affordance. Persistent by default; pass [duration] to
 * auto-dismiss. Swipe-left/right dismisses early, matching [Toast].
 *
 * For several banners at once use [NotificationHost] with a [NotificationState]
 * (PRD §8.4: stacking).
 *
 * ```kotlin
 * if (showBanner) {
 *     Notification(
 *         title = "Upload complete",
 *         text = "3 files are now available.",
 *         onDismissRequest = { showBanner = false },
 *     )
 * }
 * ```
 *
 * @param duration Auto-dismiss delay in milliseconds; `null` (default) keeps
 *   the banner until dismissed.
 * @param closeContentDescription Spoken label for the close button (PRD §12:
 *   icon-only controls need one, and the caller owns the copy).
 */
@Composable
fun Notification(
    title: String,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    text: String? = null,
    position: ToastPosition = ToastPosition.Top,
    duration: Long? = null,
    dismissOnSwipe: Boolean = true,
    icon: (@Composable () -> Unit)? = null,
    action: (@Composable () -> Unit)? = null,
    closeContentDescription: String? = "Dismiss",
) {
    val motion = Compose7MotionTokens
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { visible = true }
    LaunchedEffect(duration) {
        if (duration != null) {
            delay(duration)
            visible = false
            onDismissRequest()
        }
    }

    NotificationPopup(position) {
        AnimatedVisibility(
            visible = visible,
            enter = notificationEnter(position, motion),
            exit = notificationExit(position, motion),
        ) {
            NotificationCard(
                title = title,
                text = text,
                icon = icon,
                action = action,
                dismissOnSwipe = dismissOnSwipe,
                closeContentDescription = closeContentDescription,
                onDismiss = {
                    visible = false
                    onDismissRequest()
                },
                modifier = modifier,
            )
        }
    }
}

/**
 * Stacking banner queue (PRD §8.4). [show] returns the message id so a caller
 * can dismiss one early; persistent banners stay until dismissed.
 *
 * ```kotlin
 * val banners = rememberNotificationState()
 * NotificationHost(banners)
 * banners.show("Upload complete", text = "3 files ready")
 * ```
 */
@Stable
class NotificationState internal constructor() {
    var messages: List<NotificationMessage> by mutableStateOf(emptyList())
        private set

    private var nextId = 0L

    fun show(
        title: String,
        text: String? = null,
        durationMillis: Long? = null,
        dismissOnSwipe: Boolean = true,
    ): Long {
        val message = NotificationMessage(
            id = ++nextId,
            title = title,
            text = text,
            durationMillis = durationMillis,
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

/** Remembers a [NotificationState] for [NotificationHost]. */
@Composable
fun rememberNotificationState(): NotificationState = remember { NotificationState() }

/** Renders a [NotificationState] as a stack of banners, newest nearest the edge. */
@Composable
fun NotificationHost(
    state: NotificationState,
    modifier: Modifier = Modifier,
    position: ToastPosition = ToastPosition.Top,
    maxVisible: Int = 3,
    closeContentDescription: String? = "Dismiss",
) {
    val visible = visibleWindow(state.messages, position, maxVisible)
    if (visible.isEmpty()) return

    NotificationPopup(position) {
        Column(
            modifier = modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .statusBarsPadding()
                .navigationBarsPadding()
                .widthIn(max = 420.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            visible.forEach { message ->
                key(message.id) {
                    LaunchedEffect(message.id, message.durationMillis) {
                        val duration = message.durationMillis
                        if (duration != null) {
                            delay(duration)
                            state.dismiss(message.id)
                        }
                    }
                    NotificationCard(
                        title = message.title,
                        text = message.text,
                        icon = null,
                        action = null,
                        dismissOnSwipe = message.dismissOnSwipe,
                        closeContentDescription = closeContentDescription,
                        onDismiss = { state.dismiss(message.id) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationPopup(position: ToastPosition, content: @Composable () -> Unit) {
    Popup(
        alignment = if (position == ToastPosition.Top) Alignment.TopCenter else Alignment.BottomCenter,
        properties = PopupProperties(focusable = false),
        content = content,
    )
}

@Composable
private fun NotificationCard(
    title: String,
    text: String?,
    icon: (@Composable () -> Unit)?,
    action: (@Composable () -> Unit)?,
    dismissOnSwipe: Boolean,
    closeContentDescription: String?,
    onDismiss: () -> Unit,
    modifier: Modifier,
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
        shape = shapes.card,
        color = colors.surface,
        contentColor = colors.onSurface,
        border = if (style == Compose7Style.DesktopUI7) BorderStroke(1.dp, colors.outlineVariant) else null,
        elevation = 6.dp,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            if (icon != null) {
                Box(Modifier.padding(end = 12.dp, top = 2.dp)) { icon() }
            }
            Column(Modifier.weight(1f)) {
                BasicText(
                    text = title,
                    style = type.titleSmall.copy(color = colors.onSurface),
                )
                if (text != null) {
                    Spacer(Modifier.height(2.dp))
                    BasicText(
                        text = text,
                        style = type.bodyMedium.copy(color = colors.onSurfaceVariant),
                    )
                }
                if (action != null) {
                    Spacer(Modifier.height(8.dp))
                    action()
                }
                NotificationCloseButton(closeContentDescription, onDismiss)
            }
        }
    }
}

@Composable
private fun NotificationCloseButton(contentDescription: String?, onClick: () -> Unit) {
    val colors = Compose7ThemeColors
    Box(
        modifier = Modifier
            .size(currentMinTouchTarget())
            .semantics { if (contentDescription != null) this.contentDescription = contentDescription }
            .compose7Clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        CloseGlyph(color = colors.onSurfaceVariant)
    }
}

/** Drawn cross (PRD §4: affordances are canvas primitives, never icon assets). */
@Composable
private fun CloseGlyph(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier.size(12.dp)) {
        val stroke = 1.5.dp.toPx()
        drawLine(color, Offset(0f, 0f), Offset(size.width, size.height), strokeWidth = stroke)
        drawLine(color, Offset(size.width, 0f), Offset(0f, size.height), strokeWidth = stroke)
    }
}

private fun notificationEnter(position: ToastPosition, motion: io.github.compose7.core.theme.Compose7Motion) =
    slideInVertically(tween<IntOffset>(motion.mediumDurationMs, easing = motion.emphasizedEasing)) {
        if (position == ToastPosition.Top) -it else it
    } + fadeIn(tween(motion.mediumDurationMs, easing = motion.emphasizedEasing))

private fun notificationExit(position: ToastPosition, motion: io.github.compose7.core.theme.Compose7Motion) =
    slideOutVertically(tween<IntOffset>(motion.fastDurationMs, easing = motion.standardEasing)) {
        if (position == ToastPosition.Top) -it else it
    } + fadeOut(tween(motion.fastDurationMs, easing = motion.standardEasing))

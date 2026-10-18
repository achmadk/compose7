package io.github.compose7.components.overlays

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlin.math.roundToInt
import kotlinx.coroutines.launch
import io.github.compose7.core.haptics.Compose7Haptic
import io.github.compose7.core.haptics.rememberHapticFeedback
import io.github.compose7.core.theme.Compose7MotionTokens
import io.github.compose7.core.theme.Compose7Style
import io.github.compose7.core.theme.Compose7StyleInfo
import io.github.compose7.core.theme.Compose7ThemeColors
import io.github.compose7.core.theme.Compose7ThemeShapes
import io.github.compose7.core.theme.Compose7Shapes
import io.github.compose7.core.theme.Compose7ThemeTypography
import io.github.compose7.core.theme.currentCompose7Style
import io.github.compose7.foundation.Compose7Surface
import io.github.compose7.foundation.compose7Clickable

/**
 * State holder for [SheetModal]: how far the sheet has been dragged down.
 * Owned by the sheet itself; remembered via [rememberSheetModalState].
 */
@Stable
class SheetModalState internal constructor(
    internal var dragOffsetPx: Float,
)

/** Remembers a [SheetModalState] for [SheetModal]. */
@Composable
fun rememberSheetModalState(): SheetModalState =
    remember { SheetModalState(dragOffsetPx = 0f) }

/**
 * Modal bottom sheet (PRD §7, P0 / Appendix A "Sheet Modal").
 *
 * Slides up over a scrim and dismisses by drag-down (past a third of the
 * sheet height), scrim tap, or the platform dismiss affordance. Content is a
 * plain column slot — sheets never bundle scroll containers (PRD §6.2), so
 * the caller decides scrolling behaviour.
 *
 * Motion follows the active style: Material's standard bottom-sheet motion,
 * Cupertino's inset card-style presentation ([Compose7StyleInfo.cardStyleSheetMotion],
 * PRD §8.4), DesktopUI7's snappy floating panel.
 *
 * ```kotlin
 * SheetModal(visible = show, onDismissRequest = { show = false }) {
 *     BasicText("Sheet content")
 * }
 * ```
 *
 * @param dismissOnScrimTap Tapping the dimmed backdrop dismisses the sheet.
 * @param draggable Enables drag-down-to-dismiss with a grabber affordance.
 */
@Composable
fun SheetModal(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    state: SheetModalState = rememberSheetModalState(),
    title: String? = null,
    dismissOnScrimTap: Boolean = true,
    draggable: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    if (!visible) return

    val style = currentCompose7Style
    val colors = Compose7ThemeColors
    val motion = Compose7MotionTokens
    val shapes = Compose7ThemeShapes
    val scope = rememberCoroutineScope()
    val haptic = rememberHapticFeedback()

    val offsetY = remember { Animatable(0f) }
    var heightPx by remember { mutableFloatStateOf(1f) }

    suspend fun settle(dragged: Float) {
        state.dragOffsetPx = dragged
        if (dragged > heightPx / 3f) {
            haptic(Compose7Haptic.Medium)
            offsetY.animateTo(
                targetValue = heightPx,
                animationSpec = tween(motion.mediumDurationMs, easing = motion.standardEasing),
            )
            onDismissRequest()
            offsetY.snapTo(0f)
        } else {
            offsetY.animateTo(
                targetValue = 0f,
                animationSpec = tween(motion.mediumDurationMs, easing = motion.emphasizedEasing),
            )
        }
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        val dismissFraction = (offsetY.value / heightPx).coerceIn(0f, 1f)
        val scrimAlpha by animateFloatAsState(
            targetValue = 0.4f * (1f - dismissFraction),
            animationSpec = tween(motion.fastDurationMs, easing = motion.standardEasing),
            label = "sheetScrim",
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.scrim.copy(alpha = scrimAlpha))
                .let {
                    if (dismissOnScrimTap) {
                        it.compose7Clickable(haptic = null, onClick = onDismissRequest)
                    } else {
                        it
                    }
                },
            contentAlignment = Alignment.BottomCenter,
        ) {
            val cardStyle = Compose7StyleInfo.cardStyleSheetMotion
            val sideInset = if (cardStyle) 12.dp else 0.dp

            Compose7Surface(
                modifier = Modifier
                    .onSizeChanged { heightPx = it.height.toFloat() }
                    .offset { IntOffset(0, offsetY.value.roundToInt()) }
                    .padding(start = sideInset, end = sideInset)
                    .let {
                        if (draggable) {
                            it.pointerInput(Unit) {
                                var dragged = 0f
                                detectVerticalDragGestures(
                                    onDragStart = { dragged = 0f },
                                    onVerticalDrag = { change, dragAmount ->
                                        change.consume()
                                        dragged = (dragged + dragAmount).coerceAtLeast(0f)
                                        state.dragOffsetPx = dragged
                                        scope.launch { offsetY.snapTo(dragged) }
                                    },
                                    onDragEnd = { scope.launch { settle(dragged) } },
                                    onDragCancel = { scope.launch { settle(0f) } },
                                )
                            }
                        } else {
                            it
                        }
                    }
                    .widthIn(max = 520.dp)
                    .fillMaxWidth()
                    .fillMaxHeight(fraction = sheetFraction(style)),
                shape = sheetShape(style),
                color = colors.surface,
                contentColor = colors.onSurface,
                border = if (style == Compose7Style.DesktopUI7) {
                    BorderStroke(1.dp, colors.outlineVariant)
                } else {
                    null
                },
                elevation = if (cardStyle) 12.dp else 0.dp,
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    if (draggable) {
                        Box(
                            Modifier
                                .align(Alignment.CenterHorizontally)
                                .padding(top = 8.dp, bottom = 4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(colors.outlineVariant)
                                .sheetGrabberSize(),
                        )
                    }
                    if (title != null) {
                        androidx.compose.foundation.text.BasicText(
                            text = title,
                            style = Compose7ThemeTypography.titleMedium.copy(color = colors.onSurface),
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        )
                    }
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .navigationBarsPadding()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.Top,
                        content = content,
                    )
                }
            }
        }
    }
}

/** Sheet height as a fraction of the window: Cupertino floats over more of the page. */
private fun sheetFraction(style: Compose7Style): Float = when (style) {
    Compose7Style.Cupertino -> 0.72f
    Compose7Style.DesktopUI7 -> 0.85f
    else -> 0.88f
}

/** Top-only rounded shape per style; Material keeps the token's full sheet radius. */
private fun sheetShape(style: Compose7Style): Shape =
    when (style) {
        Compose7Style.Cupertino -> RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
        Compose7Style.DesktopUI7 -> RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
        else -> RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    }

/** Grabber pill: 36×4dp. */
private fun Modifier.sheetGrabberSize(): Modifier =
    this.size(width = 36.dp, height = 4.dp)

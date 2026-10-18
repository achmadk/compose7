package io.github.compose7.components.overlays

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupPositionProvider
import io.github.compose7.core.haptics.Compose7Haptic
import io.github.compose7.core.haptics.rememberHapticFeedback
import io.github.compose7.core.nativebridge.resolveNativeBridge
import io.github.compose7.core.theme.Compose7ThemeColors
import io.github.compose7.core.theme.Compose7ThemeTypography
import io.github.compose7.foundation.compose7Clickable
import io.github.compose7.foundation.currentMinTouchTarget
import kotlin.math.roundToInt

/** Severity of a [ContextMenuItem], matching the rest of Compose7's action vocabulary. */
enum class ContextMenuItemRole { Default, Destructive }

/**
 * One row of a [ContextMenu]. Data-only so the iOS bridge can build system
 * `UIAction` rows from the same list the Compose menu renders (PRD §8.7).
 */
data class ContextMenuItem(
    val text: String,
    val onClick: () -> Unit,
    val role: ContextMenuItemRole = ContextMenuItemRole.Default,
    val enabled: Boolean = true,
)

/**
 * Long-press (touch) / right-click (pointer) menu on [content] - PRD §7,
 * Appendix A "Context Menu".
 *
 * The pure-Compose path opens a [Popover] at the press point. On iOS with the
 * Native Interop Bridge enabled, the same items are presented by a real
 * `UIContextMenuInteraction`, which brings the system haptics, highlight, and
 * menu chrome.
 *
 * Long press is detected by the anchor itself, so wrapping content that already
 * owns the gesture (a scrollable, a `Swipeout` row) keeps that gesture - the
 * menu then only opens on areas the child does not consume.
 *
 * ```kotlin
 * ContextMenu(
 *     items = listOf(
 *         ContextMenuItem("Copy", onClick = ::copy),
 *         ContextMenuItem("Delete", onClick = ::delete, role = ContextMenuItemRole.Destructive),
 *     ),
 * ) {
 *     Card { BasicText("Long-press me") }
 * }
 * ```
 *
 * @param onDismissRequest Called whenever the menu closes (outside tap,
 *   selection, or system dismiss), never for a tap that never opened it.
 * @param nativeBridge Per-component switch; `null` follows the theme.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ContextMenu(
    items: List<ContextMenuItem>,
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit = {},
    nativeBridge: Boolean? = null,
    content: @Composable () -> Unit,
) {
    val useNative = resolveNativeBridge(nativeBridge)
    val haptic = rememberHapticFeedback()

    var anchorBounds by remember { mutableStateOf(IntRect.Zero) }
    var pressOffset by remember { mutableStateOf(Offset.Zero) }
    var visible by remember { mutableStateOf(false) }
    val currentAnchor by rememberUpdatedState(anchorBounds)

    Box(
        modifier = modifier
            .onGloballyPositioned { coordinates ->
                val rect = coordinates.boundsInWindow()
                anchorBounds = IntRect(
                    left = rect.left.roundToInt(),
                    top = rect.top.roundToInt(),
                    right = rect.right.roundToInt(),
                    bottom = rect.bottom.roundToInt(),
                )
            }
            .then(
                if (useNative) {
                    // The UIKit interaction owns the long press; adding a
                    // Compose detector here too would open two menus.
                    Modifier
                } else {
                    Modifier
                        .pointerInput(items) {
                            detectTapGestures(
                                onLongPress = { point ->
                                    if (items.isEmpty()) return@detectTapGestures
                                    pressOffset = point
                                    haptic(Compose7Haptic.Medium)
                                    visible = true
                                },
                            )
                        }
                        // Right-click (desktop/trackpad) opens the same menu; the
                        // event is observed, never consumed, so child gestures win.
                        .pointerInput(items) {
                            awaitPointerEventScope {
                                while (true) {
                                    val event = awaitPointerEvent()
                                    if (
                                        event.type == PointerEventType.Press &&
                                        event.buttons.isSecondaryPressed
                                    ) {
                                        val change = event.changes.firstOrNull() ?: continue
                                        if (items.isEmpty()) continue
                                        pressOffset = change.position
                                        haptic(Compose7Haptic.Medium)
                                        visible = true
                                    }
                                }
                            }
                        }
                },
            ),
    ) {
        content()
    }

    if (useNative) {
        if (items.isNotEmpty() && !anchorBounds.isEmpty) {
            NativeContextMenu(
                items = items,
                anchorBounds = anchorBounds,
                onDismissRequest = onDismissRequest,
            )
        }
    } else if (visible) {
        val density = LocalDensity.current
        val gapPx = with(density) { 8.dp.roundToPx() }
        // Press point in window coordinates: the anchor's origin plus the
        // pointer's offset inside it (both Compose pixels).
        val menuAnchor = remember(currentAnchor, pressOffset, gapPx) {
            IntRect(
                offset = IntOffset(
                    (currentAnchor.left + pressOffset.x).roundToInt(),
                    (currentAnchor.top + pressOffset.y).roundToInt(),
                ),
                size = IntSize.Zero,
            )
        }
        ContextMenuPopup(
            items = items,
            anchor = menuAnchor,
            gapPx = gapPx,
            onDismiss = {
                visible = false
                onDismissRequest()
            },
        )
    }
}

@Composable
private fun ContextMenuPopup(
    items: List<ContextMenuItem>,
    anchor: IntRect,
    gapPx: Int,
    onDismiss: () -> Unit,
) {
    val colors = Compose7ThemeColors

    Popover(
        visible = true,
        onDismissRequest = onDismiss,
        positionProvider = remember(anchor, gapPx) { ContextMenuPositionProvider(anchor, gapPx) },
    ) {
        Column(modifier = Modifier.widthIn(min = 180.dp).fillMaxWidth()) {
            items.forEachIndexed { index, item ->
                if (index > 0) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(colors.outlineVariant),
                    )
                }
                ContextMenuRow(item) {
                    item.onClick()
                    onDismiss()
                }
            }
        }
    }
}

@Composable
private fun ContextMenuRow(item: ContextMenuItem, onClick: () -> Unit) {
    val colors = Compose7ThemeColors
    val type = Compose7ThemeTypography
    val color = when {
        !item.enabled -> colors.onSurface.copy(alpha = 0.38f)
        item.role == ContextMenuItemRole.Destructive -> colors.error
        else -> colors.onSurface
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = currentMinTouchTarget())
            .compose7Clickable(enabled = item.enabled, onClick = onClick, role = Role.Button)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        BasicText(text = item.text, style = type.bodyLarge.copy(color = color))
    }
}

/**
 * Places the menu just below-right of the press point, flipping above and
 * clamping to the window edges when there is no room. Extracted from the
 * composable so the placement rule is unit-tested (PRD §13).
 */
internal class ContextMenuPositionProvider(
    private val anchor: IntRect,
    private val gapPx: Int = 0,
) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset {
        val below = anchor.bottom + gapPx
        val y = if (below + popupContentSize.height <= windowSize.height) {
            below
        } else {
            (anchor.top - gapPx - popupContentSize.height).coerceAtLeast(0)
        }
        val unclampedX = if (layoutDirection == LayoutDirection.Ltr) {
            anchor.left
        } else {
            anchor.right - popupContentSize.width
        }
        val maxX = (windowSize.width - popupContentSize.width).coerceAtLeast(0)
        return IntOffset(unclampedX.coerceIn(0, maxX), y)
    }
}

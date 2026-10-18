// Cross-compiled as a klib on any host; keep UIKit usage conservative.
package io.github.compose7.components.overlays

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.uikit.LocalUIViewController
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.CoreGraphics.CGPoint
import platform.UIKit.UIAction
import platform.UIKit.UIContextMenuConfiguration
import platform.UIKit.UIContextMenuInteraction
import platform.UIKit.UIContextMenuInteractionAnimatingProtocol
import platform.UIKit.UIContextMenuInteractionDelegateProtocol
import platform.UIKit.UIMenu
import platform.UIKit.UIMenuElementAttributesDestructive
import platform.UIKit.UIMenuElementAttributesDisabled
import platform.UIKit.UIView
import platform.UIKit.addInteraction
import platform.UIKit.removeInteraction
import platform.darwin.NSObject
import kotlin.math.roundToInt

/**
 * One registered anchor. The menu finds the top-most entry whose window
 * rectangle contains the long-press point.
 */
private class ContextMenuEntry(
    val bounds: () -> IntRect,
    val items: () -> List<ContextMenuItem>,
    val onDismiss: () -> Unit,
)

private val contextMenuEntries = mutableListOf<ContextMenuEntry>()
private var contextMenuHostView: UIView? = null
private var contextMenuInteraction: UIContextMenuInteraction? = null
private var contextMenuDelegate: UIContextMenuDelegate? = null
private var contextMenuActiveEntry: ContextMenuEntry? = null

/**
 * One interaction for the whole app. Attaching it per anchor is impossible:
 * `UIContextMenuInteraction` is a `UIView` concern, not a Compose-node one.
 */
@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
private class UIContextMenuDelegate : NSObject(), UIContextMenuInteractionDelegateProtocol {

    override fun contextMenuInteraction(
        interaction: UIContextMenuInteraction,
        configurationForMenuAtLocation: CValue<CGPoint>,
    ): UIContextMenuConfiguration? {
        val point = configurationForMenuAtLocation.useContents {
            IntOffset(x.roundToInt(), y.roundToInt())
        }
        val entry = contextMenuEntries.lastOrNull { candidate ->
            val bounds = candidate.bounds()
            !bounds.isEmpty &&
                point.x in bounds.left until bounds.right &&
                point.y in bounds.top until bounds.bottom
        } ?: return null
        val actions = entry.items()
        if (actions.isEmpty()) return null

        contextMenuActiveEntry = entry
        return UIContextMenuConfiguration.configurationWithIdentifier(
            identifier = null,
            previewProvider = null,
            actionProvider = {
                UIMenu.menuWithTitle(
                    title = "",
                    children = actions.map { item ->
                        UIAction.actionWithTitle(
                            title = item.text,
                            image = null,
                            identifier = null,
                            handler = { _: UIAction? -> item.onClick() },
                        ).apply {
                            attributes = when {
                                !item.enabled -> UIMenuElementAttributesDisabled
                                item.role == ContextMenuItemRole.Destructive ->
                                    UIMenuElementAttributesDestructive
                                else -> 0uL
                            }
                        }
                    },
                )
            },
        )
    }

    override fun contextMenuInteraction(
        interaction: UIContextMenuInteraction,
        willEndForConfiguration: UIContextMenuConfiguration,
        animator: UIContextMenuInteractionAnimatingProtocol?,
    ) {
        contextMenuActiveEntry?.onDismiss()
        contextMenuActiveEntry = null
    }
}

private fun attachContextMenuInteraction(view: UIView) {
    if (contextMenuHostView === view && contextMenuInteraction != null) return
    detachContextMenuInteraction()
    val delegate = UIContextMenuDelegate()
    val interaction = UIContextMenuInteraction(delegate = delegate)
    view.addInteraction(interaction)
    contextMenuHostView = view
    contextMenuDelegate = delegate
    contextMenuInteraction = interaction
}

private fun detachContextMenuInteraction() {
    val view = contextMenuHostView
    val interaction = contextMenuInteraction
    if (view != null && interaction != null) view.removeInteraction(interaction)
    contextMenuHostView = null
    contextMenuInteraction = null
    contextMenuDelegate = null
    contextMenuActiveEntry = null
}

private fun registerContextMenu(view: UIView, entry: ContextMenuEntry) {
    contextMenuEntries += entry
    attachContextMenuInteraction(view)
}

private fun unregisterContextMenu(entry: ContextMenuEntry) {
    contextMenuEntries -= entry
    if (contextMenuEntries.isEmpty()) detachContextMenuInteraction()
}

@OptIn(ExperimentalForeignApi::class)
@Composable
internal actual fun NativeContextMenu(
    items: List<ContextMenuItem>,
    anchorBounds: IntRect,
    onDismissRequest: () -> Unit,
) {
    val view = LocalUIViewController.current.view
    val itemsState = rememberUpdatedState(items)
    val boundsState = rememberUpdatedState(anchorBounds)
    val dismissState = rememberUpdatedState(onDismissRequest)

    val entry = remember(view) {
        ContextMenuEntry(
            bounds = { boundsState.value },
            items = { itemsState.value },
            onDismiss = { dismissState.value() },
        )
    }

    DisposableEffect(entry) {
        registerContextMenu(view, entry)
        onDispose { unregisterContextMenu(entry) }
    }
}

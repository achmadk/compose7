// Cross-compiled as a klib on any host; keep UIKit usage conservative.
package io.github.compose7.components.overlays

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSURL
import platform.UIKit.UIActivityViewController
import platform.UIKit.popoverPresentationController

/**
 * Presents [items] through the system share sheet. `UIActivityViewController`
 * reports completion and cancellation through the same handler, so the caller
 * always gets its `onDismissRequest`.
 */
@OptIn(ExperimentalForeignApi::class)
@Composable
internal actual fun NativeShareSheet(
    items: List<ShareItem>,
    onDismissRequest: () -> Unit,
) {
    val currentOnDismiss by rememberUpdatedState(onDismissRequest)

    DisposableEffect(items) {
        val activityItems = items.mapNotNull { item ->
            when (item.kind) {
                ShareItemKind.Text -> item.value
                ShareItemKind.Link -> NSURL.URLWithString(item.value)
            }
        }
        if (activityItems.isEmpty()) {
            currentOnDismiss()
            onDispose {}
        } else {
            val controller = UIActivityViewController(
                activityItems = activityItems,
                applicationActivities = null,
            )
            controller.completionWithItemsHandler = { _, _, _, _ -> currentOnDismiss() }

            val presenter = compose7TopViewController()
            if (presenter == null) {
                currentOnDismiss()
                onDispose {}
            } else {
                // iPad presents share sheets in a popover and needs an anchor.
                controller.popoverPresentationController?.let { popover ->
                    popover.sourceView = presenter.view
                    popover.setSourceRect(presenter.view.bounds)
                }
                presenter.presentViewController(controller, animated = true, completion = null)
                onDispose {
                    if (controller.presentingViewController != null) {
                        controller.dismissViewControllerAnimated(false, completion = null)
                    }
                }
            }
        }
    }
}

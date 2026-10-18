// Cross-compiled as a klib on any host; keep UIKit usage conservative.
package io.github.compose7.components.overlays

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIAlertAction
import platform.UIKit.UIAlertActionStyleCancel
import platform.UIKit.UIAlertActionStyleDefault
import platform.UIKit.UIAlertActionStyleDestructive
import platform.UIKit.UIAlertController
import platform.UIKit.UIAlertControllerStyleActionSheet
import platform.UIKit.UIAlertControllerStyleAlert
import platform.UIKit.UIApplication
import platform.UIKit.UISceneActivationStateForegroundActive
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowScene
import platform.UIKit.popoverPresentationController

/**
 * Best-effort top-most view controller to present from: walk the key window's
 * root controller up through presented controllers (boring, well-known pattern).
 *
 * `connectedScenes`/`windows` are untyped in the binding (`Set<*>`/`List<*>`),
 * so the lookup filters to [UIWindowScene]/[UIWindow] instead of assuming
 * element types.
 */
@OptIn(ExperimentalForeignApi::class)
private fun compose7TopViewController(): UIViewController? {
    val application = UIApplication.sharedApplication
    val scenes = application.connectedScenes.filterIsInstance<UIWindowScene>()
    val scene = scenes.firstOrNull { it.activationState == UISceneActivationStateForegroundActive }
        ?: scenes.firstOrNull()
    val windows = scene?.windows?.filterIsInstance<UIWindow>().orEmpty()
    val window = windows.firstOrNull { it.isKeyWindow() } ?: windows.firstOrNull()
    var top = window?.rootViewController
    while (true) {
        val presented = top?.presentedViewController ?: break
        top = presented
    }
    return top
}

/**
 * Presents [controller] from the top-most controller. Returns `false` when
 * there is no window to present from at all (never crash; the caller dismisses).
 *
 * @param anchorToSourceView iPad requires action sheets to have a popover
 *   anchor; without one UIKit raises an exception.
 */
@OptIn(ExperimentalForeignApi::class)
private fun present(controller: UIAlertController, anchorToSourceView: Boolean): Boolean {
    val presenter = compose7TopViewController() ?: return false
    if (anchorToSourceView) {
        controller.popoverPresentationController?.let { popover ->
            popover.sourceView = presenter.view
            popover.setSourceRect(presenter.view.bounds)
        }
    }
    presenter.presentViewController(controller, animated = true, completion = null)
    return true
}

/** Dismisses a controller that is still on screen (action taps self-dismiss). */
private fun dismissIfPresented(controller: UIAlertController) {
    if (controller.presentingViewController != null) {
        controller.dismissViewControllerAnimated(true, completion = null)
    }
}

@Composable
internal actual fun NativeDialog(
    onDismissRequest: () -> Unit,
    title: String?,
    message: String?,
    actions: List<DialogAction>,
) {
    val currentOnDismiss by rememberUpdatedState(onDismissRequest)
    val currentActions by rememberUpdatedState(actions)

    DisposableEffect(title, message, actions.size) {
        val controller = UIAlertController.alertControllerWithTitle(
            title = title,
            message = message,
            preferredStyle = UIAlertControllerStyleAlert,
        )
        currentActions.forEach { action ->
            val style = when (action.role) {
                DialogActionRole.Destructive -> UIAlertActionStyleDestructive
                DialogActionRole.Cancel -> UIAlertActionStyleCancel
                DialogActionRole.Default, DialogActionRole.Confirm -> UIAlertActionStyleDefault
            }
            controller.addAction(
                UIAlertAction.actionWithTitle(
                    title = action.text,
                    style = style,
                    handler = { _: UIAlertAction? ->
                        if (action.enabled) action.onClick()
                        currentOnDismiss()
                    },
                ),
            )
        }
        if (!present(controller, anchorToSourceView = false)) currentOnDismiss()
        onDispose { dismissIfPresented(controller) }
    }
}

@Composable
internal actual fun NativeActionSheet(
    onDismissRequest: () -> Unit,
    title: String?,
    message: String?,
    actions: List<ActionSheetAction>,
) {
    val currentOnDismiss by rememberUpdatedState(onDismissRequest)
    val currentActions by rememberUpdatedState(actions)

    DisposableEffect(title, message, actions.size) {
        val controller = UIAlertController.alertControllerWithTitle(
            title = title,
            message = message,
            preferredStyle = UIAlertControllerStyleActionSheet,
        )
        currentActions.forEach { action ->
            val style = when (action.role) {
                ActionSheetActionRole.Destructive -> UIAlertActionStyleDestructive
                ActionSheetActionRole.Cancel -> UIAlertActionStyleCancel
                ActionSheetActionRole.Default -> UIAlertActionStyleDefault
            }
            controller.addAction(
                UIAlertAction.actionWithTitle(
                    title = action.text,
                    style = style,
                    handler = { _: UIAlertAction? ->
                        if (action.enabled) action.onClick()
                        currentOnDismiss()
                    },
                ),
            )
        }
        // Action sheets need a popover anchor on iPad; alerts are always modal.
        if (!present(controller, anchorToSourceView = true)) currentOnDismiss()
        onDispose { dismissIfPresented(controller) }
    }
}

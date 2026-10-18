// Compiles only on macOS hosts — keep UIKit usage conservative.
package io.github.compose7.components.overlays

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import platform.Foundation.NSURL
import platform.UIKit.UIAlertAction
import platform.UIKit.UIAlertActionStyle
import platform.UIKit.UIAlertController
import platform.UIKit.UIAlertControllerStyle
import platform.UIKit.UIApplication
import platform.UIKit.UIViewController
import platform.UIKit.UIWindowScene
import platform.darwin.NSObject

/**
 * Best-effort top-most view controller to present from: walk the key window's
 * root controller up through presented controllers (boring, well-known pattern).
 */
private fun compose7TopViewController(): UIViewController? {
    val application = UIApplication.sharedApplication
    val scene = application.connectedScenes
        .filterIsInstance<UIWindowScene>()
        .firstOrNull { it.activationState == platform.UIKit.UIWindowSceneActivationStateForegroundActive }
        ?: application.connectedScenes.filterIsInstance<UIWindowScene>().firstOrNull()
    val window = scene?.windows?.firstOrNull { it.isKeyWindow }
        ?: scene?.windows?.firstOrNull()
        ?: application.windows.firstOrNull { it.isKeyWindow }
        ?: application.windows.firstOrNull()
    var top = window?.rootViewController
    while (true) {
        val presented = top?.presentedViewController ?: break
        top = presented
    }
    return top
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

    DisposableEffect(Unit) {
        val controller = UIAlertController.alertControllerWithTitle(
            title = title,
            message = message,
            preferredStyle = UIAlertControllerStyle.UIAlertControllerStyleAlert,
        )
        currentActions.forEach { action ->
            val style = when (action.role) {
                DialogActionRole.Destructive -> UIAlertActionStyle.UIAlertActionStyleDestructive
                DialogActionRole.Cancel -> UIAlertActionStyle.UIAlertActionStyleCancel
                DialogActionRole.Default, DialogActionRole.Confirm ->
                    UIAlertActionStyle.UIAlertActionStyleDefault
            }
            val handler: (UIAlertAction) -> Unit = { _ ->
                if (action.enabled) action.onClick()
                currentOnDismiss()
            }
            controller.addAction(
                UIAlertAction.actionWithTitle(
                    title = action.text,
                    style = style,
                    handler = handler,
                ),
            )
        }
        val presenter = compose7TopViewController()
        if (presenter == null) {
            // Nothing to present on — fall back to dismissing rather than crash.
            currentOnDismiss()
        } else {
            presenter.presentViewController(controller, animated = true, completion = null)
        }
        onDispose {
            // Only dismiss if still on screen (action taps auto-dismiss first).
            if (controller.presentingViewController != null) {
                controller.dismissViewControllerAnimated(true, completion = null)
            }
        }
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

    DisposableEffect(Unit) {
        val controller = UIAlertController.alertControllerWithTitle(
            title = title,
            message = message,
            preferredStyle = UIAlertControllerStyle.UIAlertControllerStyleActionSheet,
        )
        currentActions.forEach { action ->
            val style = when (action.role) {
                ActionSheetActionRole.Destructive -> UIAlertActionStyle.UIAlertActionStyleDestructive
                ActionSheetActionRole.Cancel -> UIAlertActionStyle.UIAlertActionStyleCancel
                ActionSheetActionRole.Default -> UIAlertActionStyle.UIAlertActionStyleDefault
            }
            val handler: (UIAlertAction) -> Unit = { _ ->
                if (action.enabled) action.onClick()
                currentOnDismiss()
            }
            controller.addAction(
                UIAlertAction.actionWithTitle(
                    title = action.text,
                    style = style,
                    handler = handler,
                ),
            )
        }
        val presenter = compose7TopViewController()
        if (presenter == null) {
            currentOnDismiss()
        } else {
            // iPad requires a popover anchor for action sheets or it throws.
            controller.popoverPresentationController?.let { popover ->
                popover.sourceView = presenter.view
                presenter.view?.bounds?.let { bounds -> popover.setSourceRect(bounds) }
            }
            presenter.presentViewController(controller, animated = true, completion = null)
        }
        onDispose {
            if (controller.presentingViewController != null) {
                controller.dismissViewControllerAnimated(true, completion = null)
            }
        }
    }
}

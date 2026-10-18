package io.github.compose7.components.overlays

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.compose7.core.nativebridge.resolveNativeBridge

/** What a [ShareItem] carries: plain text, or something the OS should treat as a link. */
enum class ShareItemKind { Text, Link }

/**
 * One thing to hand to the system share sheet.
 *
 * [label] is used only by the pure-Compose fallback (it labels the row); the
 * native `UIActivityViewController` shows the item itself and ignores it.
 */
data class ShareItem(
    val label: String,
    val value: String,
    val kind: ShareItemKind = ShareItemKind.Text,
)

/**
 * System share sheet (PRD §7 / §8.7 bridge list item 4).
 *
 * On iOS with the Native Interop Bridge enabled this presents a real
 * `UIActivityViewController` - share extensions, AirDrop, copy, and the rest
 * are UIKit-owned behavior that pure Compose cannot reproduce. Everywhere else
 * it renders a [ActionSheet] of the same items and calls [onItemSelected] so
 * the app can plug in its own platform share (for example an Android
 * `ACTION_SEND` intent); the bridge path never calls that callback.
 *
 * ```kotlin
 * ShareSheet(
 *     visible = sharing,
 *     onDismissRequest = { sharing = false },
 *     items = listOf(ShareItem("Copy link", url, ShareItemKind.Link)),
 * )
 * ```
 *
 * @param visible Presentation state; the call site owns it.
 * @param onItemSelected Pure-fallback hook. Not invoked on iOS.
 * @param nativeBridge Per-component switch; `null` follows the theme.
 */
@Composable
fun ShareSheet(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    items: List<ShareItem>,
    modifier: Modifier = Modifier,
    nativeBridge: Boolean? = null,
    onItemSelected: (ShareItem) -> Unit = {},
) {
    if (!visible || items.isEmpty()) return

    if (resolveNativeBridge(nativeBridge)) {
        NativeShareSheet(items = items, onDismissRequest = onDismissRequest)
    } else {
        PureShareSheet(
            visible = true,
            onDismissRequest = onDismissRequest,
            items = items,
            modifier = modifier,
            onItemSelected = onItemSelected,
        )
    }
}

@Composable
internal fun PureShareSheet(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    items: List<ShareItem>,
    modifier: Modifier,
    onItemSelected: (ShareItem) -> Unit,
) {
    val actions = shareSheetFallbackActions(
        items = items,
        onItemSelected = onItemSelected,
        onDismissRequest = onDismissRequest,
    )

    PureActionSheet(
        visible = visible,
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        title = null,
        message = null,
        actions = actions,
        content = null,
    )
}

/**
 * Rows for the pure-Compose fallback: one per [items] entry in source order,
 * then a single Cancel. Selecting a row notifies the caller before dismissing;
 * Cancel dismisses without notifying. Extracted so this ordering is unit-tested
 * (PRD §13) instead of living only inside the composable.
 */
internal fun shareSheetFallbackActions(
    items: List<ShareItem>,
    onItemSelected: (ShareItem) -> Unit,
    onDismissRequest: () -> Unit,
): List<ActionSheetAction> = items.map { item ->
    ActionSheetAction(
        text = item.label,
        onClick = {
            onItemSelected(item)
            onDismissRequest()
        },
    )
} + ActionSheetAction("Cancel", onDismissRequest, ActionSheetActionRole.Cancel)

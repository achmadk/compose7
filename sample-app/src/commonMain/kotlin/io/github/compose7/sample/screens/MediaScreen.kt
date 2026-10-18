package io.github.compose7.sample.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupPositionProvider
import io.github.compose7.components.buttons.Button
import io.github.compose7.components.buttons.ButtonVariant
import io.github.compose7.components.media.PhotoBrowser
import io.github.compose7.components.media.Swiper
import io.github.compose7.components.messaging.Message
import io.github.compose7.components.messaging.MessageBar
import io.github.compose7.components.messaging.MessageList
import io.github.compose7.components.navigation.Searchbar
import io.github.compose7.components.navigation.Tabs
import io.github.compose7.components.navigation.TabSpec
import io.github.compose7.components.overlays.ActionSheet
import io.github.compose7.components.overlays.ActionSheetAction
import io.github.compose7.components.overlays.ActionSheetActionRole
import io.github.compose7.components.overlays.Dialog
import io.github.compose7.components.overlays.DialogAction
import io.github.compose7.components.overlays.DialogActionRole
import io.github.compose7.components.overlays.Popup
import io.github.compose7.components.overlays.SheetModal
import io.github.compose7.components.overlays.Toast
import io.github.compose7.components.overlays.Tooltip
import io.github.compose7.core.theme.Compose7ThemeColors
import io.github.compose7.core.theme.Compose7ThemeTypography
import io.github.compose7.core.theme.Compose7ThemeShapes

private val ToastMessages = listOf(
    "Saved to your library",
    "Copied to clipboard",
    "Sync complete",
)

/** Anchors tooltips under their anchor with no clamping - enough for the gallery. */
private val BelowAnchorProvider = object : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset = IntOffset(anchorBounds.left, anchorBounds.bottom + 8)
}

/** Media, overlays, navigation extras, and messaging (PRD §7 P1–P2 groups). */
@Composable
fun MediaScreen() {
    var dialogVisible by remember { mutableStateOf(false) }
    var sheetVisible by remember { mutableStateOf(false) }
    var actionSheetVisible by remember { mutableStateOf(false) }
    var popupVisible by remember { mutableStateOf(false) }
    var toastText by remember { mutableStateOf<String?>(null) }
    var photoBrowserVisible by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    var tabIndex by remember { mutableStateOf(0) }
    var messageDraft by remember { mutableStateOf("") }
    var messages by remember {
        mutableStateOf(
            listOf(
                Message("1", "Welcome to the Compose7 gallery.", sent = false, time = "09:41"),
                Message("2", "Everything here is a library component.", sent = true, time = "09:42"),
                Message("3", "Try the style switch up top.", sent = false, time = "09:43"),
            ),
        )
    }

    DemoSection("Overlays") {
        DemoRow {
            Button(onClick = { dialogVisible = true }, text = "Dialog")
            Button(onClick = { sheetVisible = true }, text = "Sheet", variant = ButtonVariant.Tonal)
            Button(
                onClick = { actionSheetVisible = true },
                text = "Action sheet",
                variant = ButtonVariant.Outline,
            )
            Button(onClick = { popupVisible = true }, text = "Popup", variant = ButtonVariant.Text)
        }
        Spacer(Modifier.height(8.dp))
        DemoRow {
            Button(
                onClick = { toastText = ToastMessages.random() },
                text = "Toast",
                variant = ButtonVariant.Tonal,
            )
            Tooltip(
                positionProvider = BelowAnchorProvider,
                content = { BasicText("Long-press or hover me", style = Compose7ThemeTypography.bodySmall) },
            ) {
                Button(onClick = {}, text = "Tooltip", variant = ButtonVariant.Outline)
            }
        }
    }

    DemoSection("Search & tabs") {
        Searchbar(
            query = query,
            onQueryChange = { query = it },
            onSubmit = {},
            onClear = { query = "" },
        )
        Spacer(Modifier.height(8.dp))
        Tabs(
            tabs = listOf(TabSpec("All"), TabSpec("Unread"), TabSpec("Flagged")),
            selectedIndex = tabIndex,
            onSelect = { tabIndex = it },
        )
        BasicText(
            text = "Query: $query | Tab: $tabIndex",
            modifier = Modifier.padding(top = 8.dp),
            style = Compose7ThemeTypography.bodySmall.copy(color = Compose7ThemeColors.onSurfaceVariant),
        )
    }

    DemoSection("Swiper") {
        Swiper(
            pageCount = 3,
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp),
        ) { page ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .background(
                        Brush.linearGradient(
                            listOf(
                                Compose7ThemeColors.primary,
                                Compose7ThemeColors.secondary,
                            ),
                        ),
                        Compose7ThemeShapes.card,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                BasicText(
                    text = "Page ${page + 1}",
                    style = Compose7ThemeTypography.titleLarge.copy(color = Compose7ThemeColors.onPrimary),
                )
            }
        }
    }

    DemoSection("Photo browser") {
        Button(onClick = { photoBrowserVisible = true }, text = "Open gallery")
    }

    DemoSection("Messages") {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
        ) {
            MessageList(
                messages = messages,
                modifier = Modifier.fillMaxWidth(),
                dateSeparator = { "Today" },
            )
        }
        Spacer(Modifier.height(8.dp))
        MessageBar(
            value = messageDraft,
            onValueChange = { messageDraft = it },
            placeholder = "Type a message",
            onSubmit = {
                if (messageDraft.isNotBlank()) {
                    messages = messages + Message(
                        id = (messages.size + 1).toString(),
                        text = messageDraft,
                        sent = true,
                        time = "now",
                    )
                    messageDraft = ""
                }
            },
            sendEnabled = messageDraft.isNotBlank(),
        )
    }

    if (dialogVisible) {
        Dialog(
            onDismissRequest = { dialogVisible = false },
            title = "Delete project?",
            message = "This cannot be undone. The project and its history are removed.",
            actions = listOf(
                DialogAction("Cancel", role = DialogActionRole.Cancel, onClick = { dialogVisible = false }),
                DialogAction(
                    "Delete",
                    role = DialogActionRole.Destructive,
                    onClick = { dialogVisible = false },
                ),
            ),
        )
    }

    if (actionSheetVisible) {
        ActionSheet(
            visible = true,
            onDismissRequest = { actionSheetVisible = false },
            title = "Share",
            message = "Pick a destination",
            actions = listOf(
                ActionSheetAction("Copy link", onClick = { actionSheetVisible = false }),
                ActionSheetAction("Add to favourites", onClick = { actionSheetVisible = false }),
                ActionSheetAction(
                    "Delete",
                    role = ActionSheetActionRole.Destructive,
                    onClick = { actionSheetVisible = false },
                ),
                ActionSheetAction(
                    "Cancel",
                    role = ActionSheetActionRole.Cancel,
                    onClick = { actionSheetVisible = false },
                ),
            ),
        )
    }

    SheetModal(
        visible = sheetVisible,
        onDismissRequest = { sheetVisible = false },
        title = "Bottom sheet",
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            DemoCaption("Drag down or tap the scrim to dismiss.")
            Spacer(Modifier.height(8.dp))
            Button(onClick = { sheetVisible = false }, text = "Close", modifier = Modifier.fillMaxWidth())
        }
    }

    Popup(visible = popupVisible, onDismissRequest = { popupVisible = false }) {
        BasicText(
            text = "A window-level popup anchored to the centre.",
            style = Compose7ThemeTypography.bodyMedium.copy(color = Compose7ThemeColors.onSurface),
        )
        Spacer(Modifier.height(8.dp))
        Button(onClick = { popupVisible = false }, text = "Dismiss", modifier = Modifier.fillMaxWidth())
    }

    toastText?.let { message ->
        Toast(
            text = message,
            onDismissRequest = { toastText = null },
        )
    }

    PhotoBrowser(
        items = listOf(
            Color(0xFF2F6FED),
            Color(0xFFE5484D),
            Color(0xFF30A46C),
        ),
        visible = photoBrowserVisible,
        onVisibleChange = { photoBrowserVisible = it },
        // Named, because the trailing lambda would otherwise bind to
        // `backgroundColor` (image is not the last parameter).
        image = { color, page, modifier ->
            Box(
                modifier = modifier
                    .size(width = 320.dp, height = 420.dp)
                    .background(color),
                contentAlignment = Alignment.Center,
            ) {
                BasicText(
                    text = "Photo ${page + 1}",
                    style = Compose7ThemeTypography.titleLarge.copy(color = Color.White),
                )
            }
        },
    )
}

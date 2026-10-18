package io.github.compose7.components.messaging

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import io.github.compose7.core.theme.Compose7Style
import io.github.compose7.core.theme.Compose7StyleInfo
import io.github.compose7.core.theme.Compose7ThemeColors
import io.github.compose7.core.theme.Compose7ThemeShapes
import io.github.compose7.core.theme.Compose7ThemeTypography
import io.github.compose7.core.theme.currentCompose7Style

/**
 * One chat message rendered by [MessageList].
 *
 * @param id Stable identity — must be unique within the list (used as the lazy
 *   item key).
 * @param text Message body, rendered as `BasicText` unless [MessageList]'s
 *   `bubbleContent` overrides it.
 * @param sent True for outgoing messages (aligned end, primary bubble).
 * @param time Optional timestamp shown under the bubble in `labelSmall`.
 * @param authorName Optional sender name shown above the bubble.
 * @param failed True when delivery failed — shows an error-colored label.
 */
data class Message(
    val id: String,
    val text: String,
    val sent: Boolean,
    val time: String? = null,
    val authorName: String? = null,
    val failed: Boolean = false,
)

/**
 * Messages / chat bubble list (PRD §7 "Messaging" P2 / Appendix A "Messages").
 *
 * Lazy-column of bubbles: outgoing [Message.sent] rows align to the end in
 * `primary`, received rows align to the start in `surfaceVariant`; corners are
 * token-shaped and asymmetric — the corner on the sender's side tightens
 * (16dp elsewhere, 8dp on DesktopUI7). A time (`labelSmall`) and an
 * error-colored "Not delivered" line sit under each bubble, and the list
 * auto-scrolls to the newest message.
 *
 * ```kotlin
 * MessageList(
 *     messages = chat,
 *     dateSeparator = { msg -> if (msg.firstOfDay) msg.dayLabel else null },
 *     avatar = { msg -> Avatar(msg.authorName) },
 * )
 * ```
 *
 * @param messages Conversation to render (newest last).
 * @param state Lazy list state; auto-scrolls to the last message when
 *   [messages] grows.
 * @param dateSeparator Called for every message; return a non-null label to
 *   render a centered pill separator above it (return null between messages
 *   of the same date).
 * @param avatar Optional leading/trailing avatar slot, placed on the message's
 *   own outer edge (start for received, end for sent).
 * @param bubbleContent Overrides the default `Message.text` rendering inside
 *   the bubble; surrounding chrome (avatar, time, failure) stays intact.
 */
@Composable
fun MessageList(
    messages: List<Message>,
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    dateSeparator: ((Message) -> String?)? = null,
    avatar: (@Composable (Message) -> Unit)? = null,
    bubbleContent: (@Composable (Message) -> Unit)? = null,
) {
    if (messages.isNotEmpty()) {
        LaunchedEffect(messages.size) {
            state.animateScrollToItem(messages.lastIndex)
        }
    }

    LazyColumn(
        modifier = modifier,
        state = state,
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        items(messages, key = { it.id }) { message ->
            val separator = dateSeparator?.invoke(message)
            if (separator != null) {
                DateSeparator(label = separator)
            }
            MessageRow(
                message = message,
                avatar = avatar,
                bubbleContent = bubbleContent,
            )
        }
    }
}

/** Centered `surfaceVariant` pill used between chat days. */
@Composable
private fun DateSeparator(label: String) {
    val colors = Compose7ThemeColors
    val type = Compose7ThemeTypography
    val shapes = Compose7ThemeShapes

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .background(colors.surfaceVariant, shapes.badge)
                .padding(horizontal = 10.dp, vertical = 4.dp),
        ) {
            BasicText(
                text = label,
                style = type.labelSmall.copy(color = colors.onSurfaceVariant),
            )
        }
    }
}

/** One message row: outer edge avatar, bubble column, style-correct alignment. */
@Composable
private fun MessageRow(
    message: Message,
    avatar: (@Composable (Message) -> Unit)?,
    bubbleContent: (@Composable (Message) -> Unit)?,
) {
    val colors = Compose7ThemeColors
    val type = Compose7ThemeTypography
    val shapes = Compose7ThemeShapes
    val style = currentCompose7Style
    val insets = Compose7StyleInfo
    val isLtr = LocalLayoutDirection.current == LayoutDirection.Ltr

    val bubbleColor: Color
    val bubbleContentColor: Color
    if (message.sent) {
        bubbleColor = colors.primary
        bubbleContentColor = colors.onPrimary
    } else {
        bubbleColor = colors.surfaceVariant
        bubbleContentColor = colors.onSurfaceVariant
    }

    // Tight corner sits on the sender's own (outer) side of the bubble.
    val tightTopStart = if (message.sent) !isLtr else isLtr
    val bigCorner = if (style == Compose7Style.DesktopUI7) shapes.medium.topStart else shapes.large.topStart
    val smallCorner = shapes.extraSmall.topStart
    val bubbleShape = RoundedCornerShape(
        topStart = if (tightTopStart) smallCorner else bigCorner,
        topEnd = if (tightTopStart) bigCorner else smallCorner,
        bottomStart = bigCorner,
        bottomEnd = bigCorner,
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = insets.listContentInset),
        horizontalArrangement = Arrangement.spacedBy(
            8.dp,
            if (message.sent) Alignment.End else Alignment.Start,
        ),
        verticalAlignment = Alignment.Top,
    ) {
        if (avatar != null && !message.sent) {
            avatar(message)
        }
        Column(
            modifier = Modifier.widthIn(max = 320.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp),
            horizontalAlignment = if (message.sent) Alignment.End else Alignment.Start,
        ) {
            if (message.authorName != null) {
                BasicText(
                    text = message.authorName,
                    style = type.labelMedium.copy(color = colors.onSurfaceVariant),
                )
            }
            Box(
                modifier = Modifier
                    .background(bubbleColor, bubbleShape)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            ) {
                if (bubbleContent != null) {
                    bubbleContent(message)
                } else {
                    BasicText(
                        text = message.text,
                        style = type.bodyLarge.copy(color = bubbleContentColor),
                    )
                }
            }
            if (message.time != null || message.failed) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (message.time != null) {
                        BasicText(
                            text = message.time,
                            style = type.labelSmall.copy(color = colors.onSurfaceVariant),
                        )
                    }
                    if (message.failed) {
                        BasicText(
                            text = "Not delivered",
                            style = type.labelSmall.copy(color = colors.error),
                        )
                    }
                }
            }
        }
        if (avatar != null && message.sent) {
            avatar(message)
        }
    }
}

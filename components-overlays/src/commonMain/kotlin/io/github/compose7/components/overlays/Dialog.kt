package io.github.compose7.components.overlays

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import io.github.compose7.core.nativebridge.resolveNativeBridge
import io.github.compose7.core.theme.Compose7ThemeColors
import io.github.compose7.core.theme.Compose7ThemeShapes
import io.github.compose7.core.theme.Compose7ThemeTypography
import io.github.compose7.core.theme.Compose7MotionTokens
import io.github.compose7.core.theme.currentCompose7Style
import io.github.compose7.core.theme.Compose7Style
import io.github.compose7.foundation.Compose7Surface
import io.github.compose7.foundation.compose7Clickable

/** Severity of a [DialogAction], mapped to each style's own vocabulary (PRD §7: "including a destructive style"). */
enum class DialogActionRole { Default, Confirm, Destructive, Cancel }

/**
 * Data-driven action button — the same list renders a Compose7 alert in pure
 * mode and feeds the native `UIAlertController` on the iOS bridge (PRD §8.7:
 * the native path renders data only).
 */
data class DialogAction(
    val text: String,
    val onClick: () -> Unit,
    val role: DialogActionRole = DialogActionRole.Default,
    val enabled: Boolean = true,
)

/**
 * Modal alert (PRD §7, P0 / Appendix A "Dialog / Alert").
 *
 * Title, message, and 1–3 action buttons with a destructive style. When
 * `title`/`message`/`actions` are used (no custom [content]) the iOS Native
 * Interop Bridge may render a real `UIAlertController` (PRD §8.7); a custom
 * [content] slot always falls back to pure Compose so there is no silent
 * divergence between the two paths.
 *
 * ```kotlin
 * if (showDialog) {
 *     Dialog(
 *         onDismissRequest = { showDialog = false },
 *         title = "Delete photo?",
 *         message = "This can't be undone.",
 *         actions = listOf(
 *             DialogAction("Cancel", { showDialog = false }, DialogActionRole.Cancel),
 *             DialogAction("Delete", { delete() }, DialogActionRole.Destructive),
 *         ),
 *     )
 * }
 * ```
 */
@Composable
fun Dialog(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    message: String? = null,
    actions: List<DialogAction> = emptyList(),
    nativeBridge: Boolean? = null,
    content: (@Composable ColumnScope.() -> Unit)? = null,
) {
    val useNative = resolveNativeBridge(nativeBridge) && content == null
    if (useNative) {
        NativeDialog(
            onDismissRequest = onDismissRequest,
            title = title,
            message = message,
            actions = actions,
        )
    } else {
        PureDialog(
            onDismissRequest = onDismissRequest,
            modifier = modifier,
            title = title,
            message = message,
            actions = actions,
            content = content,
        )
    }
}

@Composable
internal fun PureDialog(
    onDismissRequest: () -> Unit,
    modifier: Modifier,
    title: String?,
    message: String?,
    actions: List<DialogAction>,
    content: (@Composable ColumnScope.() -> Unit)?,
) {
    val style = currentCompose7Style
    val motion = Compose7MotionTokens
    val shape = Compose7ThemeShapes.dialog

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        AnimatedVisibility(
            visible = true,
            enter = fadeIn(tween(motion.mediumDurationMs, easing = motion.emphasizedEasing)) +
                if (style == Compose7Style.Cupertino) {
                    scaleIn(initialScale = 1.08f, animationSpec = tween(motion.mediumDurationMs, easing = motion.emphasizedEasing))
                } else {
                    slideInVertically(
                        animationSpec = tween(motion.mediumDurationMs, easing = motion.emphasizedEasing),
                        initialOffsetY = { it / 6 },
                    ) + scaleIn(initialScale = 0.92f, animationSpec = tween(motion.mediumDurationMs, easing = motion.emphasizedEasing))
                },
            exit = fadeOut(tween(motion.fastDurationMs, easing = motion.standardEasing)) +
                scaleOut(targetScale = 0.96f, animationSpec = tween(motion.fastDurationMs, easing = motion.standardEasing)),
        ) {
            DialogCard(
                modifier = modifier,
                style = style,
                shape = shape,
                title = title,
                message = message,
                actions = actions,
                content = content,
            )
        }
    }
}

@Composable
private fun DialogCard(
    modifier: Modifier,
    style: Compose7Style,
    shape: CornerBasedShape,
    title: String?,
    message: String?,
    actions: List<DialogAction>,
    content: (@Composable ColumnScope.() -> Unit)?,
) {
    val colors = Compose7ThemeColors
    val type = Compose7ThemeTypography
    val centered = style == Compose7Style.Cupertino

    val maxWidth = when (style) {
        Compose7Style.Cupertino -> 270.dp
        Compose7Style.DesktopUI7 -> 440.dp
        else -> 312.dp
    }

    Compose7Surface(
        modifier = modifier.widthIn(max = maxWidth),
        shape = shape,
        color = colors.surface,
        contentColor = colors.onSurface,
        border = if (style == Compose7Style.DesktopUI7) {
            BorderStroke(1.dp, colors.outlineVariant)
        } else null,
        elevation = if (style == Compose7Style.DesktopUI7) 8.dp else 0.dp,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(
                    horizontal = if (centered) 16.dp else 24.dp,
                    vertical = if (centered) 16.dp else 20.dp,
                ),
                horizontalAlignment = if (centered) Alignment.CenterHorizontally else Alignment.Start,
            ) {
                if (title != null) {
                    androidx.compose.foundation.text.BasicText(
                        text = title,
                        style = (if (centered) type.titleMedium else type.titleLarge)
                            .copy(color = colors.onSurface, textAlign = if (centered) TextAlign.Center else TextAlign.Start),
                    )
                    if (message != null || content != null || actions.isNotEmpty()) {
                        Spacer(Modifier.height(if (centered) 4.dp else 12.dp))
                    }
                }
                if (message != null) {
                    androidx.compose.foundation.text.BasicText(
                        text = message,
                        style = type.bodyMedium.copy(
                            color = colors.onSurfaceVariant,
                            textAlign = if (centered) TextAlign.Center else TextAlign.Start,
                        ),
                    )
                }
                if (content != null) {
                    if (message != null || title != null) Spacer(Modifier.height(12.dp))
                    content()
                }
            }
            if (actions.isNotEmpty()) {
                DialogActions(style, actions)
            }
        }
    }
}

@Composable
private fun DialogActions(style: Compose7Style, actions: List<DialogAction>) {
    val colors = Compose7ThemeColors
    val type = Compose7ThemeTypography
    val stacked = style == Compose7Style.Cupertino && actions.size > 2

    if (stacked) {
        Column {
            actions.forEachIndexed { index, action ->
                if (index > 0) Hairline(style, horizontal = true)
                DialogActionButton(action, type, colors, Modifier.fillMaxWidth())
            }
        }
    } else {
        Row(modifier = Modifier.fillMaxWidth()) {
            actions.forEachIndexed { index, action ->
                if (index > 0) Hairline(style, horizontal = false)
                Box(modifier = Modifier.weight(1f)) {
                    DialogActionButton(action, type, colors, Modifier.fillMaxWidth())
                }
            }
        }
    }
}

@Composable
private fun DialogActionButton(
    action: DialogAction,
    type: io.github.compose7.core.theme.Compose7Typography,
    colors: io.github.compose7.core.theme.Compose7ColorScheme,
    modifier: Modifier,
) {
    val style = currentCompose7Style
    val centered = style == Compose7Style.Cupertino
    val labelColor = when (action.role) {
        DialogActionRole.Destructive -> colors.error
        DialogActionRole.Default, DialogActionRole.Confirm, DialogActionRole.Cancel -> colors.primary
    }.let { if (action.enabled) it else it.copy(alpha = 0.38f) }

    val height = when (style) {
        Compose7Style.Cupertino -> 44.dp
        Compose7Style.DesktopUI7 -> 36.dp
        else -> 48.dp
    }

    Box(
        modifier = modifier
            .height(height)
            .compose7Clickable(
                enabled = action.enabled,
                onClick = action.onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        androidx.compose.foundation.text.BasicText(
            text = action.text,
            style = when (style) {
                Compose7Style.Cupertino ->
                    if (action.role == DialogActionRole.Cancel) type.labelLarge.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
                    else type.labelLarge
                else -> type.labelLarge
            }.copy(color = labelColor, textAlign = TextAlign.Center),
        )
    }
}

@Composable
private fun Hairline(style: Compose7Style, horizontal: Boolean) {
    val colors = Compose7ThemeColors
    if (horizontal) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(colors.outlineVariant)
        )
    } else {
        Box(
            Modifier
                .width(1.dp)
                .background(colors.outlineVariant)
        )
    }
}

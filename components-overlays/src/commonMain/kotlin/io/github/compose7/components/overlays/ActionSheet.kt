package io.github.compose7.components.overlays

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.BasicText
import androidx.compose.ui.text.font.FontWeight
import io.github.compose7.core.nativebridge.resolveNativeBridge
import io.github.compose7.core.theme.Compose7MotionTokens
import io.github.compose7.core.theme.Compose7ColorScheme
import io.github.compose7.core.theme.Compose7Style
import io.github.compose7.core.theme.Compose7Typography
import io.github.compose7.core.theme.Compose7ThemeColors
import io.github.compose7.core.theme.Compose7ThemeShapes
import io.github.compose7.core.theme.Compose7ThemeTypography
import io.github.compose7.core.theme.currentCompose7Style
import io.github.compose7.foundation.Compose7Surface
import io.github.compose7.foundation.compose7Clickable

/** Severity of an [ActionSheetAction], mirroring each style's own vocabulary. */
enum class ActionSheetActionRole { Default, Destructive, Cancel }

/**
 * Data-driven sheet action — feeds both the pure-Compose sheet and the native
 * `UIAlertController` action sheet on the iOS bridge (PRD §8.7).
 */
data class ActionSheetAction(
    val text: String,
    val onClick: () -> Unit,
    val role: ActionSheetActionRole = ActionSheetActionRole.Default,
    val enabled: Boolean = true,
)

/**
 * Bottom action sheet (PRD §7, P0 / Appendix A "Action Sheet / Actions").
 *
 * A list of actions presented from the bottom edge with a scrim; the cancel
 * action is visually separated per platform convention. With data-only
 * parameters the iOS bridge may render a real `UIAlertController`
 * (action-sheet style, PRD §8.7); a custom [content] slot always uses the
 * pure-Compose implementation.
 *
 * ```kotlin
 * ActionSheet(
 *     visible = showSheet,
 *     onDismissRequest = { showSheet = false },
 *     title = "Photo",
 *     actions = listOf(
 *         ActionSheetAction("Take Photo", { … }),
 *         ActionSheetAction("Choose File", { … }),
 *         ActionSheetAction("Cancel", { showSheet = false }, ActionSheetActionRole.Cancel),
 *     ),
 * )
 * ```
 */
@Composable
fun ActionSheet(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    message: String? = null,
    actions: List<ActionSheetAction> = emptyList(),
    nativeBridge: Boolean? = null,
    content: (@Composable ColumnScope.() -> Unit)? = null,
) {
    val useNative = resolveNativeBridge(nativeBridge) && content == null && visible
    if (useNative) {
        NativeActionSheet(
            onDismissRequest = onDismissRequest,
            title = title,
            message = message,
            actions = actions,
        )
    } else {
        PureActionSheet(
            visible = visible,
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
internal fun PureActionSheet(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier,
    title: String?,
    message: String?,
    actions: List<ActionSheetAction>,
    content: (@Composable ColumnScope.() -> Unit)?,
) {
    val style = currentCompose7Style
    val colors = Compose7ThemeColors
    val motion = Compose7MotionTokens

    if (!visible) return

    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismissRequest,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.scrim.copy(alpha = 0.4f))
                .compose7Clickable(onClick = onDismissRequest, haptic = null),
            contentAlignment = Alignment.BottomCenter,
        ) {
            AnimatedVisibility(
                visible = true,
                enter = slideInVertically(
                    animationSpec = tween(motion.mediumDurationMs, easing = motion.emphasizedEasing),
                    initialOffsetY = { it },
                ) + fadeIn(tween(motion.mediumDurationMs, easing = motion.emphasizedEasing)),
                exit = slideOutVertically(
                    animationSpec = tween(motion.fastDurationMs, easing = motion.standardEasing),
                    targetOffsetY = { it },
                ) + fadeOut(tween(motion.fastDurationMs, easing = motion.standardEasing)),
            ) {
                SheetContent(
                    modifier = modifier,
                    style = style,
                    colors = colors,
                    title = title,
                    message = message,
                    actions = actions,
                    content = content,
                    onDismissRequest = onDismissRequest,
                )
            }
        }
    }
}

@Composable
private fun SheetContent(
    modifier: Modifier,
    style: Compose7Style,
    colors: Compose7ColorScheme,
    title: String?,
    message: String?,
    actions: List<ActionSheetAction>,
    content: (@Composable ColumnScope.() -> Unit)?,
    onDismissRequest: () -> Unit,
) {
    val type = Compose7ThemeTypography
    val shapes = Compose7ThemeShapes
    val cupertino = style == Compose7Style.Cupertino

    // Cupertino insets the sheet from all edges; Material/edge-to-edge styles
    // hug the bottom; DesktopUI7 floats a compact menu above the bottom edge.
    val sideInset = when (style) {
        Compose7Style.Cupertino -> 12.dp
        Compose7Style.DesktopUI7 -> 16.dp
        else -> 0.dp
    }
    val bottomInset = when (style) {
        Compose7Style.Cupertino -> 12.dp
        Compose7Style.DesktopUI7 -> 16.dp
        else -> 0.dp
    }

    val main = actions.filter { it.role != ActionSheetActionRole.Cancel }
    val cancel = actions.firstOrNull { it.role == ActionSheetActionRole.Cancel }

    Column(
        modifier = modifier
            .padding(start = sideInset, end = sideInset, bottom = bottomInset)
            .navigationBarsPadding()
            .widthIn(max = 480.dp)
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(if (cupertino) 8.dp else 0.dp),
    ) {
        if (title != null || message != null || content != null || main.isNotEmpty()) {
            Compose7Surface(
                shape = shapes.sheet,
                color = if (cupertino) colors.surfaceVariant else colors.surface,
                contentColor = colors.onSurface,
                border = if (style == Compose7Style.DesktopUI7) BorderStroke(1.dp, colors.outlineVariant) else null,
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    if (title != null || message != null) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            if (title != null) {
                                BasicText(
                                    text = title,
                                    style = type.titleSmall.copy(
                                        color = colors.onSurfaceVariant,
                                        textAlign = TextAlign.Center,
                                    ),
                                )
                            }
                            if (message != null) {
                                if (title != null) Spacer(Modifier.height(2.dp))
                                BasicText(
                                    text = message,
                                    style = type.bodySmall.copy(
                                        color = colors.onSurfaceVariant,
                                        textAlign = TextAlign.Center,
                                    ),
                                )
                            }
                        }
                        SheetHairline(colors, style)
                    }
                    main.forEachIndexed { index, action ->
                        if (index > 0) SheetHairline(colors, style)
                        ActionRow(action, type, colors, style, Modifier.fillMaxWidth())
                    }
                    content?.invoke(this)
                }
            }
        }
        if (cancel != null) {
            Compose7Surface(
                shape = shapes.sheet,
                color = if (cupertino) colors.surfaceVariant else colors.surface,
                contentColor = colors.onSurface,
                border = if (style == Compose7Style.DesktopUI7) BorderStroke(1.dp, colors.outlineVariant) else null,
            ) {
                ActionRow(cancel, type, colors, style, Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun ActionRow(
    action: ActionSheetAction,
    type: Compose7Typography,
    colors: Compose7ColorScheme,
    style: Compose7Style,
    modifier: Modifier,
) {
    val height = when (style) {
        Compose7Style.Cupertino -> 50.dp
        Compose7Style.DesktopUI7 -> 36.dp
        else -> 52.dp
    }
    val labelColor = when {
        !action.enabled -> colors.onSurface.copy(alpha = 0.38f)
        action.role == ActionSheetActionRole.Destructive -> colors.error
        else -> colors.onSurface
    }
    Box(
        modifier = modifier
            .height(height)
            .compose7Clickable(enabled = action.enabled, onClick = action.onClick),
        contentAlignment = Alignment.Center,
    ) {
        BasicText(
            text = action.text,
            style = type.labelLarge.copy(
                color = labelColor,
                textAlign = TextAlign.Center,
                fontWeight = if (action.role == ActionSheetActionRole.Cancel) FontWeight.SemiBold else FontWeight.Medium,
            ),
        )
    }
}

@Composable
private fun SheetHairline(colors: Compose7ColorScheme, style: Compose7Style) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(colors.outlineVariant)
    )
}

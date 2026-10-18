package io.github.compose7.components.messaging

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.compose7.core.haptics.Compose7Haptic
import io.github.compose7.core.theme.Compose7Style
import io.github.compose7.core.theme.Compose7ThemeColors
import io.github.compose7.core.theme.Compose7ThemeShapes
import io.github.compose7.core.theme.Compose7ThemeTypography
import io.github.compose7.core.theme.currentCompose7Style
import io.github.compose7.foundation.Divider
import io.github.compose7.foundation.compose7Clickable
import io.github.compose7.foundation.currentMinTouchTarget

/**
 * Messagebar (PRD §7 "Messaging" P2 / Appendix A "Messagebar").
 *
 * Chat composer: a growing `BasicTextField` in a style-correct container —
 * Cupertino gets a pill input on a hairline-topped bar, Material a filled
 * `surfaceVariant` field, DesktopUI7 a bordered one. The field grows to about
 * five lines (then scrolls internally). When [onSubmit] is set and [value] is
 * not blank, a send affordance appears: [sendButton] if provided, otherwise a
 * minimal token-styled pill labelled "Send" (text — no icons, PRD §4).
 *
 * ```kotlin
 * MessageBar(
 *     value = draft,
 *     onValueChange = { draft = it },
 *     onSubmit = { send(draft); draft = "" },
 * )
 * ```
 *
 * @param value Current input text (controlled — PRD §11).
 * @param onValueChange Receives every edit.
 * @param placeholder Hint shown while the field is empty.
 * @param onSubmit Invoked by the send affordance; null hides it entirely.
 * @param minHeight Field height, defaulting to the platform minimum touch
 *   target (44pt Cupertino / 48dp elsewhere).
 * @param sendEnabled Whether the default send pill is interactive; it always
 *   renders in a disabled style when false.
 * @param sendButton Replaces the default "Send" pill — wire [onSubmit]
 *   yourself; only shown while [onSubmit] is set and [value] is not blank.
 * @param leading Slot rendered before the input (attach button, etc.).
 * @param trailing Slot rendered after the send affordance (voice, emoji, …).
 * @param applySafeArea Pads the bottom by the navigation-bar inset so the bar
 *   docks above home indicators.
 */
@Composable
fun MessageBar(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Message",
    onSubmit: (() -> Unit)? = null,
    minHeight: Dp = currentMinTouchTarget(),
    sendEnabled: Boolean = true,
    sendButton: (@Composable () -> Unit)? = null,
    leading: (@Composable RowScope.() -> Unit)? = null,
    trailing: (@Composable RowScope.() -> Unit)? = null,
    applySafeArea: Boolean = true,
) {
    val colors = Compose7ThemeColors
    val type = Compose7ThemeTypography
    val shapes = Compose7ThemeShapes
    val style = currentCompose7Style

    val inputShape = when (style) {
        Compose7Style.Cupertino -> shapes.chip
        Compose7Style.DesktopUI7 -> shapes.input
        else -> shapes.button
    }
    val inputBorder: BorderStroke? = when (style) {
        Compose7Style.DesktopUI7 -> BorderStroke(1.dp, colors.outlineVariant)
        else -> null
    }
    // minHeight covers the first line; four more steps cap growth at ~5 lines.
    val fieldMaxHeight = minHeight + MessageBarLineStep * MessageBarExtraLines

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.surface)
            .then(
                if (applySafeArea) {
                    Modifier.windowInsetsPadding(
                        WindowInsets.navigationBars.only(WindowInsetsSides.Bottom),
                    )
                } else {
                    Modifier
                },
            ),
    ) {
        if (style == Compose7Style.Cupertino) {
            Divider(insetStart = 0.dp, insetEnd = 0.dp, color = colors.outlineVariant)
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            leading?.invoke(this)

            Box(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = minHeight, max = fieldMaxHeight)
                    .clip(inputShape)
                    .background(
                        if (style == Compose7Style.DesktopUI7) colors.surface
                        else colors.surfaceVariant,
                    )
                    .let { if (inputBorder != null) it.border(inputBorder, inputShape) else it }
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    textStyle = type.bodyLarge.copy(color = colors.onSurface),
                    cursorBrush = SolidColor(colors.primary),
                    decorationBox = { innerTextField ->
                        Box {
                            if (value.isEmpty()) {
                                BasicText(
                                    text = placeholder,
                                    style = type.bodyLarge.copy(color = colors.onSurfaceVariant),
                                )
                            }
                            innerTextField()
                        }
                    },
                )
            }

            if (onSubmit != null && value.isNotBlank()) {
                if (sendButton != null) {
                    sendButton()
                } else {
                    DefaultSendButton(
                        enabled = sendEnabled,
                        height = minHeight,
                        onClick = onSubmit,
                    )
                }
            }

            trailing?.invoke(this)
        }
    }
}

/** Default text-only send pill (PRD §4: never an icon asset). */
@Composable
private fun DefaultSendButton(
    enabled: Boolean,
    height: Dp,
    onClick: () -> Unit,
) {
    val colors = Compose7ThemeColors
    val type = Compose7ThemeTypography
    val shapes = Compose7ThemeShapes
    val interaction = remember { MutableInteractionSource() }

    val container = if (enabled) colors.primary else colors.onSurface.copy(alpha = 0.12f)
    val labelColor = if (enabled) colors.onPrimary else colors.onSurface.copy(alpha = 0.38f)

    Row(
        modifier = Modifier
            .height(height)
            .background(container, shapes.button)
            .compose7Clickable(
                interactionSource = interaction,
                enabled = enabled,
                role = Role.Button,
                haptic = Compose7Haptic.Light,
                shape = shapes.button,
                onClick = onClick,
            )
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BasicText(
            text = "Send",
            style = type.labelLarge.copy(color = labelColor),
        )
    }
}

/** One line of body type inside the field — part of the ~5-line growth cap. */
private val MessageBarLineStep = 24.dp

/** Lines above the first that the field may grow into. */
private val MessageBarExtraLines = 4

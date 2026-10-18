package io.github.compose7.components.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import io.github.compose7.core.haptics.Compose7Haptic
import io.github.compose7.core.theme.Compose7MotionTokens
import io.github.compose7.core.theme.Compose7Style
import io.github.compose7.core.theme.Compose7ThemeColors
import io.github.compose7.core.theme.Compose7ThemeShapes
import io.github.compose7.core.theme.Compose7ThemeTypography
import io.github.compose7.core.theme.currentCompose7Style
import io.github.compose7.foundation.Compose7Surface
import io.github.compose7.foundation.compose7Clickable

/**
 * Search input chrome (PRD §7 P0 / Appendix A "SearchBar"): a styled
 * [BasicTextField] with a drawn magnifier glyph (no icon assets — PRD §4),
 * a drawn clear (×) button while non-empty, and an optional iOS-style
 * Cancel action that appears on focus.
 *
 * Style branch (PRD §9): Cupertino — 10dp-rounded surfaceVariant pill with
 * a Cancel text action on focus; Material — rounded filled surfaceVariant
 * container with a leading glyph (Material search-bar look); DesktopUI7 —
 * bordered field on surface. The IME action is Search; [onSubmit] fires on
 * its keyboard action.
 *
 * The field keeps its platform-native visual height (32–40dp); the hit area
 * is the full field width.
 *
 * ```kotlin
 * Searchbar(query = query, onQueryChange = { query = it }, onCancel = { clearFocus() })
 * ```
 */
@Composable
fun Searchbar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    onSubmit: (() -> Unit)? = null,
    onClear: (() -> Unit)? = null,
    onCancel: (() -> Unit)? = null,
    placeholder: String = "Search",
    cancelLabel: String = "Cancel",
    enabled: Boolean = true,
    containerColor: Color? = null,
    contentColor: Color? = null,
) {
    val style = currentCompose7Style
    val colors = Compose7ThemeColors
    val type = Compose7ThemeTypography
    val motion = Compose7MotionTokens

    val shape = when (style) {
        Compose7Style.Material -> Compose7ThemeShapes.chip
        else -> Compose7ThemeShapes.input
    }
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    val focusRequester = remember { FocusRequester() }
    val background = containerColor ?: if (style == Compose7Style.DesktopUI7) {
        colors.surface
    } else {
        colors.surfaceVariant
    }
    val content = contentColor ?: colors.onSurface
    val border = if (style == Compose7Style.DesktopUI7) {
        BorderStroke(1.dp, colors.outlineVariant)
    } else {
        null
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Compose7Surface(
            modifier = Modifier.weight(1f),
            shape = shape,
            color = background,
            contentColor = content,
            border = border,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(searchbarHeight(style))
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                MagnifierGlyph(color = colors.onSurfaceVariant)
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(focusRequester),
                    enabled = enabled,
                    textStyle = type.bodyLarge.copy(color = content),
                    singleLine = true,
                    interactionSource = interaction,
                    cursorBrush = SolidColor(colors.primary),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { onSubmit?.invoke() }),
                    decorationBox = { innerTextField ->
                        Box {
                            if (query.isEmpty()) {
                                BasicText(
                                    text = placeholder,
                                    style = type.bodyLarge.copy(color = colors.onSurfaceVariant),
                                )
                            }
                            innerTextField()
                        }
                    },
                )
                if (query.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .compose7Clickable(
                                role = Role.Button,
                                onClick = {
                                    onQueryChange("")
                                    onClear?.invoke()
                                },
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        CloseGlyph(color = colors.onSurfaceVariant)
                    }
                }
            }
        }
        AnimatedVisibility(
            visible = focused && onCancel != null,
            enter = expandHorizontally(
                tween(motion.fastDurationMs, easing = motion.standardEasing),
            ) + fadeIn(tween(motion.fastDurationMs, easing = motion.standardEasing)),
            exit = shrinkHorizontally(
                tween(motion.fastDurationMs, easing = motion.standardEasing),
            ) + fadeOut(tween(motion.fastDurationMs, easing = motion.standardEasing)),
        ) {
            BasicText(
                text = cancelLabel,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .compose7Clickable(
                        haptic = Compose7Haptic.Light,
                        onClick = {
                            onCancel?.invoke()
                            focusRequester.freeFocus()
                        },
                    ),
                style = type.labelLarge.copy(color = colors.primary),
            )
        }
    }
}

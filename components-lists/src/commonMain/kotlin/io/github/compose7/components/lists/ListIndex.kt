package io.github.compose7.components.lists

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.unit.dp
import io.github.compose7.core.haptics.Compose7Haptic
import io.github.compose7.core.theme.Compose7MotionTokens
import io.github.compose7.core.theme.Compose7ThemeColors
import io.github.compose7.core.theme.Compose7ThemeTypography
import io.github.compose7.foundation.compose7Clickable

/**
 * Fast-scroll alphabet index (PRD §7, P1 / Appendix A "List Index").
 *
 * A pure-text vertical strip (no icon assets, PRD §4) that the caller
 * overlays at the end edge of a list — e.g.
 * `Box { ContactsList(...); ListIndex(letters, onLetterSelected, Modifier.align(Alignment.CenterEnd)) }`.
 * Tapping or dragging over a letter fires [onLetterSelected] with a selection
 * haptic, highlights the letter, and (optionally) shows a floating bubble with
 * the current letter.
 *
 * @param letters Entries to render, top to bottom ("#", "A", "Z", …).
 * @param onLetterSelected Called with the tapped letter; typically scrolls the
 *   accompanying list to that section.
 * @param showBubble Shows the floating selected-letter bubble beside the strip.
 */
@Composable
fun ListIndex(
    letters: List<String>,
    onLetterSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    showBubble: Boolean = true,
) {
    val colors = Compose7ThemeColors
    val type = Compose7ThemeTypography
    val motion = Compose7MotionTokens
    var selected by remember { mutableStateOf<String?>(null) }

    Box(modifier = modifier) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            letters.forEach { letter ->
                val isSelected = letter == selected
                BasicText(
                    text = letter,
                    style = type.labelSmall.copy(
                        color = if (isSelected) colors.primary else colors.onSurfaceVariant,
                    ),
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 2.dp)
                        .compose7Clickable(haptic = Compose7Haptic.Selection) {
                            selected = letter
                            onLetterSelected(letter)
                        },
                )
            }
        }
        if (showBubble) {
            Box(modifier = Modifier.align(Alignment.CenterStart)) {
                AnimatedVisibility(
                    visible = selected != null,
                    enter = fadeIn(tween(motion.fastDurationMs, easing = motion.standardEasing)),
                    exit = fadeOut(tween(motion.fastDurationMs, easing = motion.standardEasing)),
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(colors.primary, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        BasicText(
                            text = selected.orEmpty(),
                            style = type.titleMedium.copy(color = colors.onPrimary),
                        )
                    }
                }
            }
        }
    }
}

package io.github.compose7.core.haptics

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import io.github.compose7.core.theme.LocalCompose7StyleProperties

/**
 * The kinds of haptic feedback Compose7 components emit.
 *
 * Mapping is platform-appropriate rather than identical: iOS maps every kind
 * to its nearest `UIFeedbackGenerator` style, Android to the closest
 * `HapticFeedbackConstants` value, desktop ignores haptics entirely (and the
 * [io.github.compose7.core.theme.Compose7StyleProperties.usesHapticFeedback]
 * flag is false for DesktopUI7, so nothing is attempted).
 */
enum class Compose7Haptic {
    /** Selection change — toggles, checkboxes, steppers. */
    Selection,

    /** Light tap — buttons, rows, segmented options. */
    Light,

    /** Medium impact — sheet open, drag snap. */
    Medium,

    /** Heavy impact — destructive confirm. */
    Heavy,

    /** Positive completion (maps to the nearest available success cue). */
    Success,

    /** Negative completion (maps to the nearest available warning cue). */
    Error,
}

/**
 * Returns a handler that emits [Compose7Haptic] feedback, or a no-op when the
 * active style opts out of haptics (PRD §6: accessibility & platform feel are
 * not optional, but desktop pointers don't buzz).
 *
 * ```kotlin
 * val haptic = rememberHapticFeedback()
 * Button(onClick = { haptic(Compose7Haptic.Light); submit() })
 * ```
 */
@Composable
fun rememberHapticFeedback(): (Compose7Haptic) -> Unit {
    val haptics = LocalHapticFeedback.current
    val enabled = LocalCompose7StyleProperties.current.usesHapticFeedback
    return remember(haptics, enabled) {
        { kind: Compose7Haptic ->
            if (enabled) {
                val type = when (kind) {
                    Compose7Haptic.Selection -> HapticFeedbackType.TextHandleMove
                    else -> HapticFeedbackType.LongPress
                }
                haptics.performHapticFeedback(type)
            }
        }
    }
}

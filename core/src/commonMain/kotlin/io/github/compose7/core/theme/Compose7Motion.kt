package io.github.compose7.core.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf

/**
 * Motion tokens for one style: durations plus the two canonical easings
 * components should use ([emphasizedEasing] for entering/overlay motion,
 * [standardEasing] for everything else).
 */
@Immutable
class Compose7Motion(
    /** Press feedback, hover transitions, small state flips. */
    val fastDurationMs: Int,
    /** Expanding surfaces, cross-fades. */
    val mediumDurationMs: Int,
    /** Large structural transitions (panel, sheet entrance). */
    val slowDurationMs: Int,
    val standardEasing: Easing,
    /** For entering & emphasis moments (dialogs, sheets). */
    val emphasizedEasing: Easing,
)

/** The active motion token set. Provided by [Compose7Theme]. */
val LocalCompose7Motion = compositionLocalOf { materialMotion() }

/** Convenience accessor for the active motion token set. */
val Compose7MotionTokens: Compose7Motion
    @Composable get() = LocalCompose7Motion.current

/** Material: 150/300/500ms with M3 emphasized decelerate. */
fun materialMotion() = Compose7Motion(
    fastDurationMs = 150,
    mediumDurationMs = 300,
    slowDurationMs = 500,
    standardEasing = CubicBezierEasing(0.4f, 0f, 0.2f, 1f),
    emphasizedEasing = CubicBezierEasing(0.2f, 0f, 0f, 1f),
)

/** Cupertino: slightly slower, ease curve, sheet-specific emphasized curve. */
fun cupertinoMotion() = Compose7Motion(
    fastDurationMs = 200,
    mediumDurationMs = 350,
    slowDurationMs = 500,
    standardEasing = CubicBezierEasing(0.25f, 0.1f, 0.25f, 1f),
    emphasizedEasing = CubicBezierEasing(0.32f, 0.72f, 0f, 1f),
)

/** DesktopUI7: snappy — 120/200/320ms, ease-out-expo entrances. */
fun desktopUi7Motion() = Compose7Motion(
    fastDurationMs = 120,
    mediumDurationMs = 200,
    slowDurationMs = 320,
    standardEasing = CubicBezierEasing(0.2f, 0f, 0f, 1f),
    emphasizedEasing = CubicBezierEasing(0.16f, 1f, 0.3f, 1f),
)

internal fun motionFor(style: Compose7Style): Compose7Motion = when (style) {
    Compose7Style.Material -> materialMotion()
    Compose7Style.Cupertino -> cupertinoMotion()
    Compose7Style.DesktopUI7 -> desktopUi7Motion()
    Compose7Style.Auto -> materialMotion()
}

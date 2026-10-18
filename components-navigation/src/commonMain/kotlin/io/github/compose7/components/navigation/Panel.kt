package io.github.compose7.components.navigation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import io.github.compose7.core.haptics.Compose7Haptic
import io.github.compose7.core.theme.Compose7MotionTokens
import io.github.compose7.core.theme.Compose7ThemeColors
import io.github.compose7.core.theme.currentCompose7Style
import io.github.compose7.foundation.Compose7Surface
import io.github.compose7.foundation.cardBorder
import io.github.compose7.foundation.cardElevation
import io.github.compose7.foundation.compose7Clickable

/** Which screen edge a [Panel] docks to — start/end so RTL mirrors automatically (PRD §12). */
enum class PanelSide { Start, End }

/** Presentation mode of a [Panel] (PRD §8.2). */
enum class PanelPresentation {
    /** Slides over the app content behind a tap-to-dismiss scrim. */
    Overlay,

    /** Pushes the app content aside (pass [Panel.appContent]); no scrim. */
    Reveal,
}

/**
 * Side drawer (PRD §8.2 / Appendix A "Panel") with reveal and overlay
 * presentation modes.
 *
 * Place it as the last child of a root `Box` — it draws a full-size overlay
 * and consumes nothing else. For [PanelPresentation.Reveal], also pass
 * [appContent]: the app subtree is translated by the panel width while open.
 * The scrim dismisses on tap by calling `onVisibleChange(false)` — wire your
 * platform back handler to the same callback (no back-dispatch exists in
 * commonMain).
 *
 * Slides in with the slow motion duration and emphasized easing; scrim
 * alpha animates separately. Side is expressed as start/end for automatic
 * RTL mirroring (PRD §12).
 *
 * ```kotlin
 * Box {
 *     AppContent()
 *     Panel(visible = drawerOpen, onVisibleChange = { drawerOpen = it }) {
 *         Text("Menu")
 *     }
 * }
 * ```
 */
@Composable
fun Panel(
    visible: Boolean,
    onVisibleChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    side: PanelSide = PanelSide.Start,
    presentation: PanelPresentation = PanelPresentation.Overlay,
    panelWidth: Dp = defaultPanelWidth(currentCompose7Style),
    scrimColor: Color = Compose7ThemeColors.scrim,
    appContent: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val motion = Compose7MotionTokens
    val colors = Compose7ThemeColors
    val direction = if (LocalLayoutDirection.current == LayoutDirection.Rtl) -1f else 1f
    val isStart = side == PanelSide.Start
    val overlay = presentation == PanelPresentation.Overlay || appContent == null

    val progress by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(motion.slowDurationMs, easing = motion.emphasizedEasing),
        label = "panel",
    )
    val scrimProgress by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(motion.mediumDurationMs, easing = motion.standardEasing),
        label = "panelScrim",
    )

    if (!visible && progress <= 0f && presentation != PanelPresentation.Reveal) {
        return
    }

    Box(modifier = modifier.fillMaxSize()) {
        if (overlay) {
            if (visible || scrimProgress > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { alpha = scrimProgress }
                        .background(scrimColor)
                        .compose7Clickable(
                            overlayColor = Color.Transparent,
                            haptic = Compose7Haptic.Light,
                            onClick = { onVisibleChange(false) },
                        ),
                )
            }
        } else {
            val shift = panelWidth * progress * direction * (if (isStart) 1f else -1f)
            Box(modifier = Modifier.graphicsLayer { translationX = shift.toPx() }) {
                appContent.invoke()
            }
        }
        if (visible || progress > 0f) {
            val offsetFraction = 1f - progress
            val translation = if (isStart) {
                -panelWidth * offsetFraction * direction
            } else {
                panelWidth * offsetFraction * direction
            }
            Compose7Surface(
                modifier = Modifier
                    .align(if (isStart) Alignment.CenterStart else Alignment.CenterEnd)
                    .fillMaxHeight()
                    .width(panelWidth)
                    .graphicsLayer { translationX = translation.toPx() },
                color = colors.surface,
                contentColor = colors.onSurface,
                elevation = cardElevation(),
                border = cardBorder(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(
                            WindowInsets.safeDrawing.only(
                                if (isStart) WindowInsetsSides.Start else WindowInsetsSides.End,
                            ),
                        ),
                    content = content,
                )
            }
        }
    }
}

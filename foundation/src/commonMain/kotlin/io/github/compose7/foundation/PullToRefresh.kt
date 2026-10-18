package io.github.compose7.foundation

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import io.github.compose7.core.theme.Compose7MotionTokens
import io.github.compose7.core.theme.Compose7ThemeColors
import kotlin.math.roundToInt

/**
 * Pull-to-refresh container (PRD §7 "Scroll behaviors", P1).
 *
 * Composes over any scrollable content: dragging down while the content is at
 * the top stretches an indicator out; releasing past the threshold calls
 * [onRefresh] and holds the indicator up until [isRefreshing] turns `false`.
 * Uses nested scroll rather than its own gesture detector (PRD §8.3: thin
 * wrappers over Compose scroll, no second event engine).
 *
 * ```kotlin
 * PullToRefresh(isRefreshing = loading, onRefresh = viewModel::reload) {
 *     LazyColumn { items(rows) { ListItem(it.title) } }
 * }
 * ```
 *
 * @param indicator Slot for the pull affordance; receives pull progress in
 *   `0f..1f` (capped at 1 at the threshold) and whether a refresh is running.
 */
@Composable
fun PullToRefresh(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    threshold: androidx.compose.ui.unit.Dp = 64.dp,
    indicator: @Composable (progress: Float, refreshing: Boolean) -> Unit = { progress, refreshing ->
        DefaultRefreshIndicator(progress = progress, refreshing = refreshing)
    },
    content: @Composable BoxScope.() -> Unit,
) {
    val motion = Compose7MotionTokens
    val density = LocalDensity.current
    val thresholdPx = with(density) { threshold.toPx() }
    val currentOnRefresh by rememberUpdatedState(onRefresh)
    val currentEnabled by rememberUpdatedState(enabled)
    val currentRefreshing by rememberUpdatedState(isRefreshing)

    var pull by remember { mutableFloatStateOf(0f) }
    var indicatorHeightPx by remember { mutableFloatStateOf(0f) }

    // Retract once the host says the refresh finished (or never started).
    LaunchedEffect(isRefreshing, thresholdPx) {
        if (!isRefreshing && pull > 0f && pull >= thresholdPx) {
            animate(
                initialValue = pull,
                targetValue = 0f,
                animationSpec = tween(motion.mediumDurationMs, easing = motion.emphasizedEasing),
            ) { value, _ -> pull = value }
        }
    }

    val connection = remember(thresholdPx) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                // Dragging back up folds the stretched indicator in first.
                if (currentEnabled && source == NestedScrollSource.UserInput &&
                    available.y < 0f && pull > 0f
                ) {
                    val consumed = available.y.coerceAtLeast(-pull)
                    pull = (pull + consumed).coerceAtLeast(0f)
                    return Offset(0f, consumed)
                }
                return Offset.Zero
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource,
            ): Offset {
                if (currentEnabled && source == NestedScrollSource.UserInput &&
                    available.y > 0f && !currentRefreshing
                ) {
                    // Damped, so the indicator never races the finger.
                    pull = (pull + available.y * 0.5f).coerceAtMost(thresholdPx * 1.5f)
                    return Offset(0f, available.y)
                }
                return Offset.Zero
            }

            override suspend fun onPostFling(
                consumed: Velocity,
                available: Velocity,
            ): Velocity {
                if (!currentEnabled) return Velocity.Zero
                when {
                    pull >= thresholdPx -> {
                        animate(
                            initialValue = pull,
                            targetValue = thresholdPx,
                            animationSpec = tween(motion.fastDurationMs, easing = motion.emphasizedEasing),
                        ) { value, _ -> pull = value }
                        currentOnRefresh()
                    }
                    pull > 0f -> {
                        animate(
                            initialValue = pull,
                            targetValue = 0f,
                            animationSpec = tween(motion.fastDurationMs, easing = motion.standardEasing),
                        ) { value, _ -> pull = value }
                    }
                }
                return Velocity.Zero
            }
        }
    }

    Box(modifier = modifier.nestedScroll(connection)) {
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .onSizeChanged { indicatorHeightPx = it.height.toFloat() }
                .offset {
                    IntOffset(
                        x = 0,
                        y = (pull - indicatorHeightPx).roundToInt(),
                    )
                },
        ) {
            indicator((pull / thresholdPx).coerceIn(0f, 1f), isRefreshing)
        }
        content()
    }
}

/** Drawn refresh affordance: a filling arc that spins while [refreshing]. */
@Composable
private fun DefaultRefreshIndicator(progress: Float, refreshing: Boolean) {
    val colors = Compose7ThemeColors
    val infinite = rememberInfiniteTransition(label = "pullToRefresh")
    val spin by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
        ),
        label = "pullToRefreshSpin",
    )
    val rotation = if (refreshing) spin else progress * 270f
    val visible = refreshing || progress > 0.01f

    Canvas(
        modifier = Modifier.size(32.dp),
    ) {
        if (!visible) return@Canvas
        val stroke = 3.dp.toPx()
        val inset = stroke / 2f
        val arcSize = Size(size.width - stroke, size.height - stroke)
        val sweep = if (refreshing) 270f else 30f + 240f * progress
        drawArc(
            color = colors.outlineVariant,
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = Offset(inset, inset),
            size = arcSize,
            style = Stroke(width = stroke),
        )
        drawArc(
            color = colors.primary,
            startAngle = rotation - 90f,
            sweepAngle = sweep,
            useCenter = false,
            topLeft = Offset(inset, inset),
            size = arcSize,
            style = Stroke(width = stroke),
        )
    }
}

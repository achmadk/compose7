package io.github.compose7.components.media

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import io.github.compose7.core.theme.Compose7MotionTokens
import io.github.compose7.core.theme.Compose7ThemeTypography
import io.github.compose7.foundation.compose7Clickable
import io.github.compose7.foundation.compose7MinimumTouchTarget
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

private const val MaxPhotoZoom = 4f
private const val DismissThresholdFraction = 0.2f
private const val ScrimFadeFactor = 0.7f
private const val CloseDescription = "Close"

/**
 * Fullscreen gallery viewer (PRD §7 P1–P2 / Appendix A "Photo Browser" / §8.6).
 *
 * Renders a full-screen overlay paging over [items] with:
 *  - **pinch-zoom + pan** per page (`transformable` + `graphicsLayer`); the
 *    zoomed page pans inside its bounds and pages do not flip while zoomed;
 *  - **swipe-to-dismiss**: a vertical drag past 20% of the height follows the
 *    finger while the scrim fades, then closes via [onVisibleChange](false).
 *    The dismiss gesture yields to horizontal paging and to zoom (it only
 *    engages while the current page is at 1x);
 *  - **pluggable image slot** — [image] receives the caller's own content plus
 *    a `Modifier` carrying the zoom/pan transforms, so the library never
 *    depends on any image loader (PRD §8.6).
 *
 * The header is caller-supplied via [header]; when absent, a default header
 * with a drawn `×` close affordance (no icon assets, PRD §4) and a "3/12"
 * page counter is shown. Double-tap zoom is intentionally not included in
 * this version.
 *
 * ```kotlin
 * PhotoBrowser(
 *     items = photos,
 *     visible = viewerOpen,
 *     onVisibleChange = { viewerOpen = it },
 *     image = { photo, _, modifier -> AsyncImage(photo.url, modifier = modifier) },
 * )
 * ```
 *
 * @param items The photos to browse; paging is bounded to this list.
 * @param initialPage Page shown first; clamped into the list's bounds.
 * @param visible Whether the browser is on screen. Dismissal is controlled:
 *   the caller owns this flag and hides the browser when it turns false.
 * @param onVisibleChange Reports visibility changes requested by user actions
 *   (close affordance, swipe-to-dismiss).
 * @param image Renders one photo. Apply the provided `Modifier` to the image
 *   content so zoom/pan and sizing work.
 * @param header Optional header slot replacing the default counter + close bar.
 * @param onClose Additional callback whenever the browser dismisses itself in
 *   response to user action (analytics, cleanup); dismissal itself always
 *   goes through [onVisibleChange].
 * @param backgroundColor Backdrop behind the photos. Unlike themed components,
 *   viewer chrome is fixed light-on-dark regardless of style so photos stay
 *   legible in either brightness — the only intentionally non-token color in
 *   the library.
 */
@Composable
fun <T> PhotoBrowser(
    items: List<T>,
    modifier: Modifier = Modifier,
    initialPage: Int = 0,
    visible: Boolean,
    onVisibleChange: (Boolean) -> Unit,
    image: @Composable (item: T, page: Int, Modifier) -> Unit,
    header: (@Composable () -> Unit)? = null,
    onClose: (() -> Unit)? = null,
    backgroundColor: Color = Color.Black,
) {
    val motion = Compose7MotionTokens
    val alpha = remember { Animatable(0f) }
    LaunchedEffect(visible) {
        alpha.animateTo(
            targetValue = if (visible) 1f else 0f,
            animationSpec = tween(motion.mediumDurationMs, easing = motion.emphasizedEasing),
        )
    }

    if (visible || alpha.value > 0.001f) {
        val dismissOffset = remember { mutableFloatStateOf(0f) }
        val dismissDragging = remember { mutableStateOf(false) }
        val currentScale = remember { mutableFloatStateOf(1f) }
        val containerHeight = remember { mutableIntStateOf(1) }
        val settleJob = remember { mutableStateOf<Job?>(null) }
        val scope = rememberCoroutineScope()
        val pagerState = rememberPagerState(
            initialPage = initialPage.coerceIn(0, (items.size - 1).coerceAtLeast(0)),
        ) { items.size }

        // A page that is panned away resets the shared scale so the dismiss
        // gesture re-arms for the page now under the finger.
        LaunchedEffect(pagerState.currentPage) { currentScale.floatValue = 1f }

        val dismiss: () -> Unit = {
            onVisibleChange(false)
            onClose?.invoke()
        }
        val settleBack: () -> Unit = {
            settleJob.value?.cancel()
            settleJob.value = scope.launch {
                animate(
                    initialValue = dismissOffset.floatValue,
                    targetValue = 0f,
                    animationSpec = tween(motion.mediumDurationMs, easing = motion.standardEasing),
                ) { value, _ -> dismissOffset.floatValue = value }
            }
        }

        val dragFraction =
            (dismissOffset.floatValue.absoluteValue / containerHeight.intValue).coerceIn(0f, 1f)
        val scrimAlpha = 1f - dragFraction * ScrimFadeFactor

        Box(
            modifier = modifier
                .fillMaxSize()
                .graphicsLayer { this.alpha = alpha.value }
                .onSizeChanged { containerHeight.intValue = it.height.coerceAtLeast(1) },
        ) {
            // Scrim stays put; the content box below translates over it.
            Box(
                Modifier
                    .fillMaxSize()
                    .background(backgroundColor.copy(alpha = scrimAlpha)),
            )

            Box(
                Modifier
                    .fillMaxSize()
                    .pointerInput(visible) {
                        if (!visible) return@pointerInput
                        detectVerticalDragGestures(
                            onDragStart = {
                                settleJob.value?.cancel()
                                dismissDragging.value = true
                            },
                            onVerticalDrag = { change, dragAmount ->
                                // Engage only at 1x so a zoomed page pans
                                // instead of dragging the browser down.
                                if (currentScale.floatValue == 1f) {
                                    change.consume()
                                    dismissOffset.floatValue += dragAmount
                                }
                            },
                            onDragEnd = {
                                dismissDragging.value = false
                                val threshold = size.height * DismissThresholdFraction
                                if (dismissOffset.floatValue.absoluteValue > threshold) {
                                    dismiss()
                                }
                                settleBack()
                            },
                            onDragCancel = {
                                dismissDragging.value = false
                                settleBack()
                            },
                        )
                    }
                    .graphicsLayer { translationY = dismissOffset.floatValue },
            ) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                ) { page ->
                    PhotoBrowserPage(
                        dismissDragging = dismissDragging,
                        onScaleChanged = { currentScale.floatValue = it },
                    ) { pageModifier ->
                        image(items[page], page, pageModifier)
                    }
                }

                if (header != null) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .align(Alignment.TopCenter)
                            .statusBarsPadding(),
                    ) { header() }
                } else {
                    DefaultPhotoBrowserHeader(
                        pageCount = items.size,
                        page = pagerState.currentPage,
                        onDismiss = dismiss,
                        modifier = Modifier.align(Alignment.TopCenter),
                    )
                }
            }
        }
    }
}

/**
 * One browsable page: the caller's image content wrapped with pinch-zoom + pan
 * (`transformable` over a `graphicsLayer`). State is per page, so navigating
 * away and back resets that page to 1x. Pan is clamped to the zoomed bounds.
 */
@Composable
private fun PhotoBrowserPage(
    dismissDragging: State<Boolean>,
    onScaleChanged: (Float) -> Unit,
    content: @Composable (Modifier) -> Unit,
) {
    val scale = remember { mutableFloatStateOf(1f) }
    val offset = remember { mutableStateOf(Offset.Zero) }
    val pageSize = remember { mutableStateOf(IntSize.Zero) }
    // Centroid-aware overload (the 3-arg one is deprecated in Compose 1.12).
    val transformableState = rememberTransformableState { _, zoom, panChange, _ ->
        if (!dismissDragging.value) {
            val newScale = (scale.floatValue * zoom).coerceIn(1f, MaxPhotoZoom)
            scale.floatValue = newScale
            if (newScale > 1f) {
                val maxOffsetX = pageSize.value.width * (newScale - 1f) / 2f
                val maxOffsetY = pageSize.value.height * (newScale - 1f) / 2f
                val current = offset.value
                offset.value = Offset(
                    (current.x + panChange.x).coerceIn(-maxOffsetX, maxOffsetX),
                    (current.y + panChange.y).coerceIn(-maxOffsetY, maxOffsetY),
                )
            } else {
                offset.value = Offset.Zero
            }
            onScaleChanged(newScale)
        }
    }

    content(
        Modifier
            .fillMaxSize()
            .onSizeChanged { pageSize.value = it }
            .graphicsLayer {
                scaleX = scale.floatValue
                scaleY = scale.floatValue
                translationX = offset.value.x
                translationY = offset.value.y
            }
            .transformable(transformableState),
    )
}

/**
 * Default header: a drawn `×` close affordance (start side, mirrored in RTL)
 * and the "3/12" page counter. No icon assets ever (PRD §4, §11).
 */
@Composable
private fun DefaultPhotoBrowserHeader(
    pageCount: Int,
    page: Int,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PhotoBrowserCloseButton(onClick = onDismiss)
        BasicText(
            text = if (pageCount > 0) "${page + 1}/$pageCount" else "",
            style = Compose7ThemeTypography.labelLarge.copy(color = Color.White),
            modifier = Modifier.padding(horizontal = 12.dp),
        )
    }
}

/** Minimum-touch-target close control with a Canvas-drawn `×`. */
@Composable
private fun PhotoBrowserCloseButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .compose7MinimumTouchTarget()
            .semantics { contentDescription = CloseDescription }
            .compose7Clickable(role = Role.Button, onClick = onClick)
            .padding(12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.size(20.dp)) {
            val stroke = size.minDimension * 0.14f
            val inset = size.minDimension * 0.18f
            drawLine(
                color = Color.White,
                start = Offset(inset, inset),
                end = Offset(size.width - inset, size.height - inset),
                strokeWidth = stroke,
                cap = StrokeCap.Round,
            )
            drawLine(
                color = Color.White,
                start = Offset(size.width - inset, inset),
                end = Offset(inset, size.height - inset),
                strokeWidth = stroke,
                cap = StrokeCap.Round,
            )
        }
    }
}

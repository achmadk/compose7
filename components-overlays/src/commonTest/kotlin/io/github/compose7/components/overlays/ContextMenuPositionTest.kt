package io.github.compose7.components.overlays

import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Placement rule for the pure-Compose [ContextMenu] popup (PRD §13): below the
 * press point when there is room, above when there is not, and never off-screen
 * horizontally.
 */
class ContextMenuPositionTest {

    private val window = IntSize(400, 600)
    private val popup = IntSize(160, 200)

    private fun positionAt(
        x: Int,
        y: Int,
        gapPx: Int = 0,
        layoutDirection: LayoutDirection = LayoutDirection.Ltr,
        contentSize: IntSize = popup,
    ): IntOffset = ContextMenuPositionProvider(
        anchor = IntRect(IntOffset(x, y), IntSize.Zero),
        gapPx = gapPx,
    ).calculatePosition(
        anchorBounds = IntRect.Zero,
        windowSize = window,
        layoutDirection = layoutDirection,
        popupContentSize = contentSize,
    )

    @Test
    fun opensBelowThePressPointWhenThereIsRoom() {
        assertEquals(IntOffset(100, 100), positionAt(100, 100))
    }

    @Test
    fun gapIsAppliedBelowAndSubtractedAbove() {
        assertEquals(IntOffset(100, 108), positionAt(100, 100, gapPx = 8))
        // 500 + 8 + 200 > 600, so it flips above the point minus the gap.
        assertEquals(IntOffset(100, 292), positionAt(100, 500, gapPx = 8))
    }

    @Test
    fun flipsAboveWhenTheMenuWouldOverflowTheBottom() {
        assertEquals(IntOffset(100, 300), positionAt(100, 500))
    }

    @Test
    fun neverGoesAboveTheWindow() {
        // Flipping up would put it at -100; clamp to the top edge instead.
        assertEquals(IntOffset(100, 0), positionAt(100, 400, contentSize = IntSize(160, 500)))
    }

    @Test
    fun clampsToTheRightEdge() {
        assertEquals(IntOffset(240, 100), positionAt(350, 100))
    }

    @Test
    fun rightToLeftAlignsTheMenuEndToTheAnchor() {
        assertEquals(
            IntOffset(190, 100),
            positionAt(350, 100, layoutDirection = LayoutDirection.Rtl),
        )
    }

    @Test
    fun menuWiderThanTheWindowStartsAtTheEdge() {
        assertEquals(IntOffset(0, 100), positionAt(100, 100, contentSize = IntSize(500, 100)))
    }
}

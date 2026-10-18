package io.github.compose7.foundation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Drawn checkmark — a canvas primitive, never an icon asset (PRD §4:
 * bring-your-own-icons, no bundled set). Used by selection controls and chips.
 */
@Composable
fun Check(
    color: Color,
    modifier: Modifier = Modifier,
    size: Dp = 16.dp,
    thickness: Dp = 2.dp,
) {
    Canvas(modifier = modifier.size(size)) {
        val w = size.toPx()
        val t = thickness.toPx()
        val path = Path().apply {
            moveTo(w * 0.18f, w * 0.52f)
            lineTo(w * 0.42f, w * 0.74f)
            lineTo(w * 0.82f, w * 0.26f)
        }
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = t, cap = StrokeCap.Round, join = StrokeJoin.Round),
        )
    }
}

/** Drawn minus (steppers, quantity controls) — see [Check] for the no-icons rule. */
@Composable
fun Minus(
    color: Color,
    modifier: Modifier = Modifier,
    size: Dp = 16.dp,
    thickness: Dp = 2.dp,
) {
    Canvas(modifier = modifier.size(size)) {
        val w = size.toPx()
        val t = thickness.toPx()
        drawLine(
            color = color,
            start = Offset(w * 0.18f, w * 0.5f),
            end = Offset(w * 0.82f, w * 0.5f),
            strokeWidth = t,
            cap = StrokeCap.Round,
        )
    }
}

/** Drawn plus (steppers) — see [Check]. */
@Composable
fun Plus(
    color: Color,
    modifier: Modifier = Modifier,
    size: Dp = 16.dp,
    thickness: Dp = 2.dp,
) {
    Canvas(modifier = modifier.size(size)) {
        val w = size.toPx()
        val t = thickness.toPx()
        val c = Offset(w / 2f, w / 2f)
        drawLine(
            color = color,
            start = Offset(w * 0.18f, c.y),
            end = Offset(w * 0.82f, c.y),
            strokeWidth = t,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = color,
            start = Offset(c.x, w * 0.18f),
            end = Offset(c.x, w * 0.82f),
            strokeWidth = t,
            cap = StrokeCap.Round,
        )
    }
}

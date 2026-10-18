package io.github.compose7.core.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Shape tokens for one style. Includes the standard 5-step scale plus named
 * component shapes so components never hard-code radii.
 */
@Immutable
class Compose7Shapes(
    val extraSmall: RoundedCornerShape,
    val small: RoundedCornerShape,
    val medium: RoundedCornerShape,
    val large: RoundedCornerShape,
    val extraLarge: RoundedCornerShape,
    /** Text button / filled button corners. */
    val button: RoundedCornerShape,
    val fab: RoundedCornerShape,
    val card: RoundedCornerShape,
    /** Text field / input container. */
    val input: RoundedCornerShape,
    val chip: RoundedCornerShape,
    val badge: RoundedCornerShape,
    val dialog: RoundedCornerShape,
    /** Top corners of bottom sheets / action sheets. */
    val sheet: RoundedCornerShape,
    val snackbar: RoundedCornerShape,
    val popover: RoundedCornerShape,
) {
    constructor(
        extraSmall: Dp, small: Dp, medium: Dp, large: Dp, extraLarge: Dp,
        button: Dp, fab: Dp, card: Dp, input: Dp, chip: Dp, badge: Dp,
        dialog: Dp, sheet: Dp, snackbar: Dp, popover: Dp,
    ) : this(
        extraSmall = RoundedCornerShape(extraSmall),
        small = RoundedCornerShape(small),
        medium = RoundedCornerShape(medium),
        large = RoundedCornerShape(large),
        extraLarge = RoundedCornerShape(extraLarge),
        button = RoundedCornerShape(button),
        fab = RoundedCornerShape(fab),
        card = RoundedCornerShape(card),
        input = RoundedCornerShape(input),
        chip = RoundedCornerShape(chip),
        badge = RoundedCornerShape(badge),
        dialog = RoundedCornerShape(dialog),
        sheet = RoundedCornerShape(sheet),
        snackbar = RoundedCornerShape(snackbar),
        popover = RoundedCornerShape(popover),
    )
}

/** The active shape token set. Provided by [Compose7Theme]. */
val LocalCompose7Shapes = compositionLocalOf { materialShapes() }

/** Convenience accessor for the active shape token set. */
val Compose7ThemeShapes: Compose7Shapes
    @Composable get() = LocalCompose7Shapes.current

/** Material: 4/8/12/16/28 scale, pill buttons and chips, 12dp cards. */
fun materialShapes() = Compose7Shapes(
    extraSmall = 4.dp, small = 8.dp, medium = 12.dp, large = 16.dp, extraLarge = 28.dp,
    button = 20.dp, fab = 16.dp, card = 12.dp, input = 4.dp, chip = 8.dp, badge = 10.dp,
    dialog = 28.dp, sheet = 28.dp, snackbar = 4.dp, popover = 4.dp,
)

/** Cupertino: tighter scale, pill buttons/chips, 14dp alerts, 12dp sheets. */
fun cupertinoShapes() = Compose7Shapes(
    extraSmall = 6.dp, small = 10.dp, medium = 12.dp, large = 16.dp, extraLarge = 24.dp,
    button = 12.dp, fab = 14.dp, card = 12.dp, input = 10.dp, chip = 16.dp, badge = 10.dp,
    dialog = 14.dp, sheet = 12.dp, snackbar = 12.dp, popover = 12.dp,
)

/**
 * DesktopUI7 — Compose7's original geometry: a disciplined 4-step scale with
 * crisp, slightly squared containers and pill primary actions.
 */
fun desktopUi7Shapes() = Compose7Shapes(
    extraSmall = 3.dp, small = 6.dp, medium = 8.dp, large = 12.dp, extraLarge = 16.dp,
    button = 8.dp, fab = 12.dp, card = 8.dp, input = 6.dp, chip = 6.dp, badge = 999.dp,
    dialog = 12.dp, sheet = 12.dp, snackbar = 8.dp, popover = 6.dp,
)

internal fun shapesFor(style: Compose7Style): Compose7Shapes = when (style) {
    Compose7Style.Material -> materialShapes()
    Compose7Style.Cupertino -> cupertinoShapes()
    Compose7Style.DesktopUI7 -> desktopUi7Shapes()
    Compose7Style.Auto -> materialShapes()
}

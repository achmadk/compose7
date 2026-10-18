package io.github.compose7.core.theme

import androidx.compose.runtime.compositionLocalOf
import io.github.compose7.core.platform.platformDefaultStyle

/**
 * The visual languages Compose7 can render in.
 *
 * [Auto] resolves to the host platform's convention (Android → [Material],
 * iOS → [Cupertino], Desktop → [DesktopUI7]) via [Compose7Theme]; every other
 * value forces that look on every platform. See PRD §8.1.
 */
enum class Compose7Style {
    /** Resolve to the host platform's convention. */
    Auto,

    /** Material-flavored rendering. */
    Material,

    /** iOS/Cupertino-flavored rendering. */
    Cupertino,

    /** Compose7's original desktop visual language (not modeled on Framework7's Aurora). */
    DesktopUI7,
    ;

    companion object {
        /** The concrete (non-[Auto]) style for the current platform. */
        fun platformDefault(): Compose7Style = platformDefaultStyle()
    }
}

/**
 * The resolved style for the current subtree — never [Compose7Style.Auto].
 *
 * [Compose7Theme] always provides a resolved value; when used outside the
 * theme the platform default applies.
 */
val LocalCompose7Style = compositionLocalOf { platformDefaultStyle() }

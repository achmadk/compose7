package io.github.compose7.core.platform

import io.github.compose7.core.theme.Compose7Style

/**
 * The style [Compose7Style.Auto] resolves to on the current platform
 * (PRD §8.1: Android → Material, iOS → Cupertino, Desktop → DesktopUI7).
 */
expect fun platformDefaultStyle(): Compose7Style

/** Short platform identifier for diagnostics and the sample app header. */
expect fun platformName(): String

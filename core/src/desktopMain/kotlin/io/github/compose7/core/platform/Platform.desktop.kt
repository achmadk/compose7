package io.github.compose7.core.platform

import androidx.compose.runtime.Composable
import io.github.compose7.core.theme.Compose7ColorScheme
import io.github.compose7.core.theme.Compose7Style

actual fun platformDefaultStyle(): Compose7Style = Compose7Style.DesktopUI7

actual fun platformName(): String = "Desktop"

@Composable
actual fun platformDynamicColorScheme(darkTheme: Boolean): Compose7ColorScheme? = null

/** No-op: desktop windows have no system bars. */
@Composable
actual fun Compose7SystemBarAppearance(darkContent: Boolean) = Unit

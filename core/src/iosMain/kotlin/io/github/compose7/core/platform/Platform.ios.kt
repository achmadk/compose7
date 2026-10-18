package io.github.compose7.core.platform

import androidx.compose.runtime.Composable
import io.github.compose7.core.theme.Compose7ColorScheme
import io.github.compose7.core.theme.Compose7Style

actual fun platformDefaultStyle(): Compose7Style = Compose7Style.Cupertino

actual fun platformName(): String = "iOS"

@Composable
actual fun platformDynamicColorScheme(darkTheme: Boolean): Compose7ColorScheme? = null

/**
 * No-op on iOS: UIKit derives status-bar content from the interface style,
 * which `Compose7Theme` already follows from system settings.
 */
@Composable
actual fun Compose7SystemBarAppearance(darkContent: Boolean) = Unit

package io.github.compose7.sample

import androidx.compose.runtime.Composable
import io.github.compose7.core.theme.Compose7Style
import io.github.compose7.sample.screens.ShellScreen

/**
 * Entry point for every Compose7 sample host (PRD §16: Kitchen Sink demo).
 *
 * The gallery is one app shell so every platform runs identical component code:
 * Android/iOS/Desktop hosts only supply the window; the style switch in the
 * navbar flips the whole tree between [Compose7Style] values (PRD §9).
 */
@Composable
fun Compose7SampleApp() {
    ShellScreen()
}

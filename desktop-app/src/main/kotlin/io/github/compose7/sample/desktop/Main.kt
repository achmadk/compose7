package io.github.compose7.sample.desktop

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import io.github.compose7.sample.Compose7SampleApp

/**
 * Desktop host for the Kitchen Sink (PRD §16). The window is phone-shaped so the
 * gallery can be compared against Android/iOS at the same size.
 */
fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Compose7 Kitchen Sink",
        state = rememberWindowState(width = 420.dp, height = 900.dp),
    ) {
        Compose7SampleApp()
    }
}

package io.github.compose7.sample

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

/**
 * iOS host entry point for the Kitchen Sink (PRD §16).
 *
 * Swift side: `MainViewControllerKt.MainViewController()` inside a
 * `UIHostingController`-style wrapper - see README for the Xcode wiring.
 * Compiled only on macOS hosts / the macOS CI job.
 */
fun MainViewController(): UIViewController = ComposeUIViewController {
    Compose7SampleApp()
}

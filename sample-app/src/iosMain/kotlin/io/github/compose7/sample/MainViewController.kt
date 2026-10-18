package io.github.compose7.sample

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

/**
 * iOS host entry point for the Kitchen Sink (PRD §16).
 *
 * Swift side: `MainViewControllerKt.MainViewController()` inside a
 * `UIViewControllerRepresentable`. The framework is built/linked by
 * `:sample-app:linkDebugFramework*` or Xcode's
 * `embedAndSignAppleFrameworkForXcode` - see docs/IOS.md.
 * Linking needs macOS + Xcode; the klib itself compiles on any host.
 */
fun MainViewController(): UIViewController = ComposeUIViewController {
    Compose7SampleApp()
}

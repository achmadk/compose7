# Running the Kitchen Sink on iOS

`:sample-app` contains the shared Compose UI (the Kitchen Sink gallery) and a
single iOS host function in `src/iosMain/.../MainViewController.kt`. Everything
else - the app icon, `Info.plist`, signing, launch screen - belongs to a small
Xcode app shell you create once. This page covers both halves.

## Prerequisites

- macOS with **Xcode 16+** to *link* the framework (Kotlin/Native uses the
  Xcode toolchain).
- **JDK 21** for Gradle.
- The iOS targets are already declared in `sample-app/build.gradle.kts`; no
  extra Gradle setup is needed.

Kotlin/Native compiles the iOS *klibs* on any host, so `./gradlew build`
type-checks this code on Windows/Linux too. The `link*Framework*` tasks are
skipped there; that is expected.

## 1. Build the framework

```bash
# Apple Silicon simulator
./gradlew :sample-app:linkDebugFrameworkIosSimulatorArm64
# Device (arm64)
./gradlew :sample-app:linkDebugFrameworkIosArm64
```

Output:

```
sample-app/build/bin/iosSimulatorArm64/debugFramework/Compose7Sample.framework
sample-app/build/bin/iosArm64/debugFramework/Compose7Sample.framework
```

The framework is **static** (`isStatic = true`): it is linked into the app
binary rather than embedded as a dynamic library. `MainViewController()` is
exported to Swift as `MainViewControllerKt.MainViewController()`.

## 2. Create the Xcode app shell

1. Xcode -> **File -> New -> Project -> iOS -> App**.
   - Product name: `iosApp`
   - Interface: **SwiftUI**, language: **Swift**
   - Save it as `iosApp/` next to the Gradle modules (repo root), so
     `$SRCROOT/..` is the Gradle root.
2. Select the app target -> **Build Phases** -> **+** -> **New Run Script
   Phase**. Drag it **above "Compile Sources"** and use:

   ```sh
   cd "$SRCROOT/.."
   ./gradlew :sample-app:embedAndSignAppleFrameworkForXcode
   ```

   Xcode passes `CONFIGURATION`, `SDK_NAME` and friends; the Gradle task links
   the right architecture and copies it to
   `sample-app/build/xcode-frameworks/...`.

3. Target -> **Build Settings**:
   - **Framework Search Paths** add:
     `$(SRCROOT)/../sample-app/build/xcode-frameworks/$(CONFIGURATION)/$(SDK_NAME)`
   - **User Script Sandboxing** (`ENABLE_USER_SCRIPT_SANDBOXING`) = **No**.
     Xcode 15+ enables it by default, and the sandbox blocks Gradle from
     writing outside the build directory.

4. Replace the template `ContentView.swift` with:

   ```swift
   import SwiftUI
   import Compose7Sample

   struct ComposeView: UIViewControllerRepresentable {
       func makeUIViewController(context: Context) -> UIViewController {
           MainViewControllerKt.MainViewController()
       }

       func updateUIViewController(
           _ uiViewController: UIViewController,
           context: Context
       ) {}
   }

   struct ContentView: View {
       var body: some View {
           ComposeView()
               .ignoresSafeArea(.keyboard) // let Compose handle the keyboard inset
       }
   }
   ```

   Keep the generated `iosApp.swift` (`@main` + `WindowGroup { ContentView() }`).

5. Pick an Apple Silicon simulator and **Run**. The first build downloads the
   Kotlin/Native toolchain and links a large framework, so it takes a few
   minutes; later builds are incremental.

## Verifying the native bridge

The gallery's shell has a **Native bridge** switch (`ShellScreen.kt`), backed by
`Compose7Theme(nativeBridge = ...)`. With it on:
- the Dialog and Action Sheet demos render through `UIAlertController`;
- the Forms screen's **Due date** field opens a real `UIDatePicker` wheel;
- the Overlays **Share sheet** button opens the system
  `UIActivityViewController`;
- long-pressing the Overlays card opens a system context menu
  (`UIContextMenuInteraction`).

The component keeps its pure-Compose fallback for any dialog that passes a
custom `content` slot, and every path outside iOS (including a `DatePicker`
with the bridge forced off). See [NATIVE_BRIDGE.md](NATIVE_BRIDGE.md) for the
contract and the remaining component list.

## Troubleshooting

| Symptom | Cause / fix |
|---|---|
| `No such module 'Compose7Sample'` | The framework is not linked yet: run the Gradle task once, check the Framework Search Path, then **Product -> Clean Build Folder**. |
| Gradle task says `SKIPPED` | You are not on macOS. Linking is macOS-only. |
| Build script fails with sandbox errors | Set `ENABLE_USER_SCRIPT_SANDBOXING = NO` on the target. |
| Framework not found at build time | The Run Script phase must run **before Compile Sources** and `$SRCROOT` must point at the `iosApp/` folder. |
| Blank screen | Make sure you are returning `MainViewControllerKt.MainViewController()`, not instantiating a `ComposeUIViewController` in Swift. |

## CI

The `ios-sim` job on `macos-latest` compiles the iOS targets and links
`linkDebugFrameworkIosSimulatorArm64` (no signing identity required), so a
broken UIKit actual or an incompatible Compose update fails the PR instead of
waiting for a manual Xcode run.

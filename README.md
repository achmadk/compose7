# Compose7

Adaptive UI components for **Kotlin Compose Multiplatform** — one API that
renders in the visual language of the platform you ship to (or the one you
pick), inspired by [Framework7](https://framework7.io/)'s breadth.

| | Material | Cupertino | DesktopUI7 |
|---|---|---|---|
| Where it fits | Android apps | iOS apps | Desktop apps |
| How it's chosen | `Compose7Theme(style = …)` — `Auto` picks per platform | same | same |

- **One component API, three renderings.** `Button`, `Dialog`, `Navbar`, lists
  and the rest render with the target platform's native design language from
  identical call-site code — token-driven, no `if (platform)` in your app.
- **No icon baggage, ever.** Compose7 bundles no icon set. Components take
  icon slots; affordances Compose7 draws itself (checkmarks, chevrons,
  steppers) are pure `Canvas`.
- **Opt-in iOS native bridge.** For the handful of components where hand-drawn
  fidelity drifts every WWDC (Alert, Action Sheet — see
  [docs/NATIVE_BRIDGE.md](docs/NATIVE_BRIDGE.md)), enable real UIKit
  rendering per theme or per component. Pure Compose stays the default.
- **Modular artifacts.** Depend on `components-lists` without dragging in
  media or messaging.

## Modules

| Artifact | Contents |
|---|---|
| `io.github.compose7:core` | Theme engine, tokens, platform detection, haptics, bridge API |
| `io.github.compose7:foundation` | Surfaces, pages, cards, grids, press/toggle interaction, glyphs, pull-to-refresh and infinite-scroll helpers |
| `io.github.compose7:components-buttons` | Button, FAB, Chip, Badge |
| `io.github.compose7:components-indicators` | ProgressBar, Preloader, Gauge, Skeleton |
| `io.github.compose7:components-navigation` | Navbar, Tabbar, Tabs, SegmentedControl, Panel, Searchbar |
| `io.github.compose7:components-lists` | List, ListItem, Swipeout, SortableList, ListIndex, VirtualList |
| `io.github.compose7:components-forms` | TextField, Checkbox, Radio, Switch, Stepper, Slider, SmartSelect, Autocomplete, Calendar/DatePicker, ColorPicker |
| `io.github.compose7:components-overlays` | Dialog, ActionSheet, SheetModal, Popover, Popup, Toast, Tooltip |
| `io.github.compose7:components-media` | Swiper, PhotoBrowser |
| `io.github.compose7:components-data` | Accordion, Timeline, Breadcrumbs |
| `io.github.compose7:components-messaging` | MessageList, MessageBar |
| `io.github.compose7:templates` | Login screen template |

Targets: **Android, iOS (arm64 + simulator), Desktop (JVM)**. Wasm/JS are
post-1.0 (PRD §16 Phase 5).

## Quick start

```kotlin
dependencies {
    implementation("io.github.compose7:components-buttons:0.1.0-alpha01")
}
```

```kotlin
@Composable
fun App() {
    Compose7Theme {                       // Auto = host platform's language
        Button("Save", onClick = ::save)  // Material pill on Android, tinted
    }                                     // pill on iOS, squared on Desktop
}
```

Force one look everywhere (a first-class use case, not an escape hatch):

```kotlin
Compose7Theme(style = Compose7Style.Cupertino) { … }
```

## Building

```bash
./gradlew build                  # all modules, desktop + Android
./gradlew :desktop-app:run       # run the Kitchen Sink on desktop
./gradlew publishToMavenLocal
```

Requires JDK 21 for everything. Kotlin/Native cross-compiles iOS klibs on any
host, so `compileKotlinIosArm64` and the UIKit bridge actuals are type-checked
even on Windows/Linux. **Linking** an `.app`/framework still needs Xcode on
macOS, so that step stays a local (or macOS-runner) concern; CI's `ios-sim`
job compiles the simulator targets to catch bridge breakage.

Android builds need **SDK Platform 37+**. Compose Multiplatform 1.12 ships
AndroidX Compose artifacts whose AAR metadata declares a minimum `compileSdk`
of 37, so the Gradle build (and any app consuming these modules) must compile
against that platform. Point Gradle at your SDK with `local.properties`
(git-ignored, so it stays machine-specific):

```properties
# local.properties
sdk.dir=/path/to/Android/Sdk
```

Install the platform with `sdkmanager "platforms;android-37"`, or let Android
Studio do it on first sync. `compileSdk` is 37 while `targetSdk` stays 36, so
the sample opts into new compile-time APIs without silently adopting new
runtime behavior. The library's own `minSdk` is 24.

## Documentation

- [docs/COMPONENTS.md](docs/COMPONENTS.md) — every public entry point by category.
- [docs/THEMING.md](docs/THEMING.md) — style switching, token overrides,
  dynamic color, and the accessibility deltas from stock platform colors.
- [docs/NATIVE_BRIDGE.md](docs/NATIVE_BRIDGE.md) — iOS bridge component list,
  rollout order, and contract (Phase 0 deliverable).
- [CHANGELOG.md](CHANGELOG.md)
- API reference: generated with Dokka (`./gradlew dokkaHtml`) per release.
- Framework7 → Compose7 concept map: PRD Appendix A (in-repo).

## License

[MIT](LICENSE) — no icon fonts, no asset licensing, no surprises.

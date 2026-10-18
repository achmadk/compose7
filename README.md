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
  fidelity drifts every WWDC (Alert, Action Sheet, Date wheel, Share sheet,
  Context menu — see [docs/NATIVE_BRIDGE.md](docs/NATIVE_BRIDGE.md)), enable
  real UIKit rendering per theme or per component. Pure Compose stays the
  default.
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
| `io.github.compose7:components-forms` | TextField, Checkbox, Radio, Switch, Stepper, Slider, SmartSelect, Autocomplete, Calendar/Range, DatePicker/Range, ColorPicker |
| `io.github.compose7:components-overlays` | Dialog, ActionSheet, ShareSheet, ContextMenu, SheetModal, Popover, Popup, Toast/ToastHost, Notification, Tooltip |
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
./gradlew build                      # all modules, desktop + Android
./gradlew desktopTest                # unit + golden tests
./gradlew :desktop-app:run           # run the Kitchen Sink on desktop
./gradlew :benchmarks:benchmark      # recomposition/scroll microbenchmarks
./gradlew publishToMavenLocal
```

CI builds against the **last two stable Compose Multiplatform releases**
(`-PcomposeVersion=…`) on Linux and macOS (PRD §10).

Requires JDK 21 for everything. Kotlin/Native cross-compiles iOS klibs on any
host, so `compileKotlinIosArm64` and the UIKit bridge actuals are type-checked
even on Windows/Linux. **Linking** the sample's framework needs Xcode on
macOS: `./gradlew :sample-app:linkDebugFrameworkIosSimulatorArm64`. A checked-in
[XcodeGen](https://github.com/yonaskolb/XcodeGen) app shell lives in `iosApp/`;
step-by-step instructions are in [docs/IOS.md](docs/IOS.md), and CI's `ios-sim`
job builds that app for the simulator to catch bridge breakage.

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
- [docs/IOS.md](docs/IOS.md) — building the sample framework and wiring it into
  an Xcode app.
- [docs/BENCHMARKS.md](docs/BENCHMARKS.md) — what the microbenchmarks measure,
  the tolerance, and their limits.
- [docs/RELEASING.md](docs/RELEASING.md) — tag-driven Maven Central releases,
  required secrets, and the maintainer hand-off path.
- [CHANGELOG.md](CHANGELOG.md)
- API reference: generated with Dokka (`./gradlew dokkaHtml`) per release.
- Framework7 → Compose7 concept map: PRD Appendix A (in-repo).

## Governance & continuity

Compose7 is MIT-licensed and currently maintained by
[@achmadk](https://github.com/achmadk) on a personal account. That is the same
starting position `compose-cupertino` had before it went dormant, so the
hand-off is planned rather than left open-ended (PRD §3.7, §18):

- **Transition trigger:** once the project secures sponsorship or funding, the
  repository moves to an organization with at least two maintainers holding
  Maven Central publish access. Nothing else is required to start that process.
- **Releases are automated.** Pushing a `v*` tag builds, tests, signs, and
  publishes every module from GitHub Actions
  ([`.github/workflows/release.yml`](.github/workflows/release.yml)); no
  artifact depends on a maintainer's laptop. The process and required secrets
  are in [docs/RELEASING.md](docs/RELEASING.md).
- **Credentials are revocable.** Central Portal tokens and the GPG key live in
  repository secrets, not in personal config. On hand-off, rotate all five and
  transfer the signing key — it is the only secret that cannot be regenerated
  without losing continuity of signatures.
- **The code stays forkable.** Every release is reproducible from its tag, and
  the MIT license lets a fork publish the same source under new coordinates
  without permission.

This note lives in the README so it survives repository moves, and
[docs/RELEASING.md](docs/RELEASING.md) is written for a successor who has never
cut a release here.

## License

[MIT](LICENSE) — no icon fonts, no asset licensing, no surprises.

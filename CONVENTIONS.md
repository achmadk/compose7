# Compose7 Component Authoring Conventions

This is the contract for every component module (`components-*`). Read it fully
before writing code. PRD = `PRD.md` in the repo root.

## 1. Module & package layout

- One module per PRD §10 group: `components-buttons`, `components-indicators`,
  `components-navigation`, `components-lists`, `components-forms`,
  `components-overlays`, `components-media`, `components-data`,
  `components-messaging`. Full-screen starting points live in `templates`
  (`io.github.compose7.templates`); they compose library components rather
  than inventing new primitives.
- Package: `io.github.compose7.components.<area>` (e.g.
  `io.github.compose7.components.lists`).
- One public component per file, file named after the component
  (`Swipeout.kt`). Shared internals in `internal` files prefixed `…Internal.kt`.
- Dependencies: modules depend on `:foundation` only (declared in the module's
  `build.gradle.kts` — do not edit build files). `components-forms` additionally
  depends on `:components-overlays`.
- **Only `commonMain` + (if bridging) `iosMain`/`androidMain`/`desktopMain`.
  No new dependencies** — compose.foundation/ui/animation come via `:foundation`.

## 2. Naming & API shape

- Public composables use **plain Framework7 names**: `Button`, `ListItem`,
  `Navbar`, `ActionSheet`, `Swiper`… (PRD Appendix A is the authoritative list).
  No `C7`/`Compose7` prefix on components.
- Signature order: `modifier: Modifier = Modifier` first, then state
  (`value`/`checked`/`expanded`…), then `on…Change`, then content slots, then
  visual overrides (`colors`/`shape`/… defaulting to tokens), then misc.
- **State**: controlled by default — `value` + `onValueChange`. When a component
  owns convenience state, make the change-callback **nullable**
  (`onValueChange: ((Boolean) -> Unit)? = null`) and remember state internally
  with `rememberSaveable` — one signature, no overload explosion (M3 pattern).
  PRD §11.
- **Slots**: leading/trailing/content lambdas with receiver scopes
  (`@Composable RowScope.() -> Unit`), never long fixed parameter lists
  (PRD §6.2, §11).
- Data-shaped where the API must also drive native bridge rendering (Dialog /
  ActionSheet actions): plain `data class` + `List<…>` (see §6).
- Every public composable: **KDoc** with one-line purpose + `PRD §x` reference
  where applicable. KDoc only — no narrating comments in code bodies.

## 3. Tokens — never hard-code

Read tokens from core; **never** write raw colors, radii, durations, or font
styles inside components:

```kotlin
val colors = Compose7ThemeColors            // Compose7ColorScheme
val type = Compose7ThemeTypography          // Compose7Typography
val shapes = Compose7ThemeShapes            // Compose7Shapes
val motion = Compose7MotionTokens           // Compose7Motion
val style = currentCompose7Style            // Compose7Style (never Auto)
val info = Compose7StyleInfo                // list insets, sheet motion, …
```

Style branching (`when (style)`) is how components diverge — e.g. Button:
pill + tinted fill on Cupertino, filled `shapes.button` on Material, bordered
on DesktopUI7. Differences must be **token-driven**, not ad-hoc values.

## 4. Interaction, motion, a11y (PRD §6.4–6.5, §12)

- Clickable surfaces: `Modifier.compose7Clickable(…)` from `:foundation`
  (press overlay + haptics + semantics). Toggles:
  `Modifier.compose7Toggleable(…)` with the right `Role`.
- Min touch targets: `currentMinTouchTarget()` / `Modifier.compose7MinimumTouchTarget()`
  (48dp Material/Desktop, 44pt Cupertino).
- Animate with `tween(motion.…)`, `animateFloatAsState`, `AnimatedVisibility` —
  durations/easings from tokens only.
- RTL: only `start`/`end` padding, `Alignment.Start/End`, `Arrangement.Start`
  — never `left`/`right`.
- Icon-only controls need `contentDescription: String?`.
- Stateful controls expose semantics (toggleable does; lists rows should add
  `Modifier.semantics { }` when they act as selectable rows).

## 5. Icons & text (PRD §4, §11)

- **No icon assets or ImageVector references in library code, ever.** Draw
  affordances (checkmarks, dots, chevrons, +/-, slider thumbs) with `Canvas` /
  `Path`; expose icon slots for caller-provided icons. `foundation.Chevron` and
  `foundation.Icon` exist for reuse.
- Text: `BasicText(text, style = …)` with a `Compose7ThemeTypography` role,
  color explicitly from tokens (`colors.onSurface` etc.) or
  `foundation.LocalContentColor.current` inside surfaces.

## 6. Native Interop Bridge (PRD §8.7) — overlays/forms only

Components on the finalized list (Alert/Dialog, ActionSheet, DatePicker wheel,
ShareSheet, ContextMenu) get:

```kotlin
@Composable
fun ActionSheet(
    …,
    nativeBridge: Boolean? = null,   // null = follow theme; true = force native; false = force pure
) {
    val useNative = resolveNativeBridge(nativeBridge)
    if (useNative) {
        NativeActionSheet(…)         // expect fun, defined in commonMain
    } else {
        PureActionSheet(…)           // private composable with the pure-Compose UI
    }
}
```

- commonMain declares `expect @Composable fun NativeX(…)`.
- `iosMain` actual → real UIKit (`platform.UIKit` interop).
- `androidMain`/`desktopMain` actuals → call the pure-Compose implementation
  (bridge is iOS-only; flag is already gated, actuals are just for compilation).
- Native path renders **data only** (title/message/actions, a date plus
  min/max bounds, share items); if a composable slot is in use, fall back to
  pure (documented — no silent divergence). A predicate the UIKit control
  cannot express (for example arbitrary disabled dates) is a reason to expose
  bounds instead, not to silently ignore it.
- UIKit code cross-compiles as a klib on any host (that is how it is
  type-checked in a plain `./gradlew build`); keep it minimal and use boring,
  well-known APIs (`UIAlertController`, `UIAlertAction`, `UIDatePicker`,
  `UIActivityViewController`). Linking an app/framework still needs macOS +
  Xcode. Bridge files start with:
  `// Cross-compiled as a klib on any host; keep UIKit usage conservative.`

## 7. Testing (PRD §13)

Non-UI logic gets `commonTest` (`kotlin.test`) — date math, filter/autocomplete
persistence logic, state reducers, index sectioning. UI-only components don't
need tests here (screenshot tests are CI-side, later).

## 8. Done criteria

1. Code compiles for `desktop` target (I will run the build).
2. No TODOs left except documented deferrals.
3. Component listed in `sample-app` gallery? — **not your job**; I wire the
   sample. Just expose clean public APIs.

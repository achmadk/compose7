# Native Interop Bridge (iOS) — Component List & Rollout Order

**Status:** 5 of 5 shipped — Dialog, ActionSheet, DatePicker, ShareSheet,
ContextMenu. The list itself was finalized as the Phase 0 planning deliverable
(PRD §8.7 / §16).
**Scope:** iOS only. Android and Desktop always render Compose7's own
token-driven styles.

## What the bridge is

For a small, opt-in set of components whose iOS fidelity is expensive to
maintain in pure Compose, Compose7 can render through the real UIKit view the
system provides (`UIAlertController`, `UIActivityViewController`, …). Pure
Compose stays the default everywhere; the bridge is an explicit opt-in per
theme or per component, and every bridged component keeps a pure-Compose
fallback (PRD §8.7 — no silent divergence).

Opt-in:

```kotlin
// Theme-wide:
Compose7Theme(nativeBridge = true) { … }

// Per component (wins over the theme-level switch):
Dialog(…, nativeBridge = true)
ActionSheet(…, nativeBridge = false)  // force pure even when theme enables it
```

`nativeBridge: Boolean? = null` means "follow the theme". Forcing native on a
platform that cannot host UIKit (Android/Desktop) silently falls back to pure
Compose — it never crashes.

## Finalized component list & rollout order

| # | Component | UIKit surface | Phase | Status | Rationale |
|---|-----------|---------------|-------|--------|-----------|
| 1 | `Dialog` (Alert) | `UIAlertController` (alert style) | 1 | Shipped | Highest drift risk: alert chrome changes with every major iOS release; data-only API maps 1:1. |
| 2 | `ActionSheet` | `UIAlertController` (action-sheet style) | 1 | Shipped | Same controller family as Alert — nearly free once #1 exists; iPad popover anchoring handled. |
| 3 | `DatePicker` (wheel) | `UIDatePicker` (wheels style) | 2 | Shipped | The date wheel is famously hard to match by hand; the pure-Compose calendar remains the cross-platform default. |
| 4 | `ShareSheet` | `UIActivityViewController` | 2 | Shipped | System share UI is per-definitionally native (share extensions, entitlements); pure-Compose cannot replicate it. |
| 5 | `ContextMenu` | `UIContextMenuInteraction` | 2 | Shipped | System haptics + preview annotations are UIKit-owned behavior. |

Deferred (candidates for post-1.0, not committed):

- `UIStepper` — our Stepper already renders identically; low drift risk.
- `UISwitch` — visual parity is close via tokens; revisit if iOS changes the
  switch design again.
- `UIPickerView` wheel beyond dates — only if a concrete consumer asks.

## Contract (implemented in `components-overlays` and `components-forms`)

- commonMain declares `expect fun NativeX(…)` taking **data only** — never
  composable slots (PRD §8.7). That is why `DatePicker` exposes
  `minDate`/`maxDate` instead of a `(CalendarDate) -> Boolean` predicate
  (`UIDatePicker` has `minimumDate`/`maximumDate`), and `ShareSheet` takes
  `ShareItem` values instead of a render callback.
- `iosMain` actuals present real UIKit controllers from the top-most view
  controller:
  - `NativeDialog`/`NativeActionSheet` — correct action roles and iPad popover
    anchoring.
  - `NativeDatePicker` — a `UIDatePicker` (wheels) inside a page-sheet
    navigation controller with the system Cancel/Done items. The sheet is not
    interactively dismissible, so the call site's `expanded` flag stays the
    single source of truth.
  - `NativeShareSheet` — `UIActivityViewController` with iPad popover anchoring
    and a completion handler that always reaches `onDismissRequest`.
  - `NativeContextMenu` — one `UIContextMenuInteraction` attached to the
    Compose host view (UIKit interactions belong to a `UIView`, not a Compose
    node). Each `ContextMenu` registers its anchor's window rectangle; the
    delegate returns a configuration only when the long-press point falls
    inside one, so long-presses elsewhere stay inert. Items become `UIAction`
    rows with the destructive/disabled attributes mapped. Menu dismissal always
    reaches `onDismissRequest`. Because the interaction is host-wide, the
    bridge assumes the Compose view fills its window and reports window
    coordinates 1:1 with UIKit points (true for the sample's full-screen
    `ComposeUIViewController`); nesting it offset inside a larger SwiftUI view
    is out of scope.
- `androidMain`/`desktopMain` actuals delegate to the pure implementation;
  they exist only so the `expect` compiles — `resolveNativeBridge()` already
  gates the native path to iOS.
- **Pure fallbacks:** `Dialog`/`ActionSheet` render Compose chrome;
  `DatePicker` opens the Compose `Calendar`; `ShareSheet` renders an
  `ActionSheet` of the items and calls `onItemSelected`, which the native path
  never invokes (the system owns the action list there); `ContextMenu` opens a
  `Popover` at the press point.
- **Composable-slot escape hatch:** if a caller passes a custom `content` slot
  to `Dialog`/`ActionSheet`, the component renders pure Compose even when the
  bridge is enabled, because UIKit cannot host a Compose slot in these
  controllers. This is documented behavior, not a silent fallback.
- UIKit code needs Xcode only to *link*: Kotlin/Native cross-compiles the
  `iosMain` klibs on any host, so type errors in this bridge surface in a plain
  `./gradlew build` (that is how it was first compiled - on Windows). Xcode on
  macOS is still required to produce a runnable app/framework.

## Maintenance policy

- CI's macOS runner compiles every iOS target **and builds the `iosApp` shell
  for the simulator**, so breakage surfaces on iOS betas rather than after
  release (PRD §18 risk mitigation). Signing/provisioning and gesture-level
  behaviour still need a local Xcode run.
- Compilation and linking cannot prove gesture behaviour: the first run of a
  new bridge component on a simulator should check that the system chrome
  appears, dismisses, and leaves Compose gestures (scroll, drag, taps) intact.
- A new component may join the bridge list only by editing this document in
  the same PR that implements it.

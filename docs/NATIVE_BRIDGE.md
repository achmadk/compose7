# Native Interop Bridge (iOS) — Component List & Rollout Order

**Status:** finalized (Phase 0 planning deliverable, PRD §8.7 / §16)
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

| # | Component | UIKit surface | Phase | Rationale |
|---|-----------|---------------|-------|-----------|
| 1 | `Dialog` (Alert) | `UIAlertController` (alert style) | 1 | Highest drift risk: alert chrome changes with every major iOS release; data-only API maps 1:1. |
| 2 | `ActionSheet` | `UIAlertController` (action-sheet style) | 1 | Same controller family as Alert — nearly free once #1 exists; iPad popover anchoring handled. |
| 3 | `DatePicker` (wheel) | `UIDatePicker` (wheels style) | 2 | The date wheel is famously hard to match by hand; the pure-Compose calendar remains the cross-platform default. |
| 4 | Share sheet | `UIActivityViewController` | 2 | System share UI is per-definitionally native (share extensions, entitlements); pure-Compose cannot replicate it. |
| 5 | `ContextMenu` | `UIContextMenuInteraction` | 2 | System haptics + preview annotations are UIKit-owned behavior. |

Deferred (candidates for post-1.0, not committed):

- `UIStepper` — our Stepper already renders identically; low drift risk.
- `UISwitch` — visual parity is close via tokens; revisit if iOS changes the
  switch design again.
- `UIPickerView` wheel beyond dates — only if a concrete consumer asks.

## Contract (implemented in `components-overlays`)

- commonMain declares `expect fun NativeDialog(…)` / `NativeActionSheet(…)`
  taking **data only** (title, message, actions) — never composable slots
  (PRD §8.7).
- `iosMain` actuals present real UIKit controllers from the top-most view
  controller, with correct action roles (default/destructive/cancel) and
  iPad popover anchoring for action sheets.
- `androidMain`/`desktopMain` actuals delegate to the pure implementation;
  they exist only so the `expect` compiles — `resolveNativeBridge()` already
  gates the native path to iOS.
- **Composable-slot escape hatch:** if a caller passes a custom `content` slot
  to `Dialog`/`ActionSheet`, the component renders pure Compose even when the
  bridge is enabled, because UIKit cannot host a Compose slot in these
  controllers. This is documented behavior, not a silent fallback.
- UIKit code needs Xcode only to *link*: Kotlin/Native cross-compiles the
  `iosMain` klibs on any host, so type errors in this bridge surface in a plain
  `./gradlew build` (that is how it was first compiled - on Windows). Xcode on
  macOS is still required to produce a runnable app/framework.

## Maintenance policy

- CI's macOS runner compiles every iOS target, including this bridge, so
  breakage surfaces on iOS betas rather than after release (PRD §18 risk
  mitigation). Linking a runnable app/framework stays a local Xcode step.
- A new component may join the bridge list only by editing this document in
  the same PR that implements it.

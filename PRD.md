# Product Requirements Document

## Compose7 — A Kotlin Compose Multiplatform UI Component Library Inspired by Framework7

| Field | Value |
|---|---|
| Status | Draft |
| Version | 0.6 |
| Author / Maintainer | achmadk (initial, sole maintainer) |
| Date | 2026-09-09 |
| Name | Compose7 (confirmed) |

**Change log**
- **0.6** — Moved the Native Interop Bridge component-list decision into **Phase 0** as an explicit planning deliverable, rather than leaving it as an open question resolved "once Phase 0 is 
underway." Updated §8.7, Roadmap Phase 0/1 (§16), the risk mitigation (§18), and moved the item from "Still open" into the resolved decisions log (§19.1).
- **0.5** — Added a new capability, the **Native Interop Bridge (iOS)** (§8.7), targeting the most structural gap suggested by `compose-cupertino`'s prior art: pure Compose-drawn iOS widgets must be 
manually re-painted every time Apple revises its Human Interface Guidelines (most recently "Liquid Glass" in iOS 26, Sept 2025). *Evidence note: this is grounded in the actively-maintained fork's 
published module lineup (it ships a `cupertino-native` module) plus the public record of the Liquid Glass redesign — not a read of `compose-cupertino`'s actual issue tracker, which search did not 
surface specific threads from.* Rationale expanded in §2.3; differentiator note added to §9; rollout added to Roadmap Phases 1–2 (§16); new risk row (§18); new open item on exact component scope 
(§19); new **Appendix B** tracing each `compose-cupertino`-related gap raised across this PRD to its Compose7 mitigation.
- **0.4** — Governance: named achmadk as the initial, sole maintainer, with org-ownership/multi-maintainer as a post-sponsorship transition rather than a day-one requirement (§3.7, §18, §19). 
Desktop style named **DesktopUI7** (checked for conflicts — none found against "Desktop7" or similar; §6, §8.1, §13, §18, §19). §19 "Still open" items both resolved and moved into the decisions log.
- **0.3** — Resolved open questions 1–7: name confirmed as Compose7; Web/Wasm explicitly deferred to a post-1.0 phase; no bundled icon set of any kind (bring-your-own-icons only); a thin optional 
router module is planned but for post-1.0; license set to MIT; Desktop gets an original Compose7 visual language instead of mimicking Framework7's Aurora theme; a `compose-cupertino` migration guide 
is planned but post-1.0. Roadmap split to add a Phase 5 (Post-1.0). §19 restructured into a Decisions log plus remaining open items.
- **0.2** — Added competitive-landscape note on `compose-cupertino`'s fork lineage and maintainer-dormancy pattern (§2.3); added a corresponding governance goal (§3.7) and risk entry (§18); 
sharpened the governance open question (§19.5) and added a new one on fork interop (§19.7).
- **0.1** — Initial draft.

---

## 1. Executive Summary

Compose7 is a Kotlin Compose Multiplatform (CMP) UI library that brings Framework7's broad, batteries-included component catalog and **adaptive, platform-aware theming** to native Compose targets — 
Android, iOS, Desktop (JVM), and eventually Web (Wasm/JS).

Framework7 became popular because it gave hybrid-app developers a near-complete UI toolkit — lists, navigation chrome, forms, overlays, media — that automatically rendered iOS-flavored on iOS and 
Material-flavored on Android from the *same markup*. Compose Multiplatform has strong fundamentals (declarative UI, shared business logic, real native rendering) but the component ecosystem is still 
sparse compared to what a Framework7 developer expects out of the box. Compose7 closes that gap: one dependency, one API surface, native look-and-feel per platform, without the developer 
hand-rolling action sheets, smart selects, swipeouts, or a theming system. It also targets a specific weakness observed in existing Cupertino-style Compose libraries: for the handful of components 
where visual fidelity matters most, Compose7 offers an opt-in bridge to real native iOS views instead of a hand-painted approximation that drifts every time Apple revises its design language (§8.7).

---

## 2. Background & Motivation

### 2.1 What Framework7 got right
- A **large, consistent component catalog** covering nearly every common mobile UI need (navigation chrome, lists, forms, overlays, media, messaging), so teams rarely need to build primitives from 
scratch.
- **Adaptive theming**: the same component renders with iOS conventions on iOS and Material conventions on Android (plus an Aurora/desktop theme), driven by a single theme switch rather than 
parallel codebases.
- A **router and app-shell model** (View, Page, Navbar, Panel) that gives structure to multi-page apps, not just isolated widgets.
- A companion **"Kitchen Sink" demo app** that doubles as documentation and QA surface.

### 2.2 The gap in Compose Multiplatform today
- Compose Multiplatform ships layout primitives and Material 3, but many "everyday" components (action sheets, smart select, swipeout, sortable lists, messages/chat bubbles, calendar/date picker, 
photo browser) are left to each team to build or stitch together from disparate third-party libraries.
- iOS-style ("Cupertino") component sets exist in the ecosystem but are typically separate from Material implementations, so teams wanting *adaptive* behavior (iOS look on iOS, Material look on 
Android) must maintain two component sets and branch on platform themselves.
- This fragmentation is exactly the problem Framework7 solved for the web/hybrid world. Compose7's thesis: solve it once, idiomatically, for Compose.

### 2.3 Competitive landscape note: lessons from `compose-cupertino`

The closest existing analogue to Compose7's adaptive-theming layer is `compose-cupertino` (originally `alexzhirkevich/compose-cupertino`), and its history is a useful data point rather than a 
comfortable existence proof:

- The original repository has not shipped a release since an alpha tag in April 2024, despite accumulating roughly 1.6k stars and 60 forks — evidence of real demand, not evidence of a healthy 
release cadence.
- Active development moved to community forks — first `schott12521/compose-cupertino`, then `robinpcrd/compose-cupertino` — the latter created explicitly because upstream had "been inactive for 
months." That fork is the one shipping current releases and tracking current Kotlin/Compose versions, but each hand-off has forced consumers through an import-path migration.

Two takeaways for Compose7:
1. **Validates the thesis.** A single-maintainer library sustained meaningful adoption purely on the strength of "one API, adapts per platform" — the same bet Compose7 is making (Goal 2, §3).
2. **Cautions on governance.** A maintenance model resting on one person is fragile in this ecosystem, and the cost of it lapsing lands on consumers as forced migrations. This directly shapes the 
governance goal added below (§3.7), the risk entry in §18, and the open questions in §19.

**A specific, addressable gap.** The actively-maintained fork's published module lineup is a useful signal of exactly which gaps mattered enough to build for — alongside general upkeep, it ships a 
`cupertino-native` module, a `cupertino-decompose` navigation-integration module, and an extended SF Symbols icon pack. (This is read from the fork's published Maven module list, not from 
`compose-cupertino`'s issue tracker directly — search did not surface specific issue threads for this repo.) The most structural of the three is `cupertino-native`: a hand-painted Cupertino clone 
has to be manually re-drawn every time Apple revises its Human Interface Guidelines — most recently with the system-wide "Liquid Glass" redesign shipped in iOS 26 (Sept 2025) — and until that 
repaint happens, apps built on the library visibly lag behind real iOS. Compose7 addresses this directly with an opt-in native interop bridge — see §8.7 and the traceability table in **Appendix B**.

---

## 3. Goals

1. **Comprehensive catalog** — implement the large majority of Framework7's core component categories as native Compose components.
2. **Adaptive by default** — a component's visual language (spacing, motion, iconography, control style) automatically matches the host platform's conventions, with the ability to force a specific 
style.
3. **Idiomatic Compose APIs** — composable functions, state hoisting (`value` / `onValueChange`), `Modifier`-first customization; no imperative or DOM-like API surface.
4. **True multiplatform** — one artifact works across Android, iOS, and Desktop from day one; Web (Wasm/JS) is explicitly a post-1.0 addition (see Roadmap, Phase 5), not part of the 1.0 scope.
5. **Modular & lean** — consumers depend only on the component groups they use; no monolithic "god artifact."
6. **Documentation parity with Framework7** — a public component gallery / Kitchen-Sink-equivalent sample app and full API reference.
7. **Governance that scales, honestly staged.** Phase 1 (now): a single maintainer (achmadk) owns the repository and releases — the same starting point as most solo Compose Multiplatform libraries, 
including `compose-cupertino`'s original (§2.3). Phase 2 (triggered by securing sponsorship/funding): transition to an org-owned repository with 2+ maintainers holding publish access, rather than 
leaving that transition undefined until a crisis forces it.

## 4. Non-Goals

- **Not a full app framework — at 1.0.** Compose7 provides navigation *chrome* (Navbar, Tabbar, Panel) but does not ship its own router at 1.0; it integrates with existing Compose navigation 
solutions (Compose Navigation, Voyager, Decompose) rather than replacing them. A thin, optional router module is planned for a later, post-1.0 phase (see Roadmap, Phase 5) — it is a "yes, later," 
not a permanent non-goal.
- **Not a PWA/hybrid runtime.** No service-worker, Cordova/Capacitor-style native bridge, or web-view packaging — this is a native Compose UI toolkit only.
- **No bundled icon set of any kind.** Compose7 ships zero icon assets and no optional icon module — bring-your-own-icons on every platform, permanently, not just as a licensing safeguard against 
cloning SF Symbols but as a firm scope boundary (see §10, §11).
- **Not a design tool.** No Figma kit, no visual builder — code-first only (at least for v1).
- **No cross-framework ambition.** Kotlin/Compose only — not a target for React, Flutter, or other ecosystems.

## 5. Target Users & Use Cases

| Persona | Need |
|---|---|
| KMP app team (startup/product) | Ship an Android + iOS app fast with consistent, native-feeling components without hand-building overlays, pickers, and list chrome. |
| Team migrating off Framework7 (Cordova/Capacitor) | Wants a familiar component vocabulary and adaptive theming while moving to a native Compose codebase. |
| Agency / consultancy | Needs a reliable, well-documented component set to reuse across many client apps with different branding. |
| Solo/indie developer | Wants "batteries included" so they can focus on product logic, not rebuilding a design system. |

## 6. Design Principles

1. **Adaptive by default, override-able.** Platform is detected automatically and drives visual style; a `LocalCompose7Style` (or theme parameter) lets teams force a single look (e.g., Material 
everywhere) if they don't want platform divergence.
2. **Composable & slot-based.** Components expose leading/trailing/content slots as `@Composable` lambdas (mirroring `ListItem` in Material3), not fixed prop lists.
3. **Token-driven theming.** Color, spacing, typography, and shape come from a token set per style (iOS-like, Material, and **DesktopUI7** — an original Compose7 visual language, not modeled on 
Framework7's Aurora theme), each with light/dark variants.
4. **Compose-native state model.** `value` + `onValueChange`, `expanded` + `onExpandedChange`, etc. — no internal hidden mutable state in the public API beyond convenience "uncontrolled" overloads.
5. **Accessibility is not optional.** Every interactive component ships correct semantics, minimum touch targets, and contrast-safe token defaults.
6. **Lean dependency graph.** Core has no dependencies beyond Kotlin/Compose; integrations (image loading, navigation) are opt-in modules, not requirements.

---

## 7. Scope: Component Inventory & Prioritization

Priorities: **P0** = MVP (Phase 0–1), **P1** = near-term follow-up, **P2** = later / stretch.

| Category | Components | Priority |
|---|---|---|
| App shell & layout | Theme provider, App root, Page, Block/content block, Safe-area handling | P0 |
| Navigation chrome | Navbar (incl. large-title collapse), Toolbar/Tabbar, Subnavbar, Panel (side drawer), Tabs, Segmented control | P0 |
| Lists | List, List Item, List Button, Simple/Grouped list, Contacts-style list, Virtual/lazy list (built on `LazyColumn`) | P0 |
| Buttons & indicators | Button, FAB, Badge, Chip/Tag, Icon wrapper, Progressbar, Preloader/spinner, Gauge | P0 |
| Cards | Card, Expandable card | P0 |
| Forms — basic | Text input, Checkbox, Radio, Toggle/Switch, Stepper, Range slider | P0 |
| Overlays & modals | Dialog/Alert, Action Sheet, Popover, Popup, Sheet Modal (bottom sheet), Toast/Notification | P0 |
| Search | Searchbar | P0 |
| Forms — advanced | Smart Select, Autocomplete, Calendar/Date Picker, Color Picker | P1 |
| List interactions | Sortable list, Swipeout (swipe actions), List Index (fast-scroll index) | P1 |
| Loading states | Skeleton placeholders | P1 |
| Data display | Accordion, Timeline, Breadcrumbs, Grid/layout-grid helpers | P1 |
| Media | Swiper/Carousel, Photo Browser (gallery viewer), lazy image loading integration | P1–P2 |
| Messaging | Messagebar, Messages (chat bubble list) | P2 |
| Templates | Login screen template, Statusbar tint helper | P2 |
| Scroll behaviors | Pull-to-refresh, infinite scroll helper | P1 |
| Tooltip | Tooltip | P2 |

Full one-to-one mapping against Framework7's own catalog is in **Appendix A**.

---

## 8. Functional Requirements (by category)

### 8.1 Theming
- `Compose7Theme { ... }` composable at the app root supplies tokens via `CompositionLocal`.
- Auto-detects platform (Android → Material style, iOS → Cupertino-like style, Desktop → **DesktopUI7**, an original Compose7 visual language not modeled on Framework7's Aurora theme) and 
light/dark from system settings.
- Exposes an explicit `style: Compose7Style` override (`Material`, `Cupertino`, `DesktopUI7`, or `Auto`) so teams can force one look everywhere.
- Supports Android 12+ dynamic color as an opt-in token source.

### 8.2 Navigation chrome
- Navbar supports title, subtitle, leading/trailing action slots, and an iOS-style large-title-to-compact collapse tied to scroll position.
- Tabbar/Tabs support icon+label, badge overlays, and both top and bottom placements.
- Panel (side drawer) supports left/right placement, reveal and overlay presentation modes.

### 8.3 Lists
- List/List Item support leading media (icon/avatar/image), title/subtitle, trailing content, and platform-correct divider insets (inset on Cupertino style, edge-to-edge on Material style).
- Swipeout exposes leading/trailing swipe actions with configurable thresholds.
- Virtual/lazy list is a thin, styled wrapper over `LazyColumn`/`LazyRow` — no independent virtualization engine.

### 8.4 Overlays
- Action Sheet, Popup, and Sheet Modal each have a platform-idiomatic presentation animation (card-style slide-up on Cupertino, standard bottom sheet motion on Material) sharing one public API.
- Dialog supports title, message, and 1–3 action buttons including a destructive style.
- Toast/Notification support auto-dismiss duration, swipe-to-dismiss, and stacking.

### 8.5 Forms
- All inputs use `value`/`onValueChange` (or `checked`/`onCheckedChange`, etc.), an `isError` flag, and a supporting-text slot for validation messages.
- Smart Select opens a Material-style list dialog on Android-style theme and a wheel-picker sheet on Cupertino-style theme, matching Framework7's own behavior split.
- Calendar/Date Picker supports single date, range, and inline vs. modal presentation.

### 8.6 Media
- Swiper supports paging, indicator dots, and both horizontal/vertical axes.
- Photo Browser supports pinch-zoom, swipe-to-dismiss, and a pluggable image-loading slot (so the library doesn't hard-depend on a specific image loader).

### 8.7 Native Interop Bridge (iOS)
- Addresses the most structural gap identified in comparable prior art (§2.3): a pure Compose-drawn approximation of iOS's visual language has to be manually re-painted every time Apple revises its 
Human Interface Guidelines — most recently the system-wide "Liquid Glass" redesign shipped with iOS 26 (Sept 2025) — and until that repaint lands, apps built on it visibly lag behind real iOS.
- For a defined, opt-in subset of high-fidelity, high-drift-risk components, Compose7 renders through actual native UIKit views via Compose Multiplatform's iOS interop layer instead of a hand-drawn 
Compose approximation. Illustrative starting candidates: Alert, Action Sheet, Share Sheet, Context Menu, and the Date/Time wheel picker — the **exact list and rollout order is finalized as a Phase 0 
planning deliverable** (§16), before any bridge component is built, rather than decided ad hoc once implementation is underway.
- Default behavior stays pure-Compose (consistent cross-platform rendering, testability, and Android/Desktop parity); the native bridge is an explicit opt-in (e.g., `Compose7Theme(nativeBridge = 
true) { ... }`, or a per-component override), so teams choose fidelity vs. consistency deliberately rather than getting silently different behavior across iOS versions.
- Native-bridged components automatically inherit whatever visual language the user's actual iOS version renders (Liquid Glass today, whatever supersedes it later), removing the "re-paint after 
every WWDC" maintenance tax for exactly the components where end users notice fidelity most.
- Explicitly iOS-only. Android and Desktop continue to use Compose7's own token-driven Material/`DesktopUI7` styles — this bridge does not attempt to solve the equivalent problem on other platforms, 
where no comparable "always current native reference" exists in the same way.

---

## 9. Theming & Adaptive System — Key Differentiator

This is the feature most directly inherited from Framework7 and the primary reason to choose Compose7 over assembling Material3 + a separate Cupertino-style package by hand:

- **Single API, dual (or triple) rendering.** `Compose7Button(...)` renders as a Material button on Android and a Cupertino-style button on iOS from identical call-site code.
- **Token layering**: `ColorTokens`, `TypeTokens`, `ShapeTokens`, `MotionTokens` per style, each with light/dark variants — analogous in spirit to Material3's token system but spanning multiple 
visual languages.
- **Escape hatches at every level**: override tokens globally (rebrand), override style per-subtree (mix styles intentionally), or override a single component instance (one-off design need).
- **No silent divergence**: differences between styles are documented per component (e.g., "Smart Select" behavior table) so teams aren't surprised by platform-specific behavior.
- **Fidelity where it matters, by choice**: for the highest-drift-risk iOS components, teams can opt into rendering through real native UIKit views instead of a Compose approximation — see the 
Native Interop Bridge (§8.7), added specifically to close a gap identified in comparable prior art (§2.3).

---

## 10. Technical Architecture

- **Module layout**: `core` (theme engine, tokens, platform detection), `foundation` (layout primitives, list scaffolding), `components-navigation`, `components-lists`, `components-forms`, 
`components-overlays`, `components-media`, `sample-app`. No icon module of any kind — icons are a caller-supplied slot, never a Compose7 dependency (§4, §11).
- **Targets, phased**: `androidTarget` and `iosArm64/iosX64/iosSimulatorArm64` first; `jvm` (Desktop) next; `wasmJs`/`js` explicitly post-1.0 (Roadmap Phase 5), gated on Compose Multiplatform's web 
support and this library's own dependencies being stable there.
- **`expect`/`actual` kept minimal** — reserved for genuine platform needs (platform/theme detection, haptics, system bar tinting), with the large majority of code in `commonMain`.
- **Built on current stable Compose Multiplatform and Kotlin** releases, tracking JetBrains' release cadence; CI matrix tests against the last two stable Compose Multiplatform versions.
- **Publishing** via Maven Central using the standard Kotlin Multiplatform publish plugin, with semantic versioning and alpha/beta/stable channels.

## 11. API Design Guidelines

- Every visual component is a public `@Composable` function with `modifier: Modifier = Modifier` as a parameter, following standard Compose convention.
- State is hoisted: callers own `value`/`onValueChange` (or equivalent); "uncontrolled" convenience overloads use `rememberSaveable` internally but are thin wrappers over the controlled version.
- Content customization uses `@Composable` slot lambdas (leading/trailing/content), matching Material3's `ListItem`-style API rather than long fixed parameter lists.
- **Icons are bring-your-own, permanently**: components accept an `ImageVector`/composable icon slot; Compose7 bundles no icon font and offers no optional icon module, so teams supply Material 
Symbols, a custom set, or their own iOS-appropriate assets without licensing entanglement or a dependency to track.
- No assumption of a specific DI or navigation library; the sample app includes adapter examples for Compose Navigation, Voyager, and Decompose, but core modules don't depend on any of them.

## 12. Non-Functional Requirements

- **Accessibility**: correct `Modifier.semantics`, TalkBack/VoiceOver-equivalent labeling, minimum touch targets per platform guideline (48dp / 44pt), and default tokens meeting WCAG-AA contrast 
where feasible.
- **Internationalization**: automatic RTL mirroring via `LayoutDirection`; no hard-coded left/right assumptions in layout logic.
- **Performance**: recomposition overhead benchmarked against hand-written equivalents; lazy-list-based components must not regress `LazyColumn` baseline scroll performance.
- **Footprint**: modular artifacts so an app using only, say, `components-lists` and `components-forms` doesn't pull in media or messaging code.

## 13. Testing Strategy

- Unit tests for non-UI logic (state holders, token resolution, date/range logic for the calendar component).
- Screenshot/golden tests per platform and per style (Material/Cupertino/DesktopUI7 × light/dark) to catch visual regressions.
- A **Kitchen-Sink sample app** exercising every component in every style/mode combination, used both as manual QA surface and as the source for documentation screenshots — directly mirroring 
Framework7's own Kitchen Sink demo.

## 14. Documentation & Sample App

- Dokka-generated API reference published alongside releases.
- A component gallery site (or in-repo docs) with usage snippets per component and per style.
- A **Framework7 → Compose7 concept map** (Appendix A) specifically to shorten onboarding for teams coming from Framework7/Cordova/Capacitor backgrounds.
- A **`compose-cupertino` migration/compatibility guide** — confirmed as planned, but scheduled for post-1.0 (Roadmap Phase 5), not a 1.0 requirement.

## 15. Distribution, Licensing, Versioning

- Open source under the **MIT License** — chosen over Apache 2.0 despite Apache 2.0 being the more common norm among Kotlin/Compose libraries (including `compose-cupertino`'s fork lineage, §2.3), 
for simplicity and maximum permissiveness.
- Maven Central distribution, semantic versioning, public changelog, and clearly labeled alpha → beta → stable tracks per module (not all modules need to reach 1.0 simultaneously).

## 16. Roadmap (Phased)

| Phase | Focus | Target platforms |
|---|---|---|
| Phase 0 — Foundations | Theme engine, token system, adaptive style switching, Button, Card, List/List Item, Navbar, Page; **finalize Native Interop Bridge component list & rollout order (§8.7) — 
planning only, no bridge code shipped yet** | Android, iOS |
| Phase 1 — Forms & Overlays | Text input, Checkbox, Radio, Toggle, Stepper, Range slider, Dialog, Action Sheet, Sheet Modal, Toast; **Native Interop Bridge infra + implementation of the first 
components finalized in Phase 0 (candidates: Alert, Action Sheet)** | Android, iOS |
| Phase 2 — Navigation & Advanced Pickers | Tabs, Segmented control, Panel, Searchbar, Smart Select, Autocomplete, Calendar; **remaining Phase-0-finalized Native Bridge components (candidates: 
Date/Time picker, Context Menu, Share Sheet)** | Android, iOS |
| Phase 3 — Media & Data Display | Swiper, Photo Browser, Accordion, Timeline, Skeleton, Swipeout, Sortable list | Android, iOS, Desktop |
| Phase 4 — Expansion & 1.0 | Messages/Messagebar, remaining P2 items, Desktop polish, **1.0 stable release** | Android, iOS, Desktop |
| Phase 5 — Post-1.0 | Web/Wasm target; thin optional router module; `compose-cupertino` migration/compatibility guide | Android, iOS, Desktop, Web |

## 17. Success Metrics

- **Coverage**: % of Framework7 core component categories implemented at 1.0 (target: all P0 + P1 categories).
- **Adoption**: Maven Central download trend and GitHub stars over the first two release cycles.
- **Quality**: crash-free sessions in the sample app; count of open critical/high-severity issues trending down release over release.
- **Performance**: recomposition/scroll benchmarks within an agreed tolerance of hand-written baselines.
- **Community health**: external contributors, median issue-response and PR-review time.

## 18. Risks & Mitigations

| Risk | Mitigation |
|---|---|
| Compose Multiplatform's Web/Wasm and iOS interop are less mature than Android | Phase rollout: Android + iOS first, Desktop next, Web last, gated on ecosystem readiness. |
| Framework7's catalog is large — scope creep is easy | Strict P0/P1/P2 prioritization and phased roadmap (§7, §16); no new component added to a phase without dropping or deferring another. |
| Some teams want one unified brand look, not per-platform divergence | Global style override (`Material`/`Cupertino`/`DesktopUI7`/`Auto`) documented as a first-class use case, not an escape hatch. 
|
| Fast-moving Compose Multiplatform releases can break APIs | CI matrix across the last two stable CMP versions; clear deprecation policy. |
| Icon/asset licensing (e.g., anything resembling SF Symbols) | No bundled iOS-style icon font; icons are always a pluggable slot (§11). |
| Single-maintainer dependency risk — precedent: `compose-cupertino`'s original repo going dormant post-alpha (no release since Apr 2024), forcing two rounds of consumer migration through community 
forks (§2.3). Compose7 starts from the same structural position: sole maintainer (achmadk), personal account. | Transition trigger is defined up front rather than left open-ended: once the project 
secures sponsorship/funding (§3.7), move to an org-owned repository with 2+ maintainers holding publish access. Until then, reduce lapse-impact with automated CI releases and a documented succession 
note in the README, so the risk is disclosed rather than hidden. |
| Native Interop Bridge risk (§8.7) — embedding real UIKit views inside a Compose hierarchy adds interop complexity (state sync, sizing/measurement edge cases, accessibility bridging) and couples a 
subset of components to UIKit API changes across iOS versions | Component list and rollout order are fixed during Phase 0 planning before any bridge code is written (§16), keeping scope deliberate 
rather than ad hoc; the bridge stays strictly opt-in with a pure-Compose fallback always available per component; track iOS betas (including future HIG revisions) in CI so bridge components aren't 
caught off guard by the next redesign. |

## 19. Decisions & Open Questions

### 19.1 Resolved (v0.4)

| # | Question | Decision |
|---|---|---|
| 1 | Final library name/branding | **Compose7** — confirmed, no longer a placeholder. |
| 2 | Is Web/Wasm in scope for 1.0? | **No** — explicitly deferred to post-1.0 (Roadmap Phase 5). |
| 3 | Bundle an icon set, or bring-your-own? | **Bring-your-own-icons**, permanently — no bundled or optional icon module (§4, §10, §11). |
| 4 | Ship an optional router module? | **Yes, but later** — planned for post-1.0 (Roadmap Phase 5), not part of 1.0. |
| 5a | License: Apache 2.0 vs. MIT? | **MIT** (§15). |
| 5b | Governance model / initial maintainer | **achmadk, sole maintainer, starting now.** Org-ownership + 2+ maintainers is the target state, triggered by securing sponsorship (§3.7, §18) — not an 
open-ended "someday." |
| 6 | Mimic Framework7's Aurora desktop theme, or invent an original one? | **Invent an original Compose7 visual language, named `DesktopUI7`** — not modeled on Aurora. A quick naming-conflict check 
turned up nothing under that name or "Desktop7" in the Kotlin/Compose or wider OSS space as of this check (§6, §8.1, §13, §18). |
| 7 | Offer a `compose-cupertino` migration path? | **Yes, but later** — planned for post-1.0 (Roadmap Phase 5), not part of 1.0. |
| 8 | When should the exact Native Interop Bridge component list be decided? | **During Phase 0**, as an explicit planning deliverable (§8.7, §16) — not left to be worked out once implementation is 
already underway in Phase 1. |

### 19.2 Still open

Nothing outstanding at this time — new questions will be logged here as they arise.

---

## Appendix A: Framework7 → Compose7 Component Mapping

| Framework7 Component | Compose7 Equivalent | Priority |
|---|---|---|
| App / View | Theme provider / App root | P0 |
| Page | `Page` | P0 |
| Navbar | `Navbar` (large-title support) | P0 |
| Subnavbar | `Subnavbar` | P0 |
| Toolbar / Tabbar | `Toolbar` / `Tabbar` | P0 |
| Panel / Side Panels | `Panel` (drawer) | P0 |
| Block / Content Block | `Block` | P0 |
| List View / List Item | `List` / `ListItem` | P0 |
| List Button | `ListButton` | P0 |
| List Index | `ListIndex` | P1 |
| Virtual List | Styled `LazyColumn` wrapper | P0 |
| Sortable | `SortableList` | P1 |
| Contacts List | `ContactsList` (List preset) | P1 |
| Swipeout | `Swipeout` (swipe actions) | P1 |
| Card | `Card`, `ExpandableCard` | P0 |
| Button | `Button` | P0 |
| Floating Action Button | `FAB` | P0 |
| Badge | `Badge` | P0 |
| Chips / Tags | `Chip` | P0 |
| Icon | Icon slot wrapper | P0 |
| Progressbar | `ProgressBar` | P0 |
| Preloader | `Preloader`/spinner | P0 |
| Gauge | `Gauge` | P1 |
| Inputs / Form Inputs | `TextField` and variants | P0 |
| Checkbox | `Checkbox` | P0 |
| Radio | `RadioButton` | P0 |
| Toggle | `Switch` | P0 |
| Stepper | `Stepper` | P0 |
| Range Slider | `RangeSlider` | P0 |
| Smart Select | `SmartSelect` | P1 |
| Autocomplete | `Autocomplete` | P1 |
| Calendar | `DatePicker`/`Calendar` | P1 |
| Color Picker | `ColorPicker` | P2 |
| Searchbar | `SearchBar` | P0 |
| Tabs | `Tabs` | P0 |
| Breadcrumbs | `Breadcrumbs` | P1 |
| Accordion | `Accordion` | P1 |
| Grid / Layout Grid | Grid layout helpers | P1 |
| Action Sheet / Actions | `ActionSheet` | P0 |
| Popover | `Popover` | P0 |
| Popup | `Popup` | P0 |
| Sheet Modal | `BottomSheet` | P0 |
| Dialog / Alert | `Dialog` | P0 |
| Toast / Notification | `Toast`, `Notification` | P0 |
| Swiper | `Swiper`/carousel | P1 |
| Photo Browser | `PhotoBrowser` | P2 |
| Messagebar | `MessageBar` | P2 |
| Messages | `MessageList` (chat bubbles) | P2 |
| Login Screen | Login screen template | P2 |
| Statusbar | Status-bar tint helper | P2 |
| Tooltip | `Tooltip` | P2 |
| Skeleton (loading placeholders) | `Skeleton` | P1 |
| Pull-to-refresh / Infinite scroll | Scroll-behavior helpers | P1 |

*(Compiled against Framework7's core and Vue component reference lists.)*

---

## Appendix B: `compose-cupertino` Gaps → Compose7 Mitigations

Every `compose-cupertino`-related gap raised anywhere in this PRD, gathered in one place for traceability. Evidence basis is noted per row — most of this is inferred from the actively-maintained 
fork's public module lineup and release history (§2.3), not from a direct read of the original repo's issue tracker, which search did not surface specific threads from.

| Gap observed | Evidence | Compose7 mitigation | Where |
|---|---|---|---|
| Pure Compose-drawn iOS widgets drift from real iOS every time Apple revises its HIG (e.g., "Liquid Glass" in iOS 26) | Fork ships a `cupertino-native` module | **Native Interop Bridge** — opt-in 
real UIKit views for the highest-drift-risk components; exact list finalized in Phase 0 | §8.7, §9, Roadmap Phase 0 (planning) → 1–2 (implementation) |
| No first-party navigation/routing integration | Fork ships a `cupertino-decompose` module | Thin, optional router module — deliberately deferred to post-1.0 rather than rushed | §4, Roadmap Phase 
5 (decided v0.3) |
| Limited SF Symbols icon coverage in the original | Fork ships an extended `cupertino-icons-extended` icon pack | Deliberately out of scope — Compose7 is bring-your-own-icons, permanently, so teams 
pair with any icon pack (including community SF Symbols sets) without Compose7 owning that surface | §4, §10, §11 (decided v0.3) |
| Original maintainer went dormant post-alpha (no release since Apr 2024), forcing two rounds of consumer migration through forks | Original repo's own release history | Governance plan with a named 
starting maintainer and a defined (not open-ended) transition trigger to org-ownership | §3.7, §18, §19 (decided v0.4) |
| Each fork hand-off forced an import-path migration on consumers | Fork lineage's own migration notes | A `compose-cupertino` migration/compatibility guide, planned post-1.0 | §14, Roadmap Phase 5 
(decided v0.3) |

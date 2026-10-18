# Changelog

All notable changes to Compose7 are documented here. The format follows
[Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and the project
adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

- **Theme engine** (`core`): `Compose7Theme` with three visual languages
  (Material, Cupertino, DesktopUI7) + `Auto` platform detection, token sets
  for color/type/shape/motion with light & dark variants, Android 12+ dynamic
  color opt-in, and WCAG-AA-checked `onX` color pairs.
- **Foundation**: `Compose7Surface`, `Page`/`LazyPage`, `Card`
  (+ expandable), `List` scaffolding primitives, `Grid`, drawn glyphs
  (check/plus/minus), press & toggle interaction helpers with haptics and
  platform-correct touch targets, `PullToRefresh`, `InfiniteScrollEffect`.
- **Buttons** (`components-buttons`): `Button` (4 variants × 3 sizes,
  style-specific renderings), `FAB`, `Chip`, `Badge`/`BadgeCount`/`BadgeDot`.
- **Indicators** (`components-indicators`): `ProgressBar`
  (determinate/indeterminate), `Preloader`, `Gauge`, `Skeleton`.
- **Navigation** (`components-navigation`): `Navbar` (large-title collapse),
  `Toolbar`, `Subnavbar`, `Tabbar` (top or bottom), `Tabs` (icon + label,
  indicator on either edge), `SegmentedControl`, `Panel`, `Searchbar`.
- **Lists** (`components-lists`): `List` (plain/grouped), `ListItem`,
  `ListButton`, `ListSection`, `ContactsList`, `Swipeout`, `SortableList`,
  `ListIndex`, `VirtualList` (vertical `LazyColumn` or horizontal `LazyRow`).
- **Overlays** (`components-overlays`): `Dialog` (+ destructive actions),
  `ActionSheet`, `ShareSheet`, `ContextMenu`, `SheetModal` (drag-to-dismiss),
  `Popover`, `Popup`, `Toast` + `ToastHost` (stacking), `Notification` +
  `NotificationHost`, `Tooltip`.
- **Forms** (`components-forms`): `TextField`, `TextArea`, `Checkbox`,
  `Radio`/`RadioGroup`, `Switch`, `Stepper`, `Slider`, `RangeSlider`,
  `SmartSelect` (list dialog on Material, wheel sheet on Cupertino, list sheet
  on DesktopUI7), `Autocomplete`, `Calendar` + `CalendarRange` + `DatePicker` +
  `DateRangePicker` (+ `CalendarDate` date math), `ColorPicker`. Every input
  exposes `isError` and a supporting-text slot.
- **Data display** (`components-data`): `Accordion` (single/multiple),
  `Timeline`, `Breadcrumbs`.
- **Media** (`components-media`): `Swiper`, `PhotoBrowser`.
- **Messaging** (`components-messaging`): `MessageList`, `MessageBar`.
- **Templates** (`templates`): `LoginTemplate`.
- **Native Interop Bridge (iOS, opt-in)**: `Dialog`, `ActionSheet`,
  `DatePicker` (wheel), `ShareSheet`, and `ContextMenu` can render through real
  UIKit views (`UIAlertController`, `UIDatePicker`, `UIActivityViewController`,
  `UIContextMenuInteraction`); list and rollout order in
  [docs/NATIVE_BRIDGE.md](docs/NATIVE_BRIDGE.md). Pure Compose remains the
  default on every platform.
- **Unit tests** for calendar bounds and range selection, autocomplete
  filtering, share-sheet fallback ordering, context-menu placement,
  toast/notification queues, and golden/screenshot baselines for indicators
  across every style × brightness (`desktopTest`).
- **Sample app** (`sample-app`, `desktop-app`, `android-app`): Kitchen Sink
  gallery exercising every component in every style. A checked-in XcodeGen
  shell (`iosApp/`) hosts it on iOS; CI builds that app for the simulator.

[Unreleased]: https://github.com/compose7/compose7/compare/HEAD...HEAD

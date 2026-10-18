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
  `Toolbar`, `Subnavbar`, `Tabbar`, `Tabs`, `SegmentedControl`, `Panel`,
  `Searchbar`.
- **Lists** (`components-lists`): `List` (plain/grouped), `ListItem`,
  `ListButton`, `ListSection`, `ContactsList`, `Swipeout`, `SortableList`,
  `ListIndex`, `VirtualList`.
- **Overlays** (`components-overlays`): `Dialog` (+ destructive actions),
  `ActionSheet`, `SheetModal` (drag-to-dismiss), `Popover`, `Popup`,
  `Toast`, `Tooltip`.
- **Forms** (`components-forms`): `TextField`, `TextArea`, `Checkbox`,
  `Radio`/`RadioGroup`, `Switch`, `Stepper`, `Slider`, `RangeSlider`,
  `SmartSelect`, `Autocomplete`, `Calendar` (+ `CalendarDate` date math),
  `ColorPicker`.
- **Data display** (`components-data`): `Accordion` (single/multiple),
  `Timeline`, `Breadcrumbs`.
- **Media** (`components-media`): `Swiper`, `PhotoBrowser`.
- **Messaging** (`components-messaging`): `MessageList`, `MessageBar`.
- **Templates** (`templates`): `LoginTemplate`.
- **Native Interop Bridge (iOS, opt-in)**: `Dialog` and `ActionSheet` can
  render through real `UIAlertController` views; list and rollout order in
  [docs/NATIVE_BRIDGE.md](docs/NATIVE_BRIDGE.md). Pure Compose remains the
  default on every platform.
- **Sample app** (`sample-app`, `desktop-app`, `android-app`): Kitchen Sink
  gallery exercising every component in every style.

[Unreleased]: https://github.com/compose7/compose7/compare/HEAD...HEAD

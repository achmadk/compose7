# Component index

Every public entry point in Compose7, grouped the way PRD §7 groups them.
`P0`/`P1`/`P2` are the PRD priorities; all of them ship in 0.1.0.

## App shell & layout — `core`, `foundation`

| API | Notes | PRD |
|---|---|---|
| `Compose7Theme` | Style, brightness, token overrides, dynamic color, native bridge | P0 |
| `Compose7Style` | `Auto`, `Material`, `Cupertino`, `DesktopUI7` | P0 |
| `Page`, `LazyPage` | Safe-area-aware scroll containers | P0 |
| `Block` | Content block with the style's list inset | P0 |
| `Compose7SystemBarAppearance` | Status-bar tint helper | P2 |
| `PullToRefresh` | Nested-scroll refresh container | P1 |
| `InfiniteScrollEffect` | Paging trigger for `LazyListState` | P1 |

## Navigation chrome — `components-navigation`

`Navbar` (large-title collapse) · `Toolbar` · `Subnavbar` · `Tabbar` ·
`Tabs` · `SegmentedControl` · `Panel` (overlay/reveal) · `Searchbar`.

## Lists — `components-lists`

`List` (plain/grouped) · `ListItem` · `ListButton` · `ListSection` ·
`ContactsList` · `Swipeout` · `SortableList` · `ListIndex` · `VirtualList`.

## Buttons & indicators — `components-buttons`, `components-indicators`

`Button` (Filled/Tonal/Outline/Text × Small/Medium/Large) · `FAB` · `Chip` ·
`Badge`/`BadgeCount`/`BadgeDot` · `ProgressBar` · `IndeterminateProgressBar` ·
`Preloader` · `Gauge` · `Skeleton`/`SkeletonText`.

## Cards — `foundation`

`Card` · `ExpandableCard`.

## Forms — `components-forms`

| API | Priority |
|---|---|
| `TextField` · `TextArea` | P0 |
| `Checkbox` · `Radio`/`RadioGroup` · `Switch` · `Stepper` · `Slider` · `RangeSlider` | P0 |
| `SmartSelect` · `Autocomplete` · `Calendar` (+ `CalendarDate`) · `ColorPicker` | P1/P2 |

## Overlays — `components-overlays`

`Dialog` · `ActionSheet` · `SheetModal` · `Popover` · `Popup` · `Toast` ·
`Tooltip`. `Dialog` and `ActionSheet` can route through the opt-in iOS Native
Interop Bridge — see [NATIVE_BRIDGE.md](NATIVE_BRIDGE.md).

## Media — `components-media`

`Swiper` (horizontal/vertical pager + style-driven dots) · `PhotoBrowser`
(zoom, pan, swipe-to-dismiss).

## Data display — `components-data`

`Accordion` (single/multiple) · `Timeline` · `Breadcrumbs` · `Grid` (in
`foundation`).

## Messaging — `components-messaging`

`MessageList` (bubbles, grouping, separators) · `MessageBar` (growing input).

## Templates — `templates`

`LoginTemplate` — a complete sign-in screen; authentication stays in the app.

## Icons

Compose7 ships none (PRD §4). `Icon`/`IconBox` accept an `ImageVector` or
`Painter` you supply; every affordance Compose7 draws itself (checks, chevrons,
plus/minus, steppers, page dots, gauges) is a `Canvas` primitive. Pass `null`
as `contentDescription` only when the icon is decorative.

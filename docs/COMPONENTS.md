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

`Navbar` (large-title collapse) · `Toolbar` · `Subnavbar` · `Tabbar` (top or
bottom placement) · `Tabs` (icon + label, indicator on either edge) ·
`SegmentedControl` · `Panel` (overlay/reveal) · `Searchbar`.

## Lists — `components-lists`

`List` (plain/grouped) · `ListItem` · `ListButton` · `ListSection` ·
`ContactsList` · `Swipeout` · `SortableList` · `ListIndex` · `VirtualList`
(vertical `LazyColumn` or horizontal `LazyRow`).

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
| `SmartSelect` · `Autocomplete` · `Calendar` / `CalendarRange` · `DatePicker` / `DateRangePicker` · `ColorPicker` | P1/P2 |

Every input takes `isError` and a `supportingText` slot (PRD §8.5).

## Overlays - `components-overlays`

`Dialog` · `ActionSheet` · `ShareSheet` · `ContextMenu` · `SheetModal` ·
`Popover` · `Popup` · `Toast` / `ToastHost` · `Notification` /
`NotificationHost` · `Tooltip`. `Dialog`, `ActionSheet`, `ContextMenu`,
`DatePicker` (in `components-forms`), and `ShareSheet` can route through the
opt-in iOS Native Interop Bridge — see [NATIVE_BRIDGE.md](NATIVE_BRIDGE.md).

## Style behavior notes

PRD §9 asks for documented divergence rather than surprises:

| Component | Material | DesktopUI7 | Cupertino |
|---|---|---|---|
| `SmartSelect` | List dialog with checkmark | List sheet with checkmark | Drawn wheel picker + Done row |
| `Tabbar` | Bottom or top, no hairline | Bottom or top, border on the screen edge | Bottom or top, flat surface + hairline |
| `Dialog` actions | Horizontal row | Horizontal row | Stacked when >2 actions |
| `ProgressBar`/`Gauge` | Rounded token radius | Rounded token radius | Thinner track/stroke |

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

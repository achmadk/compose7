# Theming

`Compose7Theme` is the single entry point. It resolves a visual language,
assembles that style's token set for the active brightness, and provides
everything components read through composition locals.

```kotlin
Compose7Theme {                              // Auto: follow the host platform
    App()
}

Compose7Theme(style = Compose7Style.Cupertino, darkTheme = true) {
    App()
}
```

## Styles

| `Compose7Style` | Host default | Feel |
|---|---|---|
| `Auto` | — | Resolves to the platform's style; never observed by components |
| `Material` | Android | Filled/tinted surfaces, outlined inputs, 48dp targets |
| `Cupertino` | iOS | Grouped inset lists, card sheets, 44pt targets, hairlines |
| `DesktopUI7` | Desktop | Compose7's own compact desktop language |

A component never asks "which OS am I on" — it asks for tokens and for
`currentCompose7Style` when a *structural* difference exists (e.g. Cupertino
presents sheets as cards). PRD §9.

## Token sets

| Local | Accessor | Contains |
|---|---|---|
| `LocalCompose7ColorScheme` | `Compose7ThemeColors` | 21 color roles, `onX` pairs |
| `LocalCompose7Typography` | `Compose7ThemeTypography` | 14 text roles |
| `LocalCompose7Shapes` | `Compose7ThemeShapes` | corner sizes + per-component shapes |
| `LocalCompose7Motion` | `Compose7MotionTokens` | durations + easings |
| `LocalCompose7StyleProperties` | `Compose7StyleInfo` | list insets, sheet motion, haptics, large-title support |

Override any of them wholesale:

```kotlin
Compose7Theme(colorScheme = myBrandScheme) { App() }   // rebrand
Compose7Theme(typography = myType) { App() }            // house font
```

`Compose7Shapes`/`Compose7Motion` are data classes, so `copy(...)` is the
practical way to nudge one value.

## Dynamic color (Android 12+)

```kotlin
Compose7Theme(dynamicColor = true) { App() }
```

Wallpaper colors are mapped onto Compose7's own token shape rather than
pulling in Material3. `withDynamicColors` regenerates the container/`onX`
pairs so contrast holds against whatever accent the system supplies.

## Accessibility deltas from stock platform colors

Compose7's palette follows the platform languages, but two iOS accents are
already below WCAG AA as used (white text on a filled control):

| Token | Platform value | Compose7 | Ratio |
|---|---|---|---|
| Cupertino light `primary` | `#007AFF` | `#0066CC` | 4.02 → 5.57 |
| Cupertino light `error` | `#FF3B30` | `#D70015` | 3.55 → 5.38 |
| Cupertino dark `onPrimary` | white | `#001428` | 3.65 → 5.09 |
| Cupertino dark `onError` | white | `#2B0705` | 3.41 → 6.16 |

The bright iOS blue and red are kept where they are *text* on black (they read
fine there), which is why dark mode darkens the label instead of desaturating
the accent. `TokenContrastTest` enforces every `onX` pair in all six palettes,
so a future token tweak that regresses this fails CI rather than shipping.

## Minimum touch targets

`currentMinTouchTarget()` returns 44dp under Cupertino and 48dp otherwise;
`Modifier.compose7MinimumTouchTarget()` expands the hit area without changing
visual size. Icon-only controls take a `contentDescription`; decorative ones
should pass `null` only when a label exists nearby.

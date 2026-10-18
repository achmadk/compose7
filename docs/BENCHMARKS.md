# Benchmarks

PRD §12/§17 ask for recomposition and scroll cost to be measured against
hand-written equivalents instead of asserted by feel. `:benchmarks` is a
desktop-only, unpublished module that does exactly that.

```bash
./gradlew :benchmarks:benchmark
```

## What is measured

| Workload | Compose7 side | Baseline side |
|---|---|---|
| Recomposition | 10 `Button`s whose labels read a state value that changes every frame | two variants: **bare** (10 raw `Box` + `BasicText` rows) and **parity** (the same rows plus `clickable`, button semantics, and a 48dp touch target) |
| Scroll | `VirtualList` (styled `LazyColumn`) with 500 `ListItem`s | bare `LazyColumn` with 500 two-line rows |

Both sides render offscreen through `ImageComposeScene` at density 1 with
Skiko's software rasterizer, so the numbers do not depend on a GPU or a display.
The scene clock advances 16 ms per frame, and each measured frame includes the
scroll step or state change.

The **parity** baseline exists to answer one question honestly: is Compose7
slower than raw Compose because of Compose7, or because a real button does more
than a colored box? It carries the interaction and accessibility cost a
component must have.

## Methodology and tolerance

- 12 warm-up frames, then 40 measured frames; the reported number is the
  **median** frame time, which resists GC pauses better than a mean.
- Each variant is measured **3 times** and its best median is kept. Without
  that repeat, JIT warm-up favoured whichever variant ran last — an earlier
  version of this harness reported the feature-parity baseline as *faster* than
  the bare one, which is impossible.
- Compose7 fails the run only when it exceeds the baseline by more than
  `COMPOSE7_BENCH_MAX_RATIO` (default **3.0**). The tolerance is deliberately
  generous: CI runners are shared and noisy, and this harness is meant to catch
  order-of-magnitude regressions, not to police a 5% drift.
- For a tighter measurement, run locally with a lower ratio:

  ```bash
  COMPOSE7_BENCH_MAX_RATIO=1.4 ./gradlew :benchmarks:benchmark
  ```

- Ratios above ~2x locally are worth investigating; ratios above 3x on CI fail
  the build.

## Reading the numbers

Across repeated local runs on one Windows machine, Compose7 sits at roughly
**1.5–2.2x** the bare baseline and **1.6–2.2x** the parity baseline, with a
run-to-run spread of ±20–30% even after taking the best of three rounds. Two
things follow:

1. The raw ratio overstates the cost, because the bare baseline draws fewer
   nodes and no interaction at all.
2. The residual gap survives feature parity, so it is real Compose-side work:
   the extra modifier chain in `compose7Clickable` (interaction source +
   pressed-state animation + `drawWithContent` overlay), per-composition
   allocations such as the `ButtonPalette` data class and `.copy(...)` text
   styles, and `ListItem`'s divider/semantics nodes.

Run-to-run spread is still about ±20–30%, so treat a single run as a smoke test.
Any real optimization should be validated with several local runs before and
after, not one CI number.

## Why not rewrite in Rust

The measured cost is **Compose runtime work**: composition, layout, semantics,
and the modifier chain. Those nodes exist whether or not the code that describes
them is Kotlin, and Compose's runtime is Kotlin/JVM — a Rust component cannot
replace it without abandoning Compose Multiplatform.

The parts that are genuinely heavy already run in native code: Skia (C++) for
drawing and text shaping, Skiko (C++ with Kotlin bindings) for the desktop/iOS
renderer. What is left in Kotlin is orchestration and small math, which is
exactly where an FFI boundary costs more than the work being moved: a JNI or
cinterop call per node per frame is tens of nanoseconds of marshalling before
any useful work happens, and it cannot be inlined.

A native rewrite would also mean shipping per-OS/per-ABI binaries (`dll`/`so`/
`dylib`, iOS static libs), losing the single-artifact Kotlin Multiplatform
promise and the future WASM target. The trade does not pay for this workload.

## Limitations

- This is a purpose-built harness, not JMH. It reports a single median without
  confidence intervals, and JIT warm-up inside a shared process is not a
  microbenchmark-grade methodology.
- Numbers are **relative**: they answer "is Compose7 much slower than a raw
  Compose tree?" rather than "how many frames per second does the widget hit on
  device".
- iOS/Android performance is not covered; mobile regressions need
  macrobenchmarks on hardware, which the PRD lists as a later quality gate.

## Adding a workload

Add a class in
`benchmarks/src/desktopMain/kotlin/io/github/compose7/benchmarks/Benchmarks.kt`
that renders Compose7 content and a baseline with the same geometry, then add
it to the `results` list in `main`. Keep both sides drawing the same number of
nodes so the ratio stays meaningful.

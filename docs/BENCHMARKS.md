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
| Recomposition | 10 `Button`s whose labels read a state value that changes every frame | 10 raw `Box` + `BasicText` rows with hard-coded Material colors/radii |
| Scroll | `VirtualList` (styled `LazyColumn`) with 500 `ListItem`s | bare `LazyColumn` with 500 two-line rows |

Both sides render offscreen through `ImageComposeScene` at density 1 with
Skiko's software rasterizer, so the numbers do not depend on a GPU or a display.
The scene clock advances 16 ms per frame, and each measured frame includes the
scroll step or state change.

## Methodology and tolerance

- 12 warm-up frames, then 40 measured frames; the reported number is the
  **median** frame time, which resists GC pauses better than a mean.
- Compose7 fails the run only when it exceeds the baseline by more than
  `COMPOSE7_BENCH_MAX_RATIO` (default **3.0**). The tolerance is deliberately
  generous: CI runners are shared and noisy, and this harness is meant to catch
  order-of-magnitude regressions, not to police a 5% drift.
- For a tighter measurement, run locally with more samples and a lower ratio:

  ```bash
  COMPOSE7_BENCH_MAX_RATIO=1.4 ./gradlew :benchmarks:benchmark
  ```

- Ratios above ~2x locally are worth investigating; ratios above 3x on CI fail
  the build.

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

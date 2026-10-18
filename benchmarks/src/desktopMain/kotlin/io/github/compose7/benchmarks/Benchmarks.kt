@file:OptIn(ExperimentalComposeUiApi::class)

package io.github.compose7.benchmarks

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.compose7.components.buttons.Button
import io.github.compose7.components.lists.ListItem
import io.github.compose7.components.lists.VirtualList
import io.github.compose7.core.theme.Compose7Style
import io.github.compose7.core.theme.Compose7Theme
import kotlinx.coroutines.runBlocking
import kotlin.system.measureNanoTime

/**
 * Desktop microbenchmark harness (PRD §12/§17).
 *
 * Renders each workload offscreen through [ImageComposeScene] at density 1 and
 * compares Compose7 against a hand-written baseline that draws the same tree
 * with raw Compose primitives and hard-coded values:
 *
 *  1. Recomposition: 10 buttons whose label reads a state value that changes
 *     every frame (Compose7 token lookups vs hard-coded styles).
 *  2. Scroll: 500 rows scrolled 120px per frame (`VirtualList` styling vs a
 *     bare `LazyColumn`).
 *
 * Methodology: 12 warm-up frames, 40 measured frames, median of the samples.
 * A workload fails when Compose7 exceeds the baseline by more than
 * `COMPOSE7_BENCH_MAX_RATIO` (default 3.0, deliberately generous because this
 * is a shared CI runner, not a dedicated benchmark host).
 */
private const val Width = 480
private const val Height = 800
private const val WarmupFrames = 12
private const val MeasuredFrames = 40

/**
 * Whole-workload repeats. Each variant is measured [Rounds] times and its best
 * median is kept: JIT warm-up and GC noise otherwise favour whichever variant
 * runs last, which is how an earlier version reported the feature-parity
 * baseline as faster than the bare one.
 */
private const val Rounds = 3
private const val FrameNanos = 16_000_000L

private class Measurements(val label: String, val compose7: Double, val baseline: Double) {
    val ratio: Double get() = compose7 / baseline
}

fun main() {
    // Offscreen rasterization only; no GPU/display dependency on CI.
    System.setProperty("skiko.renderApi", "SOFTWARE")

    val maxRatio = System.getenv("COMPOSE7_BENCH_MAX_RATIO")?.toDoubleOrNull() ?: 3.0
    println("Compose7 benchmarks - $MeasuredFrames frames, median, density 1")
    println("Tolerance: Compose7 <= ${maxRatio}x the hand-written baseline")
    println()

    val results = measureRecomposition() + measureScroll()

    println()
    println("| workload | Compose7 ms/frame | baseline ms/frame | ratio |")
    println("|---|---:|---:|---:|")
    val failures = results.filter { it.ratio > maxRatio }
    results.forEach { result ->
        println(
            "| %-30s | %17.3f | %17.3f | %5.2fx |".format(
                result.label,
                result.compose7,
                result.baseline,
                result.ratio,
            ),
        )
    }
    println()

    if (failures.isNotEmpty()) {
        println(
            "FAILED: " + failures.joinToString("; ") {
                "${it.label} is ${"%.2f".format(it.ratio)}x the baseline"
            },
        )
        kotlin.system.exitProcess(1)
    }
    println("All workloads within tolerance.")
}

private fun measureRecomposition(): List<Measurements> {
    val compose7Workload = RecompositionWorkload()
    val bareWorkload = RecompositionWorkload()
    val parityWorkload = RecompositionWorkload()

    val compose7 = bestOfRounds(
        content = { compose7Workload.Content(Variant.Compose7) },
        advance = { compose7Workload.advance() },
    )
    val bare = bestOfRounds(
        content = { bareWorkload.Content(Variant.Bare) },
        advance = { bareWorkload.advance() },
    )
    val parity = bestOfRounds(
        content = { parityWorkload.Content(Variant.Parity) },
        advance = { parityWorkload.advance() },
    )
    return listOf(
        Measurements("recomposition vs bare baseline", compose7, bare),
        // Same visual tree, but with the accessibility/interaction plumbing a
        // real button needs (clickable, semantics, min touch target, press
        // source). Isolates "Kotlin is slower" from "Compose7 does more".
        Measurements("recomposition vs parity baseline", compose7, parity),
    )
}

private fun measureScroll(): Measurements {
    val compose7Workload = ScrollWorkload(compose7 = true)
    val baselineWorkload = ScrollWorkload(compose7 = false)

    val compose7 = bestOfRounds(
        content = { compose7Workload.Content() },
        advance = { runBlocking { compose7Workload.state.scrollBy(120f) } },
    )
    val baseline = bestOfRounds(
        content = { baselineWorkload.Content() },
        advance = { runBlocking { baselineWorkload.state.scrollBy(120f) } },
    )
    return Measurements("scroll (500 rows, 120px/frame)", compose7, baseline)
}

private fun runBenchmark(
    content: @Composable () -> Unit,
    advance: () -> Unit,
): Double {
    val scene = ImageComposeScene(
        width = Width,
        height = Height,
        density = Density(1f),
        content = content,
    )
    scene.render(0)

    var tick = 0L
    fun step() {
        tick++
        advance()
        scene.render(tick * FrameNanos)
    }

    repeat(WarmupFrames) { step() }
    val samples = LongArray(MeasuredFrames) { measureNanoTime { step() } }
    samples.sort()
    val median = samples[MeasuredFrames / 2] / 1_000_000.0
    scene.close()
    return median
}

/** Best of [Rounds] independent runs - see [Rounds]. */
private fun bestOfRounds(
    content: @Composable () -> Unit,
    advance: () -> Unit,
): Double = (1..Rounds).minOf { runBenchmark(content, advance) }

private class RecompositionWorkload {
    var tick by mutableStateOf(0)
        private set

    fun advance() {
        tick++
    }

    @Composable
    fun Content(variant: Variant) {
        when (variant) {
            Variant.Compose7 -> Compose7Theme(style = Compose7Style.Material, darkTheme = false) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    repeat(10) { index ->
                        Button(
                            onClick = {},
                            text = "Compose7 $index $tick",
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }

            Variant.Bare -> Column(
                modifier = Modifier.fillMaxSize().background(Color(0xFFFEF7FF)).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                repeat(10) { index ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .background(Color(0xFF6750A4), RoundedCornerShape(22.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        BasicText(
                            text = "Baseline $index $tick",
                            style = TextStyle(color = Color.White, fontSize = 14.sp),
                        )
                    }
                }
            }

            Variant.Parity -> Column(
                modifier = Modifier.fillMaxSize().background(Color(0xFFFEF7FF)).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                repeat(10) { index ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .background(Color(0xFF6750A4))
                            // Interaction + semantics cost a real button carries.
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                role = Role.Button,
                                onClick = {},
                            )
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        BasicText(
                            text = "Parity $index $tick",
                            style = TextStyle(color = Color.White, fontSize = 14.sp),
                        )
                    }
                }
            }
        }
    }
}

private enum class Variant { Compose7, Bare, Parity }

private class ScrollWorkload(private val compose7: Boolean) {
    val state = LazyListState()

    @Composable
    fun Content() {
        if (compose7) {
            Compose7Theme(style = Compose7Style.Material, darkTheme = false) {
                VirtualList(state = state, modifier = Modifier.fillMaxSize()) {
                    items(count = 500) { index ->
                        ListItem(
                            title = "Row $index",
                            subtitle = "Compose7",
                            showDivider = true,
                            onClick = {},
                        )
                    }
                }
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().background(Color(0xFFFEF7FF)), state = state) {
                items(count = 500) { index ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.Center,
                    ) {
                        BasicText(
                            text = "Row $index",
                            style = TextStyle(color = Color(0xFF1C1B1F), fontSize = 16.sp),
                        )
                        BasicText(
                            text = "Baseline",
                            style = TextStyle(color = Color(0xFF49454F), fontSize = 13.sp),
                        )
                    }
                }
            }
        }
    }
}

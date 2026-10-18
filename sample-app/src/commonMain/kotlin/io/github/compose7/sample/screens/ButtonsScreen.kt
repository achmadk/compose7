package io.github.compose7.sample.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.compose7.components.buttons.Badge
import io.github.compose7.components.buttons.BadgeCount
import io.github.compose7.components.buttons.BadgeDot
import io.github.compose7.components.buttons.Button
import io.github.compose7.components.buttons.ButtonSize
import io.github.compose7.components.buttons.ButtonVariant
import io.github.compose7.components.buttons.Chip
import io.github.compose7.components.buttons.FAB
import io.github.compose7.components.buttons.FabSize
import io.github.compose7.components.indicators.Gauge
import io.github.compose7.components.indicators.IndeterminateProgressBar
import io.github.compose7.components.indicators.Preloader
import io.github.compose7.components.indicators.ProgressBar
import io.github.compose7.components.indicators.Skeleton
import io.github.compose7.components.indicators.SkeletonText
import io.github.compose7.core.theme.Compose7ThemeColors
import io.github.compose7.core.theme.Compose7ThemeShapes
import io.github.compose7.core.theme.Compose7ThemeTypography
import io.github.compose7.foundation.Card
import io.github.compose7.foundation.Check
import io.github.compose7.foundation.Divider
import io.github.compose7.foundation.ExpandableCard
import io.github.compose7.foundation.Plus

/** Buttons, cards, and indicators (PRD §7: "Buttons & indicators", "Cards"). */
@Composable
fun ButtonsScreen() {
    var favourite by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }

    DemoSection("Button variants") {
        DemoRow {
            Button(onClick = {}, text = "Filled")
            Button(onClick = {}, text = "Tonal", variant = ButtonVariant.Tonal)
            Button(onClick = {}, text = "Outline", variant = ButtonVariant.Outline)
            Button(onClick = {}, text = "Text", variant = ButtonVariant.Text)
        }
        DemoRow {
            Button(onClick = {}, text = "Small", size = ButtonSize.Small)
            Button(onClick = {}, text = "Medium")
            Button(onClick = {}, text = "Large", size = ButtonSize.Large)
        }
        DemoRow {
            Button(onClick = {}, text = "Disabled", enabled = false)
            Button(
                onClick = { favourite = !favourite },
                leadingIcon = { Check(color = Compose7ThemeColors.onPrimary, size = 14.dp) },
                content = { BasicText(text = if (favourite) "Saved" else "Save") },
            )
        }
    }

    DemoSection("Chips") {
        var selectedA by remember { mutableStateOf(true) }
        var selectedB by remember { mutableStateOf(false) }
        DemoRow {
            Chip(selected = selectedA, onSelectedChange = { selectedA = it }) {
                BasicText("News")
            }
            Chip(selected = selectedB, onSelectedChange = { selectedB = it }) {
                BasicText("Sports")
            }
            Chip(onClick = {}) {
                BasicText("Action")
            }
        }
    }

    DemoSection("Badges & FAB") {
        DemoRow {
            Badge { BasicText("New") }
            BadgeCount(count = 12)
            BadgeDot()
            Spacer(Modifier.width(8.dp))
            FAB(onClick = {}) { Plus(color = Compose7ThemeColors.onPrimary) }
            FAB(onClick = {}, size = FabSize.Small) { Plus(color = Compose7ThemeColors.onPrimary) }
            FAB(onClick = {}, label = "Compose") { Plus(color = Compose7ThemeColors.onPrimary) }
        }
    }

    DemoSection("Progress & loading") {
        ProgressBar(progress = 0.62f)
        Spacer(Modifier.height(10.dp))
        IndeterminateProgressBar()
        Spacer(Modifier.height(14.dp))
        DemoRow(spacing = 24.dp) {
            Preloader()
            Skeleton(modifier = Modifier.size(40.dp), shape = Compose7ThemeShapes.card)
            Box(modifier = Modifier.weight(1f)) { SkeletonText(lines = 3) }
        }
    }

    DemoSection("Gauge") {
        DemoRow(spacing = 24.dp) {
            Gauge(value = 0.72f, label = { DemoCaption("CPU") })
            Gauge(value = 0.35f, label = { DemoCaption("Disk") })
        }
    }

    DemoSection("Cards") {
        Card(onClick = {}, modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier.size(36.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Badge { BasicText("A") }
                }
                Spacer(Modifier.width(12.dp))
                BasicText(
                    text = "Tappable card",
                    style = Compose7ThemeTypography.bodyLarge.copy(color = Compose7ThemeColors.onSurface),
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        ExpandableCard(
            expanded = expanded,
            onExpandedChange = { expanded = it },
            title = "Expandable card",
            subtitle = "Token-driven chevron rotation",
        ) {
            BasicText(
                text = "Body hidden until the header is tapped.",
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                style = Compose7ThemeTypography.bodyMedium.copy(color = Compose7ThemeColors.onSurfaceVariant),
            )
            Divider()
        }
    }

    // Keeps the `Color` import honest for the container override demo above.
    DemoCaption(
        text = "All chrome above comes from Compose7 tokens - no Material3, no icon assets.",
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
    )
}

package io.github.compose7.components.lists

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.compose7.core.theme.Compose7Style
import io.github.compose7.core.theme.Compose7ThemeColors
import io.github.compose7.core.theme.Compose7ThemeShapes
import io.github.compose7.core.theme.Compose7ThemeTypography
import io.github.compose7.core.theme.currentCompose7Style
import io.github.compose7.foundation.Compose7Surface
import io.github.compose7.foundation.Divider

/**
 * Section header + rows for grouped lists (PRD §8.3, Appendix A "List View").
 *
 * The [title] renders exactly as given — casing/transformation is the
 * caller's concern (no locale-unsafe uppercase inside the library).
 *
 * Style divergence (PRD §9):
 *  - **Cupertino** — small gray inset label above a rounded [Compose7Surface]
 *    card holding the section's rows.
 *  - **Material** — primary-tinted header; rows sit directly on the list surface.
 *  - **DesktopUI7** — onSurfaceVariant label with a hairline rule below.
 *
 * ```kotlin
 * List {  // grouped by default on Cupertino
 *     ListSection("GENERAL") {
 *         ListItem("About", showChevron = true, onClick = { … })
 *         ListItem("Legal", showChevron = true, onClick = { … })
 *     }
 * }
 * ```
 */
@Composable
fun ListSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val platformStyle = currentCompose7Style
    val style = currentListStyle()
    val colors = Compose7ThemeColors
    val shapes = Compose7ThemeShapes
    val type = Compose7ThemeTypography
    val inset = currentListContentInset()

    val (labelStyle, labelColor) = when (platformStyle) {
        Compose7Style.Cupertino -> type.labelMedium to colors.onSurfaceVariant
        Compose7Style.DesktopUI7 -> type.labelMedium to colors.onSurfaceVariant
        else -> type.labelLarge to colors.primary
    }

    Column(modifier = modifier) {
        BasicText(
            text = title,
            style = labelStyle.copy(color = labelColor),
            modifier = Modifier.padding(start = inset, end = inset, top = inset / 2, bottom = 4.dp),
        )
        if (platformStyle == Compose7Style.DesktopUI7) {
            Divider()
        }
        when (style) {
            ListStyle.Grouped -> Compose7Surface(
                shape = shapes.card,
                color = colors.surface,
            ) {
                Column(modifier = Modifier.fillMaxWidth(), content = content)
            }
            ListStyle.Plain -> Column(
                modifier = Modifier.fillMaxWidth().background(colors.surface),
                content = content,
            )
        }
    }
}

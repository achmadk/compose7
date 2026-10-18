package io.github.compose7.components.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.compose7.core.theme.Compose7Style
import io.github.compose7.core.theme.Compose7ThemeColors
import io.github.compose7.core.theme.currentCompose7Style
import io.github.compose7.foundation.Compose7Surface
import io.github.compose7.foundation.Divider

/**
 * Secondary bar docked under a [Navbar] (PRD Appendix A "Subnavbar"): a flat
 * surface strip for filters, sub-tabs, or a [Searchbar]. Applies no
 * system-bar inset by design — it sits below the navbar.
 *
 * Material and DesktopUI7 draw a bottom hairline; Cupertino stays flat
 * (a real iOS sub-navbar blurs — Compose7 approximates with a flat surface,
 * no blur effects).
 *
 * ```kotlin
 * Subnavbar { Searchbar(query, ::onQueryChange) }
 * ```
 */
@Composable
fun Subnavbar(
    modifier: Modifier = Modifier,
    containerColor: Color? = null,
    contentColor: Color? = null,
    showHairline: Boolean? = null,
    content: @Composable RowScope.() -> Unit,
) {
    val style = currentCompose7Style
    val colors = Compose7ThemeColors
    val hairline = showHairline ?: (style != Compose7Style.Cupertino)

    Compose7Surface(
        modifier = modifier.fillMaxWidth(),
        color = containerColor ?: colors.surface,
        contentColor = contentColor ?: colors.onSurface,
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(subnavbarHeight(style))
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                content = content,
            )
            if (hairline) {
                Divider(color = colors.outlineVariant, insetStart = 0.dp)
            }
        }
    }
}

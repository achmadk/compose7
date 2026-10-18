package io.github.compose7.components.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
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

/** Default hairline for [Toolbar]: only DesktopUI7 draws one (Material relies on elevation, Cupertino on blur). */
@Composable
internal fun toolbarHairlineDefault(): Boolean = currentCompose7Style == Compose7Style.DesktopUI7

/**
 * Generic top bar without title semantics (PRD §8.2 / Appendix A "Toolbar"):
 * leading/center/trailing slots with the same system-bar inset handling as
 * [Navbar]. Use it for searchbars, filters, and sub-navigation stacking.
 *
 * ```kotlin
 * Toolbar(leading = { BackArrow() }, center = { BasicText("Title") }, trailing = { More() })
 * ```
 */
@Composable
fun Toolbar(
    modifier: Modifier = Modifier,
    containerColor: Color = Compose7ThemeColors.surface,
    contentColor: Color = Compose7ThemeColors.onSurface,
    applyTopInset: Boolean = true,
    showHairline: Boolean = toolbarHairlineDefault(),
    leading: (@Composable RowScope.() -> Unit)? = null,
    center: (@Composable RowScope.() -> Unit)? = null,
    trailing: (@Composable RowScope.() -> Unit)? = null,
) {
    val style = currentCompose7Style
    val colors = Compose7ThemeColors

    Compose7Surface(
        modifier = modifier.fillMaxWidth(),
        color = containerColor,
        contentColor = contentColor,
    ) {
        Column(
            modifier = if (applyTopInset) {
                Modifier.windowInsetsPadding(WindowInsets.statusBars.only(WindowInsetsSides.Top))
            } else {
                Modifier
            },
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(topBarHeight(style))
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                leading?.invoke(this)
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    // Explicit receiver: inside Box the implicit `this` is BoxScope,
                    // not the enclosing RowScope the slot signature declares.
                    center?.invoke(this@Row)
                }
                trailing?.invoke(this)
            }
            if (showHairline) {
                Divider(color = colors.outlineVariant, insetStart = 0.dp)
            }
        }
    }
}

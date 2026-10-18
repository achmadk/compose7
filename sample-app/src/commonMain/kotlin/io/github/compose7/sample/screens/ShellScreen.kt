package io.github.compose7.sample.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.compose7.components.forms.Switch
import io.github.compose7.components.navigation.Navbar
import io.github.compose7.components.navigation.SegmentedControl
import io.github.compose7.components.navigation.Tabbar
import io.github.compose7.components.navigation.TabData
import io.github.compose7.core.theme.Compose7Style
import io.github.compose7.core.theme.Compose7Theme
import io.github.compose7.foundation.Page

private val SectionTitles = listOf("Buttons", "Forms", "Lists", "Media", "Login")

/**
 * The Kitchen Sink shell: one chrome (Navbar + style switcher + Tabbar) around
 * four galleries. Every screen is plain library code with no sample-only
 * component wrappers, so the gallery doubles as an integration test (PRD §16).
 */
@Composable
fun ShellScreen() {
    var style by remember { mutableStateOf(Compose7Style.Auto) }
    var darkTheme by remember { mutableStateOf(false) }
    var nativeBridge by remember { mutableStateOf(false) }
    var tab by remember { mutableStateOf(0) }

    Compose7Theme(style = style, darkTheme = darkTheme, nativeBridge = nativeBridge) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                Navbar(title = "Compose7", subtitle = SectionTitles[tab])
                StyleSwitcher(
                    style = style,
                    darkTheme = darkTheme,
                    nativeBridge = nativeBridge,
                    onStyleChange = { style = it },
                    onDarkThemeChange = { darkTheme = it },
                    onNativeBridgeChange = { nativeBridge = it },
                )
                if (tab == 4) {
                    LoginScreen(modifier = Modifier.weight(1f))
                } else {
                    Page(
                        modifier = Modifier.weight(1f),
                        // Navbar draws the status-bar background itself.
                        topSafeArea = false,
                    ) {
                        when (tab) {
                            0 -> ButtonsScreen()
                            1 -> FormsScreen()
                            2 -> ListsScreen()
                            else -> MediaScreen()
                        }
                    }
                }
                Tabbar(
                    tabs = SectionTitles.map { TabData(label = it) },
                    selectedIndex = tab,
                    onSelect = { tab = it },
                )
            }
        }
    }
}

/**
 * Live style / brightness / bridge switches - the PRD §9 "one API, three
 * renderings" claim is only checkable if a consumer can flip it at runtime.
 */
@Composable
private fun StyleSwitcher(
    style: Compose7Style,
    darkTheme: Boolean,
    nativeBridge: Boolean,
    onStyleChange: (Compose7Style) -> Unit,
    onDarkThemeChange: (Boolean) -> Unit,
    onNativeBridgeChange: (Boolean) -> Unit,
) {
    val styles = Compose7Style.entries
    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
        SegmentedControl(
            segments = styles.map { it.name },
            selectedIndex = styles.indexOf(style),
            onSelect = { onStyleChange(styles[it]) },
        )
        Row(
            modifier = Modifier.padding(top = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(checked = darkTheme, onCheckedChange = onDarkThemeChange)
                Spacer(Modifier.width(6.dp))
                DemoCaption("Dark")
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                // iOS-only bridge (PRD §8.7); inert on Android/Desktop by design.
                Switch(checked = nativeBridge, onCheckedChange = onNativeBridgeChange)
                Spacer(Modifier.width(6.dp))
                DemoCaption("Native bridge")
            }
        }
    }
}

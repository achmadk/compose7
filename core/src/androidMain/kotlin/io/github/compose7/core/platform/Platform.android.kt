package io.github.compose7.core.platform

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import io.github.compose7.core.theme.Compose7ColorScheme
import io.github.compose7.core.theme.Compose7Style
import io.github.compose7.core.theme.darkMaterialColorScheme
import io.github.compose7.core.theme.lightMaterialColorScheme

actual fun platformDefaultStyle(): Compose7Style = Compose7Style.Material

actual fun platformName(): String = "Android"

/**
 * Android 12+ dynamic color derived from the current wallpaper colors
 * (PRD §8.1). Deliberately avoids depending on Material3: the wallpaper
 * colors are converted into Compose7's own token shape. Returns null below
 * API 31 or when the system declines to share wallpaper colors, in which case
 * [io.github.compose7.core.theme.Compose7Theme] falls back to static tokens.
 */
@Composable
actual fun platformDynamicColorScheme(darkTheme: Boolean): Compose7ColorScheme? {
    val context = LocalContext.current
    return androidx.compose.runtime.remember(context, darkTheme) {
        wallpaperColorScheme(context, darkTheme)
    }
}

private fun wallpaperColorScheme(context: Context, darkTheme: Boolean): Compose7ColorScheme? {
    if (Build.VERSION.SDK_INT < 31) return null
    return try {
        val colors = android.app.WallpaperManager.getInstance(context)
            .getWallpaperColors(android.app.WallpaperManager.FLAG_SYSTEM)
            ?: return null
        val base = if (darkTheme) darkMaterialColorScheme() else lightMaterialColorScheme()
        val secondary = colors.secondaryColor ?: colors.tertiaryColor ?: colors.primaryColor
        base.withDynamicColors(
            newPrimary = Color(colors.primaryColor.toArgb()),
            newSecondary = Color(secondary.toArgb()),
        )
    } catch (_: SecurityException) {
        null
    } catch (_: RuntimeException) {
        null
    }
}

@Suppress("DEPRECATION")
private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@Composable
actual fun Compose7SystemBarAppearance(darkContent: Boolean) {
    val view = LocalView.current
    SideEffect {
        val window = view.context.findActivity()?.window ?: return@SideEffect
        if (Build.VERSION.SDK_INT >= 30) {
            val controller = window.insetsController ?: return@SideEffect
            val statusMask = android.view.WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
            val navMask = android.view.WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
            controller.setSystemBarsAppearance(
                if (darkContent) statusMask else 0,
                statusMask,
            )
            controller.setSystemBarsAppearance(
                if (darkContent) navMask else 0,
                navMask,
            )
        } else {
            // Pre-30: the legacy decor-view flags; no androidx.core dependency.
            @Suppress("DEPRECATION")
            var flags = window.decorView.systemUiVisibility
            @Suppress("DEPRECATION")
            flags = if (darkContent) {
                flags or android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            } else {
                flags and android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
            }
            if (Build.VERSION.SDK_INT >= 26) {
                @Suppress("DEPRECATION")
                flags = if (darkContent) {
                    flags or android.view.View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
                } else {
                    flags and android.view.View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR.inv()
                }
            }
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = flags
        }
    }
}

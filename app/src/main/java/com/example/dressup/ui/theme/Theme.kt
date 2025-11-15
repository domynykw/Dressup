package com.example.dressup.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = AquaPrimary,
    onPrimary = Color.White,
    primaryContainer = AquaSecondary,
    onPrimaryContainer = NightBlue,
    secondary = AccentMint,
    onSecondary = NightBlue,
    background = SnowWhite,
    onBackground = Slate,
    surface = SnowWhite,
    onSurface = Slate,
    surfaceVariant = MistBlue,
    onSurfaceVariant = SoftSlate,
    outline = AquaPrimary.copy(alpha = 0.35f)
)

private val DarkColorScheme = darkColorScheme(
    primary = AquaSecondary,
    onPrimary = NightBlue,
    primaryContainer = AzureDeep,
    onPrimaryContainer = SnowWhite,
    secondary = AccentMint,
    onSecondary = NightBlue,
    background = NightBlue,
    onBackground = Color.White,
    surface = NightBlue,
    onSurface = Color.White,
    surfaceVariant = AzureDeep,
    onSurfaceVariant = SnowWhite.copy(alpha = 0.85f),
    outline = AquaSecondary.copy(alpha = 0.5f)
)

@Composable
fun DressUpTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val activity = context as? Activity
            if (activity != null) {
                if (darkTheme) dynamicDarkColorScheme(activity) else dynamicLightColorScheme(activity)
            } else {
                if (darkTheme) DarkColorScheme else LightColorScheme
            }
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = Color.Transparent.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

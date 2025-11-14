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
    primary = DeepLilac,
    onPrimary = SoftCream,
    primaryContainer = Lilac,
    onPrimaryContainer = MidnightBlue,
    secondary = BabyBlue,
    onSecondary = MidnightBlue,
    background = SoftCream,
    onBackground = Color.Black,
    surface = ColorPalette.surface,
    onSurface = Color.Black,
    surfaceVariant = ColorPalette.surface,
    onSurfaceVariant = Color.Black
)

private val DarkColorScheme = darkColorScheme(
    primary = Lilac,
    onPrimary = MidnightBlue,
    primaryContainer = DeepLilac,
    onPrimaryContainer = SoftCream,
    secondary = BabyBlue,
    onSecondary = MidnightBlue,
    background = MidnightBlue,
    onBackground = SoftCream,
    surface = ColorPalette.darkSurface,
    onSurface = SoftCream
)

private object ColorPalette {
    val surface = Color.White
    val darkSurface = Color(0xFF111827)
}

@Composable
fun DressUpTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val activity = context as Activity
            if (darkTheme) dynamicDarkColorScheme(activity) else dynamicLightColorScheme(activity)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

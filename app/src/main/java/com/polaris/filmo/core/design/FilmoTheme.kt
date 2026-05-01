package com.polaris.filmo.core.design

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val FilmoColorScheme = darkColorScheme(
    primary = FilmoColors.Primary,
    onPrimary = FilmoColors.TextOnLight,
    secondary = FilmoColors.PrimaryMuted,
    onSecondary = FilmoColors.TextPrimary,
    background = FilmoColors.Background,
    onBackground = FilmoColors.TextPrimary,
    surface = FilmoColors.Surface,
    onSurface = FilmoColors.TextPrimary,
    surfaceVariant = FilmoColors.SurfaceOverlay,
    onSurfaceVariant = FilmoColors.TextSecondary,
    error = FilmoColors.Error,
    onError = FilmoColors.TextPrimary,
    outline = FilmoColors.Divider
)

@Composable
fun FilmoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = FilmoColors.Background.toArgb()
            window.navigationBarColor = FilmoColors.Background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = FilmoColorScheme,
        typography = FilmoTypography,
        shapes = FilmoShapes,
        content = content
    )
}

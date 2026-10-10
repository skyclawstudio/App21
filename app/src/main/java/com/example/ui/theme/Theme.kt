package com.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val SereneDarkColorScheme = darkColorScheme(
    primary = GlowCyan,
    onPrimary = MidnightBlack,
    primaryContainer = TwilightSurfaceVariant,
    onPrimaryContainer = StarlightWhite,
    secondary = MysticLavender,
    onSecondary = MidnightBlack,
    secondaryContainer = TwilightSurface,
    onSecondaryContainer = StarlightWhite,
    tertiary = WarmAmber,
    onTertiary = MidnightBlack,
    background = MidnightBlack,
    onBackground = StarlightWhite,
    surface = TwilightSurface,
    onSurface = StarlightWhite,
    surfaceVariant = TwilightSurfaceVariant,
    onSurfaceVariant = MoonMuted
)

@Composable
fun SereneTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = SereneDarkColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = MidnightBlack.toArgb()
                window.navigationBarColor = MidnightBlack.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

// Alias for backwards compatibility with any template references
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    SereneTheme(darkTheme = true, content = content)
}

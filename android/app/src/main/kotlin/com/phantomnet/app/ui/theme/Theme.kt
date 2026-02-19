package com.phantomnet.app.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val PhantomColorScheme = darkColorScheme(
    primary = PhantomGreen,
    secondary = SecureBlue,
    tertiary = TorOnion,
    background = Background,
    surface = Surface,
    surfaceVariant = SurfaceVariant,
    error = ErrorRed,
    onPrimary = Background,
    onSecondary = Background,
    onTertiary = TextPrimary,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextMuted,
    outline = Divider,
    outlineVariant = Divider
)

@Composable
fun PhantomNetTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Background.toArgb()
            window.navigationBarColor = Background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = PhantomColorScheme,
        typography = Typography,
        content = content
    )
}

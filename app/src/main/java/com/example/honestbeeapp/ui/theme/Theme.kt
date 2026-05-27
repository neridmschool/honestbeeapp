package com.example.honestbeeapp.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = BeePrimaryYellow,
    onPrimary = BeeDarkText,
    primaryContainer = BeePressedYellow,
    onPrimaryContainer = BeeDarkText,
    secondary = BeeHoneyYellow,
    onSecondary = BeeDarkText,
    tertiary = BeeSuccess,
    onTertiary = BeeCard,
    background = BeeDarkBackground,
    onBackground = BeeCard,
    surface = BeeDarkSurface,
    onSurface = BeeCard,
    surfaceVariant = BeeCream,
    onSurfaceVariant = BeeCream,
    outline = BeeBorder,
    error = BeeError,
    onError = BeeCard
)

private val LightColorScheme = lightColorScheme(
    primary = BeePrimaryYellow,
    onPrimary = BeeDarkText,
    primaryContainer = BeeNavigationSelected,
    onPrimaryContainer = BeeDarkText,
    secondary = BeeHoneyYellow,
    onSecondary = BeeDarkText,
    tertiary = BeeSuccess,
    onTertiary = BeeCard,
    background = BeeBackground,
    onBackground = BeeDarkText,
    surface = BeeCard,
    onSurface = BeeBodyText,
    surfaceVariant = BeeCream,
    onSurfaceVariant = BeeMuted,
    outline = BeeBorder,
    outlineVariant = BeeCream,
    error = BeeError,
    onError = BeeCard
)

private val HonestbeeShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(10.dp),
    large = RoundedCornerShape(12.dp),
    extraLarge = RoundedCornerShape(16.dp)
)

@Composable
fun HonestbeeAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = !darkTheme
            controller.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        shapes = HonestbeeShapes,
        typography = Typography,
        content = content
    )
}

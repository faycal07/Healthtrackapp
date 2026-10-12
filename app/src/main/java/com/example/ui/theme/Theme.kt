package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = EmeraldLight,
    onPrimary = Color(0xFF003824),
    primaryContainer = Color(0xFF005237),
    onPrimaryContainer = MintContainer,
    secondary = CoralSecondary,
    onSecondary = Color(0xFF450B00),
    secondaryContainer = Color(0xFF6B1B0A),
    onSecondaryContainer = CoralContainer,
    tertiary = SkyTertiary,
    onTertiary = Color(0xFF003549),
    tertiaryContainer = Color(0xFF004D69),
    onTertiaryContainer = SkyContainer,
    background = DarkBackground,
    onBackground = Color(0xFFE2E9E4),
    surface = DarkSurface,
    onSurface = Color(0xFFE2E9E4),
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFBFCAC1),
    outline = DarkOutline,
)

private val LightColorScheme = lightColorScheme(
    primary = EmeraldDark,
    onPrimary = Color.White,
    primaryContainer = MintContainer,
    onPrimaryContainer = MintOnContainer,
    secondary = CoralSecondary,
    onSecondary = Color.White,
    secondaryContainer = CoralContainer,
    onSecondaryContainer = CoralOnContainer,
    tertiary = SkyTertiary,
    onTertiary = Color.White,
    tertiaryContainer = SkyContainer,
    onTertiaryContainer = SkyOnContainer,
    background = LightBackground,
    onBackground = Color(0xFF18201C),
    surface = LightSurface,
    onSurface = Color(0xFF18201C),
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = Color(0xFF49554E),
    outline = LightOutline,
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep branded emerald palette by default
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

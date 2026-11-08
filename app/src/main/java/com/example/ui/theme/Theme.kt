package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val IndustrialDarkColorScheme = darkColorScheme(
    primary = SafetyRed,
    onPrimary = Color.White,
    primaryContainer = SafetyRedDark,
    onPrimaryContainer = Color.White,
    secondary = SafetyAmber,
    onSecondary = Charcoal900,
    secondaryContainer = Charcoal600,
    onSecondaryContainer = SafetyAmber,
    tertiary = SafetyOrange,
    onTertiary = Charcoal900,
    background = Charcoal900,
    onBackground = TextPrimary,
    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceCard,
    onSurfaceVariant = TextSecondary,
    outline = BorderDark,
    error = SafetyRed,
    onError = Color.White
)

@Composable
fun WFHTheme(
    darkTheme: Boolean = true, // Industrial mobile app defaults to high-contrast dark theme
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = IndustrialDarkColorScheme,
        typography = Typography,
        content = content
    )
}

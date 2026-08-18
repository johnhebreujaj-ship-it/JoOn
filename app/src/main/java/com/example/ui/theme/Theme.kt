package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ElegantDarkColorScheme = darkColorScheme(
    primary = ElegantAccentBlue,
    onPrimary = Color(0xFF041E49),
    primaryContainer = Color(0xFF1B3B6F),
    onPrimaryContainer = Color(0xFFD2E3FC),
    secondary = ElegantAccentTeal,
    onSecondary = Color(0xFF003731),
    secondaryContainer = Color(0xFF1E4E47),
    onSecondaryContainer = Color(0xFFB2DFDB),
    tertiary = ElegantAccentGold,
    onTertiary = Color(0xFF3F2E00),
    tertiaryContainer = Color(0xFF5C4300),
    onTertiaryContainer = Color(0xFFFFE082),
    background = ElegantDarkBg,
    onBackground = TextPrimary,
    surface = ElegantSurface,
    onSurface = TextPrimary,
    surfaceVariant = ElegantSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = ElegantBorder,
    outlineVariant = ElegantBorderSubtle,
    error = ElegantAccentCoral,
    onError = Color(0xFF601410)
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ElegantDarkColorScheme,
        typography = Typography,
        content = content
    )
}



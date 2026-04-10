package com.katafract.parkarmor.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ParkArmor branding colors
private val PrimaryBlue = Color(0xFF3B82F6)
private val PrimaryDarkBlue = Color(0xFF1E3A5F)
private val SecondaryBlue = Color(0xFF60A5FA)
private val TertiaryBlue = Color(0xFF93C5FD)
private val BackgroundLight = Color(0xFFFAFAFA)
private val SurfaceLight = Color(0xFFFFFFFF)
private val OnSurfaceLight = Color(0xFF1F2937)
private val BackgroundDark = Color(0xFF0F172A)
private val SurfaceDark = Color(0xFF1E293B)
private val OnSurfaceDark = Color(0xFFF1F5F9)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = Color.White,
    primaryContainer = TertiaryBlue,
    onPrimaryContainer = Color(0xFF001D3D),
    secondary = SecondaryBlue,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDEEDFF),
    onSecondaryContainer = Color(0xFF001D3D),
    tertiary = Color(0xFF0EA5E9),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFCFF0FF),
    onTertiaryContainer = Color(0xFF001E2E),
    error = Color(0xFFB3261E),
    onError = Color.White,
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B),
    background = BackgroundLight,
    onBackground = OnSurfaceLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = Color(0xFFE8EEF7),
    onSurfaceVariant = Color(0xFF49454F),
    outline = Color(0xFF79747E),
    outlineVariant = Color(0xFFC9C7CF),
    scrim = Color(0xFF000000)
)

private val DarkColorScheme = darkColorScheme(
    primary = SecondaryBlue,
    onPrimary = PrimaryDarkBlue,
    primaryContainer = Color(0xFF00254C),
    onPrimaryContainer = TertiaryBlue,
    secondary = TertiaryBlue,
    onSecondary = PrimaryDarkBlue,
    secondaryContainer = Color(0xFF003A73),
    onSecondaryContainer = Color(0xFFDEEDFF),
    tertiary = Color(0xFF80DEEA),
    onTertiary = Color(0xFF003D4D),
    tertiaryContainer = Color(0xFF005270),
    onTertiaryContainer = Color(0xFFCFF0FF),
    error = Color(0xFFF2B8B5),
    onError = PrimaryDarkBlue,
    errorContainer = Color(0xFF8C1D18),
    onErrorContainer = Color(0xFFF9DEDC),
    background = BackgroundDark,
    onBackground = OnSurfaceDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = Color(0xFF49454F),
    onSurfaceVariant = Color(0xFFC9C7CF),
    outline = Color(0xFF938F99),
    outlineVariant = Color(0xFF49454F),
    scrim = Color(0xFF000000)
)

@Composable
fun ParkArmorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = androidx.compose.material3.Typography(),
        content = content
    )
}

package com.example.trackinggym.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val GymColorScheme = darkColorScheme(
    primary = GymPrimaryBlue,
    onPrimary = TextLight,
    secondary = GymSecondaryBlue,
    tertiary = GymAccentYellow,
    background = GymBackground,
    surface = GymSurface,
    surfaceVariant = GymSurfaceVariant,
    onBackground = TextLight,
    onSurface = TextLight,
    onSurfaceVariant = TextGrey,
    error = GymRed
)

@Composable
fun TrackingGymTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color is available on Android 12+
  dynamicColor: Boolean = true,
  content: @Composable () -> Unit,
) {
  // Always use our custom Gym light theme for branding
  val colorScheme = GymColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

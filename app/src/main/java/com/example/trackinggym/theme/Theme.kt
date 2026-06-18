package com.example.trackinggym.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val GymColorScheme = darkColorScheme(
    primary = GymNeonOrange,
    onPrimary = TextWhite,
    secondary = GymNeonGreen,
    tertiary = GymRed,
    background = GymDarkGrey,
    surface = GymMediumGrey,
    surfaceVariant = GymLightGrey,
    onBackground = TextWhite,
    onSurface = TextWhite,
    onSurfaceVariant = TextLightGrey
)

@Composable
fun TrackingGymTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color is available on Android 12+
  dynamicColor: Boolean = true,
  content: @Composable () -> Unit,
) {
  // Always use our custom Gym dark theme for branding
  val colorScheme = GymColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

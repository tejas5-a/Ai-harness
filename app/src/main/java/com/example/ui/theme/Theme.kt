package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Stitch Warm Light Theme
private val LightColorScheme = lightColorScheme(
  primary = StitchPrimaryAccent,
  onPrimary = Color.White,
  primaryContainer = StitchPrimaryAccent,
  onPrimaryContainer = Color.White,
  secondary = StitchSecondaryText,
  onSecondary = Color.White,
  secondaryContainer = StitchSecondaryBackground,
  onSecondaryContainer = StitchPrimaryText,
  background = StitchMainBackground,
  onBackground = StitchPrimaryText,
  surface = StitchCardBackground,
  onSurface = StitchPrimaryText,
  surfaceVariant = StitchSecondaryBackground,
  onSurfaceVariant = StitchSecondaryText,
  outline = StitchBorder,
  outlineVariant = StitchBorder.copy(alpha = 0.6f),
  error = StitchError,
  onError = Color.White
)

// Stitch Warm Dark Theme
private val DarkColorScheme = darkColorScheme(
  primary = StitchDarkAccent,
  onPrimary = Color.White,
  primaryContainer = StitchDarkAccent,
  onPrimaryContainer = Color.White,
  secondary = StitchDarkSecondaryText,
  onSecondary = Color.White,
  secondaryContainer = StitchDarkElevated,
  onSecondaryContainer = StitchDarkPrimaryText,
  background = StitchDarkBackground,
  onBackground = StitchDarkPrimaryText,
  surface = StitchDarkCards,
  onSurface = StitchDarkPrimaryText,
  surfaceVariant = StitchDarkElevated,
  onSurfaceVariant = StitchDarkSecondaryText,
  outline = StitchDarkBorders,
  outlineVariant = StitchDarkBorders.copy(alpha = 0.6f),
  error = StitchError,
  onError = Color.White
)

@Composable
fun AltronTheme(
  darkTheme: Boolean = false,
  content: @Composable () -> Unit
) {
  MaterialTheme(
    colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
    typography = Typography,
    content = content
  )
}

package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Default theme is now Premium Orange-and-White (Light) and Graphite/Orange (Dark)
private val LightColorScheme = lightColorScheme(
  primary = PrimaryOrange,
  onPrimary = Color.White,
  primaryContainer = AccentOrange,
  onPrimaryContainer = Color.Black,
  secondary = DeepOrange,
  onSecondary = Color.White,
  background = WhiteBase,
  onBackground = WhiteText,
  surface = WhiteSurface,
  onSurface = WhiteText,
  surfaceVariant = WhiteSecondary,
  onSurfaceVariant = SecondaryText,
  outline = BorderColor,
  error = ErrorRed
)

private val DarkColorScheme = darkColorScheme(
  primary = PrimaryOrange,
  onPrimary = Color.Black,
  primaryContainer = DeepOrange,
  onPrimaryContainer = Color.White,
  background = Graphite900,
  onBackground = Color.White,
  surface = Graphite950,
  onSurface = Color.White,
  surfaceVariant = Graphite850,
  onSurfaceVariant = Graphite400,
  outline = Graphite700,
  error = ErrorRed
)

@Composable
fun AltronTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}

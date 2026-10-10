package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
  primary = AltronOrange,
  onPrimary = Color.Black,
  primaryContainer = AltronOrangeMuted,
  onPrimaryContainer = GraphiteWhite,
  secondary = AltronAmber,
  onSecondary = Color.Black,
  secondaryContainer = Graphite800,
  onSecondaryContainer = Graphite100,
  tertiary = TerminalCyan,
  onTertiary = Color.Black,
  background = Graphite950,
  onBackground = GraphiteWhite,
  surface = Graphite900,
  onSurface = Graphite100,
  surfaceVariant = Graphite850,
  onSurfaceVariant = Graphite200,
  outline = Graphite700,
  outlineVariant = Graphite800,
  error = TerminalRed,
  onError = Color.White
)

private val LightColorScheme = lightColorScheme(
  primary = AltronOrange,
  onPrimary = Color.White,
  primaryContainer = Color(0xFFFFEAD6),
  onPrimaryContainer = Color(0xFF6B2600),
  secondary = AltronAmber,
  onSecondary = Color.Black,
  secondaryContainer = LightSurfaceVariant,
  onSecondaryContainer = LightTextPrimary,
  tertiary = TerminalCyan,
  onTertiary = Color.White,
  background = LightBg,
  onBackground = LightTextPrimary,
  surface = LightSurface,
  onSurface = LightTextPrimary,
  surfaceVariant = LightSurfaceVariant,
  onSurfaceVariant = LightTextSecondary,
  outline = LightBorder,
  outlineVariant = Color(0xFFE2E8F0),
  error = TerminalRed,
  onError = Color.White
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

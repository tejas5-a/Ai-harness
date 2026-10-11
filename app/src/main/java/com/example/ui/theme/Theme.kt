package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Light Theme: White background, black text, orange buttons with black text
private val LightColorScheme = lightColorScheme(
  primary = BrandPrimaryOrange,
  onPrimary = BrandTextBlack,
  primaryContainer = BrandPrimaryOrange,
  onPrimaryContainer = BrandTextBlack,
  secondary = BrandPrimaryOrange,
  onSecondary = BrandTextBlack,
  secondaryContainer = LightSurfaceElevated,
  onSecondaryContainer = BrandTextBlack,
  background = PureWhiteBackground,
  onBackground = BrandTextBlack,
  surface = LightSurface,
  onSurface = BrandTextBlack,
  surfaceVariant = LightSurfaceElevated,
  onSurfaceVariant = BrandTextSecondary,
  outline = LightBorder,
  outlineVariant = LightBorder.copy(alpha = 0.6f),
  error = BrandError,
  onError = Color.White
)

@Composable
fun AltronTheme(
  darkTheme: Boolean = false,
  content: @Composable () -> Unit
) {
  MaterialTheme(
    colorScheme = LightColorScheme,
    typography = Typography,
    content = content
  )
}

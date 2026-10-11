package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Premium Colors
val PrimaryOrange = Color(0xFFFF6B00)
val DeepOrange = Color(0xFFE65100)
val AccentOrange = Color(0xFFFF8A3D)
val AltronAmber = Color(0xFFFFB300)

// Graphite (Dark Palette)
val Graphite950 = Color(0xFF0A0B0E)
val Graphite900 = Color(0xFF101216)
val Graphite850 = Color(0xFF191C22)
val Graphite800 = Color(0xFF262626)
val Graphite700 = Color(0xFF404040)
val Graphite400 = Color(0xFF737373)

// Light (White Palette)
val WhiteBase = Color(0xFFFFFFFF)
val WhiteSurface = Color(0xFFF9F9F9)
val WhiteSecondary = Color(0xFFF0F0F0)
val WhiteText = Color(0xFF171717)

// Aliases for Type.kt compatibility
val GraphiteWhite = WhiteBase
val Graphite100 = Color(0xFFE5E5E5)
val Graphite200 = Color(0xFFCCCCCC)

// Common
val SuccessGreen = Color(0xFF16A34A)
val ErrorRed = Color(0xFFDC2626)
val TerminalCyan = Color(0xFF0891B2)
val TerminalYellow = Color(0xFFD97706)

// Aliases for compatibility
val AltronOrange = PrimaryOrange
val AltronOrangeBright = AccentOrange
val AltronOrangeGlow = PrimaryOrange.copy(alpha = 0.3f)
val AltronOrangeRim = PrimaryOrange.copy(alpha = 0.5f)
val TerminalGreen = SuccessGreen
val TerminalRed = ErrorRed
val BorderColor = PrimaryOrange.copy(alpha = 0.2f)
val SecondaryText = Graphite400

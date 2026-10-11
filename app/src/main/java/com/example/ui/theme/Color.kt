package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Required Core Brand Palette (Aurora Glass Light Theme)
val PureWhiteBackground = Color(0xFFFFFFFF)     // Main background: #FFFFFF
val BrandPrimaryOrange = Color(0xFFFF8A00)      // Primary button background: #FF8A00
val BrandTextBlack = Color(0xFF000000)          // Primary text & icons on orange: #000000
val LightSurface = Color(0xFFFFFFFF)            // Card backgrounds: #FFFFFF
val LightBorder = Color(0xFFEAEAEA)             // Borders: #EAEAEA
val BrandTextSecondary = Color(0xFF555555)      // Secondary text: #555555
val SoftDecorativePeach = Color(0xFFFFF0E0)     // Soft decorative peach: #FFF0E0

// Translucent Glass Surfaces
val GlassSurface = Color(0xEEFFFFFF)            // Translucent glass white
val GlassSurfaceSubtle = Color(0xD9FFFFFF)      // Translucent high-glass white
val GlassBorder = Color(0xFFEAEAEA)             // Delicate border
val GlassHighlight = Color(0x66FFFFFF)          // Specular reflection highlight

// Aurora Ambient Glow Colors
val AuroraOrangeGlow = Color(0xFFFF8A00).copy(alpha = 0.12f)
val AuroraPeachGlow = Color(0xFFFFF0E0).copy(alpha = 0.85f)
val AuroraSoftAmber = Color(0xFFFFB74D).copy(alpha = 0.15f)

// Standard Secondary Palette
val LightSurfaceElevated = Color(0xFFFAFAFA)
val BrandTextInactive = Color(0xFF666666)
val BrandError = Color(0xFFDC2626)
val BrandSuccess = Color(0xFF16A34A)

// Code Block Specific (Dedicated dark editor theme)
val CodeBlockBackground = Color(0xFF18181B)
val CodeBlockText = Color(0xFFF4F4F5)
val CodeBlockHeader = Color(0xFF27272A)

// Compatibility Aliases
val PrimaryOrange = BrandPrimaryOrange
val DeepOrange = Color(0xFFE65100)
val AccentOrange = Color(0xFFFF9233)
val AltronAmber = Color(0xFFFFB300)

val Graphite950 = CodeBlockBackground
val Graphite900 = Color(0xFF202023)
val Graphite850 = Color(0xFF27272A)
val Graphite800 = Color(0xFF3F3F46)
val Graphite700 = LightBorder
val Graphite400 = BrandTextSecondary

val WhiteBase = PureWhiteBackground
val WhiteSurface = LightSurface
val WhiteSecondary = LightSurfaceElevated
val WhiteText = BrandTextBlack
val BorderColor = LightBorder
val SecondaryText = BrandTextSecondary

val GraphiteWhite = BrandTextBlack
val Graphite100 = BrandTextBlack
val Graphite200 = BrandTextSecondary

val SuccessGreen = BrandSuccess
val ErrorRed = BrandError
val TerminalCyan = Color(0xFF0891B2)
val TerminalYellow = Color(0xFFD97706)

val AltronOrange = BrandPrimaryOrange
val AltronOrangeBright = Color(0xFFFF9233)
val AltronOrangeGlow = BrandPrimaryOrange.copy(alpha = 0.18f)
val AltronOrangeRim = BrandPrimaryOrange
val TerminalGreen = BrandSuccess
val TerminalRed = BrandError

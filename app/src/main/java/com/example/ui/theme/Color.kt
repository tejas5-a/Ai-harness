package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Stitch & Claude Warm Palette Design Tokens
val StitchMainBackground = Color(0xFFFAF9F6)     // MAIN BACKGROUND: #FAF9F6
val StitchSecondaryBackground = Color(0xFFF2F0EB) // SECONDARY BACKGROUND: #F2F0EB
val StitchCardBackground = Color(0xFFFFFFFF)      // CARD BACKGROUND: #FFFFFF
val StitchPrimaryText = Color(0xFF292824)         // PRIMARY TEXT: #292824
val StitchSecondaryText = Color(0xFF77746D)       // SECONDARY TEXT: #77746D
val StitchPrimaryAccent = Color(0xFFD97757)       // PRIMARY ACCENT: #D97757 (Terracotta)
val StitchPressedAccent = Color(0xFFBF6245)       // PRESSED ACCENT: #BF6245
val StitchSoftAccent = Color(0xFFF7E8E0)          // SOFT ACCENT: #F7E8E0
val StitchBorder = Color(0xFFE7E3DC)              // BORDER: #E7E3DC
val StitchSuccess = Color(0xFF548568)             // SUCCESS: #548568
val StitchWarning = Color(0xFFC18B43)             // WARNING: #C18B43
val StitchError = Color(0xFFC65B52)               // ERROR: #C65B52

// Dark Mode Tokens
val StitchDarkBackground = Color(0xFF20201E)
val StitchDarkCards = Color(0xFF292926)
val StitchDarkElevated = Color(0xFF32312D)
val StitchDarkPrimaryText = Color(0xFFF4F1EA)
val StitchDarkSecondaryText = Color(0xFFB4B0A7)
val StitchDarkAccent = Color(0xFFE18B69)
val StitchDarkBorders = Color(0xFF41403B)

// Core Brand Palette mapped to Stitch Tokens
val PureWhiteBackground = StitchMainBackground
val BrandPrimaryOrange = StitchPrimaryAccent
val BrandTextBlack = StitchPrimaryText
val LightSurface = StitchCardBackground
val LightBorder = StitchBorder
val BrandTextSecondary = StitchSecondaryText
val SoftDecorativePeach = StitchSoftAccent

// Translucent Glass Surfaces
val GlassSurface = Color(0xEEFAF9F6)
val GlassSurfaceSubtle = Color(0xD9FAF9F6)
val GlassBorder = StitchBorder
val GlassHighlight = Color(0x66FFFFFF)

// Aurora Ambient Glow Colors
val AuroraOrangeGlow = StitchPrimaryAccent.copy(alpha = 0.12f)
val AuroraPeachGlow = StitchSoftAccent.copy(alpha = 0.85f)
val AuroraSoftAmber = StitchWarning.copy(alpha = 0.15f)

// Standard Secondary Palette
val LightSurfaceElevated = StitchSecondaryBackground
val BrandTextInactive = StitchSecondaryText
val BrandError = StitchError
val BrandSuccess = StitchSuccess

// Code Block Specific (Dedicated dark editor theme)
val CodeBlockBackground = Color(0xFF1E1E1C)
val CodeBlockText = Color(0xFFF4F1EA)
val CodeBlockHeader = Color(0xFF282825)

// Compatibility Aliases
val PrimaryOrange = BrandPrimaryOrange
val DeepOrange = StitchPressedAccent
val AccentOrange = StitchPrimaryAccent
val AltronAmber = StitchWarning

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
val TerminalYellow = StitchWarning

val AltronOrange = BrandPrimaryOrange
val AltronOrangeBright = StitchPrimaryAccent
val AltronOrangeGlow = StitchPrimaryAccent.copy(alpha = 0.18f)
val AltronOrangeRim = StitchPrimaryAccent
val TerminalGreen = StitchSuccess
val TerminalRed = StitchError

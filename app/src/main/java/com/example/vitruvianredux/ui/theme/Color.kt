package com.example.vitruvianredux.ui.theme

import androidx.compose.ui.graphics.Color

// Core mono palette for dark mode.
val MonoBlack = Color(0xFF050505)
val MonoSurface = Color(0xFF101010)
val MonoSurfaceHigh = Color(0xFF191919)
val MonoSurfaceHighest = Color(0xFF242424)
val MonoOutline = Color(0xFF343434)
val MonoOutlineSubtle = Color(0xFF272727)
val MonoTextPrimary = Color(0xFFEDEDED)
val MonoTextSecondary = Color(0xFFB8B8B8)
val MonoTextMuted = Color(0xFF858585)

// Clean neutral palette for light mode.
val LightBackground = Color(0xFFFAFAF9)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceHigh = Color(0xFFF5F5F4)
val LightSurfaceHighest = Color(0xFFEDEDED)
val LightOutline = Color(0xFFD6D3D1)
val LightOutlineSubtle = Color(0xFFE7E5E4)
val LightTextPrimary = Color(0xFF171717)
val LightTextSecondary = Color(0xFF57534E)

// Light-mode brand/action pop.
val PrimaryOrange = Color(0xFFF97316)
val PrimaryOrangeActive = Color(0xFFEA580C)
val PrimaryOrangeContainer = Color(0xFFFFEDD5)

// Semantic status roles.
val SuccessGreen = Color(0xFF22C55E)
val SuccessGreenContainer = Color(0xFF14532D)
val SuccessGreenContainerLight = Color(0xFFDCFCE7)
val WarningAmber = Color(0xFFF59E0B)
val WarningAmberContainer = Color(0xFF78350F)
val WarningAmberContainerLight = Color(0xFFFEF3C7)
val ErrorRed = Color(0xFFEF4444)
val ErrorRedContainer = Color(0xFF7F1D1D)
val ErrorRedContainerLight = Color(0xFFFEE2E2)
val InfoBlue = Color(0xFF3B82F6)
val InfoBlueContainer = Color(0xFF1E3A8A)
val InfoBlueContainerLight = Color(0xFFDBEAFE)

// Muted data visualization colors. These are intentionally warmer and more
// neutral than the old purple/blue/teal app identity.
val ChartGold = Color(0xFFEAB308)
val ChartGoldDark = Color(0xFFCA8A04)
val ChartCopper = Color(0xFFB45309)
val ChartCopperDark = Color(0xFF92400E)
val ChartSlate = Color(0xFF94A3B8)
val ChartSlateDark = Color(0xFF64748B)
val ChartStone = Color(0xFFA8A29E)
val ChartStoneDark = Color(0xFF78716C)

// Compatibility aliases. Keep old names compiling while routing them to the
// current mono/orange system instead of preserving the old purple/teal identity.
val BackgroundBlack = MonoBlack
val BackgroundDarkGrey = MonoSurface
val SurfaceDarkGrey = MonoSurface
val CardBackground = MonoSurfaceHigh

val ColorLightBackground = LightBackground
val ColorOnLightBackground = LightTextPrimary
val ColorLightSurface = LightSurface
val ColorOnLightSurface = LightTextPrimary
val ColorLightSurfaceVariant = LightSurfaceHigh
val ColorOnLightSurfaceVariant = LightTextSecondary

val PrimaryPurpleDark = MonoTextPrimary
val SecondaryPurpleDark = MonoTextSecondary
val TertiaryPurpleDark = MonoTextMuted
val PurpleAccentDark = MonoTextPrimary

val PrimaryBlueLight = PrimaryOrange
val SecondaryBlueLight = PrimaryOrangeActive
val TertiaryBlueLight = PrimaryOrangeContainer
val BlueAccentLight = PrimaryOrange

val TopAppBarDark = MonoBlack
val TopAppBarLight = LightSurface

val TextPrimary = MonoTextPrimary
val TextSecondary = MonoTextSecondary
val TextTertiary = MonoTextMuted
val TextDisabled = MonoTextMuted

val WarningOrange = WarningAmber

val Purple80 = PrimaryPurpleDark
val PurpleGrey80 = SecondaryPurpleDark
val Pink80 = TertiaryPurpleDark
val Purple40 = PurpleAccentDark
val PurpleGrey40 = MonoTextSecondary
val Pink40 = PrimaryOrange

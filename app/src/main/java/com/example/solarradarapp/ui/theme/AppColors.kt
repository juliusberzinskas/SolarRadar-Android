package com.example.solarradarapp.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class AppColors(
    val backgroundScreen: Color,
    val backgroundCard: Color,
    val backgroundTopBar: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val primaryBlue: Color,
    val divider: Color,
    val isDark: Boolean = false,
)

val LightAppColors = AppColors(
    backgroundScreen = Color(0xFFF0F4F8),
    backgroundCard = Color.White,
    backgroundTopBar = Color.White,
    textPrimary = Color(0xFF0F172A),
    textSecondary = Color(0xFF64748B),
    primaryBlue = Color(0xFF3B82F6),
    divider = Color(0x14000000),   // rgba(0,0,0,0.08)
    isDark = false,
)

val DarkAppColors = AppColors(
    backgroundScreen = Color(0xFF0F172A),
    backgroundCard = Color(0xFF1E293B),
    backgroundTopBar = Color(0xFF1E293B),
    textPrimary = Color(0xFFE2E8F0),
    textSecondary = Color(0xFF94A3B8),
    primaryBlue = Color(0xFF60A5FA),
    divider = Color(0x14FFFFFF),   // rgba(255,255,255,0.08)
    isDark = true,
)

val LocalAppColors = staticCompositionLocalOf { LightAppColors }

package com.example.weather_app

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

data class WeatherColors(
    val cardBackground: Color,
    val pageBackground: Color,
    val labelColor:     Color,
    val valueColor:     Color,
    val historyBg:      Color
)

val LightColors = WeatherColors(
    cardBackground = Color(0xFF00E5FF),
    pageBackground = Color(0xFFF5E6E6),
    labelColor     = Color(0xFF002222),
    valueColor     = Color(0xFF003333),
    historyBg      = Color(0xFFE0F7FA)
)

val DarkColors = WeatherColors(
    cardBackground = Color(0xFF006064),
    pageBackground = Color(0xFF121212),
    labelColor     = Color(0xFF80DEEA),
    valueColor     = Color(0xFFE0F7FA),
    historyBg      = Color(0xFF1E1E1E)
)

val LocalWeatherColors = compositionLocalOf { LightColors }
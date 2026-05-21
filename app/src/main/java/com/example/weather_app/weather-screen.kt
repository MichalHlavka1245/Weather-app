package com.example.weather_app

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val SUPPORTED_LANGUAGES = listOf(
    "en" to "🇬🇧 English",
    "cs" to "🇨🇿 Čeština",
    "sk" to "🇸🇰 Slovenčina",
    "de" to "\uD83C\uDDE9\uD83C\uDDEA Deutsch"
)

fun countryCodeToFlag(countryCode: String): String {
    return countryCode
        .uppercase()
        .map { char -> String(Character.toChars(char.code + 0x1F1A5)) }
        .joinToString("")
}

@Composable
fun LanguageDropdownButton(onLanguageChange: (String) -> Unit) {
    val colors   = LocalWeatherColors.current
    var expanded by remember { mutableStateOf(false) }

    Box(contentAlignment = Alignment.TopEnd) {
        OutlinedButton(
            onClick = { expanded = true },
            shape   = RoundedCornerShape(12.dp)
        ) {
            Text(text = stringResource(R.string.change_language), fontSize = 14.sp)
        }
        DropdownMenu(
            expanded         = expanded,
            onDismissRequest = { expanded = false },
            modifier         = Modifier.background(colors.pageBackground)
        ) {
            SUPPORTED_LANGUAGES.forEach { (tag, displayName) ->
                DropdownMenuItem(
                    text    = { Text(displayName, color = colors.labelColor) },
                    onClick = { expanded = false; onLanguageChange(tag) }
                )
            }
        }
    }
}

@Composable
fun DarkModeButton(isDark: Boolean, onToggle: () -> Unit) {
    val colors = LocalWeatherColors.current

    OutlinedButton(
        onClick = onToggle,
        shape   = RoundedCornerShape(12.dp)
    ) {
        Text(
            text     = if (isDark) "☀️ ${stringResource(R.string.light_mode)}"
            else        "🌙 ${stringResource(R.string.dark_mode)}",
            fontSize = 14.sp,
            color    = colors.labelColor
        )
    }
}

@Composable
fun WeatherScreen(
    weatherData: Weather?,
    isLoading: Boolean,
    errorMessage: String?,
    cityInput: String,
    countryInput: String,
    searchedCity: String,
    searchHistory: List<String>,
    isDarkMode: Boolean,
    onCityInputChange: (String) -> Unit,
    onCountryInputChange: (String) -> Unit,
    onHistoryClick: (String) -> Unit,
    onSearch: () -> Unit,
    onLanguageChange: (String) -> Unit,
    onToggleDarkMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors              = LocalWeatherColors.current
    val allowedCharsRegex   = remember { Regex("^[\\p{L}\\s-]*$") }
    val allowedCountryRegex = remember { Regex("^[a-zA-Z]*$") }
    val configuration       = LocalConfiguration.current
    val isLandscape         = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    // ── Custom color configurations driven by your theme mappings ───────────
    val textFieldColors = OutlinedTextFieldDefaults.colors(
        // Text inside input: Forces exact pure white in Dark Mode, drops back to theme values in Light Mode
        focusedTextColor     = if (isDarkMode) Color.White else colors.valueColor,
        unfocusedTextColor   = if (isDarkMode) Color.White else colors.valueColor,

        // Borders mapping: Light blue (0xFF90CAF9) vs. traditional light-mode primary layouts
        focusedBorderColor   = if (isDarkMode) Color(0xFF90CAF9) else colors.cardBackground,
        unfocusedBorderColor = if (isDarkMode) Color(0xFF90CAF9).copy(alpha = 0.6f) else colors.labelColor.copy(alpha = 0.5f),

        // Animated background label configurations
        focusedLabelColor    = if (isDarkMode) Color(0xFF90CAF9) else colors.valueColor,
        unfocusedLabelColor  = if (isDarkMode) Color.White.copy(alpha = 0.6f) else colors.labelColor
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.pageBackground)
            .padding(if (isLandscape) 8.dp else 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // ── Top button row ────────────────────────────────────────────────────
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            DarkModeButton(isDark = isDarkMode, onToggle = onToggleDarkMode)
            LanguageDropdownButton(onLanguageChange = onLanguageChange)
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = cityInput,
            onValueChange = { input ->
                if (input.length <= 50 && input.matches(allowedCharsRegex))
                    onCityInputChange(input)
            },
            label    = { Text(stringResource(R.string.enter_city)) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction    = ImeAction.Next
            ),
            singleLine = true,
            colors     = textFieldColors
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = countryInput,
            onValueChange = { input ->
                if (input.length <= 2 && input.matches(allowedCountryRegex))
                    onCountryInputChange(input.uppercase())
            },
            label    = { Text(stringResource(R.string.country_code)) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction    = ImeAction.Search
            ),
            keyboardActions = KeyboardActions(onSearch = { onSearch() }),
            singleLine = true,
            colors     = textFieldColors
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = { onSearch() }, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.show_weather))
        }

        // ── Last searched ─────────────────────────────────────────────────────
        if (searchHistory.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text       = stringResource(R.string.last_searched),
                fontSize   = 16.sp,
                fontWeight = FontWeight.Bold,
                color      = colors.labelColor
            )
            Spacer(modifier = Modifier.height(8.dp))
            searchHistory.forEach { entry ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .background(colors.historyBg, RoundedCornerShape(12.dp))
                        .border(1.dp, colors.cardBackground, RoundedCornerShape(12.dp))
                        .clickable { onHistoryClick(entry) }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "🔍 $entry", fontSize = 16.sp, color = colors.valueColor, modifier = Modifier.weight(1f))
                    Text(text = "↗",         fontSize = 18.sp, color = colors.labelColor)
                }
            }
        }

        Spacer(modifier = Modifier.height(if (isLandscape) 10.dp else 20.dp))

        when {
            isLoading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                Spacer(modifier = Modifier.height(8.dp))
                Text(stringResource(R.string.loading), modifier = Modifier.align(Alignment.CenterHorizontally))
            }
            errorMessage != null -> Text(text = errorMessage, color = Color.Red)
            weatherData  != null -> WeatherGrid(weather = weatherData, cityName = searchedCity, isLandscape = isLandscape)
        }
    }
}
package com.example.weather_app

import android.app.LocaleManager
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.LocaleList
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.Saver
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import com.example.weather_app.ui.theme.WeatherappTheme
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.Locale

class MainActivity : ComponentActivity() {

    // ── 1. Intercept context creation to apply our saved language ───────────
    override fun attachBaseContext(newBase: Context) {
        val sharedPrefs = newBase.getSharedPreferences("settings", MODE_PRIVATE)
        val localeTag = sharedPrefs.getString("locale_tag", "") ?: ""

        if (localeTag.isNotEmpty()) {
            val locale = Locale.forLanguageTag(localeTag)
            Locale.setDefault(locale)

            val config = Configuration(newBase.resources.configuration)
            config.setLocale(locale)

            // Creates a localized context that ComponentActivity will safely use
            val localizedContext = newBase.createConfigurationContext(config)
            super.attachBaseContext(localizedContext)
        } else {
            super.attachBaseContext(newBase)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            WeatherappTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->

                    val weatherSaver = Saver<Weather?, String>(
                        save    = { weather -> if (weather != null) Json.encodeToString(weather) else "" },
                        restore = { str     -> if (str.isNotEmpty()) Json.decodeFromString(str) else null }
                    )

                    var weatherData   by rememberSaveable(stateSaver = weatherSaver) { mutableStateOf<Weather?>(null) }
                    var isLoading     by rememberSaveable { mutableStateOf(false) }
                    var errorMessage  by rememberSaveable { mutableStateOf<String?>(null) }
                    var cityInput     by rememberSaveable { mutableStateOf("") }
                    var searchedCity  by rememberSaveable { mutableStateOf("") }
                    var countryInput  by rememberSaveable { mutableStateOf("") }
                    var searchHistory by rememberSaveable { mutableStateOf(listOf<String>()) }
                    var isDarkMode    by rememberSaveable { mutableStateOf(false) }

                    val coroutineScope     = rememberCoroutineScope()
                    val keyboardController = LocalSoftwareKeyboardController.current

                    fun search() {
                        if (cityInput.isNotBlank()) {
                            keyboardController?.hide()
                            coroutineScope.launch {
                                isLoading    = true
                                errorMessage = null
                                try {
                                    weatherData  = WeatherRepository.getCleanWeatherData(cityInput, countryInput)
                                    searchedCity = cityInput
                                    val entry = if (countryInput.isNotBlank()) "$cityInput, $countryInput" else cityInput
                                    searchHistory = (listOf(entry) + searchHistory).distinct().take(5)
                                } catch (e: Exception) {
                                    errorMessage = if (e.message == "City not found") {
                                        getString(R.string.error_city_not_found, cityInput)
                                    } else {
                                        getString(R.string.error_no_internet)
                                    }
                                    weatherData = null
                                } finally {
                                    isLoading = false
                                }
                            }
                        }
                    }

                    val currentColors = if (isDarkMode) DarkColors else LightColors

                    CompositionLocalProvider(LocalWeatherColors provides currentColors) {
                        WeatherScreen(
                            weatherData          = weatherData,
                            isLoading            = isLoading,
                            errorMessage         = errorMessage,
                            cityInput            = cityInput,
                            countryInput         = countryInput,
                            searchedCity         = searchedCity,
                            searchHistory        = searchHistory,
                            isDarkMode           = isDarkMode,
                            onCityInputChange    = { cityInput = it },
                            onCountryInputChange = { countryInput = it },
                            onHistoryClick       = { entry ->
                                val parts = entry.split(", ")
                                cityInput    = parts[0]
                                countryInput = if (parts.size > 1) parts[1] else ""
                            },
                            onSearch             = { search() },

                            // ── 2. Updated target action execution ─────────────────
                            onLanguageChange     = { localeTag ->
                                // Save selection permanently
                                getSharedPreferences("settings", MODE_PRIVATE)
                                    .edit()
                                    .putString("locale_tag", localeTag)
                                    .apply()

                                // Support system integrations on API 33+ (Android 13)
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    val localeManager = getSystemService(LocaleManager::class.java)
                                    localeManager?.applicationLocales = LocaleList.forLanguageTags(localeTag)
                                }

                                recreate()
                            },
                            onToggleDarkMode     = { isDarkMode = !isDarkMode },
                            modifier             = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        }
    }
}
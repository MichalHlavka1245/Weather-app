package com.example.weather_app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun translateSkyState(apiSkyString: String?): String {
    if (apiSkyString.isNullOrBlank()) {
        return stringResource(R.string.na)
    }

    val stringResId = when (apiSkyString.lowercase().trim()) {

        // Thunderstorm
        "thunderstorm with light rain" -> R.string.thunderstorm_with_light_rain
        "thunderstorm with rain" -> R.string.thunderstorm_with_rain
        "thunderstorm with heavy rain" -> R.string.thunderstorm_with_heavy_rain
        "light thunderstorm" -> R.string.light_thunderstorm
        "thunderstorm" -> R.string.thunderstorm
        "heavy thunderstorm" -> R.string.heavy_thunderstorm
        "ragged thunderstorm" -> R.string.ragged_thunderstorm
        "thunderstorm with light drizzle" -> R.string.thunderstorm_with_light_drizzle
        "thunderstorm with drizzle" -> R.string.thunderstorm_with_drizzle
        "thunderstorm with heavy drizzle" -> R.string.thunderstorm_with_heavy_drizzle

        // Drizzle
        "light intensity drizzle" -> R.string.light_intensity_drizzle
        "drizzle" -> R.string.drizzle
        "heavy intensity drizzle" -> R.string.heavy_intensity_drizzle
        "light intensity drizzle rain" -> R.string.light_intensity_drizzle_rain
        "drizzle rain" -> R.string.drizzle_rain
        "heavy intensity drizzle rain" -> R.string.heavy_intensity_drizzle_rain
        "shower rain and drizzle" -> R.string.shower_rain_and_drizzle
        "heavy shower rain and drizzle" -> R.string.heavy_shower_rain_and_drizzle
        "shower drizzle" -> R.string.shower_drizzle

        // Rain
        "light rain" -> R.string.light_rain
        "moderate rain" -> R.string.moderate_rain
        "heavy intensity rain",
        "heavy rain" -> R.string.heavy_intensity_rain
        "very heavy rain" -> R.string.very_heavy_rain
        "extreme rain" -> R.string.extreme_rain
        "freezing rain" -> R.string.freezing_rain
        "light intensity shower rain" -> R.string.light_intensity_shower_rain
        "shower rain" -> R.string.shower_rain
        "heavy intensity shower rain" -> R.string.heavy_intensity_shower_rain
        "ragged shower rain" -> R.string.ragged_shower_rain

        // Snow
        "light snow" -> R.string.light_snow
        "snow" -> R.string.snow
        "heavy snow" -> R.string.heavy_snow
        "sleet" -> R.string.sleet
        "light shower sleet" -> R.string.light_shower_sleet
        "shower sleet" -> R.string.shower_sleet
        "light rain and snow" -> R.string.light_rain_and_snow
        "rain and snow" -> R.string.rain_and_snow
        "light shower snow" -> R.string.light_shower_snow
        "shower snow" -> R.string.shower_snow
        "heavy shower snow" -> R.string.heavy_shower_snow

        // Atmosphere
        "mist" -> R.string.mist
        "smoke" -> R.string.smoke
        "haze" -> R.string.haze
        "sand/dust whirls" -> R.string.sand_dust_whirls
        "fog" -> R.string.fog
        "sand" -> R.string.sand
        "dust" -> R.string.dust
        "volcanic ash" -> R.string.volcanic_ash
        "squalls" -> R.string.squalls
        "tornado" -> R.string.tornado

        // Clear / Clouds
        "clear sky" -> R.string.clear_sky
        "few clouds" -> R.string.few_clouds
        "scattered clouds" -> R.string.scattered_clouds
        "broken clouds" -> R.string.broken_clouds
        "overcast clouds" -> R.string.overcast_clouds

        else -> null
    }

    return stringResId?.let { stringResource(it) }
        ?: apiSkyString.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase() else it.toString()
        }
}



@Composable
fun WeatherGrid(weather: Weather, cityName: String, isLandscape: Boolean = false) {
    val colors = LocalWeatherColors.current


    val formattedCityName = cityName.split(" ").joinToString(" ") { word ->
        word.replaceFirstChar { it.uppercase() }
    }

    Column {
        Text(
            text       = "${stringResource(R.string.weather_in)} $formattedCityName ${countryCodeToFlag(weather.country)}",
            fontSize   = if (isLandscape) 16.sp else 22.sp,
            fontWeight = FontWeight.Bold,
            color      = colors.labelColor,
            modifier   = Modifier.padding(bottom = if (isLandscape) 8.dp else 16.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(if (isLandscape) 8.dp else 14.dp)
        ) {
            WeatherCard(label = stringResource(R.string.temperature), value = "${weather.temperature} °C", modifier = Modifier.weight(1f), isLandscape = isLandscape)
            WeatherCard(label = stringResource(R.string.humidity),    value = "${weather.humidity} %",      modifier = Modifier.weight(1f), isLandscape = isLandscape)
        }

        Spacer(modifier = Modifier.height(if (isLandscape) 6.dp else 12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(if (isLandscape) 8.dp else 14.dp)
        ) {
            WeatherCard(label = stringResource(R.string.pressure), value = "${weather.pressure} HPa", modifier = Modifier.weight(1f), isLandscape = isLandscape)
            CoordinationCard(lon = weather.lon, lat = weather.lat,                                     modifier = Modifier.weight(1f), isLandscape = isLandscape)
        }

        Spacer(modifier = Modifier.height(if (isLandscape) 6.dp else 12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(if (isLandscape) 8.dp else 14.dp)
        ) {
            WeatherCard(label = stringResource(R.string.wind_speed), value = "${weather.wind_speed} m/s", modifier = Modifier.weight(1f), isLandscape = isLandscape)

            WeatherCard(
                label = stringResource(R.string.sky),
                value = translateSkyState(weather.sky),
                modifier = Modifier.weight(1f),
                isLandscape = isLandscape
            )
        }
    }
}

@Composable
fun CoordinationCard(lon: Double, lat: Double, modifier: Modifier = Modifier, isLandscape: Boolean = false) {
    val colors     = LocalWeatherColors.current
    val lonRounded = String.format("%.4f", lon)
    val latRounded = String.format("%.4f", lat)

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .background(colors.cardBackground, RoundedCornerShape(16.dp))
            .padding(if (isLandscape) 8.dp else 16.dp)
    ) {
        Text(
            text       = stringResource(R.string.coordination),
            fontSize   = if (isLandscape) 50.sp else 20.sp,
            lineHeight = if (isLandscape) 65.sp else 22.sp,
            fontWeight = FontWeight.Bold,
            color      = colors.labelColor,
            modifier   = Modifier.align(Alignment.TopStart)
        )
        Column(
            modifier            = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "\n"
            )
            Text(
                text       = "${stringResource(R.string.lon)} $lonRounded",
                fontSize   = if (isLandscape) 50.sp else 20.sp,
                lineHeight = if (isLandscape) 65.sp else 22.sp,
                fontWeight = FontWeight.Bold,
                color      = colors.valueColor
            )
            Spacer(modifier = Modifier.height(if (isLandscape) 4.dp else 8.dp))
            Text(
                text       = "${stringResource(R.string.lan)} $latRounded",
                fontSize   = if (isLandscape) 50.sp else 20.sp,
                lineHeight = if (isLandscape) 65.sp else 22.sp,
                fontWeight = FontWeight.Bold,
                color      = colors.valueColor
            )
        }
    }
}

@Composable
fun WeatherCard(label: String, value: String, modifier: Modifier = Modifier, isLandscape: Boolean = false) {
    val colors = LocalWeatherColors.current

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .background(colors.cardBackground, RoundedCornerShape(16.dp))
            .padding(if (isLandscape) 8.dp else 16.dp)
    ) {
        Text(
            text       = label,
            fontSize   = if (isLandscape) 55.sp else 20.sp,
            lineHeight = if (isLandscape) 65.sp else 22.sp,
            fontWeight = FontWeight.Bold,
            color      = colors.labelColor,
            modifier   = Modifier.align(Alignment.TopStart)
        )
        Text(
            text       = value,
            fontSize   = if (isLandscape) 60.sp else 28.sp,
            lineHeight = if (isLandscape) 65.sp else 30.sp,
            fontWeight = FontWeight.Bold,
            color      = colors.valueColor,
            modifier   = Modifier.align(Alignment.Center)
        )
    }
}
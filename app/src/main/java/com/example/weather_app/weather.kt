package com.example.weather_app
import kotlinx.serialization.Serializable

@Serializable
data class Weather(
    val lat: Double,
    val lon: Double,
    val temperature: Double,
    val humidity: Int,
    val pressure: Int,
    val wind_speed:Double ,
    val sky : String ,
    val country: String
)


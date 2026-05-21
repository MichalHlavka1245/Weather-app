package com.example.weather_app

import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.double
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray

object WeatherRepository {

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    private val jsonParser = Json { ignoreUnknownKeys = true }

    suspend fun getCleanWeatherData(city: String, country: String): Weather {

        val query = if (country.isNotBlank()) "$city,$country" else city

        val url = "https://api.openweathermap.org/data/2.5/weather?q=$query&appid=PUT_HERE_YOUR_API_KEY&units=metric"

        val response: HttpResponse = client.get(url)

        if (response.status != HttpStatusCode.OK) {
            throw Exception("City not found")
        }

        val responseBody: String = response.bodyAsText()

        val jsonRoot      = jsonParser.parseToJsonElement(responseBody).jsonObject
        val coordObject   = jsonRoot["coord"]?.jsonObject
        val mainObject    = jsonRoot["main"]?.jsonObject
        val windObject    = jsonRoot["wind"]?.jsonObject
        val weatherArray  = jsonRoot["weather"]?.jsonArray
        val weatherObject = weatherArray?.get(0)?.jsonObject
        val sysObject     = jsonRoot["sys"]?.jsonObject

        return Weather(
            lat         = coordObject?.get("lat")?.jsonPrimitive?.double ?: 0.0,
            lon         = coordObject?.get("lon")?.jsonPrimitive?.double ?: 0.0,
            temperature = mainObject?.get("temp")?.jsonPrimitive?.double ?: 0.0,
            humidity    = mainObject?.get("humidity")?.jsonPrimitive?.int ?: 0,
            pressure    = mainObject?.get("pressure")?.jsonPrimitive?.int ?: 0,
            wind_speed  = windObject?.get("speed")?.jsonPrimitive?.double ?: 0.0,
            sky         = weatherObject?.get("description")?.jsonPrimitive?.content ?: "",
            country     = sysObject?.get("country")?.jsonPrimitive?.content ?: ""
        )
    }
}
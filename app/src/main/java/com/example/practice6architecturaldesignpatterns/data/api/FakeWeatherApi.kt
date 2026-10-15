package com.example.practice6architecturaldesignpatterns.data.api

import com.example.practice6architecturaldesignpatterns.data.model.Weather
import kotlinx.coroutines.delay

class FakeWeatherApi(private val latencyMs: Long = 1_200) : WeatherApi {
    private val dataBase = mapOf(
        "Tashkent" to Weather("Tashkent", 24, "Sunny"),
        "Samarkand" to Weather("Samarkand", 21, "Cloudy"),
        "Bukhara" to Weather("Bukhara", 27, "Windy"),
    )

    override suspend fun fetchWeather(city: String): Weather {
        delay(latencyMs)
        return dataBase[city] ?: throw IllegalArgumentException("No forecast for $city")
    }
}

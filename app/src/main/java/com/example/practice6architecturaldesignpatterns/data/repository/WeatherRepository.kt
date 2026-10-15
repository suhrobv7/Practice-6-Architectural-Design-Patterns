package com.example.practice6architecturaldesignpatterns.data.repository

import com.example.practice6architecturaldesignpatterns.data.api.WeatherApi
import com.example.practice6architecturaldesignpatterns.data.model.Weather

class WeatherRepository(private val api: WeatherApi) {
    suspend fun getWeather(city: String): Result<Weather> {
        return try {
            val weather = api.fetchWeather(city)
            Result.success(weather)
        } catch (e: IllegalArgumentException) {
            Result.failure(e)
        }
    }
}

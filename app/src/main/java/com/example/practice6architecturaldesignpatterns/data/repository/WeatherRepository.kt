package com.example.practice6architecturaldesignpatterns.data.repository

import com.example.practice6architecturaldesignpatterns.data.api.WeatherApi
import com.example.practice6architecturaldesignpatterns.data.model.Weather

class WeatherRepository(private val dataSource: WeatherApi) {
    suspend fun getWeather(location: String): Result<Weather> {
        return if (location.isBlank()) {
            Result.failure(IllegalArgumentException("Invalid location"))
        } else {
            try {
                val data = dataSource.fetchWeather(location)
                Result.success(data)
            } catch (ex: IllegalArgumentException) {
                Result.failure(ex)
            }
        }
    }
}

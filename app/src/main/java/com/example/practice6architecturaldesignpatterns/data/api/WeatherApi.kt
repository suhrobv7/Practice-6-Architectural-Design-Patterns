package com.example.practice6architecturaldesignpatterns.data.api

import com.example.practice6architecturaldesignpatterns.data.model.Weather

interface WeatherApi {
    suspend fun fetchWeather(city: String): Weather
}

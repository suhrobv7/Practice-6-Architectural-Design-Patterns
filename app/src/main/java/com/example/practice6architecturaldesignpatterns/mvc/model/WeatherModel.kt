package com.example.practice6architecturaldesignpatterns.mvc.model

import com.example.practice6architecturaldesignpatterns.data.model.Weather
import com.example.practice6architecturaldesignpatterns.data.repository.WeatherRepository

class WeatherModel(private val weatherRepo: WeatherRepository) {
    var loading: Boolean = false
        private set
    var currentWeather: Weather? = null
        private set
    var errorMessage: String? = null
        private set

    fun beginLoading() {
        loading = true
        errorMessage = null
    }

    suspend fun fetch(city: String) {
        val res = weatherRepo.getWeather(city)
        res.onSuccess {
            currentWeather = it
        }.onFailure {
            errorMessage = it.message ?: "Error"
        }
        loading = false
    }
}

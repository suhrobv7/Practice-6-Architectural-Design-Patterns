package com.example.practice6architecturaldesignpatterns.mvp

import com.example.practice6architecturaldesignpatterns.data.model.Weather

interface WeatherContract {
    interface View {
        fun showLoading()
        fun showWeather(weather: Weather)
        fun showError(message: String)
    }
}

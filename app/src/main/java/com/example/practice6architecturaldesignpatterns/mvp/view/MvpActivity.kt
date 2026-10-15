package com.example.practice6architecturaldesignpatterns.mvp.view

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.practice6architecturaldesignpatterns.data.model.Weather
import com.example.practice6architecturaldesignpatterns.di.WeatherModule
import com.example.practice6architecturaldesignpatterns.mvp.WeatherContract
import com.example.practice6architecturaldesignpatterns.mvp.presenter.WeatherPresenter
import com.example.practice6architecturaldesignpatterns.ui.WeatherScreen

class MvpActivity : ComponentActivity(), WeatherContract.View {
    private var status by mutableStateOf("Pick a city")
    private val presenter = WeatherPresenter(
        view = this,
        repository = WeatherModule.repository,
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MVP", "onCreate   activity=${hashCode()}")
        setContent {
            MaterialTheme {
                WeatherScreen(status = status, onCitySelected = presenter::onCitySelected)
            }
        }
    }

    override fun onDestroy() {
        Log.d("MVP", "onDestroy  activity=${hashCode()}")
        presenter.destroy()
        super.onDestroy()
    }

    override fun showLoading() { status = "Loading…" }

    override fun showWeather(weather: Weather) {
        Log.d("MVP", "showWeather called on activity=${hashCode()}")
        status = "${weather.city}: ${weather.temperatureC}°C, ${weather.condition}"
    }

    override fun showError(message: String) { status = "Error: $message" }
}

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
    private var uiText by mutableStateOf("Select a location")
    private val presenter = WeatherPresenter(
        viewRef = this,
        repo = WeatherModule.repository,
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MVP", "onCreate activity=${hashCode()}")
        setContent {
            MaterialTheme {
                WeatherScreen(status = uiText, onCitySelected = presenter::selectCity)
            }
        }
    }

    override fun onDestroy() {
        Log.d("MVP", "onDestroy activity=${hashCode()}")
        presenter.destroy()
        super.onDestroy()
    }

    override fun showLoading() {
        uiText = "Loading data..."
    }

    override fun showWeather(weather: Weather) {
        Log.d("MVP", "showWeather called on activity=${hashCode()}")
        uiText = "${weather.city}: ${weather.temperatureC}°C, ${weather.condition}"
    }

    override fun showError(message: String) {
        uiText = "Error: $message"
    }
}

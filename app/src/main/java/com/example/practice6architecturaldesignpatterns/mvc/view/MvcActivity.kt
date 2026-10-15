package com.example.practice6architecturaldesignpatterns.mvc.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.practice6architecturaldesignpatterns.di.WeatherModule
import com.example.practice6architecturaldesignpatterns.mvc.controller.WeatherController
import com.example.practice6architecturaldesignpatterns.mvc.model.WeatherModel
import com.example.practice6architecturaldesignpatterns.ui.WeatherScreen

class MvcActivity : ComponentActivity(), WeatherView {
    private val model = WeatherModel(WeatherModule.repository)
    private val controller = WeatherController(model, mView = this)
    private var uiStatus by mutableStateOf("Select a location")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                WeatherScreen(status = uiStatus, onCitySelected = controller::selectCity)
            }
        }
    }

    override fun render() {
        uiStatus = when {
            model.loading -> "Loading data..."
            model.errorMessage != null -> "Error: ${model.errorMessage}"
            model.currentWeather != null -> {
                val item = model.currentWeather!!
                "${item.city}: ${item.temperatureC}°C, ${item.condition}"
            }
            else -> "Select a location"
        }
    }
}

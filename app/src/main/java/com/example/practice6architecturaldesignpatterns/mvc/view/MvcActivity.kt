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
    private val controller = WeatherController(model, view = this)
    private var status by mutableStateOf("Pick a city")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                WeatherScreen(status = status, onCitySelected = controller::onCitySelected)
            }
        }
    }

    override fun render() {
        status = when {
            model.isLoading -> "Loading…"
            model.error != null -> "Error: ${model.error}"
            model.weather != null -> {
                val w = model.weather!!
                "${w.city}: ${w.temperatureC}°C, ${w.condition}"
            }
            else -> "Pick a city"
        }
    }
}

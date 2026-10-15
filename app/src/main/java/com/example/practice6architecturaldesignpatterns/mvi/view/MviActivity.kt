package com.example.practice6architecturaldesignpatterns.mvi.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.practice6architecturaldesignpatterns.di.WeatherModule
import com.example.practice6architecturaldesignpatterns.mvi.model.WeatherIntent
import com.example.practice6architecturaldesignpatterns.mvi.model.WeatherState
import com.example.practice6architecturaldesignpatterns.mvi.viewmodel.WeatherMviViewModel
import com.example.practice6architecturaldesignpatterns.ui.WeatherScreen

class MviActivity : ComponentActivity() {
    private val viewModel: WeatherMviViewModel by viewModels {
        WeatherModule.weatherMviViewModelFactory
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                val currentState by viewModel.uiState.collectAsStateWithLifecycle()
                WeatherScreen(
                    status = currentState.convertToText(),
                    onCitySelected = { cityName ->
                        viewModel.processIntent(WeatherIntent.SelectCity(cityName))
                    },
                )
            }
        }
    }
}

private fun WeatherState.convertToText(): String = when {
    isLoading -> "Loading data..."
    error != null -> "Error: $error"
    weather != null -> "${weather.city}: ${weather.temperatureC}°C, ${weather.condition}"
    else -> "Select a location"
}

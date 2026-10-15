package com.example.practice6architecturaldesignpatterns.mvvm.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.practice6architecturaldesignpatterns.di.WeatherModule
import com.example.practice6architecturaldesignpatterns.mvvm.viewmodel.WeatherUiState
import com.example.practice6architecturaldesignpatterns.mvvm.viewmodel.WeatherViewModel
import com.example.practice6architecturaldesignpatterns.ui.WeatherScreen

class MvvmActivity : ComponentActivity() {
    private val viewModel: WeatherViewModel by viewModels {
        WeatherModule.weatherViewModelFactory
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                val currentUiState by viewModel.stateFlow.collectAsStateWithLifecycle()
                WeatherScreen(
                    status = currentUiState.convertToStatusText(),
                    onCitySelected = viewModel::selectCity,
                )
            }
        }
    }
}

private fun WeatherUiState.convertToStatusText(): String = when (this) {
    is WeatherUiState.Idle -> "Select a location"
    is WeatherUiState.Loading -> "Loading data..."
    is WeatherUiState.Success -> "${weather.city}: ${weather.temperatureC}°C, ${weather.condition}"
    is WeatherUiState.Error -> "Error: $message"
}

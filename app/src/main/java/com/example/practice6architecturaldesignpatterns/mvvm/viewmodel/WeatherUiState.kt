package com.example.practice6architecturaldesignpatterns.mvvm.viewmodel

import com.example.practice6architecturaldesignpatterns.data.model.Weather

sealed interface WeatherUiState {
    data object Idle : WeatherUiState
    data object Loading : WeatherUiState
    data class Success(val weather: Weather) : WeatherUiState
    data class Error(val message: String) : WeatherUiState
}

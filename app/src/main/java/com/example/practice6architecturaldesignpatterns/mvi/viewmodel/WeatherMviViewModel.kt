package com.example.practice6architecturaldesignpatterns.mvi.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.practice6architecturaldesignpatterns.data.repository.WeatherRepository
import com.example.practice6architecturaldesignpatterns.mvi.model.WeatherIntent
import com.example.practice6architecturaldesignpatterns.mvi.model.WeatherState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class WeatherMviViewModel(private val weatherRepository: WeatherRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(WeatherState())
    val uiState: StateFlow<WeatherState> = _uiState.asStateFlow()

    fun processIntent(intent: WeatherIntent) {
        when (intent) {
            is WeatherIntent.SelectCity -> loadWeather(intent.city)
            WeatherIntent.DismissError -> {
                _uiState.update { current -> current.copy(error = null) }
            }
        }
    }

    private fun loadWeather(city: String) {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            val res = weatherRepository.getWeather(city)
            res.onSuccess { data ->
                _uiState.update { it.copy(isLoading = false, weather = data, error = null) }
            }.onFailure { err ->
                _uiState.update { it.copy(isLoading = false, error = err.message ?: "Error") }
            }
        }
    }
}

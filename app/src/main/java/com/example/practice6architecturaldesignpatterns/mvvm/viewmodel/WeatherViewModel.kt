package com.example.practice6architecturaldesignpatterns.mvvm.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.practice6architecturaldesignpatterns.data.repository.WeatherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WeatherViewModel(private val repository: WeatherRepository) : ViewModel() {
    private val _uiState = MutableStateFlow<WeatherUiState>(WeatherUiState.Idle)
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    fun onCitySelected(city: String) {
        _uiState.value = WeatherUiState.Loading
        viewModelScope.launch {
            val result = repository.getWeather(city)
            result.onSuccess { weather ->
                _uiState.value = WeatherUiState.Success(weather)
            }.onFailure { e ->
                _uiState.value = WeatherUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
}

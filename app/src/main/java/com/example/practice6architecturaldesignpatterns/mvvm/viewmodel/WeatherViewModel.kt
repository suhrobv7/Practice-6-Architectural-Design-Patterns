package com.example.practice6architecturaldesignpatterns.mvvm.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.practice6architecturaldesignpatterns.data.repository.WeatherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WeatherViewModel(private val repository: WeatherRepository) : ViewModel() {
    private val _stateFlow = MutableStateFlow<WeatherUiState>(WeatherUiState.Idle)
    val stateFlow: StateFlow<WeatherUiState> = _stateFlow.asStateFlow()

    fun selectCity(city: String) {
        _stateFlow.value = WeatherUiState.Loading
        viewModelScope.launch {
            val response = repository.getWeather(city)
            if (response.isSuccess) {
                _stateFlow.value = WeatherUiState.Success(response.getOrThrow())
            } else {
                _stateFlow.value = WeatherUiState.Error(response.exceptionOrNull()?.message ?: "Error")
            }
        }
    }
}

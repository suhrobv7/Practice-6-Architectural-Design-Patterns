package com.example.practice6architecturaldesignpatterns.di

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.practice6architecturaldesignpatterns.data.api.FakeWeatherApi
import com.example.practice6architecturaldesignpatterns.data.repository.WeatherRepository
import com.example.practice6architecturaldesignpatterns.mvi.viewmodel.WeatherMviViewModel
import com.example.practice6architecturaldesignpatterns.mvvm.viewmodel.WeatherViewModel

object WeatherModule {
    val repository = WeatherRepository(FakeWeatherApi(latencyMs = 3_000))

    val weatherViewModelFactory: ViewModelProvider.Factory = viewModelFactory {
        initializer { WeatherViewModel(repository) }
    }

    val weatherMviViewModelFactory: ViewModelProvider.Factory = viewModelFactory {
        initializer { WeatherMviViewModel(repository) }
    }
}

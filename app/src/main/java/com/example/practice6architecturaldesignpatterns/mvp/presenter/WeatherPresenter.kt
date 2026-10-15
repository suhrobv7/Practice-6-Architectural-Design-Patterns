package com.example.practice6architecturaldesignpatterns.mvp.presenter

import com.example.practice6architecturaldesignpatterns.data.repository.WeatherRepository
import com.example.practice6architecturaldesignpatterns.mvp.WeatherContract
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class WeatherPresenter(
    private val view: WeatherContract.View,
    private val repository: WeatherRepository,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main),
) {
    fun onCitySelected(city: String) {
        view.showLoading()
        scope.launch {
            val result = repository.getWeather(city)
            result.onSuccess { weather ->
                view.showWeather(weather)
            }.onFailure { e ->
                view.showError(e.message ?: "Unknown error")
            }
        }
    }

    fun destroy() {
        scope.coroutineContext[kotlinx.coroutines.Job]?.cancel()
    }
}

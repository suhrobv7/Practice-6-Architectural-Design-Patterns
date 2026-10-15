package com.example.practice6architecturaldesignpatterns.mvp.presenter

import com.example.practice6architecturaldesignpatterns.data.repository.WeatherRepository
import com.example.practice6architecturaldesignpatterns.mvp.WeatherContract
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class WeatherPresenter(
    private val viewRef: WeatherContract.View,
    private val repo: WeatherRepository,
    private val jobScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
) {
    fun selectCity(cityName: String) {
        viewRef.showLoading()
        jobScope.launch {
            val res = repo.getWeather(cityName)
            if (res.isSuccess) {
                viewRef.showWeather(res.getOrThrow())
            } else {
                viewRef.showError(res.exceptionOrNull()?.message ?: "Failed")
            }
        }
    }

    fun destroy() {
        jobScope.coroutineContext[kotlinx.coroutines.Job]?.cancel()
    }
}

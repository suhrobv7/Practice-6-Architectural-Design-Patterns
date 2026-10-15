package com.example.practice6architecturaldesignpatterns.mvc.controller

import com.example.practice6architecturaldesignpatterns.mvc.model.WeatherModel
import com.example.practice6architecturaldesignpatterns.mvc.view.WeatherView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class WeatherController(
    private val model: WeatherModel,
    private val view: WeatherView,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main),
) {
    fun onCitySelected(city: String) {
        scope.launch {
            model.startLoading()
            view.render()
            model.load(city)
            view.render()
        }
    }
}

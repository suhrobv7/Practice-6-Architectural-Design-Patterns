package com.example.practice6architecturaldesignpatterns.mvc.controller

import com.example.practice6architecturaldesignpatterns.mvc.model.WeatherModel
import com.example.practice6architecturaldesignpatterns.mvc.view.WeatherView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class WeatherController(
    private val mModel: WeatherModel,
    private val mView: WeatherView,
    private val backgroundScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
) {
    fun selectCity(name: String) {
        backgroundScope.launch {
            mModel.beginLoading()
            mView.render()
            mModel.fetch(name)
            mView.render()
        }
    }
}
